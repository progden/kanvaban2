# T-21-cr007-topbar-display-name state

> 2026-09-19 Review 第 2 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

# T-21-cr007-topbar-display-name state（Review 第 2 輪）

判定：核准（done）。

- 本輪重跑 `./gradlew clean build --no-daemon` 成功（後端 28 個測試，user-login-logout 5/5）；前端 vitest 16/16，tsc、oxlint 都沒有問題。
- D-01 已修好：`UserSteps.thenTopBarShowsName` 用同一個 session 斷言 `GET /api/session` 的 `displayName`，涵蓋 user1 和 user5／王小明。
- 邊界乾淨，沒有 OQ。
- 後續由人工處理：把 CR-007 狀態改成「處理完成」，並拿掉 spec 的 `@added @wip`（tasks.md 備註）。
