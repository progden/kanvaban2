# T-14-fe-board-item 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：退回

判定：退回（`doing`）。建置與測試全綠，但有兩處行為與 `ui-kanban-basic.md` 明文不符、一處測試缺口、一處 OQ 引文非逐字，已開 D-01～D-04。

### 1. 自己跑過的建置與測試（不採信交接摘要）

- `./gradlew build --no-daemon --rerun-tasks`（強制重跑，不吃 up-to-date 快取）：`BUILD SUCCESSFUL in 3m 27s`，12 個 task 全部實際執行，`kanban-core`／`kanban-spring` 測試與 Cucumber 全過。
- `kanban-frontend`：`npx tsc -b` 無輸出（0 錯誤）；`npx vitest run`：`Test Files 7 passed (7)`／`Tests 67 passed (67)`。
- `git status --porcelain` 無輸出，工作區乾淨。

### 2. spec／ui 對應（逐一核對九個 Screen）

- 已對上的：`s-board` 交會格渲染＋跨 Swimlane 拖曳＋新增／開詳情／刪除卡片入口；`s-swimlane-list` 新增／刪除停用條件；`s-stage-list` 新增（含插入位置）／設角色；`s-card-add-dialog`／`s-card-detail`／`s-card-delete-dialog`／`s-card-assignee-picker` 的成功與取消路徑。
- 抽查 `@fail-p1`（`uc-add-card` p1「拒絕，不建立新的 `card`」）：測試「開啟時顯示目的 Swimlane、Stage 名稱；標題為空時保留輸入並顯示訊息」讓 `POST /api/boards/board-a/cards` 回 400，斷言 `role="alert"` 顯示訊息且「新增卡片」對話框仍在；實作 `CardAddDialog.handleSubmit` 失敗時不呼叫 `onAdded`、不清空 `title`、不重新載入卡片，符合「輸入內容保留，顯示訊息」且資料不變。
- 不符的行為：D-01（s-card-detail 留言未顯示 `comment.created-at`，ui 資料表與 `CardDetail.dc.html` 都要求顯示）、D-02（刪除 Swimlane／Stage 對話框卡片數為 0 時整段不顯示，ui 寫「無卡片時卡片數顯示為 0」）。
- 測試缺口：D-03（`uc-move-card-stage`、兩個移動的 p2 失敗、`uc-rename-swimlane`／`uc-reorder-swimlane`／新增泳道空名稱、`uc-rename-stage`／`uc-reorder-stage`／START-DONE 互斥顯示、`uc-add-comment` p2，共 9 條 ui 驗收條件全無測試；全檔 grep 不到 `move-stage`／重新命名／排序字樣）。
- 端點形狀逐一比對 `BoardController`／`CardController`：`boardApi.ts` 新增的九個函式路徑、HTTP method 與 `NameRequest`／`MoveRequest`／`AddStageRequest`／`SetStageRoleRequest`／`confirmed`／`destinationStageId` 參數都對得上，沒有呼叫不存在的端點。

### 3. `kanban-core` 純度

本輪 diff 完全沒有動 `kanban-core/**`；新增的後端程式碼只在 `kanban-spring` 的 application／web 層，沒有把讀取邏輯塞進 aggregate。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：23 檔，前端集中在 `kanban-frontend/src/board/**`＋`api/boardApi.ts`／`cardApi.ts`，`pages/BoardCanvasPage.tsx` 只多一個 `BoardContext.Provider` 與 side-effect import。後端只加 `CardApplicationService.listCardsForBoard`＋`GET /api/boards/{boardId}/cards`＋兩個單元測試——這與已核准合併的 T-12（`12340f6`，前端任務同樣新增 `BoardApplicationService` 的 card-count 方法與端點）是同一種做法，且有成員資格檢查（非成員被拒的測試存在），判定在邊界內。`.state/` 只動 `.state/tasks/T-14-fe-board-item/**`，沒有碰 `.state/tasks.md`／`archive`／別的任務目錄，也沒有動 `.dev/conventions/**`、`scripts/**`、spec／ui 本體。

### 5. 交接摘要的待確認事項與 OQ

交接摘要列出的唯一待確認事項（`uc-assign-card-owner-by-drag` 無拖曳來源）確實已開成 `OQ-T-14-fe-board-item-01`，`loopctl show` 列得出來。覆核該則：等級「高」／阻塞「否」正確——`s-board` 狀態是「討論中」且待確認事項已標 ⚠️，略過該操作列不需違反任何定稿原文；「接手：無」也正確——`.state/tasks.md` 第 43 行 T-15-fe-member-management 的產出範圍只寫 `s-member-management`，沒有涵蓋「看板成員」item 本身。但第三段 OQ-19 引文非逐字（原文是「看板成員清單」，OQ 寫成「看板成員頭像清單」，且省略號略掉了關鍵句），見 D-04。決策紀錄提到的 `OQ-T-12-fe-board-list-02` 本輪未新增動作，維持原狀交人工。

