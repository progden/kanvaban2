# 看板標準圖表使用案例（BDD）

本文件涵蓋看板的標準分析圖表：

- Cycle Time／Lead Time 分析（散佈圖 + 統計摘要）
- WIP 與 Aging WIP（進行中卡片年齡）監控
- Throughput（單位時間完成數）與 CFD（累積流量圖）
- 逾期／即將到期提醒

核心只回傳計算後的資料（既有決策），前端負責畫圖。所有圖表都以 `asOf`（= Board Clock 目前時間，見 [`spec-board-clock.md`](../F04-board-clock/spec-board-clock.md)）為基準，不使用瀏覽器時間。所有圖表都依賴 Stage 是否已設定 `Start`／`Done` 角色（見 CR-003，`spec-kanban-basic.md`「Stage 管理」Feature）。

狀態：開發中

## 名詞定義

### 實體
| ID | 名詞 | 所屬 Aggregate | 說明 |
|---|---|---|---|

### 欄位
| ID | 型別／格式 | 限制 | 說明 |
|---|---|---|---|

### 關係
| 來源 | 目標 | min | max | 說明 |
|---|---|---|---|---|

### 其他名詞
| 名詞 | 說明 |
|------|------|
| Lead Time | 卡片從建立到完成（進入 Done 角色的 Stage）所經過的時間 |
| Cycle Time | 卡片從第一次進入 Start 角色的 Stage 到完成所經過的時間 |
| WIP（Work In Progress） | 目前不在 Done 角色 Stage 的卡片數量，依 Stage 分組統計 |
| Aging | 目前在 Start 與 Done 之間、尚未完成的卡片，從進入 Start 到現在（`asOf`）已經過的時間 |
| Throughput | 單位時間（日／週）內完成（進入 Done）的卡片數量 |
| CFD（Cumulative Flow Diagram，累積流量圖） | 每天各 Stage 累積卡片數量隨時間變化的圖 |
| asOf | 圖表計算基準時間，取自 Board Clock 目前時間 |

## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|

## Aggregate 標記說明

每個 Scenario 上方以 Gherkin 註解標記會存取哪些 Aggregate 以及存取方式（`read` / `write`），格式與判定原則同 `spec-kanban-basic.md`。本文件所有 Scenario 都只讀取，不寫入：

- `board`：讀取 Board 的 Stage 設定（含角色）與事件歷史，用於重播出卡片時間軸。
- `card`：讀取卡片目前狀態（是否已刪除、目前所在 Stage）。

## 變更紀錄（Change Log）

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-13 | F03 | 定案 | 定案「待釐清 / 未來擴充」段落之 Open Question（詳見該段落決議說明），本規格尚未進入開發，尚無程式碼變更 |
| 2026-09-13 | F03 | 開發完成 | `kanban-spring` 完成四個 Feature 的查詢邏輯：Cycle Time／Lead Time（`query.timeline`）、WIP／Aging WIP（`query.wip`）、Throughput／CFD（`query.throughput`）、逾期／即將到期提醒（`query.duedate`），均以 `CardTimelineProjector` 重播出的 `CardTimeline` 為共同資料來源；JUnit5+AssertJ 單元測試逐條對應本檔 Scenario，`./mvnw verify` 全綠，詳見 `design.md`「實作狀態」段落 |

---

## Feature: Cycle Time 與 Lead Time 分析

```gherkin
Feature: Cycle Time 與 Lead Time 分析
  身為 看板使用者
  我想要檢視卡片從開始到完成花費的時間
  以便評估團隊的交付速度與承諾交期

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And Stage "進行中" 已設定角色為 Start，Stage "完成" 已設定角色為 Done

  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視已完成卡片的 Cycle Time 與 Lead Time
    Given 卡片 "A" 於 2026-09-01 建立、2026-09-02 進入 Start、2026-09-05 進入 Done
    When 我開啟 Cycle Time / Lead Time 圖表
    Then 卡片 "A" 的 Lead Time 應該顯示為 4 天
    And 卡片 "A" 的 Cycle Time 應該顯示為 3 天

  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 沒有經過 Start 就完成的卡片，Cycle Time 不計入統計
    Given 卡片 "B" 於 2026-09-01 建立、未曾進入 Start 角色的 Stage、2026-09-03 進入 Done
    When 我開啟 Cycle Time / Lead Time 圖表
    Then 卡片清單中應該包含卡片 "B"，其 Cycle Time 顯示為「無」
    And 統計摘要的 Cycle Time 平均值與百分位計算應該排除卡片 "B"
    And 統計摘要應該顯示「排除計算的卡片數」為 1

  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 卡片離開 Done 後再次完成，只計最後一次完成時間
    Given 卡片 "C" 於 2026-09-05 首次進入 Done，之後於 2026-09-06 被移出 Done，並於 2026-09-08 再次進入 Done
    When 我開啟 Cycle Time / Lead Time 圖表
    Then 卡片 "C" 的完成時間應該顯示為 2026-09-08
```

