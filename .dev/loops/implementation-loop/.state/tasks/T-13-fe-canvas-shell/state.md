# T-13-fe-canvas-shell state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：`s-canvas` 畫布主畫面已依 spec-canvas-layout.md／ui-canvas-layout.md 完整實作
（放置／移除、移動、調整大小、能力設定、錨定方式切換、置頂置底、批次移動與移除、
每位使用者各自的檢視區平移縮放），為之後的前端任務提供 item.component 掛載點。

這輪做了什麼：
- `api/canvasApi.ts`（對應 CanvasController 全部端點）、`boardMembershipApi.ts`（取角色）；
  `http.ts` 補 patch/put；`boardApi.ts` 補 getBoard。
- `canvas/`：CanvasStage.tsx（選取／拖曳移動／八把手調整大小／浮動工具列／批次操作／
  檢視區平移縮放）、geometry.ts、itemComponentRegistry.tsx（供 T-14 起掛載 item 內容）、
  PlaceItemDialog.tsx、RemoveItemsDialog.tsx、CanvasStage.css。
- 重寫 BoardCanvasPage.tsx（原 T-12 佔位頁），依 board-membership.role 換算可否編輯。
- 新增 BoardCanvasPage.test.tsx（9 案例）；更新 BoardListPage.test.tsx 一則過時斷言。

Check：tsc -b／lint／test（6 檔 36 測試全過）／build 皆通過。

Review 建議先看 decision-log.md 判斷 1～7，再看 CanvasStage.tsx 拖曳／批次操作邏輯。
