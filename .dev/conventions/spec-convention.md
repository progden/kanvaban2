# 規格文件撰寫規範（Spec Convention）

本文件定義專案中以 BDD / Gherkin 撰寫規格文件的方式，包含：文件結構、Gherkin 撰寫規則、Aggregate 標記、變更管理（tag 與流程）、何時該拆新 feature 檔，以及讓腳本能解析規格的 ID 規則。

規格文件視同程式碼：進版控、走 PR、用 tag 標狀態，不依賴口頭或記憶來區分「哪些做完、哪些沒做、哪些改過」。

規格的檢查分三段（詳見 `checks.md`、`scripts.md`、`llm-review.md`）：

1. 作者依本文件的固定格式寫規格。
2. 腳本（`spec-check`）解析固定格式，做參照完整性與矛盾檢查；有 error 就不能合併。
3. LLM 只做要讀懂內容才能判斷的語意 review；腳本檢查過的項目 LLM 不重做。

---

## 1. 檔案組織

| 項目 | 規則 |
|------|------|
| 一個模組一份文件 | 例如 `spec-kanban-basic.md`、`spec-approval-flow.md` |
| 文件格式 | Markdown，Gherkin 內容放在 ```gherkin 程式碼區塊中；Use Case 定義放在 ```usecase 程式碼區塊中（內容為 YAML） |
| 一份文件可含多個 Feature | 以 `---` 分隔，每個 Feature 一個 `## Feature:` 標題 |
| Feature 對應 `.feature` 檔 | 若導入自動化測試，每個 `## Feature:` 區塊 1:1 對應一個 `.feature` 檔，內容直接複製程式碼區塊。這份 `.feature` 執行於應用層／API，不經 UI；UI 層的測試來源是 `ui-<模組>.md` 的驗收條件 |
| 檔名 | 規格文件一律以 `spec-` 開頭，後接模組名：`spec-<模組>.md`，小寫英文 + 連字號。對應的 `.feature` 檔同樣以 `spec-` 開頭：`spec-<模組>-<feature>.feature`，例如 `spec-kanban-basic-card-editing.feature` |
| 配套的 UI 短規格 | 同目錄下的 `ui-<模組>.md`，格式見 `ui-convention.md`；它只引用本文件定義的 ID，不定義新的實體、角色或 Use Case |
| 段落標題 | 第 2 節列出的段落標題是**固定字串**，腳本靠它們切段；不得改寫、翻譯或調整層級 |

---

## 2. 文件結構模板

每份模組規格文件依序包含以下區段。標題以「固定字串開頭」即可，固定字串後只能接行尾、空白、`（`、`(` 或 `/`（例如 `## 變更紀錄（Change Log）`、`## 待釐清 / 未來擴充`），腳本以前綴比對。

**格式範例**（規格檔 `spec-<模組>.md`）：

````markdown
# <模組名稱>使用案例（BDD）

<一段簡介：本文件涵蓋哪些功能，條列列出>

狀態：草稿

## 名詞定義

### 實體
| ID | 名詞 | 說明 |
|---|---|---|

### 欄位
| ID | 型別／格式 | 限制 | 說明 |
|---|---|---|---|

### 關係
| 來源 | 目標 | min | max | 說明 |
|---|---|---|---|---|

### 其他名詞
| 名詞 | 說明 |
|---|---|

## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|

## Aggregate 標記說明
（固定段落，見第 4 節）

## 變更紀錄
（見第 6 節；新模組可先留空）

---

## Feature: <功能名稱>

### Use Case 定義
```usecase
- id: uc-...
  ...
```

```gherkin
Feature: <功能名稱>
  ...
```

---

## Feature: <功能名稱>
...

---

## 待釐清
- ...
````

### 固定字串標題一覽

