# 規格檢查清單（checks.md）

腳本對 `spec-<模組>.md`、`ui-<模組>.md`、`.dev/CR.md` 做的所有檢查。每一項對應到 `scripts.md` 的一支腳本，反之亦然；對不上就是規範自己的矛盾。

- **層級**：`error` 擋合併（腳本非零退出）；`warn` 不擋，只列出。
- **ID 編號**：依組別流水，新增檢查往後加，不重排、不重用。
- **訊息**：腳本輸出格式 `<檔案>:<行號>: <層級> <檢查 ID>: <訊息>`，訊息欄是固定模板，`{}` 內是實際值。
- 所有檢查都跑在 `scripts.md` 定義的物件模型上，不直接讀文字；「讀哪個檔的哪一段」是說明來源，不是實作方式。

規範出處：`spec-convention.md`（簡寫 SC）、`ui-design-convention.md`（UDC）、`cr-convention.md`（CRC）。

---

## REF：參照完整性

| ID | 層級 | 讀哪個檔的哪一段 | 規則 | 訊息 |
|---|---|---|---|---|
| REF-01 | error | 所有 `spec-*.md` 全文的反引號（gherkin 區塊除外） | 反引號內容必須符合 SC §9 六種 ID 形式之一，且能在定義處找到：`r-` → 角色表、`uc-` → 任一 usecase 區塊、`ev-` → 任一 usecase 的 `emits`、`s-` → 任一 ui 檔畫面標題、含 `.` → 欄位表、其餘 → 實體表。找不到或形式不合都是 error。反引號內容含 `/` 或以 `.md` 結尾視為檔案路徑，略過 | `反引號 ID "{id}" 未定義（種類：{kind}）` |
| REF-02 | error | spec 每個 Scenario 的 `# Related aggregate:` 註解 | 註解每一行的名稱必須是實體表的 ID | `Aggregate 註解 "{name}" 不是實體 ID` |
| REF-03 | error | spec 每個 gherkin 區塊 Feature 標頭第一行 | 「身為 」後的字串必須與角色表某一列的「名稱」完全相同 | `Feature 標頭角色 "{name}" 不在角色定義表` |
| REF-04 | error | spec 欄位表 `ID` 欄 | `<entity>.<attr>` 的 `<entity>` 必須是實體表的 ID；`<attr>` 非空 | `欄位 "{id}" 的實體 "{entity}" 不存在` |
| REF-05 | error | spec 關係表 `來源`、`目標` 欄 | 兩者都必須是實體表的 ID；`min` 為整數、`max` 為整數或 `n` | `關係 {source}→{target} 的 "{side}" 不是實體 ID` |
| REF-06 | error | spec 每個 `@CR-xxx` tag 與變更紀錄「票號」欄 | 編號必須在 `.dev/CR.md` 總表存在 | `@CR-{n} 未在 .dev/CR.md 登記` |
| REF-07 | error | 所有 `ui-*.md` 全文的反引號 | 同 REF-01，但 `s-` 可指向本檔或其他 ui 檔的畫面標題；ui 檔不得定義 `s-` 以外的任何 ID | `ui 檔引用的 ID "{id}" 未定義（種類：{kind}）` |
| REF-08 | error | 所有 spec／ui 檔的定義處（實體表、欄位表、角色表、usecase `id`、`emits`、畫面標題） | 同一個 ID 全專案只能定義一次；跨檔重複定義是 error（跨模組引用直接用 ID，不重定義） | `ID "{id}" 重複定義於 {file1}:{line1} 與 {file2}:{line2}` |
| REF-09 | error | 實體表 `ID` 欄 | 實體 ID 只能是小寫英文、數字、連字號，且不得以 `r-`、`uc-`、`ev-`、`s-` 開頭、不含 `.` | `實體 ID "{id}" 格式不合法` |

---

## UC：Use Case 一致性

