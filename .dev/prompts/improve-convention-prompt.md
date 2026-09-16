# 提示詞：規範修訂 — 讓 spec 與 design 檔可被腳本解析與檢查

> 用法：把這份提示詞連同 `spec-convention.md`、`cr-convention.md`、`ui-design-convention.md` 交給 Claude Code。它會產出修訂後的三份規範、檢查清單、腳本規格、LLM review 清單，以及待你決定的問題。

---

## 你的角色

你是負責維護規格規範的架構師。目標：**規格只有兩種檔案**——`spec-<模組>.md` 與 `design-<模組>.md`——腳本直接解析這兩種檔案做矛盾檢查。不另外維護本體論資料檔，不產生推導出來的矩陣檔進版控。

修訂既有規範，不重寫。章節編號不動，新增內容加在對應章節或另開章節。每處修改都要對應到下面某一條設計決策，沒有對應的不要改。

---

## 第一部分：設計決策（已定案，照做）

### D1. 三段式檢查
1. 作者在 spec／design 檔裡用**固定格式**寫規則（D3、D4）。
2. 腳本解析固定格式，做參照完整性與矛盾檢查，全部通過才進下一步。
3. LLM 只做語意 review：要讀懂內容才能判斷的東西。

腳本能做的不交給 LLM；LLM 不重做腳本做過的檢查。

### D2. 概念與 ID（核心規範）

規格裡只有六種有 ID 的東西。ID 一律小寫英文、數字、連字號，全專案唯一，**在任何自由文字裡出現時必須用反引號包住**——腳本只認反引號裡的內容，反引號外的文字一律不解析。

| 概念 | ID 形式 | 定義在哪 | 範例 |
|---|---|---|---|
| Entity（= Aggregate） | 純英文小寫，與 Aggregate 註解用的名稱相同 | spec「## 名詞定義」實體表 | `swimlane` |
| Attribute | `<entity>.<attr>` | spec「## 名詞定義」欄位表 | `card.swimlane` |
| Role | `r-` 前綴 | spec「## 角色定義」 | `r-board-admin` |
| UseCase（一個交易，不是跨步驟的 UML 使用案例） | `uc-` 前綴 | spec 每個 Feature 的 usecase 區塊 | `uc-delete-swimlane` |
| Event | `ev-` 前綴 | 由 UseCase 的 `emits` 宣告，不另立段落 | `ev-swimlane-deleted` |
| Screen | `s-` 前綴 | design 每個畫面標題 | `s-swimlane-list` |

沒有 Operation、UserStory、Constraint 這些獨立概念。前置／後置條件是 UseCase 的欄位；user story 就是 Feature 標頭那三行。

### D3. spec-<模組>.md 的固定格式（核心規範）

**段落標題是固定字串，不得改寫**，腳本靠它們定位：`## 名詞定義`、`## 角色定義`、`## Aggregate 標記說明`、`## 變更紀錄`、`## Feature:`、`### Use Case 定義`、`## 待釐清`。

在既有結構上加三樣東西，其餘不動：

**(1) 名詞定義改成三張表**

```markdown
## 名詞定義

### 實體
| ID | 名詞 | 說明 |
|---|---|---|
| swimlane | Swimlane（泳道） | 看板上的水平分區 |
| card | Card（卡片） | 工作項目 |

### 欄位
| ID | 型別／格式 | 限制 | 說明 |
|---|---|---|---|
| swimlane.name | string(50) | 非空、同看板內唯一 | 泳道名稱 |
| card.swimlane | ref swimlane | 必填 | 所屬泳道 |

### 關係
| 來源 | 目標 | min | max | 說明 |
|---|---|---|---|---|
| swimlane | card | 0 | n | 一個泳道有多張卡片 |
```

實體表的 ID 就是 Aggregate 註解裡用的名稱；欄位表的 ID 一律 `entity.attr`；關係表的來源／目標必須是實體 ID。非實體的領域名詞（例如「WIP 限制」這種規則名）另外一張「其他名詞」表，沒有 ID，腳本不解析。

