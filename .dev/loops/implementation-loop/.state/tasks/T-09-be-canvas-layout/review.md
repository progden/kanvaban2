# T-09-be-canvas-layout 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：退回

### 1. 自己跑建置與完整測試

- `./gradlew build --no-daemon`：第一次全部 UP-TO-DATE（等於沒有真的跑測試），因此改跑
  `./gradlew clean build --no-daemon`，結果 **BUILD SUCCESSFUL in 3m 21s，14 actionable tasks: 14 executed**。
- 測試結果檔逐一確認 `failures="0" errors="0"`：`kanban-core` 含新增的 `CanvasTest`（4）／`ItemTest`（17）／
  `ViewportTest`（2）與 `NoSpringDependencyTest`（1）；`kanban-spring` 21 個 feature 檔全綠。
- canvas 五個 feature 檔的 Scenario 數：canvas-init 3、canvas-item-placement 8、canvas-item-arrangement 19、
  canvas-item-batch 7、canvas-viewport 6，合計 43，與 `spec-canvas-layout.md` 的 43 個 Scenario 一致。

### 2. spec 對應

- 11 個 uc（`uc-init-canvas`／`uc-place-item`／`uc-remove-item`／`uc-move-item`／`uc-resize-item`／
  `uc-set-item-capabilities`／`uc-set-item-anchor`／`uc-reorder-item`／`uc-move-items`／`uc-remove-items`／
  `uc-set-viewport`）全部有對應的 Cucumber Scenario 執行紀錄，無缺漏。
- 以 python 逐字比對 spec 五個 ```gherkin 區塊與五個 `.feature` 檔：除了檔首兩行來源註解外**完全一致**。
- 角色檢查與 spec `roles` 相符：`uc-init-canvas`／`uc-set-viewport`（含 `r-canvas-viewer`）走 `ensureMember`，
  其餘走 `ensureCanEdit`。失敗訊息文字與 spec 的 `"..."` 全部相符（含批次與單一操作用不同訊息）。
- `@fail-pN` 抽查：`uc-move-item @fail-p2`「不可移動設為不可移動的元素」。程式碼上
  `Item.move` 先 `ensureMovable()` 才改欄位，寫入在其後，行為成立；**但測試本身沒有驗證「且資料不變」**，
  見 D-01。

### 3. kanban-core 純度

- `grep -rn "org.springframework|jakarta.persistence|@Entity|@Autowired" kanban-core/src/main/java` 無任何命中，
  `NoSpringDependencyTest` 亦通過。`Canvas`／`Item`／`Viewport` 皆為純 Java，z 值計算等跨 item 查詢留在
  application 層。本任務沒有跨 aggregate 讀取投影，`io.progden.kanban.query.*` 未被動到（正確）。

### 4. 任務邊界

- `git diff loop/implementation...HEAD --stat`：45 檔，全部落在 `kanban-core/**`、`kanban-spring/**`
  與 `.state/tasks/T-09-be-canvas-layout/**`。`.dev/conventions/**`、`scripts/**`、spec／ui 本體、
  `.state/tasks.md`、`.state/archive/**`、別的任務目錄都沒有被改。共用檔只動到
  `ErrorCode.java`（純新增 9 個 canvas 用列舉值），屬本任務必要範圍。工作區乾淨。

### 5. OQ 核對

- `OQ-T-09-be-canvas-layout-01`（HTTP 狀態碼）：三段引文（uc-move-item／uc-resize-item／uc-remove-item 的
  `Then 拒絕，訊息為 "..."，且資料不變`）與 spec 第 285／415／456 行逐字相符；spec 確實沒有任何狀態碼定義，
  做完本任務不必違反任何定稿原文，所以「高、不阻塞」正確；「接手：無」可接受（狀態碼慣例是人工決定的事，
  T-13-fe-canvas-shell 只是消費端，屆時須一併知會）。此則不影響核准。
- 但 Dev 另外有**兩件同樣屬於「spec 兩處文字互相拉扯」的決定只寫在 Java 註解／完全沒寫**，沒有依規則開 OQ：
  `item.z` 唯一鍵範圍（D-02）、Background「畫布已由系統建立」改用空白 canvas 前置（D-03）。

### 6. 前端

本任務為後端任務，不適用設計稿核對。

### 判定：退回（doing）

三條 D-xx：D-01（fail 情境的「且資料不變」完全沒被斷言，12 個 Scenario 受影響）、D-02（`item.z`
唯一鍵偏離欄位表原文未開 OQ）、D-03（Background 繞過 `uc-init-canvas` 且無紀錄）。D-02／D-03 只要求補 OQ，
實作可維持現況；D-01 要改測試。

另記一件不構成退回理由的觀察（不列 D-xx，避免腦補 spec 未定義的行為）：
`CanvasApplicationService.moveItems` 在 `itemIds` 為空陣列時會在 `items.get(0)` 丟
IndexOutOfBoundsException（HTTP 500）。spec 沒有定義空選取的行為，故不要求本輪處理。