---

## Feature: WIP 與 Aging WIP 監控

```gherkin
Feature: WIP 與 Aging WIP 監控
  身為 看板使用者
  我想要檢視各 Stage 目前的卡片數量，以及進行中卡片已經停留多久
  以便及早發現流程卡住的地方

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And Stage "進行中" 已設定角色為 Start，Stage "完成" 已設定角色為 Done

  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視各 Stage 目前的卡片數量
    Given Stage "待辦" 有 3 張卡片，Stage "進行中" 有 2 張卡片，Stage "完成" 有 5 張卡片
    When 我開啟 WIP 圖表
    Then 應該顯示 Stage "待辦" 卡片數 3、"進行中" 卡片數 2、"完成" 卡片數 5

  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視進行中卡片的年齡
    Given 卡片 "D" 於 2026-09-01 進入 Start 角色的 Stage，目前仍在該 Stage 未進入 Done
    And 看板時間目前為 2026-09-12
    When 我開啟 Aging WIP 圖表
    Then 卡片 "D" 的年齡應該顯示為 11 天
```

---

## Feature: Throughput 與累積流量圖

```gherkin
Feature: Throughput 與累積流量圖
  身為 看板使用者
  我想要檢視單位時間完成的卡片數量與各 Stage 卡片數量隨時間的變化
  以便掌握團隊的產出趨勢與流程瓶頸

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And Stage "進行中" 已設定角色為 Start，Stage "完成" 已設定角色為 Done

  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視每日完成卡片數量
    Given 2026-09-10 有 2 張卡片進入 Done，2026-09-11 有 1 張卡片進入 Done
    When 我開啟 Throughput 圖表，並選擇以「日」為單位
    Then 2026-09-10 的完成數應該顯示為 2
    And 2026-09-11 的完成數應該顯示為 1

  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視累積流量圖
    When 我開啟 CFD 圖表
    Then 應該顯示每一天、每個 Stage 的累積卡片數量
```

---

## Feature: 截止日期提醒

```gherkin
Feature: 截止日期提醒
  身為 看板使用者
  我想要檢視已逾期或即將到期的卡片
  以便優先處理有時間壓力的工作項目

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And 看板時間目前為 2026-09-12

  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視已逾期的卡片
    Given 卡片 "E" 截止日期為 2026-09-10，尚未完成
    When 我開啟逾期提醒圖表
    Then 卡片 "E" 應該出現在「已逾期」清單中

  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視即將到期的卡片
    Given 卡片 "F" 截止日期為 2026-09-14，尚未完成
    And 即將到期的門檻設定為 3 天
    When 我開啟逾期提醒圖表
    Then 卡片 "F" 應該出現在「即將到期」清單中
```

---

## 待釐清 / 未來擴充（Open Questions，已定案）

- Flow Efficiency（工作中時間 ÷ 總時間）需要 Stage 標記「等待／工作中」，F01 目前沒有此標記，**本次不納入範圍**（維持既有結論）。
- Blocked（卡片被阻塞）時間統計，spec 完全沒有對應概念，**本次不納入範圍**（維持既有結論）。
- CFD 每日快照是否需要另外物化儲存（避免每次查詢都重播全部事件），屬於效能實作細節，**待 `kanban-spring` 實作時依實際資料量決定**（維持既有結論，非驗收標準的一部分）。
- 「即將到期」的門檻天數是否可由使用者調整、或固定值：**定案為由使用者於查詢時傳入參數，非固定值**（2026-09-13 決議）。理由：規格文字本身已寫「即將到期的門檻設定為 3 天」而非寫死的常數，且與 F03 其餘圖表一致採「核心只回傳計算後的資料，前端負責畫圖／互動」的既有原則對齊，查詢參數化不影響任何既有驗收標準；依 workplan「決策規則 2」，此為文件已有明確傾向的 Open Question，直接採用該預設值定案。
