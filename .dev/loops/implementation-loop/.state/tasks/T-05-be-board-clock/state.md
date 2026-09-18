# T-05-be-board-clock state

> 2026-09-19 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪：退回（doing）。
- 建置與測試綠燈（core 47、spring 46，0 failure），核心純度與任務邊界通過。
- D-01：CardApplicationService 在 card 驗證前就把 board 的 lastEventAt 推進並存檔，且沒有 @Transactional；寫入失敗（例如空白留言）會留下不存在的「最後一筆事件」基準。要改成原子操作並補測試。
- D-02：board-clock.feature「調整看板時間應記錄一筆活動紀錄」的 Then 沒有斷言指定時間與操作人；暫停／恢復的 Then 也要補操作人斷言。
- OQ-T-05-be-board-clock-02（高、不阻塞、接手：人工）取代 01 的接手欄：T-04 的範圍與依賴都不含看板時鐘的 Owner 檢查。
- 下一輪 Dev：修 D-01、D-02 後交回 review-pending。
