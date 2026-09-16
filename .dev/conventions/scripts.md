# 規格檢查腳本規格（scripts.md）

三支腳本：`spec-check`、`ui-check`、`cr-check`。它們共用一個解析器，把 `spec-*.md`、`ui-*.md`、`.dev/CR.md` 解析成記憶體內的物件模型，再由各檢查函式（`checks.md` 的每個 ID 一個函式）對模型做檢查。本文件只定義輸入、解析步驟、輸出與執行時機；語言與函式庫的選擇見 `open-questions.md` Q4。

**目前狀態：已實作於 `scripts/`（Python 3.10+、PyYAML），用法見 `scripts/README.md`；既有 `.dev/F*/` 規格尚未遷移到新格式，跑起來會有大量 error（見 `open-questions.md` Q10）。**

---

## 1. 共同規定

### 1.1 解析層與檢查層分開

```
Markdown 檔 ──parser──> Model ──checks[]──> Finding[] ──reporter──> stdout / exit code
```

- **parser** 只負責把固定格式轉成物件，不做任何判斷；解析失敗（例如找不到固定字串標題、YAML 錯誤）以 finding 回報（GH-08、UC-01、DS-01、CR-03），不中斷整個流程。
- **checks** 每條是純函式 `(Model) -> Finding[]`，只讀模型，不讀檔案。新增檢查只加一個函式並在 `checks.md` 登記 ID。
- **reporter** 把 finding 排序（檔案、行號）後輸出；有任何 `error` 就非零退出。

### 1.2 物件模型

所有物件都帶 `loc: {file, line}` 供輸出用。

```
Model
├─ entities:   Entity[]      { id, name, desc, loc }
├─ attributes: Attribute[]   { id, entity, type, constraint, desc, loc }
├─ relations:  Relation[]    { source, target, min, max, desc, loc }
├─ roles:      Role[]        { id, name, desc, loc }
├─ features:   Feature[]     { name, module, role_name, usecases: UseCase[], scenarios: Scenario[], loc }
├─ usecases:   UseCase[]     { id, name, feature, roles[], crud: {entity: "CUD"}, pre: {p1: Sentence}, post: Sentence[], fail: {p1: Sentence}, emits[], requires[], calls_sync[], loc }
├─ events:     Event[]       { id, emitted_by: uc, required_by: uc[] }     ← 由 usecases 推導
├─ scenarios:  Scenario[]    { name, feature, tags: {status, wip, cr, uc, fail}, aggregates: {entity: {read, write}}, steps: Step[], loc }
├─ screens:    Screen[]      { id, name, feature, type, status, sections: {...}, data: Row[], ops: Row[], roles: Row[], nav: {from[], to[], abort}, states: {loading, empty, error, forbidden, variants}, loc }
├─ changelog:  ChangeLogRow[] { date, cr, type, summary, loc }
├─ crs:        CR[]          { id, title, type, status, modules[], impact: {id, action}[], loc }
└─ refs:       Ref[]         { id, kind, loc, context }   ← 全文掃到的每一個反引號 ID
```

`Sentence = { text, ids[] }`，`ids` 是句子裡反引號 ID 的清單。`Ref.kind` 由前綴判定：`r-`／`uc-`／`ev-`／`s-`／含 `.` → attribute／其餘 → entity。

### 1.3 輸出

- 每個 finding 一行：`<檔案>:<行號>: <層級> <檢查 ID>: <訊息>`，訊息模板見 `checks.md`。
- 檔案路徑相對 repo 根目錄。
- 依檔案、行號排序；同一位置多個 finding 依檢查 ID 排序。
- 結尾一行摘要：`{n} error(s), {m} warning(s)`。
- 有 error → exit code 1；只有 warn 或全過 → exit code 0。
- `--format json` 輸出 finding 陣列，欄位同上，給 CI 註解 PR 用。

### 1.4 執行時機

| 時機 | 跑什麼 | 說明 |
|---|---|---|
| pre-commit | `spec-check`、`ui-check` | 只讀工作區，秒級完成；有 error 就擋 commit |
| CI（每個 PR） | 三支都跑 | `cr-check` 需要 PR base（`--base <ref>`）才能算 diff |
| 本機隨時 | `spec-check --report`、`ui-check --report` | 印矩陣看現況，不寫檔 |

### 1.5 檢查 ↔ 腳本對照

以 `checks.md` 最後一節的表為準，實作時每支腳本用一個列舉宣告自己涵蓋的 ID；CI 另跑一個小檢查：三支腳本宣告的 ID 聯集 = `checks.md` 表格裡的 ID 集合，對不上就 fail。

---

## 2. `spec-check`

| 項目 | 內容 |
|---|---|
| 名稱 | `spec-check` |
| 輸入 | `spec-*.md`（預設 `.dev/F*/spec-*.md`，可用位置參數覆寫；`.dev/conventions/` 底下的規範文件不是規格，不在預設範圍）；`--cr .dev/CR.md`（REF-06 用，預設路徑）；`--ui <glob>`（選填，只給 `--report` 印 Screen 欄用，不做 ui 檢查） |
| 涵蓋 | REF-01～REF-06、REF-08（spec 部分）、REF-09、UC-01～UC-14、GH-01～GH-04、GH-05（判定部分）、GH-06～GH-09 |
| 時機 | pre-commit、CI |
| 輸出 | 1.3 的格式；`--report` 另印矩陣（2.2） |

