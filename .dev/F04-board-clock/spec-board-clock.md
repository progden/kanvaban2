# 看板時間（Board Clock）使用案例（BDD）

本文件涵蓋看板「現在」時間的管理機制：

- 每個 Board 擁有自己的時鐘，可調整到未來、可暫停，取代系統時間作為 Board／Card 事件的時間來源
- 時鐘的單調性限制：可以往回調閱讀歷史，但不可以在調回過去後寫入新事件

Board Clock 是 F03（標準圖表）計算「asOf」、Aging、逾期判斷的時間基準，也是 CR-004（事件時間戳來源改為 Board Clock）的規格依據。

狀態：定稿

## 名詞定義

### 實體
| ID | 名詞 | 所屬 Aggregate | 說明 |
|---|---|---|---|

### 欄位
| ID | 型別／格式 | 限制 | 說明 |
|---|---|---|---|
| board.clock-time | datetime | 必填 | 該 `board` 目前的看板時間值（BoardClock 的「現在」） |
| board.clock-status | enum(REALTIME, PAUSED) | 必填，預設 REALTIME | 看板時鐘目前模式，決定看板時間是否隨系統時間前進 |

### 關係
| 來源 | 目標 | min | max | 說明 |
|---|---|---|---|---|

### 其他名詞
| 名詞 | 說明 |
|------|------|
| Board Clock（看板時鐘） | 某個 Board 專屬的「現在」時間來源，取代系統時間（wall clock）作為該 Board 底下所有事件的「occurredAt」 |
| REALTIME 模式 | 看板時鐘隨系統時間正常前進，可能帶有一個時間偏移量（offset） |
| PAUSED 模式 | 看板時鐘停在某個固定時間點，不隨系統時間前進 |
| 單調性（Monotonicity） | 新事件的發生時間不得早於該 Board 已存在的最後一筆事件時間 |

## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|

## Aggregate 標記說明

每個 Scenario 上方以 Gherkin 註解標記會存取哪些 Aggregate 以及存取方式（「read」／「write」），格式與判定原則同 `spec-kanban-basic.md`。本文件用到的 Aggregate：

- `board`：「BoardClock」是「Board」 aggregate 內部狀態（不是獨立 aggregate），調整時鐘即為對「Board」的「write」。

## 變更紀錄（Change Log）

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-13 | CR-004 | 定案 | Open Question 定案：調整／暫停／恢復看板時間限 Owner 並記錄活動紀錄，補上對應 Scenario；PAUSED 模式排序沿用既有 ArrayList 插入順序，不新增 sequence 欄位（見決議紀錄） |
| 2026-09-13 | CR-004 | 開發中 | 「kanban-core」完成「BoardClock」（「Board」內部值物件）與「Board.now()」／「adjustClock」／「pauseClock」／「resumeClock」；既有「Board」寫入方法的單調性檢查已生效（「BOARD_CLOCK_BEHIND_LAST_EVENT」）。對應 Cucumber Scenario（「board-clock.feature」）以 Board 既有寫入動作作為「新事件」的測試替身，全綠。「Card」尚未改用「Board.now()」，「kanban-spring」呼叫端尚未串接，留待下一輪。 |
| 2026-09-13 | CR-004 | 開發完成 | 「Card」全部寫入方法改用呼叫端傳入的「OperationContext(operatorId, now)」，時間來源改為「Board.newEventTime()」；「board-clock.feature」「不可寫入新事件」情境改用真正建立卡片驗證。「kanban-spring」目前仍無 application 層程式碼，呼叫端串接留待該層實際開發時再處理，不阻塞本次結案。CR-004 狀態改「處理完成」。 |
| 2026-09-16 | CR-005 | 變更 | 規格格式遷移至 usecase 區塊（`uc-adjust-board-clock`、`uc-guard-clock-monotonicity`、`uc-pause-resume-board-clock`） |
| 2026-09-18 |  | 新增 | 補上遺漏的看板時間欄位（ui-authoring-loop OQ-36 發現：名詞定義實體表／欄位表皆為空，`uc-adjust-board-clock`／`uc-pause-resume-board-clock` post 只用文字描述），新增 `board.clock-time`（目前值）、`board.clock-status`（enum REALTIME/PAUSED），兩個 usecase 的 post／fail 同步改為引用這兩個欄位；本檔尚未進入開發，可直接補上，不需開 CR |

---

## Feature: 看板時間管理

### Use Case 定義
```usecase
- id: uc-adjust-board-clock
  name: 調整看板時間
  roles: [r-board-owner]
  crud: {board: U}
  pre:
    p1: "我是該 `board` 的 Owner"
  post:
    - "`board.clock-time` 更新為指定時間"
    - "該操作被記錄為 `board` 的一筆活動紀錄，說明看板時間被調整為指定時間，包含操作人與操作時間"
  fail:
    p1: "拒絕，顯示錯誤訊息「只有 Owner 可以調整看板時間」，`board.clock-time` 維持不變"
  emits: []
  requires: []
  calls-sync: []

- id: uc-guard-clock-monotonicity
  name: 看板時間早於最後事件時阻擋寫入
  roles: [r-user]
  crud: {board: U}
  pre:
    p1: "看板時間不早於該 `board` 最後一筆事件的發生時間"
  post:
    - "允許在 `board` 底下建立新事件，新事件的時間戳記使用目前的看板時間"
  fail:
    p1: "拒絕，顯示錯誤訊息「看板時間早於最後一筆事件（13:00），無法建立新事件」，不建立新事件，`board` 資料不變"
  emits: []
  requires: []
  calls-sync: []

- id: uc-pause-resume-board-clock
  name: 暫停或恢復看板時間
  roles: [r-board-owner]
  crud: {board: U}
  pre:
    p1: "我是該 `board` 的 Owner"
  post:
    - "暫停時，`board.clock-status` 切換為 PAUSED，`board.clock-time` 停在暫停當下的時間"
    - "恢復時，`board.clock-status` 切換為 REALTIME，`board.clock-time` 從暫停時的時間繼續隨系統時間前進"
    - "該操作被記錄為 `board` 的一筆活動紀錄，說明看板時間已暫停或已恢復，包含操作人與操作時間"
  fail: {}
  emits: []
  requires: []
  calls-sync: []
```