| ID | 層級 | 讀哪個檔的哪一段 | 規則 | 訊息 |
|---|---|---|---|---|
| UC-01 | error | 每個 Feature 的 ```usecase 區塊 | 合法 YAML、頂層是 list；每項 `id`（`uc-` 前綴）、`name`、`roles`（list）、`crud`（map，value 只含 `CRUD` 字母且不重複）、`pre`（map，key 為 `p1`…連號）、`post`（list）必填；`fail`（map）、`emits`、`requires`、`calls-sync`（list）欄位必須存在、可空；不得有多餘欄位。每個 Feature 恰好一個 `### Use Case 定義` 與一個 usecase 區塊 | `usecase 區塊格式錯誤：{detail}` |
| UC-02 | error | usecase `fail` | 每個 key 必須存在於同一項的 `pre` | `uc "{uc}" 的 fail.{key} 在 pre 中不存在` |
| UC-03 | error | usecase `pre`、`post`、`fail` 每一句 | 至少含一個反引號 ID（`fail` 句允許固定句「拒絕，資料不變」不含 ID） | `uc "{uc}" 的 {field} 句 "{text}" 沒有引用任何 ID` |
| UC-04 | error | Scenario 的 `@fail-<key>` tag | `<key>` 必須存在於該 Scenario `@uc-` 所指 Use Case 的 `fail` 裡 | `Scenario "{name}" 的 @fail-{key} 在 uc "{uc}" 的 fail 中不存在` |
| UC-05 | error | usecase `fail` × 所有 Scenario tag | `fail` 每個 key 至少一個掛 `@uc-<id> @fail-<key>` 的 Scenario | `uc "{uc}" 的 fail.{key} 沒有對應的 @fail-{key} Scenario` |
| UC-06 | error | usecase `crud`、`roles`、`pre`、`post` × Scenario tag | 每個 Use Case 至少一個成功 Scenario（掛 `@uc-` 且無 `@fail-`）；`crud` 含 C／U／D 的 Use Case：`pre`、`post` 非空，且 `requires` 為空（使用者觸發）時 `roles` 也非空 | `uc "{uc}" {detail}`（detail：`沒有成功 Scenario` / `為寫入 Use Case 但 roles 為空` / `…pre 為空` / `…post 為空`） |
| UC-07 | error | usecase `post` 的反引號 Attribute × `crud` | `post` 引用的 `<entity>.<attr>`，其 `<entity>` 在 `crud` 裡必須含 C 或 U | `uc "{uc}" 的 post 更新 "{attr}"，但 crud.{entity} 不含 C/U` |
| UC-08 | error | 所有 usecase 的 `emits` | 每個 `ev-` ID 全專案恰好一個 Use Case `emits` 它（0 個由 REF-01 報，2 個以上由本項報） | `事件 "{ev}" 被 {n} 個 Use Case emits：{ucs}` |
| UC-09 | warn | 所有 usecase 的 `emits`、`requires` | 每個被 `emits` 的事件至少一個 Use Case `requires` 它 | `事件 "{ev}" 沒有任何 Use Case requires` |
| UC-10 | warn | 關係表 `max`=1 的關係 × usecase `post` | `post` 某句同時含該關係的來源與目標實體 ID（或其欄位）且含「新增」時 warn（字面檢查，語意由 LLM review） | `uc "{uc}" 的 post "{text}" 可能違反 {source}→{target} max=1` |
| UC-11 | error | 所有 usecase 的 `requires` × `emits` | 以「A emits e、B requires e」為 A→B 建圖，不得有循環 | `requires 形成循環：{cycle}` |
| UC-12 | error | 所有 usecase 的 `calls-sync` | 以 `calls-sync` 建圖不得有循環；被呼叫的 ID 必須存在（存在性由 REF-01 報） | `calls-sync 形成循環：{cycle}` |
| UC-13 | warn | 實體表 × 所有 usecase `crud` | 每個實體至少被一個 Use Case 標 C | `實體 "{entity}" 沒有任何 Use Case 建立它` |
| UC-14 | error | usecase `roles` | 每個元素必須是 `r-` ID（存在性由 REF-01 報）；`crud` 的 key 必須是實體 ID | `uc "{uc}" 的 {field} 含非法 ID "{id}"` |

---

## GH：Gherkin 格式

