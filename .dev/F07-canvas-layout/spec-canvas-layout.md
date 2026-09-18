# 畫布使用案例（BDD）

本文件涵蓋畫布（Canvas）模組的核心行為：

- 把元件放置到畫布上、從畫布上移除
- 畫布元素的移動、調整大小、層序調整
- 多個畫布元素的批次移動與移除（一筆交易，全成功或全失敗）
- 畫布元素的能力設定（可否移動、可否調整大小、可否移除）
- 畫布元素的錨定（隨畫布移動，或固定在畫面上）
- 檢視區的平移與縮放（每位使用者各自記住上次的位置與縮放比例）

每個 Board（見 `.dev/F01-basic-kanban/spec-kanban-basic.md`）對應一個 Canvas：使用者開啟某個 Board 時，看到的就是該 Board 的 Canvas，Canvas 不由使用者建立或刪除（使用者第一次開啟該 Board 時，由系統自動建立，見「看板畫布初始化」Feature）；同一個 Canvas 上的元素由該 Board 的所有使用者共用；檢視區（平移偏移、縮放比例）則是每位使用者在每個 Canvas 各自一份，互不影響（同一位使用者在不同 Board 的檢視區各自獨立）。元件本身的內容與行為（例如圖表的資料、設定、互動）不屬於本模組，由元件所屬模組定義；本模組只記錄元件在畫布上的**相對資訊**：座標、大小、層序、錨定方式、可否移動、可否調整大小、可否移除。旋轉角度、群組不納入；對齊格線的吸附屬 UI 行為，座標不要求為格線倍數。檢視區要在重新開啟後恢復，因此屬本模組；縮圖是由全部元素與檢視區推導出來的畫面，與選取、對齊格線一樣由 `ui-canvas-layout.md` 承接。

狀態：草稿

## 名詞定義

### 實體
| ID | 名詞 | 所屬 Aggregate | 說明 |
|---|---|---|---|
| canvas | Canvas（畫布） | canvas（root） | 某個 Board 的版面配置，可放置元件的二維工作區；每個 `board` 對應一個 Canvas，使用者第一次開啟該 Board 時由系統自動建立，使用者不建立、不刪除 |
| item | Item（畫布元素） | item（root） | 某個元件在畫布上的一次放置，持有該元件的位置、大小、層序、錨定方式與能力設定；元件本體由其他模組定義 |
| viewport | Viewport（檢視區） | viewport（root） | 某位使用者目前看到的畫布範圍：平移偏移與縮放比例，會被記住；每位使用者各一個 |

### 欄位
| ID | 型別／格式 | 限制 | 說明 |
|---|---|---|---|
| canvas.board | ref board | 必填、同一個 `board` 至多一個 `canvas` | 所屬看板，`board` 定義於 `.dev/F01-basic-kanban/spec-kanban-basic.md` |
| canvas.zoom-min | number | > 0、< `canvas.zoom-max`、預設 0.1 | 縮放比例下限；由系統設定，設定方式不在本模組 |
| canvas.zoom-max | number | > `canvas.zoom-min`、預設 4 | 縮放比例上限；由系統設定，設定方式不在本模組 |
| item.component | string(100) | 必填 | 元件本體的識別碼，由元件所屬模組定義；本模組不解讀其內容。看板本體固定為實體 ID `board`（見「看板畫布初始化」Feature；不用 UI 層的 Screen ID，spec 不引用 ui，見 `docs-convention.md` 第 3 節）；其餘元件的值待各自所屬模組實作對應 Item 時決定。元件模組定案後改為 ref，元件與元素的生命週期連動（誰刪誰）屆時一併補 |
| item.anchor | enum(canvas, screen) | 預設 canvas | 錨定方式：canvas 表示隨畫布平移縮放；screen 表示固定在畫面上，不隨畫布平移、縮放或視窗大小改變而移動或縮放 |
| item.x | number | 必填 | 元素左上角的 X 座標；依 `item.anchor` 以畫布座標系或畫面座標系解讀 |
| item.y | number | 必填 | 元素左上角的 Y 座標；依 `item.anchor` 以畫布座標系或畫面座標系解讀 |
| item.width | number | 必填、> 0 | 元素寬度；錨定於畫布時為畫布單位，固定於畫面時為畫面單位。最小、最大尺寸與是否鎖定長寬比由元件所屬模組宣告，本模組不限制 |
| item.height | number | 必填、> 0 | 元素高度；單位同 `item.width` |
| item.z | integer | 必填、同一 `canvas` 內唯一 | 層序，值越大越靠上；不要求連續。畫面固定元素一律高於畫布元素（見其他名詞「層序」） |
| item.movable | boolean | 預設 true | 可否移動 |
| item.resizable | boolean | 預設 true | 可否調整大小 |
| item.removable | boolean | 預設 true | 可否移除 |
| viewport.user | string(100) | 必填、同一 `canvas` 內以此欄位唯一 | 所屬使用者的識別碼，由帳號模組定義；帳號模組定案後改為 ref。同一位使用者在不同 `canvas`（不同 Board）各自有一個 `viewport` |
| viewport.x | number | 必填 | 檢視區左上角在畫布座標系的 X 座標（平移偏移） |
| viewport.y | number | 必填 | 檢視區左上角在畫布座標系的 Y 座標（平移偏移） |
| viewport.zoom | number | 必填、在縮放範圍內、預設 1 | 縮放比例，1 表示一畫布單位等於一畫面單位 |

