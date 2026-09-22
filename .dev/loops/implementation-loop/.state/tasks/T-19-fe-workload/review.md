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

## 2026-09-22 Review 第 2 輪：附保留核准

判定：附保留核准（`done`）。第 1 輪退回的 D-01、D-02 都已依指示補齊，建置／測試／邊界／視覺重跑一次全部通過；四則 OQ 逐字比對源頭文件無誤，等級／阻塞判定成立，皆不阻塞。

### 1. 自行跑建置與完整測試（本輪實跑，不採信交接摘要）

Dev 第 2 輪交接摘要寫『本輪未改動 `kanban-frontend/**` 程式碼…未重跑』，所以我自己重跑一次，在 `kanban-frontend/`：

- `pnpm run test -- --run`：`Test Files  10 passed (10)`、`Tests  61 passed (61)`、`Duration  38.97s`。
- `pnpm run build`（`tsc -b && vite build`）：`✓ 56 modules transformed`、`✓ built in 793ms`，無型別錯誤。
- `pnpm run lint`（oxlint）：只有 `src/canvas/itemComponentRegistry.tsx:22:10: warning react(only-export-components)`，是 T-13 既有檔案、非本任務改動。
- `git status --porcelain`：無輸出，工作區乾淨。

### 2. spec 對應

- `uc-view-workload`（`spec-workload.md` usecase 區塊 `roles: [r-user]`、`crud: {board: R, board-membership: R, card: R}`、4 條 post）：4 個 Scenario 的計算落在後端 `WorkloadCalculator`（T-07 已合併）；前端 `WorkloadDashboard.tsx` 直接渲染 API 回傳的 `cardCount`／`unassignedCount`，未在前端重算，與 post p1～p4 一致。`WorkloadDashboard.test.tsx` 5 案涵蓋顯示、API 觸發、開啟 `s-cards-by-assignee`、Viewer／非 Viewer 拖曳差異。
- `uc-list-cards-by-assignee`（`spec-user-membership.md:685`，`post` 逐字『清單列出所有以指定成員為負責人的 `card`』）：`CardsByAssigneeDialog.test.tsx` 三案對上 `ui-user-membership.md`「s-cards-by-assignee」驗收條件前兩條與「中途放棄會怎樣」。第三條『點擊清單中的卡片開啟 F01 `s-card-detail`』仍未實作，已由 `OQ-T-19-fe-workload-04` 承接（見保留事項）。
- `uc-drag-assign-card-owner`：`cardAssigneeDrag.test.ts` 4 案涵蓋來源端與協定（含「沒有攜帶使用者 ID 時不呼叫 API」）。前端路徑與後端實際端點逐一核對相符：`dragAssignCardOwner` → `CardController.java:122` `@PostMapping("/api/cards/{cardId}/assignees/drag")`；`listCardsByAssignee` → `CardController.java:143` `@GetMapping("/api/boards/{boardId}/cards/by-assignee/{assigneeUserId}")`；`getWorkload` → `WorkloadController.java:29` `@RequestMapping("/api/boards/{boardId}/workload")`。拖放目標端未掛上，由 `OQ-T-19-fe-workload-03` 承接。
- fail 情境抽查：本任務三個 uc 的 `fail` 欄皆為空（`spec-workload.md` 兩則與 `spec-user-membership.md:692` 皆逐字 `fail: {}`），`grep -c "@fail" .dev/F05-workload/spec-workload.md` 回 `0`，`ui-workload.md:40` 亦逐字『- 錯誤：不適用（`uc-view-workload`／`uc-drag-assign-card-owner` 皆無 fail 定義）』。本任務範圍內沒有 `@fail-pN` Scenario 可抽查；元件保留的 `ApiError` 呈現屬傳輸層後備，不與規格衝突。

### 3. `kanban-core` 純度

本輪與前一輪皆未動 `kanban-core`／`kanban-spring`（`git diff loop/implementation...HEAD --stat` 只有 `kanban-frontend/**` 與 `.state/**`），此項不適用。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：18 檔、+800/-1。程式碼全部落在 `kanban-frontend/src/{api,canvas,pages}`；`.state/` 只有 `tasks/T-19-fe-workload/**` 與新增的 `adr/ADR-T-19-fe-workload-01-card-assignee-drag-contract.md`，沒有碰 `tasks.md`／`archive/**`／別的任務目錄。兩個共用檔仍是純追加：`BoardCanvasPage.tsx` 只多一行 `registerItemComponent('workload-dashboard', WorkloadDashboard)`＋import，`CanvasStage.css` 只追加 `.workload-dashboard*`／`.cards-by-assignee__list` 區塊，未改既有規則。邊界判定：通過。

### 5. D-01／D-02 與 OQ 核對

