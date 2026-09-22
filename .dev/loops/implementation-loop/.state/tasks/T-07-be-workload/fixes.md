# T-07-be-workload 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | done | — | Workload API 回應缺少 `user.display-name`，下游 `s-workload-dashboard` 無法依 ui 規格顯示成員名稱。 問題：`WorkloadResponse.java` 的 `MemberWorkloadEntry(UUID userId, String username, int cardCount)` 只帶 `username`，`WorkloadController.toEntry()` 填的是 `user.getUsername()`。但 `.dev/F05-workload/ui-workload.md` 「資料」表第一列寫的是『｜ 成員名稱 ｜ `user.display-name`（F02，跨模組） ｜ 顯示 ｜ — ｜ 依 `uc-view-workload` post p1，依 `board-membership` 成員分組 ｜』，而 `.dev/F02-user-membership/spec-user-membership.md` 欄位表 定義『｜ user.display-name ｜ string ｜ 未指定時預設等於 `user.username` ｜ 顯示名字，看板上顯示用，可以與其他 帳號重複 ｜』——兩者是不同欄位，只有在未指定顯示名字時才碰巧相等。 CR-007 已經為同一類問題留下先例：F02 變更紀錄寫『新增 Scenario「登入後 TopBar 顯示的是顯示名字而不是帳號 ID」』。本專案既有的其他回應一律同時帶兩個欄位：`MemberResponse(String username, String displayName, BoardRole role)`、 `AssigneeCandidateResponse(UUID id, String username, String displayName)`、`UserResponse`，只有 `MemberWorkloadEntry` 例外。 怎樣才算修好： 1. `MemberWorkloadEntry` 比照 `AssigneeCandidateResponse` 補上 `displayName`，由 `user.getDisplayName()` 填入 （`username` 可保留，不必移除）。 2. 在 `WorkloadSteps` 或 `WorkloadCalculatorTest` 之外補一個涵蓋「顯示名字與帳號 ID 不同」的驗證，確認回應 帶回的是顯示名字（例如在既有 Cucumber Scenario 的前置資料中讓某成員的 display-name 與 username 不同， 再斷言回應欄位）。注意 Gherkin 步驟必須維持與 spec 逐字相同，新增驗證請放在 step definition／單元測試層， 不可以在 `workload.feature` 自行增修步驟。 |
| D-02 | 1 | todo | — | 清理 T-07 產出中的兩處殘留（不影響行為，但會誤導後續維護者）。 1. `kanban-spring/src/main/java/io/progden/kanban/query/workload/WorkloadQueryService.java:11` `import io.progden.kanban.spring.persistence.CardJpaEntity;` 未被使用（該處改用 `var`），請移除。 2. `kanban-spring/src/test/java/io/progden/kanban/spring/cucumber/WorkloadSteps.java` 的 class javadoc 寫 「登入與開板、Stage 角色設定沿用 BoardSteps／FeatureCrBoardSteps 已註冊的共用步驟（…）這裡不重複定義」， 但同檔 `givenSingleStageRole` 實際上定義了 `@Given("Stage {string} 已設定角色為 {word}")`。請修正註解敘述， 讓它與實際註冊的步驟一致。 怎樣才算修好：上述兩點修正後 `./gradlew build --no-daemon` 仍為 BUILD SUCCESSFUL。 |
