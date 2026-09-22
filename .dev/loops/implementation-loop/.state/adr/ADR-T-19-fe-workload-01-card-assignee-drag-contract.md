## ADR-T-19-fe-workload-01：拖曳成員頭像追加卡片負責人的跨 item 拖放協定

- 狀態：Proposed
- 日期：2026-09-22
- 提出者：Dev（T-19-fe-workload）

### 背景（Context）

`spec-workload.md`「拖曳成員頭像到卡片上，追加該成員為負責人」（`uc-drag-assign-card-owner`）依 `ui-authoring-loop` OQ-50 定案：拖放目標是同一畫布（F07 `s-canvas`）上 F01 `s-board` item 的卡片縮圖，`s-workload-dashboard` 本身不顯示個別卡片清單。依 ADR-T-17-fe-clock-control-01，每個模組的畫面內容是各自獨立掛進 `itemComponentRegistry` 的 React 元件樹；拖曳來源（`s-workload-dashboard`，本任務）與拖放目標（`s-board` 的卡片縮圖，T-14-fe-board-item）因此是兩棵互不知情的元件樹，不能透過共用 React state 或 context 溝通，只能靠瀏覽器原生 HTML5 drag-and-drop 事件（`dataTransfer`）傳遞「被拖曳的是哪個使用者」。T-19 開工時 T-14 仍是 `doing`（未合併），卡片縮圖節點還不存在，見 `OQ-T-19-fe-workload-01`。

### 決策（Decision）

1. 拖放協定抽成共用模組 `kanban-frontend/src/canvas/cardAssigneeDrag.ts`，不放進 `WorkloadDashboard.tsx` 內部，讓拖放目標端（未來的 T-14）可以直接 import：
   - `CARD_ASSIGNEE_DRAG_MIME = 'application/x-kanban-user-id'`：`dataTransfer` 自訂格式，攜帶被拖曳成員的 `userId`；同時寫入 `text/plain` 作為降級後備。
   - `startUserAvatarDrag(e, userId)`：拖曳來源（頭像）的 `onDragStart` 呼叫。
   - `acceptsCardAssigneeDrop(e)`：拖放目標可在 `onDragOver` 用它判斷這次拖曳是不是本協定（例如決定要不要顯示可放置樣式），依 `e.dataTransfer.types` 是否包含上述格式判斷（部分瀏覽器在 `dragover` 階段讀不到實際資料，只能讀 `types`）。
   - `handleCardAssigneeDrop(e, cardId)`：拖放目標的 `onDrop` 呼叫，內部呼叫 `POST /api/cards/{cardId}/assignees/drag`（`kanban-frontend/src/api/cardApi.ts` 的 `dragAssignCardOwner`，即 `uc-assign-card-owner-by-drag`／`uc-drag-assign-card-owner` 共用的既有端點）。
2. 函式簽章用最小需要的 `DragDataCarrier` 介面（只描述 `dataTransfer.getData`／`setData`／`types`／`effectAllowed`），不直接綁定 `React.DragEvent`，方便測試（可傳入假物件）與未來如果改用其他跨元件溝通方式時降低影響面。
3. `s-workload-dashboard` 只實作拖曳來源端（頭像 `draggable`，依 `board-membership.role` 非 `VIEWER` 才可拖曳）；拖放目標（卡片縮圖的 `onDragOver`／`onDrop`）留給 T-14 實作時直接呼叫上述函式，不在 T-19 這裡放置任何假的卡片節點。

### 考慮過的替代方案（Alternatives）

- 用全域 store（例如一個掛在 `window` 或某個 context 的變數）暫存「目前被拖曳的 userId」：省去處理 `dataTransfer` 的瀏覽器相容性細節，但等於自製一套拖放狀態機、且會被瀏覽器原生拖放事件之外的操作（例如程式觸發的假拖放）繞過，維護成本更高，未採用。
- 等 T-14 合併後才實作拖曳來源端：會讓 T-19 的驗收範圍卡在別的並行分支進度上，且 `tasks.md` 並未把 T-14 列為 T-19 的依賴（見 `OQ-T-19-fe-workload-01`），未採用。

### 後果（Consequences）

- T-14-fe-board-item 實作卡片縮圖時，只需在縮圖節點的 `onDragOver`（呼叫 `e.preventDefault()` 並可選用 `acceptsCardAssigneeDrop` 決定要不要顯示可放置樣式）與 `onDrop`（呼叫 `handleCardAssigneeDrop(e, card.id)` 後用回傳的 `CardSummary` 更新畫面）接上這個模組，不需要重新設計協定。
- `kanban-frontend/src/api/cardApi.ts` 目前只有 T-19 需要的 `listCardsByAssignee`／`dragAssignCardOwner` 兩個函式與最小的 `CardSummary` 型別；T-14 若需要完整的 `CardResponse` 形狀，預期會在同一檔案擴充，屆時若與本檔案定義的型別重複，以 T-14 為準整併。
- 若之後人工對 OQ-50／拖放機制另有決議（例如改成不用原生拖放、而是點擊選單），這個協定模組與其呼叫端（`WorkloadDashboard.tsx` 的 `onDragStart`）都要一併調整，`item.component` 字串本身不受影響。

對應 OQ：`OQ-T-19-fe-workload-01`。
