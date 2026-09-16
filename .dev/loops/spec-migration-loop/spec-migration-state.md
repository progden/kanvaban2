# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 20）
- 目前階段：階段 2（F02 遷移）進行中
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.08（[F02]「檢視看板活動紀錄」usecase 區塊＋tag＋Aggregate 註解）｜結果：done
- 下一個任務：T2.09（[F02] 遷移程序 10～11 與收尾：正文反引號清理（含「Aggregate 事件盤點」「實作備註」段落）、design.md 稱呼改實際檔名、變更紀錄改格式並追加 CR-005；F02 全檔 0 error）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02 已完成「建立使用者帳號」「使用者登入與登出」「Board 建立與成員邀請」「Board 權限管理」「Board 存取權限」「卡片負責人指派」「檢視看板活動紀錄」；F02 error 數由 26 降為 18（全部 spec 176）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；F02 已定義實體 user、board-membership，角色 r-system-user、r-board-owner、r-board-member；uc-view-board-activity-log 的 crud 為 {board: R, board-membership: R}，roles: [r-user]（依 OQ-05），emits／requires 皆空（F01／F02 目前沒有任何 uc 有非空 emits）
- 最近 3 條假設：本輪為低影響格式清理：修正「Aggregate 事件盤點」前的敘述段落反引號（`Board.activityLog`／`Card.activityLog`／`kanban-core`／`kanban-spring` 改為「」），`design.md` 改為實際檔名 `design-user-membership.md`；未新增 OQ
- 待注意：全部 spec error 總數 176；Iteration 11 標題含兩個任務編號、不符 HEADING 正則，依鐵則 4 不可再修改；F02 尚有 T2.09 待做（收尾整份 F02）