### 關係
| 來源 | 目標 | min | max | 說明 |
|---|---|---|---|---|
| board | canvas | 0 | 1 | 每個 Board 至多一個 Canvas；使用者第一次開啟該 Board 時，由系統自動建立（見「看板畫布初始化」Feature，`uc-init-canvas`） |
| canvas | item | 0 | n | 畫布可以沒有任何元素 |
| canvas | viewport | 0 | n | 同一 Canvas 內每位使用者至多一個檢視區，第一次設定時建立 |

### 其他名詞
| 名詞 | 說明 |
|---|---|
| 畫布座標系 | 以畫布原點為 (0, 0)，X 向右、Y 向下遞增的邏輯座標系；單位為畫布單位，與檢視縮放比例無關；無邊界，座標可為負值 |
| 畫面座標系 | 以檢視區左上角為 (0, 0) 的座標系，單位為畫面單位；平移或縮放畫布不影響此座標系中的位置，視窗大小改變也不換算 |
| 畫布單位 | 畫布座標系的長度單位，與畫面單位的換算由檢視縮放決定，不在本模組 |
| 縮放範圍 | `canvas.zoom-min` 到 `canvas.zoom-max`（含）之間；預設 0.1 ～ 4 |
| 縮圖 | 顯示全部元素與目前檢視區框的小圖；由資料推導，屬 `ui-canvas-layout.md` |
| 層序 | 元素重疊時的前後順序，由 z 值決定；元素之間允許重疊。畫面固定元素永遠繪於畫布元素之上，兩者各自比較 z 值。因各元素為獨立 aggregate，z 值的唯一性由儲存層唯一鍵保證 |
| 置頂 / 置底 | 層序調整的兩個方向：置頂使元素位於同一錨定方式所有元素之上，置底使其位於所有元素之下 |

## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|
| r-canvas-editor | 畫布編輯者 | 可放置與排列畫布元素，並平移與縮放自己的檢視區 |
| r-canvas-viewer | 畫布檢視者 | 只能平移與縮放自己的檢視區，不可改動元素 |

## Aggregate 標記說明

每個 Scenario 上方以 Gherkin 註解標記存取的實體與方式，名稱一律使用實體表的 ID。本文件用到的實體：`canvas`、`item`、`viewport`；`canvas.board` 參照 `.dev/F01-basic-kanban/spec-kanban-basic.md` 定義的 `board`，本文件不重新定義 `board`，Scenario 也不需要為它加 Aggregate 註解（本文件的 Use Case 都不寫入 `board`）。

- 「read」：需要先查詢既有資料才能決定如何寫入或驗證（例如放置時讀取現有 z 值、移動前確認元素可移動）。
- 「write」：會實際新增／修改／刪除該實體。

失敗 Scenario 只標「read」。註解須與所屬 Use Case 的 crud 一致。

## 變更紀錄

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-18 |  | 新增 | 依人工決策定案「待釐清」原第 1、3 項：`canvas` 建立時機（使用者第一次開啟 Board 時由系統自動建立）、看板本體如何成為 `item`（固定使用 `item.component` = `board`，即 F01 的實體 ID，不引用 ui 層 Screen ID）；新增「看板畫布初始化」Feature（`uc-init-canvas`）；本檔仍為草稿，不需開 CR |

