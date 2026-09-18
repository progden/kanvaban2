# T-08-be-feature-cr-board state

> 2026-09-19 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

Review 第 2 輪待審。這輪修完第 1 輪退回的三條 D-xx：

- D-01：orphan 清單改成每張 CR 卡只判斷一次（LinkedHashMap／boolean 累積，非逐 target 累加），補單元測試。
- D-02：警告文字改成如實描述「只忽略 Feature 標籤、CR 標籤仍照常統計」；補「雙 Feature 標籤卡不影響其他正常卡片」單元測試；Cucumber step 補註解說明其驗證範圍。
- D-03：`KanbanApplication` 改用 `@SpringBootApplication(scanBasePackages = "io.progden.kanban")`，拿掉獨立 `@ComponentScan`。

`./gradlew clean build --no-daemon` BUILD SUCCESSFUL，`FeatureCrBoardCalculatorTest` 5→7 筆全綠，Cucumber 5 個 Scenario 仍全綠。

OQ-01／OQ-02（第 1 輪留下，ui／spec 對 orphan 與 affects 同時成立的矛盾）維持不動，本輪未新增 OQ。

Review 請先看：D-01 的 boolean 累積邏輯是否真的涵蓋「多個 affects 目標都指到不存在 Feature」的情況；D-02 選擇「改文字不改行為」的理由是否站得住腳。
