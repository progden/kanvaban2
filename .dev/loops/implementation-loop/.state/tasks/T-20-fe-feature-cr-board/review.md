# T-20-fe-feature-cr-board 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：附保留核准

### 1. 自行重跑建置與測試（未採信 Dev 交接摘要）

於 `kanban-frontend/` 實際執行：

- `pnpm install --frozen-lockfile`：`Already up to date`
- `pnpm exec vitest run`：`Test Files 9 passed (9)`、`Tests 59 passed (59)`
- `pnpm run build`（`tsc -b && vite build`）：`✓ 57 modules transformed`、`✓ built in 972ms`
- `pnpm run lint`（oxlint）：僅 `src/canvas/itemComponentRegistry.tsx:22:10` 的 fast-refresh 警告，該檔為 T-13 產出、本任務未改動，非本輪新增

三項全綠。

### 2. spec／ui 對應核對

`spec-feature-cr-board.md` 唯一 uc `uc-view-feature-cr-board`（`crud: {board: R, card: R}`、`pre: {}`、`fail: {}`、`emits: []`）的 post p1～p5 與五個 Scenario，逐一核對 `FeatureCrBoardItem.tsx` 與 `FeatureCrBoardItem.test.tsx`：

- post p1／Scenario「檢視 Feature 的開發狀態」→ `feature-cr-board-features` 清單顯示 `featureId` 與 `status`；測試「顯示 Feature 的開發狀態」斷言 F01／「已完成」。
- post p2／Scenario「檢視 CR 影響哪個 Feature 以及其狀態」（`@CR-013`）→ CR 巢狀於該 Feature 的 `feature-cr-board__cr-list`；測試以 `within(list)` 斷言 CR-004／「開發中」確實在 Feature 清單內，互斥語意（存在對應 Feature 卡才掛在底下）由後端 `FeatureCrBoardCalculator` 決定，前端原樣呈現。
- post p3／Scenario「CR 指到不存在的 Feature 時列為 orphan」→ `feature-cr-board-orphans` 區塊；測試斷言 CR-099。
- post p4／Scenario「標籤格式不合時列為警告」→ `feature-cr-board-warnings` 區塊獨立於 Feature 清單渲染，不影響其他卡片呈現；測試斷言警告文字。
- post p5／Scenario「標籤不分大小寫」→ 正規化屬後端職責（`FeatureCrBoardCalculator`，T-08 已合併並有 Cucumber 驗收），前端原樣顯示回傳的 `featureId`／`crId`，無自行處理大小寫的邏輯，符合分層。

`uc-view-feature-cr-board` 的 `fail` 為空（無 `@fail-pN` Scenario 可抽查），改為核對 ui 檔「失敗時」欄逐字『不適用（`uc-view-feature-cr-board` 無 fail 定義）』：實作未憑空發明業務性失敗訊息，只在網路／HTTP 層失敗時以 `role="alert"` ＋ `form-error` 呈現後端訊息，比照已合併的 `ActivityLogItem` 慣例，且此路徑不改任何資料（純 GET），符合「資料不變」。測試「載入失敗時顯示錯誤訊息」以 500 回應驗證。

`ui-feature-cr-board.md` 逐段核對：

- 資料段七個欄位（Feature 編號／狀態、CR 編號／所屬 Feature／狀態、orphan CR 清單、警告）皆有對應呈現。
- 操作段「需確認？」欄為『否』→ 實作無任何確認對話框，符合。
- 狀態段：載入中（`載入中…`）、空資料（`尚無 Feature／CR 卡片`）皆實作；「錯誤／無權限」標不適用，實作未加角色差異。
- 驗收條件「開啟畫面時觸發 `uc-view-feature-cr-board`」→ `useEffect` 掛載即呼叫，測試以 `calls.some(... endsWith('/api/boards/board-a/feature-cr-board'))` 斷言確實發出請求。

API 契約對照後端（`loop/implementation` 已合併的 T-08）：`FeatureCrBoardController` `@RequestMapping("/api/boards")` ＋ `@GetMapping("/{boardId}/feature-cr-board")`，`FeatureCrBoardResponse(features, orphanCrIds, warnings)`／`FeatureView(featureId, status, crs)`／`CrView(crId, status)`，與 `featureCrBoardApi.ts` 的型別逐欄一致。