| 固定字串 | 層級 | 出現次數 | 腳本用途 |
|------|------|------|------|
| `狀態：` | 第一個 H2 之前的一行 | 恰好 1 | 值為「草稿」或「開發中」，判定是否已進入開發 |
| `## 名詞定義` | H2 | 恰好 1 | 定位實體／欄位／關係／其他名詞四張表 |
| `### 實體`、`### 欄位`、`### 關係`、`### 其他名詞` | H3 | 各至多 1（其他名詞可省略） | 解析各表 |
| `## 角色定義` | H2 | 恰好 1 | 解析角色表 |
| `## Aggregate 標記說明` | H2 | 恰好 1 | 不解析內容，只確認存在 |
| `## 變更紀錄` | H2 | 恰好 1 | 解析變更紀錄表（票號、類型） |
| `## Feature:` | H2 | 1 個以上 | 切出每個 Feature 區段 |
| `### Use Case 定義` | H3 | 每個 Feature 恰好 1，位於 gherkin 區塊之前 | 解析 ```usecase 區塊 |
| `## 待釐清` | H2 | 至多 1 | 不解析內容 |

### 2.1 各區段說明

- **簡介**：一段話說明模組範圍，並條列本文件涵蓋的功能群。
- **狀態行**：第一個 H2 之前一行 `狀態：草稿` 或 `狀態：開發中`（固定字串，必填）。「開發中」代表規格已交付開發，之後的改動都要走 CR（第 5、6 節與 `cr-convention.md`）；首版交付時把「草稿」改成「開發中」，並在變更紀錄加一列「首版進入開發」，這一步本身不用 CR。
- **名詞定義**：四張表，見 2.2。所有在 Scenario 中出現的領域名詞都要在此定義，中英文並列（例：Swimlane（泳道））。
- **角色定義**：本模組會出現在 Feature 標頭「身為」與 usecase `roles` 的角色，見 2.3。
- **Aggregate 標記說明**：固定段落，說明註解格式與 read / write 的判定原則。
- **變更紀錄**：規格進入開發後的所有變更摘要，一行一筆。
- **Feature 區塊**：主要內容，每個 Feature 先放 Use Case 定義（2.4），再放 Gherkin。
- **待釐清**：尚未決定、但已知需要討論的問題。已決定的項目要從此區移除並轉成 Scenario。

### 2.2 名詞定義的四張表

腳本解析前三張表；「其他名詞」表沒有 ID，腳本不解析。

**格式範例**（名詞定義）：

```markdown
## 名詞定義

### 實體
| ID | 名詞 | 所屬 Aggregate | 說明 |
|---|---|---|---|
| board | Board（看板） | board（root） | 整體工作區，包含多個 Swimlane 與 Stage |
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
```

規則：

- **實體表**：`ID` 欄是實體 ID（純小寫英文，可含連字號），就是第 4 節 Aggregate 註解裡用的名稱。實體不等於 aggregate：一個 aggregate 是一棵實體樹，樹根那個實體是 aggregate root；所有有身分、會被單獨建立／修改／刪除的實體都列在這張表，`所屬 Aggregate` 欄寫它所在的樹根 ID（root 自己標「（root）」），這一欄是給人看的提示，腳本不解析。ID 全專案唯一，跨模組引用時直接用同一個 ID，不在第二個模組重新定義；`spec-check --report` 的「外部引用」表會列出每個模組引用了哪些別處定義的 ID。
- **欄位表**：`ID` 欄一律 `<實體 ID>.<欄位名>`；`<實體 ID>` 必須在實體表存在。`型別／格式` 欄自由文字，參照其他實體時寫 `ref <實體 ID>`。`限制` 欄寫驗證規則，UI 短規格的驗證欄位會直接抄這裡。
- **關係表**：`來源`、`目標` 必須是實體表的 ID；`min`、`max` 填數字或 `n`。腳本用 `max` 做字面上的 cardinality 檢查（見 `checks.md` UC-10）。
- **其他名詞表**：非實體的領域名詞（規則名、計算指標、時間概念）放這裡，沒有 ID，不得在自由文字裡用反引號包住（反引號只給 ID 用，見第 9 節）。

