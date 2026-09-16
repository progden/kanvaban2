# 文件規範總覽（Docs Convention）

本文件是所有規格相關文件的**入口**：說明有哪些文件、各自負責什麼、彼此怎麼引用、由誰簽字、被哪支腳本檢查。細節規則一律寫在各自的 convention，本文件只定邊界，不重複內容。

規則衝突時，以各自的 convention 為準；邊界爭議（某條規則該寫在哪份文件）以本文件為準。

---

## 1. 文件一覽

| 文件種類 | 檔名 | 回答的問題 | 簽字 | 規範 | 腳本 |
|---|---|---|---|---|---|
| 規格 | `spec-<模組>.md` | 系統**應該做什麼**：Use Case 合約（pre / post / fail）、領域名詞、角色、驗收 Scenario | PO | `spec-convention.md` | `spec-check` |
| UI 短規格 | `ui-<模組>.md` | 使用者**怎麼走到 Use Case**：畫面、進入與離開、角色可見範圍、操作與觸發、呈現狀態 | 設計 + PO | `ui-convention.md` | `ui-check` |
| 設計備忘 | `design-<模組>.md` | 系統**內部怎麼做**：API 契約、資料模型、交易邊界、效能與併發策略 | 開發 | `design-convention.md` | 無（不解析、不進 CR 影響 ID） |
| 變更單 | `.dev/CR.md`（總表）、`.dev/cr/CR-xxx.md`（明細，選填） | 規格進入開發後**為什麼改、改了什麼、改到哪裡** | SA / 架構師 | `cr-convention.md` | `cr-check` |

同一模組的 spec / ui / design 三份檔案放同一目錄，`<模組>` 字串相同。

### 1.1 輔助文件

| 檔名 | 用途 |
|---|---|
| `checks.md` | 三支腳本所有檢查項目的 ID、嚴重度（error / warn）、判定條件 |
| `scripts.md` | 腳本的用法、參數、`--report` 輸出格式 |
| `llm-review.md` | 腳本做不到、需要讀懂內容才能判斷的語意 review 提問 |
| `.dev/prompts/` | 驅動各階段工作的 agent 提示詞（規格訪談、UI 畫面討論、convention 稽核等）。提示詞不是規範，改提示詞不需要 CR |

---

## 2. 三層的責任邊界（MECE）

三種模組文件各擁有一種「斷言主詞」，同一個主詞只能出現在一種文件：

| 文件 | 擁有的斷言主詞 | 不得出現 |
|---|---|---|
| spec | 領域物件狀態、Use Case 的成功 / 失敗結果、回傳的訊息文字 | 點擊、輸入、顯示、畫面、按鈕、視窗、對話框、HTTP 狀態碼、資料表 |
| ui | 畫面元素、導覽、是否觸發某個 Use Case、結果如何呈現 | 業務結果本身（改為引用 `uc-` 與其 fail key）、API 內部行為 |
| design | HTTP 狀態碼、DB 結構、鎖、延遲、交易實作 | 業務規則的「為什麼」、畫面流程 |

判斷準則：

- **換掉 UI（CLI、swipe、語音）這句話還成立嗎？** 成立 → spec；不成立 → ui。
- **換掉實作（換資料庫、換框架）這句話還成立嗎？** 成立 → spec 或 ui；不成立 → design。
- 確認／取消等互動不進 spec；spec 的一個 Scenario 對應恰好一個交易。

### 2.1 測試守備範圍

| 文件 | 測試種類 | 執行對象 | 斷言主詞 |
|---|---|---|---|
| spec 的 Scenario | Gherkin 驗收測試、單元測試 | 應用層 / API，不經 UI | 領域物件、Use Case 結果 |
| ui 的驗收條件 | 元件測試、E2E | 前端元件，Use Case 用 mock / stub | 畫面元素、Use Case 是否被呼叫 |
| design | 整合測試、契約測試、效能測試 | 真實基礎設施 | 狀態碼、DB 列數、延遲 |