### 2.1 解析步驟

1. **切段**：依 H2／H3 固定字串（`spec-convention.md` §2 表）把檔案切成區段。標題以「固定字串開頭」前綴比對，允許後接括號補充。找不到必要標題或次數不對 → GH-08，該區段視為空、後續檢查照跑。
2. **名詞定義**：解析 `### 實體`、`### 欄位`、`### 關係` 三張 Markdown 表格（第一列為表頭，欄位順序固定）→ `entities`、`attributes`、`relations`。`### 其他名詞` 跳過。
3. **角色定義**：解析表格 → `roles`。
4. **變更紀錄**：解析表格 → `changelog`（只取票號、類型、摘要；摘要裡的反引號進 `refs`）。
5. **每個 `## Feature:` 區段**：
   1. 取 `### Use Case 定義` 底下第一個 ```usecase 區塊，YAML 解析 → `usecases`（掛 `feature`）。YAML 錯誤 → UC-01。`pre`／`post`／`fail` 每句掃反引號 → `Sentence.ids`，同時進 `refs`。
   2. 取第一個 ```gherkin 區塊，逐行解析：
      - `Feature:` 行與其後三行（去縮排）→ `features[].name`、`role_name`。
      - 以 `Scenario:`／`Scenario Outline:` 為單位切 Scenario；往上收集連續的 `#` 註解行（Aggregate 註解）與 `@` 開頭的 tag 行；`Background:` 不算 Scenario。
      - tag 行以空白切分，依前綴歸類到 `tags.status`（`@added`／`@changed`／`@deprecated`）、`tags.wip`、`tags.cr`、`tags.uc`、`tags.fail`；記錄原始順序供 GH-04。
      - Aggregate 註解：第一行必須是 `# Related aggregate:`，其後每行 `#   <name>: <read|write>[, <read|write>]` → `aggregates`。
      - 步驟行（Given／When／Then／And／But）→ `steps`，同時抽出 `"..."` 字串供 GH-07；步驟含反引號 → GH-09。
