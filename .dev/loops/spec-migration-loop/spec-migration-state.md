# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 32）
- 目前階段：階段 2（F05 遷移即將開始）
- 上一輪任務：上一輪驗證 FAIL（PDCA 標題格式，Iteration 31）——查明該格式錯誤屬已提交的舊內容，依 PDCA 只能追加的規則無法回溯修正，本輪僅確保新追加標題格式正確；完成 T2.16（F04 遷移程序 10～11：正文反引號清理＋變更紀錄收尾加 CR-005 列）｜結果：done
- 下一個任務：T2.17（[F05] 遷移程序 1～6：狀態行、名詞／角色表、「身為」行）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03（全檔 0 error）；F04（全檔 0 error）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user）、F02（r-board-owner）；F04 新增 3 個 uc（uc-adjust-board-clock、uc-guard-clock-monotonicity、uc-pause-resume-board-clock，皆 crud={board:U}），未新增實體／角色 ID
- 最近 3 條假設：F04 正文反引號清理採「檔案路徑僅限現存 .md／.feature 等真實檔名」原則，`board-clock.feature`（非本 repo 現存檔）改用「」而非反引號；F04 變更紀錄末列追加 CR-005；PDCA Iteration 31 標題格式錯誤因只能追加而無法回溯修正，僅在本則記錄說明，不記 OQ（純流程格式問題，非規格假設）
- 待注意：全部 spec error 總數 62（100 → 62）；F01=0、F02=0、F03=0、F04=0、F05=32、F06=30
