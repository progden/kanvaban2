# T-08-be-feature-cr-board-r2 state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：CR-013 差異已實作完成，等待 Review。

這輪做了什麼：`FeatureCrBoardCalculator` 改成 Feature 分組與 orphan 判定互斥（affects 目標沒有對應
Feature 卡時只列入 orphan，不再自建佔位 Feature），同步更新 `feature-cr-board.feature`（補
`@CR-013` tag 與新增的 Given）與 `FeatureCrBoardCalculatorTest`（新增一則測試驗證無佔位 Feature）。
`./gradlew clean build --no-daemon` BUILD SUCCESSFUL，全部測試綠，含 Cucumber 5 個 Scenario 與
Calculator 8 個單元測試。沒有新開 OQ。

Review 要先看什麼：`FeatureCrBoardCalculator.calculate` 的 affects 迴圈改動是否真的互斥（不再
`putIfAbsent` 佔位 Feature）；`feature-cr-board.feature` 是否與目前 `spec-feature-cr-board.md` 逐字
相符；新測試 `crAffectingUnknownFeatureDoesNotCreatePlaceholderFeature` 是否確實驗到「features 為
空」這個關鍵斷言。
