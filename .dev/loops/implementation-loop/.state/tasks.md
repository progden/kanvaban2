# tasks（任務清單）

> 唯一任務來源。安排階段（`planning-prompt.md`）產生／校正；開發／審查階段只能改狀態欄、追加 `D-xx`，不可改任務描述／依賴／驗收條件（那要回安排階段或人工調整）。
>
> **2026-09-18 全面校正**：原始種子資料誤判了兩件事——(1) 把 F03／F05／F06「名詞定義」表格是空的當成「spec 尚未遷移」，實際上 `spec-migration-loop` 已於本輪之前完成（`spec-migration-state.md`：「F01～F06 全部完成，全檔 0 error」），這三個模組表格空白是正確的定案狀態（它們本來就不新增 aggregate，只引用 F01／F02／F04 既有實體）；(2) 誤以為只有 F01 有 `ui-*.md`，實際上 F02～F07 皆已由 `ui-authoring-loop` 補齊。更關鍵的是 `ui-authoring-loop` 的 OQ-49（已由人工確認定案）把前端架構定為 **Canvas-centric**：F01 `s-board`、F03 四個儀表板、F04 時鐘控制、F05 工作量儀表板、F06 追蹤表全部改為「F07 `s-canvas` 上的獨立 item」，不再是各自獨立導覽的頁面。下表依此全面重排，校正依據見 `decision-log.md` 對應條目。

## 規則

- **狀態值**：`todo`（依賴未必滿足）／`doing`（有 worktree 正在跑 Dev 或 Review 輪）／`review-pending`（Dev 交出、等 Review）／`blocked`（缺依據或反覆卡住，等人工）／`done`（Review 核准且已合併回整合分支）。
- **挑選順序**：驅動腳本每輪巡視，選出「依賴任務全部 `done`（且已合併）」且自身狀態為 `todo` 的任務，依下表 `T-xx` 序號由小到大挑，最多同時維持 5 條 `doing` 管線（`MAX_PARALLEL`，見 `run-loop.sh`）。
- **`D-xx`**：Review 退回時在該任務下方追加，狀態獨立（`todo`／`done`），母任務要等所有 `D-xx` 都 `done` 且 Review 再次核准才能轉 `done`；`D-xx` 不佔用新的並行名額，由同一條管線的 Dev 輪處理。
- **`blocked` 解除**：只有人工，或安排階段重跑確認缺的依據已補齊後，才能把 `blocked` 改回 `todo`。
- **顆粒度**：一列 = 一個 Aggregate Root（後端）或一個畫面群組（前端），不可再拆更細的子任務列（子步驟寫在 Dev 的決策紀錄裡）。

## 後端任務

| ID | 產出範圍 | 依賴（需已合併） | 狀態 | 備註 |
|---|---|---|---|---|
| T-00-scaffold | 建立 `kanban-core`（Gradle）、`kanban-spring`（Gradle，依賴 core）、`kanban-frontend`（pnpm）三個專案骨架；CI 可跑 build/test，無業務邏輯 | 無 | done | 其餘任務皆依賴本任務；2026-09-18 Review 核准，見 `review.md` |
| T-01-be-user | F02 `user` Aggregate Root（domain + port + application + web + persistence，不含 `board-membership`） | T-00-scaffold | done | 2026-09-18 Review 第 2 輪**附保留核准**（見 `review.md`）：OQ-IMPL-09（HTTP 狀態碼）、OQ-IMPL-10（`user.username` 非空）仍待處理，兩者定案後可能要回頭改 `UserController`／`User.create` 與測試 |
| T-02-be-board | F01 `board` Aggregate（`board`＋`swimlane`＋`stage`，含 `stage.role` START/DONE 唯一性） | T-01-be-user（`board.created-by`） | todo | |
| T-03-be-card | F01 `card` Aggregate（`card`＋`comment`） | T-02-be-board、T-01-be-user（`card.assignees`／`comment.author`） | todo | |
| T-04-be-board-membership | F02 `board-membership` ＋ ActivityRecord | T-02-be-board、T-01-be-user | todo | |
| T-05-be-board-clock | F04 Board Clock（`board.clock-time`／`clock-status`，改寫 board/card 事件的 `occurredAt` 來源，含單調性；含 `uc-guard-clock-monotonicity`，見 spec `D-07` 的建模說明） | T-02-be-board、T-03-be-card | todo | |
| T-06-be-kanban-widgets | F03 唯讀 projection（Lead/Cycle Time、WIP、Aging、Throughput/CFD、到期提醒；六個 `uc-view-*` 已定案，角色皆 `r-board-member`） | T-02-be-board、T-03-be-card、T-05-be-board-clock | todo | 原標 blocked 已解除，見 OQ-IMPL-01「解除說明」 |
| T-07-be-workload | F05 唯讀 projection（Active Card／Workload／未指派統計；`uc-view-workload` 已定案） | T-03-be-card、T-04-be-board-membership | todo | 原標 blocked 已解除，見 OQ-IMPL-02「解除說明」 |
| T-08-be-feature-cr-board | F06 唯讀 projection（Feature／CR 卡標籤解讀、orphan CR 判定） | T-03-be-card | todo | 原標 blocked 已解除，見 OQ-IMPL-03「解除說明」 |
| T-09-be-canvas-layout | F07 `canvas`＋`item`＋`viewport`（`uc-init-canvas`「看板畫布初始化」＋十個既有 item/viewport CRUD uc，共 11 個 uc 皆已在 spec 定義） | T-02-be-board、T-04-be-board-membership（`viewport.user`） | todo | 2026-09-18 隨 OQ-IMPL-07／08 定案：canvas 於使用者第一次開啟 Board 時自動建立並放置看板本體 `item`（`item.component`＝`board`）；`r-canvas-editor`＝`board-membership.role` Owner／Member，`r-canvas-viewer`＝Viewer（`r-board-viewer`），角色檢查依此對應實作 |

