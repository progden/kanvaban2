# T-16-fe-activity-log 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：s-activity-log 依 T-04 投影補齊實作

### 這輪做了什麼

`s-activity-log` 在 `ui-user-membership.md` 因 OQ-29 標記「未討論」（跨 aggregate 查詢投影當時尚未實作），但 T-04-be-board-membership 已合併並補上 `BoardActivityLogQueryService`／`GET /api/boards/{boardId}/activity-log`（回傳 `operatorUsername`／`operatorDisplayName`／`action`／`occurredAt`，已依時間新到舊排序），OQ-29 的前提已滿足。依 `uc-view-board-activity-log` 的 usecase 區塊（roles/pre/post）與 Gherkin Scenario「活動紀錄依時間新到舊排序，並顯示操作人與動作」實作最小可用畫面：

- `kanban-frontend/src/api/activityLogApi.ts`：呼叫上述端點。
- `kanban-frontend/src/canvas/ActivityLogItem.tsx`＋`ActivityLogItem.css`：Canvas item 內容，列出操作人（`displayName` 缺省用 `username`，都缺省顯示「未知使用者」）、動作、時間，空清單與載入失敗各自顯示對應文字。
- `kanban-frontend/src/main.tsx`：`registerItemComponent('activity-log', ActivityLogItem)`，掛上 T-13 建立的一般化 item 掛載點。

進入路徑沿用 T-13 已實作的一般化機制（畫布上「＋ 加入元件」對話框輸入 `item.component` 識別碼），未新增專屬入口按鈕——`ui-canvas-layout.md`／`spec-canvas-layout.md` 目前也沒有為個別元件定義專屬入口的機制，這與 T-15 的 OQ-45（機制待 T-13 實作時一併定案）指向同一個既有結論。

`item.component` 的字串值（"activity-log"）、畫面版面（清單樣式、時間格式、空狀態／錯誤文字）皆為本任務推論決定，`ui-user-membership.md` 沒有逐字定案的內容可依循，已開 OQ-T-16-fe-activity-log-01（不阻塞）說明並徵詢是否要回頭補齊 ui 文件。

### 涵蓋範圍

- Entity／UseCase：`uc-view-board-activity-log`（唯一對應的 uc）
- Scenario：「活動紀錄依時間新到舊排序，並顯示操作人與動作」— 前端呈現面已涵蓋（排序、操作人、動作內容皆顯示）；後端行為與 Cucumber 驗收由 T-04 負責，本任務不重複測。
- 未涵蓋／延後：`ui-user-membership.md` `s-activity-log` 八段內容本身的正式定案（見 OQ-T-16-fe-activity-log-01）。

### Check

- `pnpm install`（首次於本 worktree 安裝 `kanban-frontend` 依賴）
- `pnpm exec vitest run`：7 個測試檔、47 個測試全過（含新增的 `ActivityLogItem.test.tsx` 4 個測試）
- `pnpm run build`（`tsc -b && vite build`）：成功
- `pnpm run lint`（oxlint）：僅既有 `itemComponentRegistry.tsx` 的 fast-refresh 警告（與本次改動無關），無新增警告
