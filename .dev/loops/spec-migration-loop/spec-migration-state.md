# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 24）
- 目前階段：階段 3（F03 遷移）進行中
- 上一輪任務：上一輪驗證 PASS；本輪任務：D-05（[F02] 修正 uc-view-board-activity-log 的 post 措辭）｜結果：done
- 下一個任務：T2.12（[F03] 「Throughput 與累積流量圖」與「截止日期提醒」兩個 Feature 的 usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03 已完成遷移程序 1～6 與前兩個 Feature 的 usecase 區塊（7～9），尚餘「Throughput 與累積流量圖」「截止日期提醒」的 usecase 區塊＋正文清理＋變更紀錄
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）與 F02（user、board-membership；r-system-user、r-board-owner、r-board-member）；F03 已新增 uc-view-cycle-lead-time、uc-view-wip、uc-view-aging-wip（均 roles: [r-user]，純讀取 uc）
- 最近 3 條假設：無新增假設；D-05 為純措辭修正，未新增 OQ
- 待注意：全部 spec error 總數 140（F01=0、F02=0、F03=24、F04=54、F05=32、F06=30，本輪未變動）
