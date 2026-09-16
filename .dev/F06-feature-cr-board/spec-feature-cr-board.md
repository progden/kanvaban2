# Feature／CR 追蹤表使用案例（BDD）

本文件涵蓋用看板自己追蹤自己開發進度的功能：Feature 與 CR 各是一張卡片，「未開發／開發中／已完成」直接對應卡片所在 Stage 的角色（CR-003：NONE／START／DONE）。資料來源是看板卡片上既有的「標籤」欄位（既有欄位，零 CR），不讀 `.dev/` 檔案、不另開 aggregate。

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
| Feature 卡 | 標籤格式為「^F\d{2}$」（例：「F01」）的卡片，代表一個功能模組 |
| CR 卡 | 標籤格式為「^CR-\d{3}$」（例：「CR-003」）的卡片，代表一張變更單 |
| affects 標籤 | 格式為「affects:F01」，只在 CR 卡上有意義，標示此 CR 影響哪個 Feature，可多個 |
| 狀態 | 卡片所在 Stage 的角色：「NONE」＝未開發、「START」＝開發中、「DONE」＝已完成 |
| orphan CR | 「affects」指到不存在 Feature 編號的 CR 卡 |

## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|

## Aggregate 標記說明

每個 Scenario 上方以 Gherkin 註解標記會存取哪些 Aggregate 以及存取方式（「read」／「write」），格式與判定原則同 `spec-kanban-basic.md`。本文件所有 Scenario 都只讀取：

- `board`：讀取 Stage 角色設定。
- `card`：讀取卡片標籤、標題、描述、建立時間、所在 Stage。

## 變更紀錄（Change Log）

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-13 |  | 開發完成 | （原票號 F06）開發完成：「kanban-spring」新增「query.featurecrboard.FeatureCrBoardCalculator」，解析卡片標籤（「^F\d{2}$」／「^CR-\d{3}$」／「affects:F\d{2}$」，皆不分大小寫）組出 Feature／CR 狀態、orphan CR 清單與格式錯誤警告，對應本文件五個 Scenario 測試皆綠。 |
| 2026-09-16 | CR-005 | 變更 | 規格格式遷移至 usecase 區塊（`uc-view-feature-cr-board`） |

---

## Feature: Feature／CR 追蹤表

### Use Case 定義
```usecase
- id: uc-view-feature-cr-board
  name: 檢視 Feature／CR 追蹤表
  roles: [r-user]
  crud: {board: R, card: R}
  pre: {}
  post:
    - "標籤格式為「^F\\d{2}$」的 `card` 視為 Feature 卡；其狀態依所在 Stage 的角色顯示：角色為 Done 顯示「已完成」、角色為 Start 顯示「開發中」"
    - "標籤格式為「^CR-\\d{3}$」的 `card` 視為 CR 卡；帶有「affects:F\\d{2}$」標籤時，顯示在對應 Feature 底下，並依所在 Stage 角色顯示狀態（例如角色為 Start 顯示「開發中」）"
    - "CR 卡的「affects」標籤指到不存在的 Feature 編號時，該 `card` 列入 orphan CR 清單"
    - "`card` 同時帶有兩個 Feature 標籤時，顯示一筆警告訊息，且不影響其他 `card` 的 Feature／CR 統計"
    - "`card` 的 Feature／CR 標籤比對不分大小寫，小寫標籤視同大寫標籤處理"
  fail: {}
  emits: []
  requires: []
  calls-sync: []
```

```gherkin
Feature: Feature／CR 追蹤表
  身為 看板使用者
  我想要用看板卡片檢視每個 Feature 與 CR 目前的開發狀態
  以便掌握整個專案的進度，不需要另外查閱文件

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And Stage "進行中" 已設定角色為 Start，Stage "完成" 已設定角色為 Done

  @uc-view-feature-cr-board
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視 Feature 的開發狀態
    Given 卡片 "basic-kanban" 標籤為 "F01"，目前在角色為 Done 的 Stage
    When 我開啟 Feature／CR 追蹤表
    Then Feature "F01" 的狀態應該顯示為「已完成」

  @uc-view-feature-cr-board
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視 CR 影響哪個 Feature 以及其狀態
    Given 卡片 "看板時間" 標籤為 "CR-004"，並帶有 "affects:F01" 標籤，目前在角色為 Start 的 Stage
    When 我開啟 Feature／CR 追蹤表
    Then Feature "F01" 底下應該顯示一筆狀態為「開發中」的 CR "CR-004"

  @uc-view-feature-cr-board
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: CR 指到不存在的 Feature 時列為 orphan
    Given 卡片 "某 CR" 標籤為 "CR-099"，並帶有 "affects:F99" 標籤
    When 我開啟 Feature／CR 追蹤表
    Then "CR-099" 應該出現在 orphan CR 清單中

  @uc-view-feature-cr-board
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 標籤格式不合時列為警告，不影響其他卡片顯示
    Given 卡片 "格式錯誤的卡" 同時帶有 "F01" 與 "F02" 兩個 Feature 標籤
    When 我開啟 Feature／CR 追蹤表
    Then 應該顯示一筆警告訊息，說明該卡片有兩個 Feature 標籤
    And 其他卡片的 Feature／CR 統計不應該受影響

  @uc-view-feature-cr-board
  # Related aggregate:
  #   card: read
  Scenario: Feature／CR 標籤不分大小寫
    Given 卡片 "basic-kanban" 標籤為 "f01"
    When 我開啟 Feature／CR 追蹤表
    Then 應該視同標籤為 "F01" 顯示
```

---

## 待釐清 / 未來擴充（Open Questions）

- 無（標籤命名慣例、大小寫規則、寬容處理原則已於草稿定案）。