---

## Feature: 看板畫布初始化

### Use Case 定義
```usecase
- id: uc-init-canvas
  name: 開啟看板時初始化畫布
  roles: [r-canvas-editor, r-canvas-viewer]
  crud: {canvas: CR, item: CR}
  pre:
    p1: "`board` 存在"
  post:
    - "`canvas` 存在，`canvas.board` 為所開啟的 `board`（原本不存在時建立，`canvas.zoom-min` 為 0.1、`canvas.zoom-max` 為 4）"
    - "若該 `canvas` 原本沒有任何 `item`，建立一個 `item`：`item.component` 為 `board`、`item.x` 為 0、`item.y` 為 0、`item.width` 為 900、`item.height` 為 600、`item.anchor` 為 canvas、`item.z` 為 1、`item.movable`／`item.resizable`／`item.removable` 皆為 true"
    - "若該 `canvas` 原本已存在，或已存在任何 `item`，不重複建立，資料不變"
  fail:
    p1: "拒絕，不建立 `canvas`"
  emits: [ev-canvas-initialized]
  requires: []
  calls-sync: []
```

```gherkin
Feature: 看板畫布初始化
  身為 畫布編輯者
  我想要 開啟看板時自動看到畫布與看板本體
  以便 不用自己手動建立畫布或放置看板本體

  @uc-init-canvas
  # Related aggregate:
  #   canvas: read, write
  #   item: read, write
  Scenario: 開啟尚未建立畫布的看板時自動初始化
    Given 看板 "產品開發看板" 尚無 canvas
    When 我開啟看板 "產品開發看板"
    Then 該看板存在一個 canvas，縮放範圍為 0.1 ～ 4
    And 該 canvas 存在一個元素，元件識別碼為 "board"
    And 該元素的左上角位於 (0, 0)，大小為 900 × 600
    And 該元素錨定於畫布，層序為 1
    And 該元素可移動、可調整大小且可移除

  @uc-init-canvas
  # Related aggregate:
  #   canvas: read, write
  #   item: read, write
  Scenario: 開啟已有畫布的看板不重複初始化
    Given 看板 "產品開發看板" 已有 canvas
    And 該 canvas 存在元素 "看板本體"，元件識別碼為 "board"，層序為 1
    And 該 canvas 另存在元素 "銷售圖表"，層序為 2
    When 我再次開啟看板 "產品開發看板"
    Then 該看板仍只有一個 canvas
    And 元素 "看板本體" 的層序仍為 1
    And 元素 "銷售圖表" 的層序仍為 2

  @uc-init-canvas @fail-p1
  # Related aggregate:
  #   canvas: read
  Scenario: 不可為不存在的看板初始化畫布
    Given 看板 "已刪除的看板" 不存在
    When 我開啟看板 "已刪除的看板"
    Then 拒絕，訊息為 "看板不存在"，且資料不變
```

---

## Feature: 元件放置

### Use Case 定義
```usecase
- id: uc-place-item
  name: 放置元件到畫布
  roles: [r-canvas-editor]
  crud: {item: CR}
  pre:
    p1: "指定的 `item.width` 與 `item.height` 皆大於 0"
  post:
    - "`item` 存在，`item.component` 為指定的元件識別碼"
    - "`item.x`、`item.y`、`item.width`、`item.height` 為指定值"
    - "`item.anchor` 為指定值；未指定時為 canvas"
    - "`item.z` 為同一錨定方式現有元素最大 z 值加一；尚無元素時為 1"
    - "`item.movable`、`item.resizable`、`item.removable` 為指定值；未指定時為 true"
  fail:
    p1: "拒絕，`item` 不建立"
  emits: [ev-item-placed]
  requires: []
  calls-sync: []

- id: uc-remove-item
  name: 從畫布移除元素
  roles: [r-canvas-editor]
  crud: {item: RD}
  pre:
    p1: "`item` 存在"
    p2: "`item.removable` 為 true"
  post:
    - "`item` 不存在；其他 `item` 的 z 值不變"
  fail:
    p1: "拒絕，資料不變"
    p2: "拒絕，`item` 仍存在"
  emits: [ev-item-removed]
  requires: []
  calls-sync: []
```

