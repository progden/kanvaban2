# T-06-be-kanban-widgets 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Dev 第 1 輪：F03 四個圖表唯讀投影，第 1 輪實作完成

### 這輪做了什麼

實作 F03（`.dev/F03-kanban-widgets/spec-kanban-widgets.md`）六個 `uc-view-*`（角色皆
`r-board-member`）對應的跨 aggregate 唯讀投影，四個 Feature 全數覆蓋：

- `Feature: Cycle Time 與 Lead Time 分析`（`uc-view-cycle-lead-time`）
- `Feature: WIP 與 Aging WIP 監控`（`uc-view-wip`、`uc-view-aging-wip`）
- `Feature: Throughput 與累積流量圖`（`uc-view-throughput`、`uc-view-cfd`）
- `Feature: 截止日期提醒`（`uc-view-duedate-reminder`）

共同資料來源沿用 `design-kanban-widgets.md` 的 `CardTimeline`／`CardTimelineProjector` 設計
（`kanban-spring` 新增 `io.progden.kanban.query.timeline`／`wip`／`throughput`／`duedate`／
`widgets` 五個套件，比照既有 `query.featurecrboard` 的純函式計算器＋`@Service` 查詢層＋web
Response 轉換三層結構）；web 層新增 `KanbanWidgetsController`，掛
`GET /api/boards/{boardId}/widgets/{cycle-lead-time|wip|aging-wip|throughput|cfd|duedate-reminder}`。

### 規格沒寫清楚、由我判斷的地方

- **`Card` 沒有 `createdAt` 欄位，`design-kanban-widgets.md` 第 5 節原本建議在 `kanban-core`
  補這個欄位，但那是 F01（`card`）的 domain 程式碼，不在 T-06 任務範圍**：改用該卡片
  `activityLog` 中最早一筆（「建立卡片」）的 `occurredAt` 推算，效果等價，不動 `Card.java`。
- **WIP 是否要排除 Done 角色的 Stage**：「其他名詞」表定義 WIP 為「目前不在 Done 角色 Stage
  的卡片數量」，但 Scenario「檢視各 Stage 目前的卡片數量」明確要求連 Stage "完成"（Done 角色）
  也要顯示卡片數（案例：待辦 3、進行中 2、完成 5）。依規則書「Gherkin/post 是驗收依據」，
  `WipCalculator` 不排除 Done 角色 Stage，純按目前所在 Stage 分組計數。低風險技術決定。
- **Lead/Cycle Time／Aging 的天數計算採「UTC 日曆日差」而非精確 24 小時倍數**：Scenario 只給
  日期（無時分秒），若用 `Duration.toDays()`，Cucumber 測試中每個動作之間有幾毫秒的真實延遲，
  會讓總時長略少於整數天數而被無條件捨去（例如 3 天差 20ms 被算成 2 天），改用
  `ChronoUnit.DAYS.between(UTC LocalDate, UTC LocalDate)` 後穩定符合 Scenario 期待值，且與
  Throughput／CFD 既有的 UTC 分組假設（design 第 7 節）一致。低風險技術決定。
- **百分位數演算法**：spec 只定案「固定顯示 P50、P85、P95」，未指定內插方式；採
  nearest-rank（`rank = ceil(p/100*n)`）。低風險技術決定。
- **截止日期提醒的 `thresholdDays` 何時計算「即將到期」**：Scenario「檢視已逾期的卡片」沒有
  設定門檻就直接開圖表；「檢視即將到期的卡片」才另外有一步「即將到期的門檻設定為 3 天」。
  判斷：`thresholdDays` 為選填 query 參數，未提供時只計算「已逾期」清單、「即將到期」清單維持
  空清單，不套用固定預設值（spec 已定案「非固定值，由使用者於查詢時傳入」，OQ 待釐清段落）。
  低風險技術決定。
- **`uc-view-duedate-reminder` fail p1 的錯誤代碼**：新增 `ErrorCode.
  INVALID_DUEDATE_THRESHOLD_DAYS`（`kanban-core/.../ErrorCode.java` 新增一個列舉值），沿用
  全專案既有的 `DomainException`／`ErrorCode`／per-controller try-catch 慣例（同
  `FeatureCrBoardController` 的作法）。`ErrorCode` 是跨 aggregate 共用的錯誤代碼列舉，不屬於
  Card／Board 任一 aggregate 的 domain 邏輯本體，判斷屬於低風險的共用檔案新增，不算「動其他
  aggregate 的 domain 程式碼」。

### 待確認事項

- `OQ-T-06-be-kanban-widgets-01`（不阻塞）：合併回整合分支後執行 `./gradlew clean build
  --no-daemon`，`RunCucumberTest` 因為 `BoardClockSteps`（T-05）與 `FeatureCrBoardSteps`
  （T-08）各自獨立宣告了完全相同的步驟文字「我已登入系統，並開啟 Board {string}」而報
  `DuplicateStepDefinitionException`，導致 kanban-spring 85 個測試裡 58 個失敗（涵蓋 F01／
  F02／F04／F06 既有 feature，不是我新增的程式碼造成，也不是 F03 規格問題）。已用暫時、未
  提交的本地修改（移除 `FeatureCrBoardSteps` 該步驟的 `@Given` 註解）驗證：修掉這個衝突後
  85 個測試全綠（含本任務新增的 16 個單元測試＋4 份 feature 檔全部 Scenario）；驗證後已用
  `git checkout` 還原該檔，這次 commit 不包含這個暫時修改（不在 T-06 任務範圍內，不可以動
  `BoardClockSteps.java`／`FeatureCrBoardSteps.java`）。已開 `ADR-T-06-be-kanban-widgets-01`
  記錄「合併後應補跑一次完整 RunCucumberTest」的流程建議。

### Check（實際跑的指令與結果）

- `./gradlew :kanban-core:compileJava :kanban-spring:compileJava --no-daemon` → BUILD
  SUCCESSFUL
- `./gradlew :kanban-spring:test --tests "io.progden.kanban.query.*" --no-daemon` → BUILD
  SUCCESSFUL，16 個新單元測試（`CardTimelineProjectorTest`／`CycleLeadTimeCalculatorTest`／
  `WipCalculatorTest`／`AgingCalculatorTest`／`ThroughputCalculatorTest`／
  `CfdCalculatorTest`／`DueDateReminderTest`）全過
- `./gradlew :kanban-spring:test --tests "io.progden.kanban.spring.cucumber.RunCucumberTest"
  --no-daemon`（本任務新增的 4 份 `.feature` 檔＋`KanbanWidgetsSteps`）：在暫時解除
  `BoardClockSteps`／`FeatureCrBoardSteps` 步驟文字衝突後跑過一次，58 個 scenario 全過
  （含既有 F01／F02／F04／F06 全部既有 scenario＋本任務新增 12 個 scenario）；還原衝突後
  `./gradlew clean build --no-daemon` 的完整結果見上方 OQ 說明（85 測試、58 失敗，全部
  `DuplicateStepDefinitionException`，非本任務程式碼問題）
- `kanban-core` 既有 47 個測試（`BoardClockTest`／`BoardTest`／`CardTest`／
  `NoSpringDependencyTest`／`UserTest`）在 `./gradlew clean build` 過程中全數通過，確認
  `kanban-core` 仍不依賴 Spring