## 前端任務（Canvas-centric，2026-09-18 依 OQ-49 全面重排）

| ID | 產出範圍 | 依賴（需已合併） | 狀態 | 備註 |
|---|---|---|---|---|
| T-10-fe-shell | 前端 app shell（路由、API client、登入態管理） | T-00-scaffold、T-01-be-user | doing | 2026-09-18 Review 第 1 輪退回（見 `review.md`），待 Dev 處理 D-03、D-04；建置／測試本身全綠，程式碼行為未發現錯誤 |
| T-11-fe-auth | `s-login`、`s-signup` | T-10-fe-shell | todo | |
| T-12-fe-board-list | `s-board-list`、`s-board-create-dialog`、`s-board-delete-dialog` | T-10-fe-shell、T-02-be-board、T-04-be-board-membership | todo | 選定 Board 後導向 T-13 的 Canvas，不是導向 T-14 |
| T-13-fe-canvas-shell | `s-canvas`：F07 item 放置容器（開啟看板時初始化＋移動／調整大小／排層序／錨定 canvas／screen／Viewport 平移縮放記憶／批次操作），提供給其他前端任務掛載自己的 item 內容；`.dev/F07-canvas-layout/ui-canvas-layout.md` 已存在且操作表／資料表完整，直接依它實作 | T-12-fe-board-list、T-09-be-canvas-layout | todo | 基礎設施型任務，T-14～T-20 都依賴它才能把畫面掛上 Canvas；2026-09-18 隨 OQ-IMPL-07／08 定案（見 T-09 備註），`item.component`／canvas 建立時機／角色對應三件事都已寫進 `spec-canvas-layout.md`（草稿，不需 CR）；`ui-canvas-layout.md` 本身仍是「討論中」狀態（待確認事項可能還有殘留，Dev 動工前重讀一次確認），但不再是本任務動工的阻礙 |
| T-14-fe-board-item | F01 內容作為 Canvas item：`s-board`、`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list`、`s-stage-delete-dialog`、`s-card-add-dialog`、`s-card-detail`、`s-card-delete-dialog`、`s-card-assignee-picker`（F02，負責人選取，從 `s-card-detail` 進入） | T-13-fe-canvas-shell、T-03-be-card | todo | |
| T-15-fe-member-management | `s-member-management`（從 Canvas 上「看板成員」item 進入，機制待 T-13 實作時一併定案，見 OQ-45） | T-13-fe-canvas-shell、T-04-be-board-membership | todo | |
| T-16-fe-activity-log | `s-activity-log`（合併 `board` 與 `board-membership` 的活動紀錄） | T-13-fe-canvas-shell、T-04-be-board-membership | todo | 原任務清單遺漏，2026-09-18 校正時補上 |
| T-17-fe-clock-control | `s-board-clock-control`（F04，Canvas item） | T-13-fe-canvas-shell、T-05-be-board-clock | todo | 原任務清單遺漏，2026-09-18 校正時補上 |
| T-18-fe-widgets | F03 四個儀表板 Canvas item：`s-cycle-lead-time-dashboard`、`s-wip-dashboard`、`s-throughput-cfd-dashboard`、`s-duedate-reminder` | T-13-fe-canvas-shell、T-06-be-kanban-widgets | todo | 原標 blocked（無 ui 檔）已解除，見 OQ-IMPL-04「解除說明」；依賴改為 T-13（原本誤依賴 T-13-fe-board-detail） |
| T-19-fe-workload | `s-workload-dashboard`（Canvas item）＋ `s-cards-by-assignee`（從前者點擊進入） | T-13-fe-canvas-shell、T-07-be-workload | todo | 原標 blocked 已解除，見 OQ-IMPL-05「解除說明」；依賴改為 T-13 |
| T-20-fe-feature-cr-board | `s-feature-cr-board`（Canvas item） | T-13-fe-canvas-shell、T-08-be-feature-cr-board | todo | 原標 blocked 已解除，見 OQ-IMPL-06「解除說明」；依賴改為 T-13 |