- **D-01 已修好**：`OQ-T-19-fe-workload-02`（高／不阻塞／人工／`spec-conflict`）已開立，情況欄用「兩處矛盾並列」。逐字比對源頭：`ui-workload.md:21`、`:41`（『- 無權限：不適用（F05 spec 僅使用跨模組 `r-user` 一種角色，無角色差異）』）與 `spec-user-membership.md:56`、`:866`（「待釐清」那段）四處引文全部逐字相符，無縮寫。decision-log 第 2 輪「待確認事項」也補上了這則編號。等級／阻塞判定成立：`spec-user-membership.md` 是 spec 層、`ui-workload.md` 是 ui 層，引用方向 `spec ← ui`，照上游 spec 做（Viewer 不可拖曳）不需要動任何定稿文字，屬「高／不阻塞」而非「覆蓋」。程式碼維持現狀正確。
- **D-02 已修好**：拆成 `OQ-T-19-fe-workload-03`（拖放目標端）與 `OQ-T-19-fe-workload-04`（`s-cards-by-assignee` 導向 `s-card-detail`）。引文逐字核對相符：`ui-workload.md:34` 操作列、`ui-user-membership.md`「s-cards-by-assignee」操作表『開啟卡片詳情 | — | 開啟 F01 `s-card-detail`（跨模組） | 不適用 | 否』、`tasks.md:42`（T-14 產出範圍含 `s-card-detail`）、`tasks.md:47`（T-19 產出範圍）四處皆與源頭一致。接手判定成立：OQ-03 的掛載點確實在 T-14 的產出範圍內，且內文依 D-02 的要求註明 T-14 仍 `doing`、看不到本 ADR、需人工轉達；OQ-04 屬 T-19 自己範圍但目的地不存在，`--owner 人工` 正確（T-14 不會回頭改 T-19 的檔案）。兩則皆註明取代 `OQ-T-19-fe-workload-01` 的哪一半。
- **本輪新開 `OQ-T-19-fe-workload-05`**（高／不阻塞／人工／`tooling-missing`）：`OQ-T-19-fe-workload-01` 的兩半都已被取代，但它的「狀態：待處理」「接手：T-14-fe-board-item」仍留著，而 `loopctl oq` 只有 `add` 子指令（實跑 `loopctl oq --help` 確認），loop 角色無法標記取代關係，`open-questions.md` 檔頭也寫明解除說明由人工補。這則殘留沒有任何已登記任務能處理，故另開 OQ 指給人工，不是「之後再處理」帶過。

### 6. 前端視覺依據

`ls .dev/ui-prototype/` 確認沒有 `s-workload-dashboard`／`s-cards-by-assignee` 對應的 `*.dc.html`；該目錄 `README.md` 逐字『沒有設計稿的畫面（`s-activity-log`、`s-cards-by-assignee`、F03／F04／F05／F06 的 Canvas item 等）：沿用上面這些檔案的視覺語彙（同樣的字體、配色、按鈕／輸入框／對話框樣式）做最簡潔可用的版面，不另外發明風格。』本任務兩個畫面都在這份名單內。實測 CSS diff：只用既有變數 `var(--color-primary)`／`--color-bg-muted`／`--color-border`／`--color-text-muted`，字體全部 `font-family: inherit`，未出現自訂色碼（僅頭像文字用 `#fff`，與既有 primary 按鈕一致）。對話框沿用既有 `dialog-backdrop`／`dialog-panel`／`btn`／`form-error`／`canvas-dialog__actions`。產品畫面中沒有 Attribute ID、`uc-xxx` 或「⚠️ 規格未定義」這類灰色註記（這些只出現在原始碼註解裡）。「需確認？」欄：`ui-workload.md:34` 逐字『否（可透過 F02 `uc-set-card-assignees` 調整負責人清單，非不可逆操作）』、`ui-user-membership.md`「開啟卡片詳情」列亦為『否』，實作皆未加確認對話框，一致。「失敗時」欄兩處皆為『不適用』，實作沒有為不存在的 fail 編造訊息，一致。

### 保留事項與接手者

核准是「附保留」，四則未決 OQ 皆為「高／不阻塞」，沒有「覆蓋」或「環境」等級，故不需標 `blocked`：

1. `OQ-T-19-fe-workload-02`（Viewer 是否停用頭像拖曳）——**接手：人工**。現況照上游 `spec-user-membership.md` 實作，裁示為選項 B 時要改 `WorkloadDashboard.tsx` 與其兩個 Viewer 相關測試。
2. `OQ-T-19-fe-workload-03`（拖放目標端掛上卡片縮圖 `onDrop`）——**接手：T-14-fe-board-item**，但需人工在安排階段把 `ADR-T-19-fe-workload-01` 轉達給 T-14（T-14 已在 `doing`，看不到本分支的 ADR）。
3. `OQ-T-19-fe-workload-04`（`s-cards-by-assignee` 卡片列導向 `s-card-detail`）——**接手：人工**，需在 T-14 合併後安排一個 T-19 的修訂實例補上；這是 `ui-user-membership.md` 驗收條件第三條唯一未落實的項目。
4. `OQ-T-19-fe-workload-05`（`OQ-T-19-fe-workload-01` 殘留的錯誤接手者與待處理狀態）——**接手：人工**，`loopctl` 無對應子指令，只能由人工補解除說明。
