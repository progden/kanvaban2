# T-12-fe-board-list 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | `s-board-list` 驗收條件「選擇列表中的 Board 後開啟該 Board（F07 `s-canvas`，跨模組）」沒有對應測試。 現況：`BoardListPage.tsx` 的 `board-card__open` 按鈕呼叫 `navigate(/boards/${board.id})`，路由在 `App.tsx:35` 掛到 `BoardCanvasPage`，但 `BoardListPage.test.tsx` 三個 `describe` 裡沒有任何一則點擊 Board 名稱、驗證導覽結果的測試（`grep -n "navigate\｜boards/" kanban-frontend/src/pages/BoardListPage.test.tsx` 無結果）。ui 操作表「選擇 Board 進入」與驗收條件都列在 `s-board-list`，屬於本任務範圍。 怎樣才算修好：在 `BoardListPage.test.tsx` 補一則測試，點擊列表中某個 Board 後斷言畫面切換到 `/boards/<id>` 對應的內容（例如 `BoardCanvasPage` 目前的佔位文字），並維持既有 25 則測試全數通過。 |
