# T-18-fe-widgets 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：實作 F03 四個儀表板 Canvas item

### 本輪判斷與理由

- **`item.component` 命名**：規格未定義 F03 四個儀表板 item 的 `item.component` 值（見 OQ-T-18-fe-widgets-01，OQ-49 待整合 CR 定案）。採用 Screen ID 字串（`s-cycle-lead-time-dashboard`／`s-wip-dashboard`／`s-throughput-cfd-dashboard`／`s-duedate-reminder`）作為暫定值，透過 `canvas/itemComponentRegistry.tsx`（T-13 留下的掛載點）用 `registerItemComponent` 掛上，並在 `App.tsx` 匯入時呼叫 `registerKanbanWidgets()` 完成註冊（比照 T-13 測試檔案的用法，唯一差異是這裡是正式產品程式碼路徑，T-13 當時只有測試示範用法，production 尚無先例）。
- **boardId 取得方式**：`ItemContentProps`（T-13 定義）只有 `itemId`／`component`／`width`／`height`，沒有 `boardId`。因為 `s-canvas` 目前只掛在 `/boards/:boardId` 路由下，四個 widget 元件改用 `useParams<{ boardId: string }>()` 自行取得，不用改動 T-13 的 `itemComponentRegistry.tsx`／`CanvasStage.tsx`（避免動到已合併、不屬於本任務範圍的檔案）。
- **Cycle Time／Lead Time「平均值」欄位**：ui-kanban-widgets.md 資料表要求顯示「統計摘要（平均值、P50／P85／P95）」，但 `uc-view-cycle-lead-time` post 與後端 `PercentileView` 都只定義 P50/P85/P95，沒有平均值欄位。判斷為前端可從回應的每張卡片 Lead/Cycle Time 自行算平均（資料已經在回應裡，屬單純衍生計算，不是新業務規則），不需要開 OQ 或改後端。
- **CFD Stage 名稱**：`GET .../widgets/cfd` 只回傳 `stageId`，沒有 Stage 名稱；沿用既有 `getBoard(boardId).stages` 取得名稱與顯示順序（`stage.order`），沒有新增後端端點。
- **截止日期提醒預設門檻天數**：spec 只定案「門檻天數由使用者於查詢時設定」，未給初始值，但驗收條件要求「開啟畫面時觸發 `uc-view-duedate-reminder`」，代表一開啟就要帶一個門檻值查詢。採預設 7 天（低風險技術決定，非業務規則），使用者可用「套用」按鈕重新查詢；輸入 0 或非正整數時前端先擋（訊息與後端 fail-p1 一致：「門檻天數必須是 1 到 365 之間的正整數」），不呼叫 API，符合驗收條件「不觸發 uc-view-duedate-reminder」。
- **Throughput 單位時間**：ui 只定義列舉「日、週」，無預設值規定；預設「日」（低風險技術決定），切換下拉選單即重新查詢。
- **視覺樣式**：`.dev/ui-prototype/` 沒有這四個畫面的設計稿（README.md 明講「F03／F04／F05／F06 的 Canvas item 等」沿用既有視覺語彙），新增 `widgets/WidgetShell.css`，沿用 `index.css` 既有色票變數（`--color-error-*`、`--color-text-*` 等）與 `.field-input`／`.btn-sm` 既有元件樣式，自製最簡潔可用的表格版面，未發明新風格。

### 涵蓋範圍

- `uc-view-cycle-lead-time`、`uc-view-wip`、`uc-view-aging-wip`、`uc-view-throughput`、`uc-view-cfd`、`uc-view-duedate-reminder` 六個 usecase 皆已串接對應 REST 端點並在畫面上顯示。
- 四個 Screen（`s-cycle-lead-time-dashboard`／`s-wip-dashboard`／`s-throughput-cfd-dashboard`／`s-duedate-reminder`）的資料表、操作表、驗收條件皆已依 `ui-kanban-widgets.md` 實作，包含：
  - Cycle Time 顯示「無」＋排除計算的卡片數＋統計摘要不排除 Lead Time
  - WIP 圖表不含 Done 角色 Stage（@CR-012）
  - Aging WIP 僅列已進入 Start、尚未進入 Done 的卡片
  - Throughput 依日／週切換、CFD 依 Stage 分組顯示每日累積數
  - 截止日期提醒門檻天數驗證（前端擋 0／非正整數，訊息與後端一致）
- 未涵蓋：這四個 item 如何從畫布「新增」的使用者操作流程（OQ-49 待整合 CR 定案前，`PlaceItemDialog` 的「元件識別碼」欄位是既有的純文字輸入框，使用者可手動輸入本任務定案的 component 值，沒有另外做下拉選單或快速鍵——待 OQ-49 整合 CR 定案後再一併調整）。

### 待確認事項

- OQ-T-18-fe-widgets-01（不阻塞）：F03 四個儀表板 item 的 `item.component` 命名，待 OQ-49 對應整合 CR 定案。

### Check（實際跑的指令與結果）

- `pnpm install`：安裝相依套件（worktree 未先 install）
- `npm run build`（`tsc -b && vite build`）：通過，`dist/` 產出成功
- `npm run test`（`vitest run`）：10 個測試檔、48 個測試全數通過（含本任務新增 4 個 widget 測試檔）
- `npm run lint`（`oxlint`）：僅既有 1 則 warning（`itemComponentRegistry.tsx` 的 fast-refresh 提示，非本任務新增）

## 2026-09-22 Dev 第 2 輪：修正 D-01：OQ 逐字引文與先例回應

### D-01 修正：訂正 OQ 引文並回應先例

- 讀完 `.dev/F07-canvas-layout/spec-canvas-layout.md` 第 31 行完整原文（含被刪的括號『不用 UI 層的 Screen ID，spec 不引用 ui』與最後一句）後，另開 `OQ-T-18-fe-widgets-02` 取代 `OQ-T-18-fe-widgets-01`，逐字引用補齊，並在 `ui-canvas-layout.md` 引文修正為『待整合 CR 定案』（原引文誤刪空白）。
- 正面回應先例後，維持本任務原決定（Screen ID 命名）：先例（看板本體用實體 ID `board`、不用 Screen ID）的適用前提是「spec 本身已有可用識別碼」，但 F03 四個儀表板在 spec-kanban-widgets.md 沒有 entity，只有六個 `uc-view-*`，且與四個畫面不是一對一（`uc-view-wip`／`uc-view-aging-wip` 同屬 `s-wip-dashboard`，`uc-view-throughput`／`uc-view-cfd` 同屬 `s-throughput-cfd-dashboard`）；spec 沒有定義「一組 uc 合成一個 item」的識別碼，這個分組是 ui-kanban-widgets.md 才有的概念，先例的前提不成立。改用 `uc-` 代表值（新 OQ 選項 B）需要另外自創「取哪一個 uc 代表整組」的規則，spec 沒有依據，並不會比 Screen ID 更貼近 spec，故不採用。
- 維持原決定，`registerKanbanWidgets.ts` 與四個 widget 測試的 component 值不變，無須改動、測試維持全綠。
