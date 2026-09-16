#!/usr/bin/env bash
# spec 遷移 loop 外部驗證：每一輪結束後由 run-spec-migration-loop.sh 呼叫，不採信 agent 自己的回報。
# 用法：verify-spec-migration.sh <本輪開始時的 HEAD> <exec|review> <本輪開始的 epoch 秒數>
# 結果寫入 runtime/last-verify.md（下一輪 kickoff 必讀），通過回傳 0、失敗回傳 1。
# 每個檢查前的編號對應規則書「外部驗證」表。

set -uo pipefail

BASE="$1"
MODE="$2"
ROUND_START="$3"

LOOP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$LOOP_DIR/../../.." && pwd)"
cd "$REPO_ROOT"

LOOP=".dev/loops/spec-migration-loop"
RUNTIME="$LOOP/runtime"
GATES="$RUNTIME/gates"
TMP="$RUNTIME/tmp"
TOOLS="python3 $LOOP/spec-migration-tools.py"
LEDGER="$LOOP/spec-migration-tasks.md"
PDCA="$LOOP/spec-migration-pdca.md"
STATE_MD="$LOOP/spec-migration-state.md"
REVIEW_LOG="$LOOP/spec-migration-review.md"
OQ="$LOOP/spec-migration-open-questions.md"
CR_FILE=".dev/CR.md"
REPORT="$RUNTIME/last-verify.md"
ERRORS_JSON="$RUNTIME/errors.json"
mkdir -p "$GATES" "$TMP"

fails=()
warns=()
infos=()
fail() { fails+=("$1"); }
warn() { warns+=("$1"); }
info() { infos+=("$1"); }
# 把工具的多行輸出逐行記成失敗或警告
fail_lines() { while IFS= read -r line; do [ -n "$line" ] && fail "$1$line"; done <<< "$2"; }

HEAD_SHA="$(git rev-parse HEAD)"
TASK_ID="$($TOOLS last-task "$PDCA")"
changed_files="$(git diff --name-only "$BASE" HEAD)"
export FINDINGS_JSON="$TMP/findings.json"
rm -f "$FINDINGS_JSON"

# 1. 工作區必須乾淨
if [ -n "$(git status --porcelain)" ]; then
  fail "[1] 工作區有未提交的變更：$(git status --porcelain | head -5 | tr '\n' ' ')"
fi

# 2. commit 訊息格式，以及類型與改動檔案相符
is_spec() { [[ "$1" =~ ^\.dev/F[0-9]{2}-[^/]+/spec-[^/]+\.md$ ]]; }
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
    if is_spec "$f"; then kinds+="spec "
    elif [ "$f" = "$CR_FILE" ]; then kinds+="cr "
    else kinds+="loop "
    fi
  done <<< "$(git diff-tree --no-commit-id --name-only -r "$c")"
  kinds="$(tr ' ' '\n' <<< "$kinds" | sed '/^$/d' | sort -u | tr '\n' ' ')"
  case "$kinds" in
    "spec ") printf '%s' "$subject" | grep -Eq '^\[spec/design\]' || fail "[2] commit ${c:0:7} 改規格檔，類型應為 [spec/design]：$subject" ;;
    "cr ") printf '%s' "$subject" | grep -Eq '^\[chore\]\(cr\) ' || fail "[2] commit ${c:0:7} 改 CR.md，訊息應為 [chore](cr) …：$subject" ;;
    "loop ") printf '%s' "$subject" | grep -Eq '^\[docs\]\(loops\) ' || fail "[2] commit ${c:0:7} 改 loop 文件，訊息應為 [docs](loops) …：$subject" ;;
    "") ;;
    *) fail "[2] commit ${c:0:7} 混合了不同種類的檔案（${kinds}），規格、CR.md、loop 文件要分開 commit" ;;
  esac
done
info "本輪 commit 數：$commit_count"

# 3. 受保護檔案：執行輪只能改白名單內的檔案
exec_allowed=("$LEDGER" "$PDCA" "$STATE_MD" "$OQ" "$CR_FILE")
spec_changed=0
while IFS= read -r f; do
  [ -z "$f" ] && continue
  if is_spec "$f"; then
    spec_changed=$((spec_changed + 1))
    [ "$MODE" = review ] && fail "[14] 審查輪只能修改任務清單與審查紀錄，卻修改了：$f"
    continue
  fi
  if [ "$MODE" = review ]; then
    [ "$f" != "$LEDGER" ] && [ "$f" != "$REVIEW_LOG" ] && fail "[14] 審查輪只能修改任務清單與審查紀錄，卻修改了：$f"
    continue
  fi
  ok=no
  for a in "${exec_allowed[@]}"; do [ "$f" = "$a" ] && ok=yes; done
  [ "$ok" = no ] && fail "[3] 修改了白名單以外的檔案（受保護或不屬於遷移範圍）：$f"
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
  fail "[14] 審查輪不可修改 PDCA"
fi
snap "$REVIEW_LOG" "$TMP/review-base.md"
out="$($TOOLS check-append "$TMP/review-base.md" "$REVIEW_LOG")" || fail "[4] 審查紀錄舊內容被修改（只能在檔尾追加）"
if [ "$MODE" = review ] && cmp -s "$TMP/review-base.md" "$REVIEW_LOG"; then
  fail "[14] 審查輪沒有追加審查紀錄"
