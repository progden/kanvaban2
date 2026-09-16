# 人員 Workload 表使用案例（BDD）

本文件涵蓋看板成員的工作量檢視：

- 依成員統計目前手上有幾張進行中的卡片
- 未指派負責人的卡片統計
- 拖曳指派負責人（追加單一負責人）

依賴 F02（`spec-user-membership.md`）的 `board-membership`、卡片多選負責人（`card.assignees`，CR-002），以及 CR-003 的 Stage 角色（用於判斷卡片是否已完成）。

狀態：定稿

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
| Active Card（進行中卡片） | 未刪除、且目前所在 Stage 的角色不是 Done 的卡片 |
| Workload | 某成員名下 Active Card 的數量 |
| 未指派（Unassigned） | `card.assignees` 為空的 Active Card |

## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|

## Aggregate 標記說明

每個 Scenario 上方以 Gherkin 註解標記會存取哪些 Aggregate 以及存取方式（「read」／「write」），格式與判定原則同 `spec-kanban-basic.md`。本文件用到的 Aggregate：

- `board`：讀取 Stage 角色設定與卡片分佈。
- `board-membership`：讀取看板成員清單。
- `card`：讀取／寫入卡片的 `card.assignees`。

## 變更紀錄（Change Log）

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-13 |  | 變更 | （原票號 F05）「拖曳成員頭像到卡片上，追加該成員為負責人」Scenario 移除「@wip」（對應的「拖曳追加單一負責人」情境已在 `spec-user-membership.md` 的「卡片負責人指派」Feature 補上）；新增「拖曳已經是負責人的成員頭像到卡片上，不重複新增」Scenario，定案 Open Question「拖曳到已是負責人的卡片」為靜默忽略、不提示、不重複新增 |
| 2026-09-13 |  | 開發完成 | （原票號 F05）「kanban-spring」新增「io.progden.kanban.query.workload.WorkloadCalculator」，依 Active Card（非 Done Stage）的「assigneeIds」分組統計工作量與未指派數量；追加負責人 use case 已在 `spec-user-membership.md` 開發完成。F05 四個查詢 Scenario 與兩個拖曳追加 Scenario 皆已完成，階段 5 結束 |
| 2026-09-16 | CR-005 | 變更 | 規格格式遷移至 usecase 區塊（`uc-view-workload`、`uc-drag-assign-card-owner`） |

---

## Feature: 人員工作量檢視

### Use Case 定義
```usecase
- id: uc-view-workload
  name: 檢視人員工作量表
  roles: [r-user]
  crud: {board: R, board-membership: R, card: R}
  pre: {}
  post:
    - "依 `board-membership` 成員分組統計各成員擔任負責人的 Active Card 數量，作為該成員的 Workload"
    - "`card` 同時有多位負責人時，每位負責人的 Workload 各自包含該 `card`"
    - "未指派任何負責人的 Active Card 數量顯示為「未指派」的 `card` 數量"
    - "所在 Stage 角色為 Done 的 `card` 不計入任何負責人的 Workload"
  fail: {}
  emits: []
  requires: []
  calls-sync: []
- id: uc-drag-assign-card-owner
  name: 拖曳頭像追加卡片負責人（Workload 表）
  roles: [r-user]
  crud: {board-membership: R, card: U}
  pre:
    p1: "指定的 `card` 已存在"
  post:
    - "若該成員原本不在 `card.assignees` 中，追加為負責人，`card.assignees` 包含追加後的完整清單"
    - "若該成員已經是 `card.assignees` 成員，`card.assignees` 維持不變，且不產生新的活動紀錄"
  fail: {}
  emits: []
  requires: []
  calls-sync: []
```

拖曳追加負責人的兩個 Scenario 與 `spec-user-membership.md`「卡片負責人指派」Feature 的 `uc-assign-card-owner-by-drag` 是同一個操作；但 GH-01 要求「@uc-」tag 只能指向同一 Feature 內的 usecase，本文件另立 `uc-drag-assign-card-owner`，「pre」「post」只依本 Feature 的 Scenario 步驟推導（見 OQ-09）。

```gherkin
Feature: 人員工作量檢視
  身為 看板使用者
  我想要檢視每位成員目前手上有幾張進行中的卡片
  以便平衡團隊的工作分配

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And 看板成員包含 "雅婷" 與 "志明"
    And Stage "完成" 已設定角色為 Done

  @uc-view-workload
  # Related aggregate:
  #   board: read
  #   board-membership: read
  #   card: read
  Scenario: 檢視單一負責人的工作量
    Given "雅婷" 是 3 張進行中卡片的負責人
    When 我開啟 Workload 表
    Then "雅婷" 的工作量應該顯示為 3

  @uc-view-workload
  # Related aggregate:
  #   board: read
  #   board-membership: read
  #   card: read
  Scenario: 多位負責人的卡片，每人各算一張
    Given 卡片 "A" 的負責人同時是 "雅婷" 與 "志明"，且尚未完成
    When 我開啟 Workload 表
    Then "雅婷" 的工作量應該包含卡片 "A"
    And "志明" 的工作量應該包含卡片 "A"

  @uc-view-workload
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視未指派負責人的卡片數量
    Given 有 2 張進行中卡片沒有指派任何負責人
    When 我開啟 Workload 表
    Then "未指派" 的卡片數量應該顯示為 2

  @uc-view-workload
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 已完成的卡片不計入工作量
    Given "雅婷" 是 1 張已進入 Done 角色 Stage 的卡片的負責人
    When 我開啟 Workload 表
    Then "雅婷" 的工作量不應該包含該卡片

  @uc-drag-assign-card-owner
  # Related aggregate:
  #   board-membership: read
  #   card: write
  Scenario: 拖曳成員頭像到卡片上，追加該成員為負責人
    Given 卡片 "B" 目前的負責人只有 "志明"
    When 我將 "雅婷" 的頭像拖曳到卡片 "B" 上
    Then 卡片 "B" 的負責人應該包含 "志明" 與 "雅婷"

  @uc-drag-assign-card-owner
  # Related aggregate:
  #   board-membership: read
  #   card: write
  Scenario: 拖曳已經是負責人的成員頭像到卡片上，不重複新增
    Given 卡片 "B" 目前的負責人是 "志明" 與 "雅婷"
    When 我將 "雅婷" 的頭像拖曳到卡片 "B" 上
    Then 卡片 "B" 的負責人應該仍然只有 "志明" 與 "雅婷"
```

---

## 待釐清 / 未來擴充（Open Questions，已定案）

- 拖曳到已經是負責人的卡片：已定案為靜默忽略，不提示、不重複新增、不產生活動紀錄（見上方 Change Log 與 Scenario）。
- Workload 是否需要加權（例如依卡片估點計算，而非單純張數）：F01 目前沒有卡片估點欄位，需另開規格與 CR，刻意不做，超出本次範圍。
- 「拖曳追加單一負責人」的實際操作方式：已定案為 F02「指派多位負責人」Scenario 的追加變形（呼叫既有「Card.assignTo」，追加而非覆蓋），對應 Scenario 已補在 `spec-user-membership.md` 的「卡片負責人指派」Feature。
- OQ-09：本文件「拖曳成員頭像到卡片上，追加該成員為負責人」等兩個 Scenario 另立 `uc-drag-assign-card-owner`，未沿用 `spec-user-membership.md` 已定義的同義 `uc-assign-card-owner-by-drag`（見 `.dev/loops/spec-migration-loop/spec-migration-open-questions.md`）。
