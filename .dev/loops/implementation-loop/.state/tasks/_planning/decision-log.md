# 安排階段 決策紀錄（`_planning`）

> 安排階段（`planning-prompt.md`）自己的紀錄。任務各自的決策紀錄在 `.state/tasks/<task-id>/decision-log.md`。

## 2026-09-22 安排階段重跑（驅動腳本「無可執行任務」觸發）

### 決策 1：備註欄補上「涵蓋 CR」，值依 spec／ui 的 `@CR-` tag 與各實例合併時點回溯認定

- **決策**：替 `.state/tasks.md` 23 列既有任務全部補上「涵蓋 CR：…」，並在「規則」段補上兩條（備註欄格式、修訂實例 `-rN`）。
- **依據**：`iteration-prompt.md` 第 2 節「備註欄固定記「涵蓋 CR：CR-xxx, CR-yyy」…這是安排階段判斷程式碼是否落後 spec 的唯一依據」；先前幾輪安排階段沒有寫入這一欄，本輪第 5-1 步的 A−B 比對缺基準，先補齊。
- **回溯認定方式**：各列的值不是「spec 現在全部的 tag」，而是「該實例實作／合併當時 spec 已有的 tag」。依 `iteration-prompt.md` 第 3 節第 4 條「安排階段第一次排任務時依『規格現在定案的樣子』實作，已併入 spec 的 CR 不重放」，若直接把現況 tag 全記為未涵蓋，會替每個主體都誤開修訂實例。逐列比對 `git log` 的合併 commit 與各 CR 的 spec commit 先後順序後認定：
  - `T-01-be-user` = CR-006（spec `18f8099` → 程式碼 `cd0a1b8`，同批處理）。`uc-login` 的 `@CR-007` 由專屬任務 `T-21-cr007-topbar-display-name` 承接（`fe8546d`／`dc61fda`，已合併 `048fa06`），故 `user` 主體的 tag 集合 {CR-006, CR-007} 已被兩個 done 實例的聯集涵蓋，不追加修訂實例。
  - `T-02-be-board` = CR-001, CR-003（F01 `@CR-001` 掛在 `uc-add-swimlane`…`uc-delete-stage`、`@CR-003` 掛在 `uc-set-stage-role`，皆屬 `board` 主體）。
  - `T-03-be-card` = CR-001, CR-002, CR-010, CR-011（F01 `@CR-001`／`@CR-002` 掛在 `uc-add-card`／`uc-edit-card`…；CR-010 `38143cb`、CR-011 `84efd39` 兩筆在 T-03 合併 `240b1db` 之後才進來，但兩筆 commit 本身已同時改到 spec、`Board.java`／`CardApplicationService`／`CardController`／`CardSteps.java` 與 feature 檔，程式碼已跟上，故計入涵蓋、不開修訂實例）。
  - `T-04-be-board-membership` = CR-009（F02 `uc-create-board` 的兩個 `@CR-009` tag，step definition 依該列備註併入本任務）。
  - `T-05-be-board-clock` = 無。F04 spec 全檔沒有 `@CR-` tag；CR-004 的變更說明自己寫明『既有 Scenario 文字不需修改，故不掛 Scenario 層級 tag』，不是漏打 tag，故不適用第 5-3 步的 `blocked`＋OQ。
  - `T-06-be-kanban-widgets` = CR-012（spec `ddf6cfe` → 程式碼 `a773e7a`「WipCalculator 排除 Done 角色 Stage」，人工複核 `bee7582` 核准 status=done）。
  - 前端任務全部為「無」：七份 `ui-*.md` 全檔沒有任何 `@CR-` tag，第 5-4 步的比對集合 A 為空。CR-008 只動 `ui-user-membership.md` `s-signup` 的驗收條件文字，已在 `T-11-fe-auth` 範圍內完成，另行在該列備註說明。

### 決策 2：追加修訂實例 `T-08-be-feature-cr-board-r2`（CR-013）

- **決策**：新增一列 `T-08-be-feature-cr-board-r2`，依賴 `T-08-be-feature-cr-board`（需已合併），產出範圍只寫 CR-013 的差異，狀態不建 `status` 檔（＝`todo`）。
- **依據**：第 5-1 步比對——`spec-feature-cr-board.md` 現有 `@CR-013`（集合 A={CR-013}），`T-08-be-feature-cr-board` 這個唯一的 done 實例涵蓋 CR 為空（集合 B=∅），A−B={CR-013} 非空。時序佐證：T-08 合併 commit 是 `9b3819e`，CR-013 的 spec commit `aadc005` 在其之後，且該 commit 只改了 `.dev/CR.md`／`spec-feature-cr-board.md`／T-08 的 `open-questions.md` 三個檔，沒有碰 `kanban-spring`；CR-013 的驗收標準也明寫『本 CR 只涵蓋 spec 修正，程式碼變更留待後續任務／D-xx 處理』。
- **不 blocked 的理由**：第 5-3 步只在「CR.md 有列但 spec 沒打 tag、或 tag 指向的實體找不到」時才標 `blocked`。CR-013 的 `@CR-013` tag 確實掛在 `uc-view-feature-cr-board` 上，該 usecase 在 spec 中存在，範圍有明確依據，不需推斷。
- **依賴方向（第 5-2 步）**：CR-013 只波及 F06 一個主體，前端 `T-20-fe-feature-cr-board` 尚未開工（`todo`、無 `@CR-` tag），依第 5-1 步末句直接留在原列，不開修訂實例。

### 決策 3：本輪不改任何既有 `status` 檔

