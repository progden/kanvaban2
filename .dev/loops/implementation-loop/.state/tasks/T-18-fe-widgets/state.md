# T-18-fe-widgets state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現況：F03 四個儀表板 Canvas item（s-cycle-lead-time-dashboard／s-wip-dashboard／
s-throughput-cfd-dashboard／s-duedate-reminder）已實作完成，串接 T-06 六個
uc-view-* 端點，透過 T-13 的 registerItemComponent 掛上 canvas。

本輪做了什麼：
- 新增 api/kanbanWidgetsApi.ts（六個 widgets 端點）
- 新增 widgets/ 下四個內容元件 + 對應測試 + 註冊模組，App.tsx 匯入時註冊
- item.component 值暫採 Screen ID 字串（OQ-49 未定案，見 OQ-T-18-fe-widgets-01，不阻塞）
- build／test／lint 皆綠：tsc+vite build 成功、vitest 48/48 通過、oxlint 僅既有 1 則 warning

Review 請先看：
- decision-log.md「本輪判斷與理由」六點技術決定（尤其 item.component 命名、boardId 取得方式、
  平均值欄位衍生計算三點，偏離規格字面或補了規格沒寫的細節）
- OQ-T-18-fe-widgets-01 是否認同暫定的 item.component 命名方式
