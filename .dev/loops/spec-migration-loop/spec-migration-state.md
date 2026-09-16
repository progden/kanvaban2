# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 27）
- 目前階段：階段 3（F04 遷移進行中）
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.14（[F04] 遷移程序 1～6：狀態行、名詞／角色表、「身為」行）｜結果：done
- 下一個任務：T2.15（[F04]「看板時間管理」usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03（全檔 0 error）；F04 遷移程序 1～6 完成，7～11 待做
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）、F02（user、board-membership；r-system-user、r-board-owner、r-board-member）、F03（uc-view-cycle-lead-time 等 6 個純讀取 uc）；F04 本輪未新增 ID（角色表沿用 F01 的 r-user，未重列，做法同 F03）
- 最近 3 條假設：F04「身為看板的使用者」統一改為「身為 看板使用者」對應 F01 已定義的 r-user，不新設角色（低影響，同 F03 先例）；F04 實體／欄位／關係表暫留空表頭，待 T2.15 usecase 區塊確認後才填（低影響，同 F01～F03 遷移順序）；決議紀錄段落依任務描述原樣保留，未搬動
- 待注意：全部 spec error 總數 116 → 110（F01=0、F02=0、F03=0、F04=54→48、F05=32、F06=30）
