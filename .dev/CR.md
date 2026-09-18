# CR 總表

| 編號 | 標題 | 類型 | 提出人 | 提出日期 | 影響模組 | 影響 ID | 狀態 | 完成日期 | 明細 |
|------|------|------|--------|----------|----------|---------|------|----------|------|
| CR-001 | Board/Card 補上操作人記錄 | 變更 | SA | 2026-09-12 | spec-kanban-basic、spec-user-membership | `board`、`card`、`uc-add-swimlane`、`uc-rename-swimlane`、`uc-reorder-swimlane`、`uc-delete-swimlane`、`uc-add-stage`、`uc-rename-stage`、`uc-reorder-stage`、`uc-delete-stage`、`uc-add-card`、`uc-edit-card`、`uc-move-card-swimlane`、`uc-move-card-stage`、`uc-delete-card` | 處理完成 | 2026-09-12 | |
| CR-002 | 卡片負責人改為多選 | 變更 | SA | 2026-09-12 | spec-kanban-basic、spec-user-membership | `card`、`uc-edit-card`、`uc-set-card-assignees`(新增)、`uc-list-card-assignee-candidates`(新增)、`uc-view-card-assignees`(新增)、`uc-list-cards-by-assignee`(新增) | 處理完成 | 2026-09-12 | |
| CR-003 | Stage 新增角色標記（Start／Done） | 新增 | SA | 2026-09-13 | spec-kanban-basic | `stage`、`uc-set-stage-role`(新增) | 處理完成 | 2026-09-13 | |
| CR-004 | 事件時間改用 Board Clock | 變更 | SA | 2026-09-13 | spec-kanban-basic、spec-user-membership、spec-board-clock | `board`、`card`、`uc-adjust-board-clock`(新增)、`uc-guard-clock-monotonicity`(新增)、`uc-pause-resume-board-clock`(新增) | 處理完成 | 2026-09-13 | |
| CR-005 | 規格格式遷移至 usecase 區塊 | 變更 | SA | 2026-09-16 | spec-kanban-basic、spec-user-membership、spec-kanban-widgets、spec-board-clock、spec-workload、spec-feature-cr-board | `board`、`board-membership`、`card`、`stage`、`swimlane`、`user`、`uc-add-card`、`uc-add-comment`、`uc-add-stage`、`uc-add-swimlane`、`uc-adjust-board-clock`、`uc-assign-card-owner-by-drag`、`uc-change-member-role`、`uc-create-board`、`uc-create-user`、`uc-delete-board`、`uc-delete-card`、`uc-delete-stage`、`uc-delete-swimlane`、`uc-drag-assign-card-owner`、`uc-edit-card`、`uc-guard-clock-monotonicity`、`uc-invite-member`、`uc-list-card-assignee-candidates`、`uc-list-cards-by-assignee`、`uc-login`、`uc-logout`、`uc-member-add-card`、`uc-move-card-stage`、`uc-move-card-swimlane`、`uc-pause-resume-board-clock`、`uc-reject-board-access-by-nonmember`、`uc-reject-invite-by-member`、`uc-reject-role-change-by-member`、`uc-reject-structure-change-by-member`、`uc-remove-member`、`uc-rename-stage`、`uc-rename-swimlane`、`uc-reorder-stage`、`uc-reorder-swimlane`、`uc-set-card-assignees`、`uc-set-stage-role`、`uc-view-aging-wip`、`uc-view-board-activity-log`、`uc-view-board-list`、`uc-view-card-assignees`、`uc-view-cfd`、`uc-view-cycle-lead-time`、`uc-view-duedate-reminder`、`uc-view-feature-cr-board`、`uc-view-throughput`、`uc-view-wip`、`uc-view-workload` | 修改規格 | | |
| CR-006 | 建立帳號時 username 不可留空的 pre／fail 補齊 | 變更 | implementation-loop（T-01-be-user，OQ-IMPL-10） | 2026-09-18 | spec-user-membership | `uc-create-user` | 處理完成 | 2026-09-18 | |
| CR-007 | 登入後 TopBar 顯示 display-name | 變更 | implementation-loop（T-10-fe-shell，OQ-IMPL-11） | 2026-09-18 | spec-user-membership、ui-user-membership | `uc-login`、`s-login` | 待處理 | | |
| CR-008 | 帳號 ID 重複時的驗收條件改為「觸發 uc-create-user」 | 變更 | implementation-loop（T-11-fe-auth，OQ-IMPL-13） | 2026-09-19 | ui-user-membership | `s-signup` | 處理完成 | 2026-09-19 | |
| CR-009 | 建立看板時的預設 Swimlane／Stage | 變更 | implementation-loop（T-02-be-board，OQ-IMPL-14） | 2026-09-19 | spec-user-membership | `uc-create-board` | 待處理 | | |

