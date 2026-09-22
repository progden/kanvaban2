# T-17-fe-clock-control 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：附保留核准

判定：**附保留核准**（status＝`done`）。以下為本輪 Review 自己實際執行的驗證，不採信 Dev 交接摘要的「Check」欄。

### 1. 自己跑建置與完整測試

在 `kanban-frontend/`（worktree 已有 `node_modules`，未安裝任何新工具）：

- `npm run build`（`tsc -b && vite build`）：`✓ 51 modules transformed`、`✓ built in 1.57s`，成功。
- `npx vitest run`：`Test Files 7 passed (7)`、`Tests 49 passed (49)`，全綠。
- `npm run lint`（oxlint）：只有 1 個 warning `src/canvas/itemComponentRegistry.tsx:22:10: warning react(only-export-components)`，該檔的多重匯出是 T-13 既有結構，本任務只加了一個欄位，非新增問題。
- 後端未被改動（diff 無 `kanban-core`／`kanban-spring` 檔），故未跑 Gradle。

### 2. spec／ui 對應

`ui-board-clock.md` 的五條驗收條件逐條對上 `BoardClockControl.test.tsx` 的測試，且行為（非僅測試存在）核對過原始碼：

- 「開啟時顯示看板時間目前值與狀態」→ 測試「開啟時顯示看板時間目前值與狀態」；`clock-control-time`／`clock-control-status` 來源為 `board.clockTime`／`board.clockStatus`，對應資料表的 `board.clock-time`／`board.clock-status`。
- 「調整看板時間後…且觸發 `uc-adjust-board-clock`」→ 測試斷言打到 `PATCH /api/boards/{id}/clock`；`boardApi.adjustClock` 送 `{ newTime }`，與後端 `BoardController.java:199` 的 `AdjustClockRequest(Instant newTime)` 形狀一致（已對讀後端原始碼）。
- **抽查 `@fail-p1`**（`uc-adjust-board-clock` fail p1：『拒絕，顯示錯誤訊息「只有 Owner 可以調整看板時間」，`board.clock-time` 維持不變』）→ 測試「uc-adjust-board-clock 失敗時顯示訊息，看板時間顯示不變」：403＋message 時 `role="alert"` 顯示該訊息，且 `clock-control-time` 仍為 `2026-09-12T11:00:00Z`。原始碼核對 `handleAdjust` 的 catch 分支只 `setError`，不呼叫 `setBoard`、也不清掉 `targetTime`，符合 ui 操作表失敗欄『保留對話框與已輸入的目標時間，顯示訊息』——「資料不變」成立。
- 暫停／恢復兩條 → 對應兩個測試，打 `POST .../clock/pause`／`.../clock/resume`（後端 `BoardController.java:208`／`216` 存在）；`uc-pause-resume-board-clock` 的 `fail: {}` 確實沒有失敗情境，未做多餘的失敗處理，正確。
- 角色差異：ui 角色表 `r-board-member`『不做得到』＋「無權限」段 → 測試「非 Owner 只能檢視…」；實作以 `GET /api/boards/{id}/members` 找自己的 `role === 'OWNER'` 決定是否渲染操作區，非 Owner 仍看得到時間與狀態（符合 OQ-55「比照畫布 item 一律可見」）。
- 活動紀錄（兩個 uc 的 post 第 2 條）屬後端 T-05 範圍，本任務不重複實作，同意。

### 3. `kanban-core` 純度

本任務 diff 未觸及 `kanban-core`／`kanban-spring`，此項不適用（已用 `git diff --stat` 確認）。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：13 檔、+423/-2，全部落在 `kanban-frontend/src/{api,canvas,pages}`、`.state/tasks/T-17-fe-clock-control/**` 與**新增**的 `.state/adr/ADR-T-17-fe-clock-control-01-*.md`。沒有動到 `.state/tasks.md`、`.state/archive/**`、別的任務目錄、`.dev/conventions/**`、`scripts/**`、spec／ui 文件本體。邊界乾淨。跨檔改動（`ItemContentProps` 加 `boardId`、`CanvasStage` 傳入、`BoardCanvasPage` 註冊）是讓 item 內容能觸發 uc 的必要最小改動，屬本任務範圍。

