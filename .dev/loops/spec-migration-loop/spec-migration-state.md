# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 25）
- 目前階段：階段 3（F03 遷移）進行中
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.12（[F03] Throughput 與截止日期提醒兩 Feature 的 usecase 區塊）｜結果：done
- 下一個任務：T2.13（[F03] 遷移程序 10～11 與收尾，F03 全檔 0 error）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03 四個 Feature 的 usecase 區塊已全部完成（1～9），尚餘遷移程序 10（正文清理：反引號、`design.md`→`design-kanban-widgets.md`）與 11（變更紀錄改四欄格式＋補 CR-005 列）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）與 F02（user、board-membership；r-system-user、r-board-owner、r-board-member）；F03 已新增 uc-view-cycle-lead-time、uc-view-wip、uc-view-aging-wip、uc-view-throughput、uc-view-cfd、uc-view-duedate-reminder（均 roles: [r-user]，純讀取 uc）
- 最近 3 條假設：Throughput 與 CFD 依不同 When 動作拆成兩個 uc；截止日期提醒兩 Scenario 因共用同一 When 合併為一個 uc；均為低影響格式選擇，未記 OQ
- 待注意：全部 spec error 總數 132（F01=0、F02=0、F03=16、F04=54、F05=32、F06=30，本輪 F03 由 24 降至 16）
