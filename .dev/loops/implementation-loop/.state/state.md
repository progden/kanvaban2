# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-10-fe-shell：Dev 輪完成，狀態改為 `review-pending`，等 Review。
- 產出：`kanban-frontend/src/api`（`http.ts` fetch 包裝＋`ApiError`、`authApi.ts` 對應 `UserController` 四個端點）、`src/auth`（`AuthContext`／`useAuth`／`ProtectedRoute`／`GuestOnlyRoute`）、`src/layout/AppShell.tsx`（TopBar：帳號名稱＋登出）、`src/pages`（`LoginPage`／`SignupPage`／`BoardListPage`／`BoardCanvasPage` 四個佔位頁面，內容待 T-11／T-12／T-13 實作）、`src/App.tsx` 路由定義、`main.tsx` 包 `BrowserRouter`；新增 `react-router-dom` 依賴、`vite.config.ts` 加 `/api` proxy。
- 測試：`src/api/http.test.ts`（成功／204／失敗三種情境）、`src/App.test.tsx`（未登入導向登入頁、未登入擋 `/boards`、已登入顯示 TopBar 帳號名稱並可登出、已登入擋 `/login`）共 7 個測試，`pnpm run test` 全過；`pnpm run build`／`pnpm run lint` 皆通過（lint 0 警告）；根目錄 `./gradlew clean build --no-daemon` 也重跑過確認沒有波及後端。
- 待確認事項／新開 OQ：無。OQ-IMPL-09（HTTP 狀態碼未定案）不影響本任務，`ApiError` 只帶狀態碼與訊息，呼叫端預期依訊息內容判斷，見 `decision-log.md`。
- 對應 spec／ui：本任務不涵蓋任何 `uc-`／Scenario（純前端骨架），`s-login`／`s-signup`／`s-board-list`／`s-canvas` 四個畫面本體留給 T-11／T-12／T-13，只在此建立路由掛載點與登入態守衛。
