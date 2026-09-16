# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 14）
- 目前階段：階段 2（F02 遷移）進行中
- 上一輪任務：上一輪驗證 PASS；本輪任務：D-03（修正 OQ-04 的角色分組，「卡片負責人指派」「檢視看板活動紀錄」改沿用 F01 的 `r-user`）｜結果：done
- 下一個任務：T2.04（[F02]「Board 建立與成員邀請」usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02 已完成「建立使用者帳號」（uc-create-user）、「使用者登入與登出」（uc-login、uc-logout），F02 error 數維持 79（D-03 未新增 usecase 區塊，只修正角色描述）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；F02 已定義實體 user、board-membership，角色 r-system-user、r-board-owner、r-board-member；「卡片負責人指派」「檢視看板活動紀錄」兩個 Feature 標頭沿用 F01 的 r-user，F02 角色表不重列；新增 uc：uc-create-user、uc-login、uc-logout
- 最近 3 條假設：D-03 為高影響修正（OQ-05），已在 OQ 檔追加並於 F02 `## 待釐清` 指向；OQ-04 原列保留不改；「Board 存取權限」維持沿用 r-system-user
- 待注意：全部 spec error 總數維持 237；Iteration 11 標題含兩個任務編號、不符 HEADING 正則，依鐵則 4 不可再修改；F02 尚有 T2.04～T2.09 待做
