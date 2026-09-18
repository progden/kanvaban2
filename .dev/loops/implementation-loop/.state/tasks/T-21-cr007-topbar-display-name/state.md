# T-21-cr007-topbar-display-name state

> 2026-09-19 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

# T-21-cr007-topbar-display-name state（Dev 第 2 輪）

修好 D-01：`UserSteps.thenTopBarShowsName` 現在同時斷言 `GET /api/session`
的 `displayName`，涵蓋 user1（預設等於 username）與 user5／王小明兩個既有
Scenario，未改 Gherkin 文字、未改 kanban-core。

驗證：`./gradlew clean build --no-daemon` 全綠；並以暫時刪掉
`UserController` 裡設定 session displayName 那行、確認新斷言會失敗的方式，
證實這條測試真的守住 CR-007 該項驗收標準，之後已還原。

Review 這輪請先看：`UserSteps.java` 的 diff（只有 6 行）與這輪的 Check 紀錄。
