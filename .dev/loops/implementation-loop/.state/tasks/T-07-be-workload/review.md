# T-07-be-workload 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：退回

判定：退回（`doing`），新增 D-01、D-02。建置與測試全綠，spec 行為正確，退回原因是下游 ui 規格要求的欄位缺漏。

### 1. 自己跑一次建置與完整測試

- `./gradlew build --no-daemon`：第一次執行全部 `UP-TO-DATE`（沿用 Dev 上一輪的快取），不採信，改跑下一項。
- `./gradlew clean build --no-daemon`：`BUILD SUCCESSFUL in 3m 16s`，`14 actionable tasks: 14 executed`，確認測試真的重跑。
- 測試結果檔核對：`kanban-spring/build/test-results/test/TEST-feature_classpath_features-workload.feature.xml`
  為 `tests="6" skipped="0" failures="0" errors="0"`（對應 spec 的 6 個 Scenario）；
  `TEST-io.progden.kanban.query.workload.WorkloadCalculatorTest.xml` 為 `tests="4" ... failures="0"`；
  受本輪改動波及的 `TEST-feature_classpath_features-card-assignment.feature.xml` 為 `tests="7" ... failures="0"`。
  全部 30 份結果檔皆 0 failures／0 errors。
- `git status --porcelain`：無輸出，工作區乾淨。

### 2. spec 對應

- `workload.feature` 與 `spec-workload.md` 的 ```gherkin 區塊逐字比對（以 script 去掉檔頭兩行註解後字串比對）：**逐字相符**，6 個 Scenario 全在。
- `uc-view-workload` 四個 post 都有對應實作與測試：`WorkloadCalculator.calculate()` 以 `memberUserIds`
  預先填 0（p1 分組）、對 `card.assigneeIds()` 逐一 `merge(...,1,Integer::sum)`（p2 每人各算一次）、
  `assigneeIds().isEmpty()` 累加 `unassignedCount`（p3）、`roleByStageId.get(stageId) == StageRole.DONE` 時
  `continue`（p4）。`WorkloadQueryService` 的 `findByBoardIdAndDeletedFalse` 對應「Active Card」定義中的「未刪除」。
- `uc-drag-assign-card-owner` 兩個 Scenario 確實打到真實寫入端點：`CardAssignmentSteps.whenDragAvatarToCard`
  送 `POST /api/cards/{cardId}/assignees/drag` 並斷言 200，Then 由 `thenCardAssigneesContainBoth`／
  `thenCardAssigneesStillOnlyBoth` 從 DB（`loadCard`）讀 `assigneeIds` 驗證，post p2「維持不變」有
  `assertEquals(2, ...)` 把關。沿用 `spec-user-membership.md` 的既有端點符合 spec「Use Case 定義」下方的說明。
- `@fail-pN` 抽查：本檔兩個 usecase 的 `fail` 皆為 `{}`，spec 沒有任何 `@fail-` Scenario，無可抽查對象（非缺漏）。
- 測試強度觀察（不構成退回）：`thenWorkloadIncludesCard` 以 `assertEquals(1, workloadOf(username))` 斷言，
  沒有驗證卡片身分。因為 spec「其他名詞」把 Workload 定義為『某成員名下 Active Card 的數量』、回應本來就沒有
  卡片明細，該 Scenario 又只有一張卡片，這個斷言在該情境下等價；post p2 另有 `WorkloadCalculatorTest
  .cardWithMultipleAssigneesCountsForEachOfThem` 直接涵蓋。接受 Dev 決策紀錄第 1 點。

### 3. `kanban-core` 純度

- `grep -rn "org.springframework\|jakarta.persistence" kanban-core/src/main/java/`：無輸出。
- `git diff loop/implementation...HEAD --stat` 顯示本輪完全沒有動到 `kanban-core/**`。
- 跨 aggregate 讀取投影放在 `io.progden.kanban.query.workload`（`WorkloadCalculator` 為純函式、
  `WorkloadQueryService` 負責讀 `board`／`board-membership`／`card`），與 `query/wip`、`query/throughput`
  等既有投影一致，沒有散落到各 aggregate。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：15 檔、+669/-1。產出集中在
`kanban-spring/src/main/java/io/progden/kanban/query/workload/**`、`web/Workload*.java`、
`src/test/.../workload/**`、`cucumber/WorkloadSteps.java`、`features/workload.feature`。
`.state/` 只動到 `.state/tasks/T-07-be-workload/`（`decision-log.md`／`rounds.log`／`state.md`／`status`），
沒有碰 `.state/tasks.md`、`.state/archive/**` 或別的任務目錄。未觸及 `.dev/conventions/**`、`scripts/**`、
spec／ui 文件本體。

唯一跨出本任務產出範圍的是 `CardAssignmentSteps.setAssignees`（+5 行，卡片第一次被提及時就地建立）。
**判定為可接受、不退回**：`workload.feature` 的 Gherkin 必須逐字複製 spec，而
『Given 卡片 "B" 目前的負責人只有 "志明"』這句與 `card-assignment.feature` 完全相同，Cucumber 不允許重複
註冊同一句步驟，Dev 沒有合法的替代做法；該改動是純追加分支，`card-assignment.feature` 7 個 Scenario 本輪
重跑仍全數通過。已在本紀錄留痕，供之後整合分支合併時辨識。

### 5. 交接摘要的待確認事項／OQ

Dev 交接摘要「待確認事項」寫『無新增 OQ』，`loopctl show` 也顯示本任務沒有任何 OQ，兩者一致，沒有
「只寫在交接摘要裡卻沒開 OQ」的情形。逐項檢查 Dev 的三個低風險決策（只回計數不回明細、含工作量為 0 的成員、
共用 step definition 就地建卡）都能由 spec 原文推導、不需違反任何定稿文字，維持「高風險／覆蓋」以外的分級正確。
本輪也沒有新的阻塞事由，因此不開 OQ。

### 6. 前端設計稿

不適用：本任務為後端唯讀 projection 與 API，沒有 `kanban-frontend/**` 產出。

### 退回原因

**D-01（主因）**：`MemberWorkloadEntry` 只回傳 `username`，但 `ui-workload.md`「資料」表指定成員名稱來源是
`user.display-name`，而 F02 欄位表明確定義這是與 `user.username` 不同的欄位（只在未指定時才相等）。
專案既有的 `MemberResponse`／`AssigneeCandidateResponse`／`UserResponse` 一律同時帶 `displayName`，
只有 Workload 例外；下游 `T-19-fe-workload` 依 ui 規格顯示成員名稱時會拿不到正確的值。詳見 `fixes.md` D-01。

**D-02（附帶清理）**：`WorkloadQueryService` 有未使用的 `CardJpaEntity` import；`WorkloadSteps` 的 class
javadoc 宣稱不重複定義 Stage 角色設定步驟，實際上同檔有定義。兩者不影響行為，趁同一輪一併修掉。

### 觀察（不構成保留事項，無須接手）

`WorkloadCalculator` 用 `countByMember.merge(...)` 累加，若某 `card` 的負責人已不在 `board-membership`
成員清單中，會在結果多出一個非成員的分組，嚴格看與 post p1『依 `board-membership` 成員分組統計』有落差。
但 `spec-user-membership.md` 的 `uc-remove-member` post 寫『若目標成員仍是某些 `card` 的負責人，這些 `card`
的負責人欄位移除該成員；若該 `card` 因此沒有其他負責人，則變成未指派』，正常流程下不會產生非成員負責人，
此分支實務上不可達，spec 也沒有定義該情境，因此不列為修正項，僅記錄供日後參考。
