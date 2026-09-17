#!/usr/bin/env bash
# UI 撰寫 loop：每一輪都是全新的 claude process（zero context），狀態只存在檔案與 git 歷史。
# 跟 spec-migration-loop 的驅動腳本同結構，差別只在呼叫的提示詞／驗證腳本／分支名稱不同。
#
# 每一輪：
#   1. 由任務清單算出下一個可執行任務；遇到關卡（G*）先跑審查輪，不中斷模式（預設）下無待修正任務即自動核准。
#   2. 以 Haiku 讀取任務清單／PDCA／上一輪驗證結果，產生一句話說明本輪主要任務。
#   3. 執行輪（kickoff 提示詞）或審查輪（review 提示詞，每 REVIEW_EVERY 輪一次）。
#   4. 由 verify-ui-authoring.sh 外部驗證，不採信 agent 自己的回報。
#
# 停止條件：DONE 標記、MAX_ITERATIONS、無可執行任務（全部 blocked；AUTO_APPROVE_GATES=0 時含等待人工關卡）、
#           同一任務連續驗證失敗 MAX_TASK_FAILS 次、連續 MAX_NO_PROGRESS 個執行輪沒有前進。
#
# 若不想跑無人值守全自動 loop，可以只用 Claude Code 內建 `/loop`（見本檔規則書「操作手冊」）；
# 本腳本是給要跑完整六模組、需要外部驗證與自動 commit 的場景用的。

set -euo pipefail

LOOP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$LOOP_DIR/../../.." && pwd)"
cd "$REPO_ROOT"

LOOP=".dev/loops/ui-authoring-loop"
RUNTIME="$LOOP/runtime"
GATES="$RUNTIME/gates"
LOGS="$RUNTIME/logs"
TOOLS="python3 $LOOP/ui-authoring-tools.py"
LEDGER="$LOOP/ui-authoring-tasks.md"
PDCA="$LOOP/ui-authoring-pdca.md"
KICKOFF="$LOOP/ui-authoring-kickoff-prompt.md"
REVIEW_PROMPT="$LOOP/ui-authoring-review-prompt.md"
VERIFY="$LOOP/verify-ui-authoring.sh"
DONE_FILE="$RUNTIME/DONE"
STATE="$RUNTIME/state"
BASELINE="$RUNTIME/baseline"

MAX_ITERATIONS="${MAX_ITERATIONS:-80}"
MODEL="${MODEL:-claude-sonnet-5}"
EFFORT="${EFFORT:-medium}"
REVIEW_MODEL="${REVIEW_MODEL:-claude-opus-5}"
REVIEW_EFFORT="${REVIEW_EFFORT:-medium}"
REVIEW_EVERY="${REVIEW_EVERY:-5}"
SUMMARY_MODEL="${SUMMARY_MODEL:-claude-haiku-4-5-20251001}"
SUMMARY_EFFORT="${SUMMARY_EFFORT:-medium}"
ROUND_TIMEOUT="${ROUND_TIMEOUT:-30m}"
MAX_TASK_FAILS="${MAX_TASK_FAILS:-3}"
MAX_NO_PROGRESS="${MAX_NO_PROGRESS:-3}"
# 不中斷模式：關卡在審查輪後由腳本自動核准；同一關卡最多審查 GATE_MAX_REVIEWS 次，之後直接核准
AUTO_APPROVE_GATES="${AUTO_APPROVE_GATES:-1}"
GATE_MAX_REVIEWS="${GATE_MAX_REVIEWS:-2}"
LOOP_BRANCH="${LOOP_BRANCH:-loop/ui-authoring}"

mkdir -p "$GATES" "$LOGS" "$RUNTIME/tmp"

log() { echo "[$(date '+%F %T')] $*"; }

# ---------- 事前檢查 ----------
for cmd in claude git python3 timeout jq; do
  command -v "$cmd" > /dev/null || { echo "缺少指令：$cmd" >&2; exit 1; }
done
if [ -n "$(git status --porcelain)" ]; then
  echo "工作區不乾淨，請先 commit 或處理後再啟動 loop。" >&2
  exit 1
fi
if ! python3 -m unittest discover -s scripts/tests > "$RUNTIME/tmp/unittest.log" 2>&1; then
  echo "scripts/tests 沒有通過，腳本壞了不要跑 loop（見 $RUNTIME/tmp/unittest.log）。" >&2
  exit 1
