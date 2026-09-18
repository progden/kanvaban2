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
| T-02-be-board | F01 `board` Aggregate（`board`＋`swimlane`＋`stage`，含 `stage.role` START/DONE 唯一性） | T-01-be-user（`board.created-by`） | blocked | 2026-09-18 Dev 第 1 輪交出：`kanban-core`（`Board`／`Swimlane`／`Stage`／`ActivityRecord`／`CardLookupPort`）＋ `kanban-spring`（persistence／application／web）＋ 2 個 feature 檔（swimlane/stage 管理，共 15 個 Scenario）全過；`Board.create` 預設 Swimlane/Stage 名稱依 OQ-IMPL-14 推論；`r-board-owner` 權限檢查未加（依賴 T-04，見 OQ-IMPL-15）；`uc-create-board` 只實作 `board` 部分，不含 `board-membership`（見 OQ-IMPL-16）。2026-09-18 Dev 第 2 輪處理 D-05～D-08：新開 OQ-IMPL-17（刪除 Swimlane／Stage 連帶卡片的協調流程，見下方修正任務列）；`BoardSteps`／`BoardTest` 補齊活動紀錄與替身警告；OQ-IMPL-14／13 引文訂正。正式程式碼（`Board`／`BoardController`／`BoardApplicationService`）本輪未變動，`./gradlew clean build --no-daemon` 全綠（`kanban-core` 28 測試、`kanban-spring` 4 個 feature 檔全過）。2026-09-18 Review 第 2 輪附保留核准（保留事項 R1～R4 見 `review.md`：OQ-IMPL-14～15 未決，其中 OQ-IMPL-17 的卡片連帶刪除／轉移尚無任務接手）。 |
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
| T-10-fe-shell | 前端 app shell（路由、API client、登入態管理） | T-00-scaffold、T-01-be-user | done | 2026-09-18 Review 第 2 輪**附保留核准**（見 `review.md`）：D-03、D-04 已處理；OQ-IMPL-11（TopBar 顯示 `user.username` 還是 `user.display-name`）仍待處理，定案為 `display-name` 時要回頭改 `AppShell.tsx` 與 T-01 的 `SessionResponse`。 |
| T-11-fe-auth | `s-login`、`s-signup` | T-10-fe-shell | done | 2026-09-18 Review 第 2 輪**附保留核准**（見 `review.md`）：D-05～D-07 已處理；OQ-IMPL-12（設計稿無法存取，版面待對照 `Login.dc.html`／`Signup.dc.html`）、OQ-IMPL-13（`ui-user-membership.md` 第 45 行「不觸發 `uc-create-user`」與 spec `pre p2`／`fail p2`／Scenario 矛盾）仍待處理；OQ-IMPL-13 若定案為 A（前端查重），要另開 CR 新增查重 usecase，並回頭改 `SignupPage.tsx` 與測試 |
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
| D-03 | T-10-fe-shell | done | 已在 `open-questions.md` 新開 OQ-IMPL-11（情況：推論＋所本原文），逐字引用 `ui-user-membership.md` 第 78、90 行操作表／驗收條件、第 26～27 行資料表，以及 `spec-user-membership.md` 第 28～29 行欄位表，列出「TopBar 目前顯示 `user.username`」這個推論與 A／B／C 三個選項。OQ 定案前 TopBar 維持顯示 `user.username`，未自行改成 `display-name`，`state.md` 已補「待確認事項」。程式碼未變動。 |
| D-04 | T-10-fe-shell | done | 已在 `decision-log.md` 追加一則更正條目（既有條目未改），指出 OQ-IMPL-09 已於 `06c0a14`（早於本任務 Dev commit `4614e17`）解除並升級為 `adr.md` ADR-001；並說明 `ApiError.status` 的設計跟 ADR-001 分類表一致（例如未登入查 `GET /api/session` 回 401），後續任務依 ADR-001 狀態碼分類做基本分流，精確文案仍依回應內容的錯誤代碼／訊息決定。`tasks.md` T-10 備註、`state.md` 的過期字樣已同步更正。程式碼未變動。 |

