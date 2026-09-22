# T-14-fe-board-item state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：s-board、s-swimlane-list／delete、s-stage-list／delete、s-card-add-dialog、
s-card-detail、s-card-delete-dialog、s-card-assignee-picker 九個畫面已實作並掛載到
F07 s-canvas 的 item.component === 'board'。新增後端端點
GET /api/boards/{boardId}/cards（重用既有 CardRepository 查詢）供交會格取卡片。

這輪做了什麼：兩個 commit（[dev] 產出程式碼、[test] 測試），開 1 則不阻塞 OQ
（OQ-T-14-fe-board-item-01：看板成員頭像拖曳指派負責人的來源 item 不存在，F07 待補）。

Review 要先看什麼：
1. decision-log 第 2 節「規格沒講清楚、本輪自行決定的地方」5 點技術決定（尤其第 1、2 點
   swimlane/stage 管理入口改用對話框、owner 權限交後端擋，是否可接受）。
2. `kanban-frontend/src/board/BoardItemContent.tsx` 與 `BoardContext.tsx`：確認
   Context 掛法沒有動到 T-13 CanvasStage.tsx 本體邏輯。
3. `kanban-frontend/src/board/BoardItemContent.test.tsx`（24 個測試）＋
   `CardApplicationServiceTest.java` 新增的 2 個測試。
4. `./gradlew build --no-daemon` 與 `kanban-frontend` 的 `tsc -b`／`oxlint`／`vitest run`
   本輪皆已跑過且全綠，Review 仍需自行重跑驗證。
