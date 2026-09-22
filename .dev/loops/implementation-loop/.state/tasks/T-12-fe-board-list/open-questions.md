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

## OQ-T-12-fe-board-list-02

[Level: s-board-list]
- 等級：高
- 阻塞：否
- 接手：T-14-fe-board-item
- 原因代碼：spec-ambiguous
- 開立：Dev 第 2 輪（2026-09-22）
- 狀態：待處理

情況：【引用原文】

引用 `.dev/F02-user-membership/ui-user-membership.md` s-board-list 操作表第 127 行：
『| 嘗試直接開啟不屬於自己的 Board | `uc-reject-board-access-by-nonmember` | 依 `uc-reject-board-access-by-nonmember` post：顯示訊息，停留本畫面 | 不適用（`uc-reject-board-access-by-nonmember` 無 fail 定義） | 否 |』

同一份文件第 115 行「角色與權限」表：
『| `r-system-user` | 自己是 Owner 或 Member 的 `board` 清單 | 選擇列表中的 Board 進入該 Board、嘗試直接開啟不屬於自己的 Board |』

引用 `.dev/loops/implementation-loop/.state/tasks.md` 第 42 行 T-14-fe-board-item 那一列的產出範圍欄：
『F01 內容作為 Canvas item：`s-board`、`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list`、`s-stage-delete-dialog`、`s-card-add-dialog`、`s-card-detail`、`s-card-delete-dialog`、`s-card-assignee-picker`（F02，負責人選取，從 `s-card-detail` 進入）』

推論：這一列產出範圍只列 Screen ID，沒有逐一列出對應的 uc；但「嘗試直接開啟不屬於自己的 Board」這個 use case 的觸發點是「直接開啟」一個 Board（例如手動輸入網址或分享連結進到 `/boards/{boardId}`），拒絕後的行為是「顯示訊息，停留本畫面」——這個「本畫面」在單一 Board 內容載入失敗時，語意上會落在載入 `s-board`（F01 內容作為 Canvas item）那一個畫面的實作範圍內，而不是 `s-board-list` 本身（`s-board-list` 只負責列表與列表裡看得到的 Board，看不到的 Board 不會出現在列表，不需要在列表畫面處理「直接開啟」）。T-12（本任務）的產出範圍只有列表、建立、刪除三個對話框，沒有 `s-board`，所以本任務判斷這個 use case 屬於 T-14-fe-board-item（載入 `s-board` 的任務），不屬於本任務範圍。

問題：`uc-reject-board-access-by-nonmember`（嘗試直接開啟不屬於自己的 Board）該由哪個任務實作？T-14-fe-board-item 的產出範圍目前只寫 Screen ID 沒有明確列出這個 uc，若 T-14 的 Dev 認為這個 uc 不屬於它的範圍（例如認為應該在 T-12 的路由層／ProtectedRoute 攔截），這個驗收條件會沒有任何任務接手實作。

選項：A. 維持本任務的推論，由 T-14-fe-board-item 在實作 `s-board` 時一併處理這個拒絕流程；B. 這個 uc 應該在更前面的路由層（例如 `App.tsx` 的 `/boards/:boardId` 路由或一個新的守衛元件）攔截，可能需要拆成獨立任務或補進 T-12／T-14 的產出範圍說明。
