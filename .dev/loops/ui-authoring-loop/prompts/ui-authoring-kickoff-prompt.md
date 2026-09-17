# 提示詞：UI 撰寫 —— 執行輪（每次全新 context）

你是一次性啟動的 worker，只做這一輪：完成任務清單中的**一個任務**。你不記得任何先前對話，下一輪只看得到檔案與 commit。本輪結束後由 `verify-ui-authoring.sh` 外部驗證，你自己的回報不算數。

以下 `<loop>` 代表 `.dev/loops/ui-authoring-loop`，`<prompts>`／`<state>`／`<scripts>` 代表其下的 `prompts/`／`.state/`／`scripts/` 子目錄，`tools` 代表 `python3 <scripts>/ui-authoring-tools.py`。所有規則一律以 `<prompts>/ui-authoring-prompt.md`（規則書）為準，本檔只描述流程；規則書的「角色」「目標」「鐵則」「自主決策分級」「輸入怎麼讀」「操作與需確認判斷」全部照舊適用，不因為加了外部驗證而放寬。

**最重要的三條（細節見規則書）**：

1. 不定義 spec 沒有的實體／屬性／角色／Use Case／Screen；推不出來的資訊走「自主決策分級」，標 `⚠️` 記 OQ，不自己補。
2. 不寫業務結果、不寫排版與視覺（`ui-convention.md`「明確不寫的東西」）。
3. 只改 `.dev/F0x-*/ui-*.md`、任務清單、PDCA、OQ 檔、`<state>/ui-authoring-state.md`；spec、規範、腳本、`design-*.md` 一律不動。

## 一、讀什麼（依序）

1. `<loop>/runtime/DONE` 存在 → 回報「已完成」並結束。
2. `<state>/ui-authoring-state.md`：目前階段、上一輪任務、剩餘待辦、最近 OQ。
3. `<loop>/runtime/last-verify.md`（不存在代表第一輪）：結果若為 FAIL，失敗項目要最先修正。
4. `<prompts>/ui-authoring-prompt.md`：全文，特別是鐵則、自主決策分級、操作與需確認判斷。
5. `tools actionable <state>/ui-authoring-tasks.md`、`tools rows <state>/ui-authoring-tasks.md <第一個任務>`、`git log --oneline -15`、`git status`：確認 state 與任務清單、git 一致；不一致時以任務清單與 git 為準。
6. 本輪任務對應的 `spec-<模組>.md`（該任務指定的 Use Case、名詞定義三張表、角色定義），以及已完成的其他 `ui-*.md`（跨模組／同檔銜接）；需要時才讀 `.dev/conventions/ui-convention.md`、`docs-convention.md`。
7. 只有需要追溯（state 資訊不足、要查某個決定的來由）時，才讀 `<state>/ui-authoring-pdca.md` 與 `<state>/ui-authoring-open-questions.md` 的相關紀錄。

## 二、決定本輪任務

依序判斷，取第一個成立的：

1. 上一輪驗證 FAIL → 先修正失敗項目，再繼續 state 記載的同一個任務。
2. `actionable` 第一個是已核准的 `G*`（`<loop>/runtime/gates/<G>.approved` 存在）→ 先在任務清單把它標為 `done`，再重跑 `actionable`，本輪任務取新的第一個；關卡的狀態變更跟本輪收尾的 `[docs](loops)` commit 一起提交。
3. 否則 → `actionable` 第一個任務（`doing` 或 `D-xx` 會自動排在前面）。

開工前先寫下（放進 PDCA Plan）：本輪任務、要改的檔案與段落、驗收條件、預期的 `ui-check` error 數變化。

## 三、做什麼

1. 依規則書「輸入怎麼讀」「操作與需確認判斷」撰寫或修改 `ui-<模組>.md`；一輪只碰任務 `[F0x]` 指定的那份檔案（跨模組收尾任務例外，例如 T2.11 會同時碰 F01／F02 兩份）。
2. 隨時自我檢查：
   - `tools error-count <ui檔>`：這份檔案目前的 error 數（檔案還不存在時視為 0）。
   - `tools accept-check <state>/ui-authoring-tasks.md <任務 ID>`：驗收條件裡 `ui-check(<檔或 all>)=0` 這類機械 token 是否成立；沒有這種 token 的任務（多數單一畫面任務）改用 `./scripts/ui-check <檔> --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 肉眼核對規則書「執行單位：一輪一個任務」列出的判準——**單檔案務必加 `--spec`**，否則跨模組引用會被誤判成 `REF-07`（見規則書「執行單位」開頭與 OQ-10）；`tools` 系列已經內建這個邏輯，不用另外加。
   - 本輪若新增或修改了 OQ 表列、或 ui 檔「待確認事項」裡標【引用原文】／【矛盾】／【推論】／【覆蓋】的『』引用 → `python3 <scripts>/verify-quotes.py`，非 0 視為本輪未完成。
3. 遇到判斷依規則書「自主決策分級」：低風險寫進 PDCA Do；高風險／覆蓋 spec／環境限制依規則書處理（標 `⚠️`、記 OQ、視情況把任務標 `blocked`），然後低風險、高風險**繼續做**，只有環境限制與覆蓋 spec 才停在 `blocked`。
4. 發現計畫外的必要工作 → 任務清單「發現的任務」表追加 `D-xx`，狀態直接 `todo`（本 loop 沒有 proposed／rejected 中間狀態，描述與驗收條件要把判斷依據寫清楚）。
5. 驗收條件達成 → 進入收尾；不要接著開始下一個任務。做不完 → 確認 `error-count` 沒有比開工前多，任務標 `doing`，進入收尾。

## 四、怎麼收尾

0. 確認沒有任何指令還在背景執行；`ui-<模組>.md` 的變更已 commit。
1. 任務清單：完成的任務標 `done`、做一半的標 `doing`、已核准的關卡標 `done`（只改狀態欄，依規則書「commit 規範」）。
2. **覆寫** `<state>/ui-authoring-state.md`（20 行內，照該檔現有欄位）。
3. 在 `<state>/ui-authoring-pdca.md` **檔尾追加**一則：
   ```markdown
   ## Iteration <最後一則序號 + 1> — <YYYY-MM-DD HH:MM> — <本輪處理的任務編號>
   ### Plan
   （本輪任務、要改的檔案與段落、驗收條件、預期 error 數變化）

   ### Do
   （本輪所有 commit：hash + 訊息第一行；低風險決定；新增的 OQ-xx／D-xx）

   ### Check
   （`./scripts/ui-check <本輪檔案> --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 的最後一行（`N error(s), M warning(s)`，檔案還不存在時貼「找不到任何 ui 檔」）；
     `tools accept-check` 結果；逐條對照驗收條件是否達成；`verify-quotes.py` 有跑的話貼結果）

   ### Act
   （完成：下一個任務編號；未完成：剩餘工作清單）
   ```
4. 任務清單、state、PDCA、OQ 檔以規則書「commit 規範」對應的 commit 提交（ui 檔一個 commit、loop 文件一個 commit）；確認 `git status` 乾淨。
5. 若本輪完成的是 T7.02 且驗收條件全部達成：建立空檔 `<loop>/runtime/DONE`（不進版控）。
6. 結束，用一句話回報：本輪任務、是否達成、下一個任務。

## 不可以

- 向使用者提問或等待回覆；`git push`；建立 `<loop>/runtime/gates/*.approved`。
- 改驗收條件、任務描述、依賴；改 PDCA 或 OQ 檔的舊內容。
- 為了讓 `ui-check` 通過而刪減段落、亂填「不適用」、把「討論中」硬標成「已定案」。
