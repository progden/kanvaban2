# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-02-be-board：Dev 第 2 輪處理完 D-05～D-08，狀態改為 `review-pending`，等 Review 第 2 輪審查（見 `tasks.md` 底部、`decision-log.md` 同日「Dev，第 2 輪」條目）。
- 建置：本輪自己重跑 `./gradlew clean build --no-daemon -q`，exit 0；`kanban-core` 28 個測試（`BoardTest` 17、`UserTest` 10、`NoSpringDependencyTest` 1）、`kanban-spring` 4 個 feature 檔（`create-user-account` 7、`user-login-logout` 4、`swimlane-management` 7、`stage-management` 8）全過。
- D-05：已新開 `OQ-IMPL-15`，逐字引用 spec 第 136/141/287/292 行、ui 第 85/183/189/197 行、design 第 90 行，寫清楚 409 是暫時性推論行為、誰該接手協調刪除／轉移卡片、定案前的 API 限制；不阻塞本任務核准。**不要求也未修改**正式程式碼（`Board`／`BoardController`／`BoardApplicationService` 原封不動）。
- D-06：`BoardSteps` 四個 step（`whenConfirmDelete`／`thenSwimlaneAndCardsRemoved`／`whenChooseDestinationStage`／`thenCardsMovedTo`）已加註解標明是替身、指向 OQ-IMPL-15，Scenario 本身未改動。
- D-07：`BoardSteps` 改成記錄動作前活動紀錄筆數＋預期關鍵字，`thenActivityRecorded` 驗筆數加 1／操作人／動作內容；兩個 `@fail-p1` 對應的 Then 補上筆數不變斷言；`BoardTest` 補齊 8 個寫入方法的活動紀錄斷言（新增 1 個測試方法）。
- D-08：`OQ-IMPL-12` 路徑改直接寫對＋補行號；`OQ-IMPL-13` 的 design 引文改逐字（第 115 行）並註明只涵蓋 `setStageRole`（CR-003），非 9 個 uc 共同依據。
- 未決 OQ（不擋核准，Review 應以附保留核准處理）：OQ-IMPL-12（預設 Swimlane／Stage）、OQ-IMPL-13（Owner 權限要等 T-04）、OQ-IMPL-14（Owner membership 要等 T-04）、OQ-IMPL-15（刪除連帶卡片協調，新開）。
- 下一步：Review 第 2 輪重跑建置／測試，核對 D-05～D-08 是否真的處理完，以及 OQ-IMPL-15 引文是否逐字。
