# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 17）
- 目前階段：階段 2（F02 遷移）進行中
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.06（[F02]「Board 存取權限」usecase 區塊＋tag＋Aggregate 註解）｜結果：done
- 下一個任務：T2.07（[F02]「卡片負責人指派」usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02 已完成「建立使用者帳號」「使用者登入與登出」「Board 建立與成員邀請」「Board 權限管理」「Board 存取權限」，F02 error 數由 46 降為 39（全部 spec 由 204 降為 197）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；F02 已定義實體 user、board-membership，角色 r-system-user、r-board-owner、r-board-member；本輪新增 uc：uc-view-board-list（roles: [r-system-user]）、uc-reject-board-access-by-nonmember（roles 留空，crud 全 R，比照 OQ-06 既有決定），皆僅屬「Board 存取權限」Feature
- 最近 3 條假設：本輪低影響假設：H2 標題「Board 存取權限（我的 Board 列表）」對齊 gherkin Feature 名稱改為「Board 存取權限」；Aggregate 註解 boardMembership → board-membership；uc-reject-board-access-by-nonmember 的 post 補上反引號 board 以符合 UC-03（僅重述 Then 已表達的拒絕事實）
- 待注意：全部 spec error 總數 197；Iteration 11 標題含兩個任務編號、不符 HEADING 正則，依鐵則 4 不可再修改；F02 尚有 T2.07～T2.09 待做
