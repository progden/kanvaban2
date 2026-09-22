# T-16-fe-activity-log 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | done | — | 錯誤狀態未沿用專案既有的錯誤訊息視覺語彙（`kanban-frontend/src/canvas/ActivityLogItem.tsx:46`）。 問題：`ActivityLogItem` 載入失敗時輸出 `<p role="alert" className="activity-log__error">`，而 `ActivityLogItem.css` 裡的 `.activity-log__error` 只有 `padding: 12px; font-size: 12px;`，沒有任何顏色／底色／框線，會以一般內文樣式呈現。專案內其餘所有錯誤呈現一律使用 `src/index.css` 的共用 `.form-error`（`color: var(--color-error-text)`／`background: var(--color-error-bg)`／`border: 1px solid var(--color-error-border)`／`border-radius: 6px`），包含同屬 Canvas 群組的 `CanvasStage.tsx:433`（`className="form-error canvas-page__error"`）、`PlaceItemDialog.tsx:66`、`RemoveItemsDialog.tsx:34`、`BoardListPage.tsx:66` 等。 依據：`.dev/ui-prototype/README.md` 逐字寫『沒有設計稿的畫面（`s-activity-log`、`s-cards-by-assignee`、F03／F04／F05／F06 的 Canvas item 等）：沿用上面這些檔案的視覺語彙（同樣的字體、配色、按鈕／輸入框／對話框樣式）做最簡潔可用的版面，不另外發明風格。』`s-activity-log` 正是該句點名的畫面之一，錯誤訊息的配色必須沿用既有語彙，不可自成一格。 怎樣才算修好：錯誤段落改為沿用 `.form-error`（可另加一個只調整版面的修飾類別，例如 `className="form-error activity-log__error"`，並把 `.activity-log__error` 收斂成只處理 Canvas item 內的間距／margin，不自定顏色）；載入中／空狀態維持現狀即可（專案既有慣例就是純文字 `<p>`，見 `BoardListPage.tsx:71`）。既有的「載入失敗時顯示錯誤訊息」測試需仍為綠燈，`pnpm exec vitest run`／`pnpm run build`／`pnpm run lint` 全數通過。 |
