#!/usr/bin/env bash
# ui-authoring loop 外部驗證：每一輪結束後由 run-ui-authoring-loop.sh 呼叫，不採信 agent 自己的回報。
# 用法：verify-ui-authoring.sh <本輪開始時的 HEAD> <exec|review> <本輪開始的 epoch 秒數>
# 結果寫入 runtime/last-verify.md（下一輪必讀），通過回傳 0、失敗回傳 1。
# 每個檢查前的編號只在本檔內部使用，跟 spec-migration-loop 的編號無對應關係。

set -uo pipefail

BASE="$1"
MODE="$2"
ROUND_START="$3"

LOOP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$LOOP_DIR/../../.." && pwd)"
cd "$REPO_ROOT"

LOOP=".dev/loops/ui-authoring-loop"
RUNTIME="$LOOP/runtime"
GATES="$RUNTIME/gates"
TMP="$RUNTIME/tmp"
TOOLS="python3 $LOOP/ui-authoring-tools.py"
LEDGER="$LOOP/ui-authoring-tasks.md"
PDCA="$LOOP/ui-authoring-pdca.md"
STATE_MD="$LOOP/ui-authoring-state.md"
REVIEW_LOG="$LOOP/ui-authoring-review.md"
OQ="$LOOP/ui-authoring-open-questions.md"
REPORT="$RUNTIME/last-verify.md"
ERRORS_JSON="$RUNTIME/errors.json"
mkdir -p "$GATES" "$TMP"

fails=()
warns=()
infos=()
fail() { fails+=("$1"); }
warn() { warns+=("$1"); }
info() { infos+=("$1"); }
fail_lines() { while IFS= read -r line; do [ -n "$line" ] && fail "$1$line"; done <<< "$2"; }

HEAD_SHA="$(git rev-parse HEAD)"
TASK_ID="$($TOOLS last-task "$PDCA")"
changed_files="$(git diff --name-only "$BASE" HEAD)"
export FINDINGS_CACHE_DIR="$TMP/findings"
rm -rf "$FINDINGS_CACHE_DIR"

# 1. 工作區必須乾淨
if [ -n "$(git status --porcelain)" ]; then
  fail "[1] 工作區有未提交的變更：$(git status --porcelain | head -5 | tr '\n' ' ')"
fi

# 2. commit 訊息格式，以及類型與改動檔案相符
is_ui() { [[ "$1" =~ ^\.dev/F[0-9]{2}-[^/]+/ui-[^/]+\.md$ ]]; }
commit_count=0
for c in $(git rev-list --reverse "$BASE"..HEAD); do
  commit_count=$((commit_count + 1))
  subject="$(git log -1 --format=%s "$c")"
  if ! printf '%s' "$subject" | grep -Eq '^!?\[(spec/design|dev|test|docs|chore|revert)\](\([a-z-]+\))? .+'; then
    fail "[2] commit ${c:0:7} 訊息格式不符 git-convension.md：$subject"
    continue
  fi
  kinds=""
  while IFS= read -r f; do
    [ -z "$f" ] && continue
    if is_ui "$f"; then kinds+="ui "
    else kinds+="loop "
    fi
  done <<< "$(git diff-tree --no-commit-id --name-only -r "$c")"
  kinds="$(tr ' ' '\n' <<< "$kinds" | sed '/^$/d' | sort -u | tr '\n' ' ')"
  case "$kinds" in
    "ui ") printf '%s' "$subject" | grep -Eq '^\[spec/design\]\([a-z-]+\) ' || fail "[2] commit ${c:0:7} 改 ui 檔，訊息應為 [spec/design](ui-<模組名>) …：$subject" ;;
    "loop ") printf '%s' "$subject" | grep -Eq '^\[docs\]\(loops\) ' || fail "[2] commit ${c:0:7} 改 loop 文件，訊息應為 [docs](loops) …：$subject" ;;
    "") ;;
    *) fail "[2] commit ${c:0:7} 混合了不同種類的檔案（${kinds}），ui 檔與 loop 文件要分開 commit：$subject" ;;
  esac
