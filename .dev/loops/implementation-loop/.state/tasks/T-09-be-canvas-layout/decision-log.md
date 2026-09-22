# T-09-be-canvas-layout 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：實作 canvas-layout 三個 Aggregate

### 涵蓋範圍

`spec-canvas-layout.md` 五個 Feature、共 11 個 use case 全部實作：`uc-init-canvas`、`uc-place-item`、
`uc-remove-item`、`uc-move-item`、`uc-resize-item`、`uc-set-item-capabilities`、`uc-set-item-anchor`、
`uc-reorder-item`、`uc-move-items`、`uc-remove-items`、`uc-set-viewport`。對應實體 `canvas`／`item`／
`viewport` 三個獨立 Aggregate Root，皆為 `kanban-core` 新增。無跳過或延後項目。

### 實作決策

1. **`viewport.user` 型別**：spec 欄位表寫 string(100)（帳號模組定案後改為 ref），本任務直接採用既有
   `User` 的 `UUID` id，與 `BoardMembership.userId`、`Board.createdBy` 等既有欄位一致（低風險技術決定，
   `Viewport.java` 類別註解已說明）。
2. **`item.canvas`／`viewport.canvas` 內部關聯**：欄位表沒有列出這兩個欄位，但關係表定義
   `canvas→item`／`canvas→viewport` 為 0..n，且 `item.z` 唯一鍵、`viewport.user` 唯一鍵都是「同一
   `canvas` 內」——比照 `Card` 雖然欄位表沒有 `card.board` 卻仍持有 `boardId` 的既有慣例，`Item`／
   `Viewport` domain 物件內部持有 `canvasId`（不對外於 usecase 語意曝露為新欄位）。
3. **`item.z` 與置頂／置底的計算職責切分**：`Item` domain 類別本身不查詢其他 `item`（維持 Aggregate
   邊界），z 值計算（同一錨定方式最大值加一、置頂／置底）都在 `CanvasApplicationService` 完成後傳入
   `Item` 的方法，`Item` 只做傳入值的套用與能力檢查。
4. **HTTP 路由設計**：單一元素操作（移動／調整大小／能力／錨定／層序／移除）掛在
   `/api/canvas-items/{itemId}`，比照 `CardController` 對獨立 Aggregate 的既有慣例（不需要 boardId 也
   能定位，權限由 item→canvas→board 反查）；批次操作與檢視區設定因為需要先定位 canvas，掛在
   `/api/boards/{boardId}/canvas/...`。
5. **批次操作的跨 canvas 防護**：`uc-move-items`／`uc-remove-items` spec 沒有明講「所選元素都要屬於
   同一個 canvas」，但層序唯一鍵、Background 情境都隱含這個前提；`CanvasApplicationService` 額外檢查
   每個指定 item 的 `canvasId` 是否等於路徑 boardId 對應的 canvas，不符合時視為「找不到」（防止跨
   board 竄改，非 spec 明訂但不違反任何定稿內容的工程防呆）。
6. **HTTP 狀態碼分配**：spec 只定義失敗訊息文字，沒有定義狀態碼；已開 **OQ-T-09-be-canvas-layout-01**
   （高、不阻塞）記錄目前的 409/404/400 分配方式與依據。
7. **Cucumber 一檔一 Feature 限制**：`spec-canvas-layout.md` 五個 Feature 拆成
   `canvas-init.feature`／`canvas-item-placement.feature`／`canvas-item-arrangement.feature`／
   `canvas-item-batch.feature`／`canvas-viewport.feature` 五個檔案（Gherkin 語法一檔只能有一個
   `Feature:`），內容為對應 Feature 段落的逐字複製。
8. **Cucumber step definition 精簡**：spec 的座標／能力／錨定描述子句排列組合極多（例如
   「錨定於畫布，左上角 (x, y)，大小 w × h，可移動」各種子集合），改用少量以 Java 正規表示式
   （`^...$`，cucumber-jvm 會自動辨識為 Regular Expression）撰寫的共用 step definition，逐一解析
   「，」／「、」／「且」分隔的子句（`CanvasSteps.applyDescriptorClause`），避免窮舉每一種排列組合各寫
   一個方法。另外「畫布中存在元素 "X"」這段文字在 spec 裡同時當 Given（建立前置資料）與 Then（斷言
   存在），Cucumber 依文字比對 step definition 不分關鍵字，兩種用法合併成同一個方法
   （`givenOrThenItemExists`），避免 `DuplicateStepDefinitionException`／`AmbiguousStepDefinitionsException`。

### Check

- `./gradlew :kanban-core:test --no-daemon`：BUILD SUCCESSFUL（Canvas／Item／Viewport 單元測試）。
- `./gradlew :kanban-spring:test --no-daemon --tests "io.progden.kanban.spring.cucumber.RunCucumberTest"`：
  BUILD SUCCESSFUL，130 個 Cucumber scenario（含既有六個模組＋本次新增 canvas-layout 五個 Feature）
  全數通過。
- `./gradlew build --no-daemon`：BUILD SUCCESSFUL（含 `kanban-core`／`kanban-spring` 全部 test、
  `NoSpringDependencyTest` 亦通過，`kanban-core` 不依賴 Spring）。

### 待確認事項

- OQ-T-09-be-canvas-layout-01（高、不阻塞）：canvas-layout 失敗情境的 HTTP 狀態碼分配，spec 未定義，
  已依現有 `BoardController` 慣例（409/404/400）實作，供後續確認或調整。
