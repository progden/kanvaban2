# 提示詞：spec 遷移 —— 執行輪（每次全新 context）

你是一次性啟動的 worker，只做這一輪：完成任務清單中的**一個任務**。你不記得任何先前對話，下一輪只看得到檔案與 commit。本輪結束後由 `verify-spec-migration.sh` 外部驗證，你自己的回報不算數。

以下 `<loop>` 代表 `.dev/loops/spec-migration-loop`，`tools` 代表 `python3 <loop>/spec-migration-tools.py`。所有規則一律以 `<loop>/spec-migration-prompt.md`（規則書）為準，本檔只描述流程。

**最重要的三條（細節見規則書）**：

1. gherkin 行為必須與同目錄的 `legacy-spec-*.md` 備份逐字一致；只能加 `@uc-`／`@fail-`、改 Aggregate 註解、改「身為」行、去反引號，以及修正「有檔案可對照、剛好差一號」的 F 編號。看起來寫錯的其他內容一律保留，記 OQ。
2. usecase 區塊的每一句都要能指到某個 Scenario 步驟；推不出來就走假設分級，不自己補行為。
3. 只改 `.dev/F0x-*/spec-*.md`、`.dev/CR.md`、任務清單、state、PDCA、OQ 檔；備份、規範、腳本、loop 檔案一律不動。

## 一、讀什麼（依序）

1. `<loop>/runtime/DONE` 存在 → 回報「已完成」並結束。
2. `<loop>/spec-migration-state.md`：目前階段、上一輪任務、剩餘待辦、最近假設。
3. `<loop>/runtime/last-verify.md`（不存在代表第一輪）：結果若為 FAIL，失敗項目要最先修正。
4. `<loop>/spec-migration-prompt.md`：全文，特別是鐵則、假設分級、遷移程序。
5. `tools actionable <loop>/spec-migration-tasks.md`、`tools rows <loop>/spec-migration-tasks.md <第一個任務>`、`git log --oneline -15`、`git status`：確認 state 與任務清單、git 一致；不一致時以任務清單與 git 為準。
6. 本輪任務對應的 spec 檔、它的 `legacy-spec-*.md` 備份，以及新格式範本 `scripts/tests/fixtures/good/.dev/F01-basic-kanban/spec-kanban-basic.md`；需要時才讀 `.dev/conventions/spec-convention.md` 的相關章節、`checks.md` 的檢查說明、其他模組已遷移的 spec（查已定義的實體與角色 ID）。
7. 只有需要追溯（state 資訊不足、要查某個決定的來由）時，才讀 `<loop>/spec-migration-pdca.md` 與 `<loop>/spec-migration-open-questions.md` 的相關紀錄。

## 二、決定本輪任務

依序判斷，取第一個成立的：

1. 上一輪驗證 FAIL → 先修正失敗項目，再繼續 state 記載的同一個任務。
2. `actionable` 第一個是已核准的 `G*`（`<loop>/runtime/gates/<G>.approved` 存在）→ 先在任務清單把它標為 `done`（依賴它的任務要等它 done 才會出現），再重跑 `actionable`，本輪任務取新的第一個；關卡的狀態變更跟本輪收尾的 `[docs](loops)` commit 一起提交。
3. 否則 → `actionable` 第一個任務（`doing` 或 `D-xx` 會自動排在前面）。

開工前先寫下（放進 PDCA Plan）：本輪任務、要改的檔案與段落、驗收條件、預期的 error 數變化。

## 三、做什麼

1. 依規則書「遷移程序」中任務涵蓋的步驟修改 spec；一輪只碰任務 `[F0x]` 指定的那份 spec（T0.02、T3.02 改 `.dev/CR.md`；T3.x 可跨檔）。
2. 隨時自我檢查：
   - `tools accept-check <loop>/spec-migration-tasks.md <任務 ID>`：驗收條件的機械條件（無輸出、回傳 0 才算達成）。
   - `tools gherkin-diff <spec>`、`tools tag-diff <spec>`、`tools changelog-check <spec>`：鐵則 1。`gherkin-diff` 回傳 3 代表只有 F 編號 ±1 的差異，必須是你刻意修正的檔案參考，並寫進 PDCA Do。
   - `tools error-count <spec>`：這份檔案的 error 數（全部 spec 一起解析）。要看明細跑 `./scripts/spec-check | grep <檔名>`；**不要**單獨跑 `./scripts/spec-check <檔>`，跨模組引用會被誤報。
3. 遇到判斷依規則書「假設分級」：低影響寫進 PDCA Do；高影響在 OQ 檔表格末尾追加 `OQ-xx`（「採用」「依據」兩欄必填）並在該 spec `## 待釐清` 加一行指向它，然後**繼續做**；只有環境限制才把任務標 `blocked`。
4. 發現計畫外的必要工作 → 任務清單「發現的任務」追加 `D-xx`（`proposed`），本輪不做。
5. 規格改完先 commit：`[spec/design](<模組名>) <摘要>`（`.dev/CR.md` 用 `[chore](cr) <摘要>`），只 `git add` 該檔。
6. 驗收條件達成 → 進入收尾；不要接著開始下一個任務。做不完 → commit 已完成的部分（確認 `error-count` 沒有比開工前多），任務標 `doing`，進入收尾。

## 四、怎麼收尾

0. 確認沒有任何指令還在背景執行；規格與 CR.md 的變更都已各自 commit。
1. 任務清單：完成的任務標 `done`、做一半的標 `doing`、已核准的關卡標 `done`（只改狀態欄）。
2. **覆寫** `<loop>/spec-migration-state.md`（20 行內，照該檔現有欄位）。
3. 在 `<loop>/spec-migration-pdca.md` **檔尾追加**一則：
   ```markdown
   ## Iteration <最後一則序號 + 1> — <YYYY-MM-DD HH:MM> — <本輪處理的任務編號>
   ### Plan
   （本輪任務、要改的檔案與段落、驗收條件、預期 error 數變化）

   ### Do
   （本輪所有 commit：hash + 訊息第一行；低影響假設；新增的 OQ-xx；F 編號修正的原文 → 新文與對照檔案）

   ### Check
   （`./scripts/spec-check` 不帶參數的最後一行（`N error(s), M warning(s)`）；`tools error-count <spec>` 開工前 → 收尾時；
     `tools accept-check` 結果；`gherkin-diff`／`tag-diff`／`changelog-check` 結果；逐條對照驗收條件是否達成）

   ### Act
   （完成：下一個任務編號；未完成：剩餘工作清單）
   ```
4. 任務清單、state、PDCA、OQ 檔以**同一個** `[docs](loops) <摘要>` commit 提交；確認 `git status` 乾淨。
5. 若本輪完成的是 T3.04 且驗收條件全部達成：建立空檔 `<loop>/runtime/DONE`（不進版控）。
6. 結束，用一句話回報：本輪任務、是否達成、下一個任務。

## 不可以

- 向使用者提問或等待回覆；`git push`；建立 `<loop>/runtime/gates/*.approved`。
- 改驗收條件、任務描述、依賴；改 PDCA 或 OQ 檔的舊內容。
- 為了讓檢查通過而改 gherkin 行為、刪 Scenario、改規範或腳本。
