#!/usr/bin/env bash
# 把分散在 .state/tasks/<id>/ 的紀錄串成一份給人看（只印到 stdout，不寫檔）。
# 用法：collect.sh status | oq | review | decision | fixes | state
# 狀態與紀錄拆成每任務一個目錄是為了讓並行 worktree 合併不衝突；這支腳本補回
# 「打開一個檔就看到全部」的方便。

set -euo pipefail

LOOP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STATE="$LOOP_DIR/.state"
LEDGER="$STATE/tasks.md"

task_ids() { grep -E '^\| T-' "$LEDGER" | awk -F'|' '{gsub(/^ +| +$/,"",$2); print $2}'; }

# 串接每個任務目錄底下的 $1；$2 是 archive 裡對應的舊檔（可省略）
concat() {
  local file="$1" legacy="${2:-}" id
  if [ -n "$legacy" ] && [ -f "$STATE/archive/$legacy" ]; then
    echo "<!-- ===== archive/$legacy（2026-09-18 前的共用紀錄） ===== -->"
    cat "$STATE/archive/$legacy"
    echo
  fi
  for id in _planning $(task_ids); do
    [ -s "$STATE/tasks/$id/$file" ] || continue
    echo "<!-- ===== tasks/$id/$file ===== -->"
    cat "$STATE/tasks/$id/$file"
    echo
  done
}

case "${1:-status}" in
  status)
    printf '%-28s %-15s %s\n' "任務" "狀態" "依賴"
    for id in $(task_ids); do
      f="$STATE/tasks/$id/status"
      st=todo
      [ -s "$f" ] && st="$(tr -d ' \r\n' < "$f")"
      deps="$(awk -F'|' -v id="$id" '$0 ~ "^\\| "id" \\|" {print $4}' "$LEDGER" | grep -oE 'T-[0-9A-Za-z-]+' | tr '\n' ' ' || true)"
      printf '%-28s %-15s %s\n' "$id" "$st" "$deps"
    done
    ;;
  oq) concat open-questions.md open-questions.md ;;
  review) concat review.md review.md ;;
  decision) concat decision-log.md decision-log.md ;;
  fixes) concat fixes.md ;;
  state) concat state.md state.md ;;
  *)
    echo "用法：$0 status|oq|review|decision|fixes|state" >&2
    exit 2
    ;;
esac