| ID | 層級 | 讀哪個檔的哪一段 | 規則 | 訊息 |
|---|---|---|---|---|
| GH-01 | error | 每個 Scenario 的 tag 行 | 恰好一個 `@uc-<id>`，且該 ID 必須在**同一個 Feature** 的 usecase 區塊；至多一個 `@fail-` | `Scenario "{name}" {detail}`（detail：`缺少 @uc- tag` / `有多個 @uc- tag` / `@uc-{id} 不屬於本 Feature` / `有多個 @fail- tag`） |
| GH-02 | error | 每個 Scenario 上方 | 必須有 `# Related aggregate:` 註解，位置在 tag 行之後、`Scenario:` 之前，至少一行 `#   <entity>: read[, write]` | `Scenario "{name}" 缺少 Aggregate 註解` |
| GH-03 | error | 每個 gherkin 區塊開頭 | `Feature:` 後依序三行 `身為 `、`我想要 `、`以便 `；`## Feature:` 標題文字與 gherkin `Feature:` 文字相同 | `Feature "{name}" 標頭不完整：缺少 "{line}"` |
| GH-04 | error | 每個 Scenario 的 tag 行 × 同 Feature 其他 Scenario | tag 順序：狀態 tag（`@added`／`@changed`／`@deprecated` 之一，再接 `@wip`）→ `@CR-` → `@uc-` → `@fail-`；`@added`／`@changed`／`@deprecated` 互斥；`@changed` 必有同 Feature、同 `@CR-`、同 `@uc-` 的 `@deprecated` Scenario；`@wip` 必須伴隨 `@CR-`（首版規格不掛 `@wip`） | `Scenario "{name}" tag 錯誤：{detail}` |
| GH-05 | error | 每個 Scenario 的 tag 行 | 已進入開發的規格檔（檔頭 `狀態：開發中`）裡，掛狀態 tag 的 Scenario 必有 `@CR-`；PR diff 新增或修改的 Scenario 必有 `@CR-`（後者只在 CI 有 base 時檢查，由 `cr-check` 執行） | `Scenario "{name}" 已進入開發卻沒有 @CR- tag` |
| GH-06 | error | Aggregate 註解 × 所屬 Use Case 的 `crud` | 註解出現的實體必須是 `crud` 的 key；註解標 `write` 的實體 `crud` 必須含 C／U／D；成功 Scenario（無 `@fail-`）另外要求 `crud` 含 C／U／D 的實體必須標 `write`。以 usecase 區塊為準 | `Scenario "{name}" 的 Aggregate 註解與 uc "{uc}" 的 crud 不一致：{detail}` |
| GH-07 | warn | 同一 `@uc-` 的所有 Scenario 的 Then／And 步驟中的 `"..."` 字串 | 含「錯誤訊息」「確認訊息」「提示」的步驟裡，引號字串在同一 Use Case 內若有兩個以上互不相同的版本，warn（一個 Use Case 可能有多種錯誤，所以只在字串相似度高、例如編輯距離 ≤ 3 時報） | `uc "{uc}" 的訊息文字不一致："{a}" vs "{b}"` |
| GH-08 | error | spec 檔的 H2／H3 標題 | 第一個 H2 之前恰好一行 `狀態：草稿` 或 `狀態：開發中`；固定字串標題依 SC §2 出現且次數正確：`## 名詞定義`（含 `### 實體`、`### 欄位`、`### 關係`）、`## 角色定義`、`## Aggregate 標記說明`、`## 變更紀錄` 各恰好一次；`## Feature:` 至少一次，每個下面恰好一個 `### Use Case 定義` 且在 gherkin 區塊之前；`## 待釐清` 至多一次 | `文件結構錯誤：{detail}` |
| GH-09 | error | gherkin 區塊內 | 步驟文字不得含反引號（ID 引用不出現在 Gherkin） | `Scenario "{name}" 的步驟含反引號` |

---

## DS：ui 內部與跨檔

