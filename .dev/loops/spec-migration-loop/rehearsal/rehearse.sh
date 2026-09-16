#!/usr/bin/env bash
# 用假的 claude 演練 spec 遷移 loop 的控制流程與驗證腳本，不花 token、不動到原 repo。
# 用法：bash .dev/loops/spec-migration-loop/rehearsal/rehearse.sh [暫存目錄]
#
# 情境 1：合規的一輪 → 驗證 PASS
# 情境 2：各種違規各做一次 → 每一項都要被對應編號的檢查擋下
# 情境 3：多輪 → 週期審查、關卡先審查再自動核准、人工關卡模式停下、連續失敗停止

set -uo pipefail

SRC="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
WORK="${1:-$(mktemp -d)}"
REPO="$WORK/repo"
LOOP=".dev/loops/spec-migration-loop"
F01=".dev/F01-basic-kanban/spec-kanban-basic.md"
results=()
pass=0
failed=0

# 以字元（非位元組）截斷，避免切壞中文
trunc() { python3 -c 'import sys; print(sys.stdin.read().rstrip("\\n")[:int(sys.argv[1])])' "$1"; }

record() {  # record <名稱> <yes|no> <說明>
  if [ "$2" = yes ]; then pass=$((pass + 1)); results+=("✅ $1：$3"); else failed=$((failed + 1)); results+=("❌ $1：$3"); fi
}

# ---------- 建立暫存 repo（工作區的追蹤檔與未追蹤檔，不含 runtime）----------
rm -rf "$REPO"
mkdir -p "$REPO" "$WORK/bin"
(cd "$SRC" && git ls-files -co --exclude-standard -z) | (cd "$SRC" && xargs -0 cp --parents -t "$REPO")
cp "$SRC/$LOOP/rehearsal/fake-claude" "$WORK/bin/claude"
chmod +x "$WORK/bin/claude"
export PATH="$WORK/bin:$PATH"
export FAKE_CALLS="$WORK/calls.log"
cd "$REPO"
git init -q -b rehearsal
git config user.email rehearsal@example.com
git config user.name rehearsal
git add -A
git commit -q -m "[chore] 演練初始狀態"
SETUP="$(git rev-parse HEAD)"
mkdir -p "$LOOP/runtime"
git rev-parse HEAD > "$LOOP/runtime/baseline"
python3 "$LOOP/spec-migration-tools.py" errors-json > "$WORK/errors-start.json"

reset_repo() {
  git reset -q --hard "$SETUP"
  git clean -qfd
  rm -rf "$LOOP/runtime"
  mkdir -p "$LOOP/runtime"
  echo "$SETUP" > "$LOOP/runtime/baseline"
  cp "$WORK/errors-start.json" "$LOOP/runtime/errors.json"
}

# 直接跑一次驗證；回傳報告內容到 $WORK/report.md
verify() {  # verify <base> <mode> [round_start]
  bash "$LOOP/verify-spec-migration.sh" "$1" "$2" "${3:-$(date +%s)}" > /dev/null 2>&1
  cp "$LOOP/runtime/last-verify.md" "$WORK/report.md" 2> /dev/null || echo "（無報告）" > "$WORK/report.md"
}

append_pdca() {  # 合規的 PDCA 追加與 state 覆寫，讓違規項目以外的檢查都通過
  printf '\n## Iteration 1 — 2026-09-16 10:00 — %s\n### Plan\n演練\n\n### Do\n演練\n\n### Check\n0 error(s), 0 warning(s)\n\n### Act\n下一個\n' "$1" >> "$LOOP/spec-migration-pdca.md"
  git add "$LOOP/spec-migration-pdca.md"
}

# 情境 2 的一個違規：<名稱> <預期 fail 的檢查編號> <做違規的指令>
violation() {
  local name="$1" expect="$2" cmd="$3"
  reset_repo
  local base
  base="$(git rev-parse HEAD)"
  eval "$cmd"
  verify "$base" "${MODE:-exec}"
  if grep -q '^# 上一輪驗證結果：FAIL' "$WORK/report.md" && sed -n '/^## 失敗項目/,/^## 警告/p' "$WORK/report.md" | grep -qF "[$expect]"; then
    record "違規：$name" yes "被 [$expect] 擋下：$(sed -n '/^## 失敗項目/,/^## 警告/p' "$WORK/report.md" | grep -F "[$expect]" | head -1 | trunc 110)"
  else
    record "違規：$name" no "預期 [$expect] 失敗，實際報告：$(sed -n '1p;/^## 失敗項目/,/^## 資訊/p' "$WORK/report.md" | tr '\n' ' ' | trunc 300)"
  fi
}