6. **推導事件**：走訪所有 `usecases` 的 `emits`／`requires` → `events`。
7. **全文掃反引號**：所有非 gherkin 區塊、非 ```usecase 區塊的文字，以及 usecase 句子，反引號內容 → `refs`。Markdown 表格的表頭列與 `ID` 欄本身是定義處，不算引用。
8. 若給了 `--ui`，用 `ui-check` 的解析器讀入 `screens`（只供報表）。

### 2.2 `--report`

從模型即時印到 stdout，**不寫檔、不進版控**。四張表，Markdown 格式，可直接貼進 PR 說明：

| 報表 | 內容 |
|---|---|
| CRUD 矩陣 | 列 = UseCase（依 Feature 分組），欄 = Entity，格子 = `crud` 字串 |
| 角色 × UseCase 矩陣 | 列 = UseCase，欄 = Role，格子 = ✓ |
| 事件表 | 列 = Event，欄 = emits 的 UseCase、requires 的 UseCase 清單 |
| 追溯矩陣 | 列 = Feature → UseCase → 碰到的 Entity（依 crud）→ 觸發它的 Screen（有 `--ui` 才有此欄） |

`--report --cr CR-024` 只印該 CR 影響 ID 相關的列。

---

## 3. `ui-check`

| 項目 | 內容 |
|---|---|
| 名稱 | `ui-check` |
| 輸入 | `ui-*.md`（預設 `.dev/F*/ui-*.md`）+ 對應的 `spec-*.md`（同目錄同模組；用 `spec-check` 的解析器讀入，取得 entities／attributes／roles／usecases／features，不重跑 spec 檢查） |
| 涵蓋 | REF-07、REF-08（ui 部分）、DS-01～DS-08 |
| 時機 | pre-commit、CI |
| 輸出 | 1.3 的格式；`--report` 印畫面總表與含 Screen 欄的追溯矩陣 |

### 3.1 解析步驟

1. **先讀 spec**：對每個 ui 檔找同目錄的 `spec-<模組>.md`，找不到 → error（DS-01 的 detail）。用 spec 解析器建模型，只用結果、不重報 spec 的 finding。
2. **切畫面**：每個 H2 必須符合 `## s-<id>：<畫面名稱>` → `screens[]`；不符合的 H2（例如檔頭以外的自由標題）→ DS-01。
3. **標頭三行**：標題後緊接的三行 `所屬 Feature：`、`類型：`、`狀態：` → `feature`、`type`、`status`。
4. **八個 H3**：依固定字串切 `sections`，順序與齊全 → DS-02。
5. **資料表**：解析 Markdown 表格（欄位順序：欄位、來源、顯示／輸入、驗證／格式、說明）→ `data[]`，「來源」欄的反引號 → `refs`（kind 限定 attribute／entity，DS-03）。
6. **操作表**：（操作、觸發、成功後、失敗時、需確認？）→ `ops[]`，「觸發」欄反引號 → `refs`（kind 限定 uc，DS-03）。
7. **角色表**：（角色、看得到、做得到）→ `roles[]`，「角色」欄反引號 → `refs`（kind 限定 role）。「做得到」欄以 `、` 切成操作名稱清單，供 DS-05 對照操作表的「操作」欄。
8. **進入與離開**：三個項目符號行，反引號 → `nav.from`／`nav.to`（kind 限定 screen）；「從哪裡進來」含「模組入口」字樣 → `nav.entry = true`。
9. **狀態段**：五個項目符號行，以「載入中：」等固定前綴比對 → `states`；缺項或冒號後空白 → DS-04。
10. **全文掃反引號**：整個 ui 檔的反引號 → `refs`（REF-07）。同時掃是否出現 `### 實體`、`## 角色定義`、```usecase 等 spec 專屬結構 → DS-08。

### 3.2 `--report`

| 報表 | 內容 |
|---|---|
| 畫面總表 | Screen ID、名稱、所屬 Feature、類型、觸發的 UseCase 清單、狀態；取代 `ui-design-convention.md` 舊版的手寫總表 |
| 追溯矩陣 | 同 `spec-check --report` 的追溯矩陣，Screen 欄必有 |
| 未被畫面觸發的寫入 UseCase | DS-06 的 warn 清單，方便盤點時看 |

---

## 4. `cr-check`

| 項目 | 內容 |
|---|---|
| 名稱 | `cr-check` |
| 輸入 | `.dev/CR.md`；所有 `spec-*.md`、`ui-*.md`（工作區版本，用前兩支的解析器）；`--base <git ref>`（PR base，必填）；`--head <git ref>`（預設工作區） |
| 涵蓋 | GH-05（diff 部分）、CR-01～CR-05 |
| 時機 | CI（需 PR base）；本機可用 `--base main` 手動跑 |
| 輸出 | 1.3 的格式 |

### 4.1 解析步驟

1. **解析總表**：`.dev/CR.md` 第一個 Markdown 表格，欄位順序依 `cr-convention.md` §6 → `crs[]`。「影響 ID」欄以 `、` 切分，每項 `` `id` `` 後可接 `(新增)`／`(移除)` → `impact[].action`。格式錯 → CR-03。
2. **取 diff**：`git diff <base>...<head> -- '.dev/**/spec-*.md' '.dev/**/ui-*.md' '.dev/CR.md'`，得到每個檔案被改動的行號區間。
3. **兩個版本各建一次模型**：base 版與 head 版分別跑 spec／ui 解析器（base 版從 `git show <base>:<path>` 讀）。
4. **由行號映射到 ID**：對 head 模型，每個 usecase 項目、實體表列、畫面段落、Scenario 都有 `loc` 與行數範圍；改動行落在哪個範圍，就算該 ID 被改。Scenario 被改 → 歸到它 `@uc-` 的 ID。base 有、head 沒有的 ID → 視為「移除」；反之「新增」。
5. **找出 PR 涉及的 CR**：head 模型裡新出現（base 沒有）的 `@CR-xxx` tag，加上 `--cr` 參數明確指定的，就是要比對的 CR 集合。沒有任何 CR 而 diff 動到了 usecase 區塊／實體表／畫面段落，且該檔已進入開發 → GH-05。
6. **比對**（CR-01）：對每張 CR，`impact` 的 ID 集合 vs 步驟 4 算出的改動 ID 集合，集合差異兩邊都報。
7. **狀態檢查**：CR-04（狀態 vs 影響 ID 非空且存在）、CR-05（diff 新出現的 `@CR-` 其總表狀態必須是「修改規格」）。
8. **並行衝突**（CR-02）：head 模型裡同一 Feature 的 `@changed` Scenario，若兩個對應同一個 `@deprecated`（同 `@uc-`、同 Scenario 名稱），且兩者的 CR 都是進行中 → 報。

### 4.2 「已進入開發」判定

GH-05 與 `cr-check` 步驟 5 都需要判定某個 spec 檔是否已進入開發。判定：spec 檔頭的 `狀態：` 行為「開發中」（`spec-convention.md` §2.1）；缺少此行或值不是「草稿」／「開發中」由 GH-08 報。實作為 `SpecFile.in_development`，三支腳本共用。

---

## 5. 不在本次範圍

- 腳本語言、YAML 與 Markdown 表格解析函式庫（open-questions Q4）。
- Gherkin 完整語法解析（只解析本規範用到的子集：Feature 標頭、tag 行、註解、Scenario 標題、步驟關鍵字、引號字串；Scenario Outline 的 Examples 表格照原文略過）。
- 把 `.feature` 檔從規格檔匯出（另一支工具，不做檢查）。
- 對 `design.md`（後端領域設計文件）的任何解析；它不是本規範的固定格式檔案。
