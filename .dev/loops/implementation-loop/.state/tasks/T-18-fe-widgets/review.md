# T-18-fe-widgets 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：退回

判定：退回（`doing`）。六項核對中五項通過，第 5 項（Dev 的 OQ 逐一核對）不通過，已開 D-01。

### 1. 自己跑建置與測試（未採信交接摘要的 Check 欄）

在 `kanban-frontend/` 實際執行：

- `pnpm install --frozen-lockfile`：`Already up to date`
- `npm run build`（`tsc -b && vite build`）：`✓ 57 modules transformed`、`✓ built in 986ms`，`dist/assets/index-D6OUwr4Y.js 299.94 kB`
- `npm run test`（`vitest run`）：`Test Files 10 passed (10)`、`Tests 48 passed (48)`、`Duration 55.40s`
- `npm run lint`（`oxlint`）：只有 1 則既有 warning `src/canvas/itemComponentRegistry.tsx:21:10: warning react(only-export-components)`，是 T-13 的檔案，非本任務新增

全綠，與 Dev 回報一致。

### 2. spec／ui 對應

六個 uc 皆有對應 REST 呼叫與畫面呈現，四個測試檔都是走 `App` + `MemoryRouter` + `canvas-item-item-1` 的整合測試，真的驗證「開啟畫面時觸發 uc-xxx」：

- `uc-view-cycle-lead-time`：斷言 `/widgets/cycle-lead-time` 被呼叫，畫面出現卡片 A／B、Cycle Time 顯示「無」、排除計算的卡片數。對照 ui 資料表「統計摘要（平均值、P50／P85／P95）」——後端 `PercentileView` 只有 p50/p85/p95，平均值由 `CycleLeadTimeDashboardWidget.tsx` 的 `average()` 從回應內每張卡片自行算，Cycle Time 平均已 `filter(v => v !== null)` 排除「無」的卡片，符合 post p2『Cycle Time 統計摘要的平均值與百分位計算排除該 `card`…Lead Time 統計摘要不排除任何 `card`』。
- `uc-view-wip`／`uc-view-aging-wip`：測試斷言兩支端點都被呼叫，且 `queryByText('完成')` 不在文件內（@CR-012「不含 Done 角色 Stage」，實際由後端投影負責，前端忠實呈現）。
- `uc-view-throughput`／`uc-view-cfd`：預設 `?unit=day`，切換後 `?unit=week` 再查一次；`ThroughputUnit` 的 `'day'｜'week'` 對得上後端 `ThroughputGranularity.DAY/WEEK`（`valueOf(unit.toUpperCase())`）。CFD Stage 名稱取自既有 `getBoard().stages` 並依 `stage.order` 排序，未新增後端端點。
- `uc-view-duedate-reminder`：見下一點。

抽查 `@fail-p1`（`uc-view-duedate-reminder` pre p1『指定的門檻天數…為 1 到 365 之間的正整數』、fail p1『拒絕，資料不變』）：`DueDateReminderWidget.test.tsx` 第二個案例輸入 0 後按「套用」，斷言 (a) `role="alert"` 文字為「門檻天數必須是 1 到 365 之間的正整數」，(b) 輸入框 value 仍為 `'0'`（輸入保留），(c) `/widgets/duedate-reminder` 的呼叫次數未增加（不觸發 uc）。三條與 ui 驗收條件『門檻天數輸入為 0 或非正整數時，輸入保留、顯示訊息，且不觸發 `uc-view-duedate-reminder`』逐條對得上。訊息文字與後端 `KanbanWidgetsQueryService.java:108` 的 `"門檻天數必須是 1 到 365 之間的正整數"` 完全一致。本 uc 是唯讀查詢，「資料不變」自然成立。

另外核對一處疑似落差後確認無誤：`ThroughputCfdDashboardWidget.tsx` 用 `stageId in point.countByStage` 過濾 CFD 列，看似會讓卡片數為 0 的 Stage 整列消失；但 `CfdCalculator.java:49-56` 對每一天都 `for (UUID stageId : stageIdsInOrder) countByStage.put(stageId, count)`，零值也會回傳，故不會漏列，符合 ui 驗收條件『CFD 圖顯示每一天、每個 Stage 的累積卡片數量』。