### CR-001：Board/Card 補上操作人記錄
- 背景：Swimlane／Stage／Card 會改變狀態的情境，原本沒有記錄是誰做的操作，F02 要做活動紀錄需要這份資料。
- 變更內容：Swimlane、Stage、Card 會改變狀態的既有情境，補上操作人記錄，供活動紀錄使用（見 `spec-user-membership.md`）。
- 驗收標準：Swimlane／Stage／Card 的既有寫入情境（新增、命名、排序、刪除、移動等）都記錄操作人。

### CR-002：卡片負責人改為多選
- 背景：F02 引入 BoardMembership 後，卡片負責人需要參照看板成員並支援多人。
- 變更內容：原本「編輯卡片詳細內容」情境含單一負責人欄位 → 移除該欄位，負責人改為多選、參照看板成員，改由 F02「卡片負責人指派」情境處理。
- 驗收標準：「編輯卡片詳細內容」情境不再包含負責人欄位；負責人指派／移除改由 F02 新增情境處理，且參照的對象必須是該看板成員。

### CR-003：Stage 新增角色標記（Start／Done）
- 背景：F03 標準圖表要計算 Cycle/Lead Time，需要知道哪個 Stage 代表「開始」、哪個代表「完成」。
- 變更內容：新增 Stage 角色標記（Start／Done），供 F03 標準圖表計算 Cycle/Lead Time 使用。
- 驗收標準：Stage 可設定角色為 Start、Done 或無角色；F03 的 Cycle/Lead Time 計算以此為依據。

### CR-004：事件時間改用 Board Clock
- 背景：F04 引入每個 Board 自己的時鐘後，Board/Card 事件時間需要改用 Board Clock，而非系統時間，才能支援調整／暫停／恢復。
- 變更內容：名詞定義補上「操作時間（occurredAt）」，明訂 Board/Card 事件時間一律取自 Board Clock（見 `spec-board-clock.md`）；User／BoardMembership 事件維持系統時間。既有 Scenario 文字不需修改，故不掛 Scenario 層級 tag。
- 驗收標準：`kanban-core` 的 `Board`／`Card` 事件時間全面改用 Board Clock，不再直接取用系統時間；User／BoardMembership 事件不受影響。

### CR-005：規格格式遷移至 usecase 區塊
- 背景：`.dev/conventions/` 已依 `spec-migration-prompt.md` 改為腳本可解析格式，但 F01～F06 六份既有規格尚未遷移，`spec-check` 目前報大量 error。
- 變更內容：六份 spec 依新格式補上狀態行、實體／欄位／關係表、角色表、每個 Feature 的 usecase 區塊、tag（`@uc-`／`@fail-`）、Aggregate 註解與變更紀錄格式；不改變既有 Scenario 的行為。
- 驗收標準：F01～F06 六份 `spec-*.md` 全部通過 `./scripts/spec-check`（0 error）；`cr-check --base <baseline> --cr CR-005` 通過。

### CR-006：建立帳號時 username 不可留空的 pre／fail 補齊
- 背景：`implementation-loop` 實作 T-01-be-user 時發現，「名詞定義」欄位表已明訂 `user.username`「非空、全系統不可重複」，但 `uc-create-user` 的 `pre` 只有「密碼長度不可超過 40 字」「username 不可重複」兩條，沒有「username 非空」對應的 `pre`／`fail`，是覆蓋範圍缺口（不是矛盾——欄位表的「可留白」講的是 `user.password`，不是 `user.username`，已跟人工確認）。
- 變更內容：`uc-create-user` 新增 `pre.p3`「`user.username` 非空」與對應 `fail.p3`；新增 Scenario「建立帳號時使用者名稱不可留空」，掛 `@CR-006 @uc-create-user @fail-p3`。
- 驗收標準：`uc-create-user` 的 `pre`／`fail` 各有 3 條；新增的 Scenario 驗證「使用者名稱留空時拒絕，訊息為『使用者名稱不能為空』，且不建立帳號」；`kanban-core`／`kanban-spring` 依此補上驗證邏輯，`./gradlew clean build` 通過。

