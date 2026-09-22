# T-12-fe-board-list state

> 2026-09-22 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

2026-09-22 Review 第 1 輪判定退回，status＝`doing`，待 Dev 第 2 輪處理 D-01～D-03。

自己重跑的驗證結果：
- `pnpm run build`（kanban-frontend）成功；`pnpm run test` 5 檔 25 測試全過；`pnpm exec oxlint` exit 0。
- `./gradlew test --no-daemon --rerun-tasks`：BUILD SUCCESSFUL（9 tasks executed）。
- 建置／測試全綠，`kanban-core` 未被動到，邊界沒有越到別的任務目錄或文件本體。

退回原因（都不是建置問題，是規格對應與紀錄缺口）：
- D-01：`s-board-list` 驗收條件「選擇列表中的 Board 後開啟該 Board」沒有測試。
- D-02：`uc-reject-board-access-by-nonmember` 延後 T-14，只寫在 decision-log，沒開成 OQ。
- D-03：OQ-T-12-fe-board-list-01 引文不逐字，且未並列設計稿「角色僅 r-board-owner」這項反證。

已接受的越界（不需修）：`kanban-spring` 新增 `GET /api/boards/{boardId}/card-count`，因 `s-board-delete-dialog` 已定案要顯示卡片數而既有端點都沒有，純新增且已補測試。

Dev 第 2 輪只要處理 D-01～D-03，不要再擴大改動範圍。
