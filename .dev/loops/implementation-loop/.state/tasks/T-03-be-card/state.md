# T-03-be-card state

> 2026-09-19 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：T-03-be-card 第 1 輪 Dev 完成，交 Review。

這輪做了什麼：
- kanban-core 新增 Card/Comment/StageTransition Aggregate（含 CardRepository port），
  8 個 CardTest 單元測試全過。
- kanban-spring 補上 CardApplicationService/CardController/persistence 層，
  CardLookupPort 改用真正的 CardLookupPortAdapter（刪 NoOpCardLookupPort）。
- 依人工解除的 OQ-IMPL-17（選項 A）擴充 BoardApplicationService/BoardController
  兩個刪除端點（confirmed/destinationStageId），協調 uc-delete-swimlane/
  uc-delete-stage post 第 2 條；BoardSteps.java 四個替身步驟改打正式端點驗證。
- 新增 card-editing.feature + CardSteps，8 個 Scenario 全綠；新增
  CrossAggregateState 解決 3 段跨 Feature 共用 Gherkin 步驟文字的衝突。
- 兩則 ADR：軟刪除＋路由設計、共用步驟分流模式（T-04 動 BoardSteps.java 前先讀）。

Review 要先看什麼：decision-log.md 7 條低風險決定（尤其軟刪除）、兩則 ADR。
沒有開 OQ；`./gradlew build --no-daemon` 全綠。
