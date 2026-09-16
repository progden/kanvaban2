# 看板基本使用案例（BDD）

本文件以 BDD（Behavior-Driven Development）的 Gherkin 語法，描述看板（Kanban）系統的核心使用案例，涵蓋：

- Swimlane（泳道）管理
- Stage（階段 / 欄位）管理
- Card（卡片）編輯

狀態：開發中

## 名詞定義

### 實體
| ID | 名詞 | 所屬 Aggregate | 說明 |
|---|---|---|---|
| board | Board（看板） | board（root） | 整體工作區，包含多個 Swimlane 與 Stage |
| swimlane | Swimlane（泳道） | board | 橫向分組，用於區分不同類別的工作（例如：專案、優先度、負責團隊） |
| stage | Stage（階段） | board | 縱向欄位，代表工作流程狀態（例如：待辦、進行中、完成） |
| card | Card（卡片） | card（root） | 代表一項工作項目，隸屬於某個 Swimlane 與 Stage 的交會格 |

### 欄位
| ID | 型別／格式 | 限制 | 說明 |
|---|---|---|---|
| swimlane.name | string | 非空 | 泳道名稱 |
| stage.name | string | | 階段名稱 |
| stage.role | enum(NONE, START, DONE) | 同一 `board` 中 START、DONE 各至多一個 | Stage 在流程中的意義，預設 NONE |
| card.title | string | 非空 | 卡片標題 |
| card.description | string | | 卡片描述 |
| card.due-date | date | | 截止日期 |
| card.labels | string（多值） | | 標籤 |
| card.swimlane | ref swimlane | 建立時必填 | 卡片所屬泳道 |
| card.stage | ref stage | 建立時必填 | 卡片所屬階段 |

### 關係
| 來源 | 目標 | min | max | 說明 |
|---|---|---|---|---|
| board | swimlane | 1 | n | 看板至少保留一個 Swimlane |
| board | stage | 1 | n | 看板至少保留一個 Stage |
| swimlane | card | 0 | n | 一個泳道可以有多張卡片 |
| stage | card | 0 | n | 一個階段可以有多張卡片 |

### 其他名詞
| 名詞 | 說明 |
|---|---|
| Stage 角色（role） | 標記某個 Stage 在流程中的意義：NONE（無特殊意義，預設）、START（進入此欄視為開始計時）、DONE（進入此欄視為完成）。同一個 Board 中 START、DONE 各至多一個 |
| 操作時間（occurredAt） | 本文件所有 Scenario 中「事件發生時間」、「活動紀錄時間」等描述，一律代表該 Board 的 Board Clock 當下時間（「BoardClock.now()」），而非系統實際時間；詳見 `.dev/F04-board-clock/spec-board-clock.md` |

## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|
| r-user | 看板使用者 | 可操作看板（Swimlane、Stage）與卡片的一般使用者 |

## Aggregate 標記說明

`board`（含 `swimlane`、`stage`）與 `card` 是兩個獨立的 Aggregate。每個 Scenario 前方以 Gherkin 註解標記該情境會存取哪個 Aggregate、以及是「讀取」還是「寫入」，格式如下：

```gherkin
# Related aggregate:
#   board: read, write
#   card: read
```

- 只列出實際會用到的 aggregate；沒用到的可以省略整行（例如純粹只操作 Card 的情境就不會有 `board:` 那行）。
- `read` 代表需要先查詢既有資料才能決定如何寫入或驗證（例如重新命名要先找到對象、拖曳排序要先讀現有順序、刪除前要確認是否為最後一個、或是否還有卡片存在）；`write` 代表會真正新增/修改/刪除該 aggregate 的資料。

