# T-08-be-feature-cr-board 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Review 第 1 輪：退回

### 判定：退回（第 1 輪／上限 6）

### 1. 建置與測試（我自己跑的）
- 第一次跑 `./gradlew build --no-daemon`，12 個 task 全部 UP-TO-DATE，等於沒真的跑測試。改跑 `./gradlew clean build --no-daemon`：**BUILD SUCCESSFUL in 2m 9s**。
- 彙總 test-results XML：共 84 個測試，0 failure／error。kanban-core 36 個（BoardTest 17、CardTest 8、UserTest 10、NoSpringDependencyTest 1）；kanban-spring 48 個，其中 Cucumber「Feature／CR 追蹤表」5 個、FeatureCrBoardCalculatorTest 5 個。
- 建置本身沒問題，退回的原因是下面第 2 點的行為與測試缺口。

### 2. spec 對應
- `resources/features/feature-cr-board.feature` 跟 spec 的 gherkin 區塊比對過，逐字相同，只多了開頭兩行註解。5 個 Scenario 都有 step，step 透過 MockMvc 打實際端點，沒有繞過 web 層。
- p1（Feature 狀態）、p2（CR 顯示在 Feature 底下）、p5（不分大小寫）的行為符合 spec。
- p3 有瑕疵：同一張 CR 卡有多個不存在的 affects 目標時，orphan 清單會重複列出 → D-01。
- p4 有瑕疵：警告文字說『已忽略其 Feature／CR 統計』，但這張卡上的 CR 其實還是被統計；「不影響其他 card」完全沒有測試驗證，對應 step 斷言的是出錯那張卡自己 → D-02。
- `uc-view-feature-cr-board` 是 `fail: {}`，沒有 `@fail-pN` 可以抽查。controller 另外處理了未登入（401）與看板不存在（404），這兩個是 spec 沒定義的前置防護，屬於低風險。

### 3. kanban-core 純度
- `grep -rlE "org\.springframework|jakarta\.persistence" kanban-core/src/main` 沒有結果，本任務也沒改到 kanban-core。
- 投影放在 `io.progden.kanban.query.featurecrboard`，符合 CLAUDE.md 的慣例。

### 4. 任務邊界
- `git diff loop/implementation...HEAD --stat`：共 17 個檔，改動都在 `kanban-spring/**` 與 `.state/tasks/T-08-be-feature-cr-board/**`，沒動到 spec／ui／conventions／scripts／tasks.md。
- 共用檔 `KanbanApplication.java` 為了讓 Spring 掃到 query 套件而改動，這個改動本身屬於任務範圍，但寫法有問題 → D-03。

### 5. OQ 核對
- OQ-T-08-be-feature-cr-board-01 引文 p2／p3 跟 spec usecase 區塊（YAML 解碼後）逐字相符，等級「高／不阻塞／人工」也對。但它寫『ui-feature-cr-board.md 也沒有描述這個組合狀態』，這句不正確：ui 檔「CR 所屬 Feature」列寫的是『指到不存在的 Feature 編號時改列入 orphan CR 清單（post p3）』，是互斥語意，跟目前「兩處都出現」的實作矛盾。
- 我另開 **OQ-T-08-be-feature-cr-board-02**（高、不阻塞、接手：人工、spec-conflict）取代 -01。內文把 ui「改列入」、spec 兩個 Scenario、spec 檔頭的資料來源限制、ui「Feature 狀態」列逐字並列，也點出佔位 Feature 的「未開發」狀態是實作自己給的。
- 判斷不阻塞的理由：照上游的 spec 做（兩個 Scenario 都要過）不需要違反任何 spec 原文。和 ui 的矛盾會在 T-20 呈現時浮現，需要人工開 CR 統一。

### 6. 前端
- 本任務只有後端，不適用。

### 修正項
- D-01：orphan 清單重複列出同一張 CR。
- D-02：p4 警告文字跟行為不一致，「不影響其他 card」沒有測試。
- D-03：`@ComponentScan` 改成 `@SpringBootApplication(scanBasePackages=...)`。