done
info "本輪 commit 數：$commit_count"

# 3. 受保護檔案：本輪只能改白名單內的檔案
exec_allowed=("$LEDGER" "$PDCA" "$STATE_MD" "$OQ")
review_allowed=("$LEDGER" "$REVIEW_LOG")
ui_changed=0
while IFS= read -r f; do
  [ -z "$f" ] && continue
  if is_ui "$f"; then
    ui_changed=$((ui_changed + 1))
    [ "$MODE" = review ] && fail "[3] 審查輪只能修改任務清單與審查紀錄，卻修改了：$f"
    continue
  fi
  allowed=("${exec_allowed[@]}")
  [ "$MODE" = review ] && allowed=("${review_allowed[@]}")
  ok=no
  for a in "${allowed[@]}"; do [ "$f" = "$a" ] && ok=yes; done
  [ "$ok" = no ] && fail "[3] 修改了白名單以外的檔案（受保護或不屬於本 loop 範圍）：$f"
done <<< "$changed_files"

# 4. 任務清單只改狀態、PDCA／審查紀錄只追加
snap() { git show "$BASE:$1" > "$2" 2> /dev/null || : > "$2"; }
snap "$LEDGER" "$TMP/ledger-base.md"
out="$($TOOLS check-ledger "$TMP/ledger-base.md" "$LEDGER" "$MODE" "$GATES")" || fail_lines "[4] 任務清單：" "$out"
ledger_changed=no
cmp -s "$TMP/ledger-base.md" "$LEDGER" || ledger_changed=yes
snap "$PDCA" "$TMP/pdca-base.md"
if [ "$MODE" = exec ]; then
  out="$($TOOLS check-pdca-append "$TMP/pdca-base.md" "$PDCA")" || fail_lines "[4] " "$out"
elif ! cmp -s "$TMP/pdca-base.md" "$PDCA"; then
  fail "[3] 審查輪不可修改 PDCA"
fi
snap "$REVIEW_LOG" "$TMP/review-base.md"
out="$($TOOLS check-append "$TMP/review-base.md" "$REVIEW_LOG")" || fail "[4] 審查紀錄舊內容被修改（只能在檔尾追加）"
if [ "$MODE" = review ] && cmp -s "$TMP/review-base.md" "$REVIEW_LOG"; then
  fail "[3] 審查輪沒有追加審查紀錄"
fi

# 5. OQ 檔只追加且欄位齊全
snap "$OQ" "$TMP/oq-base.md"
out="$($TOOLS check-oq "$TMP/oq-base.md" "$OQ")" || fail_lines "[5] " "$out"

# 6. 逐字引用：OQ 與 ui 檔裡標【引用原文】／【矛盾】／【推論】／【覆蓋】的『』引用必須逐字相符 spec
if ! out="$(python3 "$LOOP/verify-quotes.py" 2>&1)"; then
  fail_lines "[6] " "$out"
fi

# 7、8、10. ui-check error 數
prev_json="$TMP/errors-prev.json"
if [ -f "$ERRORS_JSON" ]; then cp "$ERRORS_JSON" "$prev_json"; else echo '{}' > "$prev_json"; fi
if ! $TOOLS errors-json > "$TMP/errors-now.json" 2> "$TMP/errors-now.err"; then
  fail "[7] ui-check 無法執行：$(head -3 "$TMP/errors-now.err" | tr '\n' ' ')"
  echo '{}' > "$TMP/errors-now.json"
