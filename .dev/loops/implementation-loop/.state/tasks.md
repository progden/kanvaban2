# tasks（任務清單）

> 唯一任務來源。安排階段（`planning-prompt.md`）產生／校正；開發／審查階段只能改狀態欄、追加 `D-xx`，不可改任務描述／依賴／驗收條件（那要回安排階段或人工調整）。

## 規則

- **狀態值**：`todo`（依賴未必滿足）／`doing`（有 worktree 正在跑 Dev 或 Review 輪）／`review-pending`（Dev 交出、等 Review）／`blocked`（缺依據或反覆卡住，等人工）／`done`（Review 核准且已合併回整合分支）。
- **挑選順序**：驅動腳本每輪巡視，選出「依賴任務全部 `done`（且已合併）」且自身狀態為 `todo` 的任務，依下表 `T-xx` 序號由小到大挑，最多同時維持 5 條 `doing` 管線（`MAX_PARALLEL`，見 `run-loop.sh`）。
- **`D-xx`**：Review 退回時在該任務下方追加，狀態獨立（`todo`／`done`），母任務要等所有 `D-xx` 都 `done` 且 Review 再次核准才能轉 `done`；`D-xx` 不佔用新的並行名額，由同一條管線的 Dev 輪處理。
- **`blocked` 解除**：只有人工，或安排階段重跑確認缺的依據已補齊（例如某模組 spec 遷移完成）後，才能把 `blocked` 改回 `todo`。
- **顆粒度**：一列 = 一個 Aggregate Root（後端）或一個畫面群組（前端），不可再拆更細的子任務列（子步驟寫在 Dev 的決策紀錄裡）。

## 任務列

| ID | 產出範圍 | 依賴（需已合併） | 狀態 | 備註 |
|---|---|---|---|---|
| T-00-scaffold | 建立 `kanban-core`（Gradle）、`kanban-spring`（Gradle，依賴 core）、`kanban-frontend`（pnpm）三個專案骨架；CI 可跑 build/test，無業務邏輯 | 無 | todo | 其餘任務皆依賴本任務 |
| T-01-be-user | F02 `user` Aggregate Root（domain + port + application + web + persistence，不含 `board-membership`） | T-00-scaffold | todo | |
| T-02-be-board | F01 `board` Aggregate（`board`＋`swimlane`＋`stage`，含 `stage.role` START/DONE 唯一性） | T-01-be-user（`board.created-by`） | todo | |
| T-03-be-card | F01 `card` Aggregate（`card`＋`comment`） | T-02-be-board、T-01-be-user（`card.assignees`／`comment.author`） | todo | |
| T-04-be-board-membership | F02 `board-membership` ＋ ActivityRecord | T-02-be-board、T-01-be-user | todo | |
| T-05-be-board-clock | F04 Board Clock（`board.clock-time`／`clock-status`，改寫 board/card 事件的 `occurredAt` 來源，含單調性） | T-02-be-board、T-03-be-card | todo | |
| T-06-be-kanban-widgets | F03 唯讀 projection（Lead/Cycle Time、WIP、Aging、Throughput/CFD、到期提醒） | T-02-be-board、T-03-be-card、T-05-be-board-clock | blocked | spec 名詞定義三張表尚未遷移，見 OQ-IMPL-01 |
| T-07-be-workload | F05 唯讀 projection（Active Card／Workload／未指派統計） | T-03-be-card、T-04-be-board-membership | blocked | spec 名詞定義三張表尚未遷移，見 OQ-IMPL-02 |
| T-08-be-feature-cr-board | F06 唯讀 projection（Feature／CR 卡標籤解讀、orphan CR 判定） | T-03-be-card | blocked | spec 名詞定義三張表尚未遷移，見 OQ-IMPL-03 |
| T-09-be-canvas-layout | F07 `canvas`＋`item`＋`viewport`（不含「待釐清」的跨模組整合部分） | T-02-be-board、T-04-be-board-membership（`viewport.user`） | todo | |
| T-10-fe-shell | 前端 app shell（路由、API client、登入態管理） | T-00-scaffold、T-01-be-user | todo | |
| T-11-fe-auth | `Login`、`Signup` | T-10-fe-shell | todo | |
| T-12-fe-board-list | `BoardList`、`BoardCreateDialog`、`BoardDeleteDialog`、`StateBoardList*` | T-10-fe-shell、T-02-be-board、T-04-be-board-membership | todo | |
| T-13-fe-board-detail | `Main`、`PanelStage`、`CardAddDialog`、`CardDeleteDialog`、`CardDetail`、`StageDeleteDialog`、`SwimlaneDeleteDialog`、`AssigneePicker`、`StateBoardLoading`、`StateCardDetailLoading` | T-12-fe-board-list、T-03-be-card | todo | |
| T-14-fe-member-management | `MemberManagement` | T-13-fe-board-detail、T-04-be-board-membership | todo | |
| T-15-fe-widgets | F03 對應畫面 | T-13-fe-board-detail、T-06-be-kanban-widgets | blocked | 無 `ui-kanban-widgets.md`、設計稿無對應畫面，見 OQ-IMPL-04 |
| T-16-fe-workload | F05 對應畫面 | T-12-fe-board-list、T-07-be-workload | blocked | 無 `ui-workload.md`、設計稿無對應畫面，見 OQ-IMPL-05 |
| T-17-fe-feature-cr-board | F06 對應畫面 | T-13-fe-board-detail、T-08-be-feature-cr-board | blocked | 無 `ui-feature-cr-board.md`、設計稿無對應畫面，見 OQ-IMPL-06 |
| T-18-fe-canvas | `CanvasPanel` | T-13-fe-board-detail、T-09-be-canvas-layout | todo | 範圍限 Item/Viewport CRUD；與 F03 圖表元件整合部分 spec 本身未定義，不在範圍內 |

（此表由 `planning-prompt.md` 首次執行時的種子資料建立，任何校正請依該提示詞的流程追加決策紀錄說明依據。）
