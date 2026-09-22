# T-13-fe-canvas-shell 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：退回

### 1. 自己跑建置與完整測試（不採信 Dev 的 Check 欄）

在 `kanban-frontend/` 依序跑：

- `pnpm run build`（`tsc -b && vite build`）：通過，`✓ 50 modules transformed`、`✓ built in 737ms`。
- `pnpm test`（vitest run）：`Test Files 6 passed (6)`、`Tests 36 passed (36)`。
- `pnpm run lint`（oxlint）：只有 1 則 warning（`src/canvas/itemComponentRegistry.tsx:20:10 react(only-export-components)`），不阻斷。

建置與測試全綠，往下看程式碼細節。

### 2. spec／ui 對應

`uc-init-canvas`、`uc-place-item`、`uc-remove-item`、`uc-move-item`、`uc-resize-item`、`uc-reorder-item`、`uc-move-items`、`uc-remove-items`、`uc-set-viewport` 在 `BoardCanvasPage.test.tsx` 有對應測試；`uc-set-item-capabilities`、`uc-set-item-anchor`、`uc-remove-items` 有實作但沒有測試（後兩者由 D-02／D-03 一併處理）。

抽查失敗情境 `@uc-move-item @fail-p2`（spec：『Then 拒絕，訊息為 "此元素不可移動"，且資料不變』）：測試「移動失敗時位置還原並顯示訊息」以 409 + `{"message":"此元素不可移動"}` 模擬，斷言 `role="alert"` 顯示該訊息且 `left`／`top` 回到 0；對照 `CanvasStage.tsx` `commitMoveDrag()` 的 catch，確實用 `drag.originals` 把 x／y 還原，符合「顯示訊息，且資料不變」。`uc-place-item` p1／`uc-remove-item` p1 p2 的失敗訊息由 `PlaceItemDialog`／`RemoveItemsDialog` 各自 catch `ApiError` 後顯示，路徑成立。

### 3. kanban-core 純度

本輪只動 `kanban-frontend/**`（見第 4 點），`kanban-core`／`kanban-spring` 未變更，純度不受影響。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：17 個檔、+1544/-5。全部落在 `kanban-frontend/src/**`（`api/`、`canvas/`、`pages/`）與 `.state/tasks/T-13-fe-canvas-shell/**`（`decision-log.md`／`rounds.log`／`state.md`／`status`）。沒有動到別的任務目錄、`.state/tasks.md`、`.state/archive/**`、`.dev/conventions/**`、`scripts/**` 或 spec／ui 文件本體。共用檔的異動（`api/http.ts` 加 `patch`／`put`、`api/boardApi.ts` 加 `getBoard`、`api/boardMembershipApi.ts` 加 `listMembers`、`BoardListPage.test.tsx` 導覽測試）都是本畫面必要的，範圍乾淨。

### 5. 交接摘要的待確認事項／OQ

`loopctl show` 目前列不出任何 OQ；Dev 的交接摘要寫「無新增 OQ」。但 `decision-log.md` 判斷第 1 點其實是自行裁決了 spec 與 ui 兩份文件對角色對應的矛盾，依規則屬「等級高、不阻塞、照做並開 OQ」，OQ 沒有開出來 → D-04。其餘判斷（第 2～7 點）屬低風險技術決定，記在決策紀錄即可，我認可。

### 6. 前端設計稿核對

比對 `.dev/ui-prototype/Main.dc.html`（`s-canvas`＋`s-board`）與 `CanvasPanel.dc.html`（選取後面板）：

- 版面與視覺語彙一致：格線背景（`#E8EAF0` ＋ `radial-gradient(#C6CBD7 1.5px…)`、22px）、卡片式 item（白底、`#C4CBD8` 邊框、8px 圓角、陰影）、選取狀態 2px `#1F4BD8` 邊框、八個 10×10 把手、浮動工具列的按鈕組（置頂／置底／鎖定／固定於畫面／移除，移除為紅字）都對得上。
- 設計稿上的灰色註記（`.id`／`.uc`／`.sid`／「⚠️ 規格未定義」）沒有被做進產品畫面。
- 「需確認？」欄：移除元件／批次移除皆為「是」，`RemoveItemsDialog` 有落實；其餘操作皆為「否」，實作也沒有多加確認。
- 「失敗時」欄：各操作的 catch 都只顯示後端回傳訊息，沒有重述業務結果。
- 但工具列與「＋ 加入元件」在真實瀏覽器會被 item 蓋住／被裁掉（見 D-02），等於設計稿上這些控制項實際不可用。

### 判定：退回（第 1 輪，上限 6）

建置與測試雖然全綠，但有四項要修（`fixes.md` D-01～D-04）：

- D-01：掛載點 `itemId` 傳成 `item.component`，破壞本任務對 T-14～T-20 的對外契約。
- D-02：畫面 z-index 直接用 `item.z`，導致負 z 元素消失、「＋ 加入元件」被預設 item 蓋住、浮動工具列被裁掉／被蓋住。
- D-03：`s-canvas` 缺少 ui 資料表明訂為「輸入」的 `item.x`／`item.y`（放置時）與三項能力（放置、設定能力時）。
- D-04：spec 與 ui 對角色對應的矛盾只寫在決策紀錄，沒有依規則開成 OQ。

### 保留事項（不屬本輪退回理由，接手者已指明）

- ui 操作表的「新增 Swimlane」「新增 Stage」「開啟管理 Swimlane」「開啟管理 Stage」未實作：這些入口在看板本體 item 的內容裡，接手者為 **T-14-fe-board-item**（`tasks.md` 該列產出範圍含 `s-swimlane-list`／`s-stage-list`）。
- ui 操作表的「於『看板成員』item 選擇加入成員」未實作：接手者為 **T-15-fe-member-management**（`tasks.md` 該列註明「從 Canvas 上『看板成員』item 進入，機制待 T-13 實作時一併定案，見 OQ-45」）；T-13 這一輪提供的是 `registerItemComponent` 掛載點，`item.component` 的值仍待整合 CR 定案。
