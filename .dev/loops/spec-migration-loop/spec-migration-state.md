# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 36）
- 目前階段：階段 2（F05 遷移完成，準備開始 F06）
- 上一輪任務：完成 T2.19（[F05] 遷移程序 10～11 正文清理與變更紀錄收尾，補上 CR-005 收尾列）｜結果：done
- 下一個任務：T2.20（[F06] 遷移程序 1～6：狀態行、名詞／角色表、「身為」行）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03（全檔 0 error）；F04（全檔 0 error）；F05（全檔 0 error，遷移完成）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user）、F02（r-board-owner、board-membership、uc-assign-card-owner-by-drag）；F05 新增 uc-view-workload、uc-drag-assign-card-owner（本模組本地，見 OQ-09）
- 最近 3 條假設：T2.19 把 F05 正文與變更紀錄裡的 `BoardMembership`／`assigneeIds` 等舊名改成實際 ID `board-membership`／`card.assignees`（低影響，格式對齊既有實體／欄位 ID）；變更紀錄舊列票號 "F05" 改為空白＋摘要前綴「（原票號 F05）」，比照 F02 已有的鏡射列（低影響，遵循遷移程序步驟 11 與既有前例）；追加 CR-005 收尾列（低影響，固定格式）
- 待注意：全部 spec error 總數 30（43→30，F05 歸零）；F01=0、F02=0、F03=0、F04=0、F05=0、F06=30；接下來進入 F06 遷移（尚未開始）
