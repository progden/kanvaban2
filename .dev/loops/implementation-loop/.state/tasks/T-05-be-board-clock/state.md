# T-05-be-board-clock state

> 2026-09-19 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：D-01、D-02 已修好並跑過完整建置測試（kanban-core 47、kanban-spring 47，0 failure）。

這輪做了什麼：
- D-01：CardApplicationService 五個卡片寫入方法改成 card domain 驗證成功後才存 board，並補 @Transactional。
- D-02：BoardClockSteps 調整／暫停／恢復三個活動紀錄 Then 步驟補上 occurredAt、operatorId 斷言。

Review 要先看什麼：
- CardApplicationService.java 的方法順序（board.save 排在 card 操作成功之後）與 @Transactional。
- 新增的 CardApplicationServiceTest（留言空白被拒絕 → lastEventAt 不推進 → 之後仍能正常建卡）。
- BoardClockSteps.assertActivityRecorded 回傳值與三個呼叫端的斷言。
- OQ-T-05-be-board-clock-02（接手：人工）仍待處理，非本輪範圍。
