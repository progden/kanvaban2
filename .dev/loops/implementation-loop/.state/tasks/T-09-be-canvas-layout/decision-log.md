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

## 2026-09-22 Dev 第 2 輪：D-01～D-03 處理：補資料不變快照、補開 OQ

### D-01～D-03 處理

- D-01（改測試）：`CanvasSteps.thenRejected` 之前只驗 HTTP 狀態碼與 `message`，spec
  `Then 拒絕，訊息為 "..."，且資料不變` 的後半段沒被斷言。新增 `captureSnapshot()`／
  `snapshotState()`：在每個可能導向「拒絕」的 When 方法（開啟看板、放置元件、移動／
  調整大小／設能力／設錨定／置頂置底／移除、批次移動／批次移除、設定檢視區，共 12 處呼叫點，
  逐一對照 grep 出的 12 個 `@fail-pN` Scenario 前置 When）送出請求前，對全庫 `canvas_items`／
  `viewports` 逐欄（含 id／x／y／width／height／z／anchor／movable／resizable／removable／
  canvasId／userId／zoom）拍照；`thenRejected` 拒絕後重新拍照，`assertEquals` 逐欄比對含筆數。
  用全庫掃描而非只掃 `currentCanvasId`，是因為「看板不存在」情境連 canvas 都還沒建立。
  12 個 `@fail-pN` Scenario 全數維持通過（見下方 Check）。
- D-02（補 OQ，實作不改）：`item.z` 唯一鍵 `(canvas_id, anchor, z)` 與欄位表限制欄逐字
  『同一 canvas 內唯一』不同，但與 `uc-place-item` post／「層序」段落一致。開立
  `OQ-T-09-be-canvas-layout-02`（情況：兩處矛盾並列，逐字引用欄位表限制欄、post 第 2 條、
  「層序」段落；選項 A 維持現況＋修欄位表文字、B 改唯一鍵＋修 post／層序文字）。
- D-03（補 OQ，實作不改）：Background「畫布已由系統建立」用直接寫 DB 的空白 canvas，
  繞過 `uc-init-canvas`（其 post 第 2 條要求一定帶 z=1 看板本體 item）。開立
  `OQ-T-09-be-canvas-layout-03`（情況：兩處矛盾並列，逐字引用 post 第 2 條與 Background／
  「畫布中沒有任何元素」；選項 A 維持空白 canvas 前置＋修 spec 排除看板本體、B 改走
  `uc-init-canvas` 端點＋回頭修所有依賴「無元素」「z 從 1 起算」的 Scenario）。

### Check

`./gradlew clean build --no-daemon`：BUILD SUCCESSFUL in 3m 11s，14 actionable tasks: 14 executed。
`kanban-spring` 五個 canvas feature 檔測試結果檔逐一確認：canvas-init 3、
canvas-item-arrangement 19、canvas-item-batch 7、canvas-item-placement 8、
canvas-viewport 6，全部 `skipped="0" failures="0" errors="0"`，合計 43，與上一輪一致。
`kanban-core` 單元測試（CanvasTest／ItemTest／ViewportTest／NoSpringDependencyTest）與
`kanban-spring` 其餘 feature 檔本輪未改動，重跑同樣全綠。

### 下一輪 Review 要先看什麼

`CanvasSteps.java` 的 `captureSnapshot`／`snapshotState`／`thenRejected` 三處改動，
確認 12 個 `@fail-pN` Scenario 的快照比對是否真的會在資料被寫入時失敗（例如人工暫時
移除某個 `ensureMovable()` 前置檢查、重跑測試觀察是否轉紅，若需要可作為驗證手段）。
`OQ-T-09-be-canvas-layout-02`／`03` 的引文與選項是否合理。
