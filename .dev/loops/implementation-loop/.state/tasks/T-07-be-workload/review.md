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

## 2026-09-22 Review 第 2 輪：核准

判定：核准（`done`）。建置與完整測試自行重跑全綠，D-01／D-02 都已依指示修好，任務邊界乾淨，沒有任何 OQ（阻塞或不阻塞皆無），因此是不帶保留的核准。

### 1. 自己跑一次建置與完整測試（不採信 Dev 的 Check 欄）

- `./gradlew clean build --no-daemon`：`BUILD SUCCESSFUL in 3m 25s`，`14 actionable tasks: 14 executed`（用 clean 確保測試真的重跑，不是 UP-TO-DATE）。
- 掃描 `kanban-spring/build/test-results/test/*.xml` 共 30 份，逐份檢查 `failures`／`errors`：全部為 `failures="0" errors="0"`（篩選非 0 的指令無輸出）。
- 重點三份：`TEST-feature_classpath_features-workload.feature.xml` 為 `tests="6" skipped="0" failures="0" errors="0"`；
  `TEST-feature_classpath_features-card-assignment.feature.xml` 為 `tests="7" skipped="0" failures="0" errors="0"`（被 `CardAssignmentSteps` 共用改動波及，未回歸）；
  `TEST-io.progden.kanban.query.workload.WorkloadCalculatorTest.xml` 為 `tests="4" skipped="0" failures="0" errors="0"`。
- `git status --porcelain`：無輸出，工作區乾淨。

### 2. spec 對應

