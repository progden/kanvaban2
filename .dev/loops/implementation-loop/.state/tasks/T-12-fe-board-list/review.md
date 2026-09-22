# T-12-fe-board-list 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：退回

### 1. 建置與完整測試（自己重跑）

- `pnpm run build`（`kanban-frontend`）：`tsc -b && vite build`，`✓ built in 773ms`，成功。
- `pnpm run test`（`kanban-frontend`，vitest 5.0.1）：`Test Files 5 passed (5)`、`Tests 25 passed (25)`。
- `pnpm exec oxlint`：exit 0，無輸出。
- `./gradlew test --no-daemon`：第一次全部 `UP-TO-DATE`，因此再跑一次 `./gradlew test --no-daemon --rerun-tasks`（`9 actionable tasks: 9 executed`）：`BUILD SUCCESSFUL in 2m 58s`。

建置／測試全綠，與 Dev 交接摘要的 Check 欄一致。

### 2. spec／ui 對應

`s-board-list`（ui 驗收條件 6 條）：列表顯示並觸發 `uc-view-board-list`、空清單、開啟 `s-board-create-dialog` 不觸發 uc、開啟 `s-board-delete-dialog` 不觸發 uc 都有對應測試（`BoardListPage.test.tsx`），行為正確。
- **缺**：「選擇列表中的 Board 後開啟該 Board（F07 `s-canvas`，跨模組）」無測試 → D-01。
- **缺**：「嘗試直接開啟不屬於自己的 Board」延後 T-14，未開成 OQ → D-02。

`s-board-create-dialog`（3 條）：空白輸入欄、確認建立觸發 `POST /api/boards` 後關閉並顯示新 Board、取消不觸發 uc（測試斷言 `/api/boards` 只被呼叫 1 次，即列表載入那次），三條都有測試且行為符合。

`s-board-delete-dialog`（3 條）：顯示 Board 名稱與泳道／階段／卡片數、確認刪除觸發 `DELETE /api/boards/{boardId}` 後該 Board 自列表移除、取消不觸發 uc，三條都有測試且行為符合。

`@fail-pN` 抽查：`uc-delete-board` 的 pre p1（spec-user-membership.md「只有 Owner 可以刪除 Board」Scenario，錯誤訊息 "只有 Owner 可以刪除看板"）由後端把關，後端測試 `非成員查詢卡片數時拒絕` 驗證 `FORBIDDEN` 且無資料異動；前端 `BoardDeleteDialog` 在 `catch` 內以 `ApiError.message` 顯示（`role="alert"`），失敗時不呼叫 `onDeleted`，列表不變，符合「拒絕，訊息為 …，且資料不變」。

### 3. `kanban-core` 純度

`git diff loop/implementation...HEAD --stat` 顯示本輪未動 `kanban-core/**`。新增的 `countActiveCards` 在 `io.progden.kanban.spring.application`、`CardCountResponse` 在 `io.progden.kanban.spring.web`，層次正確。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：18 個檔、`kanban-frontend/**` 13 個、`kanban-spring/**` 4 個、`.state/tasks/T-12-fe-board-list/**` 5 個（含 `status`）。未動 `.dev/conventions/**`、`scripts/**`、spec／ui 文件本體、`.state/tasks.md`、`.state/archive/**`、別的任務目錄。

`kanban-spring` 的 4 個檔（`BoardApplicationService.countActiveCards`、`BoardController` 新增 `GET /{boardId}/card-count`、新檔 `CardCountResponse`、`BoardApplicationServiceTest` 兩則測試）跨到 T-02-be-board 的模組。我確認過現有端點清單（`grep @GetMapping kanban-spring/.../web/*.java`）確實沒有任何端點回傳該 board 的全部卡片數，而 `s-board-delete-dialog` 資料表「卡片數」是已定案內容，不顯示等於違反定稿原文；新增內容是純新增、未改動既有 `BoardResponse` 形狀或既有端點行為，且已補測試。**接受這項越界，列為保留事項**（見下）。

`App.test.tsx`／`LoginPage.test.tsx` 的改動只是把舊 stub 斷言 `/Board 列表/` 換成真實畫面的 `我的看板` 標題並補 `/api/boards` mock，屬於本任務必要的同步，接受。

### 5. OQ 核對

OQ-T-12-fe-board-list-01（等級高／不阻塞／接手 無）：
- 阻塞判定**正確**。spec-user-membership.md 的 `uc-delete-board` Scenario 本身就包含「"雅婷" 嘗試刪除這個 Board」（雅婷是 Member），代表 spec 預期非 Owner 可以發動這個嘗試，Dev 的實作不必違反任何定稿原文即可完成任務，標「高／不阻塞」成立。`接手：無` 也符合規則（沒有既有任務會處理按鈕顯示條件）。
- **但內文有兩個問題**：ui 第 116 行引文缺了三處半形空格、不是逐字；且完全沒有並列設計稿 `BoardDeleteDialog.dc.html` 的灰字註記「角色僅 r-board-owner」這項反證 → D-03。

Dev 交接摘要「待確認事項」只列了這一則，但「有意識跳過／延後的部分」裡的 `uc-reject-board-access-by-nonmember` 同樣是已定案驗收條件被延後、卻沒有任何 OQ 追蹤 → D-02。

### 6. 設計稿核對

- `BoardList.dc.html`：標題「我的看板」、副標「你是 Owner 或 Member 的看板都在這裡。」、三欄 grid（`repeat(3, minmax(0,1fr))`、gap 20px）、卡片 `#fff`／`1px solid #DDE2EA`／radius 8px／`0 1px 2px rgba(22,32,44,.05)`、刪除按鈕用 `btnsm` 加紅字，`BoardListPage.css` 逐項對上，沒有自創風格。卡片上方 112px 的畫布縮圖區未實作（縮圖內容規格未定義），屬可接受的簡化。
- `StateBoardListEmpty.dc.html` 確認空狀態稿上沒有頁首「建立看板」按鈕，Dev 在空清單時隱藏頁首按鈕的做法與設計稿一致。
- `BoardDeleteDialog.dc.html`：三格計數方塊（19px/600 數值 + 11.5px 灰字標籤）、確認鈕 `#A02515`、取消用 `btn2`，實作一致。
- 灰色註記（`board.name`、`uc-delete-board`、`→ s-board-delete-dialog`、`⚠️ 規格未定義…`）都沒有出現在產品畫面裡，已逐一比對 `BoardListPage.tsx`／`BoardCreateDialog.tsx`／`BoardDeleteDialog.tsx`。
- ui 操作表「需確認？」欄：`uc-create-board` 為「否」→ 建立對話框沒有二次確認，正確；`uc-delete-board` 為「是（本畫面即確認）」→ 刪除對話框本身即確認畫面，沒有再多一層，正確。

### 判定：退回（第 1 輪，上限 6）

建置／測試綠燈、邊界可接受、沒有阻塞的 OQ，但第 2 點有兩項已定案驗收條件的缺口（D-01 測試缺、D-02 延後未開 OQ）、第 5 點 OQ 內文不合規（D-03），交回 Dev 第 2 輪處理 D-01～D-03。

保留事項（本輪不擋，記在此供後續核對）：
- 跨 fe/be 新增 `GET /api/boards/{boardId}/card-count`：接手者＝本任務（已完成並有測試），若日後 T-02-be-board 有修訂實例需一併知悉。
