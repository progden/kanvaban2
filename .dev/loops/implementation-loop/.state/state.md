# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-01-be-user：Review 第 2 輪**附保留核准**，狀態 `done`，等驅動腳本把 `impl/T-01-be-user`（worktree `../kanban2-impl-T-01-be-user`）用 `--no-ff` 合併回 `loop/implementation`。
- Review 自己重跑 `./gradlew clean build --no-daemon`：exit 0，20 個測試全部通過（`UserTest` 8、Cucumber 6＋4、純度 1、smoke 1）。
- 保留：OQ-IMPL-09（HTTP 狀態碼未定案）、OQ-IMPL-10（`user.username` 非空未落實，F02 spec 已定稿，選 A 要先走 CR）都還是「待處理」，定案後可能要回頭修 `user` aggregate。
- 合併後 T-02-be-board、T-04（還要等 T-02）、T-10-fe-shell 的 T-01 依賴解除。T-02 要把 `uc-logout`「無法存取 board」的 step 改成真的去打 board 端點（見 `review.md`）。
