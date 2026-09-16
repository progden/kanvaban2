# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 16）
- 目前階段：階段 2（F02 遷移）進行中
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.05（[F02]「Board 權限管理」usecase 區塊＋tag＋Aggregate 註解）｜結果：done
- 下一個任務：T2.06（[F02]「Board 存取權限」usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02 已完成「建立使用者帳號」「使用者登入與登出」「Board 建立與成員邀請」「Board 權限管理」，F02 error 數由 59 降為 46（全部 spec 由 217 降為 204）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；F02 已定義實體 user、board-membership，角色 r-system-user、r-board-owner、r-board-member；本輪新增 uc：uc-reject-invite-by-member、uc-reject-role-change-by-member、uc-reject-structure-change-by-member、uc-delete-board、uc-member-add-card（皆僅屬「Board 權限管理」Feature，未重用其他 Feature 的 uc）
- 最近 3 條假設：OQ-06：「Board 權限管理」5 個 Scenario 因 GH-01（uc 須同 Feature）與 UC-06（uc 須有成功 Scenario）拆成 5 個本 Feature 專屬 uc，不重用 uc-invite-member 等既有 uc（高影響，見 OQ 檔與 PDCA）；3 個拒絕類 uc 的 crud 全為 R、roles 留空，比照原 Aggregate 註解只標 read；uc-delete-board 有 pre 無對應 fail 分支，比照 F01 uc-rename-swimlane 既有寫法
- 待注意：全部 spec error 總數 204；Iteration 11 標題含兩個任務編號、不符 HEADING 正則，依鐵則 4 不可再修改；F02 尚有 T2.06～T2.09 待做
