# T-08-be-feature-cr-board 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | done | — | orphan CR 清單會重複列出同一張 CR：`FeatureCrBoardCalculator.calculate` 的 affects 迴圈裡，每一個找不到 Feature 卡的 affects 目標都會執行一次 `orphanCrIds.add(cr.crId())`。一張 CR 卡帶「CR-010」「affects:F98」「affects:F99」時，`orphanCrIds` 會是 ["CR-010","CR-010"]。spec post p3 寫的是『該 `card` 列入 orphan CR 清單』，主詞是卡片，一張卡只該列一次。 怎樣才算修好：同一張 CR 卡在 orphan 清單最多出現一次（例如改用 LinkedHashSet，或每張 CR 只判斷一次），並在 `FeatureCrBoardCalculatorTest` 補一個「一張 CR 帶兩個不存在的 affects 目標」的測試，斷言 `orphanCrIds` 只有一筆。 |
| D-02 | 1 | todo | — | post p4『`card` 同時帶有兩個 Feature 標籤時，顯示一筆警告訊息，且不影響其他 `card` 的 Feature／CR 統計』這一條，實作和測試都沒對上： (1) 警告文字跟行為不一致：`FeatureCrBoardCalculator` 產生的警告寫『已忽略其 Feature／CR 統計』，但同一張卡上的 CR 標籤照樣進入 `crCards` 並被統計，只忽略了 Feature 標籤。請二選一：真的一併忽略這張卡的 CR 標籤，或把警告文字改成符合實際行為（例如只說忽略 Feature 標籤）。選哪一種在 decision-log 記一句理由。 (2) 沒有測試驗證「不影響其他 card」：Cucumber step「其他卡片的 Feature／CR 統計不應該受影響」（`FeatureCrBoardSteps.thenOtherCardsUnaffected`）斷言的是出錯那張卡自己的 F01／F02 沒出現，不是「其他卡片」沒受影響；`FeatureCrBoardCalculatorTest.cardWithTwoFeatureLabelsProducesWarningAndIsExcluded` 也只放一張卡。 怎樣才算修好：`FeatureCrBoardCalculatorTest` 補一個測試，輸入包含一張雙 Feature 標籤卡，加上至少一張正常 Feature 卡（例如 F03 在 DONE）和一張正常 CR 卡（affects 指向該 Feature），斷言正常卡的 Feature 狀態、CR 歸屬與狀態都正確，且 warnings 只有一筆。Cucumber step 的斷言要改成跟這個 Then 的主詞一致：Gherkin 的 Given 只有一張卡，沒辦法驗證其他卡時，在 step 註解講清楚這個 step 驗了什麼、「其他卡片不受影響」由哪個單元測試負責，不要讓斷言看起來是在驗別的東西。 |
| D-03 | 1 | todo | — | `KanbanApplication` 在 `@SpringBootApplication` 之外另外加了 `@ComponentScan(basePackages = {"io.progden.kanban.spring", "io.progden.kanban.query"})`。直接宣告的 `@ComponentScan` 會取代 `@SpringBootApplication` 內建的那一個，連帶拿掉 Spring Boot 預設的 `TypeExcludeFilter`／`AutoConfigurationExcludeFilter`。這是 Spring Boot 文件明確不建議的寫法。T-06（F03）、T-07（F05）這些 `io.progden.kanban.query.*` 投影任務之後也要動這個檔，現在的寫法容易在合併時衝突。 怎樣才算修好：拿掉 `@ComponentScan`，改成 `@SpringBootApplication(scanBasePackages = "io.progden.kanban")`（或等效的 `scanBasePackages` 寫法，要涵蓋 spring 與 query 兩個套件）。確認 JPA repository／entity 掃描不受影響，`./gradlew clean build --no-daemon` 全綠。 |