（原 `T-18-fe-canvas` 已併入 `T-13-fe-canvas-shell`；原任務清單把 Canvas 排在 F01 畫面之後，方向反了——實際上幾乎所有畫面都要先有 Canvas 容器才能掛載，已於 2026-09-18 校正。）

## 修正任務（D-xx，Review 退回時追加）

### T-01-be-user（2026-09-18 Review 第 1 輪退回）

| ID | 母任務 | 狀態 | 描述 |
|---|---|---|---|
| D-01 | T-01-be-user | done | 已在 `open-questions.md` 新開 OQ-IMPL-09，逐字引用 `uc-create-user`／`uc-login`／`uc-logout` 的 `fail`／`post` 原文，列出目前程式碼採用的 HTTP 狀態碼對應（400／409／401／204／401）與其他選項；`state.md` 已補「待確認事項」。程式碼未變動。 |
| D-02 | T-01-be-user | done | 已在 `open-questions.md` 新開 OQ-IMPL-10（情況：兩處矛盾並列），逐字並列欄位表『非空、全系統不可重複』與 `uc-create-user` pre p2『`user.username` 在系統中不可重複』（沒有非空）、以及 `ui-user-membership.md` 資料表的對應標註，問「空 username 要怎麼拒絕、訊息是什麼」。未自行編訊息、未加防護，OQ 有結論前程式碼維持原狀，`state.md` 已補「待確認事項」。 |

### T-10-fe-shell（2026-09-18 Review 第 1 輪退回）

| ID | 母任務 | 狀態 | 描述 |
|---|---|---|---|
| D-03 | T-10-fe-shell | todo | `kanban-frontend/src/layout/AppShell.tsx` 的 TopBar 顯示 `user.username`，但 `ui-user-membership.md` 第 78 行寫『TopBar 顯示帳號名稱』、第 90 行寫『成功後 TopBar 顯示該使用者名稱』，兩處都沒有指定是 `user.username`（第 26 行資料表標為「帳號 ID」）還是 `user.display-name`（spec 第 29 行『顯示名字，看板上顯示用』）。另外 `GET /api/session` 的 `SessionResponse` 只回 `username`，要顯示 `display-name` 的話得改 T-01 的後端。請在 `open-questions.md` 新開 OQ（情況選【推論＋所本原文】，逐字引用上面三處原文，並列出「目前程式碼選 username」這個推論），交接摘要的「待確認事項」也要寫進去。OQ 定案前程式碼可以維持原狀，不要自行改成 display-name。 |
| D-04 | T-10-fe-shell | todo | 更正過期的紀錄：`tasks.md` 原本 T-10 備註、`decision-log.md`「2026-09-18 T-10-fe-shell（Dev）」影響段、`state.md` 都寫 OQ-IMPL-09『仍待處理／未定案』，但 OQ-IMPL-09 在本任務 Dev commit 之前（`06c0a14`）就已經解除，並升級成 `adr.md` ADR-001。請在 decision-log 追加一則更正（既有條目不改），寫明 `ApiError` 帶狀態碼的設計跟 ADR-001 的分類表（例如未登入查 session 回 401）一致，並說明後續 T-11 等任務應該依 ADR-001 狀態碼分流，還是依回應訊息分流。 |

