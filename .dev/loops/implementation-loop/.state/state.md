# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-11-fe-auth：Review 第 1 輪**退回**，狀態改回 `doing`，待處理 D-05～D-07（見 `tasks.md`、`review.md`）。
- Review 重跑結果：`pnpm test` 16 個測試全過，`pnpm build`、`pnpm lint`、`./gradlew build -q` 都 exit 0；diff 只在 `kanban-frontend/src/**` 和 `.state/**`。
- D-05：`ui-user-membership.md` 第 45 行寫「帳號重複…不觸發 `uc-create-user`」，實作會送 API，要開 OQ（高風險），程式碼先不改。
- D-06：`LoginPage.test.tsx` 要補「TopBar 不顯示帳號名稱」，以及 fail-p1 的欄位保留、停留本畫面斷言。
- D-07：`SignupPage.test.tsx` 的空帳號測試要補「畫面維持顯示」斷言。
- 待確認事項：OQ-IMPL-12（設計稿無法存取）待處理；D-05 要新開的 OQ。
- 下一步：Dev 在同一個 worktree 處理 D-05～D-07，再回到 `review-pending`。
