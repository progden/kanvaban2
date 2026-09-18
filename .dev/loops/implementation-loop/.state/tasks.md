# tasks（任務清單）

> 唯一任務來源。安排階段（`planning-prompt.md`）產生／校正；**開發／審查階段不可以改這個檔**（任務描述／依賴／驗收條件要回安排階段或人工調整）；任務狀態、`D-xx`、各種紀錄都在 `tasks/<task-id>/` 底下，見下方規則。
>
> **2026-09-18 全面校正**：原始種子資料誤判了兩件事——(1) 把 F03／F05／F06「名詞定義」表格是空的當成「spec 尚未遷移」，實際上 `spec-migration-loop` 已於本輪之前完成（`spec-migration-state.md`：「F01～F06 全部完成，全檔 0 error」），這三個模組表格空白是正確的定案狀態（它們本來就不新增 aggregate，只引用 F01／F02／F04 既有實體）；(2) 誤以為只有 F01 有 `ui-*.md`，實際上 F02～F07 皆已由 `ui-authoring-loop` 補齊。更關鍵的是 `ui-authoring-loop` 的 OQ-49（已由人工確認定案）把前端架構定為 **Canvas-centric**：F01 `s-board`、F03 四個儀表板、F04 時鐘控制、F05 工作量儀表板、F06 追蹤表全部改為「F07 `s-canvas` 上的獨立 item」，不再是各自獨立導覽的頁面。下表依此全面重排，校正依據見 `decision-log.md` 對應條目。

## 規則

- **狀態不在這個檔**（2026-09-18 起）：每個任務的狀態是 `tasks/<task-id>/status` 這個單行檔（檔案不存在＝`todo`）。原因：並行 worktree 各改同一份 `tasks.md` 的相鄰列、各自往共用紀錄檔尾追加，合併回整合分支必衝突，流水號也會撞號；改成「一個 worktree 只寫自己任務目錄」後，合併不會衝突。總覽用 `scripts/collect.sh status` 看。
- **狀態值**：`todo`（依賴未必滿足）／`doing`（有 worktree 正在跑 Dev 或 Review 輪）／`review-pending`（Dev 交出、等 Review）／`blocked`（缺依據或反覆卡住，等人工）／`done`（Review 核准；驅動腳本隨即合併回整合分支）。
- **挑選順序**：驅動腳本巡視時，選出「依賴任務全部 `done`（且已合併）」且自身狀態為 `todo` 的任務，依下表 `T-xx` 序號由小到大挑，最多同時維持 5 條 `doing` 管線（`MAX_PARALLEL`，見 `run-loop.sh`）。
- **`D-xx`**：Review 退回時追加在 `tasks/<task-id>/fixes.md`，編號在該任務內遞增，狀態獨立（`todo`／`done`），母任務要等所有 `D-xx` 都 `done` 且 Review 再次核准才能轉 `done`；`D-xx` 不佔用新的並行名額，由同一條管線的 Dev 輪處理。
- **任務目錄內容**：`status`、`fixes.md`、`decision-log.md`（含交接摘要）、`review.md`、`open-questions.md`（ID 格式 `OQ-<task-id>-<兩位數>`）、`state.md`（每輪覆寫）。跨任務 ADR 一則一檔放 `adr/ADR-<task-id>-<兩位數>-<slug>.md`。遷移前的共用紀錄（`OQ-IMPL-01～17`、`ADR-001`、舊 `D-xx`）在 `archive/`，唯讀、ID 照舊有效。
- **`blocked` 解除**：只有人工，或安排階段重跑確認缺的依據已補齊後，才能把 `blocked` 改回 `todo`。
- **顆粒度**：一列 = 一個 Aggregate Root（後端）或一個畫面群組（前端），不可再拆更細的子任務列（子步驟寫在 Dev 的決策紀錄裡）。

## 後端任務

