# T-21-cr007-topbar-display-name state

> 2026-09-19 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現況：CR-007 已實作完成，第 1 輪即完成，無 D-xx、無新 OQ。
這輪做了什麼：`SessionResponse`／`GET /api/session`／登入態補 `displayName`，`AppShell` TopBar 改顯示 `user.display-name`；`user-login-logout.feature` 補上 CR-007 新 Scenario 與對應 step definitions；前端測試改用帳號 ID／顯示名字不同值的情境驗證。
Review 要先看什麼：`UserController`／`SessionResponse` 的 session 屬性新增方式是否合理；`user-login-logout.feature` 的 tag（拿掉 `@wip`、保留 `@added @CR-007`）是否符合慣例；`thenTopBarShowsName` 斷言鍵從 `username` 改 `displayName` 是否影響其他共用該 step 的 Scenario（已確認只有本 feature 用到，既有 Scenario "user1" 仍通過）。
Check：`./gradlew clean build --no-daemon` BUILD SUCCESSFUL；`kanban-frontend` `pnpm test`／`tsc -b`／`oxlint` 全過。