### 2.3 角色定義

**格式範例**（角色定義）：

```markdown
## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|
| r-board-admin | 看板管理者 | 可管理泳道與階段 |
| r-member | 看板成員 | 可操作卡片 |
```

規則：

- `ID` 一律 `r-` 前綴。
- `名稱` 是 Feature 標頭「身為 <名稱>」用的字串，必須跟標頭寫的**完全相同**（腳本以名稱反查 ID）。
- usecase 區塊的 `roles` 用 ID，不用名稱。

### 2.4 Use Case 定義

每個 `## Feature:` 底下、```gherkin 區塊之前，一個 `### Use Case 定義` 段落，內含一個 ```usecase 區塊，內容是 YAML，一個 Use Case 一個項目。這裡的 Use Case 是**一個交易**（一次呼叫、一次 commit），不是跨多個步驟的 UML 使用案例。

**格式範例**（Use Case 定義，沿用 CR-024「刪除含卡片的 Swimlane 改為轉移」情境）：

````markdown
### Use Case 定義
```usecase
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
```
````

欄位規則：

| 欄位 | 必填 | 格式 |
|------|------|------|
| `id` | 是 | `uc-` 前綴，全專案唯一 |
| `name` | 是 | 中文名稱，與 Scenario 名稱可以不同 |
| `roles` | 是 | 角色 ID 的 list；由使用者觸發的寫入類 Use Case（`crud` 含 C／U／D 且 `requires` 為空）不得為空；由事件觸發（`requires` 非空）或讀取類可為空 `[]` |
| `crud` | 是 | map，key 是實體 ID，value 是 `C`、`R`、`U`、`D` 的組合字串（例如 `RU`），只列實際會碰到的實體 |
| `pre` | 是 | map，key 固定 `p1`、`p2`… 連號；每句至少含一個反引號 ID |
| `post` | 是 | list；每句至少含一個反引號 ID；引用的欄位所屬實體，`crud` 必須含 C 或 U |
| `fail` | 欄位要在，可空 `{}` | map，key 必須是 `pre` 裡存在的 key；每句至少含一個反引號 ID。唯一例外是固定句「拒絕，資料不變」（一字不差）可以不含 ID。句子不限制句型 |
| `emits` | 欄位要在，可空 `[]` | 事件 ID 的 list，`ev-` 前綴。事件由這裡宣告，不另立段落；一個事件全專案恰好一個 Use Case `emits` 它 |
| `requires` | 欄位要在，可空 `[]` | 事件 ID 的 list，代表本 Use Case 由該事件觸發（非同步）；列的事件必須有某個 Use Case `emits` 它 |
| `calls-sync` | 欄位要在，可空 `[]` | Use Case ID 的 list，代表交易內同步呼叫另一個 Use Case，屬例外情況，要在 `name` 或說明中講清楚為什麼不能用事件 |

- 跨 Use Case 的順序只由 `emits`／`requires` 表達，不另畫序列圖。
- `requires` 與 `calls-sync` 各自建圖都不得有循環。
- usecase 區塊不掛 tag。規格進入開發後要改 usecase 區塊，一律開 CR（`cr-convention.md` 第 1 節），並在 CR 的影響範圍列出該 `uc-` ID；改動內容以 PR diff 為準。

---

## 3. Gherkin 撰寫規則

### 3.1 Feature 標頭

**格式範例**（寫在規格檔的 Gherkin 區塊內，Feature 開頭三行）：

```gherkin
Feature: <功能名稱>
  身為 <角色名稱>
  我想要 <做什麼>
  以便 <達成什麼目的>
```

三行固定句型：身為 / 我想要 / 以便。

- `身為` 後面寫**角色名稱**（不是 ID），中間一個半形空格，名稱必須在「角色定義」表存在，腳本以名稱反查 ID。
- 一個 Feature 只寫一個角色；多個角色會用到同一組功能時，以主要角色為準，其餘由 usecase 的 `roles` 表達。

