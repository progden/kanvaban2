# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 12）
- 目前階段：階段 2（F02 遷移）進行中
- 上一輪任務：上一輪驗證 FAIL（PDCA Iteration 11 標題格式），本輪先修正（改成單一任務編號 `T2.01`）；本輪任務：T2.02｜結果：done
- 下一個任務：T2.03（[F02]「使用者登入與登出」usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02「建立使用者帳號」Feature 已完成 usecase 區塊＋tag＋Aggregate 註解（`uc-create-user`），F02 error 數 94→86
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；F02 已定義實體 user、board-membership，角色 r-system-user、r-board-owner、r-board-member；新增 uc：uc-create-user
- 最近 3 條假設：無新增 OQ；沿用 OQ-01～OQ-04
- 待注意：全部 spec error 總數 252→244；「密碼長度超過 40 字則建立失敗」「帳號 ID（username）不可重複」兩個 fail Scenario 的 Aggregate 註解由 `user: write` 改為 `user: read`（依遷移程序步驟 9，失敗情境只標 read），屬低影響格式對齊；F02 尚有 T2.03～T2.09 待做
