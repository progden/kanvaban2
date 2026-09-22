# T-04-be-board-membership 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Dev 第 1 輪：實作 F02 board-membership 全功能，25 個 Scenario 通過

### 這輪做了什麼

實作 `spec-user-membership.md` 除「使用者帳號」「登入登出」（T-01 已完成）以外的全部內容：新增 `BoardMembership`／`BoardRole` Aggregate（`kanban-core`）；`Board` 建立時在同一交易內建立 Owner 的 `board-membership`（CR-009 交接事項 2）；九個既有看板結構調整端點（`uc-add-swimlane`…`uc-set-stage-role`）補上 Owner 檢查，並新增 `uc-reject-structure-change-by-member`／`uc-reject-invite-by-member`／`uc-reject-role-change-by-member`（交接事項 1）；新增 `uc-delete-board`、`uc-view-board-list`、`uc-reject-board-access-by-nonmember`（`BoardController`／`BoardMembershipController`）；`Card` 新增 `assigneeIds`／`assignTo`／`unassignMember`，實作卡片負責人指派 Feature 全部 5 個 uc；實作「檢視看板活動紀錄」（新增 `io.progden.kanban.query.BoardActivityLogQueryService`，合併 `Board.activityLog` 與獨立的 `board_membership_activity_records` 投影表）。

對應 spec 的 `entity`／`uc-`／Scenario 涵蓋：`board-membership`（全部 4 個 uc：`uc-invite-member`／`uc-change-member-role`／`uc-remove-member`／隱含在 `uc-create-board` 的 Owner 自動建立）、「Board 權限管理」5 個 uc、「Board 存取權限」2 個 uc、「卡片負責人指派」5 個 uc、「檢視看板活動紀錄」1 個 uc，共 25 個新 Scenario，全數以 Cucumber MockMvc 驗收通過（`board-membership.feature`／`board-permission.feature`／`board-access.feature`／`card-assignment.feature`／`board-activity-log.feature`）。連同既有 F01／F02 使用者帳號共 60 個 Cucumber Scenario 全數通過。

### 規格沒講清楚、需要判斷的地方（已開 OQ，非阻塞）

- `OQ-T-04-be-board-membership-01`：`uc-set-card-assignees` 兩條 Scenario 的活動紀錄措辭不同（新增 vs 純粹移除），規格沒有明講切換規則，採「純粹移除列被移除者姓名、其餘情況列完整名單」。
- `OQ-T-04-be-board-membership-02`：`uc-delete-board` 的 `crud` 只標 `board-membership: R`，決定刪除 Board 後保留孤兒 `board-membership` 列（查詢時過濾），不主動清除。
- `OQ-T-04-be-board-membership-03`：非 Owner 嘗試移除成員時，重用「只有 Owner 可以變更成員角色」這句錯誤訊息（沒有專屬 fail Scenario 可依據）。

### 低風險技術決定（未開 OQ）

- `BoardMembership` 的活動紀錄不掛在自己或 `Board` 的 `activityLog`（前者會隨成員移除而消失，後者會讓 `uc-invite-member` 等 usecase 多出未宣告的 `board: U`），改記在獨立的 `BoardMembershipActivityJpaEntity` 投影表，由 `BoardActivityLogQueryService` 與 `Board.activityLog` 合併顯示（design-user-membership.md 第 7～8 節已預告這個方向）。
- `uc-delete-board` 採真正刪除（非封存）：`Board` 的 Swimlane／Stage 靠既有 cascade/orphanRemoval 一併刪除，Card 走既有軟刪除（`card.delete(operatorId)`）。design-user-membership.md 的「實作備註」把封存列為「可以」而非必須，真刪除更直接對應 post「該 board 不再存在」。
- `uc-member-add-card` 沒有對應的 fail Scenario（只測「Member 可以正常新增卡片」的成功案例），所以沒有替 `CardApplicationService.addCard`／`CardController` 加上board-membership 檢查——現有「已登入即可」行為已經滿足這條 Scenario，不腦補一個規格沒要求驗證的失敗情境。
- 除了任務交接筆記明確列出的兩點（九端點 Owner 檢查、`uc-create-board` 同交易建立 Owner membership），額外把「Board 存取權限」（`uc-view-board-list`／`uc-reject-board-access-by-nonmember`）與 `uc-delete-board`／`uc-view-board-activity-log`／卡片負責人指派端點都實作進 `BoardController`／`CardController`：這幾個 uc 都是 `spec-user-membership.md` 明確定義、且非 T-02／T-03 既有端點能涵蓋的行為，判斷屬於本任務「F02 board-membership」範圍的自然延伸，不是文件遺漏；沿用既有 `withOperator` 錯誤處理慣例，沒有引入新的錯誤處理模式。
- Cucumber 測試基礎設施：`spec-user-membership.md` 多處 Background 混用 `UserSteps`「我已登入系統，帳號為 X」與 `BoardSteps`「我已開啟一個名為 X 的看板」，兩者原本各自獨立管理登入 session；新增 `BoardSteps.adoptBoard`／`adoptUserStepsSessionIfNotLoggedIn` 橋接，讓兩段 Background 步驟可以銜接同一個登入身分。「我儲存變更」在 spec-kanban-basic.md（編輯欄位）與 spec-user-membership.md（設定負責人）共用同一段文字，透過 `CrossAggregateState.savingAssignees` 旗標分流到 `CardAssignmentSteps.submitPendingAssignees`，比照既有「我確認新增」「我確認刪除」的 dispatcher 慣例。

