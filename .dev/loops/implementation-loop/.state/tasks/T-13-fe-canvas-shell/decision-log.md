# T-13-fe-canvas-shell 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：實作 s-canvas 畫布主畫面

### 涵蓋範圍

依 `spec-canvas-layout.md` 全部五個 Feature、`ui-canvas-layout.md` `s-canvas` 實作：

- `entity`：`canvas`（讀取，不由前端建立／刪除）、`item`（CRUD）、`viewport`（CRU）
- `uc-`：`uc-init-canvas`、`uc-place-item`、`uc-remove-item`、`uc-move-item`、`uc-resize-item`、
  `uc-set-item-capabilities`、`uc-set-item-anchor`、`uc-reorder-item`、`uc-move-items`、
  `uc-remove-items`、`uc-set-viewport` 全數觸發
- Scenario：spec 五個 Feature 的全部 Scenario（含 `@fail-` 失敗情境）在前端操作路徑上都對應到後端既有實作（T-09 已完成），本任務新增的是 UI 觸發與失敗訊息顯示，不重複驗證後端本身（後端測試在 T-09）

### 判斷與理由

1. **角色換算（曾對應 OQ-44）**：`spec-canvas-layout.md` 2026-09-18 變更紀錄已定案
   `board-membership.role` Owner／Member → `r-canvas-editor`，Viewer → `r-canvas-viewer`；
   `ui-canvas-layout.md`「待確認事項」仍逐字寫「三者的對應關係 spec 未定義，見 OQ-44」，
   但那是舊文字，spec 本體已經補上定案內容（同一天的變更紀錄），依 `docs-convention.md`
   引用方向 spec 優先，採用 spec 定案內容，不視為未解決。實作：`BoardCanvasPage` 呼叫既有
   `GET /api/boards/{boardId}/members`（T-04）比對操作者 username 對到的 role，
   `role !== 'VIEWER'` 即為 `canEdit`。

2. **「加入元件」的具體 UI（`PlaceItemDialog`）**：`ui-canvas-layout.md` 操作表只把「放置元件」
   對應到 `uc-place-item`，未描述觸發的畫面元素；`Main.dc.html` 工具列上的「加入元件」圖示本身
   也標注「⚠️ 規格未定義：畫布工具列本身」。屬低風險技術決定：沿用既有對話框樣式
   （`.dialog-backdrop`／`.dialog-panel`／`.field`），欄位對應 `uc-place-item` 的輸入
   （元件識別碼、寬、高、錨定方式；`x`／`y` 固定帶 0，因為目前唯一會用到的元件是
   `uc-init-canvas` 自動建立的「board」，其餘元件 `item.component` 值尚未定案，見下第 4 點）。

3. **浮動工具列「鎖定」按鈕**：`uc-set-item-capabilities` 可各自設定 `movable`／`resizable`／
   `removable`，但 `CanvasPanel.dc.html` 只畫了一顆「鎖定」按鈕。低風險技術決定：
   「鎖定」同時切換 `movable`／`resizable`（`removable` 不受影響，仍可個別由後端
   `PATCH .../capabilities` 端點設定，前端目前沒有另外的能力設定面板）。

4. **只掛載 `item.component === 'board'`，其餘元件（看板成員、F03～F06 圖表）未實作掛載**：
   spec「其他名詞」明講「其餘元件的值待各自所屬模組實作對應 Item 時決定」；
   `ui-canvas-layout.md`「待確認事項」第二項也把 F03～F06 這類元件列為「待整合 CR 定案」；
   `ui-authoring-open-questions.md` OQ-45（看板成員 item 的進入機制）結論也是「機制細節待
   T7.02 撰寫 `s-canvas` 時一併判斷」，而 `ui-canvas-layout.md` 撰寫後仍未進一步定案。
   本任務提供的掛載機制是 `canvas/itemComponentRegistry.tsx` 的
   `registerItemComponent(component, Component)`：之後的前端任務（T-14 起）只要用
   `item.component` 字串註冊自己的渲染元件即可掛上內容；未註冊的 `item.component`
   顯示佔位文字。T-15（看板成員畫面）仍需等待 F02／F07 整合 CR 定案 `item.component` 值
   後才能真的掛上去，這不是本任務能單方面決定的事，不開新 OQ（既有 OQ-45／OQ-49 已涵蓋）。

5. **錨定方式切換時的座標換算**：spec `uc-set-item-anchor` post 明講「以新的錨定座標系與單位
   解讀；不由系統換算」。低風險技術決定：前端「固定於畫面」／「錨定於畫布」按鈕在送出
   `PATCH .../anchor` 前，把目前畫面上的顯示位置／大小換算成新錨定方式的座標（見
   `canvas/geometry.ts` `convertBoxForAnchor`），讓元素切換錨定後視覺位置不變；這是呼叫端
   （UI）決定要送什麼值，沒有違反「系統不換算」。

6. **未實作 `Main.dc.html` 的「選取」／「平移」工具列模式切換按鈕**：這兩顆按鈕的模式行為
   spec／ui 皆未定義；預設互動（點擊元素＝選取並可拖曳、拖曳空白處＝平移）已經涵蓋
   `ui-canvas-layout.md` 操作表要求的全部操作，不額外發明模式切換機制。

