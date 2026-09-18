#!/usr/bin/env bash
# implementation-loop 驅動腳本：跟 spec-migration-loop／ui-authoring-loop 不同，
# 這是「平行、worktree 隔離」的驅動方式——每輪巡視任務清單，把所有依賴已滿足
# 的 todo 任務各自丟進一個 worktree，最多同時跑 MAX_PARALLEL 條 Dev→Review 管線。
#
# 規則詳見 prompts/iteration-prompt.md 第 4 節「平行執行模型」。
# 任務清單的讀寫用 flock 序列化，避免多條管線同時寫 .state/tasks.md
# 或同時 merge 回整合分支造成衝突。

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
DECISION_LOG="$STATE/decision-log.md"
REVIEW_LOG="$STATE/review.md"
PLANNING_PROMPT="$PROMPTS/planning-prompt.md"
DEV_PROMPT="$PROMPTS/dev-prompt.md"
REVIEW_PROMPT="$PROMPTS/review-prompt.md"
VERIFY="$LOOP/scripts/verify.sh"
DONE_FILE="$RUNTIME/DONE"
LEDGER_LOCK="$RUNTIME/tmp/tasks.lock"
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

# ---------- 任務清單存取（flock 序列化）----------
# 讀出目前狀態為 $1 的任務 ID 清單（一行一個），只依賴 grep，不假設有 python 工具腳本
tasks_with_status() {
  flock "$LEDGER_LOCK" grep -E "^\| T-[0-9A-Za-z-]+ \|.*\| $1 \|" "$LEDGER" | awk -F'|' '{gsub(/^ +| +$/,"",$2); print $2}'
}

# 把任務 $1 的狀態改成 $2（簡單字串替換，任務清單格式固定為一行一個 T-xx 列）；
# 只改主 repo 這份檔案，不 commit——單純標記用（doing marker），供 tasks_with_status／
# deps_satisfied 這些「掃描 todo／依賴是否已合併」的判斷排除掉正在跑的任務。
set_task_status() {
  local id="$1" new="$2"
  flock "$LEDGER_LOCK" bash -c "
    sed -i -E 's/^(\| $id \|.*\| )[a-z-]+( \|)/\1$new\2/' '$LEDGER'
  "
}

# 把任務 $1 的狀態改成 $2，並直接在主 repo commit 這個異動，讓主 worktree 的
# working tree 保持乾淨（其他無人值守流程／下一次啟動的「git status 必須乾淨」
# 前置檢查才不會被這種標記型異動卡住）。只給「不是靠合併帶進來的狀態變更」用
# （worktree 建立失敗、撞到 MAX_TASK_ROUNDS 上限）。
#
# 用 MERGE_LOCK（不是 LEDGER_LOCK）序列化，因為這裡的 git add／commit 動的是主
# repo 的 git 狀態，要跟合併步驟（也用 MERGE_LOCK）用同一把鎖，兩條並行管線才
# 不會同時對主 repo 的 git index／working tree 下手，避免 index.lock 衝突或半個
# commit 的狀態。
commit_ledger_status() {
  local id="$1" new="$2" msg="$3"
  flock "$MERGE_LOCK" bash -c "
    sed -i -E 's/^(\| $id \|.*\| )[a-z-]+( \|)/\1$new\2/' '$LEDGER' &&
    git add '$LEDGER' &&
    git commit -q -m '[docs](loops) $msg'
  " > /dev/null 2>&1 || true
}