### Check（實際跑的指令與結果）

- `./gradlew :kanban-core:test --no-daemon`：BUILD SUCCESSFUL（`kanban-core` 全部單元測試含新增 `BoardMembershipTest`、`CardTest` 新案例）。
- `./gradlew :kanban-spring:test --no-daemon`：BUILD SUCCESSFUL，60 個 Cucumber Scenario 全數通過（含本輪新增 25 個）。
- `./gradlew build --no-daemon`：BUILD SUCCESSFUL（含 `NoSpringDependencyTest`，確認 `kanban-core` 仍不依賴 Spring）。

### 下一輪／Review 先看什麼

- 三則 OQ（見上）是否需要調整實作，或等人工／後續 CR 裁決。
- `BoardController`／`CardController` 的端點擴充範圍是否符合任務預期（決策記在「低風險技術決定」最後一點）。

## 2026-09-19 Dev 第 2 輪：修正 D-01／D-02：成員資格檢查與看板列表 Viewer 排除

### 本輪判斷與理由

- D-01（成員資格檢查缺漏）：在 `BoardMembershipApplicationService` 新增 `ensureCanEdit`（既有 `ensureMember` 沿用不變）。讀取類（`uc-list-card-assignee-candidates`／`uc-list-cards-by-assignee`／看板成員名單）改用 `ensureMember`，只要求是成員、不排除 Viewer；寫入類（`uc-member-add-card`／`uc-set-card-assignees`／`uc-assign-card-owner-by-drag`）改用 `ensureCanEdit`，額外拒絕 Viewer，依據是角色表 `r-board-viewer`『不能新增／編輯／移動／刪除任何內容』。這幾個 uc 的 `fail` 都是空的，訊息與 HTTP 狀態碼（403）是本輪決定的，已開 OQ-T-04-be-board-membership-04（等級高、不阻塞）交代這個切分與訊息文字沒有逐字依據。
- D-02（`uc-view-board-list` 是否列出 Viewer 的看板）：改成照 post 字面排除 Viewer（`listBoardsForUser` 過濾 `role == VIEWER`）。理由：post 是目前唯一逐字定案的文字，角色表「Viewer 可檢視看板」與這句話的落差是規格本身的既有矛盾（變更紀錄也承認尚未逐一檢視），已開 OQ-T-04-be-board-membership-05（等級高、不阻塞，兩處矛盾並列）交代兩種做法的取捨。
- 兩則 OQ 都不阻塞：都不需要違反任何已定稿的原文就能把任務做完，改動範圍限於 T-04 自己新增的端點與 `listBoardsForUser`。

### 涵蓋範圍

- 本輪只處理 D-01、D-02 兩項退回項目，不重新檢視其他 uc／Scenario。

### 待確認事項

- OQ-T-04-be-board-membership-01～03：延續自第 1 輪，尚未解除。
- OQ-T-04-be-board-membership-04：讀取／寫入類 uc 的成員資格檢查切分與失敗訊息／狀態碼是否符合規格意圖。
- OQ-T-04-be-board-membership-05：`uc-view-board-list` 是否該把 Viewer 的看板一併列出。

### Check（實際跑的指令與結果）

- `./gradlew clean build --no-daemon --rerun-tasks`：BUILD SUCCESSFUL（2m 18s）。
- 彙整 `kanban-core` ＋ `kanban-spring` 的 `build/test-results`：共 113 個測試（108 + 本輪新增 5 個：`CardApplicationServiceTest` 3 個、`BoardMembershipApplicationServiceTest` 2 個），skipped 0、failures 0、errors 0。