### 3.2 Background

放所有 Scenario 共用的前置條件（登入、開啟看板、預設資料）。只放「每個 Scenario 都需要」的東西；只有部分 Scenario 需要的前提放在該 Scenario 的 Given。

### 3.3 Scenario 命名

- 描述「行為 + 結果」，不描述 UI 操作：`刪除包含卡片的 Swimlane 需要先轉移卡片` ✅，`點擊刪除按鈕` ❌；整個 Scenario 全文（不只名稱）都適用，依 3.4 的禁字規則
- 錯誤情境直接寫出規則：`Swimlane 名稱不可為空`、`看板至少保留一個 Stage`
- 一個 Scenario 只驗證一個行為
- 每個 Scenario 都屬於恰好一個 Use Case，以 `@uc-<id>` tag 標示（第 5 節）

### 3.4 步驟撰寫

Gherkin 步驟用意圖與領域結果語言撰寫，不是 UI 操作語言：

| 關鍵字 | 用途 | 語氣 |
|--------|------|------|
| Given | 領域前置狀態 | 「看板中存在…」「卡片 "X" 屬於泳道 "Y"」 |
| When | 使用者意圖（對應一個 Use Case，一次交易） | 「我刪除該 Swimlane 並指定目的泳道為 "…"」「我將卡片移到…」 |
| Then | 領域結果或 Use Case 回傳結果 | 「Swimlane "X" 不存在」「拒絕，訊息為 "…"，且資料不變」 |
| And | 延續上一個關鍵字 | — |

- 一個 Scenario 只包含一個 When（一次交易）。確認、取消、視窗開關等互動由 `ui-<模組>.md` 操作表的「需確認？」欄承接，不進 spec。
- 步驟中的具名資料（名稱、標題、訊息文字、日期）一律用半形雙引號：`"緊急項目"`、`"2026-09-20"`。
- 失敗結果寫成 `Then 拒絕，訊息為 "..."，且資料不變`（或 `結果為 "..."`）；訊息文字視為 Use Case 的回傳值，不用「顯示」。同一個 Use Case 的所有 Scenario 裡，同一種錯誤的訊息文字要一致（腳本會對 Then 步驟裡的引號字串做比對並 warn，見 GH-07）。
- Given / When / Then / And 步驟不得出現「點擊、輸入、拖曳、顯示、畫面、按鈕、視窗、對話框、提示我」（建議新增檢查 GH-08，error）。
- Gherkin 步驟裡**不用反引號**。步驟文字給人讀，ID 的引用只出現在 usecase 區塊、名詞表、角色表與 ui 檔（第 9 節）。
- 多欄位輸入使用資料表：

**格式範例**（Scenario 步驟中的資料表）：

```gherkin
And 我建立卡片，欄位如下：
  | 欄位     | 內容                     |
  | 描述     | 設計符合品牌風格的登入頁面 |
  | 負責人   | 小明                     |
```

- 順序類的驗證明確列出完整順序：`Stage 順序應該變為 "待辦"、"進行中"、"驗收中"、"完成"`。

---

## 4. Aggregate 標記

每個 Scenario 上方以 Gherkin 註解標記會存取哪些 Aggregate 以及存取方式。

**格式範例**（Aggregate 標記，放在 tag 之後、`Scenario:` 之前）：

```gherkin
# Related aggregate:
#   swimlane: read, write
#   card: read, write
Scenario: ...
```

規則：

