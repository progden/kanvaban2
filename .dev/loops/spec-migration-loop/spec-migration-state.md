# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 28）
- 目前階段：階段 3（F04 遷移進行中）
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.15（[F04]「看板時間管理」usecase 區塊＋tag＋Aggregate 註解）｜結果：done
- 下一個任務：T2.16（[F04] 遷移程序 10～11 與收尾，F04 全檔 0 error）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03（全檔 0 error）；F04「看板時間管理」Feature 段落 0 error，正文（簡介、變更紀錄、決議紀錄、待釐清）反引號清理與變更紀錄收尾待做
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user）、F02（r-board-owner）；F04 新增 3 個 uc：uc-adjust-board-clock、uc-guard-clock-monotonicity、uc-pause-resume-board-clock（皆 crud={board:U}），未新增實體／角色 ID
- 最近 3 條假設：Scenario「把看板時間調整到未來後建立卡片」因 GH-01（一個 Scenario 恰一個 @uc-）與 UC-06（每個 uc 至少一個成功 Scenario）改掛 uc-guard-clock-monotonicity（而非其 When 動作所屬的調整時鐘），因為該情境是唯一能證明「時鐘未落後時允許寫入新事件」的成功案例（低影響格式分組，非行為變更）；「非Owner嘗試調整」與「早於最後事件不可建立新事件」兩情境的 fail 訊息依 gherkin 原文逐字引用；暫停／恢復合併為單一 uc-pause-resume-board-clock，因 Scenario「暫停或恢復看板時間應記錄一筆活動紀錄」在同一情境內操作兩個動作，受 GH-01 限制只能掛一個 uc
- 待注意：全部 spec error 總數 110 → 100（F01=0、F02=0、F03=0、F04=48→38、F05=32、F06=30）
