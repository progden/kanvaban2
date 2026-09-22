# T-19-fe-workload 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：實作 s-workload-dashboard／s-cards-by-assignee

### 這輪做了什麼

實作 `s-workload-dashboard`（F07 canvas item，`item.component === 'workload-dashboard'`）與 `s-cards-by-assignee`（從前者點擊成員工作量進入的對話框）。後端（T-07-be-workload）與 canvas 容器（T-13-fe-canvas-shell）皆已合併，直接沿用既有 API 與 `itemComponentRegistry` 掛載機制（依 `ADR-T-17-fe-clock-control-01`：`item.component` 命名＝Screen ID 去掉 `s-` 前綴的 kebab-case，註冊呼叫放在 `BoardCanvasPage.tsx`）。

- `kanban-frontend/src/api/workloadApi.ts`：對應 `GET /api/boards/{boardId}/workload`（`uc-view-workload`）。
- `kanban-frontend/src/api/cardApi.ts`：`listCardsByAssignee`（`uc-list-cards-by-assignee`）、`dragAssignCardOwner`（沿用 `uc-assign-card-owner-by-drag` 既有端點，spec-workload.md 說明與 `uc-drag-assign-card-owner` 是同一操作）。
- `kanban-frontend/src/canvas/WorkloadDashboard.tsx`：成員清單＋工作量數字＋未指派卡片數；成員頭像 `draggable`（依 `board-membership.role` 非 VIEWER 才可拖曳，理由見下方決策）；點擊工作量數字開啟 `CardsByAssigneeDialog`。
- `kanban-frontend/src/canvas/CardsByAssigneeDialog.tsx`：`s-cards-by-assignee`，載入中／空清單／關閉回到工作量表。
- `kanban-frontend/src/canvas/cardAssigneeDrag.ts`：拖放協定共用模組（詳見 `ADR-T-19-fe-workload-01`）。

### 規格沒寫清楚的地方怎麼處理

1. **成員頭像是否所有角色都能拖曳**：`spec-workload.md`／`ui-workload.md` 對 `uc-drag-assign-card-owner` 只定義單一跨模組角色 `r-user`，沒有角色差異；但 `spec-user-membership.md` 角色定義表對 `r-board-viewer` 逐字寫『可檢視看板與相關統計圖表，不能新增／編輯／移動／刪除任何內容』。低風險技術決定：比照 `BoardClockControl.tsx` 既有的「呼叫 `listMembers` 判斷操作者角色」寫法，`board-membership.role === 'VIEWER'` 時頭像不可拖曳（`draggable={false}`），其餘角色（Owner／Member）可拖曳。理由：拖曳追加負責人是寫入動作，`r-board-viewer` 的定義明確涵蓋「不能編輯」，跟 `spec-canvas-layout.md` OQ-IMPL-08 定案的「Viewer 唯讀」方向一致，不是憑空腦補。
2. **跨 item 拖放目標尚未存在**（見 `OQ-T-19-fe-workload-01`、`ADR-T-19-fe-workload-01`）：`uc-drag-assign-card-owner` 的拖放目標（F01 `s-board` item 卡片縮圖）與 `s-cards-by-assignee` 點擊卡片要開啟的 `s-card-detail`，兩者都屬於 T-14-fe-board-item 的產出範圍；T-19 開工時 T-14 仍 `doing`、未合併，且 `tasks.md` 沒有把 T-14 列為 T-19 的依賴。已完成：拖曳來源端＋可重用的拖放協定模組（`cardAssigneeDrag.ts`），拖放目標端與卡片詳情導覽留給 T-14 合併時接上。開立 `OQ-T-19-fe-workload-01`（高風險，不阻塞），並寫 `ADR-T-19-fe-workload-01` 記錄協定本身，供 T-14 直接沿用。

### spec `entity`／`uc-`／Scenario 涵蓋範圍

