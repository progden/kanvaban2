# T-19-fe-workload 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：退回

判定：退回（`doing`）。建置／測試／邊界／視覺皆過，但有兩項紀錄面的問題要 Dev 自己補（Review 不代寫）。

### 1. 自行跑建置與完整測試

在 `kanban-frontend/` 實跑：

- `pnpm run test -- --run`：`Test Files 10 passed (10)`、`Tests 61 passed (61)`、`Duration 78.90s`。
- `pnpm run build`（`tsc -b && vite build`）：`✓ 56 modules transformed`、`✓ built in 887ms`，無型別錯誤。
- `pnpm run lint`（oxlint）：只有 `src/canvas/itemComponentRegistry.tsx:22:10: warning react(only-export-components)`，是 T-13 既有檔案、非本輪改動。

與 Dev 交接摘要的 Check 欄一致。

### 2. spec 對應

- `uc-view-workload`：4 個 Scenario 的計算邏輯在後端 `WorkloadCalculator`（T-07 已合併）；前端 `WorkloadDashboard.test.tsx` 驗證「顯示各成員工作量與未指派卡片數量」與「開啟時呼叫 `/api/boards/{boardId}/workload`」，對上 `ui-workload.md` 驗收條件前三條。`WorkloadDashboard.tsx:82`／`:89` 直接渲染 API 回傳的 `cardCount`／`unassignedCount`，未在前端重算，符合 post p1～p4。
- `uc-list-cards-by-assignee`：`CardsByAssigneeDialog.test.tsx` 三案覆蓋「多張卡片同時列出」「空清單」「關閉回到 `s-workload-dashboard`」，對上 `ui-user-membership.md`「s-cards-by-assignee」驗收條件前兩條與「中途放棄會怎樣」。第三條『點擊清單中的卡片開啟 F01 `s-card-detail`』未實作（見 D-02）。
- `uc-drag-assign-card-owner`：`cardAssigneeDrag.test.ts` 覆蓋來源端與協定；`handleCardAssigneeDrop` 打的 `POST /api/cards/{cardId}/assignees/drag` 與 `CardController.java:122` 的 `@PostMapping` 及 `DragAssignRequest.userId()` 對得上，`listCardsByAssignee`／`getWorkload` 也分別對上 `CardController.java:143` 與 `WorkloadController` 的 `@RequestMapping("/api/boards/{boardId}/workload")`＋`WorkloadResponse`／`MemberWorkloadEntry` 欄位。拖放目標端未掛上（見 D-02）。
- fail 情境抽查：`uc-view-workload`／`uc-drag-assign-card-owner`／`uc-list-cards-by-assignee` 的 `fail` 欄皆為空，`ui-workload.md`「狀態」段亦逐字寫『- 錯誤：不適用（`uc-view-workload`／`uc-drag-assign-card-owner` 皆無 fail 定義）』，本任務範圍內沒有 `@fail-pN` Scenario 可抽查。元件仍保留 `ApiError` 的錯誤呈現作為傳輸層後備，不與規格衝突。

### 3. `kanban-core` 純度

本輪未動 `kanban-core`／`kanban-spring`（`git diff loop/implementation...HEAD --stat` 只有 `kanban-frontend/**` 與 `.state/**`），此項不適用。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：16 檔、+653/-1。程式碼全部落在 `kanban-frontend/src/{api,canvas,pages}`；`.state/` 只動到 `tasks/T-19-fe-workload/**` 與新增 `adr/ADR-T-19-fe-workload-01-...md`，沒有碰 `tasks.md`／`archive/**`／別的任務目錄。兩個共用檔的改動都是純追加、且比照 T-17 既有慣例：`BoardCanvasPage.tsx` 只多一行 `registerItemComponent('workload-dashboard', ...)`＋ import，`CanvasStage.css` 只追加 `.workload-dashboard*`／`.cards-by-assignee__list` 區塊，未改既有規則。邊界判定：通過。

### 5. 交接摘要的待確認事項／OQ 核對

- `OQ-T-19-fe-workload-01` 確實已用 `loopctl oq add` 開立，`loopctl show` 列得出來。
- 等級「高」／阻塞「否」判定成立：把 T-19 做完不需要違反任何定稿原文，只是 `ui-workload.md`／`ui-user-membership.md` 指向的掛載目標（F01 `s-board` 卡片縮圖、`s-card-detail`）此刻不存在，不構成「覆蓋」或「環境」。
- 但「接手：T-14-fe-board-item」對其中一半是錯的，見 **D-02**。
- decision-log「規格沒寫清楚的地方怎麼處理」第 1 點（Viewer 不可拖曳）被當成低風險自行決定、沒開 OQ，見 **D-01**。

### 6. 前端視覺依據

`.dev/ui-prototype/README.md` 逐字：『沒有設計稿的畫面（`s-activity-log`、`s-cards-by-assignee`、F03／F04／F05／F06 的 Canvas item 等）：沿用上面這些檔案的視覺語彙（同樣的字體、配色、按鈕／輸入框／對話框樣式）做最簡潔可用的版面，不另外發明風格。』本任務兩個畫面都在這個名單內，無對應 `*.dc.html`。實作沿用既有 `dialog-backdrop`／`dialog-panel`／`btn`／`form-error`／`canvas-dialog__actions` 類別與 `var(--color-primary)`／`--color-bg-muted`／`--color-border`／`--color-text-muted` 變數，未自訂色碼或字體，判定符合。產品畫面中沒有出現 Attribute ID、`uc-xxx` 或「⚠️ 規格未定義」這類灰色註記。「需確認？」欄兩個操作都是『否』，實作也沒有加確認對話框，一致。

### 退回事項與接手者

- **D-01**：Viewer 不可拖曳的角色差異要開成 OQ（高／不阻塞）。接手：本任務 Dev 第 2 輪。
- **D-02**：`OQ-T-19-fe-workload-01` 的接手者要拆對。接手：本任務 Dev 第 2 輪。

兩項都是紀錄與 OQ 歸屬問題，不需要改動已通過測試的程式碼。
