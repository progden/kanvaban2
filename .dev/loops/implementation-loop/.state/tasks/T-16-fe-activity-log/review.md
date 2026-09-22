# T-16-fe-activity-log 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：退回

判定：退回（`doing`），新增 D-01；另開 OQ-T-16-fe-activity-log-02（不阻塞）補正 Dev OQ 的一句事實敘述。

### 1. 自己跑建置與測試（不採信交接摘要）

於 `kanban-frontend/` 實際執行：

- `pnpm exec vitest run`：`Test Files 7 passed (7)`、`Tests 47 passed (47)`、`Duration 60.40s`。
- `pnpm run build`（`tsc -b && vite build`）：`✓ 53 modules transformed`、`✓ built in 1.59s`。
- `pnpm run lint`（oxlint）：唯一一則 `src/canvas/itemComponentRegistry.tsx:21:10: warning react(only-export-components)`，該檔本輪未改動（T-13 既有），非本任務新增。

三項皆與 Dev 交接摘要一致，建置／測試綠燈，不構成退回理由。

### 2. spec 對應

`spec-user-membership.md`「Feature: 檢視看板活動紀錄」只有一個 uc：`uc-view-board-activity-log`，`roles: [r-board-owner, r-board-member]`、`crud: {board: R, board-membership: R}`、`pre: {}`、`fail: {}`、`emits: []`。**本 uc 沒有任何 fail 分支**，所以「抽查至少一個 `@fail-pN` 情境」在本任務無對象可抽（全檔搜尋亦無掛在本 uc 的 `@fail-` tag）；對應改以檢查唯一 Scenario 的 post 落實情況。

post p1『該看板的活動紀錄（合併 `board` 與 `board-membership` 的紀錄）依時間由新到舊列出，每一筆都顯示操作人與動作內容』、p2『最上面一筆對應最近發生的事件，例如邀請成員加入看板的 `board-membership` 異動』：

- 排序：後端 `BoardActivityLogQueryService.java:41` `entries.sort(Comparator.comparing(BoardActivityLogEntry::occurredAt).reversed())`（新到舊），前端 `ActivityLogItem.tsx` 依回傳順序原樣 `map`，不再重排，符合 p1／p2。
- 操作人與動作：`ActivityLogItem.tsx` 每列輸出 operator（`operatorDisplayName ?? operatorUsername ?? '未知使用者'`）、`action`、`occurredAt`，符合「每一筆都顯示操作人與動作內容」。
- 合併來源：由 T-04 的 `io.progden.kanban.query.BoardActivityLogQueryService` 負責，前端只消費，未在前端重新合併，分層正確。
- 契約核對：`activityLogApi.ts` 的 `ActivityLogEntryView{operatorUsername, operatorDisplayName, action, occurredAt}` 與後端 `ActivityLogEntryResponse`（`kanban-spring/.../web/ActivityLogEntryResponse.java`）逐欄一致；`operatorUsername`／`operatorDisplayName` 在 `BoardController.toActivityLogEntryResponse` 查無 `User` 時確為 `null`，前端的 null 備援處理正確。
- 權限：`BoardController.viewActivityLog` 先 `boardMembershipApplicationService.ensureMember(boardId, operatorId)`，對應 uc 的 roles；前端不自行放行，正確。
- 前端測試涵蓋：`ActivityLogItem.test.tsx` 4 個測試分別驗證排序＋操作人＋動作、空清單、操作人查無使用者的備援、載入失敗，均透過 `App` 與真實路由掛載，測的是行為而不只是「測試存在」。

結論：spec 對應無缺漏。

### 3. `kanban-core` 純度