### 6. 前端設計稿核對

比對 `.dev/ui-prototype/`：`Main.dc.html`（Swimlane × Stage 交會格、Stage 標頭含角色標籤、卡片縮圖含截止日期與頭像）、`CardDetail.dc.html`、`CardAddDialog.dc.html`。`Board.css` 用的是 `index.css` 既有 token（`--color-primary: #1f4bd8`、`--color-bg-muted: #f7f8fa`、`--color-border: #e2e6ee`、IBM Plex Sans／Noto Sans TC），沒有自創視覺風格。設計稿的灰色註記（`.id`／`.uc`／「⚠️ 規格未定義」）沒有外洩到產品畫面（grep 無 `⚠️`／`規格未定義`／裸 `uc-` 文字節點）。「需確認？」欄落實情形：三個刪除對話框都是「本畫面即確認」，`s-swimlane-list`／`s-stage-list`／`s-card-add-dialog`／`s-card-assignee-picker` 的「否」也都沒有多插確認框。唯一與設計稿不符的是 D-01 的留言時間。

### 保留事項（不阻塞，但要有人接手）

- `OQ-T-14-fe-board-item-01`（含 D-04 要補的逐字版）：`uc-assign-card-owner-by-drag` 這一列操作本輪未實作。接手者＝人工（決定走 A 或 B；若走 A，之後由安排階段追加 T-14 的修訂實例或新的 F07 相關任務）。

## 2026-09-22 Review 第 2 輪：附保留核准

判定：附保留核准（`done`）。D-01～D-04 四條修正逐條核對皆成立，建置與測試全綠，任務邊界乾淨，唯一保留是 `uc-assign-card-owner-by-drag` 未實作（OQ-T-14-fe-board-item-02，需人工決定）。

### 1. 自己跑過的建置與測試（不採信交接摘要）

- `./gradlew build --no-daemon`：`BUILD SUCCESSFUL in 9s`（12 task up-to-date，本輪未動後端）。
- `./gradlew test --no-daemon --rerun-tasks`（強制重跑，不吃快取）：`BUILD SUCCESSFUL in 3m 10s`，9 個 task 全部實際執行。
- `kanban-frontend`：`npx tsc -b` 無輸出（exit 0）；`npx vitest run`：`Test Files 7 passed (7)`／`Tests 80 passed (80)`（上一輪 67，本輪 +13）。
- `git status --porcelain` 無輸出，工作區乾淨。

### 2. D-01～D-04 逐條核對

- **D-01（留言未顯示 `comment.created-at`）**：`CardDetailDialog.tsx` 新增 `formatCommentTime`（`MM/DD HH:mm`），留言先 `sort((a, b) => a.createdAt.localeCompare(b.createdAt))` 再渲染，作者列多一個 `board-card-detail__comment-time`。測試「留言列表顯示 comment.created-at，並依時間由舊到新排序」以亂序（NEWER 在前）輸入，斷言渲染順序為舊→新，並用同一套換算檢查時間字串出現在該則留言的 `li` 內（避開時區 flaky）。`Board.css` 的 11px／`--color-text-label` 與 `CardDetail.dc.html` 第 66 行『09/15 14:02』的 `font-size:11px;color:#7B8896` 對得上。修好。
- **D-02（卡片數 0 不顯示）**：`SwimlanePanel.tsx` 拿掉 `(cardCountBySwimlane.get(...) ?? 0) > 0 &&` 的條件渲染；`StagePanel.tsx` 的 `StageDeleteConfirm` 把卡片數句子移到 `cardCount > 0` 區塊外，目的 Stage 下拉維持只在 > 0 出現——符合 `s-stage-delete-dialog` 狀態段『無卡片時可直接確認刪除，不需選擇目的 Stage』與 `s-swimlane-delete-dialog` 狀態段『無卡片時卡片數顯示為 0』。兩個「卡片數為 0」測試各一則。修好。
- **D-03（9 條驗收條件缺測試）**：逐條比對測試名稱與 ui 驗收條件字面：跨 Stage 拖曳（斷言 `POST /api/cards/card-1/move-stage` body `{stageId:'st-2'}`）、`uc-move-card-swimlane` p2 與 `uc-move-card-stage` p2（都斷言 `role="alert"` 訊息＋卡片仍在原交會格，資料不變）、`uc-rename-swimlane`／`uc-reorder-swimlane`（斷言 `beforeId: 'sw-1'`）／新增泳道空名稱（訊息＋輸入保留＋列表兩列不變）、`uc-rename-stage`／`uc-reorder-stage`／START-DONE 互斥（斷言原持有者的 select 值變回 `NONE`）、`uc-add-comment` p2（訊息＋輸入保留）。九條全部有對應斷言，且失敗情境都同時斷言訊息與畫面資料未變。修好。
- **D-04（OQ 引文非逐字）**：`OQ-T-14-fe-board-item-02` 第三段與 `.dev/loops/ui-authoring-loop/.state/ui-authoring-open-questions.md` OQ-19「選項」欄第 2 項原文逐字比對一致（含原先被省略的『分屬 F07 兩個不同 `item`』一句）；前兩段引文也回源頭比對過：`ui-kanban-basic.md` 第 267 行待確認事項與 s-board 操作表「拖曳看板成員頭像到卡片追加負責人」該列逐字相符。內文有註明取代第一則。修好。

