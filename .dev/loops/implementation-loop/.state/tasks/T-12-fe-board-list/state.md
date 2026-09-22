# T-12-fe-board-list state

> 2026-09-22 Review 第 2 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

Review 第 2 輪：附保留核准，status=done。

- 建置／測試（自跑）：`pnpm run build` 成功；`pnpm run test` 5 檔 26 則全過；`pnpm exec oxlint` exit 0；`./gradlew test --no-daemon --rerun-tasks` BUILD SUCCESSFUL（3m 11s）。工作區乾淨。
- D-01（導覽測試）、D-02（OQ-02）、D-03（OQ-03 逐字引文＋並列反證）三條皆驗收通過，引文已回源頭逐字比對。
- 邊界：`kanban-core` 零改動；`kanban-spring` 僅新增 `countActiveCards`／`card-count` 端點／`CardCountResponse`／兩則測試；`.state/` 只動本任務目錄。
- 設計稿註記未外洩到產品畫面；「需確認？」欄已落實。
- 本輪新開 `OQ-T-12-fe-board-list-04`（高／不阻塞／接手 人工）補上 03 漏列的 ui s-board-delete-dialog 角色與權限表原文，不改變不阻塞判定。

保留事項（接手者）：OQ-01→人工（補解除說明）；OQ-02→T-14-fe-board-item；OQ-03＋04→人工（需 CR）；`card-count` 端點→本任務已完成，供 T-02-be-board 未來修訂實例知悉。