本任務 diff 未觸及任何 Java 檔（見第 4 點），`kanban-core`／`kanban-spring` 無改動，純度不受本輪影響；跨 aggregate 讀取投影仍在 `io.progden.kanban.query.BoardActivityLogQueryService`（T-04 已合併的位置），前端未把合併邏輯搬到別處。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat` 共 10 檔、333 增 1 減：

- 程式碼：`kanban-frontend/src/api/activityLogApi.ts`、`canvas/ActivityLogItem.tsx`／`.css`／`.test.tsx`、`main.tsx`（僅新增 5 行註冊）。全部落在 `.state/tasks.md` 第 44 列 T-16 的產出範圍（`s-activity-log`）內，未動到別的模組、別的 aggregate、`.dev/conventions/**`、`scripts/**` 或 spec／ui 文件本體。
- `.state/`：只有 `.state/tasks/T-16-fe-activity-log/` 底下的 `decision-log.md`／`open-questions.md`／`rounds.log`／`state.md`／`status`，沒有 `.state/tasks.md`、`.state/archive/**` 或別的任務目錄。

邊界乾淨，不構成退回理由。

### 5. Dev 的待確認事項與 OQ 逐一核對

交接摘要的待確認事項只有一項（`item.component` 命名與版面細節係推論決定），確實已開成 OQ-T-16-fe-activity-log-01，`loopctl show` 列得出來，沒有只寫在摘要裡就消失的項目。

OQ-T-16-fe-activity-log-01 核對結果：

- 引文（逐字比對來源）：對 `ui-user-membership.md` `s-activity-log` 的引用『依 T2.10 分支規則，本畫面暫緩定案，僅保留八段標題與簡短說明，不產出完整內容，見 OQ-29』與該檔第 389 行起的段落逐字相符；八段確實皆為「不適用，理由同上」。對 `ui-authoring-open-questions.md` OQ-29「採用」欄的引用亦與 `.dev/loops/ui-authoring-loop/.state/ui-authoring-open-questions.md` 第 51 列逐字相符。引文本身無誤。
- 等級「高」／阻塞「否」：正確。用檢驗句判斷——把本任務做完不需要違反任何定稿原文：spec 的 `uc-view-board-activity-log` 與 Scenario 是已定稿且完整的行為依據，ui 檔缺的是尚未產出的內容（缺口），不是與 spec 衝突的規定；引用方向 `spec ← ui`，以 spec 為準即可完成，故屬「高、不阻塞」，不是「覆蓋」。
- 接手「無」：正確。`.state/tasks.md` 中沒有任何任務的產出範圍涵蓋「回填 `ui-user-membership.md` 的 `s-activity-log` 八段」，據實填「無」而非亂掛任務。
- 需補正之處：該則「情況」欄的推論句『OQ-29 所設的「回頭定案」前提已成立』把 OQ-29 的兩個前提說成都成立，實際上只成立一個（「spec 更新」尚未發生，spec 原文仍寫『本情境目前尚未實作』）。這不改變等級／阻塞／接手的判定，故不取代原 OQ，另開 **OQ-T-16-fe-activity-log-02**（高、不阻塞、接手：人工、`spec-outdated`）逐字並列兩處原文並說明落差。

### 6. 前端視覺依據核對

`.dev/ui-prototype/README.md` 對照表中**沒有** `s-activity-log` 的設計稿，README 第 36 行逐字寫『沒有設計稿的畫面（`s-activity-log`、`s-cards-by-assignee`、F03／F04／F05／F06 的 Canvas item 等）：沿用上面這些檔案的視覺語彙（同樣的字體、配色、按鈕／輸入框／對話框樣式）做最簡潔可用的版面，不另外發明風格。』據此核對：

- 配色：`ActivityLogItem.css` 全部使用 `src/index.css` 既有變數（`--color-text`／`--color-text-label`／`--color-text-subtle`／`--color-text-muted`／`--color-border`），未新增色票，合格。
- 灰色註記外洩：畫面輸出無 Attribute ID、無 `uc-xxx`、無「⚠️ 規格未定義」字樣；這些只出現在原始碼註解，合格。
- `ui-*.md` 操作表的「需確認？」／「失敗時」欄：`s-activity-log` 為純檢視畫面，`ui-user-membership.md` 該畫面的「操作」段為「不適用，理由同上」，無操作列可落實；`uc-view-board-activity-log` 的 `fail` 為空，無失敗分支呈現要求。此項無對象可核。
- **不合格之處（D-01）**：載入失敗的 `.activity-log__error` 只設了 `padding`／`font-size`，沒有沿用專案唯一的錯誤訊息樣式 `.form-error`（`src/index.css:148`），會以無色內文呈現；同一個 Canvas 群組內的 `CanvasStage.tsx:433`、`PlaceItemDialog.tsx:66`、`RemoveItemsDialog.tsx:34` 全都用 `form-error`。這是對 README 第 36 行「沿用視覺語彙」的偏離，落在本任務範圍內、一處可改，已開 D-01。

### 判定與理由

建置／測試綠燈、spec 對應無缺漏、邊界乾淨、無阻塞 OQ；唯第 6 點的錯誤狀態視覺語彙偏離（D-01）屬本任務範圍內、應由 Dev 修正的交付缺口，且第 1 輪（上限 6）尚有餘裕，故判定**退回**、狀態改回 `doing`，由下一輪 Dev 處理 D-01。

兩則 OQ 皆為不阻塞，接手者：OQ-T-16-fe-activity-log-01 為「無」（無任務涵蓋回填 ui 文件）、OQ-T-16-fe-activity-log-02 為「人工」；兩則都不影響本輪判定，留待核准輪以保留事項形式帶出。

## 2026-09-22 Review 第 2 輪：附保留核准

判定：附保留核准（`done`）。D-01 已確實修好，建置／測試／lint 三項自行重跑全綠，邊界乾淨，兩則 OQ 皆為不阻塞。

### 1. 自己跑建置與測試（不採信交接摘要）

於 `kanban-frontend/` 實際執行：

- `pnpm exec vitest run`：`Test Files 7 passed (7)`、`Tests 47 passed (47)`、`Duration 37.12s`。
- `pnpm run build`（`tsc -b && vite build`）：`✓ 53 modules transformed`、`✓ built in 802ms`。
- `pnpm run lint`（oxlint）：唯一一則 `src/canvas/itemComponentRegistry.tsx:21:10: warning react(only-export-components)`，該檔本任務未改動（T-13 既有），非本任務新增。

三項綠燈。`git status --short` 無輸出，工作區乾淨。

### 2. D-01 的修正驗證（本輪重點）

- `ActivityLogItem.tsx:46` 現為 `<p role="alert" className="form-error activity-log__error">`，與同群組的 `CanvasStage.tsx:433`（`form-error canvas-page__error`）、`PlaceItemDialog.tsx:66`、`RemoveItemsDialog.tsx:34`、`BoardListPage.tsx:66` 等 11 處 `form-error` 用法一致（`grep -rn "form-error" kanban-frontend/src --include=*.tsx` 共 11 列，本檔為其一）。
- `ActivityLogItem.css` 的 `.activity-log__error` 已收斂為 `margin: 0; padding: 12px;`，不再自訂 `font-size`，顏色／底色／框線／圓角全部由 `src/index.css:148` 的 `.form-error` 提供（`--color-error-text`／`--color-error-bg`／`--color-error-border`／`border-radius: 6px`）。`padding` 覆寫屬 D-01 明文允許的「只調整版面的修飾類別」。
- 載入中／空狀態維持純文字 `<p>`（`.activity-log__loading`／`.activity-log__empty`，用 `--color-text-muted`），符合 D-01「維持現狀即可」。
- 既有「載入失敗時顯示錯誤訊息」測試（`ActivityLogItem.test.tsx` 第 4 個）以 `findByRole('alert')` 查詢，不受 class 變動影響，仍為綠燈。

D-01 判定為修好。

### 3. spec 對應（複核）

`spec-user-membership.md:819` 的 `uc-view-board-activity-log`：`roles: [r-board-owner, r-board-member]`、`crud: {board: R, board-membership: R}`、`pre: {}`、`fail: {}`、`emits: []`。**`fail` 為空，全檔無掛在本 uc 的 `@fail-` tag**，故「抽查至少一個 `@fail-pN` 情境」在本任務無對象可抽，改以複核唯一 Scenario（`spec-user-membership.md:847`）的兩條 post：

- p1「依時間由新到舊列出，每一筆都顯示操作人與動作內容」：排序由後端 `BoardActivityLogQueryService`（`entries.sort(...reversed())`）保證，前端 `ActivityLogItem.tsx` 原樣 `map` 不重排；每個 `<li>` 輸出 operator／action／occurredAt 三欄。測試 1 以兩筆資料驗證順序與欄位內容。
- p2「最上面一筆對應最近發生的事件，例如邀請成員加入看板」：測試 1 的 `items[0]` 斷言正是「邀請 "雅婷" 加入看板」，與 Scenario 的 Then／And 對齊。
- 契約：`activityLogApi.ts` 的 `ActivityLogEntryView{operatorUsername, operatorDisplayName, action, occurredAt}` 與後端 `ActivityLogEntryResponse` 逐欄一致，null 備援（`?? '未知使用者'`）有專門測試覆蓋。
- 權限由後端 `BoardController.viewActivityLog` 的 `ensureMember` 落實，前端不自行放行。

spec 對應無缺漏，本輪未發現第 1 輪以外的新問題。

### 4. `kanban-core` 純度

`git diff loop/implementation...HEAD --stat` 顯示本任務未觸及任何 Java 檔，`kanban-core`／`kanban-spring` 零改動，純度不受影響；跨 aggregate 讀取投影仍在 T-04 已合併的 `io.progden.kanban.query.BoardActivityLogQueryService`，前端未把合併邏輯下放。

### 5. 任務邊界

`git diff loop/implementation...HEAD --stat`：12 檔、454 增 1 減。

- 程式碼 5 檔：`kanban-frontend/src/api/activityLogApi.ts`、`canvas/ActivityLogItem.tsx`／`.css`／`.test.tsx`、`main.tsx`（僅新增 5 行 `registerItemComponent('activity-log', ...)`）。全部落在 `.state/tasks.md:44` T-16 的產出範圍（`s-activity-log`）內。
- `.state/` 7 檔：全部在 `.state/tasks/T-16-fe-activity-log/` 底下（`decision-log.md`／`fixes.md`／`open-questions.md`／`review.md`／`rounds.log`／`state.md`／`status`），沒有 `.state/tasks.md`、`.state/archive/**` 或別的任務目錄。
- 未觸及 `.dev/conventions/**`、`scripts/**`、spec／ui 文件本體。

邊界乾淨。

### 6. 前端視覺依據核對

`.dev/ui-prototype/README.md:36` 逐字寫『沒有設計稿的畫面（`s-activity-log`、`s-cards-by-assignee`、F03／F04／F05／F06 的 Canvas item 等）：沿用上面這些檔案的視覺語彙（同樣的字體、配色、按鈕／輸入框／對話框樣式）做最簡潔可用的版面，不另外發明風格。』對照表中沒有 `s-activity-log` 的 `*.dc.html` 設計稿。

- 配色：`ActivityLogItem.css` 全部使用既有變數（`--color-text`／`--color-text-label`／`--color-text-subtle`／`--color-text-muted`／`--color-border`），錯誤態改走 `.form-error` 後已無自訂色票，本輪不合格項（D-01）消除。
- 灰色註記外洩：畫面輸出無 Attribute ID、無 `uc-xxx`、無「⚠️ 規格未定義」；這些字樣只出現在原始碼註解（`ActivityLogItem.tsx` 檔頭、`activityLogApi.ts` 檔頭、`main.tsx` 註解），不會進入產品畫面。
- `需確認？`／`失敗時` 欄：`ui-user-membership.md:389` 的 `s-activity-log` 狀態為「未討論」，八段皆『不適用，理由同上』，「操作」段無操作列；`uc-view-board-activity-log` 的 `fail` 為空。此兩欄無對象可核（已由 OQ-T-16-fe-activity-log-01 記錄此缺口）。

### 7. Dev 的待確認事項與 OQ 逐一核對

Dev 第 2 輪交接摘要的「待確認事項」只寫「兩則 OQ 皆為上一輪已開立、不阻塞，本輪未新增 OQ」，`loopctl show` 列出 `OQ-T-16-fe-activity-log-01`／`02` 兩則，與摘要一致，沒有只寫在摘要裡就消失的項目。

兩則 OQ 重新核對（用檢驗句：把這個任務做完，是否必須違反某一段已定稿的原文？）：

- OQ-01（高／否／接手：無／`spec-ambiguous`）：ui 檔 `s-activity-log` 缺的是**尚未產出的內容**，不是與 spec 衝突的規定；引用方向 `spec ← ui`，以 spec 已定稿的 `uc-view-board-activity-log` 與 Scenario 為準即可完成本任務，不需違反任何原文 → 「高、不阻塞」正確。`.state/tasks.md` 無任何任務的產出範圍涵蓋「回填 `ui-user-membership.md` 的 `s-activity-log` 八段」，接手填「無」屬實。
- OQ-02（高／否／接手：人工／`spec-outdated`）：`spec-user-membership.md:855` 至今仍逐字寫『本情境目前尚未實作。』這是**現況陳述**而非禁止實作的規定，且 `.state/tasks.md:44` 明確把 `s-activity-log` 排為 T-16 的產出範圍，故不構成「必須違反定稿原文」→ 「高、不阻塞」正確。本 loop 禁止修改 spec 本體，沒有既有任務會處理這段文字更新，接手填「人工」屬實。

兩則皆非「覆蓋」等級，故本任務可核准為帶保留的 `done`。

### 判定與理由

第 1～7 點全數通過：建置／測試／lint 綠燈、D-01 確實修好、spec 對應無缺漏、`kanban-core` 純度不受影響、邊界乾淨、視覺語彙已對齊既有系統、無阻塞 OQ。判定 **附保留核准**（`done`）。

### 保留事項與接手者

1. `ui-user-membership.md:389` 的 `s-activity-log` 仍為「未討論」、八段皆『不適用，理由同上』，本次實作的版面細節（`item.component` 識別碼 "activity-log"、欄位呈現、時間格式、空狀態文字）是推論決定而非 ui 檔定案內容。**接手：無**（`.state/tasks.md` 無任務涵蓋，已由 OQ-T-16-fe-activity-log-01 以 `--owner 無` 記錄，待人工決定要不要另開 ui-authoring 任務回填）。
2. `spec-user-membership.md:855` 的『本情境目前尚未實作。』與 `ui-user-membership.md` 的「未討論」狀態，會在本任務合併後同時落後於實作；是否需走 CR 更新亦未定。**接手：人工**（已由 OQ-T-16-fe-activity-log-02 以 `--owner 人工` 記錄）。
