## ADR-T-03-be-card-02：跨 Feature 共用逐字 Gherkin 步驟文字時，用共用 Spring bean 分流

- 狀態：Accepted
- 日期：2026-09-19
- 提出者：Dev（T-03-be-card）

### 背景（Context）

`spec-convention.md` 要求 Gherkin 步驟用意圖語言逐字複製進 `.feature` 檔（見
`spec-migration-loop` 的既有慣例），但沒有規定不同 Feature 之間的步驟文字要互斥。
`spec-kanban-basic.md`「Swimlane 管理」與「Card（卡片）編輯」兩個 Feature 剛好有三段
逐字相同的步驟：「我確認新增」「我確認刪除」「該操作應該被記錄為一筆活動紀錄，包含操作人
與操作時間」。Cucumber 同一段文字只能對應一個 step definition，兩個 step class
（`BoardSteps`／`CardSteps`）各自宣告會是「Ambiguous step definitions」執行期錯誤。

### 決策（Decision）

新增一個 Spring 單例 bean `CrossAggregateState`（`kanban-spring/src/test/.../cucumber/`），
暫存「這次操作實際要提交到哪個 Aggregate／哪個 id」。文字最先出現的一方
（`BoardSteps`，因為 Swimlane 功能先實作）保留 `@When`／`@Then` 宣告本體，內部先檢查
`CrossAggregateState` 是否有另一個 Aggregate 的待處理標記，有的話呼叫對方 step class
（`CardSteps`）暴露的 package-private 方法（例如 `submitPendingAddCard()`、
`confirmDeleteCard(cardId)`、`assertCardActivityRecorded()`）完成實際動作，沒有才走原本
的邏輯。`BoardSteps` 對 `CardSteps` 的欄位注入需要加 `@Lazy`（`org.springframework.context.
annotation.Lazy`），因為 `CardSteps` 同時也要反向注入 `BoardSteps`（取得 session／
currentBoardId／currentUserId 等背景狀態），不加會在啟動時丟出
`BeanCurrentlyInCreationException`。`CrossAggregateState` 的欄位由 `BoardSteps.
resetBoardState()`（`@Before`）統一重置，確保每個 Scenario 乾淨。

### 考慮過的替代方案（Alternatives）

- 幫每個 Feature 的重複步驟加上不同前綴文字避免撞名。放棄原因：違反
  `spec-convention.md`「Gherkin 步驟逐字複製」的要求，會讓 `.feature` 檔偏離 spec 原文。
- 把所有 Aggregate 的 step definitions 合併成一個巨大類別。放棄原因：違反本 loop
  一個任務只能動自己範圍檔案的邊界，且會讓檔案無限增長、後續任務（T-04 也要動
  `BoardSteps`）衝突機率更高。

### 後果（Consequences）

- 之後任何模組的 spec 若與既有 Feature 共用逐字步驟文字（例如 F02 的活動紀錄相關步驟、
  F06 的刪除確認流程），都應該比照這個模式：新增／重用 `CrossAggregateState`（或視情況
  另開一個同樣模式的共用 bean），不要各自宣告重複的 `@When`／`@Then`。
- `BoardSteps` 與 `CardSteps` 之間現在有雙向依賴（透過 `@Lazy` 打破初始化順序問題）；
  T-04-be-board-membership 備註寫明會依序在 T-03 之後修改 `BoardSteps.java`，動工前
  應先讀本 ADR，避免破壞這個分流機制或重新引入循環注入例外。