- 註解裡的名稱**必須是「名詞定義／實體」表的 ID**，不得用別名或大小寫變體。
- 只列出實際會用到的實體，沒用到的整行省略。
- `read`：需要先查詢既有資料才能決定如何寫入或驗證（重新命名要先找到對象、排序要先讀現有順序、刪除前要確認是否為最後一個、或是否還有卡片存在）。
- `write`：會真正新增 / 修改 / 刪除該實體的資料。
- **與 usecase 區塊的 `crud` 必須一致**：註解裡出現的實體都必須是所屬 Use Case（由 `@uc-` tag 決定）`crud` 的 key；註解標 `write` 的實體，`crud` 必須含 C／U／D。成功 Scenario（沒有 `@fail-`）另外要求：`crud` 含 C／U／D 的實體，註解必須標 `write`。失敗 Scenario 只讀不寫，所以只標 `read`。衝突時以 usecase 區塊為準，腳本回報 error。
- 各模組規格文件的「Aggregate 標記說明」段落自行列出該文件用到的實體；新增實體時同步更新實體表與該段落。
- 註解放在 tag 之後、`Scenario:` 之前（見 5.3 範例）。

---

## 5. 變更管理：Tag 規範

規格一旦進入開發，之後的新增與修改都必須掛 tag。tag 寫在 Aggregate 註解之前、獨立一行。

### 5.1 Tag 一覽

| Tag | 意義 | 何時移除 |
|-----|------|----------|
| `@wip` | 尚未開發 | 開發完成、驗收通過後移除 |
| `@added` | 規格進入開發後新增的 Scenario | 驗收通過後移除 |
| `@changed` | 用來取代某個既有 Scenario 的新版本 | 驗收通過、對應的 `@deprecated` 刪除後移除 |
| `@deprecated` | 即將被取代的舊 Scenario，仍是目前線上行為 | 新版驗收通過後整個 Scenario 刪除 |
| `@CR-<編號>` | 變更單 / 票號，標示來源 | 保留，作為歷史線索 |
| `@uc-<id>` | 此 Scenario 驗證的 Use Case，**每個 Scenario 必掛、恰好一個** | 永不移除 |
| `@fail-<pN>` | 此 Scenario 驗證「該 Use Case 的前置條件 `pN` 不成立」時的失敗行為 | 永不移除 |

驗收通過後的最終狀態：只剩 `@CR-xxx`、`@uc-`、`@fail-`，其餘狀態 tag 全部移除。首版規格（檔頭 `狀態：草稿`）不掛狀態 tag 與 `@CR-`，但 `@uc-`、`@fail-` 一開始就要掛；usecase 區塊也從第一版就要齊全。

**tag 順序固定**（同一行、半形空格分隔）：

```
狀態 tag（@added／@changed／@deprecated，再接 @wip） → @CR-xxx → @uc-<id> → @fail-<pN>
```

**`@uc-` 與 `@fail-` 的規則**：

- 一個 Use Case 至少要有一個成功 Scenario（沒有 `@fail-` 的那種）。
- usecase 區塊 `fail` 裡的每個 key，至少要有一個掛 `@fail-<key>` 的 Scenario；`@fail-` 指向的 key 必須存在於該 Use Case 的 `pre`。
- 一個 Scenario 至多一個 `@fail-`；要驗證兩個前置條件同時不成立時，拆成兩個 Scenario。
- `@deprecated` 與 `@changed` 的兩個 Scenario 掛同一個 `@uc-`；usecase 區塊寫的是**新版**行為。

**掛 `@CR-xxx` 前的必要條件**：該編號必須已經在 `.dev/CR.md` 總表登記（見 `cr-convention.md` 第 2、6 節）。規格 PR 裡出現的每一個 `@CR-xxx` 都要能在 `.dev/CR.md` 找到對應的一筆，找不到就不能合併——沒有登記的 CR 不算變更單，不能作為規格變更的依據。

### 5.2 情境 A：新增 Scenario

在既有 Feature 中追加，掛 `@added @wip @CR-xxx @uc-<id>`。若是新的 Use Case，同一個 PR 在 usecase 區塊加入該項目。

**格式範例**（新增 Scenario，CR-023 Swimlane 顏色標記，對應 2.4 的 `uc-set-swimlane-color`）：

```gherkin
  @added @wip @CR-023 @uc-set-swimlane-color
  # Related aggregate:
  #   swimlane: read, write
  Scenario: 為 Swimlane 設定顏色標記
    Given 看板中存在一個 Swimlane "緊急項目"
    When 我將該 Swimlane 的顏色設定為 "紅色"
    Then 該 Swimlane 的顏色應該為 "紅色"
```

