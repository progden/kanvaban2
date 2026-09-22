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

## 2026-09-22 Dev 第 3 輪：D-04 修正：另開正確 OQ 取代 OQ-03

### D-04：另開逐字正確的 OQ 取代 OQ-T-09-be-canvas-layout-03

- Review 第 2 輪指出 `OQ-T-09-be-canvas-layout-03` 第二段引文把 Scenario 標題寫成『Scenario: 放置畫布元素』，但 `spec-canvas-layout.md` 第 203 行原文是『Scenario: 放置元件到空畫布』，`grep -n "放置畫布元素"` 零命中，違反逐字引用規則。
- `loopctl` 無法修改既有 OQ 內文，依 D-04 指示另開 `OQ-T-09-be-canvas-layout-04`：內文開頭註明取代 OQ-03，三段引文（`uc-init-canvas` post 第 2 條、Background『Given 畫布已由系統建立』、`Scenario: 放置元件到空畫布` 連同其『Given 畫布中沒有任何元素』）已對照 spec 第 100／198／202～204 行逐字核對後照貼，推論與選項沿用原 OQ-03 內容不變。
- OQ-01～OQ-04 皆為「高、不阻塞」，`spec-canvas-layout.md` 為草稿狀態，接手皆為人工，不影響核准；OQ-03 保留在檔案中作為歷史紀錄，人工接手時以 OQ-04 為準。

### 本輪範圍

- 只處理 D-04，未改動任何 `kanban-core`／`kanban-spring` 程式碼或測試（Review 已確認 D-01～D-03 的實質要求都已達成，D-04 純粹是 OQ 引文文字修正）。
- 未重跑建置：本輪無程式碼／測試變更，上一輪（Review 第 2 輪）已確認 `./gradlew clean build --no-daemon` BUILD SUCCESSFUL、42 份測試檔 failures=0 errors=0。

### 待確認事項

- `OQ-T-09-be-canvas-layout-01`／`02`／`04`：接手人工，供 spec 定稿時處理，不阻塞本任務核准。

## 2026-09-22 Dev 第 4 輪：修 D-05：批次操作 itemIds 空值 no-op

### D-05 修正：批次操作 itemIds 為空或缺欄位時的語意選擇

`CanvasApplicationService.moveItems`／`removeItems` 原本在 `itemIds` 為空陣列時對
`items.get(0)` 丟 `IndexOutOfBoundsException`，JSON 缺該欄位（Jackson 反序列化為 `null`）時
`itemIds.stream()` 丟 `NullPointerException`，`CanvasController.withOperator` 只 catch
`DomainException`，兩者都穿透成 HTTP 500。

`uc-move-items`／`uc-remove-items` 的 `pre` 逐字為『指定的每個 `item` 皆存在』等，空集合下三個
`pre` 皆真空成立（vacuously true），屬規格未定義而非規格違反。選擇語意：**視為 no-op 成功**，
而非以 `DomainException` 拒絕——理由是空集合本來就滿足所有 `pre`，沒有正當理由回絕一個沒有做任何
事的請求；改成拒絕反而要新增一個 spec 沒提到的錯誤碼，且與「批次操作全成功或全失敗」的既有語意
（空集合視為「全部（零個）成功」）更一致。

實作：`ensureCanEdit` 權限檢查維持在最前面（no-op 不代表跳過權限），`itemIds == null ||
itemIds.isEmpty()` 時 `moveItems` 直接回傳 `List.of()`、`removeItems` 直接 return，不再載入
canvas／item。

補測試：`CanvasApplicationServiceTest`（`kanban-spring` 層整合測試）涵蓋 `itemIds` 為空陣列與
`null` 兩種輸入 × `moveItems`／`removeItems` 共 4 個案例，皆不丟例外、`moveItems` 回傳空清單。
未動 `canvas-*.feature` 五個逐字複製的 spec 檔。

### Check

- `./gradlew clean build --no-daemon`：BUILD SUCCESSFUL in 3m 11s，14 actionable tasks: 14 executed。
- 43 份測試結果檔（`kanban-core`＋`kanban-spring`）逐檔 `skipped="0" failures="0" errors="0"`，
  含新增的 `TEST-io.progden.kanban.spring.application.CanvasApplicationServiceTest.xml`（4 個測試
  全過）。canvas 五個 feature 檔合計仍為 43 個 Scenario，未變動。
- `git status --porcelain` 於 commit 後為空，本輪只動
  `CanvasApplicationService.java`（+10/-2）與新增的測試檔。