- 以 script 取出 `spec-workload.md` 的 ```gherkin 區塊，與 `kanban-spring/src/test/resources/features/workload.feature`（去掉檔頭註解行）字串比對：**逐字相符**，6 個 Scenario 全在，本輪沒有偷改步驟文字。
- `uc-view-workload` 四個 post 對應 `WorkloadCalculator.calculate()`：`memberUserIds` 預填 0（p1 依 `board-membership` 分組）、對 `card.assigneeIds()` 逐一 `merge(...,1,Integer::sum)`（p2 每位負責人各算一次）、`assigneeIds().isEmpty()` 累加 `unassignedCount`（p3）、`roleByStageId.get(stageId) == StageRole.DONE` 時 `continue`（p4）。`WorkloadQueryService` 用 `findByBoardIdAndDeletedFalse` 對應 Active Card 定義中的「未刪除」。
- `uc-drag-assign-card-owner` 兩個 Scenario 仍打真實寫入端點 `POST /api/cards/{cardId}/assignees/drag`（`CardAssignmentSteps`），post p2「維持不變」由從 DB 讀回 `assigneeIds` 的斷言把關。
- `@fail-pN` 抽查：本檔兩個 usecase 的 `fail` 皆為 `{}`，spec 內沒有任何 `@fail-` Scenario，無可抽查對象（非缺漏），`ui-workload.md` 操作表「失敗時」三列也都寫「不適用（…無 fail 定義）」，兩邊一致。

### 3. D-01／D-02 逐項複驗（本輪重點）

- D-01：`WorkloadResponse.java` 的 `MemberWorkloadEntry(UUID userId, String username, String displayName, int cardCount)` 已補 `displayName`，`username` 保留；`WorkloadController.toEntry()` 填的是 `user.getDisplayName()`，與 `AssigneeCandidateResponse` 一致，對得上 `ui-workload.md`「資料」表『成員名稱 ｜ `user.display-name`（F02，跨模組）』。
- D-01 的驗證強度：`WorkloadSteps.ensureUserExists()` 建立使用者時帶入 `username + "－顯示名稱"`（與 username 不同），`thenWorkloadIs` 多一行 `assertEquals(displayNameFor(username), memberFieldOf(username, "displayName"))`。確認這個斷言**不是空轉**：同一個 Scenario 另有 `assertEquals(3, workloadOf(username))`，該斷言成立代表該成員 entry 確實存在且 `displayName` 非 null，所以若 `displayNameFor` 取不到值會比對失敗而不是雙 null 通過。`workload.feature` 未被增修（見第 2 點逐字比對）。
- D-02：`WorkloadQueryService.java` 的 import 清單已無 `CardJpaEntity`（只留 `CardJpaRepository`）；`WorkloadSteps` class javadoc 改成「登入與開板沿用 `BoardSteps`…；『Stage X 已設定角色為 Y』在本檔另外定義（`givenSingleStageRole`，轉呼叫 `BoardSteps.whenSetStageRole`）」，與同檔實際 `@Given` 註冊一致，`FeatureCrBoardSteps` 的失效 `{@link}` 也移除。

### 4. `kanban-core` 純度

- `grep -rn "org.springframework\|jakarta.persistence\|@Entity\|Autowired" kanban-core/src/main/java/`：無輸出。
- `git diff loop/implementation...HEAD --stat` 顯示本任務完全沒有動到 `kanban-core/**`。
- 跨 aggregate 讀取投影在 `io.progden.kanban.query.workload`（`WorkloadCalculator` 純函式、`WorkloadQueryService` 讀 `board`／`board-membership`／`card`），與既有 `query/*` 投影放法一致，沒有散落到各 aggregate。

### 5. 任務邊界

`git diff loop/implementation...HEAD --stat`：17 檔、+827/-1。程式碼集中在 `kanban-spring/src/main/java/io/progden/kanban/query/workload/**`、`web/Workload*.java`、`src/test/.../query/workload/**`、`cucumber/WorkloadSteps.java`、`features/workload.feature`，外加 `CardAssignmentSteps.java` +5 行（第 1 輪已判定可接受：Gherkin 步驟文字與 spec 逐字相同、Cucumber 不允許重複註冊，純追加分支且 `card-assignment.feature` 7 個 Scenario 本輪重跑仍全過）。`.state/` 只動 `.state/tasks/T-07-be-workload/`（`decision-log.md`／`fixes.md`／`review.md`／`rounds.log`／`state.md`／`status`），沒有碰 `.state/tasks.md`、`.state/archive/**`、別的任務目錄，也沒有新增 `.state/adr/`。未觸及 `.dev/conventions/**`、`scripts/**`、spec／ui 文件本體。

### 6. 交接摘要的待確認事項／OQ

Dev 第 2 輪交接摘要寫『無新增 OQ；D-01、D-02 皆依 Review 指示可直接修正，未涉及任何規格定案文字的覆蓋或衝突』，`loopctl show` 也只列出 D-01 done、D-02 done，沒有任何 OQ，兩者一致，沒有「只寫在交接摘要裡卻沒開 OQ」的情形。本輪四項改動（DTO 加欄位、controller 取 displayName、測試前置資料與斷言、註解與 import 清理）都在 spec／ui 原文範圍內，做完不需違反任何定稿原文，依檢驗句不構成「高」或「覆蓋」等級，因此不開 OQ。

### 7. 前端設計稿

不適用：本任務為後端唯讀 projection 與 API，沒有 `kanban-frontend/**` 產出；`s-workload-dashboard` 的畫面實作屬 `T-19-fe-workload`。

### 觀察（不構成保留事項，無須接手）

- `WorkloadCalculator` 用 `countByMember.merge(...)`，若某 `card` 的負責人已不在 `board-membership` 清單中會多出一個非成員分組；但 `spec-user-membership.md` 的 `uc-remove-member` post 規定移除成員時會一併把該成員從 `card` 的負責人欄位移除，正常流程不可達，spec 也未定義該情境（同第 1 輪紀錄，維持不列為修正項）。
- `WorkloadController.withOperator` 取到的 `operatorId` 在 `viewWorkload` 的 lambda 中未使用，只用來做登入檢查；與既有 controller 的登入慣例一致，不影響行為。
