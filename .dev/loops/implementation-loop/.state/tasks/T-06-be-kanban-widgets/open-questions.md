# T-06-be-kanban-widgets open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-06-be-kanban-widgets-01

[Level: F03-kanban-widgets/T-06-be-kanban-widgets]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：cucumber-duplicate-step-definition
- 開立：Dev 第 1 輪（2026-09-19）
- 狀態：待處理

情況：【推論＋所本原文】
證據（本輪 `./gradlew clean build --no-daemon` 實際輸出，非規格原文，是本輪跑出的建置紀錄，逐字引用如下）：
『RunCucumberTest > 看板時間管理 > 把看板時間調整到未來後建立卡片，事件時間應為調整後的時間 FAILED
    io.cucumber.core.runner.DuplicateStepDefinitionException at CachingGlue.java:278』
測試報告 XML（`kanban-spring/build/test-results/test/TEST-feature_classpath_features-board-clock.feature.xml`）逐字：
『Duplicate step definitions in io.progden.kanban.spring.cucumber.BoardClockSteps.givenLoggedInAndBoardOpened(java.lang.String) and io.progden.kanban.spring.cucumber.FeatureCrBoardSteps.givenLoggedInAndBoardOpened(java.lang.String)』

推論：`BoardClockSteps`（T-05-be-board-clock 產出）與 `FeatureCrBoardSteps`（T-08-be-feature-cr-board 產出）各自獨立定義了完全相同的 Cucumber 步驟文字「我已登入系統，並開啟 Board {string}」，兩個 worktree 開發當下互相看不到對方的程式碼（見 implementation-loop 規則書第 4 節「平行執行模型」），合併回整合分支後才會同時出現在 glue package（`io.progden.kanban.spring.cucumber`），Cucumber 掃描整個 glue package 時就會報 `DuplicateStepDefinitionException`。這個衝突會讓 `RunCucumberTest` 底下**所有** feature（不限 F03／F04／F06，包含 F01 的 `card-editing.feature`／`stage-management.feature`／`swimlane-management.feature`、F02 的 `create-user-account.feature`／`user-login-logout.feature`）全部失敗，我本輪執行 `./gradlew clean build --no-daemon` 的實際結果是 85 個 kanban-spring 測試中 58 個失敗，全部是這個原因（非我新增的程式碼造成）。

我用暫時、未提交的本地修改驗證：把 `FeatureCrBoardSteps.givenLoggedInAndBoardOpened` 的 `@Given` 註解暫時移除（讓它退化成一般方法，不再重複註冊這句步驟文字），`./gradlew :kanban-spring:test`（含全部 85 個測試）全綠，驗證完後已用 `git checkout` 還原該檔，本次 commit 沒有包含這個暫時修改（見 decision-log）。

問題：這個衝突是 T-05／T-08 兩個已合併任務遺留的既有缺陷，跟 F03（T-06）規格本身無關；T-06 的任務範圍規則明確禁止修改 `BoardClockSteps.java`／`FeatureCrBoardSteps.java`（不屬於本任務範圍的檔案）。是否要在後續某個任務（或人工介入）修掉這個重複步驟定義，讓 `./gradlew build`／`RunCucumberTest` 能夠整體跑綠？
選項：A. 人工直接修（刪除其中一份重複定義，例如把 `FeatureCrBoardSteps` 的該步驟改成呼叫 `BoardClockSteps` 現有方法，或反過來）；B. 指定下一個會動到這兩個檔案其中之一的任務（例如 T-07-be-workload 若也需要新增類似 Background 步驟）順手一併修掉；C. 維持現狀，等到有任務明確需要修改這兩個檔案時再處理。

## OQ-T-06-be-kanban-widgets-02

[Level: F03-kanban-widgets/整合分支 RunCucumberTest（取代 OQ-T-06-be-kanban-widgets-01）]
- 等級：環境
- 阻塞：是
- 接手：人工
- 原因代碼：env-broken
- 開立：Review 第 1 輪（2026-09-19）
- 狀態：待處理

情況：【推論＋所本原文】
本則取代 OQ-T-06-be-kanban-widgets-01（Dev 標為「高／不阻塞」，等級與阻塞標錯；問題本身與證據相同）。

所本原文一（review-prompt.md「你要做什麼」第 1 點）：『任何一個測試沒過、建置失敗，直接判定退回，不用往下看程式碼細節。』
所本原文二（review-prompt.md「判斷要不要擋」）：『缺工具／環境壞掉、自己修不了 → 等級「環境」、阻塞』
所本原文三（review-prompt.md「你要做什麼」第 4 點）：『動到別的 aggregate、別的模組、`.dev/conventions/**`、`scripts/**`、spec／ui 文件本體，一律判定退回。』

