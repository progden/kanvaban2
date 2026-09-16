# 故意寫錯的規格（腳本測試用）

狀態：開發中

## 名詞定義

### 實體
| ID | 名詞 | 說明 |
|---|---|---|
| swimlane | Swimlane（泳道） | 看板上的水平分區 |
| Card | Card（卡片） | 大寫 ID，違反 REF-09 |

### 欄位
| ID | 型別／格式 | 限制 | 說明 |
|---|---|---|---|
| swimlane.name | string(50) | 非空 | 泳道名稱 |
| stage.name | string(50) | 非空 | 實體 stage 不存在，違反 REF-04 |

### 關係
| 來源 | 目標 | min | max | 說明 |
|---|---|---|---|---|
| swimlane | card | 0 | n | card 未定義（小寫），違反 REF-05 |

## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|
| r-board-admin | 看板管理者 | 可管理泳道 |

## Aggregate 標記說明
略。

## 變更紀錄

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-12 | CR-099 | 新增 | 未登記的 CR，違反 REF-06（`BoardClock.now()` 違反 REF-01） |

---

## Feature: Swimlane 管理

### Use Case 定義
```usecase
- id: uc-delete-swimlane
  name: 刪除 Swimlane
  roles: [r-board-admin]
  crud: {swimlane: D}
  pre:
    p1: "`swimlane` 存在"
    p3: "key 不連號，違反 UC-01"
  post:
    - "泳道不存在（沒有 ID，違反 UC-03）"
  fail:
    p2: "拒絕，`swimlane` 不變（p2 不在 pre，違反 UC-02）"
  emits: [ev-swimlane-deleted]
  requires: []
  calls-sync: [uc-rename-swimlane]

- id: uc-rename-swimlane
  name: 重新命名 Swimlane
  roles: []
  crud: {swimlane: U}
  pre: {}
  post: []
  fail: {}
  emits: [ev-swimlane-deleted]
  requires: []
  calls-sync: [uc-delete-swimlane]
```

```gherkin
Feature: Swimlane 管理
  身為 路人
  我想要 刪除 Swimlane

  @CR-024 @wip @uc-delete-swimlane
  # Related aggregate:
  #   swimlane: read
  Scenario: 刪除空的 Swimlane
    Given 看板中存在一個 `swimlane`
    When 我刪除它
    Then 它應該消失

  @changed @CR-024 @uc-delete-swimlane @fail-p9
  Scenario: 沒有註解也沒有 deprecated
    Given 任何狀態
    Then 任何結果
```