### 3. kanban-core 純度

本任務純前端，`git diff loop/implementation...HEAD --stat` 未動到 `kanban-core/**`、`kanban-spring/**`，無需檢查 import；跨 aggregate 讀取投影仍在既有 `io.progden.kanban.query.*`，本輪未新增。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：17 個檔、+1268/-1。程式碼只有 `kanban-frontend/src/` 底下的 `api/kanbanWidgetsApi.ts`、`widgets/`（四個元件＋四個測試＋註冊模組＋`WidgetShell.css`）與 `App.tsx` 三行（匯入並呼叫 `registerKanbanWidgets()`）。未動 `.dev/conventions/**`、`scripts/**`、spec／ui 本體、其他 aggregate。`.state/` 只有 `tasks/T-18-fe-widgets/**` 五個檔，沒有 `.state/tasks.md`、`archive/**` 或別的任務目錄。邊界乾淨。

### 5. 交接摘要的待確認事項／OQ（不通過）

交接摘要「待確認事項」只有一條，確實已用 `loopctl oq add` 開成 OQ-T-18-fe-widgets-01，`loopctl show` 列得出來。等級「高」／阻塞「否」判定正確：用「把這個任務做完，是否必須違反某一段已定稿的原文？」檢驗，spec 欄位表本來就寫『其餘元件的值待各自所屬模組實作對應 Item 時決定』，本任務正是那個「所屬模組」，決定這個值不違反任何定稿條文，所以不阻塞。接手填「無」也正確——`.state/tasks.md` 裡沒有任何任務的產出範圍涵蓋 OQ-49 的整合 CR。

不通過的是引文本身。OQ 引 `spec-canvas-layout.md` 第 31 行時，把括號『（見「看板畫布初始化」Feature；不用 UI 層的 Screen ID，spec 不引用 ui，見 `docs-convention.md` 第 3 節）』與最後一句整段刪掉，卻沒有任何刪節標記。刪掉的『不用 UI 層的 Screen ID』正是唯一已定案的 component 值（看板本體）為什麼不用 Screen ID 的理由，而本任務把四個值定成 ui 層 Screen ID。人工讀這則 OQ 會以為 spec 對命名毫無指引，實際上有明確的反向先例；這正是全域 CLAUDE.md『不要自己縮寫、改寫、摘要成一句話——縮寫過的版本我看不出來你到底讀到了什麼』要擋的情況。另外 ui-canvas-layout.md 的引文把原文『待整合 CR 定案』寫成『待整合CR定案』。依 review-prompt「你自己不修程式碼、也不替 Dev 補交付物（包含替 Dev 把 OQ 內容補完——那是 D-xx）」，開 D-01 交回 Dev。

註：D-01 要求的是訂正 OQ 並在決策紀錄正面回應那句先例；維持 Screen ID 命名也可以接受，改名則四個測試的 component 值要同步且維持全綠。

### 6. 前端設計稿核對

`.dev/ui-prototype/` 沒有這四個畫面的 `.dc.html`（README.md 第 36 行『沒有設計稿的畫面（`s-activity-log`、`s-cards-by-assignee`、F03／F04／F05／F06 的 Canvas item 等）：沿用上面這些檔案的視覺語彙…不另外發明風格』）。核對 `WidgetShell.css`：全檔無任何 hex／rgb 硬寫色值，用到的 7 個 CSS 變數（`--color-bg-muted`／`--color-border`／`--color-error-bg`／`--color-error-border`／`--color-error-text`／`--color-text-label`／`--color-text-subtle`）逐一比對 `index.css` 皆已定義；輸入框用既有 `.field-input`、按鈕用 `.btn-sm`。沒有發明新視覺風格。

四個元件的 JSX 輸出裡沒有 Attribute ID、`uc-xxx`、「⚠️ 規格未定義」這類灰色註記（grep 只命中檔頭註解，不會渲染）。ui 操作表「需確認？」四個畫面全是「否」，實作也沒有任何二次確認對話框；「失敗時」欄只有 `s-duedate-reminder` 有值，呈現方式（輸入保留＋顯示訊息）已落實於 `role="alert"` 區塊。
