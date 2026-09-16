# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 26）
- 目前階段：階段 3（F03 遷移）完成，F04 尚未開始
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.13（[F03] 遷移程序 10～11 與收尾，F03 全檔 0 error）｜結果：done
- 下一個任務：T2.14（[F04] 遷移程序 1～6：狀態行、名詞／角色表、「身為」行；「決議紀錄」段落保留）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03（全檔 0 error，遷移完成）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）與 F02（user、board-membership；r-system-user、r-board-owner、r-board-member）；F03 新增 uc-view-cycle-lead-time、uc-view-wip、uc-view-aging-wip、uc-view-throughput、uc-view-cfd、uc-view-duedate-reminder（均 roles: [r-user]，純讀取 uc）
- 最近 3 條假設：正文清理時 `kanban-spring`、`asOf`、`Start`／`Done`、`read`／`write` 等非實體 ID 反引號改「」；變更紀錄舊列票號 F03 改留空並加「（原票號 F03）」；變更紀錄歷史列中的 `design.md` 因 changelog-check 要求摘要逐字保留，未改成 `design-kanban-widgets.md`（均為低影響格式選擇，未記 OQ）
- 待注意：全部 spec error 總數 116（F01=0、F02=0、F03=0、F04=54、F05=32、F06=30）
