# T-22-fe-restyle-shell-auth state

> 2026-09-19 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：TopBar／`s-login`／`s-signup` 已依設計稿（`BoardList.dc.html`／`Login.dc.html`／`Signup.dc.html`）改版，建立共用樣式基礎（`index.css` 色票字體、按鈕／輸入框／對話框／錯誤訊息類別），只動版面與樣式，行為不變。

這輪做了什麼：改 `index.html`／`index.css`／`AppShell.tsx`（新增 `AppShell.css`）／`LoginPage.tsx`／`SignupPage.tsx`（新增 `AuthPage.css`）。`pnpm test`／`pnpm lint`／`pnpm build` 皆通過（16 測試全過）。無 OQ。

Review 要先看什麼：
1. 對照 `Login.dc.html`／`Signup.dc.html`／`BoardList.dc.html` 的版面結構、色票（`#1F4BD8` 等）、字體是否忠實還原。
2. `LoginPage.test.tsx`／`SignupPage.test.tsx`／`App.test.tsx` 的 `getByLabelText`／`getByRole` 斷言是否仍對應正確元素（尤其 `顯示名字`／`密碼` 欄位提示文字沒有污染 accessible name 的寫法）。
3. 確認沒有動到 T-21 已完成的 TopBar 顯示名字邏輯（CR-007）本身的行為，只動版面。
