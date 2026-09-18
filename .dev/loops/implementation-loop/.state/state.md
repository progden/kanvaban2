# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-01-be-user：Review 第 1 輪**退回**，狀態 `doing`，worktree `../kanban2-impl-T-01-be-user`、分支 `impl/T-01-be-user`（審查時 HEAD `4d8afbf`，merge-base `9469e5f`）。
- Review 重跑 `./gradlew clean build --no-daemon`：BUILD SUCCESSFUL，20 個測試（UserTest 8、Cucumber 6+4、NoSpringDependencyTest 1、Smoke 1）全過；feature 檔和 spec 逐字一致；`kanban-core` 沒有 Spring／JPA；diff 範圍乾淨。
- 退回原因是 OQ 登記有遺漏，不是程式碼錯誤：
  - D-01：失敗情境 HTTP 狀態碼（400／409／401／204）是自己決定的，屬於 iteration-prompt 第 5 節點名的高風險，要補 OQ 和交接「待確認事項」。
  - D-02：欄位表『`user.username` 非空』沒落實（空字串可以建立），`uc-create-user` pre 沒有這條，也沒有 fail 訊息，要補 OQ，不可以自己編訊息。
- 細節見 `tasks.md` 底部「修正任務」、`review.md` 2026-09-18 T-01 退回紀錄（含三則不擋判定的觀察：logout／board 驗證留給 T-02、username 併發建立會變 500、明碼密碼的依據是 design 文件而不是 spec）。
- 下一步：Dev 在同一個 worktree 處理 D-01／D-02，再轉 `review-pending`；OQ 若仍未決，Review 只能附保留核准。