### 5. OQ 核對

- `OQ-T-17-fe-clock-control-01`（元件識別碼與註冊位置慣例）：引文回源頭逐字比對過——`ui-board-clock.md`「進入與離開」那句、`itemComponentRegistry.tsx` 與 `PlaceItemDialog.tsx` 的原有註解皆一字不差。等級「高」／不阻塞正確：規格對整合機制是**留白**（『仍待該 spec「待釐清」與整合 CR 定案』），照最小可行慣例做並不需要違反任何定稿原文，通過「是否必須違反已定稿原文」檢驗。接手填「人工」如實（OQ-49 的整合 CR 是人工決策，無既有任務會處理），可接受。對應 `ADR-T-17-fe-clock-control-01` 為新增檔，合法。
- Dev 在決策紀錄裡自行判斷「『關閉』操作不實作、且不另開 OQ」——這點本輪**不同意**：`ui-board-clock.md` 同一個已定案畫面同時寫『類型：對話框』＋操作表『| 關閉 | — | 關閉對話框 | — | 否 |』與『內容以 F07 item 形式顯示於 s-canvas』，是同一份文件內兩處矛盾，不是單純的「舊分類殘留」可由實作逕自判定。已依 review-prompt 第 5 點另開 `OQ-T-17-fe-clock-control-02`（等級高／不阻塞／接手人工／`spec-conflict`），內文兩段原文並列並註明取代 Dev 未開立的部分。不阻塞的理由同上：兩處矛盾時照較上游／較新的那段（「進入與離開」明指 F07 item）實作，不需要改動任何定稿文字。

### 6. 前端視覺依據

`.dev/ui-prototype/` 沒有本畫面的設計稿（目錄僅 F01／F02／F07 的 20 個 `.dc.html`）；其 `README.md` 第 36 行明寫『沒有設計稿的畫面（`s-activity-log`、`s-cards-by-assignee`、F03／F04／F05／F06 的 Canvas item 等）：沿用上面這些檔案的視覺語彙…不另外發明風格』。實作沿用既有 `field`／`field-label`／`field-input`／`btn`／`btn-secondary`／`form-error`（`index.css`）與 `canvas-dialog__actions`（`CanvasStage.css`），新增的 `.clock-control*` 只有間距與字級並用既有 `--color-text-muted` 變數，沒有自創配色或字體。產品畫面沒有出現 Attribute ID、`uc-xxx`、「⚠️ 規格未定義」這類灰色註記；狀態顯示 `REALTIME`／`PAUSED` 是 ui 資料表明定的 enum 值，不是註記。操作表「需確認？」欄三個操作皆為『否』，實作也確實沒有二次確認對話框，一致。

### 保留事項（各自接手者）

1. `OQ-T-17-fe-clock-control-01`：canvas item 識別碼命名與註冊位置慣例，含 `BoardCanvasPage.tsx` 被多個平行任務同時追加註冊呼叫的合併衝突風險。接手者：**人工**（隨 OQ-49 整合 CR 一併定案）；若定案改用集中式註冊表，受影響任務需回頭調整註冊位置。
2. `OQ-T-17-fe-clock-control-02`：`s-board-clock-control`「類型：對話框」＋操作表「關閉」列 與「以 F07 item 形式顯示」矛盾，目前實作未提供「關閉」操作。接手者：**人工**（需走 CR 修 ui 檔，或另開修正任務改實作）。
3. 小瑕疵（不影響判定、不另開 OQ）：`ADR-T-17-fe-clock-control-01` 的「後果」第一段有一個簡體字『会各自在 BoardCanvasPage.tsx 加一行』，依規則書第 6 節 `.state/` 紀錄應一律 zh-TW。接手者：**人工**（既有 ADR 只能由人工改）。
