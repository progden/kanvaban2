# T-07-be-workload state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：spec-workload.md 四個 uc-view-workload Scenario 與兩個 uc-drag-assign-card-owner Scenario
（沿用既有拖曳端點）皆已實作並通過測試，本輪首次交付，無 D-xx。

這輪做了什麼：新增 kanban-spring 的 io.progden.kanban.query.workload（WorkloadCalculator／
WorkloadQueryService）、WorkloadController（GET /api/boards/{boardId}/workload）、6 個 Cucumber
Scenario（workload.feature + WorkloadSteps）、WorkloadCalculator 4 個單元測試；順帶修正
CardAssignmentSteps.setAssignees 讓卡片不存在時就地建立，支援 workload.feature 共用的拖曳步驟文字。

Review 要先看什麼：
1. WorkloadCalculator 的 Active Card／未指派／多負責人各算一次三條規則是否正確對應 post p1～p4。
2. WorkloadController 回應是否包含全部 board-membership 成員（含 0 工作量者），此為 decision-log 記錄
   的低風險判斷，Review 若認為應排除 0 工作量成員可提 D-xx。
3. CardAssignmentSteps.setAssignees 的改動是否影響 card-assignment.feature 既有行為（已跑過
   ./gradlew :kanban-spring:test 全數通過，含該檔）。