### CR-007：登入後 TopBar 顯示 display-name
- 背景：`uc-login` post 原文「TopBar 顯示該 `user` 的帳號名稱」、`ui-user-membership.md` `s-login` 的「帳號名稱」「該使用者名稱」，都沒有對應到欄位表的 `user.username`（帳號 ID）或 `user.display-name`（顯示名字）其中一個；既有 Scenario 的帳號 "user1" 未指定顯示名字，兩個欄位同值，分不出來。`implementation-loop` T-10-fe-shell 先依字面做成 `user.username`（OQ-IMPL-11），2026-09-18 人工決議採 `user.display-name`（欄位表：「顯示名字，看板上顯示用」，與成員清單、查詢對象的顯示一致）。
- 變更內容：`uc-login` post 改為「TopBar 顯示該 `user` 的顯示名字」；新增 Scenario「登入後 TopBar 顯示的是顯示名字而不是帳號 ID」（`@CR-007 @uc-login`）；`s-login` 操作表與三條驗收條件的用詞改為 `user.display-name`。既有 Scenario 不變。
- 驗收標準：新增的 Scenario 由 Cucumber 驗證通過；`GET /api/session` 回應帶顯示名字，`kanban-frontend` 的 TopBar 顯示它；`./gradlew clean build`、`pnpm test` 通過（implementation-loop 任務 `T-21-cr007-topbar-display-name`）。

### CR-008：帳號 ID 重複時的驗收條件改為「觸發 uc-create-user」
- 背景：`ui-user-membership.md` `s-signup` 驗收條件原文「帳號 ID 與系統中既有帳號重複時確認建立帳號，輸入內容保留、顯示訊息，且不觸發 `uc-create-user`」。帳號是否重複只有後端知道（`uc-create-user` pre p2／fail p2），畫面不呼叫 Use Case 就無從得知，這一條做不到；同檔 `s-member-management` 對同類情況（`uc-invite-member` p2）的寫法是「觸發 `uc-invite-member`，輸入內容保留、顯示訊息」。`implementation-loop` T-11-fe-auth 的 Dev 與 Review 因此各讀出一種意思（OQ-IMPL-13），2026-09-19 人工決議比照 `s-member-management` 改寫。
- 變更內容：該條驗收條件改為「帳號 ID 與系統中既有帳號重複時確認建立帳號，觸發 `uc-create-user`，輸入內容保留、顯示訊息」。spec 不變（`uc-create-user` pre p2／fail p2 與 Scenario「帳號 ID（username）不可重複」本來就是這個行為）。同時在 `ui-convention.md`「驗收條件」補上「觸發」的定義與兩種 `pre` 的寫法（convention 改動不屬於 CR 範圍，記在這裡供追溯）。
- 驗收標準：`ui-check` 0 error；`kanban-frontend` `SignupPage.test.tsx` 對應測試的名稱與這條驗收條件一致，且斷言有送出 `POST /api/users`；`pnpm test` 通過。程式碼行為不需要改（現況已是送出請求、後端回 409、畫面保留輸入並顯示訊息）。

### CR-009：建立看板時的預設 Swimlane／Stage
- 背景：`spec-kanban-basic.md` 關係表規定 `board`→`swimlane`、`board`→`stage` 的 min 都是 1（「看板至少保留一個 Swimlane／Stage」），新建的看板不能是空的，但 `uc-create-board` 的 post 沒有寫新看板帶什麼；F01 多條 Scenario 的前提用到「預設泳道」與「待辦／進行中／完成」，`implementation-loop` T-02-be-board 依此推論實作（OQ-IMPL-14）。2026-09-19 人工定案：採用這組預設，Stage 角色皆為 NONE——角色是要看 Cycle/Lead Time 圖表時才由 `uc-set-stage-role` 設定（CR-003），跟建立看板是不同時機。
- 變更內容：`uc-create-board` 新增 post「新的 `board` 帶有 1 個 `swimlane`（名稱「預設泳道」）與 3 個 `stage`，依序為「待辦」、「進行中」、「完成」，`stage.role` 皆為 NONE」，`crud` 補 `swimlane: C`、`stage: C`；新增 Scenario「建立 Board 後帶有預設的 Swimlane 與 Stage」；既有 Scenario 的 Aggregate 註解同步。
- 驗收標準：新增的 Scenario 由 Cucumber 驗證通過（step definition 由 `implementation-loop` 任務 T-04-be-board-membership 一併補上，`uc-create-board` 的 Owner membership post 也在該任務完成）；`kanban-core` 的「Board.create」行為不需要改（現況已符合）。
