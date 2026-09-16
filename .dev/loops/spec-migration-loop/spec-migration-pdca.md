# spec 遷移工作日誌（PDCA）

本檔是 [`spec-migration-prompt.md`](./spec-migration-prompt.md) 的執行日誌，供每一輪重置 context 的迴圈（[`run-spec-migration-loop.sh`](./run-spec-migration-loop.sh)）追溯決策。**只能在檔案最後追加新紀錄，不可覆寫或刪除舊紀錄**。每則標題格式：`## Iteration <n> — <YYYY-MM-DD HH:MM> — <任務編號>`，內含 `### Plan`／`### Do`／`### Check`／`### Act` 四段，Check 必須貼 `./scripts/spec-check` 的結果行。

---

## Iteration 0 — 2026-09-16（建立 loop 與演練）

### Plan
依 `.dev/prompts/spec-migration-loop-prompt.md` 建立 spec 遷移 loop（放在 `.dev/loops/spec-migration-loop/`，runtime 在同目錄 `runtime/`），並用假的 claude 演練控制流程；本則不做任何遷移。

### Do
- 六份 spec 備份為同目錄 `legacy-spec-<模組>.md`，作為行為比對基準（取代「對 baseline commit 比對」）；新檔直接改寫，舊的變更紀錄列也可以改格式，但摘要文字要保留。
- F 編號只在「有檔案參考、剛好偏移一號」時可修正，驗證腳本對這類差異只發警告，由審查輪確認；其他看似寫錯的內容保留並記 OQ。
- 驗收條件的機械語法：`errors(F01)=0`、`errors(F01#<Feature>)=0`、`errors(F01;<檢查 ID>)=0`、`errors(all)=0`、`crcheck(CR-005)=0`。
- 單檔 `spec-check <檔>` 會把跨模組引用誤報成 error，所以 `error-count` 一律全部 spec 一起解析、再依檔案過濾。
- commit scope 不能用 `F01`（`git-convension.md` 的正則只允許小寫與連字號），規格 commit 改用模組名（`basic-kanban` 等）；loop 文件用 `[docs](loops)`。

### Check
起點：461 error(s), 0 warning(s)（F01 107、F02 141、F03 62、F04 66、F05 44、F06 41）；`python3 -m unittest discover -s scripts/tests` 7 項通過。

演練（`bash .dev/loops/spec-migration-loop/rehearsal/rehearse.sh`，假的 claude、暫存 repo，約 1 分鐘）：29 項全部符合預期。

- 情境 1 合規的一輪：T0.01 標 done、追加 PDCA → PASS，last-progress=yes。
- 情境 2 違規，各自被擋下的檢查：改 Scenario 步驟 [5]、刪變更紀錄列 [6]、加 `@wip` [7]、刪 `@CR-` [7]、改 `spec-convention.md` [3]、改備份檔 [3]、改檢查腳本 [3]、任務清單改驗收條件 [4]、PDCA 改舊紀錄 [4]、沒追加 PDCA [4]、OQ 缺「採用」[11]、OQ 改舊列 [11]、沒達成就標 done [8]、commit 訊息格式錯 [2]、commit 類型與檔案不符 [2]、規格與 loop 文件混在一個 commit [2]、留下未提交檔案 [1]、doing 任務讓 error 變多 [9]、偽造關卡核准檔 [15]、條件未達成就建 DONE [16]、審查輪改規格 [14]、審查輪改 T 任務狀態 [4]。
- F 編號 ±1 修正：PASS，只留警告 [5]。
- 情境 3 多輪（短任務清單、`REVIEW_EVERY=2`）：輪次 exec → review → exec → review（G1 前）→ 自動核准 → exec（G1 與 T1.01 標 done）→ review → exec，任務做完後停止；`AUTO_APPROVE_GATES=0` 時審查一次後停下等核准檔，建立後重啟可接續；每輪都違規時同一任務連續 3 輪失敗即停止。

演練抓到並已修正的坑：驗收條件的分隔符號原本用 `|`，會切壞 Markdown 表格欄位（改成 `;`）；已核准關卡要先標 done，依賴它的任務才會出現在 `actionable`（kickoff 已寫明）。

### Act
下一個任務：T0.01。

---

## Iteration 1 — 2026-09-16 15:00 — T0.01

### Plan
本輪任務：T0.01（確認 baseline 與起點，純確認性任務，不改任何 spec 檔）。驗收條件：PDCA 本則 Check 含 baseline hash、六份備份檔名、各檔 error 數與 `spec-check` 結果行。預期 error 數不變（461）。

### Do
- 確認 `runtime/baseline` 存在，內容為 `3038528d4656e64e176809ab2ad0a794b74e84c3`（對應 git log 最新 commit）。
- 確認六份 `legacy-spec-*.md` 備份皆存在（F01～F06，見 Check）。
- 跑 `tools errors-json` 取得各檔起點 error 數。
- 本輪未修改任何 spec 或 CR.md，只更新任務清單狀態與本 PDCA。

### Check
- baseline hash：`3038528d4656e64e176809ab2ad0a794b74e84c3`
- 六份備份檔：
  - `.dev/F01-basic-kanban/legacy-spec-kanban-basic.md`
  - `.dev/F02-user-membership/legacy-spec-user-membership.md`
  - `.dev/F03-kanban-widgets/legacy-spec-kanban-widgets.md`
  - `.dev/F04-board-clock/legacy-spec-board-clock.md`
  - `.dev/F05-workload/legacy-spec-workload.md`
  - `.dev/F06-feature-cr-board/legacy-spec-feature-cr-board.md`
- 各檔 error 數（`tools errors-json`）：F01 107、F02 141、F03 62、F04 66、F05 44、F06 41、all 461
- `./scripts/spec-check` 最後一行：`461 error(s), 0 warning(s)`
- `tools accept-check <tasks> T0.01`：exit 0，符合驗收條件
- `gherkin-diff`／`tag-diff`／`changelog-check`：本輪未改任何 spec，不適用

### Act
完成：T0.01。下一個任務：T0.02。