# 依賴是否全部 done 且已合併。
# 注意：不能只看 git merge-base --is-ancestor——worktree 剛建立、分支還沒有新 commit 時，
# impl/<dep> 跟整合分支指向同一個 commit，--is-ancestor 會直接回傳 true（同一個 commit
# 互為祖先），造成「分支存在」就被誤判成「已合併完成」。必須先看任務清單狀態是否真的是
# done（Review 核准才會寫入），再用 git 確認那個 commit 真的已經進了整合分支歷史，兩者都
# 成立才算依賴滿足。
deps_satisfied() {
  local id="$1"
  local deps dep dep_status
  deps="$(flock "$LEDGER_LOCK" awk -F'|' -v id="$id" '$0 ~ "^\\| "id" \\|" {print $4}' "$LEDGER" | grep -oE 'T-[0-9A-Za-z-]+')"
  for dep in $deps; do
    dep_status="$(flock "$LEDGER_LOCK" awk -F'|' -v d="$dep" '$0 ~ "^\\| "d" \\|" {gsub(/^ +| +$/,"",$5); print $5}' "$LEDGER")"
    [ "$dep_status" = done ] || return 1
    git merge-base --is-ancestor "impl/$dep" "$LOOP_BRANCH" 2> /dev/null || return 1
  done
  return 0
}