**(2) 角色定義**

```markdown
## 角色定義
| ID | 名稱 | 說明 |
|---|---|---|
| r-board-admin | 看板管理者 | 可管理泳道與階段 |
| r-member | 成員 | 可操作卡片 |
```

Feature 標頭「身為 <角色>」寫角色**名稱**，腳本以名稱反查 ID；名稱必須在此表存在。

**(3) 每個 Feature 底下、Gherkin 之前，一個 `### Use Case 定義` 段落**，內含一個 ```` ```usecase ```` 區塊，內容是 YAML，一個 UseCase 一個項目：

````markdown
### Use Case 定義
```usecase
- id: uc-delete-swimlane
  name: 刪除 Swimlane
  roles: [r-board-admin]
  crud: {swimlane: D, card: U}
  pre:
    p1: "`swimlane` 存在"
    p2: "`card.swimlane` 的目的泳道已指定"
  post:
    - "`swimlane` 不存在"
    - "原屬該泳道的 `card.swimlane` 更新為目的泳道"
  fail:
    p2: "拒絕，資料不變"
  emits: [ev-swimlane-deleted]
  requires: []
  calls-sync: []
```
````

欄位規則：
- `id`、`name`、`roles`、`crud`、`pre`、`post` 必填；`fail`、`emits`、`requires`、`calls-sync` 可空但欄位要在。
- `crud` 的 key 是實體 ID，value 是 `C`、`R`、`U`、`D` 的組合字串（例如 `RU`）。
- `pre` 是 map，key 固定 `p1`、`p2`…；`post` 是 list；`fail` 是 map，key 必須是 `pre` 裡存在的 key。
- 每一句 `pre`、`post`、`fail` 至少含一個反引號 ID。
- `requires` 列的事件必須有某個 UseCase `emits` 它；`calls-sync` 列 UseCase ID，代表交易內同步呼叫，屬例外情況。
- 跨 UseCase 的順序只由 `emits`／`requires` 表達，不另畫序列圖。

**(4) Scenario 的 tag**

- 每個 Scenario 掛 `@uc-<id>`。
- 失敗 Scenario 額外掛 `@fail-<pN>`，指向它驗證的那句前置條件不成立。
- 一個 UseCase 至少一個成功 Scenario（沒有 `@fail-`）；`fail` 裡每個 key 至少一個對應的失敗 Scenario。
- Aggregate 註解與 `crud` 必須一致：註解標 write 的實體，`crud` 必須含 C／U／D；`crud` 含 C／U／D 的實體，註解必須標 write。衝突時以 usecase 區塊為準，視為錯誤。
- tag 順序固定：狀態 tag（`@wip`／`@added`／`@changed`／`@deprecated`）→ `@CR-xxx` → `@uc-` → `@fail-`。

### D4. design-<模組>.md 的固定格式

沿用 `ui-design-convention.md` 的短規格模板，加以下規定讓它可解析：

- 畫面標題格式固定：`## s-<id>：<畫面名稱>`。
- 段落標題固定字串：`### 目的`、`### 進入與離開`、`### 角色與權限`、`### 資料`、`### 操作`、`### 狀態`、`### 驗收條件`、`### 待確認事項`。
- 「資料」表的「來源」欄填 Attribute ID；「操作」表的「觸發」欄填 UseCase ID；「角色與權限」表的「角色」欄填 Role ID；「進入與離開」裡的畫面用 Screen ID。全部用反引號包住。
- 「狀態」段五個項目（載入中、空資料、錯誤、無權限、資料狀態差異）都要有，沒有的寫「不適用」。
- design 檔不定義新的 Entity、UseCase、Role，只引用 spec 檔的 ID。

### D5. CR
`cr-convention.md` 維持現有流程，只改：CR 的影響範圍欄位列出受影響的 ID（Entity、UseCase、Screen），腳本用它對照 PR diff。

