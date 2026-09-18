# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-02-be-board：Dev 第 1 輪交出，狀態 `review-pending`。
- 範圍：`kanban-core` 新增 `Board`（含 `Swimlane`／`Stage`／`ActivityRecord`／`StageRole`／`CardLookupPort`／`CardSummary`）＋ `BoardRepository`；`kanban-spring` 新增對應 persistence／application／web，端點在 `BoardController`（`/api/boards/**`）。
- 測試：`./gradlew clean build` 全綠。`kanban-core` 27 測試（新增 `BoardTest` 16 個）；`kanban-spring` 新增 `swimlane-management.feature`（7 Scenario）、`stage-management.feature`（8 Scenario）全過。
- 待確認事項（已開 OQ，不阻塞本輪交出）：
  - OQ-IMPL-12：建立看板的預設 Swimlane「預設泳道」／Stage「待辦、進行中、完成」是推論值（依 Background/Given 文字），非 usecase post 逐字定案。
  - OQ-IMPL-13：`r-board-owner` 權限檢查未實作（依賴 T-04 的 `BoardMembership`），目前任何已登入使用者皆可呼叫 Swimlane/Stage 結構調整端點。
  - OQ-IMPL-14：`uc-create-board`（F02）只實作 `board` 部分，不含 `board-membership`（Owner 自動加入），該 Feature 的正式驗收留給 T-04。
- 已知延後：Card Aggregate（T-03）未實作，正式環境 `CardLookupPort` 用 `NoOpCardLookupPort`（一律回傳空清單），Cucumber 測試改用 `FakeCardLookupPort` 測試替身模擬卡片資料；Swimlane/Stage 的 `SWIMLANE_HAS_CARDS`／`STAGE_HAS_CARDS` 保護機制對真實卡片要等 T-03 換上真正的 `CardLookupPort` 實作才會生效。Board Clock（T-05）未接上，活動紀錄時間暫用 `Instant.now()`。
- 唯一觸碰的既有 T-01 檔案：`UserController.java`（switch 補 default 分支，行為不變）、`UserSteps.java`（新增 package-private `setLastResult` 方法供 `BoardSteps` 重用「系統應該顯示錯誤訊息」共用步驟，純新增）。
- 下一步：Review 需自行跑 `./gradlew clean build` 驗證，核對 3 則 OQ 是否需要附保留核准；核准後 T-03／T-04 的依賴解除。
