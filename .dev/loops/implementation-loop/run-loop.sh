#!/usr/bin/env bash
# implementation-loop 驅動腳本：跟 spec-migration-loop／ui-authoring-loop 不同，
# 這是「平行、worktree 隔離」的驅動方式——每輪巡視任務清單，把所有依賴已滿足
# 的 todo 任務各自丟進一個 worktree，最多同時跑 MAX_PARALLEL 條 Dev→Review 管線。
#
# 規則詳見 prompts/iteration-prompt.md 第 4 節「平行執行模型」。
# 任務狀態放在 .state/tasks/<id>/status（一個任務一個單行檔），各種紀錄也都在
# .state/tasks/<id>/ 底下：一個 worktree 只寫自己任務的目錄，合併回整合分支就
# 不會跟別條管線衝突（2026-09-18 之前共用 tasks.md／decision-log.md 等檔，並行
# 合併必衝突、流水號撞號）。.state/tasks.md 只剩靜態欄位，執行階段沒有人寫它。
# 主 repo 的 git 操作（狀態 commit、merge）一律用 MERGE_LOCK 序列化。

set -euo pipefail

LOOP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$LOOP_DIR/../../.." && pwd)"
cd "$REPO_ROOT"

LOOP=".dev/loops/implementation-loop"
PROMPTS="$LOOP/prompts"
STATE="$LOOP/.state"
RUNTIME="$LOOP/runtime"
GATES="$RUNTIME/gates"
LOGS="$RUNTIME/logs"
LEDGER="$STATE/tasks.md"
TASKS_DIR="$STATE/tasks"
PLANNING_PROMPT="$PROMPTS/planning-prompt.md"
DEV_PROMPT="$PROMPTS/dev-prompt.md"
REVIEW_PROMPT="$PROMPTS/review-prompt.md"
VERIFY="$LOOP/scripts/verify.sh"
DONE_FILE="$RUNTIME/DONE"
MERGE_LOCK="$RUNTIME/tmp/merge.lock"

MAX_ITERATIONS="${MAX_ITERATIONS:-200}"
MAX_PARALLEL="${MAX_PARALLEL:-5}"
MAX_TASK_ROUNDS="${MAX_TASK_ROUNDS:-6}"
MODEL="${MODEL:-claude-sonnet-5}"
EFFORT="${EFFORT:-medium}"
REVIEW_MODEL="${REVIEW_MODEL:-claude-opus-5}"
REVIEW_EFFORT="${REVIEW_EFFORT:-medium}"
PLANNING_MODEL="${PLANNING_MODEL:-claude-opus-5}"
SUMMARY_MODEL="${SUMMARY_MODEL:-claude-haiku-4-5-20251001}"
SUMMARY_EFFORT="${SUMMARY_EFFORT:-medium}"
ROUND_TIMEOUT="${ROUND_TIMEOUT:-45m}"
LOOP_BRANCH="${LOOP_BRANCH:-loop/implementation}"
WORKTREE_ROOT="${WORKTREE_ROOT:-$(cd .. && pwd)}"

mkdir -p "$GATES" "$LOGS" "$RUNTIME/tmp"

# 驅動腳本自己的輸出也落檔（之前只印在終端機，事後沒辦法 review 巡視過程）
exec > >(tee -a "$LOGS/driver-$(date +%Y%m%d-%H%M%S).log") 2>&1

# 硬性關掉背景任務：Dev／Review 都是一次性 `claude -p` 行程，背景工作等於直接遺棄。
# 提示詞早就禁止了，但實際跑過仍有 Dev 輪用 Monitor／run_in_background 等通知，
# 105 個 turn 零 commit 就結束（見 lesson-learned），所以改用工具層級擋掉。
export CLAUDE_CODE_DISABLE_BACKGROUND_TASKS=1

log() { echo "[$(date '+%F %T')] $*"; }

# ---------- 事前檢查 ----------
for cmd in claude git flock timeout; do
  command -v "$cmd" > /dev/null || { echo "缺少指令：$cmd" >&2; exit 1; }
