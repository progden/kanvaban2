# T-03-be-card 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Review 第 1 輪：退回

### 第 1 輪（Review）驗證結果

1. **建置與測試（自己重跑）**：`./gradlew clean build --no-daemon --rerun-tasks` → BUILD SUCCESSFUL（2m24s）。彙總 test-results XML：71 個測試，0 失敗、0 錯誤、0 略過（kanban-core：BoardTest 17、CardTest 8、UserTest 10、NoSpringDependencyTest 1；kanban-spring：card-editing 8、swimlane-management 7、stage-management 8、create-user-account 7、user-login-logout 4、SmokeTest 1）。
2. **spec 對應**：`card-editing.feature` 與 spec「Feature: Card（卡片）編輯」Gherkin 用 diff 比對，除檔頭兩行註解外逐字相同；6 個 uc、8 個 Scenario 都有 step definition 且打正式端點、從 JPA 驗證。抽查 `@uc-add-card @fail-p1`：`UserSteps.thenErrorMessage` 比對回應 message＝"卡片標題不可為空"，`thenNoNewCardCreated` 比對未刪除卡片數不變，成立；`@uc-delete-card @fail-p2`：取消後沒有呼叫 DELETE，`deleted=false`，成立。問題：`uc-move-card-stage` 的「異動前後的 Stage」只驗了後（D-02）。
3. **kanban-core 純度**：`grep -rnE "import (org\.springframework|jakarta\.persistence|lombok)" kanban-core/src/main` 無結果；NoSpringDependencyTest 通過。本任務沒有跨 aggregate 投影。
4. **任務邊界**：`git diff loop/implementation...HEAD --name-status` 程式碼改動在 kanban-core／kanban-spring 的 card 相關檔，外加 tasks.md 明文允許的 BoardController／BoardApplicationService／BoardSteps（OQ-IMPL-17 範圍）與刪除 NoOpCardLookupPort／FakeCardLookupPort；`.state/` 只有本任務目錄與兩則新增 ADR。邊界乾淨。
5. **OQ**：Dev 沒有開任何 OQ（`loopctl show` 無 OQ）；交接摘要第 4、5 點其實是 spec 沒定義 fail 的高風險事項（D-03）。
6. 非前端任務，不適用。

**主要問題（D-01）**：專案內沒有任何 `@Transactional`，`removeSwimlane(confirmed=true)`／`removeStage(destinationStageId)` 先逐張改卡片並各自提交，之後才做 board 的可刪除檢查。只剩 1 個 Swimlane 時確認刪除，卡片會被刪掉但 Swimlane 被 `MINIMUM_SWIMLANE` 拒絕而留下；目的 Stage 不屬於該 board 時卡片會被搬到不存在的 Stage。違反 `uc-delete-swimlane` fail p1『拒絕，該 `swimlane` 不被刪除』與 `uc-delete-stage` post。

### 判定：退回（doing）

D-01（協調流程非原子／未驗證目的 Stage）、D-02（Cucumber 斷言不足）、D-03（規格未定義的失敗路徑要開 OQ）交下一輪 Dev 處理。

## 2026-09-19 Review 第 2 輪：附保留核准

### 第 2 輪（Review）驗證結果

