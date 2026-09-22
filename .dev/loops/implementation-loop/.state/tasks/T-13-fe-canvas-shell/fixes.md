# T-13-fe-canvas-shell 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | 掛載點把 `item.component` 當成 item 識別碼傳給註冊的元件，下游任務拿不到 item 的 id。 - `kanban-frontend/src/canvas/itemComponentRegistry.tsx` 宣告 `ItemContentProps { itemId: string; width; height }`。 - `kanban-frontend/src/canvas/CanvasStage.tsx:428` 實際傳的是 `<Content itemId={item.component} width={box.width} height={box.height} />`，也就是元件識別碼（例如 "board"），不是 `item.id`。 本任務在 `tasks.md` 的產出範圍就是「提供給其他前端任務掛載自己的 item 內容」，這個 prop 是掛載點的對外契約。T-14～T-20 用 `registerItemComponent` 註冊後，同一個 `item.component` 若在同一畫布放了兩個 item，兩邊拿到的 `itemId` 會一樣，無法區分是哪一個 item（spec-canvas-layout.md「item」說明：「某個元件在畫布上的一次放置」，同一元件可以放多次）。 怎樣才算修好：`Content` 收到的 `itemId` 是 `item.id`；`PlaceholderContent` 的文字不再拿識別碼當人看的字串（或另外傳 `component` prop）；補一個測試斷言註冊的元件收到的 `itemId` 等於該 item 的 id。 |
