# 規格文件撰寫規範（Spec Convention）

本文件定義專案中以 BDD / Gherkin 撰寫規格文件的方式，包含：文件結構、Gherkin 撰寫規則、Aggregate 標記、變更管理（tag 與流程）、以及何時該拆新 feature 檔。

規格文件視同程式碼：進版控、走 PR、用 tag 標狀態，不依賴口頭或記憶來區分「哪些做完、哪些沒做、哪些改過」。

---

## 1. 檔案組織

| 項目 | 規則 |
|------|------|
| 一個模組一份文件 | 例如 `spec-kanban-basic.md`、`spec-approval-flow.md` |
| 文件格式 | Markdown，Gherkin 內容放在 ```gherkin 程式碼區塊中 |
| 一份文件可含多個 Feature | 以 `---` 分隔，每個 Feature 一個 `## Feature:` 標題 |
| Feature 對應 `.feature` 檔 | 若導入自動化測試，每個 `## Feature:` 區塊 1:1 對應一個 `.feature` 檔，內容直接複製程式碼區塊 |
| 檔名 | 規格文件一律以 `spec-` 開頭，後接模組名：`spec-<模組>.md`，小寫英文 + 連字號。對應的 `.feature` 檔同樣以 `spec-` 開頭：`spec-<模組>-<feature>.feature`，例如 `spec-kanban-basic-card-editing.feature` |

---

## 2. 文件結構模板

每份模組規格文件依序包含以下區段。

**格式範例**（規格檔 `spec-<模組>.md`）：

```markdown
# <模組名稱>使用案例（BDD）

<一段簡介：本文件涵蓋哪些功能，條列列出>

## 名詞定義
| 名詞 | 說明 |
|------|------|

## Aggregate 標記說明
（固定段落，見第 4 節）

## 變更紀錄（Change Log）
（見第 6 節；新模組可先留空）

---

## Feature: <功能名稱>
```gherkin
...
```

---

## Feature: <功能名稱>
...

---

## 待釐清 / 未來擴充（Open Questions）
- ...
```

### 各區段說明

- **簡介**：一段話說明模組範圍，並條列本文件涵蓋的功能群。
- **名詞定義**：所有在 Scenario 中出現的領域名詞都要在此定義，中英文並列（例：Swimlane（泳道））。
- **Aggregate 標記說明**：固定段落，說明註解格式與 read / write 的判定原則。
- **變更紀錄**：規格進入開發後的所有變更摘要，一行一筆。
- **Feature 區塊**：主要內容。
- **Open Questions**：尚未決定、但已知需要討論的問題。已決定的項目要從此區移除並轉成 Scenario。

---

## 3. Gherkin 撰寫規則

### 3.1 Feature 標頭

**格式範例**（寫在規格檔的 Gherkin 區塊內，Feature 開頭三行）：

```gherkin
Feature: <功能名稱>
  身為 <角色>
  我想要 <做什麼>
  以便 <達成什麼目的>
```

三行固定句型：身為 / 我想要 / 以便。

### 3.2 Background

放所有 Scenario 共用的前置條件（登入、開啟看板、預設資料）。只放「每個 Scenario 都需要」的東西；只有部分 Scenario 需要的前提放在該 Scenario 的 Given。

### 3.3 Scenario 命名

- 描述「行為 + 結果」，不描述 UI 操作：`刪除包含卡片的 Swimlane 需要確認` ✅，`點擊刪除按鈕` ❌
- 錯誤情境直接寫出規則：`Swimlane 名稱不可為空`、`看板至少保留一個 Stage`
- 一個 Scenario 只驗證一個行為

### 3.4 步驟撰寫

| 關鍵字 | 用途 | 語氣 |
|--------|------|------|
| Given | 前置狀態 | 「看板中存在…」「我正在…」 |
| When | 使用者動作 | 「我點擊…」「我輸入…」「我將…拖曳到…」 |
| Then | 可觀察的結果 | 「應該顯示…」「不應該…」 |
| And | 延續上一個關鍵字 | — |

- 多階段互動（例如刪除 → 確認 → 刪除）允許 `When / Then / When / Then` 交錯，不需要拆成兩個 Scenario。
- 步驟中的具名資料（名稱、標題、訊息文字、日期）一律用半形雙引號：`"緊急項目"`、`"2026-09-20"`。
- UI 元件名稱用中文引號：`「新增 Swimlane」按鈕`。
- 錯誤訊息與確認訊息寫出完整固定文字，作為驗收依據：`系統應該顯示錯誤訊息 "Swimlane 名稱不可為空"`。
- 多欄位輸入使用資料表：

**格式範例**（Scenario 步驟中的資料表）：

```gherkin
And 我填寫以下欄位：
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
#   board: read, write
#   card: read
Scenario: ...
```

規則：

- 只列出實際會用到的 aggregate，沒用到的整行省略。
- `read`：需要先查詢既有資料才能決定如何寫入或驗證（重新命名要先找到對象、排序要先讀現有順序、刪除前要確認是否為最後一個、或是否還有卡片存在）。
- `write`：會真正新增 / 修改 / 刪除該 aggregate 的資料。
- 各模組規格文件的「Aggregate 標記說明」段落自行列出該文件用到的 aggregate；新增 aggregate 時同步更新該段落。
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

驗收通過後的最終狀態：只剩 `@CR-xxx`，其餘狀態 tag 全部移除。首版規格（尚未進入開發前寫的）不掛任何 tag。

