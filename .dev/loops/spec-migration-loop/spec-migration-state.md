# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 31）
- 目前階段：階段 3（F04 遷移進行中）
- 上一輪任務：上一輪驗證 FAIL（D-06 的 changelog-check）；本輪先修正 F03 變更紀錄的 design.md 註記位置，再完成 D-07（[F04] 補記 uc-guard-clock-monotonicity 的 OQ-08）｜結果：done
- 下一個任務：T2.16（[F04] 遷移程序 10～11 與收尾，F04 全檔 0 error）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03（全檔 0 error）；F04「看板時間管理」Feature 段落 0 error，正文（簡介、變更紀錄、決議紀錄、待釐清）反引號清理與變更紀錄收尾待做
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user）、F02（r-board-owner）；F04 新增 3 個 uc：uc-adjust-board-clock、uc-guard-clock-monotonicity、uc-pause-resume-board-clock（皆 crud={board:U}），未新增實體／角色 ID
- 最近 3 條假設：D-07 補記 OQ-08（uc-guard-clock-monotonicity 建模歸屬維持現狀，不改 usecase／gherkin）；F03 changelog 的 design.md 註記改為附加於句尾（避免打斷原摘要文字的連續性，通過 changelog-check）
- 待注意：全部 spec error 總數 100（不變）；F01=0、F02=0、F03=0、F04=38、F05=32、F06=30