## 變更紀錄（Change Log）

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-12 | CR-001 | 變更 | Swimlane / Stage / Card 會改變狀態的情境，補上操作人記錄，供活動紀錄使用（見 `.dev/F02-user-membership/spec-user-membership.md`） |
| 2026-09-12 | CR-002 | 變更 | 「編輯卡片詳細內容」移除負責人欄位，負責人改為多選、參照看板成員，改由 F02「卡片負責人指派」情境處理 |
| 2026-09-13 | CR-003 | 新增 | Stage 新增角色標記（Start / Done），供 F03 標準圖表計算 Cycle/Lead Time 使用 |
| 2026-09-13 | CR-004 | 變更 | 名詞定義補上「操作時間（occurredAt）」，明訂 Board/Card 事件時間一律取自 Board Clock（見 F04），既有 Scenario 文字不需修改，故不掛 Scenario 層級 tag |
| 2026-09-13 | CR-004 | 開發完成 | `kanban-core` 的 `Board`／`Card` 事件時間已全面改用 Board Clock（`Board.now()`／`Board.newEventTime()`），不再直接呼叫 `Instant.now()`；CR-004 狀態改「處理完成」 |

---

## Feature: Swimlane 管理

### Use Case 定義
```usecase
- id: uc-add-swimlane
  name: 新增 Swimlane
  roles: [r-user]
  crud: {board: RU, swimlane: C}
  pre:
    p1: "`swimlane.name` 非空"
  post:
    - "新的 `swimlane` 出現在 `board` 最下方"
    - "該操作被記錄為 `board` 的一筆活動紀錄，包含操作人與操作時間"
  fail:
    p1: "拒絕，不建立新的 `swimlane`"
  emits: []
  requires: []
  calls-sync: []

- id: uc-rename-swimlane
  name: 重新命名 Swimlane
  roles: [r-user]
  crud: {board: U, swimlane: U}
  pre:
    p1: "指定的 `swimlane` 存在"
  post:
    - "`swimlane.name` 更新為新名稱"
    - "該操作被記錄為 `board` 的一筆活動紀錄，包含操作人與操作時間"
  fail: {}
  emits: []
  requires: []
  calls-sync: []

- id: uc-reorder-swimlane
  name: 拖曳調整 Swimlane 順序
  roles: [r-user]
  crud: {board: U, swimlane: U}
  pre:
    p1: "`board` 中依序存在多個 `swimlane`"
  post:
    - "`swimlane` 的順序依拖曳結果更新"
    - "該操作被記錄為 `board` 的一筆活動紀錄，包含操作人與操作時間"
  fail: {}
  emits: []
  requires: []
  calls-sync: []

- id: uc-delete-swimlane
  name: 刪除 Swimlane
  roles: [r-user]
  crud: {board: U, swimlane: D, card: D}
  pre:
    p1: "`board` 中的 `swimlane` 數量大於 1"
  post:
    - "該 `swimlane` 不再存在於 `board`"
    - "若該 `swimlane` 內有 `card`，一併被刪除"
    - "該操作被記錄為 `board` 的一筆活動紀錄，包含操作人與操作時間"
  fail:
    p1: "拒絕，該 `swimlane` 不被刪除"
  emits: []
  requires: []
  calls-sync: []
```