- `uc-view-workload`：4 個 Scenario（單一負責人工作量／多負責人各算一次／未指派數量／Done 卡片不計入）全部由 `WorkloadDashboard` 顯示邏輯與後端既有 `WorkloadCalculator` 涵蓋（後端已在 T-07 完成並測試，本輪只驗證前端正確渲染 API 回傳值）。
- `uc-drag-assign-card-owner`：2 個 Scenario（追加負責人／已是負責人不重複新增）的業務邏輯已在後端 `Card.addAssignee`（T-03／T-07 範圍）測試涵蓋；本輪只涵蓋拖曳來源端與協定串接（`cardAssigneeDrag.test.ts`），實際掛上卡片縮圖節點的端到端行為待 T-14。
- `uc-list-cards-by-assignee`：驗收條件「清單同時包含多張卡片」「沒有負責卡片時清單為空」由 `CardsByAssigneeDialog.test.tsx` 涵蓋；「點擊卡片開啟 `s-card-detail`」因目的地不存在（`OQ-T-19-fe-workload-01`）暫做成不可點擊列表項，有意識跳過。

### 待確認事項

- `OQ-T-19-fe-workload-01`（不阻塞，owner＝T-14-fe-board-item）：拖放目標與卡片詳情導覽待 T-14 合併後接上，選項見 OQ 內文。

### Check（實際跑的指令與結果）

- `pnpm install`（`kanban-frontend/`，首次還原相依）：成功。
- `pnpm run test -- --run`（`kanban-frontend/`）：10 個測試檔、61 個測試全過（含本輪新增 3 個測試檔 15 個案例）。
- `pnpm run build`（`tsc -b && vite build`）：成功，無型別錯誤。
- `pnpm run lint`（oxlint）：僅既有的 1 則 `itemComponentRegistry.tsx` fast-refresh 警告（非本輪改動的檔案，本輪新增／修改檔案皆無警告）。

## 2026-09-22 Dev 第 2 輪：補 D-01／D-02 的 OQ 與歸屬

### 這輪做了什麼

處理 Review 第 1 輪退回的 D-01、D-02，兩項都是紀錄與 OQ 歸屬問題，程式碼維持第 1 輪現狀不動。

1. **D-01**：`WorkloadDashboard.tsx` 對 `r-board-viewer` 停用頭像拖曳，是 `ui-workload.md`「無角色差異」定稿內容與 `spec-user-membership.md` `r-board-viewer` 定義（不能編輯）之間的矛盾，不是低風險技術決定。已開立 `OQ-T-19-fe-workload-02`（高／不阻塞／owner＝人工／`spec-conflict`），情況欄用「兩處矛盾並列」逐字列出兩份定稿文件的原文，選項含「維持現行實作」「依 ui-workload.md 取消角色差異」「待 spec-user-membership.md 待釐清項全面盤點後再決定」。
2. **D-02**：原 `OQ-T-19-fe-workload-01` 把「拖放目標端」與「點擊卡片開啟詳情」兩件事包在同一則、都指向 `T-14-fe-board-item` 接手，但後者其實落在 T-19 自己的產出範圍。已拆成兩則新 OQ 取代：
   - `OQ-T-19-fe-workload-03`（owner＝T-14-fe-board-item）：拖放目標端（F01 `s-board` item 卡片縮圖 `onDrop`），並註明 T-14 目前 `doing`、看不到本輪的 `ADR-T-19-fe-workload-01`，需要人工在安排階段轉達或另開銜接任務。
   - `OQ-T-19-fe-workload-04`（owner＝人工）：`s-cards-by-assignee` 卡片列導向 `s-card-detail`，說明這是 T-19 自己範圍內、但依賴 T-14 合併後的路由才能補上，需人工安排後續修訂實例。
   `OQ-T-19-fe-workload-01` 保留不動（歷史紀錄），新兩則內文皆已註明取代其對應的哪一半。

### 待確認事項

- `OQ-T-19-fe-workload-02`：`r-board-viewer` 是否應停用頭像拖曳，待人工依兩份定稿文件裁示。
- `OQ-T-19-fe-workload-03`：T-14-fe-board-item 合併時是否沿用 `cardAssigneeDrag.ts` 接上卡片縮圖 `onDrop`。
- `OQ-T-19-fe-workload-04`：`s-cards-by-assignee` 卡片列導向 `s-card-detail` 由誰、何時補上。

### Check（實際跑的指令與結果）

本輪未改動 `kanban-frontend/**` 程式碼，沿用第 1 輪已通過的建置／測試結果（`pnpm run test -- --run` 10 檔 61 案全過、`pnpm run build` 成功、`pnpm run lint` 僅既有 fast-refresh 警告），未重跑。
