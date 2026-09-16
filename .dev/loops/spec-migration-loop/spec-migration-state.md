# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 19）
- 目前階段：階段 2（F02 遷移）進行中
- 上一輪任務：上一輪驗證 PASS；本輪任務：D-04（[F02] 修正 T2.04～T2.06 的活動紀錄措辭與拒絕類 uc 角色）｜結果：done
- 下一個任務：T2.08（[F02]「檢視看板活動紀錄」usecase 區塊＋tag＋Aggregate 註解；若有 emits 事件用 requires 接上）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02 已完成「建立使用者帳號」「使用者登入與登出」「Board 建立與成員邀請」「Board 權限管理」「Board 存取權限」「卡片負責人指派」，並修正 D-04 的活動紀錄措辭與拒絕類 uc 角色；F02 error 數維持 26（全部 spec 184，未變動，D-04 只是措辭與角色修正，不影響 error 數）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；F02 已定義實體 user、board-membership，角色 r-system-user、r-board-owner、r-board-member；uc-invite-member／uc-change-member-role／uc-remove-member 的活動紀錄句改記在 board-membership；uc-reject-invite-by-member／uc-reject-role-change-by-member／uc-reject-structure-change-by-member 的 roles 改為 [r-board-member]，uc-reject-board-access-by-nonmember 改為 [r-system-user]
- 最近 3 條假設：本輪新增 OQ-07（拒絕類 uc 的 roles 依 Background／Feature 標頭補上被拒絕的操作者角色，不留空），已在 OQ 檔追加、F02「## 待釐清」已加一行指向它
- 待注意：全部 spec error 總數 184；Iteration 11 標題含兩個任務編號、不符 HEADING 正則，依鐵則 4 不可再修改；F02 尚有 T2.08～T2.09 待做