- **決策**：23 個既有任務的 `status` 一律不動；沒有任何 `blocked` 列需要解除，也沒有新開 `blocked`。
- **依據**：`planning-prompt.md`「**不可以改已經存在的 `status` 檔**…除非是把已補齊依據的 `blocked` 改回 `todo`」。目前 `scripts/collect.sh status` 顯示 12 done、11 todo、0 blocked，沒有 `blocked` 可解除；新增的 `T-08-be-feature-cr-board-r2` 不建 `status` 檔（不存在＝`todo`）。

## 2026-09-22 安排階段重跑（第二次，T-08-be-feature-cr-board-r2 合併後）

### 決策 4：本輪校對後不改動 `.state/tasks.md` 任何一列

- **決策**：24 列任務的 ID／產出範圍／依賴／備註全部維持原狀，不新增、不刪除、不重寫；也不改任何既有 `status` 檔。
- **依據（第 5-1 步 A−B 比對，逐主體）**：
  - `.dev/F0*/spec-*.md` 現況 `@CR-` tag 集合：F01 = {CR-001, CR-002, CR-003, CR-010, CR-011}、F02 = {CR-006, CR-007, CR-009}、F03 = {CR-012}、F06 = {CR-013}、F04／F05／F07 = ∅。
  - 七份 `ui-*.md` 全檔仍無任何 `@CR-` tag，第 5-4 步的集合 A 為空。
  - `board` 主體：A = {CR-001, CR-003}，B（`T-02-be-board` done）= {CR-001, CR-003}；`uc-create-board` 的 CR-009 由 `T-04-be-board-membership`（done，涵蓋 CR：CR-009）承接。A−B = ∅。
  - `card` 主體：A = {CR-001, CR-002, CR-010, CR-011} = B（`T-03-be-card` done）。A−B = ∅。
  - `user` 主體：A = {CR-006, CR-007}，B = `T-01-be-user`（CR-006）∪ `T-21-cr007-topbar-display-name`（CR-007）。A−B = ∅。
  - F03 投影：A = {CR-012}，B（`T-06-be-kanban-widgets` done）= {CR-012}；已複查程式碼確認 `WipCalculator.countByStage` 有 `.filter(s -> s.role() != StageRole.DONE)`，Javadoc 亦註明「post p1『不含 Done 角色 Stage』（CR-012）」，與 spec Scenario「檢視各 Stage 目前的卡片數量」的 `Then`『不顯示 Done 角色的 Stage "完成"』一致。A−B = ∅。
  - F06 投影：A = {CR-013}，B = `T-08-be-feature-cr-board`（無）∪ `T-08-be-feature-cr-board-r2`（CR-013，本輪之前已 done 並由 `574f8f6` 合併）= {CR-013}。A−B = ∅，上一輪追加的修訂實例已消化，不再追加 `-r3`。
- **上游文件無新變動**：`git log -- .dev/F0*/ .dev/CR.md` 最新兩筆仍是 `aadc005`（CR-013）與 `ddf6cfe`（CR-012），皆為 2026-09-22 且已在上一輪安排階段認定完畢；`.dev/` 底下仍只有 F01～F07，`ui-prototype/` 的 21 份 `.dev.html` 與 `README.md` 未增減。故沒有任何列的產出範圍或依賴需要更新。
- **規則段核對**：`.state/tasks.md`「規則」段的狀態值定義、挑選順序（依賴全 `done` 且已合併、`doing` < 5）、`D-xx`、`blocked` 解除、顆粒度、備註欄「涵蓋 CR」格式、修訂實例 `-rN` 七條，與 `iteration-prompt.md` 第 0／2／3／4 節一致，無缺漏需補。

### 決策 5：`OQ-T-08-be-feature-cr-board-r2-01` 不轉成任務列

- **決策**：不為這則 OQ 開任務列，也不因它把 `T-20-fe-feature-cr-board` 或 F06 任何實例標 `blocked`。
- **依據**：該 OQ 問的是「一張 CR 卡有多個 affects 目標、部分有對應 Feature 卡部分沒有時，互斥判定以目標還是以卡片為單位」，Review 已記明「阻塞：否」「接手：人工」，且 `.dev/CR.md` 尚未為它開新 CR。依 `iteration-prompt.md` 第 3 節第 4 條，修訂實例的觸發條件是「spec 又因 CR 變動」；目前 spec 沒有變動，沒有可寫的差異範圍。人工定案並開 CR 後，下一輪安排階段會依第 5-1 步自動追加 `T-08-be-feature-cr-board-r3`。

### 決策 6：四筆 CR.md「待處理」狀態欄由人工翻，安排階段不代勞也不視為落後

- **決策**：`.dev/CR.md` 的 CR-007／CR-009／CR-012／CR-013 狀態欄仍是「待處理」，但這四筆的程式碼都已落地（分別由 `T-21`／`T-04`／`T-06`／`T-08-be-feature-cr-board-r2` 完成並合併），本輪不因狀態欄而追加任務。
- **依據**：`iteration-prompt.md` 第 2 節把 `.dev/**` 列為「上游依據：**不可修改**」，安排階段不能改狀態欄；而第 5-1 步的落後判定依據是 spec 的 `@CR-` tag 與各列「涵蓋 CR」，不是 CR.md 的狀態欄。四列的備註都已載明「完成並驗收後，人工把 `.dev/CR.md` 改『處理完成』」。
- **提醒（給人工）**：依 `CLAUDE.md`「CR 狀態改成『待處理』／改回『處理完成』是保護開關」，這四筆滯留在「待處理」會讓 `cr-check` 的 GH-05 持續嚴格擋 F02／F03／F06 的格式性修訂，建議盡快翻回「處理完成」。
