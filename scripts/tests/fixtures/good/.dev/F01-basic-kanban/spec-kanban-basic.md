# 看板基本使用案例（BDD）

本文件是腳本測試用的範例規格，沿用 `spec-convention.md` 的 Swimlane／CR-023／CR-024 情境。

- Swimlane（泳道）管理

狀態：開發中

## 名詞定義

### 實體
| ID | 名詞 | 所屬 Aggregate | 說明 |
|---|---|---|---|
| board | Board（看板） | board（root） | 整體工作區，包含多個 Swimlane |
| swimlane | Swimlane（泳道） | board | 看板上的水平分區 |
| card | Card（卡片） | card（root） | 工作項目，隸屬於某個 Swimlane |

### 欄位
| ID | 型別／格式 | 限制 | 說明 |
|---|---|---|---|
| swimlane.name | string(50) | 非空、同看板內唯一 | 泳道名稱 |
| swimlane.color | enum(red, yellow, green, none) | 預設 none | 顏色標記 |
| card.swimlane | ref swimlane | 必填 | 所屬泳道 |

### 關係
| 來源 | 目標 | min | max | 說明 |
|---|---|---|---|---|
| board | swimlane | 1 | n | 一個看板至少一個泳道 |
| swimlane | card | 0 | n | 一個泳道有多張卡片 |

### 其他名詞
| 名詞 | 說明 |
|---|---|
| WIP 限制 | 單一 Stage 內允許的卡片數上限，規則名，不是實體 |

## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|
| r-board-admin | 看板管理者 | 可管理泳道與階段 |
| r-member | 看板成員 | 可操作卡片 |

## Aggregate 標記說明

每個 Scenario 前方以 Gherkin 註解標記會存取哪個實體、以及是讀取還是寫入：

```gherkin
# Related aggregate:
#   swimlane: read, write
#   card: read
```

## 變更紀錄（Change Log）

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-12 | CR-023 | 新增 | Swimlane 顏色標記（`uc-set-swimlane-color`、`swimlane.color`） |
| 2026-09-12 | CR-024 | 變更 | 刪除含卡片的 Swimlane 改為轉移卡片（`uc-delete-swimlane`） |

---

## Feature: Swimlane 管理

### Use Case 定義
```usecase
- id: uc-add-swimlane
  name: 新增 Swimlane
  roles: [r-board-admin]
  crud: {board: R, swimlane: C}
  pre:
    p1: "`swimlane.name` 非空"
  post:
    - "新的 `swimlane` 出現在 `board` 最下方"
  fail:
    p1: "拒絕，資料不變"
  emits: [ev-swimlane-added]
  requires: []
  calls-sync: []

- id: uc-delete-swimlane
  name: 刪除 Swimlane
  roles: [r-board-admin]
  crud: {swimlane: D, card: U}
  pre:
    p1: "`swimlane` 存在，且不是看板中唯一的 `swimlane`"
    p2: "若該泳道內有 `card`，`card.swimlane` 的目的泳道已指定"
  post:
    - "`swimlane` 不存在"
    - "原屬該泳道的 `card.swimlane` 更新為目的泳道"
  fail:
    p1: "拒絕，資料不變"
    p2: "拒絕，提示選擇目的 `swimlane`，資料不變"
  emits: [ev-swimlane-deleted]
  requires: []
  calls-sync: []

- id: uc-set-swimlane-color
  name: 設定 Swimlane 顏色標記
  roles: [r-board-admin, r-member]
  crud: {swimlane: U}
  pre:
    p1: "`swimlane` 存在"
  post:
    - "`swimlane.color` 更新為指定顏色"
  fail: {}
  emits: [ev-swimlane-color-set]
  requires: []
  calls-sync: []

- id: uc-log-swimlane-activity
  name: 記錄 Swimlane 活動
  roles: []
  crud: {board: U}
  pre:
    p1: "`board` 存在"
  post:
    - "`board` 的活動紀錄新增一筆"
  fail: {}
  emits: []
  requires: [ev-swimlane-added, ev-swimlane-deleted, ev-swimlane-color-set]
  calls-sync: []
```

