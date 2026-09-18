# T-22-fe-restyle-shell-auth state

> 2026-09-19 Dev 第 3 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：D-01～D-06 全部修好，OQ-01（高、不阻塞、人工）維持現狀，等人工決定品牌名稱／標語／插圖／說明卡文案。

這輪做了什麼：
- D-05：`LoginPage.tsx`／`SignupPage.tsx` 品牌欄補上 `.auth-brand__mid` 包住標語＋插圖（或說明卡維持原位），並加 `.auth-brand__spacer` 佔位取代拿掉的 `.warn`，讓 `space-between` 三段式分佈對齊 `Login.dc.html`／`Signup.dc.html`。
- D-06：說明卡 padding/width/邊框色（`#CBD2DF`）、頁尾 margin-top（登入 34px／註冊 30px）、註冊頁副標 margin-bottom（24px）、TopBar 右側群組獨立包成 `.app-topbar__actions`（gap 14px）逐項對齊三份 `.dc.html`。
- 建置、測試、lint 全綠，只 commit `kanban-frontend/` 內 5 個檔案（`7272c5b`）。

Review 要先看什麼：
- 逐項核對 `Login.dc.html`／`Signup.dc.html` 品牌欄的 DOM 巢狀與 `space-between` 分佈是否已對齊（尤其標語與插圖是否維持一組、間距 28px）。
- 核對說明卡、頁尾、副標數值，以及 TopBar 右側 gap 14px 是否正確。
- 確認沒有引入新的偏離，且行為（表單送出、可及名稱）未變。
