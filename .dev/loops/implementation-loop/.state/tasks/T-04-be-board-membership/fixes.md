# T-04-be-board-membership 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | T-04 新增／擴充的端點沒有檢查「操作者是否為該 `board` 的 `board-membership` 成員」，任何已登入使用者（包含非成員）都能對任意看板的卡片設定負責人、查成員名單、查候選名單。 規格依據（`spec-user-membership.md`）： - `uc-member-add-card` pre：『p1: "操作者是該 `board` 的 `board-membership` 成員"』 - `uc-set-card-assignees`／`uc-assign-card-owner-by-drag`／`uc-list-card-assignee-candidates`／`uc-list-cards-by-assignee`／`uc-view-card-assignees` 的 roles 都是『roles: [r-board-member]』 - 角色表 `r-board-viewer`：『不能新增／編輯／移動／刪除任何內容』 現況：`CardController` 的 `setAssignees`／`dragAssign`／`listAssigneeCandidates`／`listCardsByAssignee`、`addCard`（`POST /api/boards/{boardId}/cards`），以及 `BoardMembershipController.listMembers` 都只有 `withOperator`（已登入）檢查。`CardApplicationService.ensureAllBoardMembers` 檢查的是「被指派的人」，不是「操作者」。decision-log 把 `uc-member-add-card` 不檢查 pre 列成低風險決定、沒有開 OQ；但規則書第 5 節寫明：『spec 的 `pre`／`post`／Scenario 沒講清楚該怎麼實作…＝高風險，寫進交接摘要「待確認事項」＋ OQ』。這裡 pre 已經寫明，只是 `fail: {}` 沒定義失敗時的訊息，所以不能當成可以不檢查。 怎樣才算修好： 1. 上述端點都要先檢查操作者的成員資格（例如重用 `BoardMembershipApplicationService.ensureMember`）。非成員被拒時資料不變。寫入類端點（新增卡片、設定／拖曳負責人）對 Viewer 要不要拒絕，由 Dev 依角色表判斷後實作，並寫進 OQ。 2. 開一則 OQ（等級「高」、不阻塞），逐字引用上面的 pre／roles 原文，說明失敗訊息與 HTTP 狀態沿用了什麼（規格 `fail: {}` 沒有定義），以及 Viewer 的處理方式。 3. 補測試（單元測試或 MockMvc 整合測試都可以，不要自己發明 Gherkin Scenario）：非成員呼叫 `addCard`／`setAssignees` 要被拒絕，且卡片數與 `card.assignees` 不變。 4. `./gradlew clean build --no-daemon` 全綠。 |
