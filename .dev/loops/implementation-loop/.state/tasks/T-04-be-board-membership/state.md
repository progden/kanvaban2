# T-04-be-board-membership state

> 2026-09-19 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

T-04-be-board-membership Review 第 1 輪：退回（doing）。
- 自己重跑 `./gradlew clean build --no-daemon --rerun-tasks`：綠燈，108 個測試全過；F02 Scenario 和 spec 逐字一致。
- kanban-core 純度、任務邊界都沒問題；OQ-01～03 的引文、等級、阻塞標記都正確。
- 待 Dev 處理：
  - D-01：T-04 新增的卡片負責人、候選名單、依負責人查詢、成員列表端點，以及 `uc-member-add-card` 的 pre，都沒有檢查操作者成員資格。要補上檢查、補測試，並開 OQ。
  - D-02：`uc-view-board-list` 會把 Viewer 的看板列出來，和 post 字面不一致，要開 OQ（兩處矛盾並列）並決定做法。
