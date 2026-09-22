# T-14-fe-board-item 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：s-board 等 9 個畫面實作

### 實作範圍與涵蓋的 uc／Scenario

依 `.state/tasks.md` 第 42 行產出範圍，本輪完成 9 個 Screen 的實作：

- `s-board`：Swimlane × Stage 交會格渲染卡片；拖曳卡片跨 Swimlane（`uc-move-card-swimlane`）、跨 Stage（`uc-move-card-stage`，同格拖曳同時變更兩者時依序呼叫兩支 API）；新增卡片入口（開 `s-card-add-dialog`）；點卡片開 `s-card-detail`；刪除卡片入口（開 `s-card-delete-dialog`）。**不含**「拖曳看板成員頭像到卡片追加負責人」（`uc-assign-card-owner-by-drag`），見 OQ-T-14-fe-board-item-01（看板成員 item 尚未存在，F07 補充中）。
- `s-swimlane-list`／`s-swimlane-delete-dialog`：新增／重新命名／拖曳排序／刪除（`uc-add-swimlane`／`uc-rename-swimlane`／`uc-reorder-swimlane`／`uc-delete-swimlane`），僅剩 1 個時刪除停用。
- `s-stage-list`／`s-stage-delete-dialog`：新增（含插入位置）／重新命名／拖曳排序／設角色／刪除（`uc-add-stage`／`uc-rename-stage`／`uc-reorder-stage`／`uc-set-stage-role`／`uc-delete-stage`），刪除時有卡片需先選目的 Stage。
- `s-card-add-dialog`：`uc-add-card`，標題非空驗證交由後端回覆訊息、輸入保留（對話框本來就不會因失敗被關閉）。
- `s-card-detail`：`uc-edit-card`（描述／截止日期／標籤）、`uc-add-comment`、負責人顯示（`uc-view-card-assignees`）與開啟 `s-card-assignee-picker` 的純前端導覽。
- `s-card-delete-dialog`：`uc-delete-card`。
- `s-card-assignee-picker`（F02）：`uc-list-card-assignee-candidates`／`uc-set-card-assignees`。

### 規格沒講清楚、本輪自行決定的地方（低風險技術決定）

1. **`s-swimlane-list`／`s-stage-list` 的進入點**：`ui-kanban-basic.md` 待確認事項與 `ui-authoring-open-questions.md` OQ-17／OQ-18 都載明「選中看板 item 後的屬性／操作面板」機制本身尚未定案（`CanvasPanel.dc.html`／`PanelStage.dc.html` 設計稿上也明白標示「⚠️ 規格未定義：面板容器與頁籤」），且待 F07 整合 CR 才會定案（D-09）。F07（T-13）目前只做了通用工具列（置頂／置底／鎖定／固定於畫面／移除），沒有針對特定 item 類型的側邊面板掛載點。本輪改用看板 item 標頭的「管理 Swimlane」「管理 Stage」按鈕直接開對話框，行為（新增／命名／排序／刪除／設角色）完全依 spec／ui 實作，只是容器換成置中對話框而非設計稿的右側面板；等 F07 補上選取面板機制後再整合，不影響現有行為。
2. **Owner-only 的結構調整改由後端拒絕，不在前端隱藏按鈕**：`ui-kanban-basic.md` s-swimlane-list／s-stage-list「無權限」寫「非 `r-board-owner` 嘗試調整結構時，依 `uc-reject-structure-change-by-member` post 顯示訊息，操作不生效」，字面是「嘗試後被拒」而非「看不到入口」，所以「管理 Swimlane／Stage」按鈕對所有非 Viewer 的看板成員顯示，Owner 以外的 Member 點擊寫入操作時由後端回傳 403／訊息，前端原樣顯示。
3. **`s-board` 的 canEdit 判斷沿用 `BoardCanvasPage` 既有的 canvas 角色換算**（Owner／Member 可編輯、Viewer 唯讀），透過新增的 `board/BoardContext.tsx` 用 React Context 從 `BoardCanvasPage` 傳給掛載在 `item.component === 'board'` 的內容，因為 `itemComponentRegistry.ItemContentProps` 只有 `itemId`／`component`／`width`／`height`，沒有 `boardId`。改動 `BoardCanvasPage.tsx` 只新增一個 Provider 包裝與一個 side-effect import（讓 `BoardItemContent` 掛上 `registerItemComponent('board', ...)`），不動它既有邏輯。
4. **跨 Swimlane＋跨 Stage 同時發生的拖曳**：交會格拖放若目的地同時換了 `swimlaneId` 與 `stageId`，依序呼叫 `moveCardSwimlane` 再 `moveCardStage`（spec 把兩者定義成獨立 uc，沒有「同時變更」的合併版本）。
5. **`GET /api/boards/{boardId}/cards`（新端點）**：`s-board` 需要看板全部卡片才能畫出交會格，spec 沒有定義對應的獨立 uc（跟 F02 的 `uc-list-cards-by-assignee` 不同，那個是依負責人過濾）。比照 T-12 新增 `card-count` 端點的先例（見 `boardApi.ts` 註解），重用既有 `CardApplicationService`／`CardRepository.findActiveByBoardId`，只加一個 application 方法＋一個 controller 端點，兩者都有對應單元測試（`CardApplicationServiceTest`）。

### 已知但判斷不需要開 OQ 的既有 OQ

`OQ-T-12-fe-board-list-02`（`uc-reject-board-access-by-nonmember` 由哪個任務接手）：確認 `BoardCanvasPage.tsx`（T-13）既有的 `getBoard` 失敗處理（`catch` 後顯示 `error` 訊息、停留在 `canvas-page`，不導頁）已經符合 spec「顯示訊息，停留本畫面」的字面要求——非成員直接開啟 `/boards/{boardId}` 時，`GET /api/boards/{boardId}` 會被後端拒絕，訊息會顯示在原地。本任務未新增動作，僅在此記錄供人工核對是否可視為已解決。

### Check（實際跑過的建置／測試）

- `./gradlew build --no-daemon`：BUILD SUCCESSFUL（含 `kanban-core`／`kanban-spring` 全部單元測試與 Cucumber）。
- `kanban-frontend`：`npx tsc -b`（0 錯誤）、`npx oxlint`（僅既有風格的 2 則 warning，其中 1 則是 T-13 遺留、1 則是本任務新檔案的同類 warning，不影響功能）、`npx vitest run`：7 個測試檔、67 個測試全過（含新增的 `src/board/BoardItemContent.test.tsx` 24 個測試，涵蓋本任務 9 個畫面的主要驗收條件；`BoardCanvasPage.test.tsx` 補上 T-14 新增內容需要的預設 mock 後仍全數通過）。
