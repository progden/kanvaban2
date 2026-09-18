# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-01-be-user：Dev 交付，狀態改為 `review-pending`，worktree `../kanban2-impl-T-01-be-user`、分支 `impl/T-01-be-user`、HEAD `18eba65`（相對 `loop/implementation` 合併點 `9469e5f`）。
- 範圍：F02 `spec-user-membership.md`「建立使用者帳號」「使用者登入與登出」兩個 Feature（`uc-create-user`／`uc-login`／`uc-logout`），共 10 個 Scenario；不含 `board`／`board-membership`／`card.assignees`（依任務定義排除，留給 T-02／T-04）。
- 產出：`kanban-core`（`User`／`DomainException`／`ErrorCode`／`UserRepository` port）＋`kanban-spring`（`UserApplicationService`／JPA persistence／`UserController`：`POST /api/users`、`/api/login`、`/api/logout`、`GET /api/session`，用 `HttpSession` 保存登入狀態）。
- 測試：`./gradlew clean build --no-daemon` 全綠——`kanban-core` `UserTest` 8 個單元測試＋既有 `NoSpringDependencyTest`；`kanban-spring` Cucumber 10 個 Scenario（`create-user-account.feature` 6、`user-login-logout.feature` 4）＋既有 `KanbanApplicationSmokeTest`，皆 `failures=0 errors=0`。
- `kanban-core` 純度確認：`grep -rn "import org.springframework\|import jakarta" kanban-core/src/main` 無結果。
- 沒有待確認事項／沒有新開 OQ；`design-user-membership.md` 的「實作狀態」段落是跨 T-01/T-02/T-04 的整體設計預告，本輪只實作 `user` 範圍。
- ⚠️ 給 Review：Spring Boot 4.1.1 的 MockMvc 測試支援與預設 Jackson 版本都有非顯而易見的套件搬遷，細節見 `decision-log.md` 最後一則「環境限制發現」，Review 重跑建置前可先讀過避免誤判。
- 下一步：Review 進這個 worktree 核准／退回；核准後合併回 `loop/implementation`，解除 T-02／T-04 的依賴。