7. **未實作 `CanvasPanel.dc.html` 右側「泳道／階段／版面」頁籤面板**：那是 F01
   `s-swimlane-list`／`s-stage-list` 的內容，依 `tasks.md` 屬於 T-14-fe-board-item 範圍
   （task 表：「開啟管理 Swimlane／開啟管理 Stage 分別導向 F01 `s-swimlane-list`／
   `s-stage-list`」是跨模組導覽，非本任務要實作的畫面本體）。

### 涵蓋的既有共用檔異動

- `kanban-frontend/src/api/http.ts`：新增 `apiClient.patch`／`put`（`CanvasController` 用
  `PATCH`／`PUT`，既有 `apiClient` 只有 `get`／`post`／`del`）。
- `kanban-frontend/src/api/boardApi.ts`：新增 `getBoard(boardId)`（既有 `GET /api/boards/{boardId}`
  端點，`s-canvas` 頂列顯示看板名稱要用）。

### Check

- `pnpm exec tsc -b`：通過
- `pnpm run lint`（oxlint）：通過（1 則非阻斷性 warning：`itemComponentRegistry.tsx` 同檔匯出
  元件與函式，fast-refresh 建議搬檔，不影響行為）
- `pnpm test`（vitest）：6 個檔案、36 個測試全數通過（含新增 `BoardCanvasPage.test.tsx` 9 個
  測試、更新後的 `BoardListPage.test.tsx` 導覽測試）
- `pnpm run build`（`tsc -b && vite build`）：通過

### 待確認事項

無新增 OQ；沿用既有 `ui-canvas-layout.md`「待確認事項」與 `ui-authoring-open-questions.md`
OQ-45／OQ-49（看板成員／F03～F06 圖表如何成為 `item` 待整合 CR 定案），本任務未也不需要解決。

## 2026-09-22 Dev 第 2 輪：修正 D-01～D-04

### 處理內容

- **D-01**：`itemComponentRegistry.tsx` 的 `ItemContentProps` 加上 `component` 欄位；`CanvasStage.tsx`
  渲染 `Content` 時改傳 `itemId={item.id}`（原本誤傳 `item.component`）、另傳 `component={item.component}`
  供顯示用；`PlaceholderContent` 改用 `component` 顯示文字。修好下游任務（T-14 起）用
  `registerItemComponent` 掛上內容後，同一 `item.component` 放置多個 item 時可用 `itemId` 互相區分。
- **D-02**：畫面 z-index 改用「同一錨定方式（canvas／screen）內依 `item.z` 排序後的相對名次」
  （`CANVAS_ITEM_Z_BASE = 1` 起算，screen 另有 `SCREEN_ITEM_Z_BASE = 100000` 起算，維持 spec
  「畫面固定元素永遠繪於畫布元素之上」），避免 `item.z` 為 0 或負值時被格線背景蓋住；
  「＋ 加入元件」、浮動工具列（單選／多選）、縮放控制、錯誤訊息統一給 `CHROME_Z = 200000`，
  確保恆高於全部 item；浮動工具列 `top` 改用 `Math.max(TOOLBAR_MIN_TOP, ...)` 夾住，避免選取
  y 為 0 的 item 時工具列被 `overflow: hidden` 的 stage 裁掉。
- **D-03**：`PlaceItemDialog.tsx` 補上 X／Y 與可移動／可調整大小／可移除三個 checkbox（預設皆勾選，
  依 spec `uc-place-item` post「未指定時為 true」），送出時一併帶進 `placeItem` 請求；浮動工具列原本
  合併 movable／resizable 的「鎖定」按鈕拆成三顆各自可切換的能力按鈕（可移動／可調整大小／可移除），
  呼叫 `uc-set-item-capabilities` 時三項皆可個別設定。
- **D-04**：開立 `OQ-T-13-fe-canvas-shell-01`（等級高、不阻塞、owner 人工、reason-code
  spec-conflict），內文列出 spec「角色定義」與 ui「待確認事項」兩處矛盾原文，說明本任務依 spec
  定案內容實作 `canEdit` 判斷不算違反定稿文字，但 `ui-canvas-layout.md` 的待確認事項與角色表尚未
  同步更新，需要人工決定是否修訂 ui 文件。

### 涵蓋的 spec entity／uc／Scenario

本輪未新增涵蓋範圍，沿用第 1 輪判斷（見 decision-log 第 1 輪紀錄）；本輪異動屬同一批 uc 的實作修正
（`uc-place-item`、`uc-set-item-capabilities`）與畫面層序技術修正，不影響第 1 輪列出的 uc／Scenario 涵蓋清單。

### 待確認事項

- `OQ-T-13-fe-canvas-shell-01`：`ui-canvas-layout.md` 待確認事項與角色表是否要依 spec 定案內容修訂。

### Check

- `pnpm exec tsc -b`：通過
- `pnpm run lint`（oxlint）：僅 1 則既有非阻斷 warning（`itemComponentRegistry.tsx` fast-refresh 建議搬檔）
- `pnpm test`（vitest run）：6 個檔案、40 個測試全數通過（新增 5 個測試涵蓋 D-01～D-03）
- `pnpm run build`（`tsc -b && vite build`）：通過
