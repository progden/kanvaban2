# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 15）
- 目前階段：階段 2（F02 遷移）進行中
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.04（[F02]「Board 建立與成員邀請」usecase 區塊＋tag＋Aggregate 註解）｜結果：done
- 下一個任務：T2.05（[F02]「Board 權限管理」usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02 已完成「建立使用者帳號」（uc-create-user）、「使用者登入與登出」（uc-login、uc-logout）、「Board 建立與成員邀請」（uc-create-board、uc-invite-member、uc-change-member-role、uc-remove-member），F02 error 數由 79 降為 59（全部 spec 由 237 降為 217）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；F02 已定義實體 user、board-membership，角色 r-system-user、r-board-owner、r-board-member；新增 uc：uc-create-user、uc-login、uc-logout、uc-create-board、uc-invite-member、uc-change-member-role、uc-remove-member
- 最近 3 條假設：uc-create-board／uc-invite-member／uc-change-member-role／uc-remove-member 的 roles 一律採 Feature 標頭字面 r-board-owner（低影響，見 PDCA）；Scenario「多位 Owner 都擁有相同的管理權限」（同時示範邀請與移除）只掛 @uc-invite-member 單一 tag（低影響，格式選擇，見 PDCA）
- 待注意：全部 spec error 總數 217；Iteration 11 標題含兩個任務編號、不符 HEADING 正則，依鐵則 4 不可再修改；F02 尚有 T2.05～T2.09 待做
