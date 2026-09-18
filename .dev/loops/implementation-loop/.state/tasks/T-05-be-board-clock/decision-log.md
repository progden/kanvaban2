# T-05-be-board-clock 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Dev 第 1 輪：實作看板時鐘 CR-004

### 這輪做了什麼

依 `.dev/F04-board-clock/spec-board-clock.md`（定稿）與 `design-board-clock.md` 的既有設計決策，
實作 Board Clock（CR-004）：

- `kanban-core`：新增 `ClockStatus`（REALTIME／PAUSED）、`BoardClock`（`Board` 內部值物件，
  package-private）、`BoardClockSnapshot`（供 reconstruct／persistence，比照 `SwimlaneSnapshot` 模式）。
  `BoardClock` 提供 `now`（唯讀）、`recordEventTime`（寫入新事件用，含
  `uc-guard-clock-monotonicity` 的單調性檢查）、`adjustTo`／`pause`／`resume`（時鐘控制本身不受單調
  性限制，可回溯）、`clampSystemNow`（design 第 5 節：夾住系統時鐘取樣抖動）。
- `Board`／`Card` 既有的寫入方法（`addSwimlane`…`setStageRole`、`Card.create`…`delete`）全部改為
  接受呼叫端傳入的 `systemNow`／`now`，取代原本內部呼叫的 `Instant.now()`；`Board` 新增
  `adjustClock`／`pauseClock`／`resumeClock`／`newEventTime`／`getClockTime`／`getClockStatus`。
- `kanban-spring`：`BoardApplicationService` 新增 `adjustClock`／`pauseClock`／`resumeClock`
  （Instant.now() 只在這一層讀取，符合 kanban-core 不依賴框架的原則）；`CardApplicationService`
  每個寫入方法先載入所屬 `Board`、透過 `newEventTime` 取得並 guard 時間，再存回 `Board`（讓
  `lastEventAt` 基準生效）。`BoardJpaEntity` 新增 5 個時鐘欄位（就地更新，比照 Swimlane／Stage
  調和模式），`BoardController` 新增 `PATCH /clock`、`POST /clock/pause`、`POST /clock/resume`，
  `BoardResponse` 新增 `clockTime`／`clockStatus`。

### 涵蓋範圍

- Entity／欄位：`board.clock-time`、`board.clock-status`（`BoardClock` 的 `now`／`getStatus`）。
- UseCase：`uc-adjust-board-clock`、`uc-guard-clock-monotonicity`、`uc-pause-resume-board-clock`
  三個全部實作，`board-clock.feature`（逐字複製 spec 8 個 Scenario）全過。
- `Board`／`Card` 既有九個結構調整方法＋六個 Card 寫入方法的 `occurredAt` 來源全部改為 Board Clock
  （CR-004 的完整範圍，不只 F04 自己的 Feature），既有 F01/F02 的 15 個 Scenario（swimlane／
  stage／card-editing／user-login）維持全過，行為不變，只換了時間來源。

### 關鍵決策

1. **調整時鐘 vs 寫入新事件分兩條路徑**：`adjustClock`／`pauseClock`／`resumeClock` 用
   `recordClockActivity`（唯讀 `clock.now()`，不 guard、不更新 `lastEventAt`），一般寫入用
   `recordActivity`（`clock.recordEventTime()`，guard＋更新 `lastEventAt`）。理由：Scenario
   「看板時間可以往回調整」證明調整動作本身不受單調性限制，但這個動作的活動紀錄若誤用
   guard 路徑，回調時間時會被自己觸發的 guard 擋下（見 `Board.adjustClock` 註解）。
2. **Owner 權限用 `board.createdBy` 代理**：`BoardMembership`（F02，T-04）不在本任務依賴範圍，
   比照 OQ-IMPL-15 的既有模式延後正式權限檢查，但 `uc-adjust-board-clock` 自己逐字定義了
   「非 Owner」fail Scenario（不像 F01 那 9 個 uc 整段延後給 F02），所以改用
   `board.createdBy.equals(operatorId)` 代理 Owner 判斷，讓這個 Scenario 現在就能測、又不會誤判
   真正的建立者為非 Owner。開了 OQ-T-05-be-board-clock-01（高風險、不阻塞、owner 指向 T-04）
   說明理由與待 T-04 後續處理方式。
3. **Cucumber 測試的日期基準問題**：`board-clock.feature` 的 Scenario 用固定的 2026-09-12
   模擬「現在」，但 `Board.create` 第一筆「建立看板」活動一定用真實系統時間（測試執行當下，
   例如今天 2026-09-19），比 Scenario 用的虛構日期晚，若不處理會讓
   `uc-guard-clock-monotonicity` 誤判「看板剛建立的真實時間」擋住所有 Scenario 的後續操作。
   低風險技術決定：`BoardClockSteps` 在每個 Scenario 的 Background 第一次執行
   「看板時間目前為」步驟時，直接改寫 persistence 層的 `lastEventAt` 到一個早於所有 Scenario
   日期的錨點（2000-01-01），之後同一個 Scenario 內都走正式的 `adjustClock`／建卡片端點，不再
   繞過 guard（見 `BoardClockSteps` 類別註解）。
4. **活動紀錄「最新一筆」改用插入順序**：`BoardSteps.thenActivityRecorded` 原本用
   `max(occurredAt)` 找最新一筆，board clock 可能把新活動的 `occurredAt` 設成早於「建立看板」
   的真實時間，`max(occurredAt)` 會選錯。`BoardClockSteps` 自己的活動斷言改用
   `activityLog.get(size-1)`（插入順序），不動 `BoardSteps` 既有邏輯（範圍外，且既有 15 個
   Scenario 都還是用真實系統時間，`max(occurredAt)` 對它們仍然正確）。

### 跳過／延後（附理由）

- Owner 正式權限查詢（`BoardMembership`）：見 OQ-T-05-be-board-clock-01，待 T-04。
- `board.clock-time`／`clock-status` 前端呈現（`s-board-clock-control` 對話框）：屬於
  T-17-fe-clock-control 範圍，本任務只做後端 API（`BoardResponse` 已附欄位）。

### Check

- `./gradlew clean build --no-daemon`：exit 0。
- `kanban-core`：47 測試全過（含新增 `BoardClockTest` 11 個、`BoardTest`／`CardTest` 更新後
  17／8 個）。
- `kanban-spring`：`board-clock.feature` 8 個 Scenario 全過，既有 4 個 feature 檔（swimlane／
  stage／card-editing／user-login-logout）共 30 個 Scenario、`BoardApplicationServiceTest` 2 個、
  smoke test 1 個，全部維持綠燈。