# ---------- 情境 1：合規的一輪 ----------
reset_repo
: > "$FAKE_CALLS"
MAX_ITERATIONS=1 REVIEW_EVERY=0 bash "$LOOP/run-spec-migration-loop.sh" > "$WORK/s1.log" 2>&1
if head -1 "$LOOP/runtime/last-verify.md" 2> /dev/null | grep -q PASS \
  && python3 "$LOOP/spec-migration-tools.py" rows "$LOOP/spec-migration-tasks.md" T0.01 | grep -q '| done |'; then
  record "合規的一輪" yes "T0.01 標 done、追加 PDCA，驗證 PASS，last-progress=$(cat "$LOOP/runtime/last-progress")"
else
  record "合規的一輪" no "見 $WORK/s1.log"
fi

# ---------- 情境 2：違規 ----------
violation "改 Scenario 步驟文字" 5 \
  "sed -i 's/Then 看板應該顯示 2 個 Swimlane/Then 看板應該顯示 3 個 Swimlane/' $F01; git commit -qam '[spec/design](basic-kanban) 演練改步驟'"
violation "刪一列變更紀錄" 6 \
  "sed -i '/CR-003 | 新增/d' $F01; git commit -qam '[spec/design](basic-kanban) 演練刪變更紀錄'"
violation "加 @wip 狀態 tag" 7 \
  "sed -i 's/  @added @CR-003/  @added @wip @CR-003/' $F01; git commit -qam '[spec/design](basic-kanban) 演練加 wip'"
violation "刪 @CR- tag" 7 \
  "sed -i '0,/  @CR-001\$/s//  @uc-add-swimlane/' $F01; git commit -qam '[spec/design](basic-kanban) 演練刪 CR tag'"
violation "改 spec-convention.md" 3 \
  "echo x >> .dev/conventions/spec-convention.md; git commit -qam '[docs](loops) 演練改規範'"
violation "改遷移前備份" 3 \
  "echo x >> .dev/F01-basic-kanban/legacy-spec-kanban-basic.md; git commit -qam '[docs](loops) 演練改備份'"
violation "改檢查腳本" 3 \
  "echo '# x' >> scripts/speccheck/ids.py; git commit -qam '[docs](loops) 演練改腳本'"
violation "任務清單改驗收條件" 4 \
  "sed -i 's/| \`errors(F01)=0\` | T1.05 |/| 看起來不錯 | T1.05 |/' $LOOP/spec-migration-tasks.md; git commit -qam '[docs](loops) 演練改驗收條件'"
violation "PDCA 改舊紀錄" 4 \
  "sed -i 's/^### Plan\$/### Plan（改）/' $LOOP/spec-migration-pdca.md; git commit -qam '[docs](loops) 演練改 PDCA'"
violation "沒有追加 PDCA" 4 \
  "sed -i 's/^- 更新：.*/- 更新：演練/' $LOOP/spec-migration-state.md; git commit -qam '[docs](loops) 演練只改 state'"
violation "OQ 缺「採用」欄" 11 \
  "echo '| OQ-01 | 2026-09-16 | F01 | 情況 | A／B |  | Swimlane 管理 | 自動決議 |' >> $LOOP/spec-migration-open-questions.md; append_pdca T0.01; git commit -qam '[docs](loops) 演練 OQ'"
violation "OQ 改舊列" 11 \
  "sed -i 's/^| 編號 |/| 編號（改） |/' $LOOP/spec-migration-open-questions.md; append_pdca T0.01; git commit -qam '[docs](loops) 演練改 OQ'"
violation "沒達成就標 done" 8 \
  "sed -i 's/^| T0.01 | todo |/| T0.01 | done |/; s/^| T0.02 | todo |/| T0.02 | done |/' $LOOP/spec-migration-tasks.md; append_pdca T0.02; git commit -qam '[docs](loops) 演練假完成'"
