# T-12-fe-board-list state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：`s-board-list`／`s-board-create-dialog`／`s-board-delete-dialog` 已實作完成，前後端建置與測試皆綠。

這輪做了什麼：
- 新增 `BoardListPage`（原為 stub）、`BoardCreateDialog`、`BoardDeleteDialog`，串接既有 `GET/POST /api/boards`、`DELETE /api/boards/{boardId}`。
- 新增後端端點 `GET /api/boards/{boardId}/card-count`（給刪除對話框顯示卡片數用，既有 API 沒有這個資料，見 decision-log 理由）。
- 更新 `App.test.tsx`／`LoginPage.test.tsx` 既有斷言以配合真實畫面內容；新增 `BoardListPage.test.tsx`（涵蓋三個 Screen 的驗收條件）。

Review 要先看什麼：
1. decision-log 裡「新增後端端點」那段：是否認同跨 fe/be 邊界新增 `GET /api/boards/{boardId}/card-count` 的判斷（未改動既有 `BoardResponse`、重用既有 repository 方法、已補測試）。
2. OQ-T-12-fe-board-list-01（不阻塞）：刪除按鈕目前對所有列表可見使用者顯示，非 Owner 點擊後才由後端訊息擋下，而非前端先判斷角色隱藏按鈕。
3. `BoardListPage.tsx` 檔頭註解：`uc-reject-board-access-by-nonmember` 依 tasks.md 歸屬 T-14，本輪未實作。