```gherkin
Feature: 元件放置
  身為 畫布編輯者
  我想要 把元件放到畫布上，或從畫布上移除
  以便 決定畫布上有哪些內容

  Background:
    Given 畫布已由系統建立

  @uc-place-item
  # Related aggregate:
  #   item: read, write
  Scenario: 放置元件到空畫布
    Given 畫布中沒有任何元素
    When 我將元件 "銷售圖表" 放置到畫布，欄位如下：
      | 欄位 | 內容 |
      | X    | 100  |
      | Y    | 200  |
      | 寬   | 300  |
      | 高   | 200  |
    Then 畫布中存在元素 "銷售圖表"
    And 元素 "銷售圖表" 的左上角位於 (100, 200)
    And 元素 "銷售圖表" 的大小為 300 × 200
    And 元素 "銷售圖表" 錨定於畫布
    And 元素 "銷售圖表" 的層序為 1
    And 元素 "銷售圖表" 可移動、可調整大小且可移除

  @uc-place-item
  # Related aggregate:
  #   item: read, write
  Scenario: 新放置的元素位於同一錨定方式的最上層
    Given 畫布中存在 2 個錨定於畫布的元素，層序分別為 1、2
    When 我將元件 "備註" 放置到畫布，左上角 (0, 0)，大小 100 × 100
    Then 元素 "備註" 的層序為 3

  @uc-place-item
  # Related aggregate:
  #   item: read, write
  Scenario: 放置固定在畫面上的元件
    When 我將元件 "總覽儀表" 放置到畫布，欄位如下：
      | 欄位   | 內容   |
      | X      | 16     |
      | Y      | 16     |
      | 寬     | 240    |
      | 高     | 120    |
      | 錨定   | 畫面   |
    Then 元素 "總覽儀表" 固定於畫面
    And 元素 "總覽儀表" 的左上角位於畫面座標 (16, 16)

  @uc-place-item
  # Related aggregate:
  #   item: read, write
  Scenario: 放置時指定元素不可移動且不可調整大小
    When 我將元件 "背景框" 放置到畫布，欄位如下：
      | 欄位       | 內容 |
      | X          | 0    |
      | Y          | 0    |
      | 寬         | 800  |
      | 高         | 600  |
      | 可移動     | 否   |
      | 可調整大小 | 否   |
    Then 元素 "背景框" 不可移動且不可調整大小

  @uc-place-item @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 元素寬與高必須大於 0
    When 我將元件 "銷售圖表" 放置到畫布，左上角 (0, 0)，大小 0 × 100
    Then 拒絕，訊息為 "寬與高必須大於 0"，且資料不變

  @uc-remove-item
  # Related aggregate:
  #   item: read, write
  Scenario: 移除畫布元素
    Given 畫布中存在元素 "銷售圖表"，層序為 1
    And 畫布中存在元素 "備註"，層序為 2
    When 我移除元素 "銷售圖表"
    Then 畫布中不存在元素 "銷售圖表"
    And 元素 "備註" 的層序仍為 2

  @uc-remove-item @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可移除不存在的元素
    Given 畫布中不存在元素 "銷售圖表"
    When 我移除元素 "銷售圖表"
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-remove-item @fail-p2
  # Related aggregate:
  #   item: read
  Scenario: 不可移除設為不可移除的元素
    Given 畫布中存在元素 "背景框"，不可移除
    When 我移除元素 "背景框"
    Then 拒絕，訊息為 "此元素不可移除"，且資料不變
```

---

## Feature: 畫布元素排列

