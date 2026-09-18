# T-22-fe-restyle-shell-auth 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | 品牌 Logo 的 3x3 網格配色跟設計稿不一致。`kanban-frontend/src/index.css` 的 `.app-topbar__logo span:nth-child(3n + 2)` 把第 2、5、8 格設成淺色 `#A9B6D6`；`BoardList.dc.html`／`Login.dc.html`／`Signup.dc.html` 的 Logo 逐列是『#1F4BD8 #1F4BD8 #A9B6D6／#A9B6D6 #1F4BD8 #1F4BD8／#1F4BD8 #A9B6D6 #1F4BD8』，淺色應該是第 3、4、8 格。 怎樣算修好：TopBar、s-login、s-signup 三處 Logo 的淺色格都是第 3、4、8 格，其餘為主色。 |