```gherkin
Feature: 看板時間管理
  身為 看板使用者
  我想要調整、暫停或恢復看板的時鐘
  以便模擬不同時間點的操作，觀察圖表與統計如何變化

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And 看板時間目前為 2026-09-12 11:00:00

  @uc-guard-clock-monotonicity
  # Related aggregate:
  #   board: read, write
  Scenario: 把看板時間調整到未來後建立卡片，事件時間應為調整後的時間
    When 我將看板時間調整為 2026-09-12 13:00:00
    And 我建立一張卡片 "A"
    Then 卡片 "A" 的建立時間應該是 2026-09-12 13:00:00

  @uc-adjust-board-clock
  # Related aggregate:
  #   board: read, write
  Scenario: 看板時間可以往回調整
    Given 看板中最後一筆事件發生於 2026-09-12 13:00:00
    When 我將看板時間調整為 2026-09-12 12:00:00
    Then 看板時間應該顯示 2026-09-12 12:00:00

  @uc-guard-clock-monotonicity @fail-p1
  # Related aggregate:
  #   board: read
  Scenario: 看板時間早於最後一筆事件時，不可建立新事件
    Given 看板中最後一筆事件發生於 2026-09-12 13:00:00
    And 看板時間目前為 2026-09-12 12:00:00
    When 我嘗試建立一張卡片 "B"
    Then 系統應該顯示錯誤訊息 "看板時間早於最後一筆事件（13:00），無法建立新事件"
    And 不應該建立新的卡片

  @uc-pause-resume-board-clock
  # Related aggregate:
  #   board: write
  Scenario: 暫停看板時間
    When 我暫停看板時間
    And 我等待 10 秒
    Then 看板時間應該仍顯示 2026-09-12 11:00:00

  @uc-pause-resume-board-clock
  # Related aggregate:
  #   board: write
  Scenario: 恢復看板時間
    Given 看板時間目前為暫停狀態，暫停時的時間為 2026-09-12 11:00:00
    When 我恢復看板時間
    Then 看板時間應該從 2026-09-12 11:00:00 繼續隨系統時間前進

  @uc-adjust-board-clock @fail-p1
  # Related aggregate:
  #   board: read
  Scenario: 非 Owner 嘗試調整看板時間
    Given 我不是該 Board 的 Owner
    When 我嘗試將看板時間調整為 2026-09-12 13:00:00
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以調整看板時間"
    And 看板時間應該維持不變

  @uc-adjust-board-clock
  # Related aggregate:
  #   board: write
  Scenario: 調整看板時間應記錄一筆活動紀錄
    Given 我是該 Board 的 Owner
    When 我將看板時間調整為 2026-09-12 13:00:00
    Then 應該新增一筆活動紀錄，說明看板時間被調整為 2026-09-12 13:00:00

  @uc-pause-resume-board-clock
  # Related aggregate:
  #   board: write
  Scenario: 暫停或恢復看板時間應記錄一筆活動紀錄
    Given 我是該 Board 的 Owner
    When 我暫停看板時間
    Then 應該新增一筆活動紀錄，說明看板時間已暫停
    When 我恢復看板時間
    Then 應該新增一筆活動紀錄，說明看板時間已恢復
```

---

## 決議紀錄

- 2026-09-13：Open Question「調整／暫停／恢復看板時間是否需要記錄活動紀錄、是否限 Owner」定案採用草稿預設值：僅 Owner 可操作，且每次調整／暫停／恢復都記錄一筆活動紀錄。依據：與 CLAUDE.md「只有 Owner 能改看板結構」既有慣例一致（權限檢查由呼叫端查「BoardMembership」後決定是否呼叫「Board」的方法，「kanban-core」本身不做權限判斷），且與 CR-001 已上線的「Board/Card 操作人活動紀錄」慣例一致。已補上對應 Scenario 與驗收標準（見上）。
- 2026-09-13：Open Question「PAUSED 模式下多個事件取得相同「occurredAt」時的排序」定案為：沿用現有 event store 實作方式（「Board」/「Card」的「activityLog」／「stageTransitions」皆為「ArrayList」，只會 append、不會重新排序，插入順序本身即是決定性的次序），不另外新增顯式「sequence」欄位。依據：最小驚訝、YAGNI——現有實作已能提供決定性排序，未來若查詢端（如 F03「CardTimeline」投影）需要依「occurredAt」重新排序時，才需要在該處另外設計穩定排序（stable sort，保留原始插入順序作為次要鍵），不需要現在就在領域模型加欄位。CR-004 開發階段不需為此另外補欄位。

## 待釐清 / 未來擴充（Open Questions）

- 多人同時開啟同一個 Board 時，時鐘是 Board 共用狀態，一人調整後其他人畫面是否即時反映（推播或輪詢），依現有「即時同步」Open Question（見 F01）尚未決定，本文件不重複展開。
- User 建立、BoardMembership（邀請／角色變更）事件是否也需要一個「系統時鐘」的模擬能力：已確認本次範圍不含，維持系統時間，若未來有需求另開規格。
- OQ-08：`uc-guard-clock-monotonicity` 的建模歸屬（見 `.dev/loops/spec-migration-loop/spec-migration-open-questions.md`）。
