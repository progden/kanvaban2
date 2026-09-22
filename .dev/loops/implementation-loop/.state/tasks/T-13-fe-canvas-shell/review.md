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

## 2026-09-22 Review 第 2 輪：退回

### 1. 自己跑建置與完整測試（不採信 Dev 的 Check 欄）

在 `kanban-frontend/` 依序前景跑：

- `pnpm run build`（`tsc -b && vite build`）：通過，`✓ 50 modules transformed`、`✓ built in 813ms`。
- `pnpm test`（vitest run）：`Test Files 6 passed (6)`、`Tests 40 passed (40)`、`Duration 32.32s`。
- `pnpm run lint`（oxlint）：1 則 warning（`src/canvas/itemComponentRegistry.tsx:21:10 react(only-export-components)`），不阻斷。

建置與測試全綠，往下看程式碼細節。

### 2. D-01～D-04 逐條複驗

- **D-01（掛載點 itemId）**：已修。`itemComponentRegistry.tsx` 的 `ItemContentProps` 為 `{ itemId, component, width, height }`；`CanvasStage.tsx:463` 傳 `<Content itemId={item.id} component={item.component} …/>`；`PlaceholderContent` 改用 `component` 顯示。測試「掛載的元件內容收到的 itemId 為該 item 的 id，不是 item.component」有斷言。
- **D-02（層序）**：已修。`CanvasStage.tsx:402-412` 依 anchor 分組、`sort((a,b)=>a.z-b.z)` 後取相對名次（`CANVAS_ITEM_Z_BASE = 1`／`SCREEN_ITEM_Z_BASE = 100000`），負 z 與 0 都會被映成 ≥ 1，仍維持「畫面固定元素永遠繪於畫布元素之上」；`.canvas-add-button`／`.canvas-toolbar`（單選與多選兩處）／`.canvas-viewport-controls`／錯誤訊息一律 `CHROME_Z = 200000`，且我確認這些節點與 item 節點是 `.canvas-stage` 的同層子節點（`CanvasStage.tsx:413-560`）、`CanvasStage.css` 裡沒有其他 z-index 或會另外開 stacking context 的宣告，比較基準一致。工具列 `top` 用 `Math.max(TOOLBAR_MIN_TOP, …-42)` 夾住。對應兩則新測試存在。
- **D-03（位置與能力輸入）**：已修。`PlaceItemDialog.tsx` 有 X／Y 數字欄位與可移動／可調整大小／可移除三個 checkbox，預設 `'0'`／`true`（符合 `uc-place-item` post「未指定時為 true」與 ui 資料表「布林，預設皆為 true」、anchor 預設 `canvas`），送出時一併帶入；浮動工具列拆成三顆 `aria-pressed` 的能力按鈕，`handleSetCapability()` 以 `patch.x ?? item.x` 形式逐項覆寫後呼叫 `setItemCapabilities(id, movable, resizable, removable)`。兩則新測試斷言送出的請求帶到這些值。
- **D-04（OQ）**：已開 `OQ-T-13-fe-canvas-shell-01`。

### 3. Dev 開的 OQ 逐一核對

`OQ-T-13-fe-canvas-shell-01`：

- 兩段引文我到源頭逐字比對過：`spec-canvas-layout.md:67`（`r-canvas-editor` 那一列）與 `ui-canvas-layout.md:81`（待確認事項最後一項），皆與 OQ 內文一字不差。
- 等級「高」、阻塞「否」正確：依檢驗句「把這個任務做完，是否必須違反某一段已定稿的原文？」——引用方向是 `spec ← ui`，照 spec 已定案的角色對應實作 `canEdit` 不需要改動任何定稿文字，故不阻塞。
- 接手「人工」正確：要修訂的是 `ui-canvas-layout.md` 本體（角色表與待確認事項），`.state/tasks.md` 裡沒有任何任務的產出範圍涵蓋 ui 文件修訂，本 loop 也禁止改 spec／ui 本體。
- 格式：情況欄為【兩處矛盾並列】，推論另起一行，問題／選項各一行，符合規則。

### 4. spec／ui 對應（本輪重點：逐一核對 uc 的測試涵蓋）

有對應前端測試的：`uc-init-canvas`、`uc-place-item`、`uc-remove-item`、`uc-move-item`、`uc-resize-item`、`uc-set-item-capabilities`、`uc-reorder-item`、`uc-move-items`、`uc-set-viewport`（共九個）。

**沒有任何測試的：`uc-set-item-anchor`、`uc-remove-items`（批次移除）**，而兩者都是 `ui-canvas-layout.md`「驗收條件」逐字列出的項目 → **D-05**。第 1 輪審查紀錄把這兩者寫成「由 D-02／D-03 一併處理」，但 D-02／D-03 的內文都沒有要求這兩則測試，Dev 沒補是合理的，因此這是新開的 D-05，不是同一問題的第二次退回。

