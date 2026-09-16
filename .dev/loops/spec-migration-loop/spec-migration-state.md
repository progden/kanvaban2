# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 21）
- 目前階段：階段 2（F02 遷移）完成，準備進入階段 3（F03 遷移）
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.09（[F02] 遷移程序 10～11 與收尾：正文反引號清理、design.md 稱呼改實際檔名、變更紀錄改格式並追加 CR-005）｜結果：done
- 下一個任務：T2.10（[F03] 遷移程序 1～6：狀態行、名詞／角色表（只引用 F01／F02 已定義的實體與角色）、四個 Feature 的「身為」行）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02 全部 7 個 Feature 已完成，全檔 0 error
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；F02 新增實體 user、board-membership，角色 r-system-user、r-board-owner、r-board-member；F02 變更紀錄已追加 CR-005（規格格式遷移至 usecase 區塊）
- 最近 3 條假設：本輪為低影響格式清理：「Aggregate 標記說明」段落 `read`／`write` 改為「read」「write」與 F01 一致；正文中非六種 ID 的程式碼／類別名稱（`User`、`BoardMembership`、`Card.assignee`、`assignTo`、`addAssignee`、`archivedAt`、`Label`、`Owner`／`Member`）改為「」；`## 實作備註（留給 design.md）` 改為實際檔名 `design-user-membership.md`；未新增 OQ
- 待注意：全部 spec error 總數 158（F01=0、F02=0、F03=42、F04=54、F05=32、F06=30）；Iteration 11 標題含兩個任務編號、不符 HEADING 正則，依鐵則 4 不可再修改
