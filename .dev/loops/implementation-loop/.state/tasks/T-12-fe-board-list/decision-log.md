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

## 2026-09-22 Dev 第 2 輪：第 2 輪：處理 D-01～D-03

### 這輪做了什麼

依 Review 第 1 輪退回的 D-01～D-03，全部處理完畢，未擴大改動範圍。

- **D-01**：`BoardListPage.test.tsx` 的 `s-board-list` describe 補一則測試——點擊列表中 Board 名稱按鈕，斷言畫面切到 `/boards/<id>`（以 `BoardCanvasPage` 目前的佔位文字 `Board 畫布（待 T-13 實作）` 驗證），對應 ui 驗收條件「選擇列表中的 Board 後開啟該 Board」。測試檔改動以外沒有動 production code（`BoardListPage.tsx` 的導覽邏輯第 1 輪已經正確，只是缺測試）。
- **D-02**：開立 `OQ-T-12-fe-board-list-02`（等級高／不阻塞／接手 T-14-fe-board-item／`spec-ambiguous`），逐字引用 `ui-user-membership.md` s-board-list 操作表第 127 行與角色權限表第 115 行、`.state/tasks.md` T-14 那一列的產出範圍原文，說明本任務判斷 `uc-reject-board-access-by-nonmember` 屬於 T-14（載入 `s-board` 的任務）而非本任務（`s-board-list` 只處理看得到的 Board，看不到的不會在列表出現，不需要在此處理「直接開啟」）。
- **D-03**：`OQ-T-12-fe-board-list-01` 引文不逐字且漏列反證，無法直接修改既有 OQ（`loopctl oq` 只有 `add`，沒有 update／edit 子指令），故開立 `OQ-T-12-fe-board-list-03` 取代它：修正成逐字引文（`ui-user-membership.md` 第 116 行含正確半形空格），並把 `.dev/ui-prototype/BoardDeleteDialog.dc.html` 第 77 行灰字註記「角色僅 r-board-owner」逐字並列為與 spec／ui 相反的原文，情況欄改標「兩處矛盾並列」、`reason-code` 改為 `spec-conflict`；仍判定等級高、不阻塞（design 與 spec/ui 的引用方向本應以 spec/ui 為準，這裡呈現矛盾是為了讓人工看到 design 稿可能需要用 CR 更正，而不是本任務必須違反某段定稿原文才能完成——本任務仍可依 spec/ui 的既有實作完成，只是留一則 OQ 供後續判斷）。原 `OQ-T-12-fe-board-list-01` 未刪除（`loopctl` 沒有刪除 OQ 的機制），內容已在新 OQ 開頭註明「本則取代 OQ-T-12-fe-board-list-01」。

### 涵蓋範圍

本輪不涉及新的 entity／uc／Scenario，延續第 1 輪的涵蓋（`s-board-list`／`s-board-create-dialog`／`s-board-delete-dialog`，`uc-view-board-list`／`uc-create-board`／`uc-delete-board`）。本輪新增測試覆蓋了「選擇 Board 進入」這條原本缺測試的驗收條件；`uc-reject-board-access-by-nonmember` 仍延後給 T-14，但這次有 OQ 正式追蹤歸屬。

### 待確認事項

- `OQ-T-12-fe-board-list-02`：`uc-reject-board-access-by-nonmember` 該由 T-14-fe-board-item 實作、還是需要拆成獨立任務／補在路由層。
- `OQ-T-12-fe-board-list-03`（取代 `OQ-T-12-fe-board-list-01`）：`s-board-list` 的「刪除」按鈕是否應該只對 Owner 顯示——spec／ui 角色權限表寫法傾向「不分角色顯示、靠後端拒絕」，但設計稿 `BoardDeleteDialog.dc.html` 灰字註記「角色僅 r-board-owner」傾向「只對 Owner 顯示」，兩處矛盾，本任務維持第 1 輪選項 A 的實作。

### Check（本輪重跑）

- `pnpm run build`（`kanban-frontend`）：`tsc -b && vite build`，成功（`✓ built in 798ms`）。
- `pnpm run test`（vitest）：`Test Files 5 passed (5)`、`Tests 26 passed (26)`（比第 1 輪多 1 則，對應 D-01 補的測試）。
- `pnpm exec oxlint`：exit 0，無輸出。
- 本輪未改動 `kanban-spring`／`kanban-core` 任何檔案，未重跑 `./gradlew test`（第 1 輪已跑過且綠燈，本輪範圍不影響後端）。