### 5.3 情境 B：變更既有 Scenario

不原地修改。舊的掛 `@deprecated`，新的掛 `@changed`，兩者同一票號、同一 `@uc-`，並排放置。usecase 區塊直接改成新版行為（改動內容由 CR 影響範圍與 PR diff 追蹤）。

**格式範例**（變更既有 Scenario，CR-024，對應 2.4 的 `uc-delete-swimlane`）：

```gherkin
  @deprecated @CR-024 @uc-delete-swimlane
  # Related aggregate:
  #   swimlane: read, write
  #   card: read, write
  Scenario: 刪除包含卡片的 Swimlane 會一併刪除卡片
    Given 看板中存在一個 Swimlane "本週優先"，其中包含 3 張卡片
    When 我刪除該 Swimlane
    Then Swimlane "本週優先" 不存在
    And 原屬該 Swimlane 的 3 張卡片不存在

  @changed @wip @CR-024 @uc-delete-swimlane
  # Related aggregate:
  #   swimlane: read, write
  #   card: read, write
  Scenario: 刪除包含卡片的 Swimlane 需要先轉移卡片
    Given 看板中存在一個 Swimlane "本週優先"，其中包含 3 張卡片
    And 看板中存在一個 Swimlane "預設泳道"
    When 我刪除該 Swimlane 並指定目的 Swimlane 為 "預設泳道"
    Then Swimlane "本週優先" 不存在
    And 這 3 張卡片應該屬於 "預設泳道"

  @added @wip @CR-024 @uc-delete-swimlane @fail-p2
  # Related aggregate:
  #   swimlane: read
  #   card: read
  Scenario: 未指定目的 Swimlane 時不可刪除包含卡片的 Swimlane
    Given 看板中存在一個 Swimlane "本週優先"，其中包含 3 張卡片
    When 我刪除該 Swimlane 而未指定目的 Swimlane
    Then 拒絕，訊息為 "請選擇接收卡片的目的 Swimlane"
    And Swimlane "本週優先" 與其中的 3 張卡片都仍然存在
```

開發者看到同票號的 `@deprecated` + `@changed` 就知道「這條是要蓋掉那條」；`@fail-p2` 則指向 usecase 區塊 `pre.p2`「目的泳道已指定」不成立時的行為。

### 5.4 情境 C：移除既有 Scenario（功能下架）

掛 `@deprecated @CR-xxx @uc-<id>`，並在變更紀錄註明「移除」。新版上線後刪除；若整個 Use Case 下架，同時刪除 usecase 區塊的該項目，並確認沒有其他 Use Case `requires` 它 `emits` 的事件、沒有 ui 檔引用它。

### 5.5 測試篩選

若有自動化測試，CI 以 tag 篩選：

- 一般 CI：`--tags "not @wip"`
- 檢視待開發清單：`--tags "@wip"`
- 檢視某張票影響範圍：`--tags "@CR-024"`
- 檢視某個 Use Case 的全部情境：`--tags "@uc-delete-swimlane"`
- 只跑失敗路徑：`--tags "@fail-p1 or @fail-p2"`（依 usecase 區塊的 key）

---

## 6. 變更流程

1. **開變更單（CR）**：依 `cr-convention.md` 在 `.dev/CR.md` 總表登記，說明為什麼改、影響哪些 ID（Entity、UseCase、Screen）、驗收標準。編號即為 tag 用的 `@CR-xxx`。
2. **修改規格並提 PR**：依第 5 節掛 tag，需要時同步修改 usecase 區塊與名詞表。PR 的 diff 就是變更清單，reviewer 只看 diff；`spec-check` 與 `cr-check` 在 CI 跑過才能合併。
3. **更新變更紀錄**：在文件的「變更紀錄」區段追加一行。
4. **開發完成、驗收通過**：移除狀態 tag（`@wip` / `@added` / `@changed`）、刪除 `@deprecated` Scenario，再提一次 PR。