---

## 第二部分：要產出的東西

### 1. `spec-convention.md` 修訂
- 第 2 節文件結構：加入角色定義、名詞定義三張表、Feature 底下的 Use Case 定義，更新模板，標明固定字串標題。
- 第 4 節：aggregate 名稱必須是實體表的 ID；標記語意與 D3(4) 對齊。
- 第 5 節：新增 `@uc-`、`@fail-` tag 與固定順序；更新所有範例（沿用 Swimlane／CR-023／CR-024 情境）。
- 新增一節「ID 與反引號規則」，內容即 D2。
- 第 8 節：標示哪些項目改由腳本檢查，清單只留人工判斷項。

### 2. `ui-design-convention.md` 修訂
- 依 D4 加 Screen ID、固定標題、引用規則。
- 畫面總表、追溯矩陣改為「腳本可從 design 檔生成」，不再要求手寫維護。

### 3. `cr-convention.md` 修訂
- 依 D5 改影響範圍欄位。
- 第 4.2 節離開條件加「腳本檢查通過」。
- 第 1 節加一列：改名詞定義、角色定義、usecase 區塊一律開 CR。

### 4. `checks.md`：檢查清單

每項寫 `ID | 層級 | 讀哪個檔的哪一段 | 規則 | 訊息`。層級分 `error`（擋合併）、`warn`（不擋）。ID 依組別編號，例如 `REF-01`、`UC-03`。至少涵蓋以下項目，可以增加：

**REF：參照完整性**
- 反引號裡的每個 ID 都能在定義處找到（Attribute → 欄位表、`r-` → 角色表、`uc-` → usecase 區塊、`s-` → design 標題、`ev-` → 某個 `emits`、純英文 → 實體表）。
- Aggregate 註解的名稱是實體 ID。
- Feature 標頭的角色名稱在角色表。
- 欄位表的 `entity.` 前綴對應存在的實體；關係表來源／目標是實體 ID。
- 每個 `@CR-xxx` 已在 `.dev/CR.md` 登記。

**UC：Use Case 一致性**
- usecase 區塊是合法 YAML 且必填欄位齊全、格式正確。
- `fail` 的 key 都在 `pre` 裡；`pre`／`post`／`fail` 每句至少一個反引號 ID。
- 寫入 UseCase（crud 含 C／U／D）：`roles` 非空、至少一個成功 Scenario、`pre` 與 `post` 非空。
- `fail` 每個 key 至少一個 `@fail-<key>` Scenario；`@fail-` 指向的 key 必須存在。
- `post` 引用的 Attribute 所屬實體，`crud` 必須含 C 或 U。
- 每個 Event 恰好一個 `emits`、至少一個 `requires`（沒有 requires 給 warn）。
- `requires` 建圖無循環；`calls-sync` 建圖無循環。
- 每個實體至少被一個 UseCase 標 C（沒有給 warn）。
- 關係表 max 為 1 的關係，不得有 `post` 描述新增第二筆（只能做字面檢查：post 句含該關係兩端 ID 且含「新增」時 warn）。

**GH：Gherkin 格式（既有規則轉腳本）**
- 每個 Scenario 有 `@uc-` tag、有 Aggregate 註解、Feature 標頭三行齊全。
- Aggregate 註解 vs `crud` 一致（D3(4)）。
- `@changed` 必有同票號 `@deprecated`；tag 順序符合規定。
- 已進入開發的 Scenario 必有 `@CR-xxx`（判定方式見 open-questions）。
- 同一 UseCase 的 Scenario 裡 `"..."` 內的錯誤／確認訊息文字不一致 → warn。