violation "commit 訊息格式錯誤" 2 \
  "append_pdca T0.01; git commit -qm 'update pdca'"
violation "commit 類型與檔案不符" 2 \
  "sed -i 's/我想要新增、命名、排序與刪除 Swimlane/我想要 新增、命名、排序與刪除 Swimlane/' $F01; git commit -qam '[docs](loops) 演練類型錯'"
violation "規格與 loop 文件混在同一個 commit" 2 \
  "sed -i 's/我想要新增、命名、排序與刪除 Swimlane/我想要 新增、命名、排序與刪除 Swimlane/' $F01; append_pdca T0.01; git commit -qam '[spec/design](basic-kanban) 演練混合'"
violation "留下未提交的檔案" 1 \
  "echo x > stray.txt"
violation "doing 任務讓 error 變多" 9 \
  "sed -i 's/^| T1.01 | todo |/| T1.01 | doing |/' $LOOP/spec-migration-tasks.md; append_pdca T1.01; git commit -qam '[docs](loops) 演練標 doing'; echo '正文引用 \`not-defined-entity\`' >> $F01; git commit -qam '[spec/design](basic-kanban) 演練讓錯誤變多'"
violation "偽造關卡核准檔" 15 \
  "sleep 1; mkdir -p $LOOP/runtime/gates; touch $LOOP/runtime/gates/G1.approved; append_pdca T0.01; git commit -qam '[docs](loops) 演練'"
violation "條件未達成就建立 DONE" 16 \
  "touch $LOOP/runtime/DONE; append_pdca T3.04; git commit -qam '[docs](loops) 演練'"
MODE=review violation "審查輪修改規格" 14 \
  "sed -i 's/我想要新增、命名、排序與刪除 Swimlane/我想要 新增、命名、排序與刪除 Swimlane/' $F01; git commit -qam '[spec/design](basic-kanban) 演練'"
MODE=review violation "審查輪修改 T 任務狀態" 4 \
  "sed -i 's/^| T0.01 | todo |/| T0.01 | done |/' $LOOP/spec-migration-tasks.md; echo x >> $LOOP/spec-migration-review.md; git commit -qam '[docs](loops) 演練'"

# F 編號偏移只發警告
reset_repo
sed -i '0,/Given 看板目前有 1 個 Swimlane "預設泳道"/s//Given 看板目前有 1 個 Swimlane "預設泳道"（依 F03 規格）/' "$F01"
cp "$F01" .dev/F01-basic-kanban/legacy-spec-kanban-basic.md
git commit -qam "[chore] 演練前置：備份與新檔都含 F 編號"
base="$(git rev-parse HEAD)"
sed -i 's/（依 F03 規格）/（依 F04 規格）/' "$F01"
git commit -qam '[spec/design](basic-kanban) 修正 F 編號偏移'
append_pdca T0.01
git commit -qm '[docs](loops) 演練'
verify "$base" exec
if head -1 "$WORK/report.md" | grep -q PASS && grep -q '\[5\].*F 編號' "$WORK/report.md"; then
  record "F 編號 ±1 修正" yes "驗證 PASS，警告 [5] 交給審查輪確認"
else
  record "F 編號 ±1 修正" no "$(head -30 "$WORK/report.md" | tr '\n' ' ' | trunc 300)"
fi

# ---------- 情境 3：多輪 ----------
small_ledger() {  # 人工把任務清單換成演練用的短清單（沒有機械條件）
  python3 - "$LOOP/spec-migration-tasks.md" <<'EOF'
import re, sys
p = sys.argv[1]
s = open(p, encoding="utf-8").read()
keep = {"T0.01", "T0.02", "G1", "T1.01", "T1.02"}
out = []
for line in s.split("\n"):
    m = re.match(r"^\| (T\d+\.\d+|G\d+) \|", line)
    if m and m.group(1) not in keep:
        continue
    if m:
        # 去掉機械條件，並把依賴重接成 T0.01 → T0.02 → G1 → T1.01 → T1.02
        line = re.sub(r"`(errors|crcheck)\([^)]*\)=0`", "演練", line)
        deps = {"T0.01": "—", "T0.02": "T0.01", "G1": "T0.02", "T1.01": "G1", "T1.02": "T1.01"}
        cells = line.split("|")
        cells[5] = f" {deps[m.group(1)]} "
        line = "|".join(cells)
    out.append(line)
open(p, "w", encoding="utf-8").write("\n".join(out))
EOF
  git commit -qam "[chore] 演練前置：短任務清單"
}

