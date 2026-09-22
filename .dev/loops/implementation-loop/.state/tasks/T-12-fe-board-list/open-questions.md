# T-12-fe-board-list open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-12-fe-board-list-01

[Level: s-board-list]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：**已由 OQ-T-12-fe-board-list-03 取代並解除（2026-09-22）**，見該則解除說明

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
- 狀態：**已解除（2026-09-22，人工核對，維持選項 A 的判斷成立）**

解除說明：T-14-fe-board-item 已合併（`decision-log.md` 第 29 行、`review.md` 第 81 行）確認 `BoardCanvasPage.tsx`（T-13 產出）既有的 `getBoard` 失敗處理已經符合這裡要求的行為，不需要另外的程式碼改動。人工複核程式碼確認：`BoardCanvasPage.tsx` 的 `getBoard(boardId)` 失敗時（含非成員被後端拒絕的情況）在 `.catch` 裡 `setError(...)`，畫面渲染 `error !== null` 時顯示 `role="alert"` 的錯誤訊息、不做任何導頁，符合 spec「顯示訊息，停留本畫面」的字面要求。

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

## OQ-T-12-fe-board-list-03

[Level: s-board-list]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-conflict
- 開立：Dev 第 2 輪（2026-09-22）
- 狀態：**已解除（2026-09-22，人工採選項 A）**

解除說明：維持現狀，刪除按鈕對所有看得到該 Board 的使用者顯示，非 Owner 點擊後由後端 `uc-delete-board` pre p1 的既有拒絕訊息擋下。理由：spec Scenario 與角色權限表都是定稿內容，設計稿灰字註記只是輔助說明、不是定案依據（`docs-convention.md` 引用方向 `spec ← ui ← design`，design 不能推翻 spec／ui）；設計稿的「角色僅 r-board-owner」註記視為尚待更正的舊註記，不另開 CR 修改，因為它本來就不是規格文字。程式碼不需變更。

情況：【兩處矛盾並列】

本則取代 OQ-T-12-fe-board-list-01（該則引文有三處漏掉半形空格、且未列出設計稿反證，人工無法據以判斷；本則修正後重開）。

引用 `.dev/F02-user-membership/ui-user-membership.md` s-board-list「角色與權限」表第 116 行（逐字，含半形空格）：
『| `r-board-owner` | 同上 | 刪除自己是 Owner 的 Board（依 `uc-delete-board` roles） |』
這一列只出現在 `r-board-owner` 角色底下，`r-system-user` 角色列沒有「刪除」這個「做得到」項目。

引用 `.dev/F02-user-membership/spec-user-membership.md`（Feature「Board 權限管理」）第 560～571 行：
『Scenario: 只有 Owner 可以刪除 Board，刪除後底下的資料一併刪除
    Given 這個 Board 有 2 個 Swimlane、3 個 Stage，以及數張卡片
    When "雅婷" 嘗試刪除這個 Board
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以刪除看板"
    When 我以 Owner 身分刪除這個 Board
    Then 這個 Board 應該不再存在』
（「雅婷」在這個 Background 是該 Board 的 Member，不是 Owner；整個 Scenario 都掛 `@uc-delete-board`，沒有另外的 `uc-reject-delete-board-by-member` 之類的 reject use case，跟同一份文件裡 uc-invite-member／uc-change-member-role／uc-add-swimlane 等操作都各自搭配一個獨立的 `uc-reject-*-by-member` 用例的寫法不一樣。）

與上面兩段矛盾的原文，引用 `.dev/ui-prototype/BoardDeleteDialog.dc.html` 第 77 行灰字註記（逐字）：
『從 s-board-list 每一列的「刪除 Board」操作進來（OQ-47）；角色僅 r-board-owner』

推論：角色與權限表、spec Scenario 的寫法（刪除沒有獨立 reject use case、非 Owner 的嘗試與 Owner 的成功放在同一個 `uc-delete-board` Scenario 裡靠 pre p1 失敗擋）傾向選項 A——「刪除」按鈕可以對所有看得到這個 Board 的使用者都顯示，非 Owner 點擊、確認後由後端依 `uc-delete-board` pre p1 用一致的錯誤訊息「只有 Owner 可以刪除看板」擋下並顯示在對話框。但設計稿灰字註記「角色僅 r-board-owner」直接寫明這個操作進入點只給 Owner 角色看到，傾向選項 B——只有 Owner 才顯示刪除按鈕，需要額外的角色判斷。這兩處原文對「刪除按鈕是否只對 Owner 顯示」給出相反的暗示，本任務判斷不出何者為準：引用方向雖然是 `spec ← ui ← design`（design 不能推翻定案的 spec／ui），但這裡design稿講的是「畫面上的操作入口是否顯示」這個 ui 層次的問題，而角色與權限表本身沒有明講「僅」這個限定詞，屬於規格留白由 design 補充的情況，還是 design 稿本身寫錯，本任務無法判斷。本任務（T-12）第 1 輪依 spec／ui 的寫法選了選項 A 實作：BoardListPage 對列表裡的每個 Board 都顯示「刪除」按鈕，不分角色；BoardDeleteDialog 確認刪除失敗時顯示 ApiError 訊息。

