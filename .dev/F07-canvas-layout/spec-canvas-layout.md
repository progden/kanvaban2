# 版面畫布使用案例（BDD）

本文件涵蓋看板版面的容器化配置：

- 每個看板擁有一個 Canvas（版面畫布），畫布上由一或多個 Container（容器）組成
- 看板本體固定佔用其中一個 Container；未來 F03（kanban-widgets）的圖表元件也會各自成為一個 Container，與看板本體同時顯示
- 使用者可以調整任一 Container 的位置與大小，畫布本身不限制內容多寡（新增 Container 內容超出可視範圍時的捲動／縮放行為由 `ui-canvas-layout.md` 定義）

本次疊代範圍排除：新增／移除 Container（例如把一個圖表元件加入畫布）、Canvas 的建立時機（是否隨 Board 建立自動產生）、Container 內容為 WIDGET 時實際顯示什麼——這些留待 F03 圖表元件正式整合時另開 CR 處理（見「待釐清」）。

狀態：草稿

## 名詞定義

### 實體
| ID | 名詞 | 所屬 Aggregate | 說明 |
|---|---|---|---|
| canvas | Canvas（版面畫布） | canvas（root） | 一個看板的版面配置，決定畫布上有哪些 Container |
| container | Container（容器） | canvas | 畫布上一個可獨立調整位置與大小的區塊，用來承載看板本體或未來的圖表元件 |

### 欄位
| ID | 型別／格式 | 限制 | 說明 |
|---|---|---|---|
| canvas.board | ref board | 必填、同一個 `board` 至多一個 `canvas` | 所屬看板，`board` 定義於 `.dev/F01-basic-kanban/spec-kanban-basic.md` |
| container.label | string | 可留空 | 顯示用標籤，供使用者辨識該 Container；未指定時依 `container.content-type` 顯示預設名稱 |
| container.content-type | enum(BOARD, WIDGET) | 必填 | Container 裝載的內容種類；本版僅使用 BOARD（看板本體），WIDGET（圖表元件）保留給未來 F03 整合 |
| container.position-x | number | 必填，>= 0 | Container 左上角的水平座標 |
| container.position-y | number | 必填，>= 0 | Container 左上角的垂直座標 |
| container.width | number | 必填，>= 100 | Container 寬度，最小 100 |
| container.height | number | 必填，>= 80 | Container 高度，最小 80 |
| container.z-order | number | 必填、同一個 `canvas` 內不重複 | 重疊時的堆疊順序，數字越大顯示在越上層 |

### 關係
| 來源 | 目標 | min | max | 說明 |
|---|---|---|---|---|
| canvas | container | 1 | n | 一個 Canvas 至少有一個 Container（看板本體所在的 Container） |

### 其他名詞
| 名詞 | 說明 |
|---|---|
| 看板本體 Container | `container.content-type` 為 BOARD 的 Container，顯示 F01 的看板畫面；每個 Canvas 恰有一個 |

## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|

## Aggregate 標記說明

`canvas` 為獨立 Aggregate Root，`container` 是它的子實體；每個 Scenario 上方以 Gherkin 註解標記會存取哪些 Aggregate 以及存取方式（「read」／「write」），格式與判定原則同 `spec-kanban-basic.md`。本文件用到的 Aggregate：

- `canvas`：透過 `canvas.board` 參照 `.dev/F01-basic-kanban/spec-kanban-basic.md` 定義的 `board`，本文件不重新定義 `board`。
- `container`：本文件的 Use Case 只調整既有 Container 的位置與大小，不涉及新增／移除 Container，故 Aggregate 註解只標記 `container`，不標記 `canvas`。

## 變更紀錄

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|

---

## Feature: 容器版面調整

### Use Case 定義
```usecase
- id: uc-move-container
  name: 調整 Container 位置
  roles: [r-board-owner, r-board-member]
  crud: {container: U}
  pre:
    p1: "`container` 存在"
  post:
    - "`container.position-x`、`container.position-y` 更新為指定的新座標"
  fail: {}
  emits: []
  requires: []
  calls-sync: []

- id: uc-resize-container
  name: 調整 Container 大小
  roles: [r-board-owner, r-board-member]
  crud: {container: U}
  pre:
    p1: "`container` 存在"
    p2: "指定的新 `container.width` 不小於 100，且新 `container.height` 不小於 80"
  post:
    - "`container.width`、`container.height` 更新為指定的新尺寸"
  fail:
    p2: "拒絕，訊息為「容器尺寸不得小於最小值（寬 100、高 80）」，`container` 資料不變"
  emits: []
  requires: []
  calls-sync: []
```

```gherkin
Feature: 容器版面調整
  身為 Board 成員
  我想要調整畫布上 Container 的位置與大小
  以便在看板內容變多，或未來加入更多圖表元件時，自行安排版面

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And 看板中存在一個標籤為 "看板本體" 的 Container，座標為 (0, 0)，尺寸為寬 800、高 600

  @uc-move-container
  # Related aggregate:
  #   container: write
  Scenario: 調整 Container 位置
    When 我將標籤為 "看板本體" 的 Container 移動到座標 (200, 150)
    Then 該 Container 的座標應該為 (200, 150)

  @uc-resize-container
  # Related aggregate:
  #   container: write
  Scenario: 調整 Container 大小
    When 我將標籤為 "看板本體" 的 Container 尺寸調整為寬 1000、高 700
    Then 該 Container 的尺寸應該為寬 1000、高 700

  @uc-resize-container @fail-p2
  # Related aggregate:
  #   container: read
  Scenario: Container 尺寸不得小於最小值
    When 我嘗試將標籤為 "看板本體" 的 Container 尺寸調整為寬 50、高 40
    Then 拒絕，訊息為 "容器尺寸不得小於最小值（寬 100、高 80）"，且資料不變
```

---

## 待釐清

- Canvas／Container 的建立時機尚未定義：`uc-create-board`（F02）建立看板時是否自動建立一個預設 Canvas 與看板本體 Container，待整合時另開 CR。
- `container.content-type` 為 WIDGET 的 Container 實際顯示哪個 F03 圖表、資料從何而來，待 F03 圖表元件正式整合畫布時另開 CR；本版只預留欄位值。
- 新增／移除 Container（例如把一個圖表元件加入或移出畫布）不在本版範圍，待整合時另開 Use Case。
- Container 之間是否允許重疊、畫布本身是否有邊界或需要捲動／縮放，尚未決定。
- 角色定義沿用 `.dev/F02-user-membership/spec-user-membership.md` 的 `r-board-owner`／`r-board-member`（本文件不重新定義，故「角色定義」表為空），是否應允許 Member 調整版面（而非僅 Owner）待確認；目前預設兩者皆可，因為版面調整視為顯示偏好，不影響看板結構或資料。