---

## 3. 引用方向

只允許單向引用，避免循環定義：

```
spec  ◄──  ui  ◄──  design
  ▲         ▲
  └── CR ───┘
```

| 引用 | 方式 |
|---|---|
| ui → spec | 只引用 spec 定義的 ID（Entity、Attribute、Role、UseCase、Event），不定義新的；缺什麼標 `⚠️` 回饋 spec |
| design → spec / ui | 引用 `uc-`、`s-` ID 說明實作對象；design 的錯誤碼必須對應 spec 某個 Use Case 的 fail key |
| CR → spec / ui | 影響 ID 列 Entity、UseCase、Screen；`cr-check` 用它對照 PR diff |
| spec → 任何 | 不引用 ui、design、CR 的內容，只在 Scenario tag 掛 `@CR-xxx` 標示來源 |

ID 的形式、前綴、反引號規則統一定義在 `spec-convention.md` 第 9 節，其他 convention 直接引用，不重新定義。

---

## 4. 誰在什麼時候讀哪份

| 角色 | 主要讀 | 順帶讀 |
|---|---|---|
| PO | spec | ui 的目的與驗收條件 |
| SA | spec、ui、CR | design（確認實作沒有偏離合約） |
| 設計 | ui | spec 的名詞表、角色表 |
| 前端開發 | ui、design | spec 的 usecase 區塊（知道每個操作背後的 pre / post / fail） |
| 後端開發 | spec、design | ui 的操作表（知道每個 Use Case 被誰觸發） |
| QA | spec 的 Scenario、ui 的驗收條件 | CR（知道哪些 `@wip` 正在改） |
| Reviewer | PR diff、CR 影響 ID | `llm-review.md` 的提問 |

---

## 5. 變更規則摘要

完整規則見 `cr-convention.md`，這裡只列各文件的邊界：

| 改動 | 需要 CR？ |
|---|---|
| spec 的名詞表、角色表、usecase 區塊、Scenario | 是（進入開發後） |
| ui 的操作表、角色與權限表、進入與離開、驗收條件 | 是（進入開發後） |
| ui 的目的、待確認事項；spec 的其他名詞表、待釐清 | 否 |
| design 任何內容 | 否（實作細節由開發 PR review 把關） |
| 任何 convention 文件 | 否，但走 PR；影響固定格式的改動要同時更新 `checks.md` 與對應腳本 |
| `.dev/prompts/` 的提示詞 | 否 |

「進入開發」以 spec 檔頭 `狀態：開發中` 為準。

---

## 6. 新增一個模組的順序

1. 建 `spec-<模組>.md`，依 `spec-convention.md` 寫名詞表、角色表、Feature 與 usecase 區塊；`spec-check` 無 error。
2. 建 `ui-<模組>.md`，依 `ui-convention.md` 從 spec 盤點畫面、逐一定案；`ui-check` 無 error。
3. spec 檔頭改 `狀態：開發中`，變更紀錄加「首版進入開發」。
4. 開發期間視需要建 `design-<模組>.md`，依 `design-convention.md` 記錄實作決策。
5. 之後所有改動走 `cr-convention.md`。

---

## 7. 各 convention 的修訂原則

- 每份 convention 開頭不放「本次修訂摘要」；修訂紀錄放 git log 或各檔末尾的「變更紀錄」段。
- 一份 convention 只定義一種文件的格式與規則，不含 agent 工作流程；工作流程寫在 `.dev/prompts/`。
- 規則能交給腳本的就寫成腳本可判定的形式（固定字串、前綴、句型禁字），並在 `checks.md` 登記檢查 ID；只有真的需要讀懂內容的才留給 `llm-review.md`。
- 用 `.dev/prompts/convention-audit-prompt.md` 定期稽核各 convention 與本文件的邊界是否一致。