### 變更紀錄格式

**格式範例**（規格檔內「變更紀錄」區段，對應第 2 節文件結構模板中的同名區段）：

```markdown
## 變更紀錄

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-12 | CR-023 | 新增 | Swimlane 顏色標記（`uc-set-swimlane-color`、`swimlane.color`） |
| 2026-09-12 | CR-024 | 變更 | 刪除含卡片的 Swimlane 改為轉移卡片，不再一併刪除（`uc-delete-swimlane`） |
```

類型固定為：新增 / 變更 / 移除。摘要裡提到的 ID 用反引號包住，腳本會檢查它們存在。

---

## 7. 何時該開新 Feature 檔

| 情況 | 做法 |
|------|------|
| 新增內容自成一塊、有自己的角色 / 目的（例如「代理人機制」「WIP 限制」） | 開新 `## Feature:` 區塊或新文件，在原 Feature 的簡介補一行「相關功能見 xxx」。原內容完全不動 |
| 新增的 Scenario 散落在既有 Feature 的職責範圍內（例如 Swimlane 多一個顏色屬性） | 原 Feature 內追加，掛 `@added`；新 Use Case 加進該 Feature 的 usecase 區塊 |
| 修改既有行為 | 原 Feature 內以 `@deprecated` + `@changed` 並存 |
| 新增內容需要新的實體 | 一律開新 Feature，並更新實體表與「Aggregate 標記說明」 |
| 單一 Feature 超過約 15 個 Scenario 或約 8 個 Use Case | 考慮依子功能拆檔 |

判斷原則：如果新內容可以用「身為 / 我想要 / 以便」獨立寫出一段合理的 Feature 標頭，就拆；不行就併入原 Feature。

---

## 8. 提交前檢查清單

### 8.1 由腳本檢查（提交前跑 `spec-check`，CI 也會跑）

以下項目**不用人工逐條看**，跑 `spec-check` 就會回報，檢查 ID 見 `checks.md`：

| 原本的人工項目 | 改由 |
|------|------|
| 所有領域名詞都在「名詞定義」中（反引號 ID 部分） | REF-01～REF-05 |
| 每個 Scenario 都有 Aggregate 註解、名稱是實體 ID | GH-02、REF-02 |
| Feature 標頭三行齊全、角色名稱在角色表 | GH-03、REF-03 |
| 進入開發後的新增 / 變更都掛了 `@CR-xxx` 與狀態 tag | GH-05 |
| 每個 `@CR-xxx` 都已在 `.dev/CR.md` 總表登記 | REF-06 |
| `@changed` 一定有對應的 `@deprecated`，且票號相同 | GH-04 |
| 每個 Scenario 有 `@uc-`、`@fail-` 對得上 `pre`、tag 順序正確 | GH-01、UC-04、GH-04 |
| Aggregate 註解與 `crud` 一致 | GH-06 |
| usecase 區塊格式、必填欄位、事件配對、無循環、cardinality 字面檢查 | UC-01～UC-14 |
| 同一 Use Case 內錯誤訊息文字一致 | GH-07（warn） |

### 8.2 人工判斷（腳本做不到的）

- [ ] Scenario 全文不含 UI 操作（禁字由 GH-08 擋，這裡看語意上是否仍在描述互動）
- [ ] 錯誤 / 確認訊息是完整固定文字，不是「顯示錯誤」這種概述
- [ ] usecase 區塊的 `pre` 各句互斥、沒有重疊或包含
- [ ] 每個 Scenario 的 Given／Then 真的體現對應的 `pre`／`post`
- [ ] `fail` 沒有漏掉使用者實際會遇到的失敗情況
- [ ] 「其他名詞」表裡的東西確實不是實體（不會被單獨建立、修改、刪除）
- [ ] 變更紀錄已追加，摘要與 CR 的變更內容一致
- [ ] 已決定的待釐清項目已移除並轉為 Scenario

