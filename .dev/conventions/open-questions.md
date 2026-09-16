# 待決定事項（open-questions.md）

本次規範修訂（依 `.dev/prompts/improve-convention-prompt.md`）留下的決定。決定後把結論寫回對應規範，並把該題移到下方「已決定」表。

## 已決定（2026-09-16）

| 題目 | 決定 | 寫回哪裡 |
|---|---|---|
| Q1 「已進入開發」的判定 | spec 檔頭一行 `狀態：草稿` 或 `狀態：開發中`（必填）；「開發中」即已進入開發，首版交付時改狀態並在變更紀錄加一列「首版進入開發」，這一步不用 CR | `spec-convention.md` §2.1、§5.1；`checks.md` GH-05／GH-08；`scripts.md` §4.2 |
| Q2 `pre`／`post`／`fail` 句型 | 不限制句型，每句至少一個反引號 ID；唯一例外是 `fail` 的固定句「拒絕，資料不變」。累積真實句子後再考慮歸納句型 | `spec-convention.md` §2.4；`checks.md` UC-03 |
| Q3 首版是否要求 usecase 區塊齊全 | 要，從第一版就齊全；腳本對草稿與開發中的檔一視同仁（草稿只差在不需要 CR） | `spec-convention.md` §5.1 |
| Q4 腳本語言 | Python 3.10+ + PyYAML，實作於 `scripts/`；行號用 `yaml.compose` 的 node mark，Markdown 表格與圍欄自寫解析 | `scripts.md`、`scripts/README.md` |
| Q5 檔名 | 既有後端設計備忘 `design.md` 改名 `design-<模組>.md`（已改，並修正彼此的相對連結）；UI 短規格叫 `ui-<模組>.md`，腳本叫 `ui-check` | `ui-design-convention.md`、`spec-convention.md` §1、`scripts.md`、CLAUDE.md |
| Q6 實體 ID 的粒度 | 實體不等於 aggregate：一個 aggregate 是一棵實體樹，樹根是 aggregate root。所有有身分的實體都列在實體表，加「所屬 Aggregate」欄（給人看，腳本不解析）；Aggregate 註解與 `crud` 可寫任何實體 ID | `spec-convention.md` §2.2、§9 |
| Q7 跨模組引用 | 不在引用端宣告，直接用同一 ID；`spec-check --report` 多印一張「外部引用」表列出每個模組引用了哪些別處定義的 ID | `spec-convention.md` §2.2、§9；`scripts/speccheck/report.py` |
| Q8 固定字串標題比對 | 前綴比對，固定字串後只能接行尾、空白、`（`、`(`、`/` | `spec-convention.md` §2；`scripts/speccheck/md.py` |

---

## Q9. usecase 區塊的變更要不要有自己的 tag

Scenario 改動走 `@deprecated`／`@changed` 並存，reviewer 一眼看到新舊；usecase 區塊目前是原地改，只靠 diff 與 CR 影響 ID 追蹤（`spec-convention.md` §2.4 已這樣寫）。

| 選項 | 做法 | 取捨 |
|---|---|---|
| A. 原地改 | 現狀 | YAML 乾淨；驗收前開發者看不到「舊版 post 是什麼」，要翻 git |
| B. 項目加 `status`／`cr` 欄位 | usecase 項目加 `cr: CR-024`、`status: changed`，驗收後清掉 | 與 Scenario tag 對稱；同一個 uc 新舊兩版 post 要並存就得複製整個項目，ID 衝突（REF-08） |
| C. 原地改 + 變更紀錄摘要必列 uc ID | 現狀，但變更紀錄那一列的摘要必須列出被改的 `uc-` ID（REF-01 會確認存在） | 幾乎零成本；仍然看不到舊版內容 |

**方向已定為 C**，`spec-convention.md` §6 的範例已照此寫；「摘要必列 uc ID」要不要做成腳本檢查，等遷移（Q10）時一起改。

---

## Q10. 既有規格的遷移順序

六個模組的 spec 都不符合新格式（沒有狀態行、三張表、角色表、usecase 區塊、`@uc-` tag），也還沒有 `.dev/CR.md`。

| 選項 | 做法 | 取捨 |
|---|---|---|
| A. 一次全遷 | 一個 PR 遷六個模組 | 腳本一上線就全綠；PR 巨大、難 review，且 F01 已上線的部分照規範要開 CR |
| B. 逐模組遷，腳本以 allowlist 放行未遷移檔 | `spec-check` 加 `--legacy <glob>` 列出尚未遷移的檔，這些檔只跑舊規則；遷一個拿掉一個 | 可分批；腳本多一個模式要維護 |
| C. 先遷 F01，其餘等腳本實作完 | F01 作為範本手工遷，驗證規範可寫；腳本已做出來，再用它輔助遷其餘五個 | 最務實；中間有一段時間規範與檔案不一致 |

**建議 C**：F01 是所有 CR 的來源、也是範例情境（Swimlane）所在。F01 的遷移本身開一張 CR（例如 CR-005「規格格式遷移至 usecase 區塊」），影響 ID 列全部實體與 uc；同一個 PR 建立 `.dev/CR.md` 並登記 CR-001～CR-005。**尚未執行。**

---

## 上線後最先會被違反的三條規則

1. **「反引號裡只能放 ID」（`spec-convention.md` §9、REF-01）**。既有檔已經大量用反引號包程式碼名稱（`BoardClock.now()`、`Instant.now()`、`NONE`／`START`／`DONE`、`kanban-core`），這是 Markdown 寫作的本能。遷移時要把它們改成中文引號或搬去 `design-<模組>.md`，之後每個新作者都會再犯一次。緩解：REF-01 的訊息要講清楚「反引號只給 ID，程式碼名稱用「」」，並在 CLAUDE.md 寫一行。

2. **「Aggregate 註解與 `crud` 必須一致」（GH-06）**。同一個事實要在兩個地方寫（註解與 usecase 區塊），任何一邊改了忘記改另一邊就 error；尤其 `read` 的判定（「要先查才能寫」）本來就靠人判斷，跟 `crud` 的 R 對不對得上很主觀。緩解：規範已寫「以 usecase 區塊為準」，可考慮日後讓腳本從 `crud` **生成**註解、註解降為唯讀輸出，徹底消掉重複。

3. **「`fail` 每個 key 至少一個 `@fail-` Scenario」（UC-05）與「每個 Use Case 至少一個成功 Scenario」（UC-06）**。SA 寫 usecase 區塊時很自然會把想得到的失敗都列進 `fail`，但不會為每一條寫 Scenario；反過來讀取類 Use Case（`crud` 只有 R）常常只有 usecase 項目、沒有 Scenario。上線第一週 error 最多的會是這兩條。緩解：規範可補一句「`fail` 只列你打算寫 Scenario 驗證的那幾條，其餘寫進待釐清」。