```gherkin
Feature: Swimlane 管理
  身為 看板使用者
  我想要新增、命名、排序與刪除 Swimlane
  以便依照類別（例如團隊、優先度）將工作項目分組呈現

  Background:
    Given 我已登入系統
    And 我已開啟一個名為 "產品開發看板" 的看板

  @CR-001 @uc-add-swimlane
  # Related aggregate:
  #   board: read, write
  #   swimlane: write
  Scenario: 新增一個 Swimlane
    Given 看板目前有 1 個 Swimlane "預設泳道"
    When 我點擊「新增 Swimlane」按鈕
    And 我輸入名稱 "緊急項目"
    And 我確認新增
    Then 看板應該顯示 2 個 Swimlane
    And 新的 Swimlane "緊急項目" 應該出現在看板最下方
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @uc-add-swimlane @fail-p1
  # Related aggregate:
  #   board: read
  Scenario: Swimlane 名稱不可為空
    Given 我正在新增一個 Swimlane
    When 我沒有輸入任何名稱就確認新增
    Then 系統應該顯示錯誤訊息 "Swimlane 名稱不可為空"
    And 不應該建立新的 Swimlane

  @CR-001 @uc-rename-swimlane
  # Related aggregate:
  #   board: read, write
  #   swimlane: read, write
  Scenario: 重新命名 Swimlane
    Given 看板中存在一個 Swimlane "緊急項目"
    When 我將該 Swimlane 重新命名為 "本週優先"
    Then 該 Swimlane 的名稱應該更新為 "本週優先"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-reorder-swimlane
  # Related aggregate:
  #   board: read, write
  #   swimlane: read, write
  Scenario: 拖曳調整 Swimlane 順序
    Given 看板中依序存在 Swimlane "A"、"B"、"C"
    When 我將 Swimlane "C" 拖曳到 "A" 的上方
    Then Swimlane 的順序應該變為 "C"、"A"、"B"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-delete-swimlane
  # Related aggregate:
  #   board: read, write
  #   swimlane: read, write
  #   card: read, write
  Scenario: 刪除空的 Swimlane
    Given 看板中存在一個沒有任何卡片的 Swimlane "測試泳道"
    When 我刪除該 Swimlane
    Then 看板不應該再顯示 "測試泳道"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-delete-swimlane
  # Related aggregate:
  #   board: read, write
  #   swimlane: read, write
  #   card: read, write
  Scenario: 刪除包含卡片的 Swimlane 需要確認
    Given 看板中存在一個 Swimlane "本週優先"，其中包含 3 張卡片
    When 我嘗試刪除該 Swimlane
    Then 系統應該顯示確認訊息，告知該 Swimlane 內有 3 張卡片將一併被刪除
    When 我確認刪除
    Then 該 Swimlane 與其所有卡片都應該被移除
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @uc-delete-swimlane @fail-p1
  # Related aggregate:
  #   swimlane: read
  Scenario: 看板至少保留一個 Swimlane
    Given 看板中只剩下 1 個 Swimlane "預設泳道"
    When 我嘗試刪除該 Swimlane
    Then 系統應該顯示錯誤訊息 "看板至少需要保留一個 Swimlane"
    And 該 Swimlane 不應該被刪除
```

---

## Feature: Stage（階段）管理

### Use Case 定義
```usecase
- id: uc-add-stage
  name: 新增 Stage
  roles: [r-user]
  crud: {board: RU, stage: C}
  pre:
    p1: "`board` 存在"
  post:
    - "新的 `stage` 依指定位置插入 `board` 的 Stage 序列，未指定位置時加到最後"
    - "該操作被記錄為 `board` 的一筆活動紀錄，包含操作人與操作時間"
  fail: {}
  emits: []
  requires: []
  calls-sync: []

- id: uc-rename-stage
  name: 重新命名 Stage
  roles: [r-user]
  crud: {board: U, stage: U}
  pre:
    p1: "指定的 `stage` 存在"
  post:
    - "`stage.name` 更新為新名稱"
    - "該操作被記錄為 `board` 的一筆活動紀錄，包含操作人與操作時間"
  fail: {}
  emits: []
  requires: []
  calls-sync: []

- id: uc-reorder-stage
  name: 拖曳調整 Stage 順序
  roles: [r-user]
  crud: {board: U, stage: U}
  pre:
    p1: "`board` 中依序存在多個 `stage`"
  post:
    - "`stage` 的順序依拖曳結果更新"
    - "該操作被記錄為 `board` 的一筆活動紀錄，包含操作人與操作時間"
  fail: {}
  emits: []
  requires: []
  calls-sync: []

- id: uc-delete-stage
  name: 刪除 Stage
  roles: [r-user]
  crud: {board: U, stage: D, card: U}
  pre:
    p1: "`board` 中的 `stage` 數量大於 1"
  post:
    - "該 `stage` 不再存在於 `board`"
    - "若該 `stage` 內有 `card`，`card.stage` 更新為使用者選擇的目的 `stage`"
    - "該操作被記錄為 `board` 的一筆活動紀錄，包含操作人與操作時間"
  fail:
    p1: "拒絕，該 `stage` 不被刪除"
  emits: []
  requires: []
  calls-sync: []

- id: uc-set-stage-role
  name: 設定 Stage 角色
  roles: [r-user]
  crud: {board: U, stage: U}
  pre:
    p1: "指定的 `stage` 存在"
  post:
    - "`stage.role` 更新為指定角色"
    - "若 `board` 中原本已有其他 `stage` 的角色與新設定的角色相同，該 `stage` 的角色自動變回 NONE"
    - "該操作被記錄為 `board` 的一筆活動紀錄，包含操作人與操作時間"
  fail: {}
  emits: []
  requires: []
  calls-sync: []
```

