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