抽查失敗情境 `@uc-move-item @fail-p2`（spec:445 附近逐字『Then 拒絕，訊息為 "此元素不可移動"，且資料不變』）：測試「移動失敗時位置還原並顯示訊息」以 409 ＋ `{"message":"此元素不可移動"}` 模擬，斷言 `role="alert"` 顯示該訊息、`left`／`top` 回到原值；對照 `CanvasStage.tsx` `commitMoveDrag()` 的 catch，確實用 `drag.originals` 還原 x／y 後才顯示訊息，符合「拒絕，資料不變」。`commitResizeDrag()`／`handleReorder()`／`handleSetCapability()`／`handleToggleAnchor()`／`commitPanDrag()`／`RemoveItemsDialog.handleConfirm()`／`PlaceItemDialog.handleSubmit()` 也都各自 catch `ApiError` 後顯示後端訊息、不改本地資料，路徑成立。

「離開」操作：`BoardCanvasPage.tsx` 上方列「← 我的看板」`navigate('/boards')`，符合驗收條件「離開後回到 F02 `s-board-list`」；載入中狀態也有。

### 5. kanban-core 純度

本輪與整個任務的 diff 只動 `kanban-frontend/**`，`kanban-core`／`kanban-spring` 未變更，純度不受影響。

### 6. 任務邊界

`git diff loop/implementation...HEAD --stat`：20 個檔、+1865/-5。程式碼全部落在 `kanban-frontend/src/**`（`api/`、`canvas/`、`pages/`），`.state/` 只動 `.state/tasks/T-13-fe-canvas-shell/**`（`decision-log.md`／`fixes.md`／`open-questions.md`／`review.md`／`rounds.log`／`state.md`／`status`）。沒有動到 `.state/tasks.md`、`.state/archive/**`、別的任務目錄、`.dev/conventions/**`、`scripts/**` 或 spec／ui 文件本體。範圍乾淨。

### 7. 前端設計稿核對

- 浮動工具列由 `CanvasPanel.dc.html:46-50`（置頂／置底／鎖定／固定於畫面）改成「置頂／置底｜可移動／可調整大小／可移除｜固定於畫面｜移除」，是 D-03 要求的結果：設計稿只畫一顆「鎖定」，但 ui 資料表要求三項能力各自可輸入，依「行為以 ui／spec 為準」，這個偏離是對的；樣式仍沿用同一套語彙（`.canvas-toolbar` 白底、`#c4cbd8` 邊框、7px 圓角、陰影、`btn-sm` 無邊框按鈕、紅字移除、`__divider` 分隔線），沒有自創風格。
- `PlaceItemDialog` 新增欄位沿用既有 `.dialog-backdrop`／`.dialog-panel`／`.field`／`.field-label`／`.field-input`。
- 設計稿上的灰色註記（`.id`／`.uc`／`.sid`／「⚠️ 規格未定義」）沒有出現在產品畫面裡。
- 「需確認？」欄：移除元件／批次移除皆為「是」，`RemoveItemsDialog` 落實；其餘操作皆為「否」，實作未多加確認。「失敗時」欄各處只顯示後端回傳訊息，沒有重述業務結果。

### 判定：退回（第 2 輪，上限 6）

建置／lint／測試全綠，D-01～D-04 我逐條複驗確認修好，OQ 的引文與等級標記正確，邊界與設計稿也沒問題。唯一未過的是第 4 點：`uc-set-item-anchor`、`uc-remove-items` 兩個 ui 驗收條件逐字列出的操作完全沒有測試（`uc-set-item-anchor` 還牽涉 `convertBoxForAnchor()` 的座標換算，是 Dev 自行決定的邏輯，無任何把關）→ `fixes.md` D-05。補齊這兩則（含一則失敗路徑）即可核准。

### 已知保留事項（不屬本輪退回理由，接手者已指明）

- `OQ-T-13-fe-canvas-shell-01`（`ui-canvas-layout.md` 角色表／待確認事項過時）：接手者 **人工**。
- ui 操作表「新增 Swimlane」「新增 Stage」「開啟管理 Swimlane」「開啟管理 Stage」未實作：接手者 **T-14-fe-board-item**。
- ui 操作表「於『看板成員』item 選擇加入成員」未實作：接手者 **T-15-fe-member-management**（`item.component` 的值待整合 CR 定案，見 OQ-45）。
- 縮放以 `clampZoom()` 事前夾住，使 `uc-set-viewport` p1（縮放超出範圍）在畫面上不可能被觸發，故驗收條件「縮放比例超出範圍時，顯示訊息」沒有對應路徑。畫面只提供固定倍率的「＋／−」按鈕、無法輸入任意縮放值，事前夾住屬合理的驗證實作，不列為退回理由；若之後要開放輸入縮放值，接手者為屆時修改 `s-canvas` 的任務。
