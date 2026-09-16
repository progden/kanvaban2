# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 42）
- 目前階段：階段 3（跨模組收尾）卡在 T3.02
- 上一輪任務：T3.02（`.dev/CR.md` 回填 CR-001～CR-005 影響 ID，跑 `cr-check` 到通過）｜結果：blocked（CR-01/03/04/05 皆 0 error，但 GH-05 對 68 個新掛 `@uc-`／`@fail-` tag 的 Scenario 報「無 @CR- tag」，補掛會違反鐵則 1／tag-diff，屬環境限制，見 OQ-10）
- 下一個任務：無（`actionable` 只剩 T3.02，已 blocked）；需人工處理 OQ-10 後把 T3.02 改回 `todo`
- 進行中任務的剩餘工作：T3.02 —— `.dev/CR.md` 影響 ID 已填齊且與 diff 精準對齊（CR-01 通過）；`crcheck(CR-005)=0` 卡在 GH-05，待人工決定是否修正 `gh_05_diff` 或改驗收方式
- 已遷移完成的模組：F01～F06 全部完成，全檔 0 error；`./scripts/spec-check` 總計 0 error(s)、0 warning(s)
- 已定義的共用 ID：無新增
- 最近 3 條假設：OQ-10（環境限制／blocked，cr-check 的 GH-05 diff 版與鐵則 1 的 tag-diff 限制互相矛盾，純格式遷移無法讓 `crcheck(CR-005)` 歸零）
- 待注意：`.dev/CR.md` 已存在（T0.02 建立），本輪只補影響 ID 欄；人工需看 OQ-10 決定後續（例如另開 CR 調整 `gh_05_diff` 排除純格式異動，或接受 T3.02 用別的條件驗收）