| ID | 產出範圍 | 依賴（需已合併） | 備註 |
|---|---|---|---|
| T-00-scaffold | 建立 `kanban-core`（Gradle）、`kanban-spring`（Gradle，依賴 core）、`kanban-frontend`（pnpm）三個專案骨架；CI 可跑 build/test，無業務邏輯 | 無 | 其餘任務皆依賴本任務；2026-09-18 Review 核准，見 `review.md` |
| T-01-be-user | F02 `user` Aggregate Root（domain + port + application + web + persistence，不含 `board-membership`） | T-00-scaffold | 2026-09-18 Review 第 2 輪**附保留核准**（見 `review.md`）：OQ-IMPL-09（HTTP 狀態碼）、OQ-IMPL-10（`user.username` 非空）仍待處理，兩者定案後可能要回頭改 `UserController`／`User.create` 與測試 |
| T-02-be-board | F01 `board` Aggregate（`board`＋`swimlane`＋`stage`，含 `stage.role` START/DONE 唯一性） | T-01-be-user（`board.created-by`） | 2026-09-18 Dev 第 1 輪交出：`kanban-core`（`Board`／`Swimlane`／`Stage`／`ActivityRecord`／`CardLookupPort`）＋ `kanban-spring`（persistence／application／web）＋ 2 個 feature 檔（swimlane/stage 管理，共 15 個 Scenario）全過；`Board.create` 預設 Swimlane/Stage 名稱依 OQ-IMPL-14 推論；`r-board-owner` 權限檢查未加（依賴 T-04，見 OQ-IMPL-15）；`uc-create-board` 只實作 `board` 部分，不含 `board-membership`（見 OQ-IMPL-16）。2026-09-18 Dev 第 2 輪處理 D-05～D-08：新開 OQ-IMPL-17（刪除 Swimlane／Stage 連帶卡片的協調流程，見下方修正任務列）；`BoardSteps`／`BoardTest` 補齊活動紀錄與替身警告；OQ-IMPL-14／13 引文訂正。正式程式碼（`Board`／`BoardController`／`BoardApplicationService`）本輪未變動，`./gradlew clean build --no-daemon` 全綠（`kanban-core` 28 測試、`kanban-spring` 4 個 feature 檔全過）。2026-09-18 Review 第 2 輪附保留核准（保留事項 R1～R4 見 `review.md`：OQ-IMPL-14～15 未決，其中 OQ-IMPL-17 的卡片連帶刪除／轉移尚無任務接手）。 |
| T-03-be-card | F01 `card` Aggregate（`card`＋`comment`） | T-02-be-board、T-01-be-user（`card.assignees`／`comment.author`） | |
| T-04-be-board-membership | F02 `board-membership` ＋ ActivityRecord | T-02-be-board、T-01-be-user | |
| T-05-be-board-clock | F04 Board Clock（`board.clock-time`／`clock-status`，改寫 board/card 事件的 `occurredAt` 來源，含單調性；含 `uc-guard-clock-monotonicity`，見 spec `D-07` 的建模說明） | T-02-be-board、T-03-be-card | |
| T-06-be-kanban-widgets | F03 唯讀 projection（Lead/Cycle Time、WIP、Aging、Throughput/CFD、到期提醒；六個 `uc-view-*` 已定案，角色皆 `r-board-member`） | T-02-be-board、T-03-be-card、T-05-be-board-clock | 原標 blocked 已解除，見 OQ-IMPL-01「解除說明」 |
| T-07-be-workload | F05 唯讀 projection（Active Card／Workload／未指派統計；`uc-view-workload` 已定案） | T-03-be-card、T-04-be-board-membership | 原標 blocked 已解除，見 OQ-IMPL-02「解除說明」 |
| T-08-be-feature-cr-board | F06 唯讀 projection（Feature／CR 卡標籤解讀、orphan CR 判定） | T-03-be-card | 原標 blocked 已解除，見 OQ-IMPL-03「解除說明」 |
| T-09-be-canvas-layout | F07 `canvas`＋`item`＋`viewport`（`uc-init-canvas`「看板畫布初始化」＋十個既有 item/viewport CRUD uc，共 11 個 uc 皆已在 spec 定義） | T-02-be-board、T-04-be-board-membership（`viewport.user`） | 2026-09-18 隨 OQ-IMPL-07／08 定案：canvas 於使用者第一次開啟 Board 時自動建立並放置看板本體 `item`（`item.component`＝`board`）；`r-canvas-editor`＝`board-membership.role` Owner／Member，`r-canvas-viewer`＝Viewer（`r-board-viewer`），角色檢查依此對應實作 |

## 前端任務（Canvas-centric，2026-09-18 依 OQ-49 全面重排）

