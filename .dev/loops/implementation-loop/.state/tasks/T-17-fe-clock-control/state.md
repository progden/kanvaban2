# T-17-fe-clock-control state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現況：`s-board-clock-control` 已實作為 F07 canvas item（`board-clock-control`），前端 build／lint／測試全過，後端未改動（T-05 端點已齊）。

這輪做了：新增 `BoardClockControl.tsx`＋測試；`itemComponentRegistry.ItemContentProps` 補 `boardId`；`boardApi.ts` 補 adjustClock/pauseClock/resumeClock；`BoardCanvasPage.tsx` 完成註冊。

Review 要先看：
1. `OQ-T-17-fe-clock-control-01`（canvas item 命名／註冊位置慣例，推論所得，非阻塞）與對應 `ADR-T-17-fe-clock-control-01`，是否認可這個先例。
2. `BoardClockControl.tsx` 的角色判斷（讀 `board-membership` role === 'OWNER'）是否符合預期，以及「關閉」操作未實作的理由（決策紀錄有說明）。
3. `ItemContentProps` 新增 `boardId` 是否會影響其他平行分支（T-14/15/16）合併。