reset_repo
small_ledger
: > "$FAKE_CALLS"
MAX_ITERATIONS=8 REVIEW_EVERY=2 bash "$LOOP/run-spec-migration-loop.sh" > "$WORK/s3a.log" 2>&1
calls="$(tr '\n' ' ' < "$FAKE_CALLS")"
if [ "$calls" = "exec review exec review exec review exec " ] \
  && grep -q '自動核准關卡 G1（審查後無待修正任務）' "$WORK/s3a.log" \
  && python3 "$LOOP/spec-migration-tools.py" rows "$LOOP/spec-migration-tasks.md" G1 T1.02 | grep -c '| done |' | grep -q 2 \
  && ! grep -q 'FAIL' "$WORK/s3a.log"; then
  record "多輪：週期審查＋關卡自動核准" yes "輪次 $calls；G1 在審查後自動核准，下一個執行輪標 done"
else
  record "多輪：週期審查＋關卡自動核准" no "輪次 $calls；見 $WORK/s3a.log"
fi
if grep -q '沒有可執行的任務' "$WORK/s3a.log" || grep -q 'MAX_ITERATIONS' "$WORK/s3a.log"; then
  record "多輪：任務做完後停止" yes "$(grep -E '沒有可執行的任務|MAX_ITERATIONS' "$WORK/s3a.log" | head -1 | trunc 80)"
else
  record "多輪：任務做完後停止" no "見 $WORK/s3a.log"
fi

reset_repo
small_ledger
: > "$FAKE_CALLS"
AUTO_APPROVE_GATES=0 MAX_ITERATIONS=8 REVIEW_EVERY=0 bash "$LOOP/run-spec-migration-loop.sh" > "$WORK/s3b.log" 2>&1
if grep -q '等待人工關卡 G1' "$WORK/s3b.log" && [ ! -f "$LOOP/runtime/gates/G1.approved" ]; then
  record "多輪：人工關卡模式" yes "審查一次後停下等 touch gates/G1.approved（輪次 $(tr '\n' ' ' < "$FAKE_CALLS")）"
else
  record "多輪：人工關卡模式" no "見 $WORK/s3b.log"
fi
touch "$LOOP/runtime/gates/G1.approved"
sleep 1
: > "$FAKE_CALLS"
AUTO_APPROVE_GATES=0 MAX_ITERATIONS=1 REVIEW_EVERY=0 bash "$LOOP/run-spec-migration-loop.sh" > "$WORK/s3c.log" 2>&1
if head -1 "$LOOP/runtime/last-verify.md" | grep -q PASS \
  && python3 "$LOOP/spec-migration-tools.py" rows "$LOOP/spec-migration-tasks.md" G1 | grep -q '| done |'; then
  record "多輪：人工核准後接續" yes "建立核准檔後重啟，執行輪把 G1 標 done 並繼續 T1.01"
else
  record "多輪：人工核准後接續" no "見 $WORK/s3c.log"
fi

reset_repo
: > "$FAKE_CALLS"
FAKE_MODE=bad MAX_ITERATIONS=10 REVIEW_EVERY=0 bash "$LOOP/run-spec-migration-loop.sh" > "$WORK/s3d.log" 2>&1
if grep -q '連續 3 輪驗證失敗' "$WORK/s3d.log" && [ "$(wc -l < "$FAKE_CALLS")" -eq 3 ]; then
  record "多輪：連續失敗停止" yes "$(grep '連續 3 輪驗證失敗' "$WORK/s3d.log" | trunc 90)"
else
  record "多輪：連續失敗停止" no "見 $WORK/s3d.log"
fi

echo "# 演練結果（$(date '+%F %T')，暫存目錄 $WORK）"
printf -- '- %s\n' "${results[@]}"
echo
echo "通過 $pass 項，失敗 $failed 項"
[ "$failed" -eq 0 ]