fi

# 5～7. 每份 spec 對遷移前備份：行為逐字一致、變更紀錄保留、tag 只加 @uc-／@fail-
for spec in $($TOOLS spec-files); do
  legacy="$($TOOLS legacy-path "$spec")"
  if [ ! -f "$legacy" ]; then
    fail "[5] 找不到遷移前備份 $legacy"
    continue
  fi
  out="$($TOOLS gherkin-diff "$spec")"
  case $? in
    0) ;;
    3) warn "[5] $out" ;;
    *) fail_lines "[5] " "$out" ;;
  esac
  out="$($TOOLS changelog-check "$spec")" || fail_lines "[6] $spec：" "$out"
  out="$($TOOLS tag-diff "$spec")" || fail_lines "[7] $spec：" "$out"
done

# 8. 已完成任務的機械驗收條件（errors(...)=0、crcheck(...)=0）必須仍成立
if ! out="$($TOOLS accept-check "$LEDGER" --done 2>&1)"; then
  fail_lines "[8] 已完成任務的驗收條件不成立：" "$out"
fi

# 9、10、13. error 數
prev_json="$TMP/errors-prev.json"
if [ -f "$ERRORS_JSON" ]; then cp "$ERRORS_JSON" "$prev_json"; else echo '{}' > "$prev_json"; fi
if ! $TOOLS errors-json > "$TMP/errors-now.json" 2> "$TMP/errors-now.err"; then
  fail "[10] spec-check 無法執行：$(head -3 "$TMP/errors-now.err" | tr '\n' ' ')"
  echo '{}' > "$TMP/errors-now.json"
fi
prev_of() { python3 -c 'import json,sys; print(json.load(open(sys.argv[1])).get(sys.argv[2], ""))' "$1" "$2"; }
while IFS= read -r row; do
  tid="$(cut -d'|' -f2 <<< "$row" | tr -d ' ')"
  for f in $($TOOLS task-files "$LEDGER" "$tid"); do
    before="$(prev_of "$prev_json" "$f")"
    now="$(prev_of "$TMP/errors-now.json" "$f")"
    if [ -n "$before" ] && [ -n "$now" ] && [ "$now" -gt "$before" ]; then
      fail "[9] 進行中任務 $tid 的 $f error 數由 $before 增加為 $now（未完成的任務不可讓檔案變得更糟）"
    fi
  done
done <<< "$(grep -E '^\| *(T[0-9]+\.[0-9]+|D-[0-9]+) *\| *doing *\|' "$LEDGER")"
total_before="$(prev_of "$prev_json" all)"
total_now="$(prev_of "$TMP/errors-now.json" all)"
info "[10] spec-check error 總數：${total_before:-（無紀錄）} → ${total_now:-?}"
info "[13] spec-check warning 總數：$($TOOLS warn-count all 2> /dev/null || echo ?)"
[ -n "$total_now" ] && cp "$TMP/errors-now.json" "$ERRORS_JSON"

# 11. OQ 檔只追加且欄位齊全
snap "$OQ" "$TMP/oq-base.md"
out="$($TOOLS check-oq "$TMP/oq-base.md" "$OQ")" || fail_lines "[11] " "$out"

# 12. 單輪改動的 spec 檔數
if [ "$spec_changed" -gt 1 ] && [[ "$TASK_ID" != T3.* ]]; then
  warn "[12] 本輪改了 $spec_changed 份 spec（任務 ${TASK_ID:-無}），一輪應只遷移一個模組"
fi

# 15. 關卡核准檔不可由 agent 建立
for g in "$GATES"/*.approved; do
  [ -e "$g" ] || continue
  if [ "$(stat -c %Y "$g")" -ge "$ROUND_START" ]; then
    fail "[15] 本輪期間出現關卡核准檔 $g（只有 loop 腳本或人工可以建立），已刪除"
    rm -f "$g"
  fi
done

# 16. 完成標記必須名副其實
if [ -f "$RUNTIME/DONE" ]; then
  done_errs=""
  [ "${total_now:-1}" != 0 ] && done_errs+="spec-check 仍有 ${total_now:-?} 個 error；"
  cr_out="$($TOOLS crcheck CR-005)" || done_errs+="$cr_out；"
  if $TOOLS status-summary "$LEDGER" | head -1 | grep -Eq '(todo|doing|blocked|proposed)=[1-9]'; then
    done_errs+="任務清單仍有未結項目；"
  fi
  if [ -n "$done_errs" ]; then
    fail "[16] 建立了 DONE 但條件未達成（${done_errs}），已刪除 DONE"
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
  echo "## 警告（交給審查輪判斷）"
  if [ "${#warns[@]}" -eq 0 ]; then echo "- 無"; else printf -- '- %s\n' "${warns[@]}"; fi
  echo
  echo "## 資訊"
  printf -- '- %s\n' "${infos[@]}"
  echo "- 各檔 error 數："
  sed 's/^/    /' "$TMP/errors-now.json"
} > "$REPORT"

cat "$REPORT"
[ "$status" = PASS ]
