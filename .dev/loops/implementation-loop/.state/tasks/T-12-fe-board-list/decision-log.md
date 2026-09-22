# T-12-fe-board-list 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：實作 s-board-list／建立與刪除對話框

### 這輪做了什麼

實作 `s-board-list`（我的 Board 列表）、`s-board-create-dialog`（建立 Board 對話框）、`s-board-delete-dialog`（刪除 Board 對話框），對應 `.dev/F02-user-membership/ui-user-membership.md` 同名三個 Screen 的操作表與驗收條件，行為依據 `.dev/F02-user-membership/spec-user-membership.md` 的 `uc-view-board-list`／`uc-create-board`／`uc-delete-board`（Feature「Board 存取權限」「Board 建立與成員邀請」「Board 權限管理」）。版面依 `.dev/ui-prototype/BoardList.dc.html`／`BoardCreateDialog.dc.html`／`BoardDeleteDialog.dc.html`／`StateBoardListEmpty.dc.html`，只調整版面與樣式。

### 涵蓋範圍

- `uc-view-board-list`：`BoardListPage` 掛載時呼叫既有 `GET /api/boards`（`BoardApplicationService.listBoards` 已由 T-04 依成員資格過濾，不需要前端再篩選）。
- `uc-create-board`：`BoardCreateDialog` 呼叫既有 `POST /api/boards`，成功後關閉對話框、新 Board 加入列表（不導頁，依 ui doc「完成後去哪裡：回到 s-board-list」）。
- `uc-delete-board`：`BoardDeleteDialog` 呼叫既有 `DELETE /api/boards/{boardId}`，成功後關閉對話框、該 Board 從列表移除。
- 空清單狀態：依 `StateBoardListEmpty.dc.html`，沒有 Board 時只顯示空狀態區塊裡的「建立看板」按鈕，不重複顯示頁首按鈕（頁首按鈕與空狀態按鈕若同時存在，兩者文字相同會造成兩個同名按鈕，且原型稿本身空狀態也沒有畫頁首按鈕）。

### 新增後端端點（跨越 fe/be 邊界，決策理由）

`s-board-delete-dialog` 驗收條件要求顯示 Swimlane 數、Stage 數與卡片數。前兩者可以直接用既有 `GET /api/boards`／`GET /api/boards/{boardId}` 回應裡 `swimlanes`／`stages` 陣列的長度，不用新端點。但「卡片數」（彙總屬於該 board 的所有未刪除 card）在現有 API 裡找不到：
- `BoardResponse` 沒有這個欄位；
- widgets 的 `/wip` 端點會排除 Done 角色 Stage 的卡片（`WipCalculator` 的 CR-012 邏輯），跟 spec 要的「全部」card 不同，不能借用。

因為這是 s-board-delete-dialog 已定案的驗收條件（不顯示等於違反已定稿內容），而且所需的資料已經有現成的 repository 方法可以重用（`BoardApplicationService.deleteBoard` 本身就在用 `CardRepository.findActiveByBoardId`），我判斷新增一個**最小、獨立**的端點 `GET /api/boards/{boardId}/card-count`（新檔 `CardCountResponse`，`BoardApplicationService.countActiveCards`）風險可控：不改動既有 `BoardResponse` 形狀、不影響其他呼叫 `/api/boards` 系列端點的任務或測試。已補上 `BoardApplicationServiceTest` 兩則測試（卡片數彙總、非成員查詢拒絕）。這屬於低風險技術實作細節，未違反任何定案內容，不需要 OQ；但因為跨越了本任務「fe」范圍去動 `kanban-spring`，在此明記理由供 Review 檢查。

### 有意識跳過／延後的部分

- `uc-reject-board-access-by-nonmember`（嘗試直接開啟不屬於自己的 Board）：ui doc 把它列在 s-board-list 操作表底下，但依 `.dev/loops/implementation-loop/.state/tasks.md` T-14-fe-board-item 那一列，載入單一 Board 內容（`s-board`）與這個拒絕流程屬於 T-14 的範圍（`BoardCanvasPage` 目前仍是 T-13 之前的 stub，實際打 `GET /api/boards/{boardId}` 並處理 403 是 T-14 的工作）。T-12 只做「選擇 Board 進入」的導覽（`navigate('/boards/:id')`），不實作拒絕與顯示訊息。這不是缺漏，是任務邊界問題，已在 `BoardListPage.tsx` 檔頭註解說明。
- 刪除按鈕是否應該只對 Owner 顯示：開了 OQ-T-12-fe-board-list-01（不阻塞），見該 OQ 說明；目前實作是「所有列表可見的使用者都看到刪除按鈕，非 Owner 點擊後由後端 `uc-delete-board` pre p1 的既有錯誤訊息擋下」。

### 待確認事項

- OQ-T-12-fe-board-list-01（不阻塞）：刪除按鈕是否該只對 Owner 顯示。

### Check（實際跑的指令與結果）

- `pnpm run build`（`kanban-frontend`）：`tsc -b && vite build` 成功。
- `pnpm run test`（`kanban-frontend`，vitest）：5 個測試檔、25 個測試全數通過。
- `pnpm exec oxlint`：exit code 0，無警告無錯誤。
- `./gradlew test --no-daemon`（repo 根目錄，含 `kanban-core`、`kanban-spring` 兩個模組全部測試，含既有 Cucumber 規格測試與新增的 `BoardApplicationServiceTest` 兩則測試）：BUILD SUCCESSFUL。
