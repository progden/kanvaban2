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

## 2026-09-22 Review 第 2 輪：退回

### 1. 自己跑建置與完整測試

- `./gradlew clean build --no-daemon`（前景執行）：**BUILD SUCCESSFUL in 3m 9s，14 actionable tasks: 14 executed**。
- 42 份 `TEST-*.xml` 逐檔檢查 `failures="0" errors="0"`，無任何失敗或錯誤，也沒有 skipped。
- canvas 五個 feature 檔 Scenario 數 3＋8＋19＋7＋6＝43，與 spec 的 43 個 Scenario 相同。

### 2. spec 對應

- 以 python 把 spec 的 5 個 ```gherkin 區塊與 5 個 `.feature` 檔逐行比對（略過註解行）：五檔全部 `OK`，一字不差。
- `@fail-pN` 共 19 個，五檔中「拒絕，訊息為 …，且資料不變」步驟也正好 19 個（1／9／5／3／1），
  每個失敗情境都真的走到共用的 `thenRejected`。
- **D-01 複驗**：`CanvasSteps.thenRejected`（`CanvasSteps.java:383-386`）現在比對 `preActionSnapshot` 與
  `snapshotState()`；`snapshotState()` 把 `canvas_items` 全部欄位（id／canvasId／component／anchor／x／y／
  width／height／z／movable／resizable／removable）與 `viewports`（id／canvasId／userId／x／y／zoom）
  **複製成不可變 Map 的值**，不是持有 entity 參照，所以不會因為物件被就地改動而自動相等。
  全部 13 個 `@When` step 都在送出請求前呼叫了 `captureSnapshot()`（grep 逐一核對）。
  斷言也不是空轉：`CanvasSteps` 與 `CucumberSpringConfiguration` 都沒有 `@Transactional`，測試端沒有跨
  step 的 persistence context，兩次 `findAll()` 各自開一個交易、真的重新讀 H2，資料若被寫進去會被抓到。
- `@fail-p2` 抽查「不可移動設為不可移動的元素」：`CanvasApplicationService.moveItem` 為 `@Transactional`，
  `item.move(x, y)` 內先 `ensureMovable()` 才改欄位、`save` 在其後，加上交易回滾，「且資料不變」成立，
  且現在有測試守住。

### 3. kanban-core 純度

- `grep -rn "org.springframework|jakarta.persistence|@Entity|@Autowired|lombok" kanban-core/src/main/java` 無命中。
- 本任務沒有跨 aggregate 讀取投影，`io.progden.kanban.query.*` 未被動到（正確）。

### 4. 任務邊界

- `git diff loop/implementation...HEAD --stat`：47 檔，全部落在 `kanban-core/**`、`kanban-spring/**` 與
  `.state/tasks/T-09-be-canvas-layout/**`。`.dev/conventions/**`、`scripts/**`、spec／ui 本體、
  `.state/tasks.md`、`.state/archive/**`、別的任務目錄皆未異動；`git status --porcelain` 空白。

### 5. OQ 核對

- `OQ-...-01`（HTTP 狀態碼）：三段引文與 spec 第 285／415／456 行逐字相符；spec 未定義狀態碼，
  照做不必違反任何原文，「高、不阻塞」正確。
- `OQ-...-02`（`item.z` 唯一鍵）：三段引文與 spec 第 37 行（欄位表限制欄）、第 166 行（`uc-place-item` post）、
  第 61 行（其他名詞「層序」）逐字相符；實作 `ItemJpaEntity.java:19` 唯一鍵確為 `{canvas_id, anchor, z}`，
  與 OQ 描述一致。`spec-canvas-layout.md` 第 14 行為『狀態：草稿』，非定稿，故「高、不阻塞、接手：人工」正確。
- `OQ-...-03`（Background 繞過 `uc-init-canvas`）：`uc-init-canvas` post 第 2 條（第 100 行）與 Background
  『Given 畫布已由系統建立』（第 198 行）逐字相符，**但第二段引用的 Scenario 標題『Scenario: 放置畫布元素』
  在 spec 裡不存在**（原文第 203 行為『Scenario: 放置元件到空畫布』，grep「放置畫布元素」零命中）——
  違反 CLAUDE.md「逐字引用、不可改寫」的規則，列為 D-04。
- D-02／D-03 要求的補 OQ 動作本身已完成（兩則都用【兩處矛盾並列】、接手人工），只有 D-03 產出的 OQ 引文有上述瑕疵。

### 6. 前端

本任務為後端任務，不適用設計稿核對。

### 判定：退回（doing）

程式碼、測試、邊界都沒有問題，D-01～D-03 的實質要求都做到了；唯一未過的是 D-03 產出的
`OQ-...-03` 引文不是原文（D-04）。因 `loopctl` 不能改既有 OQ 內文，請 Dev 另開一則逐字正確、
註明取代 OQ-...-03 的 OQ，實作與測試不必更動。

另記一件不構成退回理由、也不列 D-xx 的觀察（沿用第 1 輪）：`CanvasApplicationService.moveItems` 在
`itemIds` 為空陣列時會於 `items.get(0)` 丟 IndexOutOfBoundsException；spec 未定義空選取行為，不要求處理。