done
if [ -n "$(git status --porcelain)" ]; then
  echo "工作區不乾淨，請先 commit 或處理後再啟動 loop。" >&2
  exit 1
fi
branch="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$branch" =~ ^(main|master)$ ]] && [ "${ALLOW_MAIN:-0}" != 1 ]; then
  if git show-ref --verify --quiet "refs/heads/$LOOP_BRANCH"; then
    git switch "$LOOP_BRANCH"
    log "已從 $branch 切換到既有分支 $LOOP_BRANCH（接續上次進度）。"
  else
    git switch -c "$LOOP_BRANCH"
    log "已從 $branch 建立並切換到分支 $LOOP_BRANCH。"
  fi
fi

# ---------- 任務清單／狀態存取 ----------
# 任務清單上所有任務 ID（一行一個，依清單順序）
task_ids() {
  grep -E '^\| T-[0-9A-Za-z-]+ \|' "$LEDGER" | awk -F'|' '{gsub(/^ +| +$/,"",$2); print $2}'
}

# 讀任務 $2 的狀態；$1 是 repo 根目錄（主 repo 傳 "."，worktree 傳 "$wt"）。
# 合併回整合分支之前，Dev／Review 改的是 worktree 自己那份，主 repo 看不到，
# 所以管線內判斷核准與否一定要傳 "$wt"。狀態檔不存在＝todo。
task_status() {
  local root="$1" id="$2" f
  f="$root/$TASKS_DIR/$id/status"
  if [ -s "$f" ]; then tr -d ' \r\n' < "$f"; else echo todo; fi
}

# 主 repo 裡狀態為 $1 的任務 ID 清單
tasks_with_status() {
  local id
  for id in $(task_ids); do
    [ "$(task_status . "$id")" = "$1" ] && echo "$id"
  done
  return 0
}

# 把任務 $1 的狀態改成 $2，並直接在主 repo commit，讓主 worktree 保持乾淨（之前
# 只改檔不 commit，別條管線合併時被「local changes would be overwritten」擋掉）。
# 只給「不是靠合併帶進來的狀態變更」用（開始執行、worktree 建立失敗、達回合上限、
# 合併失敗、啟動時清孤兒）。標 blocked 時順便把原因記在 tasks/<id>/driver-note.md
# （只有驅動腳本寫這個檔，worktree 不碰，不會衝突），人工與 verify.sh 才看得到為什麼。
# 用 MERGE_LOCK 跟合併步驟共用同一把鎖，兩條並行管線
# 才不會同時對主 repo 的 git index 下手。
commit_task_status() {
  local id="$1" new="$2" msg="$3"
  flock "$MERGE_LOCK" bash -c "
    mkdir -p '$TASKS_DIR/$id' &&
    echo '$new' > '$TASKS_DIR/$id/status' &&
    { [ '$new' != blocked ] || echo '- $(date '+%F %T') 驅動腳本標 blocked：$msg' >> '$TASKS_DIR/$id/driver-note.md'; } &&
    git add '$TASKS_DIR/$id' &&
    git commit -q -m '[docs](loops) $msg'
  " > /dev/null 2>&1 || true
}

# 任務 $1 的依賴 ID 清單（任務清單第 3 欄）
task_deps() {
  awk -F'|' -v id="$1" '$0 ~ "^\\| "id" \\|" {print $4}' "$LEDGER" | grep -oE 'T-[0-9A-Za-z-]+' || true
}

# 依賴是否全部 done 且已合併。
# 注意：不能只看 git merge-base --is-ancestor——worktree 剛建立、分支還沒有新 commit 時，
# impl/<dep> 跟整合分支指向同一個 commit，--is-ancestor 會直接回傳 true。必須先看狀態
# 是否真的是 done（Review 核准才會寫入），再用 git 確認那個 commit 真的已經進了整合
# 分支歷史，兩者都成立才算依賴滿足。
deps_satisfied() {
  local dep
  for dep in $(task_deps "$1"); do
    [ "$(task_status . "$dep")" = done ] || return 1
    git merge-base --is-ancestor "impl/$dep" "$LOOP_BRANCH" 2> /dev/null || return 1
  done
  return 0
}

