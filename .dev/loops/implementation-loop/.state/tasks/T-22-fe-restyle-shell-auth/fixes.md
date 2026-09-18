# T-22-fe-restyle-shell-auth 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | 品牌 Logo 的 3x3 網格配色跟設計稿不一致。`kanban-frontend/src/index.css` 的 `.app-topbar__logo span:nth-child(3n + 2)` 把第 2、5、8 格設成淺色 `#A9B6D6`；`BoardList.dc.html`／`Login.dc.html`／`Signup.dc.html` 的 Logo 逐列是『#1F4BD8 #1F4BD8 #A9B6D6／#A9B6D6 #1F4BD8 #1F4BD8／#1F4BD8 #A9B6D6 #1F4BD8』，淺色應該是第 3、4、8 格。 怎樣算修好：TopBar、s-login、s-signup 三處 Logo 的淺色格都是第 3、4、8 格，其餘為主色。 |
| D-02 | 1 | todo | — | 共用樣式基礎少了設計稿的文字色 `#3B4756`。設計稿三份檔共用的 `.lbl`（欄位標籤）、`.btn2`（次要按鈕）、`.btnsm`（小按鈕，TopBar 登出）都是 `color:#3B4756`；`kanban-frontend/src/index.css` 的 `.field-label`、`.btn-secondary`、`.btn-sm` 卻用 `--color-text-muted`（`#5C6878`，設計稿裡這是副標／說明文字的顏色）。註冊頁說明卡標題（設計稿 `color:#3B4756`）也一樣用錯。這個任務是之後所有前端任務共用的樣式基礎，色票錯了會一路擴散。 怎樣算修好：`:root` 補上對應 `#3B4756` 的色票變數，`.field-label`、`.btn-secondary`、`.btn-sm`、`.auth-brand__note-title` 改用它；其餘顏色再跟設計稿 `<style>` 逐項核對一次。 |