```gherkin
Feature: Stage（階段）管理
  身為 看板使用者
  我想要新增、命名、排序與刪除 Stage
  以便定義工作項目在流程中會經過的各個狀態

  Background:
    Given 我已登入系統
    And 我已開啟一個名為 "產品開發看板" 的看板
    And 看板目前的 Stage 依序為 "待辦"、"進行中"、"完成"

  @CR-001 @uc-add-stage
  # Related aggregate:
  #   board: read, write
  #   stage: write
  Scenario: 新增一個 Stage
    When 我點擊「新增 Stage」按鈕
    And 我輸入名稱 "驗收中"
    And 我選擇插入在 "進行中" 與 "完成" 之間
    Then Stage 順序應該變為 "待辦"、"進行中"、"驗收中"、"完成"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-add-stage
  # Related aggregate:
  #   board: read, write
  #   stage: write
  Scenario: 新增 Stage 時未指定插入位置，預設加到最後面
    When 我點擊「新增 Stage」按鈕
    And 我輸入名稱 "驗收中"
    And 我沒有指定插入位置就確認新增
    Then Stage 順序應該變為 "待辦"、"進行中"、"完成"、"驗收中"
    And 新的 Stage "驗收中" 應該出現在最後一個欄位
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-rename-stage
  # Related aggregate:
  #   board: read, write
  #   stage: read, write
  Scenario: 重新命名 Stage
    When 我將 Stage "待辦" 重新命名為 "規劃中"
    Then Stage 名稱應該更新為 "規劃中"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-reorder-stage
  # Related aggregate:
  #   board: read, write
  #   stage: read, write
  Scenario: 拖曳調整 Stage 順序
    When 我將 Stage "完成" 拖曳到 "待辦" 的左側
    Then Stage 順序應該變為 "完成"、"待辦"、"進行中"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-delete-stage
  # Related aggregate:
  #   board: read, write
  #   stage: read, write
  #   card: read, write
  Scenario: 刪除空的 Stage
    Given Stage "驗收中" 目前沒有任何卡片
    When 我刪除 Stage "驗收中"
    Then 看板不應該再顯示 "驗收中"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-delete-stage
  # Related aggregate:
  #   board: read, write
  #   stage: read, write
  #   card: read, write
  Scenario: 刪除包含卡片的 Stage 需要先轉移卡片
    Given Stage "進行中" 中包含 2 張卡片
    When 我嘗試刪除 Stage "進行中"
    Then 系統應該提示我選擇一個目的 Stage 來接收這 2 張卡片
    When 我選擇目的 Stage 為 "待辦"
    Then 這 2 張卡片應該被移動到 "待辦"
    And Stage "進行中" 應該被刪除
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @uc-delete-stage @fail-p1
  # Related aggregate:
  #   stage: read
  Scenario: 看板至少保留一個 Stage
    Given 看板中只剩下 1 個 Stage "待辦"
    When 我嘗試刪除該 Stage
    Then 系統應該顯示錯誤訊息 "看板至少需要保留一個 Stage"
    And 該 Stage 不應該被刪除

  @added @CR-003 @uc-set-stage-role
  # Related aggregate:
  #   board: read, write
  #   stage: read, write
  Scenario: 設定 Stage 角色
    Given 看板目前所有 Stage 的角色皆為 NONE
    When 我將 Stage "待辦" 的角色設定為 START
    Then Stage "待辦" 的角色應該變為 START
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間
    When 我將 Stage "進行中" 的角色設定為 START
    Then Stage "進行中" 的角色應該變為 START
    And Stage "待辦" 的角色應該自動變回 NONE
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間
```

---

## Feature: Card（卡片）編輯