### Use Case 定義
```usecase
- id: uc-move-item
  name: 移動畫布元素
  roles: [r-canvas-editor]
  crud: {item: RU}
  pre:
    p1: "`item` 存在"
    p2: "`item.movable` 為 true"
  post:
    - "`item.x`、`item.y` 更新為指定值（依 `item.anchor` 的座標系解讀）；`item.width`、`item.height`、`item.z` 不變"
  fail:
    p1: "拒絕，資料不變"
    p2: "拒絕，`item.x`、`item.y` 不變"
  emits: [ev-item-moved]
  requires: []
  calls-sync: []

- id: uc-resize-item
  name: 調整畫布元素大小
  roles: [r-canvas-editor]
  crud: {item: RU}
  pre:
    p1: "`item` 存在"
    p2: "`item.resizable` 為 true"
    p3: "指定的 `item.width` 與 `item.height` 皆大於 0"
    p4: "`item.movable` 為 false 時，指定的 `item.x`、`item.y` 與現值相同"
  post:
    - "`item.x`、`item.y`、`item.width`、`item.height` 更新為指定值；`item.z` 不變"
  fail:
    p1: "拒絕，資料不變"
    p2: "拒絕，`item.x`、`item.y`、`item.width`、`item.height` 不變"
    p3: "拒絕，`item.x`、`item.y`、`item.width`、`item.height` 不變"
    p4: "拒絕，`item.x`、`item.y`、`item.width`、`item.height` 不變"
  emits: [ev-item-resized]
  requires: []
  calls-sync: []

- id: uc-set-item-capabilities
  name: 設定畫布元素的能力
  roles: [r-canvas-editor]
  crud: {item: U}
  pre:
    p1: "`item` 存在"
  post:
    - "`item.movable`、`item.resizable`、`item.removable` 更新為指定值"
  fail:
    p1: "拒絕，資料不變"
  emits: [ev-item-capabilities-set]
  requires: []
  calls-sync: []

- id: uc-set-item-anchor
  name: 設定畫布元素的錨定方式
  roles: [r-canvas-editor]
  crud: {item: RU}
  pre:
    p1: "`item` 存在"
  post:
    - "`item.anchor` 更新為指定值"
    - "`item.x`、`item.y`、`item.width`、`item.height` 更新為指定值，以新的錨定座標系與單位解讀；不由系統換算"
    - "`item.z` 為新錨定方式現有元素最大 z 值加一"
  fail:
    p1: "拒絕，資料不變"
  emits: [ev-item-anchor-set]
  requires: []
  calls-sync: []

- id: uc-reorder-item
  name: 調整畫布元素層序（置頂或置底）
  roles: [r-canvas-editor]
  crud: {item: RU}
  pre:
    p1: "`item` 存在"
  post:
    - "指定置頂時，`item.z` 大於同一錨定方式其他所有 `item` 的 z 值；指定置底時，小於其他所有 `item` 的 z 值"
    - "其他 `item` 的 z 值不變"
  fail:
    p1: "拒絕，資料不變"
  emits: [ev-item-reordered]
  requires: []
  calls-sync: []
```