fi
# loop 會自動 commit 且跳過權限確認，一律在專用分支執行，main 不受影響、壞了可整支丟棄
branch="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$branch" =~ ^(main|master)$ ]] && [ "${ALLOW_MAIN:-0}" != 1 ]; then
  if git show-ref --verify --quiet "refs/heads/$LOOP_BRANCH"; then
    git switch "$LOOP_BRANCH"
    log "已從 $branch 切換到既有分支 $LOOP_BRANCH（接續上次進度；若 $branch 有新 commit，請自行 merge 或 rebase）。"
  else
    git switch -c "$LOOP_BRANCH"
    log "已從 $branch 建立並切換到分支 $LOOP_BRANCH。"
  fi
fi
# baseline：loop 第一次啟動時的 HEAD
if [ ! -s "$BASELINE" ]; then
  git rev-parse HEAD > "$BASELINE"
  log "記錄 baseline：$(cat "$BASELINE")"
fi
# 起點的 error 數，供驗證腳本比較
if [ ! -s "$RUNTIME/errors.json" ]; then
  $TOOLS errors-json > "$RUNTIME/errors.json"
  log "記錄起點 error 數：$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["all"])' "$RUNTIME/errors.json")"
fi

# ---------- 狀態（跨輪計數，存在 runtime，不進版控）----------
task_fails=0; fail_task=""; no_progress=0; reviewed_gate=""; gate_name=""; gate_reviews=0
# shellcheck disable=SC1090
[ -f "$STATE" ] && source "$STATE"
save_state() {
  printf 'task_fails=%s\nfail_task=%q\nno_progress=%s\nreviewed_gate=%q\ngate_name=%q\ngate_reviews=%s\n' \
    "$task_fails" "$fail_task" "$no_progress" "$reviewed_gate" "$gate_name" "$gate_reviews" > "$STATE"
}

# 自動核准關卡：核准檔必須早於下一輪開始時間，否則會被驗證腳本視為 agent 偽造
approve_gate() {
  touch "$GATES/$1.approved"
  log "不中斷模式：自動核准關卡 $1（$2），核准檔 $GATES/$1.approved"
  reviewed_gate=""
  save_state
  sleep 2
}

# 以 Haiku 產生本輪主要任務的一句話說明；失敗時退回任務清單原文，不影響 loop
summarize_round() {
  local mode="$1" next_ids="$2" data summary
  data="$(
    echo "## 模式"; echo "$mode"
    echo "## 候選任務（依優先順序）"; $TOOLS rows "$LEDGER" $next_ids
    echo "## 上一則 PDCA 的 Act"; $TOOLS last-act "$PDCA"
    echo "## 上一輪驗證結果"; head -20 "$RUNTIME/last-verify.md" 2> /dev/null || echo "（尚無）"
  )"
  summary="$(timeout 3m claude -p "你是 loop 監控員。根據以下資料，用一句繁體中文（60 字以內）說明這一輪 iteration 的主要任務：若上一輪驗證失敗，主要任務是修正失敗項目；審查輪則說明要審查什麼；否則是第一個候選任務。只輸出那一句話，不要其他文字。

$data" --model "$SUMMARY_MODEL" --effort "$SUMMARY_EFFORT" --tools "" --output-format text 2> /dev/null | tr -d '\r' | sed '/^\s*$/d' | head -1)" || summary=""
  if [ -z "$summary" ]; then
    summary="（摘要產生失敗）$($TOOLS rows "$LEDGER" "${next_ids%% *}" | cut -d'|' -f2,4)"
  fi
  echo "$summary"
}