| ID | 產出範圍 | 依賴（需已合併） | 備註 |
|---|---|---|---|
| T-10-fe-shell | 前端 app shell（路由、API client、登入態管理） | T-00-scaffold、T-01-be-user | 2026-09-18 Review 第 2 輪**附保留核准**（見 `review.md`）：D-03、D-04 已處理；OQ-IMPL-11（TopBar 顯示 `user.username` 還是 `user.display-name`）仍待處理，定案為 `display-name` 時要回頭改 `AppShell.tsx` 與 T-01 的 `SessionResponse`。 |
| T-11-fe-auth | `s-login`、`s-signup` | T-10-fe-shell | 2026-09-18 Review 第 2 輪**附保留核准**（見 `review.md`）：D-05～D-07 已處理；OQ-IMPL-12（設計稿無法存取，版面待對照 `Login.dc.html`／`Signup.dc.html`）、OQ-IMPL-13（`ui-user-membership.md` 第 45 行「不觸發 `uc-create-user`」與 spec `pre p2`／`fail p2`／Scenario 矛盾）仍待處理；OQ-IMPL-13 若定案為 A（前端查重），要另開 CR 新增查重 usecase，並回頭改 `SignupPage.tsx` 與測試 |
| T-12-fe-board-list | `s-board-list`、`s-board-create-dialog`、`s-board-delete-dialog` | T-10-fe-shell、T-02-be-board、T-04-be-board-membership | 選定 Board 後導向 T-13 的 Canvas，不是導向 T-14 |
| T-13-fe-canvas-shell | `s-canvas`：F07 item 放置容器（開啟看板時初始化＋移動／調整大小／排層序／錨定 canvas／screen／Viewport 平移縮放記憶／批次操作），提供給其他前端任務掛載自己的 item 內容；`.dev/F07-canvas-layout/ui-canvas-layout.md` 已存在且操作表／資料表完整，直接依它實作 | T-12-fe-board-list、T-09-be-canvas-layout | 基礎設施型任務，T-14～T-20 都依賴它才能把畫面掛上 Canvas；2026-09-18 隨 OQ-IMPL-07／08 定案（見 T-09 備註），`item.component`／canvas 建立時機／角色對應三件事都已寫進 `spec-canvas-layout.md`（草稿，不需 CR）；`ui-canvas-layout.md` 本身仍是「討論中」狀態（待確認事項可能還有殘留，Dev 動工前重讀一次確認），但不再是本任務動工的阻礙 |
| T-14-fe-board-item | F01 內容作為 Canvas item：`s-board`、`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list`、`s-stage-delete-dialog`、`s-card-add-dialog`、`s-card-detail`、`s-card-delete-dialog`、`s-card-assignee-picker`（F02，負責人選取，從 `s-card-detail` 進入） | T-13-fe-canvas-shell、T-03-be-card | |
| T-15-fe-member-management | `s-member-management`（從 Canvas 上「看板成員」item 進入，機制待 T-13 實作時一併定案，見 OQ-45） | T-13-fe-canvas-shell、T-04-be-board-membership | |
| T-16-fe-activity-log | `s-activity-log`（合併 `board` 與 `board-membership` 的活動紀錄） | T-13-fe-canvas-shell、T-04-be-board-membership | 原任務清單遺漏，2026-09-18 校正時補上 |
| T-17-fe-clock-control | `s-board-clock-control`（F04，Canvas item） | T-13-fe-canvas-shell、T-05-be-board-clock | 原任務清單遺漏，2026-09-18 校正時補上 |
| T-18-fe-widgets | F03 四個儀表板 Canvas item：`s-cycle-lead-time-dashboard`、`s-wip-dashboard`、`s-throughput-cfd-dashboard`、`s-duedate-reminder` | T-13-fe-canvas-shell、T-06-be-kanban-widgets | 原標 blocked（無 ui 檔）已解除，見 OQ-IMPL-04「解除說明」；依賴改為 T-13（原本誤依賴 T-13-fe-board-detail） |
| T-19-fe-workload | `s-workload-dashboard`（Canvas item）＋ `s-cards-by-assignee`（從前者點擊進入） | T-13-fe-canvas-shell、T-07-be-workload | 原標 blocked 已解除，見 OQ-IMPL-05「解除說明」；依賴改為 T-13 |
| T-20-fe-feature-cr-board | `s-feature-cr-board`（Canvas item） | T-13-fe-canvas-shell、T-08-be-feature-cr-board | 原標 blocked 已解除，見 OQ-IMPL-06「解除說明」；依賴改為 T-13 |

（原 `T-18-fe-canvas` 已併入 `T-13-fe-canvas-shell`；原任務清單把 Canvas 排在 F01 畫面之後，方向反了——實際上幾乎所有畫面都要先有 Canvas 容器才能掛載，已於 2026-09-18 校正。）

## CR 追加任務（規格定稿後的變更，改動既有已合併的任務產出）

| ID | 產出範圍 | 依賴（需已合併） | 備註 |
|---|---|---|---|
| T-21-cr007-topbar-display-name | CR-007：`uc-login` 登入後 TopBar 顯示 `user.display-name`。後端：`GET /api/session`（「SessionResponse」）與登入回應補上顯示名字，新增 Scenario「登入後 TopBar 顯示的是顯示名字而不是帳號 ID」的 Cucumber step；前端：`kanban-frontend` 登入態與「AppShell」TopBar 改顯示顯示名字，依 `ui-user-membership.md` `s-login` 驗收條件補測試。允許改動 T-01-be-user、T-10-fe-shell、T-11-fe-auth 已合併的檔案，但只限這個 CR 需要的部分 | T-01-be-user、T-10-fe-shell、T-11-fe-auth | 來源：`archive/open-questions.md` OQ-IMPL-11（2026-09-18 人工決議採選項 B）。完成並驗收後，人工把 `.dev/CR.md` CR-007 改「處理完成」並清掉 spec 的 `@added @wip` |

