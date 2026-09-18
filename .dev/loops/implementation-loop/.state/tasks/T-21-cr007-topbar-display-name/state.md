# T-21-cr007-topbar-display-name state

> 2026-09-19 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪：退回（doing）。
- 建置／測試自行重跑全綠（gradle clean build、pnpm test 16/16、tsc、oxlint）。
- 邊界乾淨、kanban-core 未動、無 OQ。
- D-01：後端缺少 `GET /api/session` 回傳 `displayName` 的測試（CR-007 驗收標準明列）。
下一輪 Dev：只需補 D-01 的測試，不需改動其他程式。
