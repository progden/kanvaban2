# spec 遷移待決事項（OQ）

遷移過程中遇到的**高影響假設**（見規則書「假設分級」）。loop 不停下等人：執行輪選定「最小驚訝、與既有 Scenario 一致」的選項照做，並在下表末尾追加一列；對應 spec 的 `## 待釐清` 會有一行指向它。人工事後檢視，不同意的再開 CR 或 D-xx 處理。

規則（由 `verify-spec-migration.sh` 檢查）：

- **只能在表格末尾追加列**，不可修改或刪除既有列，表格之後不可再加其他內容。
- 編號 `OQ-01` 起流水號；「採用」寫實際採用的選項，「依據」寫依據哪個 Scenario（模組、Feature、Scenario 名稱），兩欄都不可空白。
- 狀態：`自動決議`（loop 已照採用的選項做）／`blocked`（環境限制，對應任務已標 blocked）。

| 編號 | 日期 | 模組 | 情況 | 選項 | 採用 | 依據 | 狀態 |
|---|---|---|---|---|---|---|---|
| OQ-01 | 2026-09-16 | F01 | 「該操作應該被記錄為活動紀錄」在 usecase 區塊要如何表達：另立一個 log 類 uc、用 emits 事件讓別的 uc requires 它，還是併入原 uc 的 post 與 crud | (a) 另立 uc-log-activity，其他 uc emits 它；(b) 併入各 uc 自身 post，並在 crud 加上所屬 board 的 U，Aggregate 註解標 board: read, write | (b)：Swimlane 管理、Stage（階段）管理共 9 個 uc 的 crud 補上 board: U，成功 Scenario 的 Aggregate 註解補回 board: read, write；不另立 uc、不用 emits/requires | 依據 F01「重新命名 Swimlane」「新增一個 Stage」等 9 個 uc 的成功 Scenario（步驟「該操作應該被記錄為一筆活動紀錄」），以及 F02 legacy「檢視看板活動紀錄」Feature 的實作備註：Board／Card 既有事件的操作人記錄各自記在 `Board.activityLog`／`Card.activityLog`，代表活動紀錄屬於被操作的 Aggregate 本身，不是獨立實體 | 自動決議 |