### T-11-fe-auth（2026-09-18 Review 第 1 輪退回）

| ID | 母任務 | 狀態 | 描述 |
|---|---|---|---|
| D-05 | T-11-fe-auth | done | 登記 OQ（高風險，`iteration-prompt.md` 第 5 節），不要只寫在 `decision-log.md`。`ui-user-membership.md` 第 45 行驗收條件寫『帳號 ID 與系統中既有帳號重複時確認建立帳號，輸入內容保留、顯示訊息，且不觸發 `uc-create-user`』，但 `SignupPage.tsx` 在帳號重複時仍會呼叫 `POST /api/users`（也就是觸發 `uc-create-user`，由後端依 fail p2 拒絕），`SignupPage.test.tsx` 的「帳號 ID 與系統中既有帳號重複」測試也沒有斷言「不觸發」。Dev 在 `decision-log.md` 自行判定這是「措辭疊加」、歸為低風險、不開 OQ，這是在詮釋驗收條件的字面意思，屬於第 5 節『spec 的 `pre`／`post`／Scenario 沒講清楚該怎麼實作』，要開 OQ。OQ 情況用「兩處矛盾並列」或「推論＋所本原文」，至少逐字引用：`ui-user-membership.md` 第 45 行（上面那句）、`.dev/conventions/ui-convention.md` 第 150 行『斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」』、`spec-user-membership.md` 第 97 行 pre p2 與第 104 行 fail p2、第 143～147 行 Scenario「帳號 ID（username）不可重複」。`state.md` 補「待確認事項」。OQ 定案前程式碼維持現狀（送 API），不要自行改成前端查重。 |
| D-06 | T-11-fe-auth | done | `LoginPage.test.tsx` 沒有完整覆蓋 `ui-user-membership.md` 第 91、92 行驗收條件。(a) 「帳號不存在時送出登入表單，顯示訊息 "帳號或密碼錯誤"」（對應 `uc-login` @fail-p1）只斷言訊息，沒有斷言『帳號 ID 與密碼欄位保留』『停留本畫面』；(b) 兩個失敗測試（fail-p1、fail-p2）都沒有斷言『TopBar 不顯示帳號名稱』。請補上這些斷言，例如 `queryByRole('button', { name: '登出' })` 為 null，或 TopBar 內沒有該 username，選法由 Dev 決定。 |
| D-07 | T-11-fe-auth | done | `SignupPage.test.tsx` 的「帳號 ID 為空時確認建立帳號」測試，沒有斷言 `ui-user-membership.md` 第 43 行的『畫面維持顯示』：送出後沒有確認「建立帳號」標題仍在，也沒有確認沒有導向 `s-login`。請補上這個斷言。 |
### T-02-be-board（2026-09-18 Review 第 1 輪退回）

