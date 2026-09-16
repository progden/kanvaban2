# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 13）
- 目前階段：階段 2（F02 遷移）進行中
- 上一輪任務：上一輪驗證 FAIL（PDCA 舊內容被修改：Iteration 11 標題被改動），本輪先復原該標題文字（改回與 base 一致，等於未變動），不再嘗試修正既有標題格式；本輪任務：T2.03｜結果：done
- 下一個任務：T2.04（[F02]「Board 建立與成員邀請」usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02 已完成「建立使用者帳號」（uc-create-user）、「使用者登入與登出」（uc-login、uc-logout），F02 error 數 86→79
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；F02 已定義實體 user、board-membership，角色 r-system-user、r-board-owner、r-board-member；新增 uc：uc-create-user、uc-login、uc-logout
- 最近 3 條假設：登出 Scenario 原本無 Aggregate 註解，補上 `user: read`（對齊 crud，低影響）；login post 避免引用 `user.username`（UC-07 視為更新），改引用實體 `user` 本身
- 待注意：全部 spec error 總數 244→237；Iteration 11 標題 `## Iteration 11 — 2026-09-16 15:10 — G1、T2.01` 含兩個任務編號、不符 HEADING 正則，但依鐵則 4（PDCA 只能追加）不可再修改，維持原樣即可，之後輪次不要再嘗試改它；F02 尚有 T2.04～T2.09 待做