問題：s-board-list 的「刪除」按鈕是否應該只對該 Board 的 Owner 顯示（如設計稿灰字註記「角色僅 r-board-owner」所述，需要額外的角色判斷，例如打 GET /api/boards/{boardId}/members 找出自己的角色），還是可以像本任務實作的一樣，對 Owner 與 Member 都顯示、靠後端 uc-delete-board pre p1 的既有拒絕訊息把關（如角色與權限表、spec Scenario 的寫法所暗示）？

選項：A. 維持本任務目前實作（所有看得到 Board 的使用者都看到刪除按鈕，非 Owner 點擊後由後端訊息擋下，設計稿註記視為尚待用 CR 更正或補充說明）；B. 修改為只有 Owner 才顯示刪除按鈕（依設計稿註記，需要額外的角色判斷機制，屬於後續修正任務）

## OQ-T-12-fe-board-list-04

[Level: s-board-delete-dialog]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-conflict
- 開立：Review 第 2 輪（2026-09-22）
- 狀態：**已解除（2026-09-22，人工，與 OQ-T-12-fe-board-list-03 同一結論）**

解除說明：與 OQ-03 同一決策，維持現狀。`s-board-delete-dialog`「角色與權限」表只定義 `r-board-owner` 的「看得到／做得到」，沒有 `r-board-member` 那一列，是文件遺漏（沒寫不等於禁止開啟），不是與 OQ-03 相反方向的定案依據——真正的權責判斷仍在 `uc-delete-board` pre p1（僅 Owner 可刪除），後端一致把關；非 Owner 開啟本對話框後看到的是相同的確認畫面，送出後由後端訊息擋下。程式碼不需變更；`ui-user-membership.md` 角色與權限表暫不補 `r-board-member` 列，留待未來一併檢視「唯讀／可編輯角色在各畫面的顯示範圍」整體盤點時處理，不單獨為此開 CR。

情況：【兩處矛盾並列】

本則補充 `OQ-T-12-fe-board-list-03`（不取代它）：該則已並列 `ui-user-membership.md` s-board-list 角色與權限表與 `BoardDeleteDialog.dc.html` 的灰字註記，但漏掉了同一份**已定稿 ui 文件**裡另一段與設計稿註記同向、且比設計稿更具效力的原文，人工只看 OQ-03 會低估「只對 Owner 顯示」那一側的依據強度。

引用 `.dev/F02-user-membership/ui-user-membership.md` s-board-delete-dialog「角色與權限」表（逐字）：
『| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-board-owner` | 該 Board 名稱、其底下的 Swimlane 數、Stage 數與卡片數 | 確認刪除、取消 |』

同一節「狀態」段（逐字）：
『- 無權限：不適用（`uc-delete-board` roles 僅 `r-board-owner`，spec 未定義本畫面內的角色差異）』

與上述同向的設計稿原文，引用 `.dev/ui-prototype/BoardDeleteDialog.dc.html` 第 77 行灰字註記（逐字）：
『從 s-board-list 每一列的「刪除 Board」操作進來（OQ-47）；角色僅 r-board-owner』

相反方向的原文，引用 `.dev/F02-user-membership/spec-user-membership.md`（Feature「Board 權限管理」）：
『Scenario: 只有 Owner 可以刪除 Board，刪除後底下的資料一併刪除
    Given 這個 Board 有 2 個 Swimlane、3 個 Stage，以及數張卡片
    When "雅婷" 嘗試刪除這個 Board
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以刪除看板"
    When 我以 Owner 身分刪除這個 Board
    Then 這個 Board 應該不再存在』

推論：s-board-delete-dialog 的角色與權限表只列 `r-board-owner`，代表這個對話框在 ui 層次上預期只有 Owner 會進入；本任務目前的實作（列表每一列都顯示「刪除」按鈕，Member 點擊後也會開啟同一個對話框、按下確認才由後端訊息擋下）會讓 Member 進到一個 ui 只授予 `r-board-owner` 的畫面。但 spec 的 `uc-delete-board` Scenario 明確寫了 Member（"雅婷"）「嘗試刪除這個 Board」並收到錯誤訊息，代表 spec 預期存在一條非 Owner 也能發動刪除嘗試的路徑；依 `docs-convention.md` 的引用方向 `spec ← ui ← design`，spec 較上游，因此本審查判定本任務照 spec 做不必違反任何定稿文字，維持不阻塞。另外，前端目前沒有取得「自己在某個 Board 的角色」的管道：`GET /api/boards` 回傳的 `BoardResponse` 沒有角色欄位，要改成只對 Owner 顯示需要新增後端欄位或每列多打一次 `GET /api/boards/{boardId}/members`，屬於本任務範圍外的結構性改動。

問題：s-board-delete-dialog 的角色與權限表既然只列 `r-board-owner`，是否代表「列表的刪除按鈕與這個對話框都只該對 Owner 開放」（需要 CR 補上前端取得自身角色的機制與對應 spec 欄位），還是維持 spec Scenario 隱含的「任何成員都可嘗試、由 `uc-delete-board` pre p1 擋下」？

選項：A. 維持現況（照 spec Scenario，所有看得到 Board 的使用者都看得到刪除按鈕與對話框，非 Owner 確認後由「只有 Owner 可以刪除看板」擋下），由人工判定是否開 CR 把 ui 的角色與權限表與設計稿註記改成與 spec 一致；B. 依 ui s-board-delete-dialog 角色與權限表與設計稿註記，改成只有 Owner 看得到刪除入口，並開 CR 補上前端判斷自身角色所需的資料來源（例如 `BoardResponse` 增加呼叫者角色欄位），交由後續修訂實例實作。
