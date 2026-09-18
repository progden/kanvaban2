#!/usr/bin/env bash
# implementation-loop 外部驗證：整體巡視一次，不採信 agent 自己的回報。

set -euo pipefail

LOOP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPO_ROOT="$(cd "$LOOP_DIR/../../.." && pwd)"
cd "$REPO_ROOT"

STATE="$LOOP_DIR/.state"
RUNTIME="$LOOP_DIR/runtime"
LEDGER="$STATE/tasks.md"
REPORT="$RUNTIME/last-verify.md"

fail=0
notes=()

check() {
  local desc="$1" cmd="$2"
  if eval "$cmd"; then
    notes+=("PASS：$desc")
  else
    notes+=("FAIL：$desc")
    fail=1
  fi
}

# 任務 $1 的狀態：.state/tasks/<id>/status（不存在＝todo），跟 run-loop.sh 的 task_status 同一套規則
task_status() {
  local f="$STATE/tasks/$1/status"
  if [ -s "$f" ]; then tr -d ' \r\n' < "$f"; else echo todo; fi
}
task_ids() { grep -E '^\| T-' "$LEDGER" | awk -F'|' '{gsub(/^ +| +$/,"",$2); print $2}'; }

# 1. 任務清單格式：每個 T-xx 列必須有四個 | 分隔欄位（ID／產出範圍／依賴／備註；
# 狀態欄 2026-09-18 起移到 .state/tasks/<id>/status）
check "任務清單格式（每列 4 個欄位）" \
  "grep -E '^\| T-' '$LEDGER' | awk -F'|' 'NF!=6{print;bad=1} END{exit bad}' > /dev/null"

# 2. 狀態值合法；doing 卻沒有對應 worktree（管線異常中斷）；blocked 必須有自己的 OQ
for id in $(task_ids); do
  st="$(task_status "$id")"
  case "$st" in
    todo | review-pending | done) ;;
    doing)
      git worktree list | grep -q "impl-$id" \
        || notes+=("WARN：任務 $id 狀態 doing 但找不到對應 worktree，可能是上次執行中斷")
      ;;
    blocked)
      # agent 自己標的要有 OQ；驅動腳本標的（合併失敗、達回合上限等）原因在 driver-note.md
      [ -s "$STATE/tasks/$id/open-questions.md" ] || [ -s "$STATE/tasks/$id/driver-note.md" ] \
        || grep -qs "$id" "$STATE/archive/open-questions.md" \
        || { notes+=("FAIL：任務 $id 標 blocked 但找不到原因（tasks/$id/open-questions.md、driver-note.md、archive 都沒有）"); fail=1; }
      ;;
    *) notes+=("FAIL：任務 $id 的 status 檔內容不合法：$st"); fail=1 ;;
  esac
done

# 3. 任務目錄必須對得回任務清單（_planning 是安排階段自己的目錄）
for d in "$STATE"/tasks/*/; do
  name="$(basename "$d")"
  [ "$name" = _planning ] && continue
  grep -q "^| $name |" "$LEDGER" || { notes+=("FAIL：.state/tasks/$name 不在任務清單上"); fail=1; }
done

# 4. 三個專案骨架真的建置／測試一次，不採信 agent 自己的 Check 欄
if [ -f "$REPO_ROOT/gradlew" ]; then
  check "kanban-core／kanban-spring 建置＋測試（Gradle 多模組）" \
    "(cd '$REPO_ROOT' && ./gradlew clean build --no-daemon -q)"
fi
if [ -f "$REPO_ROOT/kanban-frontend/package.json" ]; then
  check "kanban-frontend 建置＋測試" \
    "(cd '$REPO_ROOT/kanban-frontend' && pnpm install --frozen-lockfile && pnpm run build && pnpm run test)"
fi

# TODO：核對每個 done 任務的 diff 範圍是否只動了該任務宣告的目錄（比對 tasks.md 產出範圍欄）。

{
  echo "# implementation-loop 驗證報告（$(date '+%F %T')）"
  printf '%s\n' "${notes[@]}"
} > "$REPORT"

cat "$REPORT"
exit $fail