### 3. Dev 自行補的行為改動（StagePanel 重新命名按鈕）

`StagePanel.tsx` 補上觸發 `setRenamingId` 的「重新命名」按鈕（原本 `handleRename`／`renamingId` 是死碼，`uc-rename-stage` 實際點不到）。這是 `s-stage-list` 操作表明列的「重新命名 Stage」列與驗收條件『重新命名送出後，該列名稱更新為新名稱，且觸發 `uc-rename-stage`』所必需，屬 D-03 的必要修正，不是超出範圍的自由發揮；行為與位置沿用 `SwimlanePanel.tsx` 慣例。與 `PanelStage.dc.html` 的差異（設計稿是每列「···」選單收納重新命名／刪除，實作是列上直接放按鈕）沿用第 1 輪已核准的面板容器決定（決策紀錄第 1 點：F07 選取面板機制未定案，改用對話框容器），不是新自創視覺風格。

### 4. `kanban-core` 純度

`grep -rn "org.springframework\|jakarta.persistence" kanban-core/src/main/java/io/progden/kanban/core/domain/` → 0 筆。跨 aggregate 讀取投影仍在 `io/progden/kanban/query/**`。本輪 diff 未動後端。

### 5. 任務邊界

`git diff loop/implementation...HEAD --stat`：25 檔，前端集中在 `kanban-frontend/src/board/**`＋`api/boardApi.ts`／`cardApi.ts`＋`pages/BoardCanvasPage.tsx`（Provider 與 side-effect import），後端只有第 1 輪已核准的 `CardApplicationService.listCardsForBoard`＋`GET /api/boards/{boardId}/cards`＋測試。`.state/` 只動 `.state/tasks/T-14-fe-board-item/**`，沒有 `.state/tasks.md`／`archive`／別的任務目錄；沒有動 `.dev/conventions/**`、`scripts/**`、spec／ui 本體。

### 6. 前端設計稿與呈現規則

`grep -rn "⚠️|規格未定義" kanban-frontend/src/` 只命中 `canvas/PlaceItemDialog.tsx` 的程式碼註解（T-13 遺留，不是畫面文字），產品畫面無註記外洩。「需確認？」欄落實情形同第 1 輪：三個刪除對話框「本畫面即確認」，其餘「否」的操作沒有多插確認框。

### 保留事項（不阻塞，各自接手者）

1. `OQ-T-14-fe-board-item-02`（逐字版，取代 `OQ-T-14-fe-board-item-01`）：`s-board` 操作表「拖曳看板成員頭像到卡片追加負責人」未實作，因為拖曳來源「看板成員」item 尚不存在。**接手者＝人工**（OQ 的 `接手` 欄填「無」是對的——`.state/tasks.md` 第 43 行 T-15-fe-member-management 的產出範圍只寫 `s-member-management`，沒有涵蓋「看板成員」item 本身與跨 item 拖曳機制）：需人工在選項 A（等 F07 補定義後由安排階段追加 T-14 修訂實例或新的 F07 任務）與選項 B（走 CR 移除該操作列）之間決定。覆核等級／阻塞：`s-board` 狀態為「討論中」且該事項已標 ⚠️，略過此列不需違反任何定稿原文，故「高／否」正確，可核准為附保留 `done`。
2. `OQ-T-14-fe-board-item-01`：引文有誤，已由 02 取代。**接手者＝人工**（`loopctl` 只能追加不能改既有列，請人工將 01 標記為已被 02 取代後關閉）。
3. `OQ-T-12-fe-board-list-02`（`uc-reject-board-access-by-nonmember`）：本任務未新增動作，決策紀錄已記錄現況符合「顯示訊息，停留本畫面」。**接手者＝人工**（沿用 T-12 的既有 OQ，本任務不接手）。
