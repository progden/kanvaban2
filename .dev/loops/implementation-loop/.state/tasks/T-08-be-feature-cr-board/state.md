# T-08-be-feature-cr-board state

> 2026-09-19 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪：退回（doing）。
- 自己跑 `./gradlew clean build --no-daemon`：BUILD SUCCESSFUL，84 個測試 0 失敗。
- 行為與測試的缺口：D-01（orphan 清單重複列出同一張 CR）、D-02（p4 警告文字跟行為不一致，「不影響其他 card」沒有測試）、D-03（@ComponentScan 改用 scanBasePackages）。
- OQ-02（高／不阻塞／人工）取代 OQ-01：ui「改列入 orphan」跟 spec Scenario「檢視 CR 影響哪個 Feature」衝突；後端先照 spec 做。
- 邊界乾淨，kanban-core 沒有 Spring／JPA 依賴。