```gherkin
Feature: Swimlane 管理
  身為 看板管理者
  我想要 新增、命名、排序與刪除 Swimlane
  以便 依照類別將工作項目分組呈現

  Background:
    Given 我已登入系統
    And 我已開啟一個名為 "產品開發看板" 的看板

  @uc-add-swimlane
  # Related aggregate:
  #   board: read
  #   swimlane: write
  Scenario: 新增一個 Swimlane
    Given 看板目前有 1 個 Swimlane "預設泳道"
    When 我點擊「新增 Swimlane」按鈕
    And 我輸入名稱 "緊急項目"
    And 我確認新增
    Then 看板應該顯示 2 個 Swimlane
    And 新的 Swimlane "緊急項目" 應該出現在看板最下方

  @uc-add-swimlane @fail-p1
  # Related aggregate:
  #   board: read
  Scenario: Swimlane 名稱不可為空
    Given 我正在新增一個 Swimlane
    When 我沒有輸入任何名稱就確認新增
    Then 系統應該顯示錯誤訊息 "Swimlane 名稱不可為空"
    And 不應該建立新的 Swimlane

  @uc-log-swimlane-activity
  # Related aggregate:
  #   board: read, write
  Scenario: Swimlane 異動會被記錄為活動紀錄
    Given 看板中存在一個 Swimlane "緊急項目"
    When 我重新命名該 Swimlane 為 "本週優先"
    Then 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @added @wip @CR-023 @uc-set-swimlane-color
  # Related aggregate:
  #   swimlane: read, write
  Scenario: 為 Swimlane 設定顏色標記
    Given 看板中存在一個 Swimlane "緊急項目"
    When 我為該 Swimlane 選擇顏色 "紅色"
    Then 該 Swimlane 的標題列應該顯示紅色標記

  @deprecated @CR-024 @uc-delete-swimlane
  # Related aggregate:
  #   swimlane: read, write
  #   card: read, write
  Scenario: 刪除包含卡片的 Swimlane 需要確認
    Given 看板中存在一個 Swimlane "本週優先"，其中包含 3 張卡片
    When 我嘗試刪除該 Swimlane
    Then 系統應該顯示確認訊息，告知該 Swimlane 內有 3 張卡片將一併被刪除
    When 我確認刪除
    Then 該 Swimlane 與其所有卡片都應該被移除

  @changed @wip @CR-024 @uc-delete-swimlane
  # Related aggregate:
  #   swimlane: read, write
  #   card: read, write
  Scenario: 刪除包含卡片的 Swimlane 需要先轉移卡片
    Given 看板中存在一個 Swimlane "本週優先"，其中包含 3 張卡片
    When 我嘗試刪除該 Swimlane
    Then 系統應該提示我選擇一個目的 Swimlane 來接收這 3 張卡片
    When 我選擇目的 Swimlane 為 "預設泳道"
    Then 這 3 張卡片應該被移動到 "預設泳道"
    And Swimlane "本週優先" 應該被刪除

  @added @wip @CR-024 @uc-delete-swimlane @fail-p2
  # Related aggregate:
  #   swimlane: read
  #   card: read
  Scenario: 未選擇目的 Swimlane 時不可刪除包含卡片的 Swimlane
    Given 看板中存在一個 Swimlane "本週優先"，其中包含 3 張卡片
    When 我嘗試刪除該 Swimlane
    And 我沒有選擇目的 Swimlane 就確認刪除
    Then 系統應該顯示錯誤訊息 "請選擇接收卡片的目的 Swimlane"
    And Swimlane "本週優先" 與其中的 3 張卡片都不應該被移除

  @uc-delete-swimlane @fail-p1
  # Related aggregate:
  #   swimlane: read
  Scenario: 看板至少保留一個 Swimlane
    Given 看板目前只有 1 個 Swimlane "預設泳道"
    When 我嘗試刪除該 Swimlane
    Then 系統應該顯示錯誤訊息 "看板至少需要保留一個 Swimlane"
    And 該 Swimlane 不應該被刪除
```

---

## 待釐清 / 未來擴充（Open Questions）
- 泳道是否需要「封存」而非刪除。