### 3. `kanban-core` 純度

本輪 diff 未觸及任何 Java 檔（見第 4 點 stat），`io.progden.kanban.core.domain` 無異動；F06 屬讀取投影，既有實作位於 `io.progden.kanban.query.featurecrboard.*`（T-08 已合併），位置正確。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：10 檔、+469/-1。程式碼僅
`kanban-frontend/src/api/featureCrBoardApi.ts`、`src/canvas/FeatureCrBoardItem.tsx|.css|.test.tsx`、`src/main.tsx`（僅新增一行 `registerItemComponent('feature-cr-board', ...)` 與註解）；`.state/` 僅 `.state/tasks/T-20-fe-feature-cr-board/**`。未動別的模組、`.dev/conventions/**`、`scripts/**`、spec／ui 文件本體、`.state/tasks.md`、`.state/archive/**`、別的任務目錄。`git status --porcelain` 為空。邊界乾淨。

### 5. 交接摘要待確認事項／OQ 核對

Dev 交接摘要唯一待確認事項已開成 `OQ-T-20-fe-feature-cr-board-01`（`loopctl show` 列得出）。逐項核對：

- 引文一（`ui-feature-cr-board.md`「進入與離開」）：與源頭檔逐字比對一致。
- 引文二（`.state/tasks.md` T-20 列備註、`.state/archive/open-questions.md` OQ-IMPL-06 解除說明）：逐字比對一致；OQ-IMPL-06 解除說明確實只處理依賴與編號，未處理「機制待整合 CR 定案」。
- 等級／阻塞判定：`spec-canvas-layout.md` 欄位表 `item.component` 逐字寫『其餘元件的值待各自所屬模組實作對應 Item 時決定』——由 F06 自行決定識別碼字串是定稿原文明確授權的事，實作不必違反任何定稿原文，因此「等級：高、阻塞：否」判定正確，不需改為「覆蓋」。
- 接手：`.state/tasks.md` 無任何任務的產出範圍涵蓋「各元件入口機制／`item.component` 命名慣例定案」，填『無』屬實，未虛掛任務。
- 前例一致性：`OQ-T-16-fe-activity-log-01` 確實已對 `s-activity-log` 採同一結論（沿用 T-13 一般化「＋ 加入元件」掛載、`item.component` 命名為推論），本任務比照合理。

無需另開取代性 OQ。

### 6. 前端視覺依據

`.dev/ui-prototype/README.md` 對照表無 F06／`s-feature-cr-board` 的 `*.dc.html`，README 逐字規定『沒有設計稿的畫面（`s-activity-log`、`s-cards-by-assignee`、F03／F04／F05／F06 的 Canvas item 等）：沿用上面這些檔案的視覺語彙（同樣的字體、配色、按鈕／輸入框／對話框樣式）做最簡潔可用的版面，不另外發明風格』。核對 `FeatureCrBoardItem.css`：只使用既有 design token（`--color-text`／`--color-text-label`／`--color-text-subtle`／`--color-text-muted`／`--color-border`／`--color-error-text`，均已定義於 `src/index.css:6-14`），無自訂色碼、無自訂字體，結構與 `ActivityLogItem` 一致，未發明新風格。畫面文字只有「載入中…」「尚無 Feature／CR 卡片」「orphan CR」與後端回傳內容，未出現設計稿的灰色註記（Attribute ID、`uc-xxx`、「⚠️ 規格未定義」）。

### 判定：附保留核准（`done`）

建置／測試／lint 全綠、spec 五個 Scenario 與 ui 四段皆有對應且行為相符、邊界乾淨、無阻塞 OQ。

保留事項（逐條含接手者）：

1. `OQ-T-20-fe-feature-cr-board-01`（等級高、不阻塞、接手：無）：`item.component` 值 "feature-cr-board" 與「無專屬入口、沿用畫布一般化掛載」為本任務推論決定。**接手者：無既有任務**——該 OQ 已以 `--owner 無` 開立並說明，待 `spec-canvas-layout.md`「待釐清」與 OQ-49 的整合 CR 由人工定案後，再依 CR 追加修訂任務實例（`T-20-fe-feature-cr-board-r2` 之類）回頭核對；本輪不另開重複 OQ。