# 以 Haiku 產生本輪主要任務的一句話說明；失敗時退回簡短的任務/輪次描述，不影響 loop
summarize_round() {
  local id="$1" mode="$2" round="$3" data summary
  data="$(
    echo "## 任務"; flock "$LEDGER_LOCK" grep -E "^\| $id \|" "$LEDGER"
    echo "## 模式"; echo "$mode 輪 #$round"
    echo "## 最近的決策紀錄"; tail -20 "$DECISION_LOG" 2> /dev/null || echo "（尚無）"
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
  commit_ledger_status "$id" doing "$id 開始執行，狀態改為 doing"
  if ! git worktree add "$wt" -b "$branch" "$LOOP_BRANCH" >> "$pipe_log" 2>&1; then
    log "[$id] worktree 建立失敗，任務標 blocked，見 $pipe_log"
    commit_ledger_status "$id" blocked "worktree 建立失敗，$id 標 blocked"
    return
  fi

  local round=0
  while true; do
    round=$((round + 1))
    if [ "$round" -gt "$MAX_TASK_ROUNDS" ]; then
      log "[$id] 已達 MAX_TASK_ROUNDS=$MAX_TASK_ROUNDS 仍未核准，標 blocked，保留 worktree 供人工檢查。"
      commit_ledger_status "$id" blocked "$id 達 MAX_TASK_ROUNDS=$MAX_TASK_ROUNDS 仍未核准，標 blocked"
      return
    fi

    log "[$id] 本輪任務：$(summarize_round "$id" dev "$round")"
    log "[$id] Dev 輪 #$round"
    set +e
    (cd "$wt" && timeout --foreground "$ROUND_TIMEOUT" claude -p \
      "任務 ID：$id
$(cat "$DEV_PROMPT")" \
      --model "$MODEL" --effort "$EFFORT" \
      --output-format stream-json --verbose \
      --dangerously-skip-permissions) >> "$pipe_log" 2>&1
    dev_status=$?
    set -e
    [ "$dev_status" -ne 0 ] && log "[$id] Dev 輪回傳狀態碼 $dev_status（見 $pipe_log），仍進入 Review。"

    log "[$id] 本輪任務：$(summarize_round "$id" review "$round")"
    log "[$id] Review 輪 #$round"
    set +e
    (cd "$wt" && timeout --foreground "$ROUND_TIMEOUT" claude -p \
      "任務 ID：$id
$(cat "$REVIEW_PROMPT")" \
      --model "$REVIEW_MODEL" --effort "$REVIEW_EFFORT" \
      --output-format stream-json --verbose \
      --dangerously-skip-permissions) >> "$pipe_log" 2>&1
    review_status=$?
    set -e
    [ "$review_status" -ne 0 ] && log "[$id] Review 輪回傳狀態碼 $review_status（見 $pipe_log）。"

    # 注意：這裡一定要讀 worktree 自己那份 tasks.md，不是主 repo 的 $LEDGER。
    # Dev／Review 都是在 `cd "$wt" && claude -p ...` 裡跑的，牠們改的是 worktree
    # 自己 working directory 底下的檔案；在合併回整合分支之前，主 repo 的 $LEDGER
    # 完全看不到這些改動。之前就是讀錯這份檔案，導致不管跑幾輪都讀到 pipeline 一
    # 開始寫的 doing，永遠判斷「尚未核准」，白白耗光 MAX_TASK_ROUNDS。
    local status
    status="$(awk -F'|' -v id="$id" '$0 ~ "^\\| "id" \\|" {gsub(/^ +| +$/,"",$5); print $5}' "$wt/$LEDGER" 2> /dev/null)"

    if [ "$status" = done ] || [ "$status" = blocked ]; then
      log "[$id] worktree 內狀態為 $status，嘗試合併回 $LOOP_BRANCH（保留紀錄與 commit 歷史）"
      # 注意：一定要真的檢查合併是否成功，不能用 "... || true" 這種吞掉整串失敗的寫法
      # ——之前就是這樣，合併失敗（例如主 repo working tree 不乾淨）被靜默吞掉，腳本
      # 還是照樣把 worktree 刪掉，做完的成果只留在 impl/<id> 分支上，沒進整合分支，
      # 任務清單卻沒人知道要修正。
      set +e
      flock "$MERGE_LOCK" bash -c "
        git switch '$LOOP_BRANCH' &&
        git merge --no-ff '$branch' -m '[dev] 合併任務 $id（狀態：$status）'
      " >> "$pipe_log" 2>&1
      merge_status=$?
      flock "$MERGE_LOCK" git switch - >> "$pipe_log" 2>&1
      set -e

      if [ "$merge_status" -ne 0 ]; then
        log "[$id] 合併失敗（見 $pipe_log），worktree 與分支都保留，標 blocked 交人工處理，不視為完成。"
        commit_ledger_status "$id" blocked "$id worktree 內已判定 $status，但合併回 $LOOP_BRANCH 失敗，需人工介入（見 $pipe_log）"
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
    # 其餘情況（review-pending 沒被 Review 處理、或退回成 doing）視為需要再一輪
    log "[$id] 尚未核准，狀態=$status，進入下一輪。"
  done
}

# ---------- 安排階段（只跑一次；任務清單已存在種子資料則略過，除非 FORCE_PLANNING=1）----------
if [ "${FORCE_PLANNING:-0}" = 1 ] || [ ! -s "$LEDGER" ]; then
  log "執行安排階段（planning）"
  timeout --foreground "$ROUND_TIMEOUT" claude -p "$(cat "$PLANNING_PROMPT")" \
    --model "$PLANNING_MODEL" --effort medium \
    --output-format stream-json --verbose \
    --dangerously-skip-permissions > "$LOGS/planning-$(date +%Y%m%d-%H%M%S).log" 2>&1 || true
fi

# ---------- 主迴圈：每輪巡視、補滿並行名額 ----------
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
    fi
  done

  active="${#PIDS[@]}"
  slots=$((MAX_PARALLEL - active))
  if [ "$slots" -gt 0 ]; then
    for id in $(tasks_with_status todo); do
      [ "$slots" -le 0 ] && break
      if deps_satisfied "$id"; then
        log "啟動管線：$id（目前並行數 $((active + 1))）"
        run_pipeline "$id" &
        PIDS["$id"]=$!
        active=$((active + 1))
        slots=$((slots - 1))
      fi
    done
  fi

  if [ "${#PIDS[@]}" -eq 0 ]; then
    remaining_todo="$(tasks_with_status todo | wc -l | tr -d ' ')"
    if [ "$remaining_todo" -eq 0 ]; then
      log "沒有可執行或進行中的任務，停止迴圈。請查看 $LEDGER 的 blocked 任務與 $STATE/open-questions.md。"
      break
    fi
    log "有 todo 任務但依賴尚未滿足，等待 30 秒後重新巡視。"
    sleep 30
    continue
  fi

  log "本輪並行管線數：${#PIDS[@]}，等待任一管線結束後重新巡視（最多等 60 秒）"
  sleep 60
done

log "等待所有背景管線結束"
for id in "${!PIDS[@]}"; do wait "${PIDS[$id]}" 2> /dev/null || true; done

bash "$VERIFY" || log "verify.sh 回報非 0，請人工檢查 $RUNTIME/last-verify.md"