| ID | 層級 | 讀哪個檔的哪一段 | 規則 | 訊息 |
|---|---|---|---|---|
| DS-01 | error | ui 檔每個 H2 | 格式 `## s-<id>：<畫面名稱>`（全形冒號）；標題後緊接三行 `所屬 Feature：`、`類型：`（七種之一）、`狀態：`（未討論／討論中／已定案）；`所屬 Feature` 的名稱必須是對應 spec 檔的某個 `## Feature:` | `畫面標題或標頭格式錯誤：{detail}` |
| DS-02 | error | 每個畫面段落 | 八個 H3 依固定順序齊全：目的、進入與離開、角色與權限、資料、操作、狀態、驗收條件、待確認事項 | `畫面 "{screen}" 缺少段落 "{section}" 或順序錯誤` |
| DS-03 | error | 資料表「來源」欄、操作表「觸發」欄、角色表「角色」欄、進入與離開的反引號 | 種類必須正確：來源 → Attribute 或 Entity ID；觸發 → `uc-` ID 或 `—`；角色 → `r-` ID；進入與離開 → `s-` ID 或「模組入口」。存在性由 REF-07 報 | `畫面 "{screen}" 的 {table}.{column} "{value}" 種類錯誤，應為 {kind}` |
| DS-04 | error | 「狀態」段 | 五個項目（載入中、空資料、錯誤、無權限、資料狀態差異）各恰好一行且冒號後非空 | `畫面 "{screen}" 的狀態段缺少 "{item}"` |
| DS-05 | error | 操作表「觸發」× usecase `roles` × 角色表「做得到」 | 對每個觸發 `uc-x` 的操作：角色表中「做得到」欄含該操作名稱的角色集合，必須等於 `uc-x` 的 `roles` 集合 | `畫面 "{screen}" 操作 "{op}" 的可用角色 {a} 與 uc "{uc}" 的 roles {b} 不一致` |
| DS-06 | warn | 所有 usecase（`crud` 含 C／U／D）× 所有 ui 檔操作表 | 每個寫入 Use Case 至少被一個畫面的操作表引用；沒有可能是背景作業（有 `requires`）或漏畫面。有 `requires` 的不報 | `寫入 uc "{uc}" 沒有任何畫面觸發它` |
| DS-07 | warn | 所有畫面「進入與離開」 | 每個 Screen 至少被一個其他 Screen 的「進入與離開」引用，或「從哪裡進來」寫「模組入口」 | `畫面 "{screen}" 沒有任何畫面導向它，也不是模組入口` |
| DS-08 | error | ui 檔全文 | ui 檔的實體表、角色表、usecase 區塊（```usecase）不得出現；設計檔不定義新概念 | `ui 檔不得含 "{section}"` |

---

## CR：變更單

| ID | 層級 | 讀哪個檔的哪一段 | 規則 | 訊息 |
|---|---|---|---|---|
| CR-01 | error | `.dev/CR.md` 總表「影響 ID」欄 × PR diff | 對 PR 標題或 diff 裡 `@CR-` 所指的每張 CR：diff 改動到的 usecase 項目（`uc-`）、實體表列（entity）、畫面段落（`s-`）、掛該 `@CR-` 的 Scenario 所屬 `uc-` 的集合，必須等於「影響 ID」集合（忽略 `(新增)`／`(移除)` 標註）。多改、少改都報 | `CR-{n} 影響 ID 與 diff 不符：diff 多改 {extra}；CR 列了但未改 {missing}` |
| CR-02 | error | 所有 spec 的 `@changed` Scenario × 總表狀態 | 同一個 Feature 內，兩個 `@changed` Scenario 若對應同一個 `@deprecated`（同 `@uc-`、Scenario 名稱相同），且兩張 CR 狀態都是進行中（修改規格／待處理），報 error | `Scenario "{name}" 同時被進行中的 CR-{a} 與 CR-{b} 掛 @changed` |
| CR-03 | error | `.dev/CR.md` 主表格 | 欄位依 CRC §6 順序齊全；編號 `CR-<三位數>` 唯一；類型 ∈ {新增, 變更, 移除}；狀態 ∈ {記錄, 修改規格, 待處理, 處理完成, 駁回}；影響 ID 欄每個項目是反引號 ID 加可選 `(新增)`／`(移除)` | `CR.md 格式錯誤：{detail}` |
| CR-04 | error | 總表「狀態」×「影響 ID」 | 狀態為修改規格／待處理／處理完成的 CR，影響 ID 不得為空，且每個 ID 存在（`(移除)` 的除外） | `CR-{n} 狀態為 {status} 但影響 ID {detail}` |
| CR-05 | error | PR diff × 總表 | diff 裡新出現的 `@CR-xxx`，該 CR 在總表狀態必須是「修改規格」 | `@CR-{n} 出現在 diff 但總表狀態為 {status}` |

---

## 檢查 ↔ 腳本對照

| 腳本 | 涵蓋的檢查 ID |
|---|---|
| `spec-check` | REF-01～REF-06、REF-08（spec 部分）、REF-09、UC-01～UC-14、GH-01～GH-04、GH-05（僅「已進入開發」判定部分）、GH-06～GH-09 |
| `ui-check` | REF-07、REF-08（ui 部分）、DS-01～DS-08 |
| `cr-check` | GH-05（diff 部分）、CR-01～CR-05 |

每個 ID 恰好出現在上表一次（REF-08、GH-05 由兩支腳本分工，訊息相同）。
