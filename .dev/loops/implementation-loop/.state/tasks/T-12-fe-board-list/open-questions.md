# T-12-fe-board-list open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-12-fe-board-list-01

[Level: s-board-list]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】

引用 `.dev/F02-user-membership/ui-user-membership.md` s-board-list「角色與權限」表：
『| `r-board-owner` | 同上 | 刪除自己是Owner的Board（依 `uc-delete-board` roles） |』
這一列只出現在 `r-board-owner` 角色底下，`r-system-user` 角色列沒有「刪除」這個「做得到」項目。

引用 `.dev/F02-user-membership/spec-user-membership.md`（Feature「Board 權限管理」）第 560～571 行：
『Scenario: 只有 Owner 可以刪除 Board，刪除後底下的資料一併刪除
    Given 這個 Board 有 2 個 Swimlane、3 個 Stage，以及數張卡片
    When "雅婷" 嘗試刪除這個 Board
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以刪除看板"
    When 我以 Owner 身分刪除這個 Board
    Then 這個 Board 應該不再存在』
（「雅婷」在這個 Background 是該 Board 的 Member，不是 Owner；整個 Scenario 都掛 `@uc-delete-board`，沒有另外的 `uc-reject-delete-board-by-member` 之類的 reject use case，跟同一份文件裡 uc-invite-member／uc-change-member-role／uc-add-swimlane 等操作都各自搭配一個獨立的 `uc-reject-*-by-member` 用例的寫法不一樣。）

推論：同一份 spec 對「非 Owner 嘗試特權操作」有兩種寫法——(1) 邀請成員、變更角色、調整結構都用獨立的 `uc-reject-*-by-member`，這些操作在對應的 ui 檔通常暗示需要先判斷角色才顯示操作入口；(2) 刪除 Board 沒有獨立 reject use case，非 Owner 的嘗試與 Owner 的成功放在同一個 uc-delete-board Scenario 裡，靠 pre p1 失敗來擋。這個寫法差異暗示：s-board-list 的「刪除」按鈕可以對所有看得到這個 Board 的使用者（Owner 與 Member）都顯示，非 Owner 點擊、確認後由後端依 uc-delete-board pre p1 用一致的錯誤訊息「只有 Owner 可以刪除看板」擋下並顯示在對話框，不需要前端額外打一次 API 判斷角色來隱藏按鈕。本任務（T-12）依這個推論實作：BoardListPage 對列表裡的每個 Board 都顯示「刪除」按鈕，不分角色；BoardDeleteDialog 確認刪除失敗時顯示 ApiError 訊息。

問題：s-board-list 的「刪除」按鈕是否應該只對該 Board 的 Owner 顯示（需要額外的角色判斷，例如打 GET /api/boards/{boardId}/members 找出自己的角色），還是可以像本任務實作的一樣，對 Owner 與 Member 都顯示、靠後端 uc-delete-board pre p1 的既有拒絕訊息把關？

選項：A. 維持本任務目前實作（所有看得到 Board 的使用者都看到刪除按鈕，非 Owner 點擊後由後端訊息擋下）；B. 修改為只有 Owner 才顯示刪除按鈕（需要額外的角色判斷機制，屬於後續修正任務）
