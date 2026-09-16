# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 37）
- 目前階段：階段 2（F06 遷移程序 1～6 完成，準備進入 usecase 區塊）
- 上一輪任務：完成 T2.20（[F06] 遷移程序 1～6：狀態行、名詞／角色表、「身為」行）｜結果：done
- 下一個任務：T2.21（[F06] 「Feature／CR 追蹤表」usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01～F05（全檔 0 error）；F06 進行中（GH-08/REF-03/04/05/08/09 已清零，剩 GH-01、UC-01 待 T2.21～T2.22）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user）、F02（r-board-owner、board-membership、uc-assign-card-owner-by-drag）；F06 本輪未新增實體／角色，沿用 F01 的 r-user
- 最近 3 條假設：F06「名詞定義」原表五項（Feature 卡、CR 卡、affects 標籤、狀態、orphan CR）皆非實體，全部搬到「其他名詞」，反引號改為「」（低影響，比照 F05 的 Active Card 等處理）；F06「身為看板的使用者」改為「身為 看板使用者」並沿用 F01 已定義的 r-user，本檔「角色定義」表留空（低影響，比照 F05 對 r-user 的引用方式）；F06「實體／欄位／關係」三張表留空，因本模組只讀取 F01 的 board、card，未新增任何實體（低影響，比照 F03／F04／F05 前例）
- 待注意：全部 spec error 總數 30→15（F06 由 30 降到 15，剩 GH-01/UC-01 待補 usecase 區塊與 tag）；F01=0、F02=0、F03=0、F04=0、F05=0、F06=15；接下來做 T2.21