以上人工項目的 LLM review 提問方式見 `llm-review.md`。

---

## 9. ID 與反引號規則

規格裡只有六種有 ID 的東西。ID 一律小寫英文、數字、連字號，**全專案唯一**，在任何自由文字裡出現時**必須用反引號包住**——腳本只認反引號裡的內容，反引號外的文字一律不解析；反過來說，反引號裡只能放 ID，不得放其他東西（程式碼片段、欄位值、路徑都不行）。

| 概念 | ID 形式 | 定義在哪 | 範例 |
|---|---|---|---|
| Entity（aggregate 樹裡的任一實體，樹根是 aggregate root） | 純英文小寫，與 Aggregate 註解用的名稱相同 | spec「## 名詞定義」實體表 | `swimlane` |
| Attribute | `<entity>.<attr>` | spec「## 名詞定義」欄位表 | `card.swimlane` |
| Role | `r-` 前綴 | spec「## 角色定義」 | `r-board-admin` |
| UseCase（一個交易，不是跨步驟的 UML 使用案例） | `uc-` 前綴 | spec 每個 Feature 的 usecase 區塊 | `uc-delete-swimlane` |
| Event | `ev-` 前綴 | 由 UseCase 的 `emits` 宣告，不另立段落 | `ev-swimlane-deleted` |
| Screen | `s-` 前綴 | ui 每個畫面標題 | `s-swimlane-list` |

規則：

- 沒有 Operation、UserStory、Constraint 這些獨立概念。前置／後置條件是 UseCase 的欄位；user story 就是 Feature 標頭那三行。跨多個 Use Case 的操作序列（多步驟流程）由 `ui-<模組>.md` 的「進入與離開」與「流程」類型畫面表達，spec 不描述。
- 腳本依前綴判斷種類：`r-`、`uc-`、`ev-`、`s-` 四種前綴保留給對應概念，實體 ID 不得以這四個前綴開頭；含 `.` 的視為 Attribute，其餘視為 Entity。
- 反引號 ID 會出現的地方：usecase 區塊的 `pre`／`post`／`fail`、名詞表與角色表以外的說明文字、變更紀錄摘要、ui 檔的表格與段落、CR 的影響範圍欄位。**Gherkin 步驟裡不出現反引號**（3.4）。
- 引用其他模組的 ID 直接寫同一個 ID；腳本一次讀取所有 `spec-*.md` 與 `ui-*.md` 後才做參照檢查，所以跨檔引用不需要額外宣告。要知道某個 ID 定義在哪個模組，看 `spec-check --report` 的「外部引用」表。
- ID 一旦進入開發就不改名；要改名視為「移除 + 新增」，走 CR。
- 反引號裡的字串若不符合上述任何一種形式（例如 `Instant.now()`、`BoardClock`），腳本回報 error（REF-01）。這類程式碼層級的名稱屬於後端設計文件的範圍，不該出現在規格檔。
- 唯一例外是**檔案路徑**：反引號內容含 `/` 或以 `.md` 結尾（例如 `.dev/F04-board-clock/spec-board-clock.md`）時視為檔案引用，腳本略過不解析。

---

## 變更紀錄

- 名詞定義拆成實體／欄位／關係三張表、新增「角色定義」、每個 Feature 底下新增「Use Case 定義」、段落標題改為固定字串。
- Feature 標頭「身為」後的角色名稱必須在角色表存在。
- Aggregate 註解的名稱必須是實體表的 ID，標記語意與 usecase 區塊的 `crud` 對齊。
- 新增 `@uc-`、`@fail-` tag 與固定順序，範例全部更新。
- 檢查清單拆成「腳本檢查」與「人工判斷」。
- 新增第 9 節「ID 與反引號規則」。
- Gherkin 步驟改為意圖與領域結果語言，禁止 UI 操作用詞，一個 Scenario 限一個 When。
