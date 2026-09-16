# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 30）
- 目前階段：階段 3（F04 遷移進行中）
- 上一輪任務：上一輪驗證 PASS；本輪任務：D-06（[F03] 補完 T2.13 漏做的遷移程序 10，修正 `design.md` 檔名引用）｜結果：done
- 下一個任務：T2.16（[F04] 遷移程序 10～11 與收尾，F04 全檔 0 error）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03（全檔 0 error，D-06 修正變更紀錄檔名引用）；F04「看板時間管理」Feature 段落 0 error，正文（簡介、變更紀錄、決議紀錄、待釐清）反引號清理與變更紀錄收尾待做
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user）、F02（r-board-owner）；F04 新增 3 個 uc：uc-adjust-board-clock、uc-guard-clock-monotonicity、uc-pause-resume-board-clock（皆 crud={board:U}），未新增實體／角色 ID
- 最近 3 條假設：D-06 純文字修正，無新假設；T2.15 中 Scenario「把看板時間調整到未來後建立卡片」因 GH-01 與 UC-06 改掛 uc-guard-clock-monotonicity（低影響格式分組，非行為變更）；暫停／恢復合併為單一 uc-pause-resume-board-clock，因單一 Scenario 橫跨兩動作、受 GH-01 限制只能掛一個 uc
- 待注意：全部 spec error 總數 100（不變，D-06 為純文字修正）；F01=0、F02=0、F03=0、F04=38、F05=32、F06=30；剩餘 D-07（[F04] 補記 OQ）待排入
