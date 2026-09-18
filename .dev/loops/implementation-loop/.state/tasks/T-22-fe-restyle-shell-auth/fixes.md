# T-22-fe-restyle-shell-auth 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | 品牌 Logo 的 3x3 網格配色跟設計稿不一致。`kanban-frontend/src/index.css` 的 `.app-topbar__logo span:nth-child(3n + 2)` 把第 2、5、8 格設成淺色 `#A9B6D6`；`BoardList.dc.html`／`Login.dc.html`／`Signup.dc.html` 的 Logo 逐列是『#1F4BD8 #1F4BD8 #A9B6D6／#A9B6D6 #1F4BD8 #1F4BD8／#1F4BD8 #A9B6D6 #1F4BD8』，淺色應該是第 3、4、8 格。 怎樣算修好：TopBar、s-login、s-signup 三處 Logo 的淺色格都是第 3、4、8 格，其餘為主色。 |
| D-02 | 1 | todo | — | 共用樣式基礎少了設計稿的文字色 `#3B4756`。設計稿三份檔共用的 `.lbl`（欄位標籤）、`.btn2`（次要按鈕）、`.btnsm`（小按鈕，TopBar 登出）都是 `color:#3B4756`；`kanban-frontend/src/index.css` 的 `.field-label`、`.btn-secondary`、`.btn-sm` 卻用 `--color-text-muted`（`#5C6878`，設計稿裡這是副標／說明文字的顏色）。註冊頁說明卡標題（設計稿 `color:#3B4756`）也一樣用錯。這個任務是之後所有前端任務共用的樣式基礎，色票錯了會一路擴散。 怎樣算修好：`:root` 補上對應 `#3B4756` 的色票變數，`.field-label`、`.btn-secondary`、`.btn-sm`、`.auth-brand__note-title` 改用它；其餘顏色再跟設計稿 `<style>` 逐項核對一次。 |
| D-03 | 1 | todo | — | `s-login`／`s-signup` 版面尺寸與元素跟設計稿有多處出入，而且決策紀錄沒寫理由： - 左側品牌欄：設計稿 `width:540px`、`padding:56px 48px`；`AuthPage.css` 的 `.auth-brand` 是 `380px`、`40px 36px`。 - 右側表單：設計稿 `padding:0 88px`，沒有最大寬度；`.auth-form` 是 `padding: 0 64px` 加 `max-width: 420px`。 - 品牌欄 Logo：設計稿是 `26px` 網格、產品名稱 `font-size:15px`；實作沿用 TopBar 的 `20px`／`13px`。 - 登入頁標語：設計稿 `font-size:30px`、`margin:0 0 28px`；`.auth-brand__tagline` 是 `24px`、沒有下邊距。 - 登入頁品牌欄的看板示意插圖（白底卡片裡 3 個標題條＋6 格卡片）整塊沒做。標語跟插圖在設計稿同一則「⚠️ 規格未定義：登入頁的品牌敘述、產品名稱與插圖」裡，實作卻只保留標語。 怎樣算修好：以上各項照設計稿做；如果有意偏離（例如為了窄螢幕），在 decision-log 逐項寫出偏離內容與理由。插圖要做就照設計稿，不做就寫明理由，同時跟 D-04 的 OQ 一致處理。 |
