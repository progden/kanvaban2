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

## 2026-09-22 Review 第 3 輪：退回

### 1. 自己跑建置與完整測試（未採信 Dev 交接摘要）

Dev 第 3 輪自述「未重跑建置」，因此本輪從頭跑一次：

- `./gradlew clean build --no-daemon`：**BUILD SUCCESSFUL in 3m 8s**，14 actionable tasks: 14 executed。
- 逐一讀 `kanban-spring/build/test-results/test/*.xml` 與 `kanban-core/build/test-results/test/*.xml`：42 份結果檔全部 `skipped="0" failures="0" errors="0"`。canvas 五個 feature 檔：canvas-init 3、canvas-item-placement 8、canvas-item-arrangement 19、canvas-item-batch 7、canvas-viewport 6，合計 43。

### 2. spec 對應核對

- `spec-canvas-layout.md` 的 ```gherkin 區塊共 43 個 `Scenario`，五個 feature 檔合計也是 43。以 Python 逐行比對 spec 的 gherkin 區塊與五個 feature 檔的非空行：feature 檔只多出 5 組共 10 行的中文註解（「本檔為 … 的逐字複製」），**spec 沒有任何一行遺漏**，確認是逐字複製。
- 11 個 use case 的 `@uc-` tag 全部出現在 feature 檔：`uc-init-canvas` 3、`uc-place-item` 5、`uc-remove-item` 3、`uc-move-item` 4、`uc-resize-item` 7、`uc-set-item-capabilities` 2、`uc-set-item-anchor` 3、`uc-reorder-item` 3、`uc-move-items` 4、`uc-remove-items` 3、`uc-set-viewport` 6；`@fail-p1` 11、`@fail-p2` 5、`@fail-p3` 2、`@fail-p4` 1。
- **`@fail-pN` 的「且資料不變」抽查（突變測試）**：把 `Item.move`（`Item.java` 第 61 行）的 `ensureMovable();` 暫時換成註解、重跑 `./gradlew :kanban-spring:test --tests "…RunCucumberTest"`，結果 **130 tests completed, 1 failed**，失敗的正是 `canvas-item-arrangement.feature:41` 的 `拒絕，訊息為 "此元素不可移動"，且資料不變`（`CanvasSteps.thenRejected:382`，訊息『預期操作被拒絕，實際狀態碼為 200』）。確認 D-01 的快照斷言不是裝飾，護欄拿掉真的會轉紅。驗證後已 `cp` 還原 `Item.java`，`git status --porcelain` 為空。
- 批次操作原子性：`CanvasApplicationService.moveItems`／`removeItems` 都是先全部載入、全部驗證（`ensureMovable`／`ensureRemovable`／`mixedAnchor`）才逐一套用，符合 `uc-move-items`／`uc-remove-items` 三個 `fail` 的『拒絕，資料不變』。
- 角色檢查：寫入型 uc 走 `boardMembershipApplicationService.ensureCanEdit`、`uc-set-viewport` 走 `ensureMember`，與 `.state/tasks.md` T-09 備註的 `r-canvas-editor`＝Owner／Member 對應一致。

### 3. `kanban-core` 純度

`grep -rnE "org\.springframework|jakarta\.persistence|@Entity|@Autowired" kanban-core/src/main/java/` 零命中；`NoSpringDependencyTest` 通過。本任務三個 Aggregate（`canvas`／`item`／`viewport`）皆非跨 aggregate 讀取投影，`io.progden.kanban.query.*` 未被改動，符合分層。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：47 檔、+3340/-2。全部落在 `kanban-core`／`kanban-spring` 的 canvas 相關新檔、五個 `canvas-*.feature`、`CanvasSteps.java`，加上 `.state/tasks/T-09-be-canvas-layout/**`。唯一動到既有檔的是 `ErrorCode.java`（+20/-2，只追加 9 個 canvas 錯誤碼、既有列不變）。沒有動到別的 aggregate、`.dev/conventions/**`、`scripts/**`、spec／ui 文件本體，也沒有動 `.state/tasks.md`／`.state/archive/**`／別的任務目錄。**邊界乾淨。**

### 5. OQ 核對

`spec-canvas-layout.md` 第 14 行為『狀態：草稿』，四則 OQ 皆不需要違反任何**已定稿**原文即可完成任務，因此「高、不阻塞」的標記正確。

- **OQ-01**（HTTP 狀態碼，spec 未定義）：引用的三段失敗訊息『此元素不可移動』『此元素不可調整大小』『此元素不可移除』在 spec 中均可 `grep -F` 命中；屬「規格沒寫」而非「違反定稿」，不阻塞正確。接手填「無」，但 `T-13-fe-canvas-shell` 會消費這些端點——保留事項見下。
- **OQ-02**（`item.z` 唯一鍵範圍）：三段引文（欄位表限制欄『必填、同一 `canvas` 內唯一』、`uc-place-item` post、「層序」段落）逐字比對 spec，各命中 1 次。兩處矛盾並列格式正確，接手「人工」正確（要改的是 spec 文字）。
- **OQ-03**：引文『Scenario: 放置畫布元素』在 spec 中 `grep -cF` 為 0，確為 Review 第 2 輪指出的問題；已由 OQ-04 取代。
- **OQ-04**（取代 OQ-03）：三段引文逐字比對——`uc-init-canvas` post 第 2 條（spec 第 100 行，整句照貼無誤）、『Given 畫布已由系統建立』（命中 4 次）、『Scenario: 放置元件到空畫布』與『Given 畫布中沒有任何元素』（各命中 1 次）——**全部逐字相符，D-04 已達成**。
- Dev 交接摘要列出的待確認事項與 `loopctl show` 的 OQ 清單一致，沒有只寫在摘要裡卻沒開成 OQ 的項目。

### 6. 前端設計稿

本任務為後端任務，不適用。

### 判定：退回（`doing`）

第 1～5 點除了下述一項之外都通過，但發現一個規格未涵蓋、卻會讓伺服器丟出未攔截例外的缺陷，已開 **D-05**：

`CanvasApplicationService.moveItems` 第 192 行 `items.get(0).getAnchor()` 在 `itemIds` 為空陣列時丟 `IndexOutOfBoundsException`；`MoveItemsRequest`／`RemoveItemsRequest` 對 `itemIds` 沒有任何驗證，JSON 缺該欄位時 `itemIds.stream()` 丟 `NullPointerException`；`CanvasController.withOperator`（第 176～180 行）只 catch `DomainException`，兩者都會穿透成 HTTP 500 且回應主體不是 `ErrorResponse`。`uc-move-items` `pre` p1 逐字為『指定的每個 `item` 皆存在』，空集合下三個 `pre` 皆為真、`post` 亦為真，所以這不是規格違反，而是規格未定義的邊界目前落到伺服器錯誤上——必須改成一個明確定義的回應（no-op 成功或以 `DomainException` 拒絕，擇一並記入決策紀錄），並補測試。

本輪只寫 `D-05` 與本紀錄，未改動任何產出程式碼（`Item.java` 的突變驗證已還原，工作區乾淨）。

### 若下一輪修好 D-05，屆時的保留事項與接手者

- `OQ-T-09-be-canvas-layout-01`（HTTP 狀態碼分配 409/404/400）：接手目前填「無」。實際會受影響的是 **`T-13-fe-canvas-shell`**（依賴 T-09，會消費這些端點）；定案若改變狀態碼，`T-13` 要一併調整錯誤處理。建議 Dev 下一輪在 OQ-01 或決策紀錄中補記這層關係（不另要求改 OQ 的接手欄，`loopctl` 無法改既有 OQ 內文）。
- `OQ-T-09-be-canvas-layout-02`（`item.z` 唯一鍵範圍）：接手 **人工**，需修 `spec-canvas-layout.md` 文字（草稿，不需 CR）。定案為選項 B 時要回頭改 `ItemJpaEntity` 唯一鍵與 `nextZ`。
- `OQ-T-09-be-canvas-layout-04`（Background「畫布已由系統建立」是否含看板本體 item）：接手 **人工**，需修 spec 文字。定案為選項 B 時要回頭改 `CanvasSteps.givenCanvasEstablished` 與多個 Scenario 的 z 期望值。
- `OQ-T-09-be-canvas-layout-03`：已由 OQ-04 取代，人工接手時以 OQ-04 為準，本則僅作歷史紀錄。

## 2026-09-22 Review 第 4 輪：附保留核准

本輪自行重跑建置與全部測試，並重新核對 spec 對應、core 純度、任務邊界與四則 OQ，判定為**附保留核准**。

### 1. 建置與測試（自行執行，未採信交接摘要）

- `./gradlew clean build --no-daemon` → `BUILD SUCCESSFUL in 3m 10s`，14 actionable tasks: 14 executed。
- 彙總 `build/test-results` 全部 43 份 `TEST-*.xml`：`tests=246 skipped=0 failures=0 errors=0`。
- `git status --porcelain` 為空（工作區乾淨）。

### 2. spec 對應

- `spec-canvas-layout.md` 的 11 個 `uc-`（`uc-init-canvas`／`uc-place-item`／`uc-remove-item`／`uc-move-item`／`uc-resize-item`／`uc-set-item-capabilities`／`uc-set-item-anchor`／`uc-reorder-item`／`uc-move-items`／`uc-remove-items`／`uc-set-viewport`）在五個 feature 檔的 `@uc-` tag 全部出現，無遺漏。
- 以腳本逐字比對：spec 43 個 Scenario 標題與 feature 檔 43 個完全一致，spec 中未出現在 feature 的為 0 筆。
- fail 訊息逐字比對：spec 出現的 10 句訊息（『不可同時移動畫布元素與畫面固定元素』『寬與高必須大於 0』『所選元素中有不可移動的元素』『所選元素中有不可移除的元素』『此元素不可移動』『此元素不可移除』『此元素不可調整大小』『畫布元素不存在』『看板不存在』『縮放比例超出範圍』）在 `Item.java`／`Canvas.java`／`CanvasApplicationService.java` 都有對應且字面相同。
- 抽查 `@fail-pN`「且資料不變」（D-01 的修正）：`CanvasSteps.thenRejected`（第 379～386 行）除斷言 status >= 400 與 message 外，比對 `preActionSnapshot` 與重新查詢的 `snapshotState()`；`snapshotState()` 對全部 `canvas_items`（id／canvasId／component／anchor／x／y／width／height／z／movable／resizable／removable）與全部 `viewports` 逐欄拍照。12 個 `@When` step 全數在送出請求前呼叫 `captureSnapshot()`（12 個呼叫點，逐一確認）。
- 抽查 D-05 的修正（本輪唯一程式碼變更，commit `ec50163`）：`CanvasApplicationService.moveItems`／`removeItems` 在 `ensureCanEdit` 之後、載入 item 之前加上 `itemIds == null || itemIds.isEmpty()` 的 no-op 分支；`CanvasController` 的兩個批次端點只是把 `request.itemIds()` 原樣傳下去，沒有在 controller 內解參考，因此 `IndexOutOfBoundsException`／`NullPointerException`／HTTP 500 的路徑已消失。新增 `CanvasApplicationServiceTest` 4 個案例（move／remove × 空陣列／null），符合 D-05 第 3 點「kanban-spring 層即可」；權限檢查仍在 no-op 之前，未被跳過。決策理由已寫入 `decision-log.md`（D-05 第 2 點）。

### 3. `kanban-core` 純度

- `grep -rn 'org.springframework\|jakarta.persistence\|@Entity\|@Autowired' kanban-core/src/main/java/io/progden/kanban/core/domain/` → 零命中。
- 本任務未新增跨 aggregate 讀取投影，`io.progden.kanban.query.*` 無異動。

### 4. 任務邊界

- `git diff loop/implementation...HEAD --stat`：48 檔、+3511/-2。程式碼只落在 `kanban-core/.../core/domain/`（Canvas／Item／Viewport 及其 Repository port、`ItemAnchor`、`ErrorCode` 追加 canvas 錯誤碼）與 `kanban-spring/.../application|persistence|web` 的 canvas 相關檔，測試落在 canvas 對應的單元測試、`CanvasSteps.java` 與五個 `canvas-*.feature`。
- `.state/` 只動 `.state/tasks/T-09-be-canvas-layout/**`，沒有 `.state/tasks.md`、`.state/archive/**`、其他任務目錄的異動；未觸碰 `.dev/conventions/**`、`scripts/**`、spec／ui 文件本體。

### 5. OQ 核對

- 四則 OQ 都實際存在於 `loopctl show`，交接摘要「待確認事項」沒有只寫在紀錄裡就消失的項目。
- 等級／阻塞判定：`spec-canvas-layout.md` 第 14 行為『狀態：草稿』，四則都不需要違反任何已定稿原文即可把任務做完 → 「高／不阻塞」正確，不需改成阻塞。
- 引文核對：OQ-02／OQ-04 的引文（欄位表 `item.z` 限制欄『必填、同一 `canvas` 內唯一』、`uc-place-item` post、「層序」段、`uc-init-canvas` post 第 2 條、Background『Given 畫布已由系統建立』、『Scenario: 放置元件到空畫布』／『Given 畫布中沒有任何元素』）與 spec 現行文字逐字相符；OQ-04 已按 D-04 註明取代 OQ-03，OQ-03 留作歷史紀錄。
- 接手欄：OQ-02／03／04 為 `人工`（spec 草稿文字要定案，沒有任務會處理），OQ-01 為 `無`，均與實情相符。

### 6. 前端項目

不適用（本任務為後端）。

### 判定：附保留核准（`done`）

保留事項（皆不阻塞，逐條列出接手者）：

1. `OQ-T-09-be-canvas-layout-01`（HTTP 狀態碼分配 409/404/400）——接手：**人工**（spec 未定義狀態碼）；若前端 `T-13-fe-canvas-shell` 實作時對狀態碼有依賴，由該任務在動工前確認並回頭提 OQ，不自行改後端。
2. `OQ-T-09-be-canvas-layout-02`（`item.z` 唯一鍵為 canvas 內唯一或錨定方式內唯一）——接手：**人工**，須修正 `spec-canvas-layout.md`（草稿，不需 CR）使欄位表與 `uc-place-item` post／「層序」段一致；若最後選 B（canvas 內唯一），需追加 `T-09-be-canvas-layout-r2` 修訂實例改唯一鍵。
3. `OQ-T-09-be-canvas-layout-04`（Background『畫布已由系統建立』是否含 z=1 看板本體元素）——接手：**人工**；若選 B，Scenario 的 z 期望值與測試前置都要改，同樣需要修訂實例。
4. `OQ-T-09-be-canvas-layout-03`——已由 OQ-04 取代，人工接手時只看 OQ-04。