1. **建置與測試（自己重跑）**：`./gradlew clean build --no-daemon --rerun-tasks` → BUILD SUCCESSFUL（2m11s，14 tasks executed）。彙總 `**/build/test-results/**/*.xml`：73 個測試，0 失敗、0 錯誤、0 略過（kanban-core：BoardTest 17、CardTest 8、UserTest 10、NoSpringDependencyTest 1；kanban-spring：card-editing 8、swimlane-management 7、stage-management 8、create-user-account 7、user-login-logout 4、BoardApplicationServiceTest 2、SmokeTest 1）。
2. **D-01 核對**（`git show 018a1b3`）：`BoardApplicationService.removeSwimlane`／`removeStage` 已加 `@Transactional`，並在動卡片前先呼叫 `Board.ensureSwimlaneRemovable`／`ensureStageRemovable`（`findSwimlane`／`findStage` 只查本 board 的集合 → 不屬於本 board 丟 NOT_FOUND；數量下限丟 MINIMUM_*），`removeStage` 另呼叫新增的 `ensureValidDestinationStage`（等於來源 → `INVALID_DESTINATION_STAGE` 400；不屬於本 board → `STAGE_NOT_FOUND` 404）。第 1 輪列的三個情境都在動卡片前就被擋下。`BoardApplicationServiceTest` 覆蓋情境 1（只剩 1 個 Swimlane，確認刪除 → MINIMUM_SWIMLANE，卡片 `deleted=false`）與情境 3（目的 Stage 屬於別的 board → STAGE_NOT_FOUND，卡片 `stage` 不變、StageTransition 為空、來源 Stage 仍在），符合修好標準。
3. **D-02 核對**（`git show 1ba5048`）：`CardSteps.whenDragCardToStage` 在移動前記下 `stageIdBeforeMove`，`thenStageTransitionRecorded` 新增 `assertEquals(stageIdBeforeMove, transition.getFromStageId())`；`BoardSteps.createTestCard` 新增 201 斷言並帶出回應內容。符合修好標準。
4. **spec 對應**：本輪沒有改 `card-editing.feature`（本輪兩個程式 commit 都沒動 feature 檔），第 1 輪已逐字比對與 spec 一致；8 個 Scenario 全綠。抽查 `@uc-delete-stage` 協調流程：現在拒絕時卡片確實不變（見第 2 點測試）。
5. **kanban-core 純度**：`grep -rnE "import (org\.springframework|jakarta\.persistence|lombok)" kanban-core/src/main` 無結果；NoSpringDependencyTest 通過。本任務無跨 aggregate 投影。
6. **任務邊界**：`git diff loop/implementation...HEAD --name-status` 除 kanban-core／kanban-spring 外，只有 `.state/adr/` 兩則新增 ADR 與 `.state/tasks/T-03-be-card/**`；`.dev/F0*`、`.dev/conventions`、`scripts`、`CLAUDE.md`、`.state/tasks.md`、`.state/archive/**` 皆無異動。本輪改到的 `Board.java`（新增 `ensureValidDestinationStage`）、`ErrorCode`、`BoardController.statusFor` 屬於 tasks.md 允許的 OQ-IMPL-17 刪除協調範圍。
7. **OQ 核對（D-03）**：`loopctl show` 列出 OQ-T-03-be-card-01、02，交接摘要「待確認事項」指向這兩則。到 `spec-kanban-basic.md`／`ui-kanban-basic.md` 逐字比對：`comment.content | string | 非空 | 留言內容`（spec 第 34 行）、`card.swimlane`／`card.stage` 兩列（第 32–33 行）、`uc-add-comment`（第 482–494 行）、`uc-add-card`（第 423–437 行）usecase 區塊、`uc-move-card-swimlane`／`uc-move-card-stage` 的 `fail: {}`、ui 第 349 行操作表與第 356 行驗收條件，引文皆一致。等級「高」、不阻塞：用「做完是否必須違反定稿原文」檢驗——拒絕空白留言有欄位表「非空」為據、不驗證交會格也沒有違反任何 fail／post 原文，兩者都不需改動定稿文字，判定正確。接手填「人工」：是否補 fail 要走 CR，沒有既有任務的產出範圍涵蓋，合理。小瑕疵：OQ-02 選項 B 寫「T-07 canvas」，但 tasks.md 的 T-07 是 workload（canvas 是 T-13 等前端任務），不影響判斷，不另開 OQ。
8. 非前端任務，第 6 點不適用。

### 判定：附保留核准（done）

建置／測試全綠、D-01～D-03 都修好、spec 對應完整、core 純度與邊界乾淨、沒有阻塞的 OQ。

保留事項（各自的接手者）：
- **OQ-T-03-be-card-01**（`uc-add-comment` 沒有定義空白留言的 fail；程式碼目前以 400 `EMPTY_COMMENT_CONTENT` 拒絕）→ 接手：人工（決定是否開 CR 補 fail 分支；若補，T-14-fe-board-item 的 `s-card-detail` 失敗呈現要跟著調整）。
- **OQ-T-03-be-card-02**（`uc-add-card`／`uc-move-card-swimlane`／`uc-move-card-stage` 不驗證目的 swimlane/stage 是否存在且屬於該 board，目前可建出孤兒卡片）→ 接手：人工（決定是否開 CR 補 fail；若補，要回頭改 `CardApplicationService` 與 Cucumber）。
- 沿用上游：T-01 的 OQ-IMPL-09（HTTP 狀態碼慣例）仍待人工，本任務的錯誤碼→HTTP 對應依 ADR-001，若該 OQ 定案不同需回頭改 `CardController`／`BoardController.statusFor`。
