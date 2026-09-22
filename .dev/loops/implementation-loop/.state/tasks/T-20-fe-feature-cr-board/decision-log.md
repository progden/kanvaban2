# T-20-fe-feature-cr-board 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：s-feature-cr-board 依 T-08 投影實作

### 這輪做了什麼

依 `uc-view-feature-cr-board` 實作 `s-feature-cr-board`（Canvas item）：

- `kanban-frontend/src/api/featureCrBoardApi.ts`：呼叫 T-08 已合併的 `GET /api/boards/{boardId}/feature-cr-board`，回傳型別與後端 `FeatureCrBoardResponse` 一致（`features[].featureId/status/crs[]`、`orphanCrIds`、`warnings`）。
- `kanban-frontend/src/canvas/FeatureCrBoardItem.tsx`＋`.css`：Canvas item 內容，依序顯示警告區塊（若有）、Feature 清單（每個 Feature 底下巢狀列出其 CR）、orphan CR 區塊（若有）；三者皆空時顯示「尚無 Feature／CR 卡片」。
- `kanban-frontend/src/main.tsx`：`registerItemComponent('feature-cr-board', FeatureCrBoardItem)`，掛上 T-13 建立的一般化 item 掛載點（沿用「＋ 加入元件」對話框輸入 `item.component` 識別碼，未新增專屬入口）。

`item.component` 字串值（"feature-cr-board"）與版面細節為本任務依 `ui-feature-cr-board.md`「進入與離開」段落標記的待釐清事項、比照 T-16（`s-activity-log`）已採用的相同結論推論決定，已開 `OQ-T-20-fe-feature-cr-board-01`（不阻塞）說明。

### 涵蓋範圍

- Entity／UseCase：`uc-view-feature-cr-board`（`spec-feature-cr-board.md` 唯一 uc）
- Scenario（前端呈現面，後端行為與 Cucumber 驗收由 T-08 負責，本任務不重複測）：
  - 「檢視 Feature 的開發狀態」→ 顯示 Feature 編號與狀態
  - 「檢視 CR 影響哪個 Feature 以及其狀態」（`@CR-013`）→ Feature 底下巢狀顯示 CR 與其狀態
  - 「CR 指到不存在的 Feature 時列為 orphan」→ orphan CR 區塊
  - 「標籤格式不合時列為警告，不影響其他卡片顯示」→ 警告區塊（後端已組好警告文字，前端只負責呈現）
  - 「Feature／CR 標籤不分大小寫」→ 大小寫正規化由後端 `FeatureCrBoardCalculator` 負責，前端原樣顯示後端回傳的 `featureId`／`crId`
- `ui-feature-cr-board.md`「資料」「操作」「狀態」四段（載入中／空資料／錯誤）皆已對應實作；「錯誤」段雖標「不適用（無 fail 定義）」，仍依專案既有慣例（比照 `ActivityLogItem`）處理網路層失敗，顯示 `form-error` 樣式錯誤訊息。
- 未涵蓋／延後：`ui-feature-cr-board.md`「進入與離開」提到的專屬入口機制（待 OQ-49 對應整合 CR 定案），見 OQ-T-20-fe-feature-cr-board-01。

### 待確認事項

OQ-T-20-fe-feature-cr-board-01（不阻塞）：`item.component` 命名與入口機制是否符合預期，是否需等整合 CR 定案後回頭修正。

### Check

於 `kanban-frontend/` 實際執行：

- `pnpm install`
- `pnpm exec vitest run`：`Test Files 9 passed (9)`、`Tests 59 passed (59)`（含新增 `FeatureCrBoardItem.test.tsx` 6 個測試）
- `pnpm run build`（`tsc -b && vite build`）：`✓ 57 modules transformed`、`✓ built in 1.11s`
- `pnpm run lint`（oxlint）：僅既有 `itemComponentRegistry.tsx` 的 fast-refresh 警告（T-13 遺留，本任務未改動該檔），無新增警告

三項全綠。
