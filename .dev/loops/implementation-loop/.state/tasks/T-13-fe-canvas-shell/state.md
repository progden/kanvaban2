# T-13-fe-canvas-shell state

> 2026-09-22 Review 第 3 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

Review 第 3 輪：附保留核准，status＝done。

- 自跑驗證：`pnpm test` 43/43 通過（6 檔）、`pnpm run build` 通過、`pnpm run lint` 僅 1 則既有非阻斷 warning。
- D-05 三點全數滿足：`uc-set-item-anchor` 換算值（-12.5／-25／1125／750）與 `geometry.ts` 手算一致、批次移除確認流程、批次移除 409 失敗路徑（訊息顯示且兩元件仍在）。
- 十一個 uc 全部有前端測試對應；抽查 `@uc-move-item @fail-p2`、`@uc-remove-items @fail-p2`，「拒絕、顯示訊息、資料不變」成立。
- 邊界乾淨：diff 只有 `kanban-frontend/src/**` 與 `.state/tasks/T-13-fe-canvas-shell/**`，未動 `kanban-core`／`kanban-spring`／spec／ui／conventions／scripts。
- 無阻塞 OQ；`OQ-T-13-fe-canvas-shell-01`（等級高、不阻塞、接手人工）維持待處理。

保留事項與接手者：
1. OQ-01（ui 文件角色表／待確認事項過時）→ 人工。
2. Swimlane／Stage 管理入口 → T-14-fe-board-item。
3. 看板成員 item 掛載 → T-15-fe-member-management。
4. `uc-set-viewport` p1 在畫面上無法觸發（clampZoom 事前夾住）→ 目前無任務接手，若日後開放輸入縮放值由屆時修改 `s-canvas` 的任務處理。
