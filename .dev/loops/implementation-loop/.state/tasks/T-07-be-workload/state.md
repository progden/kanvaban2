# T-07-be-workload state

> 2026-09-22 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪退回，狀態回到 `doing`，等下一輪 Dev 處理 D-01、D-02。

驗證結果：`./gradlew clean build --no-daemon` BUILD SUCCESSFUL（14 tasks executed），
workload.feature 6 Scenario／WorkloadCalculatorTest 4 測試／card-assignment.feature 7 Scenario
全數通過，30 份 test-results 皆 0 failures。`kanban-core` 未被觸及且無 Spring／JPA import；
跨 aggregate 投影正確放在 `io.progden.kanban.query.workload`。任務邊界乾淨，`.state/` 只動自己的目錄。
`workload.feature` 與 spec 的 gherkin 區塊逐字相符。本任務沒有任何 OQ，也無阻塞事由。

待處理：
- D-01：`MemberWorkloadEntry` 缺 `displayName`，與 `ui-workload.md`「資料」表指定的 `user.display-name`
  不符，下游 T-19-fe-workload 會拿不到正確成員名稱；需補欄位並加對應驗證。
- D-02：`WorkloadQueryService` 未使用的 `CardJpaEntity` import、`WorkloadSteps` 過時的 class javadoc。

已接受不退回的項目：`CardAssignmentSteps.setAssignees` 的 +5 行跨檔追加（共用 Gherkin 步驟的唯一合法做法，
既有 Scenario 全數仍通過），理由已記在審查紀錄第 4 點。
