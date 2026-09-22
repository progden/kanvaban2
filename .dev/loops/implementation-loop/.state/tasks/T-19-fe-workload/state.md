# T-19-fe-workload state

> 2026-09-22 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

2026-09-22 Review 第 1 輪判定：退回（`doing`），待 Dev 第 2 輪處理 D-01、D-02。

Review 實跑結果（`kanban-frontend/`）：`pnpm run test -- --run` 10 檔 61 案全過；`pnpm run build` 成功、無型別錯誤；`pnpm run lint` 僅既有 `itemComponentRegistry.tsx` fast-refresh 警告。程式碼與 spec／ui 對應無缺漏（拖放目標端與卡片詳情導覽兩處留白已知且合理），任務邊界乾淨（只動 `kanban-frontend/**` 與自己的 `.state/tasks/T-19-fe-workload/**`＋新增 ADR），視覺沿用既有語彙、無設計稿註記外洩。

退回原因（都不是程式碼問題，是紀錄與 OQ 歸屬）：

- D-01：`WorkloadDashboard.tsx` 讓 `r-board-viewer` 不可拖曳頭像，與 `ui-workload.md` 定稿『無角色差異』矛盾，屬規則書第 5 節的高風險決定，必須開 OQ（高／不阻塞），不能只記在 decision-log。
- D-02：`OQ-T-19-fe-workload-01` 的「接手：T-14-fe-board-item」只對「拖放目標端」那一半；『點擊清單中的卡片開啟 F01 `s-card-detail`』要改的是 `s-cards-by-assignee`，不在 `tasks.md` 登記的 T-14 產出範圍內，等於無人接手，須拆成兩則並把接手者填對。

下一輪 Dev 只需補 OQ 與 decision-log 的待確認事項，程式碼維持現狀即可；改完後 Review 會重跑一次測試再核准。
