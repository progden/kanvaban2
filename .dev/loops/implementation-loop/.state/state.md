# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-11-fe-auth：Dev 完成，狀態改 `review-pending`，等 Review。
- 實作 `s-login`（`LoginPage.tsx`）、`s-signup`（`SignupPage.tsx`），取代 T-10 佔位元件；沿用 T-10 的 `AuthProvider`／`authApi`／`ApiError`，未修改。
- 新增 `LoginPage.test.tsx`／`SignupPage.test.tsx`；`App.test.tsx`（T-10 範圍）因佔位文字被取代，改用 `getByRole('heading', …)` 選取器，行為斷言未變。
- `pnpm run test`（16 個測試全過）、`pnpm run lint`、`pnpm run build` 皆通過。
- 新增 OQ-IMPL-12：看不到 `Login.dc.html`／`Signup.dc.html` 設計稿內容，先用純語意 HTML 表單版面，標記「待對照設計稿」，待處理不阻擋本任務。
- 下一步：Review 進本 worktree 重跑建置／測試，核對 `ui-user-membership.md` s-login／s-signup 操作表與驗收條件。