```gherkin
Feature: 畫布元素排列
  身為 畫布編輯者
  我想要 移動、縮放畫布上的元素、調整前後順序，並決定元素是否固定在畫面上
  以便 安排畫布的版面

  Background:
    Given 畫布已由系統建立
    And 畫布中存在元素 "銷售圖表"，錨定於畫布，左上角 (100, 200)，大小 300 × 200，可移動且可調整大小

  @uc-move-item
  # Related aggregate:
  #   item: read, write
  Scenario: 移動元素
    When 我將元素 "銷售圖表" 移動到左上角 (350, 40)
    Then 元素 "銷售圖表" 的左上角位於 (350, 40)
    And 元素 "銷售圖表" 的大小仍為 300 × 200

  @uc-move-item
  # Related aggregate:
  #   item: read, write
  Scenario: 元素可移動到負座標
    When 我將元素 "銷售圖表" 移動到左上角 (-500, -120)
    Then 元素 "銷售圖表" 的左上角位於 (-500, -120)

  @uc-move-item @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可移動不存在的元素
    Given 畫布中不存在元素 "備註"
    When 我將元素 "備註" 移動到左上角 (0, 0)
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-move-item @fail-p2
  # Related aggregate:
  #   item: read
  Scenario: 不可移動設為不可移動的元素
    Given 畫布中存在元素 "背景框"，左上角 (0, 0)，不可移動
    When 我將元素 "背景框" 移動到左上角 (50, 50)
    Then 拒絕，訊息為 "此元素不可移動"，且資料不變

  @uc-resize-item
  # Related aggregate:
  #   item: read, write
  Scenario: 以左上角為錨點調整元素大小
    When 我將元素 "銷售圖表" 調整為左上角 (100, 200)，大小 500 × 320
    Then 元素 "銷售圖表" 的大小為 500 × 320
    And 元素 "銷售圖表" 的左上角仍位於 (100, 200)

  @uc-resize-item
  # Related aggregate:
  #   item: read, write
  Scenario: 以右下角為錨點調整元素大小
    When 我將元素 "銷售圖表" 調整為左上角 (50, 150)，大小 350 × 250
    Then 元素 "銷售圖表" 的左上角位於 (50, 150)
    And 元素 "銷售圖表" 的大小為 350 × 250

  @uc-resize-item
  # Related aggregate:
  #   item: read, write
  Scenario: 不可移動但可調整大小的元素可以在原位置調整大小
    Given 畫布中存在元素 "固定圖表"，左上角 (0, 0)，大小 300 × 200，不可移動、可調整大小
    When 我將元素 "固定圖表" 調整為左上角 (0, 0)，大小 400 × 300
    Then 元素 "固定圖表" 的大小為 400 × 300
    And 元素 "固定圖表" 的左上角仍位於 (0, 0)

  @uc-resize-item @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可調整不存在元素的大小
    Given 畫布中不存在元素 "備註"
    When 我將元素 "備註" 調整為左上角 (0, 0)，大小 100 × 100
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-resize-item @fail-p2
  # Related aggregate:
  #   item: read
  Scenario: 不可調整設為不可調整大小的元素
    Given 畫布中存在元素 "背景框"，大小 800 × 600，不可調整大小
    When 我將元素 "背景框" 調整為左上角 (0, 0)，大小 400 × 300
    Then 拒絕，訊息為 "此元素不可調整大小"，且資料不變

  @uc-resize-item @fail-p3
  # Related aggregate:
  #   item: read
  Scenario: 調整後的寬與高必須大於 0
    When 我將元素 "銷售圖表" 調整為左上角 (100, 200)，大小 300 × 0
    Then 拒絕，訊息為 "寬與高必須大於 0"，且資料不變

  @uc-resize-item @fail-p4
  # Related aggregate:
  #   item: read
  Scenario: 不可移動的元素不可藉由調整大小改變位置
    Given 畫布中存在元素 "固定圖表"，左上角 (0, 0)，大小 300 × 200，不可移動、可調整大小
    When 我將元素 "固定圖表" 調整為左上角 (-50, -50)，大小 350 × 250
    Then 拒絕，訊息為 "此元素不可移動"，且資料不變

  @uc-set-item-capabilities
  # Related aggregate:
  #   item: write
  Scenario: 將元素設為不可移動、不可調整大小、不可移除
    When 我將元素 "銷售圖表" 設為不可移動、不可調整大小、不可移除
    Then 元素 "銷售圖表" 不可移動、不可調整大小且不可移除

  @uc-set-item-capabilities @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可設定不存在元素的能力
    Given 畫布中不存在元素 "備註"
    When 我將元素 "備註" 設為不可移動、不可調整大小
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-set-item-anchor
  # Related aggregate:
  #   item: read, write
  Scenario: 將畫布元素改為固定在畫面上
    Given 畫布中存在元素 "總覽儀表"，固定於畫面，層序為 1
    When 我將元素 "銷售圖表" 固定於畫面，左上角為畫面座標 (16, 160)，大小 240 × 120
    Then 元素 "銷售圖表" 固定於畫面
    And 元素 "銷售圖表" 的左上角位於畫面座標 (16, 160)
    And 元素 "銷售圖表" 的大小為畫面單位 240 × 120
    And 元素 "銷售圖表" 的層序高於元素 "總覽儀表"

  @uc-set-item-anchor
  # Related aggregate:
  #   item: read, write
  Scenario: 將畫面固定元素改回錨定於畫布
    Given 畫布中存在元素 "總覽儀表"，固定於畫面
    When 我將元素 "總覽儀表" 錨定於畫布，左上角為畫布座標 (400, 400)，大小 300 × 150
    Then 元素 "總覽儀表" 錨定於畫布
    And 元素 "總覽儀表" 的左上角位於 (400, 400)
    And 元素 "總覽儀表" 的大小為 300 × 150

  @uc-set-item-anchor @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可設定不存在元素的錨定方式
    Given 畫布中不存在元素 "備註"
    When 我將元素 "備註" 固定於畫面，左上角為畫面座標 (0, 0)，大小 100 × 100
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-reorder-item
  # Related aggregate:
  #   item: read, write
  Scenario: 將元素置頂
    Given 畫布中存在元素 "備註"，錨定於畫布，層序高於元素 "銷售圖表"
    When 我將元素 "銷售圖表" 置頂
    Then 元素 "銷售圖表" 的層序高於所有錨定於畫布的其他元素
    And 元素 "備註" 的層序不變

  @uc-reorder-item
  # Related aggregate:
  #   item: read, write
  Scenario: 將元素置底
    Given 畫布中存在元素 "備註"，錨定於畫布，層序低於元素 "銷售圖表"
    When 我將元素 "銷售圖表" 置底
    Then 元素 "銷售圖表" 的層序低於所有錨定於畫布的其他元素
    And 元素 "備註" 的層序不變

  @uc-reorder-item @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可調整不存在元素的層序
    Given 畫布中不存在元素 "備註"
    When 我將元素 "備註" 置頂
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變
```

