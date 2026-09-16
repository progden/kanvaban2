# 核心領域模型設計（看板標準圖表）

本文件記錄 F03 對應 [`spec-kanban-widgets.md`](./spec-kanban-widgets.md) 的設計決策。這些圖表不新增 `kanban-core` 的 aggregate，而是新增一個跨 aggregate 的**讀取投影**，設計精神與 CLAUDE.md「跨 aggregate 讀取」原則一致：由 `kanban-spring` 的 infrastructure／查詢層負責重播與聚合，`kanban-core` 不持有這份投影。

## 設計決策

### 1. 共同投影：CardTimeline

四組元件裡有三組（標準圖表、workload、時間表相關的 Aging）都建立在同一件事上：從事件流重播出每張卡片的時間軸。定義一份投影 `CardTimeline`，其餘圖表都只是對它做不同的聚合：

| 欄位 | 型別 | 說明 |
|---|---|---|
| cardId | uuid | PK |
| boardId | uuid | |
| title | varchar | 圖表 tooltip 用 |
| createdAt | timestamp | 取自 CardCreated |
| startedAt | timestamp（nullable） | 首次進入 role=Start 的 Stage 時間，之後不覆寫 |
| doneAt | timestamp（nullable） | 最後一次進入 role=Done 的 Stage 時間；卡片離開 Done 時清為 null，再進 Done 時重設 |
| deletedAt | timestamp（nullable） | 有值即排除在所有指標之外 |
| currentStageId | uuid | Aging／WIP 用 |
| enteredCurrentStageAt | timestamp | 本次進入目前 Stage 的時間，Aging 用 |
| stageVisits | jsonb | `[{stageId, enteredAt, leftAt}]`，Time-in-Stage／CFD 用 |
| dueDate | date（nullable） | 逾期圖用 |

處理方式：

1. 從 Board 的事件流重播（可搭配既有 snapshot 機制，從某個時間點起算）。
2. 每張卡片依事件順序更新一列；`stageVisits` 在 `CardStageChanged` 時把上一筆 `leftAt` 補上、開一筆新的。
3. `startedAt` 只在第一次進 Start 時寫入，之後不覆寫；`doneAt` 進 Done 時寫入（覆寫舊值）、離開 Done 時清空。
4. Stage 的 `role`（CR-003）只在查詢當下讀取，不寫進投影或事件；重新指派角色不需要重建投影。

### 2. 資料規模與讀寫策略

- 預估總資料量：≤ 2,000 張卡片、≤ 100,000 事件。
- 讀多寫少，寫入頻率＝使用者操作頻率。
- 判定：不需要讀寫分離；投影可在查詢時即時重播，或在每次事件後增量更新。CFD 的每日快照若效能不足再另外物化，非首版必要項目。

### 3. 圖表只回傳資料，不畫圖（既有決策延續）

所有 Feature 的 Scenario 只描述「應該顯示 XX 數值／清單」，前端負責視覺化（散佈圖、直方圖、折線圖等）。

### 4. 發現：Stage／Swimlane 刪除連帶轉移卡片，目前尚未實作

`Board.removeStage`／`removeSwimlane`（`kanban-core/src/main/java/io/progden/kanban/core/domain/Board.java`）目前卡片非空時直接拋出 `STAGE_HAS_CARDS`／`SWIMLANE_HAS_CARDS` 例外阻擋刪除，`spec-kanban-basic.md`「刪除包含卡片的 Stage 需要先轉移卡片」「刪除包含卡片的 Swimlane 需要確認」描述的轉移／連帶刪除流程尚未實作。

這不需要另開 CR——規格文字本身沒有變、只是等實作補齊。但實作時務必注意：每張被轉移或連帶刪除的卡片，都必須各自產生 `CardStageChanged`／`CardDeleted` 事件（而不是只在 Board 層記一筆活動紀錄），否則 CardTimeline 的 `stageVisits` 會缺一段，CFD／Time-in-Stage 會算錯。此為實作提醒，非規格變更。

### 5. 資料來源可行性確認：不需要新增統一事件體系

盤點 `kanban-core` 現況（`Card.java`/`Board.java`）發現：目前是「聚合根 mutate 自身狀態＋兩份側寫歷史清單」風格（`activityLog: List<ActivityRecord>` 純文字描述、`stageTransitions: List<StageTransition>` 結構化含 `fromStageId`/`toStageId`/`occurredAt`），**沒有**統一的 Domain Event 型別體系（無 event base class、無 publish/apply pattern）。

結論：`CardTimeline` 不需要在 `kanban-core` 新增一層通用事件記錄，現有結構已足夠重播出第 1 節列出的欄位：

- `createdAt`：原本缺此欄位（`activityLog` 只有文字，無法穩定辨識建立事件），已在 `kanban-core` 補上 `Card.createdAt`（`create(...)` 時寫入、不可變，見 `spec-kanban-basic.md` 對應的 F01 `design.md` 落差說明；純技術性附加欄位，不影響任何既有 Scenario 斷言，不需開 CR）。
- 卡片初始所在 Stage（用於重建第一段 `stageVisits`）：`stageTransitions` 為空時，初始 Stage＝卡片目前的 `stageId`；不為空時，初始 Stage＝`stageTransitions.get(0).fromStageId()`（第一筆異動的來源 Stage，即建立時的位置）。
- `stageVisits`：由 `createdAt` + 初始 Stage 起頭，逐筆套用 `stageTransitions`（每筆的 `occurredAt` 同時是上一段的 `leftAt` 與下一段的 `enteredAt`）重建；`currentStageId`／`enteredCurrentStageAt` 取最後一段。
- `startedAt`／`doneAt`：查詢時讀取 Stage 目前的 `role`（CR-003），對照 `stageVisits` 找出首次進入 Start 角色 Stage 的 `enteredAt`（`startedAt`，只取第一次）與最後一次進入 Done 角色 Stage 的 `enteredAt`（`doneAt`，離開 Done 後清空、再進入時取最新一次）。
- Swimlane 移動、留言等不影響 `CardTimeline` 欄位，不需額外資料來源。

