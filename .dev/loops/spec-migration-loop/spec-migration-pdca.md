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

## Iteration 2 — 2026-09-16 20:00 — T0.02
### Plan
本輪任務 T0.02：建立 `.dev/CR.md`，依 `cr-convention.md` §6 表頭登記 CR-001～CR-004（狀態「處理完成」，影響 ID 先空）與 CR-005「規格格式遷移至 usecase 區塊」（狀態「修改規格」），每筆下方寫背景／變更內容／驗收標準。要改的檔案：`.dev/CR.md`（新建）。驗收條件：`.dev/CR.md` 存在且主表有 CR-001～CR-005 五列；`errors(F01;REF-06)=0`。預期 error 總數會下降（各 spec 裡 `@CR-001`～`@CR-004` 的 REF-06 未登記錯誤會消失）。

### Do
- commit 488dea0 `[chore](cr) 建立 CR 總表登記 CR-001 至 CR-005`
- CR-001～CR-004 的標題、類型、日期、影響模組依各 spec 變更紀錄摘要整理（F01 37-41 行、F02 45-46/423-431 行、F04 8/29-31 行）；影響 ID、明細欄依任務指示留空。
- CR-005 影響模組列六份 spec；狀態設為「修改規格」（尚未完成遷移）。
- 提出人欄無原始資料，依規則書「你是這個 Kanban 專案的 SA」填「SA」（低影響假設，純格式選擇，不影響驗收）。
- 未發生 F 編號偏移修正。

### Check
- `./scripts/spec-check`：`433 error(s), 0 warning(s)`（開工前 461 → 收尾 433，下降 28，全部是 REF-06「未登記」錯誤消失）。
- `tools error-count`：F01 107→84、F02 141→139、F03 62→62、F04 66→63、F05 44→44、F06 41→41。
- `tools accept-check T0.02`：exit 0，通過。
- 本輪未改任何 gherkin 區塊，`gherkin-diff`／`tag-diff`／`changelog-check` 不適用（未動 `.dev/F0x-*/spec-*.md`）。
- 驗收條件逐條核對：`.dev/CR.md` 存在且主表有 CR-001～CR-005 五列（達成）；`errors(F01;REF-06)=0`（F01 已無 REF-06 錯誤，達成）。

### Act
完成，下一個任務：T1.01（[F01] 遷移程序 1～6）。

## Iteration 3 — 2026-09-16 21:00 — T1.01
### Plan
本輪任務 T1.01：對 `.dev/F01-basic-kanban/spec-kanban-basic.md` 執行遷移程序 1～6：加狀態行、把原名詞表拆成實體／欄位／關係／其他名詞四張表、新增角色定義表、修正三個 Feature 的「身為」行與 `## Feature:` 標題。只改這一份檔案。驗收條件：`errors(F01;GH-08,REF-03,REF-04,REF-05,REF-09)=0`；原名詞表五列說明都要能在新表找到。預期 F01 error 數會從 84 明顯下降（GH-08／REF-03～05／REF-09 這批結構性錯誤消失），但 UC-01 等 usecase 相關錯誤（留給 T1.02～04）仍會存在。

### Do
- commit feea843 `[spec/design](basic-kanban) 遷移名詞定義、角色定義與 Feature 標頭至新格式`
- 實體表：`board`（root，含 Swimlane、Stage）、`swimlane`（board）、`stage`（board）、`card`（root），說明文字照搬原名詞表對應列。
- 欄位表：`swimlane.name`（非空）、`stage.name`、`stage.role`（enum NONE/START/DONE，START/DONE 各至多一個）、`card.title`（非空）、`card.description`、`card.due-date`、`card.labels`、`card.swimlane`（ref，建立時必填）、`card.stage`（ref，建立時必填）——皆可指到既有 Scenario 步驟或原名詞表。
- 關係表：`board`→`swimlane`（1..n，對應「看板至少保留一個 Swimlane」Scenario）、`board`→`stage`（1..n，同理）、`swimlane`→`card`（0..n）、`stage`→`card`（0..n）。
- 其他名詞：「Stage 角色（role）」「操作時間（occurredAt）」照搬原文，僅去除 code 反引號改「」（`BoardClock.now()` → 「BoardClock.now()」），檔案路徑維持反引號。
- 角色定義：只設一個 `r-user`（看板使用者），因三個 Feature 的「身為」原文皆相同（低影響假設，純命名選擇，寫入 state）。
- 三個 Feature 的「身為看板的使用者」改為「身為 看板使用者」（gherkin-diff 會忽略此行，已用工具確認）。
- 修正 H2 標題與 gherkin `Feature:` 名稱不一致的兩處（原文只在 H2，不動 gherkin，屬遷移程序步驟 6 允許的修改，非 F 編號偏移）：
  - 「## Feature: Stage 管理」→「## Feature: Stage（階段）管理」（對齊 gherkin `Feature: Stage（階段）管理`）
  - 「## Feature: Card 編輯」→「## Feature: Card（卡片）編輯」（對齊 gherkin `Feature: Card（卡片）編輯`）
- 低影響假設：`card.due-date` 用 kebab-case 而非原詞 dueDate，因欄位 ID 格式（`ATTR_RE`）不允許大寫字母；留言（新增留言 Scenario）與活動紀錄未建立獨立實體，沿用 fixture 範例作法（後續 uc 的 crud 直接標 `card: U`／`board: U`），因 Aggregate 註解與原名詞表都未把它們列為獨立名詞。
- gherkin 區塊內容（Feature/Background/Scenario/步驟/tag/Aggregate 註解）完全未動，只改了 H2 標題文字與「身為」行。

### Check
- `tools gherkin-diff .dev/F01-basic-kanban/spec-kanban-basic.md`：exit 0，「Gherkin 行為與遷移前一致（160 行）」。
- `tools tag-diff` 同檔：exit 0，無輸出。
- `tools changelog-check` 同檔：exit 0，無輸出（本輪未改變更紀錄）。
- `tools error-count`：F01 開工前 84 → 收尾 38。
- `tools accept-check <tasks> T1.01`：exit 0，通過。
- `./scripts/spec-check`：`321 error(s), 4 warning(s)`（全部 error 總數 433 → 321）。
- 原名詞表 5 列逐列對照：Board／Swimlane／Stage／Card 說明搬到實體表對應列（文字不變）；Stage 角色（role）、操作時間（occurredAt）搬到其他名詞表（文字不變，僅去反引號）——全部找到，無遺漏。

### Act
完成，下一個任務：T1.02（[F01]「Swimlane 管理」Feature 的 usecase 區塊＋tag／Aggregate 註解對齊）。
