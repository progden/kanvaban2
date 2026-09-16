# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 11）
- 目前階段：階段 2（F02 遷移）進行中
- 上一輪任務：G1｜結果：done（審查通過，任務清單已標 done）；本輪任務：T2.01｜結果：done
- 下一個任務：T2.02（[F02]「建立使用者帳號」usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02 遷移程序 1～6 完成（狀態行、實體／欄位／關係／其他名詞四張表、角色定義三個角色、七個 Feature 的「身為」行），usecase 區塊（程序 7～9）尚未開始，F02 error 數 125→94
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；新增 F02 實體 user、board-membership；新增角色 r-system-user、r-board-owner、r-board-member
- 最近 3 條假設：新增 OQ-04（F02 角色分組：「卡片負責人指派」「檢視看板活動紀錄」「Board 存取權限」改用通用角色 r-system-user，不套用 r-board-member，避免把 Owner 排除在外；Owner／Member 專屬權限差異只用於「Board 建立與成員邀請」「Board 權限管理」）；沿用 OQ-01～OQ-03（F01）
- 待注意：全部 spec error 總數 283→252（F02 尚未遷完，T2.02～T2.09 待做）；F02 的 Aggregate 註解仍是舊名 `boardMembership`（尚未改成 `board-membership`），留給 T2.02～T2.08 各 Feature 一併處理