Review 第 1 輪實際在本 worktree 執行 `./gradlew clean build --no-daemon` 的輸出（逐字）：
『85 tests completed, 58 failed』
『RunCucumberTest > 使用者登入與登出 > 使用正確帳號密碼登入 FAILED
    io.cucumber.core.runner.DuplicateStepDefinitionException at CachingGlue.java:278』
測試報告 `TEST-feature_classpath_features-board-clock.feature.xml` 逐字：
『Duplicate step definitions in io.progden.kanban.spring.cucumber.BoardClockSteps.givenLoggedInAndBoardOpened(java.lang.String) and io.progden.kanban.spring.cucumber.FeatureCrBoardSteps.givenLoggedInAndBoardOpened(java.lang.String)』
`git grep` 整合分支 `loop/implementation`（d95b95d）與本分支的分岔點 f645776 都已同時存在：
『BoardClockSteps.java:92:    @Given("我已登入系統，並開啟 Board {string}")』
『FeatureCrBoardSteps.java:49:    @Given("我已登入系統，並開啟 Board {string}")』

推論：這是 T-05-be-board-clock 與 T-08-be-feature-cr-board 平行開發、先後合併後在整合分支上留下的既有缺陷，不是 T-06 造成的；整合分支本身的 `./gradlew build` 目前就是紅燈。T-06 的 Dev 要讓建置轉綠，只能修改 `BoardClockSteps.java` 或 `FeatureCrBoardSteps.java`，這兩個檔屬於 T-05／T-08 已合併的範圍，依所本原文三不能在 T-06 裡改；所以再跑一輪 Dev 也修不好，退回只會空轉。
推論：Review 另外把 HEAD 用 `git archive` 匯出到 /tmp，只把 `FeatureCrBoardSteps.java` 第 49 行的 `@Given` 註解掉（worktree 本身沒動），`./gradlew clean build --no-daemon` → BUILD SUCCESSFUL，85 個測試全過，其中 F03 四份 feature 共 10 個 Scenario 全過。也就是說，這個衝突修好之後，T-06 本身的程式碼看起來可以核准。

問題：整合分支上 `BoardClockSteps`／`FeatureCrBoardSteps` 重複宣告「我已登入系統，並開啟 Board {string}」，導致 `./gradlew build` 紅燈。這要由誰、在哪裡修？修好之前 T-06 無法取得綠燈建置，不能核准。
選項：A. 人工直接在 `loop/implementation` 修（例如刪掉 `FeatureCrBoardSteps` 的重複 `@Given`，改呼叫 `BoardClockSteps` 的既有方法），再把整合分支合併進本 worktree，重跑 T-06 Review；B. 在 `.state/tasks.md` 新增一個修正任務（允許改 T-05／T-08 的 step 檔），完成後 T-06 再 rebase／merge 並重跑 Review；C. 人工明確授權 T-06 的 Dev 在本任務範圍內一併修掉這個重複定義（等於放寬第 4 點的邊界），再跑一輪 Dev。

## OQ-T-06-be-kanban-widgets-03

[Level: F03-kanban-widgets/uc-view-wip]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-conflict
- 開立：Review 第 1 輪（2026-09-19）
- 狀態：待處理

情況：【兩處矛盾並列】
`.dev/F03-kanban-widgets/spec-kanban-widgets.md`「其他名詞」表（第 33 行）：
『| WIP（Work In Progress） | 目前不在 Done 角色 Stage 的卡片數量，依 Stage 分組統計 |』
同檔 `uc-view-wip` 的 post（第 136 行）：
『依 Stage 分組顯示目前的 `card` 數量』
同檔 Scenario「檢視各 Stage 目前的卡片數量」（第 171 行；Background 已寫『Stage "完成" 已設定角色為 Done』）：
『Then 應該顯示 Stage "待辦" 卡片數 3、"進行中" 卡片數 2、"完成" 卡片數 5』

推論：名詞表的定義會把 Done 角色的 Stage 排除在 WIP 之外，但 Scenario 要求 Done 角色的 Stage "完成" 也要列出卡片數 5，兩者矛盾。Dev 第 1 輪依 post／Scenario 實作（`WipCalculator` 不排除 Done 角色 Stage），只寫進 decision-log 當成「低風險技術決定」，沒有開 OQ；依規則書第 5 節，spec 內部說法不一致屬於高風險，應該要開 OQ。這樣實作不用改任何定稿文字（Scenario 是驗收依據），所以不阻塞。本則由 Review 補開。

問題：`uc-view-wip` 回傳的各 Stage 卡片數，是否要包含 Done 角色的 Stage？
選項：A. 包含（維持現行實作，名詞表的 WIP 定義之後走 CR 改寫成跟 Scenario 一致）；B. 排除（改 Scenario 走 CR，實作之後跟著改 `WipCalculator`）；C. 兩者都回傳（各 Stage 卡片數照常列出，另外回一個不含 Done 的 WIP 總數），走 CR 讓名詞表與 post 一起定案。
