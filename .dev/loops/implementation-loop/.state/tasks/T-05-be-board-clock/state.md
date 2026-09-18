# T-05-be-board-clock state

> 2026-09-19 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

實作 Board Clock（CR-004）完成：kanban-core 新增 BoardClock／ClockStatus／BoardClockSnapshot，
Board／Card 既有寫入方法改用呼叫端傳入的時間；kanban-spring 新增 3 個看板時鐘端點、
CardApplicationService 改用 Board.newEventTime 取時間並 guard。

`board-clock.feature` 8 個 Scenario（uc-adjust-board-clock／uc-guard-clock-monotonicity／
uc-pause-resume-board-clock）全過，既有 F01/F02 測試維持全過（`./gradlew clean build` 全綠）。

Review 請先看：
1. OQ-T-05-be-board-clock-01（Owner 權限用 board.createdBy 代理，待 T-04 補正式查詢）是否接受。
2. Board.adjustClock／pauseClock／resumeClock 用 recordClockActivity（不 guard）vs 一般寫入用
   recordActivity（guard）這個分流是否合理（見 decision-log 決策 1）。
3. BoardClockSteps 的測試日期基準重設（決策 3）是否算合理的測試基礎建設，而非繞過待測行為本身。