fi
prev_of() { python3 -c 'import json,sys; print(json.load(open(sys.argv[1])).get(sys.argv[2], ""))' "$1" "$2"; }
while IFS= read -r row; do
  tid="$(cut -d'|' -f2 <<< "$row" | tr -d ' ')"
  for f in $($TOOLS task-files "$LEDGER" "$tid"); do
    before="$(prev_of "$prev_json" "$f")"
    now="$(prev_of "$TMP/errors-now.json" "$f")"
    if [ -n "$before" ] && [ -n "$now" ] && [ "$now" -gt "$before" ]; then
      fail "[7] 進行中任務 $tid 的 $f error 數由 $before 增加為 $now（未完成的任務不可讓檔案變得更糟）"
    fi
  done
done <<< "$(grep -E '^\| *(T[0-9]+\.[0-9]+|D-[0-9]+) *\| *doing *\|' "$LEDGER")"
total_before="$(prev_of "$prev_json" all)"
total_now="$(prev_of "$TMP/errors-now.json" all)"
info "[8] ui-check error 總數：${total_before:-（無紀錄）} → ${total_now:-?}"
[ -n "$total_now" ] && cp "$TMP/errors-now.json" "$ERRORS_JSON"

# 9. 已完成任務的機械驗收條件（ui-check(...)=0）必須仍成立
if ! out="$($TOOLS accept-check "$LEDGER" --done 2>&1)"; then
  fail_lines "[9] 已完成任務的驗收條件不成立：" "$out"
fi

# 10. 關卡核准檔不可由 agent 建立
for g in "$GATES"/*.approved; do
  [ -e "$g" ] || continue
  if [ "$(stat -c %Y "$g")" -ge "$ROUND_START" ]; then
    fail "[10] 本輪期間出現關卡核准檔 $g（只有 loop 腳本或人工可以建立），已刪除"
    rm -f "$g"
  fi
done

# 11. 完成標記必須名副其實
if [ -f "$RUNTIME/DONE" ]; then
  done_errs=""
  [ "${total_now:-1}" != 0 ] && done_errs+="ui-check 仍有 ${total_now:-?} 個 error；"
  missing="$($TOOLS expected-ui-files | while read -r f; do [ -f "$f" ] || echo "$f"; done)"
  [ -n "$missing" ] && done_errs+="缺少 ui 檔：$(tr '\n' ' ' <<< "$missing")；"
  if $TOOLS status-summary "$LEDGER" | head -1 | grep -Eq '(todo|doing|blocked)=[1-9]'; then
    done_errs+="任務清單仍有未結項目；"
  fi
  if [ -n "$done_errs" ]; then
    fail "[11] 建立了 DONE 但條件未達成（${done_errs}），已刪除 DONE"
    rm -f "$RUNTIME/DONE"
  fi
fi

# 是否有前進：任務狀態變更，或 error 總數下降
if [ "$ledger_changed" = yes ] || { [ -n "$total_before" ] && [ -n "$total_now" ] && [ "$total_now" -lt "$total_before" ]; }; then
  echo yes > "$RUNTIME/last-progress"
else
  echo no > "$RUNTIME/last-progress"
fi

status=PASS
[ "${#fails[@]}" -gt 0 ] && status=FAIL
{
  echo "# 上一輪驗證結果：$status"
  echo
  echo "- 時間：$(date '+%F %T')"
  echo "- 模式：$MODE"
  echo "- 任務編號（PDCA 最後一則）：${TASK_ID:-無}"
  echo "- 範圍：${BASE:0:7}..${HEAD_SHA:0:7}"
  echo
  echo "## 失敗項目（下一輪必須先修正）"
  if [ "${#fails[@]}" -eq 0 ]; then echo "- 無"; else printf -- '- %s\n' "${fails[@]}"; fi
  echo
  echo "## 警告"
  if [ "${#warns[@]}" -eq 0 ]; then echo "- 無"; else printf -- '- %s\n' "${warns[@]}"; fi
  echo
  echo "## 資訊"
  printf -- '- %s\n' "${infos[@]}"
  echo "- 各檔 error 數："
  sed 's/^/    /' "$TMP/errors-now.json"
} > "$REPORT"

cat "$REPORT"
[ "$status" = PASS ]
