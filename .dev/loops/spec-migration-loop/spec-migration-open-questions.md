# spec 遷移待決事項（OQ）

遷移過程中遇到的**高影響假設**（見規則書「假設分級」）。loop 不停下等人：執行輪選定「最小驚訝、與既有 Scenario 一致」的選項照做，並在下表末尾追加一列；對應 spec 的 `## 待釐清` 會有一行指向它。人工事後檢視，不同意的再開 CR 或 D-xx 處理。

規則（由 `verify-spec-migration.sh` 檢查）：

- **只能在表格末尾追加列**，不可修改或刪除既有列，表格之後不可再加其他內容。
- 編號 `OQ-01` 起流水號；「採用」寫實際採用的選項，「依據」寫依據哪個 Scenario（模組、Feature、Scenario 名稱），兩欄都不可空白。
- 狀態：`自動決議`（loop 已照採用的選項做）／`blocked`（環境限制，對應任務已標 blocked）。

| 編號 | 日期 | 模組 | 情況 | 選項 | 採用 | 依據 | 狀態 |
|---|---|---|---|---|---|---|---|
| OQ-01 | 2026-09-16 | F01 | 「該操作應該被記錄為活動紀錄」在 usecase 區塊要如何表達：另立一個 log 類 uc、用 emits 事件讓別的 uc requires 它，還是併入原 uc 的 post 與 crud | (a) 另立 uc-log-activity，其他 uc emits 它；(b) 併入各 uc 自身 post，並在 crud 加上所屬 board 的 U，Aggregate 註解標 board: read, write | (b)：Swimlane 管理、Stage（階段）管理共 9 個 uc 的 crud 補上 board: U，成功 Scenario 的 Aggregate 註解補回 board: read, write；不另立 uc、不用 emits/requires | 依據 F01「重新命名 Swimlane」「新增一個 Stage」等 9 個 uc 的成功 Scenario（步驟「該操作應該被記錄為一筆活動紀錄」），以及 F02 legacy「檢視看板活動紀錄」Feature 的實作備註：Board／Card 既有事件的操作人記錄各自記在 `Board.activityLog`／`Card.activityLog`，代表活動紀錄屬於被操作的 Aggregate 本身，不是獨立實體 | 自動決議 |
| OQ-02 | 2026-09-16 | F01 | 「刪除卡片」Feature 的「取消刪除卡片」Scenario（使用者於確認訊息中選擇取消，卡片不被刪除）該如何歸入 uc-delete-card 的 usecase 區塊：當成獨立的成功 Scenario（要求 Aggregate 註解標 card: write，但實際未寫入，與腳本 GH-06 及事實不符），還是視為一種「失敗」分支（不完成刪除） | (a) 保留為無 @fail- 的成功 Scenario，card 註解維持 read（GH-06 會報 crud.card=D 但未標 write）；(b) 在 pre 補一條「使用者於確認訊息中選擇取消」，fail 補對應句「拒絕，該 card 不被移除，資料不變」，Scenario 掛 @fail-p2 | (b)：uc-delete-card 的 pre 新增 p2、fail 新增 p2，「取消刪除卡片」Scenario 改掛 `@uc-delete-card @fail-p2`，Aggregate 註解維持 card: read 不變 | 依據 F01「Card（卡片）編輯」Feature 的「取消刪除卡片」Scenario：Given 存在一張卡片、When 點擊刪除、And 在確認訊息中選擇取消、Then 該卡片應該仍然存在於看板中——步驟本身即「使用者選擇取消、資料不變」，與 fail 分支「拒絕，資料不變」的既有措辭（見 uc-delete-swimlane／uc-delete-stage）語意一致 | 自動決議 |
| OQ-03 | 2026-09-16 | F01 | OQ-02 採用內容中 pre.p2 措辭方向寫反：原文把 p2 寫成「使用者於刪除 card 的確認訊息中選擇取消」，導致「刪除卡片需要確認」這個成功 Scenario（使用者確認刪除）反而不滿足 pre.p2，「取消刪除卡片」的 fail 分支反而滿足 pre | (a) 維持原文不動；(b) p2 改為「使用者於刪除 card 的確認訊息中確認刪除」，fail.p2 改為「不刪除，該 card 仍存在於看板中，資料不變」，post 第一句改為「該 card 從看板中移除」並補上活動紀錄句 | (b)：p2 改為確認刪除、取消為 p2 不成立，fail.p2 與 post 一併修正 | 依據 F01「Card（卡片）編輯」Feature 的「刪除卡片需要確認」Scenario（When 我確認刪除 → Then 該卡片應該從看板中移除，且該操作應該被記錄為一筆活動紀錄）與「取消刪除卡片」Scenario（And 我在確認訊息中選擇取消 → Then 該卡片應該仍然存在於看板中） | 自動決議 |
