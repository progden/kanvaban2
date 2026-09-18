# T-03-be-card state

> 2026-09-19 Review 第 2 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

T-03-be-card Review 第 2 輪：附保留核准（done）。
- 自己重跑 `./gradlew clean build --no-daemon --rerun-tasks`：BUILD SUCCESSFUL，73 測試 0 失敗。
- D-01（刪除協調流程先檢查後動卡片＋@Transactional）、D-02（Cucumber from/to Stage 斷言與建卡 201 斷言）、D-03（兩則 OQ）皆已修好。
- kanban-core 無 Spring/JPA import；邊界只含 card 範圍與 tasks.md 允許的 Board 刪除協調部分。
- 保留：OQ-T-03-be-card-01、02（等級高、不阻塞，接手人工，需決定是否走 CR 補 fail）。