---

## Feature: 畫布元素批次操作

### Use Case 定義
```usecase
- id: uc-move-items
  name: 批次移動畫布元素（同一位移量）
  roles: [r-canvas-editor]
  crud: {item: RU}
  pre:
    p1: "指定的每個 `item` 皆存在"
    p2: "指定的所有 `item` 的 `item.anchor` 相同"
    p3: "指定的每個 `item` 的 `item.movable` 皆為 true"
  post:
    - "指定的每個 `item` 的 `item.x`、`item.y` 各加上指定的位移量；其餘欄位不變"
    - "未指定的 `item` 不變"
  fail:
    p1: "拒絕，資料不變"
    p2: "拒絕，資料不變"
    p3: "拒絕，資料不變"
  emits: [ev-items-moved]
  requires: []
  calls-sync: []

- id: uc-remove-items
  name: 批次移除畫布元素
  roles: [r-canvas-editor]
  crud: {item: RD}
  pre:
    p1: "指定的每個 `item` 皆存在"
    p2: "指定的每個 `item` 的 `item.removable` 皆為 true"
  post:
    - "指定的每個 `item` 皆不存在；其他 `item` 的 z 值不變"
  fail:
    p1: "拒絕，資料不變"
    p2: "拒絕，資料不變"
  emits: [ev-items-removed]
  requires: []
  calls-sync: []
```

```gherkin
Feature: 畫布元素批次操作
  身為 畫布編輯者
  我想要 一次移動或移除多個元素
  以便 整組調整版面時不必逐一操作

  Background:
    Given 畫布已由系統建立
    And 畫布中存在元素 "銷售圖表"，錨定於畫布，左上角 (100, 200)，可移動
    And 畫布中存在元素 "備註"，錨定於畫布，左上角 (500, 200)，可移動

  @uc-move-items
  # Related aggregate:
  #   item: read, write
  Scenario: 批次移動多個元素
    When 我將元素 "銷售圖表"、"備註" 一起移動，位移量為 (+50, -30)
    Then 元素 "銷售圖表" 的左上角位於 (150, 170)
    And 元素 "備註" 的左上角位於 (550, 170)

  @uc-move-items @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 批次移動包含不存在的元素時全部不動
    Given 畫布中不存在元素 "舊圖表"
    When 我將元素 "銷售圖表"、"舊圖表" 一起移動，位移量為 (+50, 0)
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-move-items @fail-p2
  # Related aggregate:
  #   item: read
  Scenario: 不可同時批次移動畫布元素與畫面固定元素
    Given 畫布中存在元素 "總覽儀表"，固定於畫面，可移動
    When 我將元素 "銷售圖表"、"總覽儀表" 一起移動，位移量為 (+50, 0)
    Then 拒絕，訊息為 "不可同時移動畫布元素與畫面固定元素"，且資料不變

  @uc-move-items @fail-p3
  # Related aggregate:
  #   item: read
  Scenario: 批次移動包含不可移動的元素時全部不動
    Given 畫布中存在元素 "背景框"，錨定於畫布，左上角 (0, 0)，不可移動
    When 我將元素 "銷售圖表"、"背景框" 一起移動，位移量為 (+50, 0)
    Then 拒絕，訊息為 "所選元素中有不可移動的元素"，且資料不變

  @uc-remove-items
  # Related aggregate:
  #   item: read, write
  Scenario: 批次移除多個元素
    Given 畫布中存在元素 "背景框"，層序為 3
    When 我一起移除元素 "銷售圖表"、"備註"
    Then 畫布中不存在元素 "銷售圖表"
    And 畫布中不存在元素 "備註"
    And 元素 "背景框" 的層序仍為 3

  @uc-remove-items @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 批次移除包含不存在的元素時全部保留
    Given 畫布中不存在元素 "舊圖表"
    When 我一起移除元素 "銷售圖表"、"舊圖表"
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-remove-items @fail-p2
  # Related aggregate:
  #   item: read
  Scenario: 批次移除包含不可移除的元素時全部保留
    Given 畫布中存在元素 "背景框"，不可移除
    When 我一起移除元素 "銷售圖表"、"背景框"
    Then 拒絕，訊息為 "所選元素中有不可移除的元素"，且資料不變
```