**DS：design 內部與跨檔**
- 畫面標題與段落標題符合固定格式。
- 資料表來源、操作表觸發、角色表、進入與離開的 ID 全部存在於 spec。
- 狀態段五項齊全。
- 操作表觸發的 UseCase，其 `roles` 與畫面「角色與權限」表能做該操作的角色一致。
- 每個寫入 UseCase 至少被一個畫面的操作表引用（沒有給 warn：可能是背景作業或漏畫面）。
- 每個 Screen 至少被一個其他 Screen 的「進入與離開」引用，或是模組入口（warn）。

**CR：變更單**
- CR 影響範圍列的 ID 與 PR diff 實際改動的 ID 一致，多改少改都報。
- 同一 Scenario 不被兩張進行中 CR 同時掛 `@changed`。

### 5. `scripts.md`：腳本規格

三支腳本，每支寫：名稱、輸入、解析步驟、涵蓋的檢查 ID、執行時機、輸出。

| 腳本 | 讀 | 解析什麼 | 涵蓋 | 時機 |
|---|---|---|---|---|
| `spec-check` | `spec-*.md` | 依固定標題切段；解析三張名詞表、角色表；解析每個 usecase 區塊（YAML）；解析 Gherkin 區塊的 tag、Aggregate 註解、Feature 標頭、Then 裡的引號字串；掃全文反引號 ID | REF（spec 部分）、UC、GH | pre-commit、CI |
| `design-check` | `design-*.md` + `spec-*.md` 的解析結果 | 依畫面標題切段；解析資料表、操作表、角色表、狀態段、進入與離開；掃反引號 ID | REF（design 部分）、DS | pre-commit、CI |
| `cr-check` | `.dev/CR.md`、規格檔、PR diff | 解析 CR 總表；從 diff 找出被改動的 usecase 區塊、實體、畫面的 ID；比對 | CR | CI（需 PR base） |

共同規定：
- 解析層與檢查層分開：三支腳本共用一個解析器，解析結果是一個記憶體內的物件模型（entities、attributes、roles、usecases、events、scenarios、screens 與它們之間的引用），每條檢查是一個對這個模型的函式。這樣新增檢查不用動解析。
- 輸出格式 `<檔案>:<行號>: <層級> <檢查 ID>: <訊息>`；有 error 就非零退出。
- `spec-check --report` 從物件模型即時印出 CRUD 矩陣、角色 × UseCase 矩陣、事件表、追溯矩陣（Feature → UseCase → Entity → Screen），不寫檔、不進版控。
- `checks.md` 每個 ID 對到一支腳本，反之亦然；對不上就是規範自己的矛盾。
- 腳本語言、YAML 與 Markdown 表格的解析函式庫不在這次範圍，列進 open-questions。

### 6. `llm-review.md`
只放腳本做不到的：`pre` 各句之間互斥或重疊、Scenario 的 Given／Then 是否真的體現對應 `pre`／`post`、不同 UseCase 對同一概念的措辭不一致、CR 驗收標準能否一對一轉 Then、`fail` 是否遺漏使用者實際會遇到的情況、`post` 與關係表 cardinality 的語意衝突（腳本只做字面 warn 的那些）。每項附一個範例提問。

### 7. `open-questions.md`
每點 2–3 個選項與建議。至少回答：
- 「已進入開發」的機械判定方式（變更紀錄非空？特定 tag？）。
- `pre`／`post` 句子要不要限制固定句型，方便日後轉表達式。
- 首版規格（尚未進入開發）是否要求 usecase 區塊齊全，還是允許先寫 Gherkin 後補。
- 腳本語言與解析函式庫。

---

## 限制
- 三份修訂檔完整輸出，檔頭附「本次修訂摘要」對應設計決策。
- 範例一律沿用 Swimlane、看板、CR-023／CR-024 情境，且 spec 與 design 的範例要互相引用得上。
- 不引入這裡沒提到的新概念（獨立本體論檔、生成檔目錄、狀態機矩陣、決策表）。
- 繁體中文台灣用語，格式與既有規範一致。
- 最後列出這套規範一上線最先會被違反的三條規則與原因。