i=0
while true; do
  i=$((i + 1))

  if [ -f "$DONE_FILE" ]; then
    log "偵測到 $DONE_FILE，UI 撰寫已完成，停止迴圈。"
    break
  fi
  if [ "$i" -gt "$MAX_ITERATIONS" ]; then
    log "已達 MAX_ITERATIONS=$MAX_ITERATIONS，停止迴圈（尚未完成，請人工檢查）。"
    break
  fi

  next_ids="$($TOOLS actionable "$LEDGER" | head -3 | tr '\n' ' ')"
  first="${next_ids%% *}"
  if [ -z "$first" ]; then
    log "沒有可執行的任務（剩餘任務皆為 blocked 或依賴未完成），停止迴圈。"
    $TOOLS status-summary "$LEDGER"
    echo "請查看 $LOOP/ui-authoring-open-questions.md 與任務清單中 blocked 的任務，處理後將其改回 todo 再重新啟動。"
    break
  fi

  mode=exec
  if [[ "$first" == G* ]] && [ ! -f "$GATES/$first.approved" ]; then
    if [ "$gate_name" != "$first" ]; then gate_name="$first"; gate_reviews=0; fi
    if [ "$reviewed_gate" = "$first" ]; then
      if [ "$AUTO_APPROVE_GATES" != 1 ]; then
        log "等待人工關卡 $first：確認審查紀錄（$LOOP/ui-authoring-review.md）後執行"
        echo "    touch $GATES/$first.approved"
        echo "  再重新啟動 loop。"
        break
      fi
      approve_gate "$first" "審查後無待修正任務"
    elif [ "$AUTO_APPROVE_GATES" = 1 ] && [ "$gate_reviews" -ge "$GATE_MAX_REVIEWS" ]; then
      approve_gate "$first" "已審查 $gate_reviews 次，達上限 GATE_MAX_REVIEWS"
    else
      mode=review
    fi
  else
    [[ "$first" != G* ]] && reviewed_gate=""
    if [ "$REVIEW_EVERY" -gt 0 ] && [ $((i % REVIEW_EVERY)) -eq 0 ]; then
      mode=review
    fi
  fi

  base="$(git rev-parse HEAD)"
  round_start="$(date +%s)"
  summary="$(summarize_round "$mode" "$next_ids")"

  echo "=================================================="
  log "Iteration $i 開始（全新 context，mode=$mode）"
  log "本輪任務：$summary"
  echo "=================================================="

  if [ "$mode" = review ]; then
    prompt_file="$REVIEW_PROMPT"; round_model="$REVIEW_MODEL"; round_effort="$REVIEW_EFFORT"
  else
    prompt_file="$KICKOFF"; round_model="$MODEL"; round_effort="$EFFORT"
  fi
  round_log="$LOGS/iter-$(date +%Y%m%d-%H%M%S)-$i-$mode.jsonl"
  echo "$summary" > "${round_log%.jsonl}.summary.txt"

  # --dangerously-skip-permissions：跳過所有工具使用確認，才能無人值守執行 git commit。
  # 請在專用分支執行；每一輪結果仍由 verify-ui-authoring.sh 外部驗證。
  set +e
  timeout --foreground "$ROUND_TIMEOUT" claude -p "$(cat "$prompt_file")" \
    --model "$round_model" \
    --effort "$round_effort" \
    --output-format stream-json --verbose \
    --dangerously-skip-permissions > "$round_log" 2>&1
  status=$?
  set -e
  jq -r 'select(.type == "result") | .result' "$round_log" 2> /dev/null | tail -5 || true
  [ "$status" -ne 0 ] && log "claude 回傳狀態碼 $status（124 代表逾時 $ROUND_TIMEOUT），仍進行驗證。"

  rm -f "$RUNTIME/last-verify.md"
  set +e
  bash "$VERIFY" "$base" "$mode" "$round_start" > "$LOGS/verify-$i.log" 2>&1
  verify_status=$?
  set -e
  if [ -f "$RUNTIME/last-verify.md" ]; then
    head -1 "$RUNTIME/last-verify.md"
  else
    log "驗證腳本未產生報告，視為失敗，詳見 $LOGS/verify-$i.log"
    verify_status=1
  fi

  if [ "$mode" = review ] && [[ "$first" == G* ]]; then
    reviewed_gate="$first"
    gate_reviews=$((gate_reviews + 1))
  fi

  task_id="$($TOOLS last-task "$PDCA")"
  [ "$mode" = review ] && task_id="REVIEW"
  if [ "$verify_status" -ne 0 ] || [ "$status" -ne 0 ]; then
    if [ "$task_id" = "$fail_task" ]; then task_fails=$((task_fails + 1)); else task_fails=1; fail_task="$task_id"; fi
  else
    task_fails=0; fail_task=""
  fi
  if [ "$mode" = exec ] && [ "$(cat "$RUNTIME/last-progress" 2> /dev/null)" = no ]; then
    no_progress=$((no_progress + 1))
  elif [ "$mode" = exec ]; then
    no_progress=0
  fi
  save_state

  if [ "$task_fails" -ge "$MAX_TASK_FAILS" ]; then
    log "任務 $fail_task 連續 $task_fails 輪驗證失敗，停止迴圈。請查看 $RUNTIME/last-verify.md 與 $LOGS。"
    break
  fi
  if [ "$no_progress" -ge "$MAX_NO_PROGRESS" ]; then
    log "連續 $no_progress 個執行輪沒有前進（任務狀態沒變、error 總數也沒下降），停止迴圈。"
    break
  fi

  log "Iteration $i 結束"
done
