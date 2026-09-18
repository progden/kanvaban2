# T-06-be-kanban-widgets 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Review 第 1 輪：blocked

### 判定：blocked（整合分支既有的建置紅燈，再跑一輪 Dev 也修不好）

#### 1. 建置與測試（Review 自己跑的）
- worktree 內 `./gradlew clean build --no-daemon` → **BUILD FAILED**，`85 tests completed, 58 failed`，失敗原因全部是 `DuplicateStepDefinitionException`（`BoardClockSteps.givenLoggedInAndBoardOpened` 與 `FeatureCrBoardSteps.givenLoggedInAndBoardOpened`）。
- `git grep` 查到整合分支 `loop/implementation`（d95b95d）與分岔點 f645776 本來就有這兩個重複的 `@Given("我已登入系統，並開啟 Board {string}")`，是 T-05／T-08 合併後留下的，不是 T-06 造成的。
- 對照驗證（worktree 沒改）：`git archive HEAD` 匯出到 /tmp/rv-t06，只把 `FeatureCrBoardSteps.java:49` 的 `@Given` 註解掉，`./gradlew clean build --no-daemon` → BUILD SUCCESSFUL；kanban-spring 85 個測試 0 失敗，kanban-core 5 個測試類別全過（含 `NoSpringDependencyTest`）。
- F03 的 10 個 Scenario 在對照環境全過：cycle-lead-time 3、wip 2、throughput-cfd 2、duedate-reminder 3；query.* 新增的 16 個單元測試全過。

#### 2. spec 對應
- spec 裡 6 個 `uc-view-*`、10 個 Scenario 都有對應的 `.feature`＋`KanbanWidgetsSteps`，Scenario 名稱逐一比對過，都對得上。
- 抽查 `@fail-p1`（門檻天數 0）：`KanbanWidgetsQueryService.viewDueDateReminder` 在 1～365 範圍外丟 `INVALID_DUEDATE_THRESHOLD_DAYS`，controller 回 400，訊息為 "門檻天數必須是 1 到 365 之間的正整數"，跟 Scenario 一致；這是唯讀查詢，「資料不變」自然成立。
- WIP：名詞表寫「不在 Done 角色 Stage」，Scenario 卻要求列出 "完成" 5，Dev 依 Scenario 實作，但沒開 OQ → Review 補開 OQ-T-06-be-kanban-widgets-03（高、不阻塞、人工）。

#### 3. kanban-core 純度
- `grep` kanban-core/src/main 找不到 `org.springframework`／`jakarta.persistence`；`NoSpringDependencyTest` 通過。
- 投影都放在 `io.progden.kanban.query.{timeline,wip,throughput,duedate,widgets}`；query service 直接讀 `CardJpaRepository`，跟既有 `query.featurecrboard` 同一種做法。

#### 4. 任務邊界
- `git diff loop/implementation...HEAD --stat`：44 個檔案；程式碼只動 `query.*`、`KanbanWidgetsController/Responses`、4 份 feature、`KanbanWidgetsSteps`、query 測試，另外在 kanban-core 的 `ErrorCode.java` 追加一個列舉值 `INVALID_DUEDATE_THRESHOLD_DAYS`（共用錯誤代碼，只追加；判定可接受）。
- `.state/` 只動到 `tasks/T-06-be-kanban-widgets/**`，另外新增 `adr/ADR-T-06-be-kanban-widgets-01-*.md`；沒有碰 `tasks.md`、archive 或其他任務目錄。

#### 5. OQ 核對
- OQ-01：引文跟 Review 自己跑出的報告逐字相符；但等級「高／不阻塞」標錯了。要核准就一定要綠燈建置，而要轉綠只能改 T-05／T-08 的檔案，已經超出 T-06 的邊界，本質上是環境壞掉 → 另開 **OQ-T-06-be-kanban-widgets-02**（環境、阻塞、人工），內文註明取代 OQ-01。
- OQ-03 見第 2 點。

#### 6. 前端：不適用（後端任務）

#### 判定理由
建置紅燈，照規則不能核准。可是修好紅燈必須改任務範圍外、已經合併的檔案，再跑一輪 Dev 也修不好（review-prompt Block 條件 (d)），所以判 blocked，交給人工處理 OQ-02。等人工修好整合分支的重複步驟定義、把整合分支合併進本分支之後，重跑 Review 應該就能核准；屆時要保留 OQ-03（接手：人工）。