**掛 `@CR-xxx` 前的必要條件**：該編號必須已經在 `.dev/CR.md` 總表登記（見 `cr-convention.md` 第 2、6 節）。規格 PR 裡出現的每一個 `@CR-xxx` 都要能在 `.dev/CR.md` 找到對應的一筆，找不到就不能合併——沒有登記的 CR 不算變更單，不能作為規格變更的依據。

### 5.2 情境 A：新增 Scenario

在既有 Feature 中追加，掛 `@added @CR-xxx @wip`。

**格式範例**（新增 Scenario）：

```gherkin
  @added @CR-023 @wip
  # Related aggregate:
  #   board: read, write
  Scenario: 為 Swimlane 設定顏色標記
    Given 看板中存在一個 Swimlane "緊急項目"
    When 我為該 Swimlane 選擇顏色 "紅色"
    Then 該 Swimlane 的標題列應該顯示紅色標記
```

### 5.3 情境 B：變更既有 Scenario

不原地修改。舊的掛 `@deprecated`，新的掛 `@changed`，兩者同一票號並排放置。

**格式範例**（變更既有 Scenario，新舊並存）：

```gherkin
  @deprecated @CR-024
  # Related aggregate:
  #   board: read, write
  #   card: read, write
  Scenario: 刪除包含卡片的 Swimlane 需要確認
    Given 看板中存在一個 Swimlane "本週優先"，其中包含 3 張卡片
    When 我嘗試刪除該 Swimlane
    Then 系統應該顯示確認訊息，告知該 Swimlane 內有 3 張卡片將一併被刪除
    When 我確認刪除
    Then 該 Swimlane 與其所有卡片都應該被移除

  @changed @CR-024 @wip
  # Related aggregate:
  #   board: read, write
  #   card: read, write
  Scenario: 刪除包含卡片的 Swimlane 需要先轉移卡片
    Given 看板中存在一個 Swimlane "本週優先"，其中包含 3 張卡片
    When 我嘗試刪除該 Swimlane
    Then 系統應該提示我選擇一個目的 Swimlane 來接收這 3 張卡片
    When 我選擇目的 Swimlane 為 "預設泳道"
    Then 這 3 張卡片應該被移動到 "預設泳道"
    And Swimlane "本週優先" 應該被刪除
```

開發者看到同票號的 `@deprecated` + `@changed` 就知道「這條是要蓋掉那條」。

### 5.4 情境 C：移除既有 Scenario（功能下架）

掛 `@deprecated @CR-xxx`，並在變更紀錄註明「移除」。新版上線後刪除。

### 5.5 測試篩選

若有自動化測試，CI 以 tag 篩選：

- 一般 CI：`--tags "not @wip"`
- 檢視待開發清單：`--tags "@wip"`
- 檢視某張票影響範圍：`--tags "@CR-024"`

---

## 6. 變更流程

1. **開變更單（CR）**：依 `cr-convention.md` 在 `.dev/CR.md` 總表登記，說明為什麼改、影響哪些 Feature / Scenario、驗收標準。編號即為 tag 用的 `@CR-xxx`。
2. **修改規格並提 PR**：依第 5 節掛 tag。PR 的 diff 就是變更清單，reviewer 只看 diff。
3. **更新變更紀錄**：在文件的「變更紀錄」區段追加一行。
4. **開發完成、驗收通過**：移除狀態 tag（`@wip` / `@added` / `@changed`）、刪除 `@deprecated` Scenario，再提一次 PR。

### 變更紀錄格式

**格式範例**（規格檔內「變更紀錄」區段，對應第 2 節文件結構模板中的同名區段）：

```markdown
## 變更紀錄（Change Log）

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-12 | CR-023 | 新增 | Swimlane 顏色標記 |
| 2026-09-12 | CR-024 | 變更 | 刪除含卡片的 Swimlane 改為轉移卡片，不再一併刪除 |
```

類型固定為：新增 / 變更 / 移除。

---

## 7. 何時該開新 Feature 檔

| 情況 | 做法 |
|------|------|
| 新增內容自成一塊、有自己的角色 / 目的（例如「代理人機制」「WIP 限制」） | 開新 `## Feature:` 區塊或新文件，在原 Feature 的簡介補一行「相關功能見 xxx」。原內容完全不動 |
| 新增的 Scenario 散落在既有 Feature 的職責範圍內（例如 Swimlane 多一個顏色屬性） | 原 Feature 內追加，掛 `@added` |
| 修改既有行為 | 原 Feature 內以 `@deprecated` + `@changed` 並存 |
| 新增內容需要新的 Aggregate | 一律開新 Feature，並更新「Aggregate 標記說明」 |
| 單一 Feature 超過約 15 個 Scenario | 考慮依子功能拆檔 |

判斷原則：如果新內容可以用「身為 / 我想要 / 以便」獨立寫出一段合理的 Feature 標頭，就拆；不行就併入原 Feature。

---

## 8. 提交前檢查清單

- [ ] 所有領域名詞都在「名詞定義」中
- [ ] 每個 Scenario 都有 Aggregate 註解
- [ ] Feature 標頭三行齊全
- [ ] Scenario 名稱描述行為與結果，非 UI 操作
- [ ] 錯誤 / 確認訊息寫出完整固定文字
- [ ] 進入開發後的新增 / 變更都掛了 `@CR-xxx` 與狀態 tag
- [ ] 每個 `@CR-xxx` 都已在 `.dev/CR.md` 總表登記
- [ ] `@changed` 一定有對應的 `@deprecated`，且票號相同
- [ ] 變更紀錄已追加
- [ ] 已決定的 Open Question 已移除並轉為 Scenario
