# T-15-fe-member-management state

> 2026-09-22 Review 第 2 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

Review 第 2 輪核准完成（附保留），任務轉 `done`，可合併回 `loop/implementation`。

- 自行驗證：`pnpm test` 55/55 通過（8 檔）、`pnpm run build` 通過、`pnpm run lint` 僅 T-13 既有 1 則 warning。
- `D-01`（移除成員一律先確認）、`D-02`（Viewer 列不顯示「設為 Owner」）皆已修好並有對應測試。
- `s-member-management` 八條驗收條件全數有測試；`@uc-remove-member @fail-p1` 抽查成立（訊息逐字相符、清單不重新載入、資料不變）。
- 邊界乾淨：只動 `kanban-frontend/src/canvas/members/**`、`src/api/boardMembershipApi.ts`（純追加）、`src/pages/BoardCanvasPage.tsx`（3 行）與自己的 `.state/tasks/T-15-fe-member-management/`。
- 不涉及 `kanban-core`／`kanban-spring`。

保留事項（皆不阻塞，接手者已在審查紀錄逐條列出）：
1. `OQ-01` `item.component` 值 `"board-members"` 待 F07 整合 CR 核對 — 人工。
2. `OQ-02` 問題 (2) `r-board-member` 嘗試移除成員的拒絕情境缺 usecase — 人工。
3. `OQ-02` 問題 (1) 已由 `D-01` 結案，OQ 內文推論過時待補解除說明 — 人工。
4. `OQ-03` Viewer 角色在 ui 資料表／角色權限表缺漏，需走 CR — 人工；後端 `changeMemberRole` 缺 pre p2 檢查屬 T-04 範圍。
