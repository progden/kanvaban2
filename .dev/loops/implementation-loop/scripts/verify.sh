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

# 1. 任務清單格式：每個 T-xx 列必須有五個 | 分隔欄位
check "任務清單格式（每列 5 個欄位）" \
  "grep -E '^\| T-' '$LEDGER' | awk -F'|' 'NF!=7{print;bad=1} END{exit bad}' > /dev/null"

# 2. 不可有任務同時處於 doing 且沒有對應 worktree（代表管線異常中斷）
# 注意：欄位順序是 (空)/ID/產出範圍/依賴/狀態/備註/(空)，狀態是第 5 欄，不是第 4 欄
# （第 4 欄是依賴）——這支腳本先前就是欄位索引抓錯，檢查形同虛設，2026-09-18 修正。
while read -r id; do
  [ -z "$id" ] && continue
  if ! git worktree list | grep -q "impl-$id"; then
    notes+=("WARN：任務 $id 狀態 doing 但找不到對應 worktree，可能是上次執行中斷")
  fi
done < <(grep -E '^\| T-' "$LEDGER" | awk -F'|' '{gsub(/^ +| +$/,"",$5); if($5=="doing") {gsub(/^ +| +$/,"",$2); print $2}}')

# 3. blocked 任務必須有對應 OQ
while read -r id; do
  [ -z "$id" ] && continue
  grep -q "$id" "$STATE/open-questions.md" || notes+=("FAIL：任務 $id 標 blocked 但 open-questions 找不到對應紀錄")
done < <(grep -E '^\| T-' "$LEDGER" | awk -F'|' '{gsub(/^ +| +$/,"",$5); if($5=="blocked") {gsub(/^ +| +$/,"",$2); print $2}}')

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