```gherkin
Feature: Card（卡片）編輯
  身為 看板使用者
  我想要建立、編輯、移動與刪除卡片
  以便追蹤每一項工作的詳細內容與進度

  Background:
    Given 我已登入系統
    And 我已開啟一個名為 "產品開發看板" 的看板
    And 看板中存在 Swimlane "本週優先" 與 Stage "待辦"、"進行中"、"完成"

  @CR-001
  # Related aggregate:
  #   board: read
  #   card: write
  Scenario: 在指定 Swimlane 與 Stage 建立新卡片
    When 我在 Swimlane "本週優先" 的 Stage "待辦" 欄位點擊「新增卡片」
    And 我輸入標題 "設計登入頁面"
    And 我確認新增
    Then 該卡片應該出現在 Swimlane "本週優先" 與 Stage "待辦" 的交會格中
    And 卡片標題應該顯示為 "設計登入頁面"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  # Related aggregate:
  #   card: write
  Scenario: 卡片標題不可為空
    When 我嘗試新增一張標題為空的卡片
    Then 系統應該顯示錯誤訊息 "卡片標題不可為空"
    And 不應該建立新的卡片

  @CR-001 @CR-002
  # Related aggregate:
  #   card: read, write
  Scenario: 編輯卡片詳細內容（不含負責人）
    Given 存在一張卡片 "設計登入頁面"
    When 我開啟該卡片的詳細編輯畫面
    And 我填寫以下欄位：
      | 欄位     | 內容                     |
      | 描述     | 設計符合品牌風格的登入頁面 |
      | 截止日期 | 2026-09-20               |
      | 標籤     | UI, 前端                 |
    And 我儲存變更
    Then 卡片應該保存上述所有欄位的內容
    And 卡片縮圖應該顯示截止日期 "2026-09-20"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001
  # Related aggregate:
  #   board: read
  #   card: read, write
  Scenario: 在同一 Stage 內，卡片跨 Swimlane 移動
    Given 存在一張卡片 "設計登入頁面"，位於 Swimlane "本週優先" 與 Stage "待辦"
    And 看板中還有另一個 Swimlane "下週規劃"
    When 我將該卡片拖曳到 Swimlane "下週規劃" 的 Stage "待辦"
    Then 該卡片應該顯示於 Swimlane "下週規劃" 與 Stage "待辦" 的交會格中
    And 該卡片不應該再出現在 Swimlane "本週優先" 中
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001
  # Related aggregate:
  #   board: read
  #   card: read, write
  Scenario: 卡片跨 Stage 移動（更新工作狀態）
    Given 存在一張卡片 "設計登入頁面"，位於 Stage "待辦"
    When 我將該卡片拖曳到 Stage "進行中"
    Then 該卡片應該顯示於 Stage "進行中"
    And 卡片的狀態異動應該被記錄，包含操作人、異動時間與異動前後的 Stage

  # Related aggregate:
  #   card: read, write
  Scenario: 為卡片新增留言
    Given 存在一張卡片 "設計登入頁面"
    When 我在卡片中新增留言 "已完成初稿，請協助審閱"
    Then 該留言應該顯示在卡片的留言列表中
    And 留言應該記錄留言者與留言時間

  @CR-001
  # Related aggregate:
  #   card: read, write
  Scenario: 刪除卡片需要確認
    Given 存在一張卡片 "設計登入頁面"
    When 我點擊刪除該卡片
    Then 系統應該顯示確認訊息 "確定要刪除這張卡片嗎？此動作無法復原"
    When 我確認刪除
    Then 該卡片應該從看板中移除
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  # Related aggregate:
  #   card: read
  Scenario: 取消刪除卡片
    Given 存在一張卡片 "設計登入頁面"
    When 我點擊刪除該卡片
    And 我在確認訊息中選擇取消
    Then 該卡片應該仍然存在於看板中
```

---

## 待釐清 / 未來擴充（Open Questions）

- OQ-01：活動紀錄不另立 uc、不 emits 事件，改由各 uc 的 post 與 board 的 crud/Aggregate 註解表達（見 `.dev/loops/spec-migration-loop/spec-migration-open-questions.md`）
- Swimlane / Stage 是否需要支援「顏色標記」以利辨識？
- 卡片是否需要支援子任務（Checklist）或附件？
- 是否需要「已封存（Archived）」的卡片與泳道狀態，而非直接刪除？
- 是否需要多人協作時的即時同步（Real-time sync）情境？
- 是否需要 Stage 的 WIP 限制（進行中工作數量上限）？若需要，超出限制時應該「阻擋」還是僅「警告但允許」？