# 以 Haiku 產生本輪主要任務的一句話說明；失敗時退回簡短的任務/輪次描述，不影響 loop
summarize_round() {
  local id="$1" mode="$2" round="$3" wt="$4" data summary
  data="$(
    echo "## 任務"; grep -E "^\| $id \|" "$LEDGER"
    echo "## 模式"; echo "$mode 輪 #$round"
    echo "## 最近的決策紀錄"; tail -20 "$wt/$TASKS_DIR/$id/decision-log.md" 2> /dev/null || echo "（尚無）"
  )"
  summary="$(timeout 2m claude -p "你是 loop 監控員。根據以下資料，用固定格式的一句繁體中文（60 字以內）說明這一輪 iteration 的主要任務：
「把 [對象] 從 [現狀] 變成 [目標狀態]。」
[對象] 是這個任務的 Aggregate／畫面群組或這輪具體要動的東西；[現狀] 與 [目標狀態] 依 Dev／Review 輪而定——Dev 輪通常是「尚未實作／未通過的部分」變成「已實作並通過本輪要做的部分」，Review 輪通常是「review-pending、未驗證」變成「已驗證通過（或已退回並附 D-xx）」。只輸出這一句話，照格式填空，不要其他文字，不要附加說明。

$data" --model "$SUMMARY_MODEL" --effort "$SUMMARY_EFFORT" --tools "" --output-format text 2> /dev/null | tr -d '\r' | sed '/^\s*$/d' | head -1)" || summary=""
  if [ -z "$summary" ]; then
    summary="把 $id 從（摘要產生失敗，見任務清單）變成本輪目標狀態。"
  fi
  echo "$summary"
}