---

## Feature: 檢視區

### Use Case 定義
```usecase
- id: uc-set-viewport
  name: 設定自己的檢視區（平移或縮放）
  roles: [r-canvas-editor, r-canvas-viewer]
  crud: {canvas: R, viewport: CRU}
  pre:
    p1: "指定的 `viewport.zoom` 介於 `canvas.zoom-min` 與 `canvas.zoom-max` 之間（含）"
  post:
    - "操作者的 `viewport` 存在（原本不存在時建立，`viewport.user` 為操作者）"
    - "該 `viewport` 的 `viewport.x`、`viewport.y`、`viewport.zoom` 更新為指定值"
    - "其他使用者的 `viewport` 不變"
  fail:
    p1: "拒絕，`viewport` 不變"
  emits: [ev-viewport-changed]
  requires: []
  calls-sync: []
```

```gherkin
Feature: 檢視區
  身為 畫布編輯者
  我想要 平移與縮放自己的檢視區，並在下次開啟時回到同一位置
  以便 在畫布的任何範圍上工作，且不影響其他人看到的範圍

  Background:
    Given 畫布已由系統建立
    And 縮放範圍為 0.1 ～ 4
    And 我是使用者 "user1"，我的檢視區左上角位於畫布座標 (0, 0)，縮放比例為 1

  @uc-set-viewport
  # Related aggregate:
  #   canvas: read
  #   viewport: read, write
  Scenario: 平移檢視區
    When 我將檢視區設定為左上角 (1200, -300)，縮放比例 1
    Then 我的檢視區左上角位於畫布座標 (1200, -300)
    And 我的檢視區的縮放比例為 1

  @uc-set-viewport
  # Related aggregate:
  #   canvas: read
  #   viewport: read, write
  Scenario: 縮放檢視區
    When 我將檢視區設定為左上角 (150, 100)，縮放比例 2
    Then 我的檢視區的縮放比例為 2
    And 我的檢視區左上角位於畫布座標 (150, 100)

  @uc-set-viewport
  # Related aggregate:
  #   canvas: read
  #   viewport: read, write
  Scenario: 設定自己的檢視區不影響其他使用者
    Given 使用者 "user2" 的檢視區左上角位於畫布座標 (0, 0)，縮放比例為 1
    When 我將檢視區設定為左上角 (1200, -300)，縮放比例 1
    Then 使用者 "user2" 的檢視區左上角仍位於畫布座標 (0, 0)
    And 使用者 "user2" 的檢視區的縮放比例仍為 1

  @uc-set-viewport
  # Related aggregate:
  #   canvas: read
  #   viewport: read, write
  Scenario: 第一次設定檢視區時建立
    Given 使用者 "user3" 尚無檢視區
    And 我是使用者 "user3"
    When 我將檢視區設定為左上角 (300, 300)，縮放比例 1
    Then 我的檢視區左上角位於畫布座標 (300, 300)

  @uc-set-viewport
  # Related aggregate:
  #   canvas: read
  #   viewport: read, write
  Scenario: 平移檢視區不影響畫面固定元素
    Given 畫布中存在元素 "總覽儀表"，固定於畫面，左上角為畫面座標 (16, 16)
    When 我將檢視區設定為左上角 (1200, -300)，縮放比例 1
    Then 元素 "總覽儀表" 的左上角仍位於畫面座標 (16, 16)

  @uc-set-viewport @fail-p1
  # Related aggregate:
  #   canvas: read
  #   viewport: read
  Scenario: 縮放比例不可超出縮放範圍
    When 我將檢視區設定為左上角 (0, 0)，縮放比例 0
    Then 拒絕，訊息為 "縮放比例超出範圍"，且資料不變
```

---

## 待釐清

- `item.component`、`viewport.user` 兩個外部識別碼待元件模組與帳號模組定案後改為 ref，已註記於欄位表。
- `r-canvas-editor`／`r-canvas-viewer` 與帳號模組（`.dev/F02-user-membership/spec-user-membership.md`）定義的 `r-system-user`／`r-board-owner`／`r-board-member` 三者的對應關係尚未定義，待整合時另開 CR 或於本檔草稿階段直接補上。