### 6. `kanban-spring` 套件與測試風格慣例

- 查詢層套件位置：`io.progden.kanban.query.timeline`（`CardTimeline`／`StageVisit`／`CardTimelineProjector`），其餘三個 Feature（WIP/Aging、Throughput/CFD、到期提醒）依此慣例各自在 `io.progden.kanban.query` 下開子套件。
- 測試風格：比照 `kanban-core` 既有慣例（驗收測試用 Cucumber、domain/計算邏輯單元測試用 JUnit5+AssertJ）——`CardTimeline` 重建演算法本身是純函式（不依賴 Spring context），用 JUnit5+AssertJ 對照 `spec-kanban-widgets.md` 的 Scenario 逐條寫單元測試；`kanban-spring` 尚未引入 Cucumber 依賴，四個 Feature 的查詢 API／web 層要接起來時再評估是否需要補上。
- `kanban-spring` 測試依賴：`spring-boot-starter-data-jpa-test`（test scope）已透傳 `spring-boot-starter-test`，JUnit Jupiter、AssertJ、Mockito 皆可直接使用，不需另外加依賴。

### 7. Throughput／CFD 演算法與時區假設

- 套件位置：`io.progden.kanban.query.throughput`（`ThroughputCalculator`／`CfdCalculator`），沿用第 6 節套件慣例。
- Throughput（「檢視每日完成卡片數量」Scenario）：`ThroughputCalculator.countByDate` 只對 `CardTimeline.doneAt() != null` 的卡片，依 `doneAt` 對應的日期分組計數；重用既有 `CardTimeline`，不需新資料來源。
- CFD（「檢視累積流量圖」Scenario）：規格文字只要求「顯示每一天、每個 Stage 的累積卡片數量」，沒有明講「累積」的語意是「曾經到達」還是「目前仍在」。`CfdCalculator.cumulativeByStageAndDate` 採用標準 CFD 語意——卡片一旦（首次）進入某 Stage，該 Stage 從進入當天起的累積計數即視為包含這張卡片，即使卡片後續已離開該 Stage 前進到下一個 Stage（因此各 Stage 的累積數隨時間單調不減，符合 CFD 圖形「疊層越往右越寬」的典型外觀）；`asOf` 由呼叫端傳入（比照 `AgingCalculator.age` 的 `asOf` 參數慣例，對應 CR-004 的 Board Clock 當下時間），日期範圍起點取所有卡片最早的到達日期、終點為 `asOf` 所在日期。
- **本次假設（依「決策規則 2」，2026-09-13）**：日期分組（Throughput 的完成日、CFD 的到達日）一律採用 `ZoneOffset.UTC` 轉換 `Instant → LocalDate`，不使用 `ZoneId.systemDefault()`。理由：專案目前唯一與時區相關的既有程式碼是 `BoardClock` 內部 `HOUR_MINUTE` 格式化字串用 `ZoneId.systemDefault()`（純顯示用途，不影響資料判斷），而所有測試資料建置慣例（`CardTimelineProjectorTest`／`WipCalculatorTest`／`AgingCalculatorTest` 的 `instantOf` helper）都明確用 `ZoneOffset.UTC` 建立卡片時間；規格與 `spec-board-clock.md` 皆未定義看板所在時區。為避免「同一支 Instant 因執行環境的預設時區不同而算出不同日期」造成不可重現的結果，選擇與既有測試資料一致、與執行環境無關的 UTC 作為分組時區，之後若規格明確定義看板時區（例如允許使用者設定），再回來調整此假設。

- 見 `spec-kanban-widgets.md` Open Questions（Flow Efficiency、Blocked 時間、CFD 快照策略、即將到期門檻）。

## 實作狀態

- 「Cycle Time 與 Lead Time 分析」Feature：已實作（`kanban-spring` `io.progden.kanban.query.timeline` 套件），三條 Scenario（已完成卡片、未經 Start 就完成、離開 Done 再完成）皆有對應 JUnit5+AssertJ 單元測試並全綠。
- 「WIP 與 Aging WIP 監控」Feature：已實作（`kanban-spring` `io.progden.kanban.query.wip` 套件：`WipCalculator.countByStage` 依 `CardTimeline.currentStageId` 分組計數；`AgingCalculator.age` 只對已進入 Start、尚未完成（`doneAt == null`）的卡片計算 `asOf - startedAt`），兩條 Scenario 皆有對應 JUnit5+AssertJ 單元測試並全綠。
- 「Throughput 與累積流量圖」Feature：已實作（`kanban-spring` `io.progden.kanban.query.throughput` 套件：`ThroughputCalculator.countByDate`、`CfdCalculator.cumulativeByStageAndDate`，日期分組時區假設見第 7 節），兩條 Scenario 皆有對應 JUnit5+AssertJ 單元測試並全綠。
- 「截止日期提醒」Feature：已實作（`kanban-spring` `io.progden.kanban.query.duedate` 套件：`DueDateReminder.overdue` 篩選尚未完成、`dueDate` 早於 `asOf` 的卡片；`DueDateReminder.upcoming` 篩選尚未完成、`dueDate` 落在 `[asOf, asOf + thresholdDays]` 範圍內的卡片；`asOf`／`dueDate` 皆為 `LocalDate`，不需額外時區轉換），兩條 Scenario 皆有對應 JUnit5+AssertJ 單元測試並全綠。至此 F03 四個 Feature 全部實作完成。