| ID | 母任務 | 狀態 | 描述 |
|---|---|---|---|
| D-05 | T-02-be-board | done | `uc-delete-swimlane` post 第 2 條（spec 第 141 行『"若該 `swimlane` 內有 `card`，一併被刪除"』）與 `uc-delete-stage` post 第 2 條（spec 第 292 行『"若該 `stage` 內有 `card`，`card.stage` 更新為使用者選擇的目的 `stage`"』）在正式程式碼中沒有實作：`BoardController` 的 `DELETE .../swimlanes/{id}`、`DELETE .../stages/{id}` 沒有「已確認」或「目的 Stage」的輸入，有卡片時只會回 409（`SWIMLANE_HAS_CARDS`／`STAGE_HAS_CARDS`），沒有任何路徑能完成 post。這件事只寫在 `state.md`「已知延後」與 `decision-log.md`，**沒有寫進 `open-questions.md`**，而 `tasks.md` 的 T-03 範圍是「`card`＋`comment`」，沒有任何任務描述會接手「刪除 Swimlane／Stage 時協調刪除／轉移卡片」與「API 如何帶目的 Stage」。請新開一則 OQ，逐字引用 spec 第 136、141、287、292 行，`ui-kanban-basic.md` 第 85、183、189、197 行，以及 `design-kanban-basic.md` 第 90 行（『卡片實際的轉移/刪除是操作 Card 聚合完成，完成後應用層再重新呼叫一次 Board 的刪除方法』），寫清楚：(a) 目前 409 回應是當作「確認訊息／選擇目的 Stage」的觸發點，這個 fail 不在 spec 的 `fail` 裡，是依 design 推論；(b) 誰（T-03 或其他）負責在 application 層補上協調流程，以及刪除端點要怎麼帶「已確認」與目的 `stage`；(c) 在此之前對外 API 的限制。不要求本輪改程式碼。 |
| D-06 | T-02-be-board | done | 兩個有卡片的刪除 Scenario 靠測試步驟自己完成 post，斷言形同自我驗證：`BoardSteps.whenConfirmDelete`（第 274 行）先呼叫 `fakeCardLookupPort.removeAllCardsInSwimlane` 再刪 Swimlane，`thenSwimlaneAndCardsRemoved`（第 400 行）只檢查 Swimlane 不在，**完全沒檢查卡片**；`whenChooseDestinationStage`（第 332 行）自己呼叫 `fakeCardLookupPort.moveAllCardsToStage`，`thenCardsMovedTo`（第 448 行）再回頭讀同一個 fake 的狀態，驗證的是測試自己做的事，不是正式程式碼的行為。請讓這兩個 Scenario 的綠燈不要宣稱 post 已被驗證：在上述四個 step definition 加註解，說明哪些 Then 目前只是替身、指向 D-05 開的 OQ；同一則 OQ 也列出這四個步驟，等協調流程補上後改成打正式端點驗證。不可刪除 Scenario。 |
| D-07 | T-02-be-board | done | 活動紀錄斷言沒有驗證力：`BoardSteps.thenActivityRecorded`（第 479 行）只取 `occurredAt` 最新的一筆，檢查操作人是目前使用者、時間早於現在＋1 秒。但 Background 建立看板時已經由同一個使用者產生「建立看板」紀錄，所以就算被測操作完全沒寫活動紀錄，這個步驟還是會過。`swimlane-management.feature` 5 處、`stage-management.feature` 8 處的『該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間』都受影響；`BoardTest` 也只在建立看板與新增 Swimlane 兩個測試檢查 `activityLog`，改名／排序／刪除／Stage 各操作／設定角色都沒有檢查。請改成在 When 之前記下紀錄筆數，Then 時檢查筆數剛好加 1、新增那筆的操作人是目前使用者、動作內容對得上這次操作；兩個 `@fail-p1` Scenario 的「資料不變」也要一併檢查活動紀錄筆數沒變。`BoardTest` 補上其餘寫入方法的活動紀錄斷言。 |
| D-08 | T-02-be-board | done | OQ 引文不是逐字：OQ-IMPL-15 引 `design-kanban-basic.md` 寫成『權限檢查（僅 Owner 可設定）由呼叫端先查 `BoardMembership` 後才呼叫，`kanban-core` 本身不驗證』，原文第 115 行是『權限（僅 Owner 可設定）由呼叫端先查 `BoardMembership` 後才呼叫，`kanban-core` 本身不驗證。』，而且這句是在講 CR-003 的 `setStageRole`，請把出處（第 115 行、CR-003 段落）和適用範圍一起寫清楚，不要寫成適用於全部 9 個結構調整 uc 的原文。OQ-IMPL-14 開頭的路徑寫成『`.dev/F01-basic-kanban/spec-user-membership.md`（實際檔案為 `.dev/F02-user-membership/spec-user-membership.md`）』，請直接改成正確路徑並補上行號（第 260～269 行）。 |

