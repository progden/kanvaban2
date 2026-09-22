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
