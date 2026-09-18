# T-06-be-kanban-widgets state

> 2026-09-19 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：F03 六個 `uc-view-*`（Cycle/Lead Time、WIP、Aging WIP、Throughput、CFD、截止日期
提醒）已在 `kanban-spring` 實作完成，掛 `GET /api/boards/{boardId}/widgets/*`；`kanban-core`
只新增一個共用 `ErrorCode` 列舉值，未動任何 aggregate 的 domain 程式碼。

這輪做了什麼：
- 新增 `query.timeline`／`wip`／`throughput`／`duedate`／`widgets` 五個套件（純函式計算器＋
  `@Service` 查詢層），比照既有 `query.featurecrboard` 慣例；新增
  `KanbanWidgetsController`／`KanbanWidgetsResponses`。
- 16 個 JUnit5 單元測試＋4 份 `.feature` 檔（逐字複製 spec Gherkin）＋`KanbanWidgetsSteps`。
- 開 `OQ-T-06-be-kanban-widgets-01`（不阻塞）＋`ADR-T-06-be-kanban-widgets-01`：發現
  `BoardClockSteps`（T-05）與 `FeatureCrBoardSteps`（T-08）合併後步驟定義重複，導致
  `RunCucumberTest` 整體失敗（58/85），已用暫時本地修改（未提交）驗證修掉衝突後全綠。

Review 先看：1) OQ／ADR 內容，可暫時比照我的做法解除衝突重跑驗證；2) decision-log 本輪條目
（WIP 是否排除 Done、天數採日曆日差、百分位數 nearest-rank、`thresholdDays` 選填、`ErrorCode`
新增範圍判斷）；3) `query/**`＋`web/KanbanWidgets*.java`＋對應測試。
