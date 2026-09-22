# T-17-fe-clock-control 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：實作 s-board-clock-control canvas item

### 這輪做了什麼

依 `.dev/F04-board-clock/ui-board-clock.md`「s-board-clock-control：看板時鐘控制」實作 F07 canvas item：

- `kanban-frontend/src/canvas/BoardClockControl.tsx`：item 內容本體，掛載時打 `GET /api/boards/{boardId}`（已含 `clockTime`／`clockStatus`）與 `GET /api/boards/{boardId}/members` 判斷操作者是否為 `r-board-owner`；顯示看板時間目前值與狀態；Owner 可用 `datetime-local` 輸入調整目標時間並觸發 `uc-adjust-board-clock`（`PATCH /api/boards/{boardId}/clock`），或觸發 `uc-pause-resume-board-clock`（`POST .../clock/pause`／`.../clock/resume`）；非 Owner 只顯示目前值與狀態，看不到操作按鈕（對應 ui 檔角色表「不做得到」）。
- `kanban-frontend/src/canvas/itemComponentRegistry.tsx`：`ItemContentProps` 新增 `boardId`，`CanvasStage.tsx` 同步把 `boardId` 傳給 item 內容——見決策說明。
- `kanban-frontend/src/pages/BoardCanvasPage.tsx`：模組頂層呼叫 `registerItemComponent('board-clock-control', BoardClockControl)` 完成掛載。
- `kanban-frontend/src/api/boardApi.ts`：新增 `adjustClock`／`pauseClock`／`resumeClock`，對應後端既有 `BoardController` 端點（T-05 已完成，本任務未改動後端）。

### 規格沒寫清楚的地方怎麼處理

`ui-board-clock.md`「進入與離開」明講這類元件如何被加入畫布的機制待 `spec-canvas-layout.md`「待釐清」與整合 CR 定案（OQ-49）。本任務不等這個 CR，直接推論一個最小可行的註冊慣例（`item.component` 值 `board-clock-control`、註冊呼叫放在 `BoardCanvasPage.tsx` 頂層），並把 `ItemContentProps` 補上 `boardId`（原本只有 itemId/component/width/height，任何要打 API 的 item 都會缺這個）。已開 `OQ-T-17-fe-clock-control-01`（不阻塞）與 `ADR-T-17-fe-clock-control-01`（跨任務慣例，供 T-14/T-15/T-16/T-18/T-19/T-20 平行任務參考，說明合併衝突風險）。

ui 檔的「關閉」操作（型別：對話框，含「中途放棄」「關閉」列）在此實作為 canvas item 常駐顯示，不是彈出式對話框，因此沒有做「關閉」按鈕——item 的顯示／隱藏是 Canvas 既有的移除／能力設定機制（`ui-canvas-layout.md` 範圍），不重複實作。這點沒有另開 OQ，因為 ui 檔本身已說明「內容以 F07 item 形式顯示」，對話框式的進入/離開描述是舊有畫面分類殘留，判斷為推論即可決定、不涉及覆蓋定案文字。

### 涵蓋範圍

- `uc-adjust-board-clock`：成功／`fail-p1`（顯示訊息、看板時間顯示不變）皆有測試。
- `uc-pause-resume-board-clock`：暫停／恢復皆有測試（該 usecase 無 fail 分支，符合 spec）。
- 角色差異：Owner 可操作、非 Owner 僅檢視，皆有測試。
- 活動紀錄（`uc-adjust-board-clock`／`uc-pause-resume-board-clock` post 第 2 條「記錄一筆活動紀錄」）：後端 T-05 已實作並有 Cucumber 覆蓋，本任務不重複測；活動紀錄的顯示畫面屬於 T-16-fe-activity-log 範圍，不在本任務。
- 待確認事項：見 `OQ-T-17-fe-clock-control-01`（canvas item 註冊慣例）。

### Check

- `pnpm install`（首次跑，worktree 沒有 `node_modules`，只安裝既有 `package.json` 宣告的相依套件，未安裝任何新工具鏈）
- `npx vitest run`：7 個測試檔、49 個測試全過（含新增的 `BoardClockControl.test.tsx` 6 個測試）
- `npm run build`（`tsc -b && vite build`）：成功
- `npm run lint`（oxlint）：僅既有的 1 個 warning（`itemComponentRegistry.tsx` fast-refresh 提示，非本任務新增）
