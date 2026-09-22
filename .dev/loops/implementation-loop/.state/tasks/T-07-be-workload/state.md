# T-07-be-workload state

> 2026-09-22 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪的 D-01、D-02 皆已修好，本輪送出 review-pending。

- D-01：`MemberWorkloadEntry` 補上 `displayName`（`WorkloadController.toEntry()` 填
  `user.getDisplayName()`），`WorkloadSteps` 新增顯示名字與帳號 ID 不同的前置資料與斷言。
- D-02：移除 `WorkloadQueryService` 未使用的 import；修正 `WorkloadSteps` class javadoc 與
  實際註冊步驟不符的敘述。

Review 請先看：`WorkloadResponse.java`／`WorkloadController.java` 的 `displayName` 欄位、
`WorkloadSteps.thenWorkloadIs` 新增的斷言與 `ensureUserExists` 的 displayName 設定。

`./gradlew clean build --no-daemon`：BUILD SUCCESSFUL，3m16s，14 tasks executed；
workload.feature 6 Scenario／card-assignment.feature 7 Scenario／全部 30 份測試結果 0 failures。
無新增 OQ。
