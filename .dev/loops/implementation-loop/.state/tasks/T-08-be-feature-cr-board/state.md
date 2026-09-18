# T-08-be-feature-cr-board state

> 2026-09-19 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：Feature／CR 追蹤表唯讀投影已實作完成並通過建置。

這輪做了什麼：
- 新增 `io.progden.kanban.query.featurecrboard`（Calculator／QueryService／View DTOs）與
  `FeatureCrBoardController`（`GET /api/boards/{boardId}/feature-cr-board`）。
- `KanbanApplication` 補 `@ComponentScan` 納入 `io.progden.kanban.query`（否則整個 Spring
  context 起不來，見決策紀錄 Check）。
- 新增 5 個 Cucumber Scenario（`feature-cr-board.feature`）＋ 5 個 `FeatureCrBoardCalculatorTest`
  單元測試，`./gradlew build` 全綠（48 測試）。

Review 要先看什麼：
1. `OQ-T-08-be-feature-cr-board-01`：CR 的 affects 目標沒有 Feature 卡時，是否該「同時」出現在
   對應 Feature 分組底下與 orphan 清單（見決策紀錄，這是由 Scenario 給定資料反推出的行為，非
   spec 逐字明講）。
2. `KanbanApplication` 的 `@ComponentScan` 改動是否會影響既有模組（已確認既有 F01 Cucumber 測試
   仍全綠）。