# ---------- 跑一輪 Dev 或 Review ----------
# 參數：id wt 角色(Dev|Review) 回合數 附註 提示詞檔 模型 effort log檔
# prompt 開頭把「任務 ID／回合／角色」明講：agent 是 zero-context 的一次性行程，
# 不可能自己知道現在第幾輪（以前 Review 只能從紀錄裡猜）。第一行固定是
# 「任務 ID：<id>」後面直接換行——啟動時的孤兒偵測（pgrep）靠這個樣式，不要改。
run_agent() {
  local id="$1" wt="$2" role="$3" round="$4" note="$5" prompt_file="$6" model="$7" effort="$8" out="$9"
  local header rc
  header="任務 ID：$id
回合：第 $round 輪（上限 MAX_TASK_ROUNDS=$MAX_TASK_ROUNDS）
角色：$role"
  [ -n "$note" ] && header="$header
驅動腳本附註：$note"
  set +e
  (cd "$wt" && timeout --foreground "$ROUND_TIMEOUT" claude -p \
    "$header

$(cat "$prompt_file")" \
    --model "$model" --effort "$effort" \
    --output-format stream-json --verbose \
    --dangerously-skip-permissions \
    --disallowedTools Monitor) >> "$out" 2>&1
  rc=$?
  set -e
  return "$rc"
}

# ---------- 單一任務的 Dev→Review 管線 ----------
run_pipeline() {
  local id="$1"
  local wt="$WORKTREE_ROOT/kanban2-impl-$id"
  local branch="impl/$id"
  local pipe_log="$LOGS/pipeline-$id-$(date +%Y%m%d-%H%M%S).log"

  # 自我修復：任務清單上這個任務還是 todo（不是 done，還沒合併），但 worktree／分支
  # 已經存在——代表上一次執行被中斷過（人工中止、機器重開等），留下沒做完的殘留。
  # 因為還沒核准、沒合併，這些殘留本來就不是「完成的成果」，直接清掉重新開始是安全的；
  # 不清掉的話下面的 `git worktree add -b` 會直接失敗（worktree／分支已存在）。
  if [ -d "$wt" ] || git show-ref --verify --quiet "refs/heads/$branch"; then
    log "[$id] 偵測到殘留 worktree／分支（上次執行中斷），先清掉再重新開始"
    git worktree remove "$wt" --force >> "$pipe_log" 2>&1 || true
    flock "$MERGE_LOCK" git branch -D "$branch" >> "$pipe_log" 2>&1 || true
  fi

  log "[$id] 建立 worktree $wt（分支 $branch）"
  commit_task_status "$id" doing "$id 開始執行，狀態改為 doing"
  if ! git worktree add "$wt" -b "$branch" "$LOOP_BRANCH" >> "$pipe_log" 2>&1; then
    log "[$id] worktree 建立失敗，任務標 blocked，見 $pipe_log"
    commit_task_status "$id" blocked "worktree 建立失敗，$id 標 blocked"
    return
  fi

  local round=0 note="" status head_before head_after
  while true; do
    round=$((round + 1))
    if [ "$round" -gt "$MAX_TASK_ROUNDS" ]; then
      log "[$id] 已達 MAX_TASK_ROUNDS=$MAX_TASK_ROUNDS 仍未核准，標 blocked，保留 worktree 供人工檢查。"
      commit_task_status "$id" blocked "$id 達 MAX_TASK_ROUNDS=$MAX_TASK_ROUNDS 仍未核准，標 blocked"
      return
    fi

    log "[$id] 本輪任務：$(summarize_round "$id" dev "$round" "$wt")"
    log "[$id] Dev 輪 #$round"
    head_before="$(git -C "$wt" rev-parse HEAD)"
    run_agent "$id" "$wt" Dev "$round" "$note" "$DEV_PROMPT" "$MODEL" "$EFFORT" "$pipe_log" \
      || log "[$id] Dev 輪回傳非 0（見 $pipe_log）。"
    note=""
    head_after="$(git -C "$wt" rev-parse HEAD)"

    # Dev 這一輪完全沒有新 commit（實際發生過：整輪都在等背景任務通知）——沒有東西
    # 可審，不要花一輪 Review（較貴的模型）去確認「Dev 什麼都沒交」，直接算一輪重跑 Dev。
    if [ "$head_before" = "$head_after" ]; then
      log "[$id] Dev 輪 #$round 沒有任何新 commit，跳過 Review，直接進下一輪 Dev。"
      note="上一輪（第 $round 輪）Dev 結束時沒有任何新 commit，等於沒有交出東西。這一輪務必依「收尾」逐項做完並 commit；worktree 內若有上一輪留下未 commit 的改動，先檢查能不能沿用。"
      continue
    fi
    status="$(task_status "$wt" "$id")"
    [ "$status" = review-pending ] || log "[$id] 警告：Dev 輪結束但 status=$status（應為 review-pending），仍送 Review。"

    log "[$id] 本輪任務：$(summarize_round "$id" review "$round" "$wt")"
    log "[$id] Review 輪 #$round"
    run_agent "$id" "$wt" Review "$round" "" "$REVIEW_PROMPT" "$REVIEW_MODEL" "$REVIEW_EFFORT" "$pipe_log" \
      || log "[$id] Review 輪回傳非 0（見 $pipe_log）。"
    status="$(task_status "$wt" "$id")"

    # Review 漏改 status（仍是 review-pending）：不要因此多跑一輪 Dev，只補跑一次 Review。
    if [ "$status" = review-pending ]; then
      log "[$id] Review 輪 #$round 結束但 status 仍是 review-pending（沒有寫下判定），補跑一次 Review。"
      run_agent "$id" "$wt" Review "$round" \
        "上一次第 $round 輪 Review 結束時，.state/tasks/$id/status 仍是 review-pending——沒有寫下判定。先讀 .state/tasks/$id/review.md 看上一次是否已留下紀錄，沿用已驗證的結果，把判定確實寫進 status（done／doing／blocked 三選一）並 commit。" \
        "$REVIEW_PROMPT" "$REVIEW_MODEL" "$REVIEW_EFFORT" "$pipe_log" \
        || log "[$id] 補跑的 Review 回傳非 0（見 $pipe_log）。"
      status="$(task_status "$wt" "$id")"
    fi

    if [ "$status" = done ] || [ "$status" = blocked ]; then
      log "[$id] worktree 內狀態為 $status，嘗試合併回 $LOOP_BRANCH（保留紀錄與 commit 歷史）"
      # 一定要真的檢查合併是否成功（以前用 "... || true" 吞掉失敗，成果沒進整合分支卻
      # 照樣刪 worktree）。合併失敗一定要 `git merge --abort`：以前沒有 abort，主 repo 被
      # 留在「合併到一半」，之後所有管線的 commit／merge 全部跟著壞。主 worktree 固定停在
      # 整合分支上，不在的話代表有人動過，直接當失敗、不替它切分支。
      set +e
      flock "$MERGE_LOCK" bash -c "
        [ \"\$(git rev-parse --abbrev-ref HEAD)\" = '$LOOP_BRANCH' ] || { echo '主 worktree 不在 $LOOP_BRANCH 上，不合併'; exit 1; }
        git merge --no-ff '$branch' -m '[dev] 合併任務 $id（狀態：$status）' && exit 0
        echo '合併失敗，執行 git merge --abort 還原主 repo'
        git merge --abort
        exit 1
      " >> "$pipe_log" 2>&1
      merge_status=$?
      set -e

      if [ "$merge_status" -ne 0 ]; then
        log "[$id] 合併失敗（見 $pipe_log；已 merge --abort，主 repo 維持乾淨），worktree 與分支都保留，標 blocked 交人工處理。"
        commit_task_status "$id" blocked "$id worktree 內已判定 $status，但合併回 $LOOP_BRANCH 失敗，需人工介入"
        return
      fi

      if [ "$status" = done ]; then
        git worktree remove "$wt" --force >> "$pipe_log" 2>&1 || true
        log "[$id] 完成並已移除 worktree。"
      else
        log "[$id] 狀態為 blocked（agent 自行判斷），已合併紀錄但保留 worktree 供人工檢查。"
      fi
      return
    fi
    # 其餘情況（Review 退回成 doing）進下一輪 Dev
    log "[$id] 尚未核准，狀態=$status，進入下一輪。"
  done
}

# ---------- 啟動時自我修復：清掉上次執行中斷留下的孤兒任務 ----------
# 這個腳本執行個體剛啟動，PIDS 還是空的，不可能是「這次執行」自己派出去的
# doing——一定是上次執行中斷（人工中止、機器重開等）留下的孤兒。保險起見，
# 不能只憑任務清單狀態就認定是孤兒：用 ps 核對這個任務是否真的還有對應的
# `claude -p` 行程在跑（每次呼叫都會在指令列帶上「任務 ID：<id>」這個字串，
# 見 run_pipeline），真的還在跑就跳過，不誤殺別的執行個體或還沒收尾的工作。
for id in $(tasks_with_status doing); do
  # run_pipeline 組 prompt 時 "任務 ID：$id" 後面接的其實是換行字元，但 ps／pgrep
  # 讀 /proc/<pid>/cmdline 顯示與比對時，會把 argv 裡的換行等控制字元換成空白
  # （用 `ps -o args` 或 `pgrep -fa` 實測確認過），所以這裡要配「空白」結尾，
  # 不是換行；有結尾符號才能避免「T-01-be-user」誤配到未來若新增了
  # 「T-01-be-user-xxx」這種前綴重疊的 ID。
  if pgrep -f "任務 ID：${id} " > /dev/null 2>&1; then
    log "[$id] 狀態為 doing，且偵測到對應行程仍在執行，不動它（可能是另一個 loop 執行個體）"
    continue
  fi
  log "[$id] 狀態為 doing 但找不到對應行程，判定是上次執行中斷留下的孤兒，清掉殘留並改回 todo"
  wt="$WORKTREE_ROOT/kanban2-impl-$id"
  branch="impl/$id"
  git worktree remove "$wt" --force > /dev/null 2>&1 || true
  flock "$MERGE_LOCK" git branch -D "$branch" > /dev/null 2>&1 || true
  commit_task_status "$id" todo "$id 啟動時偵測到孤兒（doing 但無對應行程），清掉殘留並改回 todo"
done

# ---------- 安排階段（只跑一次；任務清單已存在種子資料則略過，除非 FORCE_PLANNING=1）----------
if [ "${FORCE_PLANNING:-0}" = 1 ] || [ ! -s "$LEDGER" ]; then
  log "執行安排階段（planning）"
  timeout --foreground "$ROUND_TIMEOUT" claude -p "$(cat "$PLANNING_PROMPT")" \
    --model "$PLANNING_MODEL" --effort medium \
    --output-format stream-json --verbose \
    --dangerously-skip-permissions > "$LOGS/planning-$(date +%Y%m%d-%H%M%S).log" 2>&1 || true
fi

# ---------- 主迴圈：有管線啟動／結束才巡視一次、補滿並行名額 ----------
# 不用 sleep 輪詢：`wait -n` 會睡到任一條管線結束才醒，所以「本輪並行管線數」這類
# 訊息只會在狀態真的有變化（管線啟動或結束）之後出現一次，不會每 60 秒洗版。
# i 計的是「巡視次數」＝管線結束事件數＋1。
i=0
declare -A PIDS=()
while true; do
  i=$((i + 1))

  if [ -f "$DONE_FILE" ]; then
    log "偵測到 $DONE_FILE，實作完成，停止迴圈。"
    break
  fi
  if [ "$i" -gt "$MAX_ITERATIONS" ]; then
    log "已達 MAX_ITERATIONS=$MAX_ITERATIONS，停止迴圈（尚未完成，請人工檢查）。"
    break
  fi

  # 清掉已結束的背景管線
  for id in "${!PIDS[@]}"; do
    if ! kill -0 "${PIDS[$id]}" 2> /dev/null; then
      wait "${PIDS[$id]}" 2> /dev/null || true
      unset 'PIDS[$id]'
      log "管線結束：$id（狀態：$(task_status . "$id")）"
    fi
  done

  slots=$((MAX_PARALLEL - ${#PIDS[@]}))
  for id in $(tasks_with_status todo); do
    [ "$slots" -le 0 ] && break
    # 防護：背景 job 還沒來得及把狀態 commit 成 doing 時，避免同一個任務被派兩條管線
    [ -n "${PIDS[$id]+x}" ] && continue
    if deps_satisfied "$id"; then
      log "啟動管線：$id"
      run_pipeline "$id" &
      PIDS["$id"]=$!
      slots=$((slots - 1))
    fi
  done

  if [ "${#PIDS[@]}" -eq 0 ]; then
    remaining="$(tasks_with_status todo)"
    if [ -z "$remaining" ]; then
      log "沒有可執行或進行中的任務，停止迴圈。請用 scripts/collect.sh status 看 blocked 任務，scripts/collect.sh oq 看待決事項。"
    else
      # 沒有任何管線在跑、剩下的 todo 依賴又都沒滿足＝死結：不會再有任何事件改變狀態，
      # 等下去沒有意義（以前每 30 秒重印一次同一句話，直到 MAX_ITERATIONS）。
      log "有 todo 任務但依賴尚未滿足，且沒有進行中的管線（死結），停止迴圈。卡住的依賴："
      for id in $remaining; do
        for dep in $(task_deps "$id"); do
          st="$(task_status . "$dep")"
          [ "$st" = done ] || log "  $id ← $dep（$st）"
        done
      done
    fi
    break
  fi

  log "並行管線數：${#PIDS[@]}（$(echo "${!PIDS[@]}" | sed 's/ /、/g')），等任一管線結束後再巡視"
  wait -n "${PIDS[@]}" 2> /dev/null || true
done

log "等待所有背景管線結束"
for id in "${!PIDS[@]}"; do wait "${PIDS[$id]}" 2> /dev/null || true; done

bash "$VERIFY" || log "verify.sh 回報非 0，請人工檢查 $RUNTIME/last-verify.md"
