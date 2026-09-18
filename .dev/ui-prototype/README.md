# ui-prototype（前端版面設計稿）

來源：claude.ai Design Artifact <https://claude.ai/artifact/5v45krEsoCJdWwu7fjV5sH>（版本 `1789720242-9af0`，2026-09-18 匯出 `project/*`）。匯出的原因：`implementation-loop` 的 Dev／Review 是一次性 `claude -p` 行程，打不開 claude.ai 的 Artifact，前端任務只能憑空做版面（OQ-IMPL-12）。設計稿更新時重新匯出覆蓋，並更新上面的版本號。

## 這些檔案是什麼、不是什麼

- 是**版面與視覺的依據**：每個 `*.dc.html` 是一個靜態畫面，直接讀 HTML／inline style 就能得到版面結構、間距、配色、字體（IBM Plex Sans／Noto Sans TC、主色 `#1F4BD8`…）。檔頭的 `<span class="sid">` 標出它對應的 Screen ID。
- **不是行為規格**：操作、驗證、失敗呈現、導覽一律以 `.dev/F0x-*/ui-*.md` 與 `spec-*.md` 為準。設計稿上的灰色小字（`.id`／`.uc`，例如 `user.display-name`、`uc-logout`）與「⚠️ 規格未定義：…」是設計時的註記，**不要做進產品畫面**，也不能拿來當規格依據；它跟 ui／spec 檔不一致時以 ui／spec 為準並開 OQ。
- 範例資料（「付款平台改版」「陳柏翰」…）只是示意。
- `support.js`／`<x-dc>`／`DCLogic` 是設計工具的執行環境，沒有匯出、也不需要；`canvas.json` 是設計工具裡各畫面的排列位置，實作用不到。
- 不被 `spec-check`／`ui-check`／`cr-check` 掃描，改動不需要 CR（引用方向：`spec ← ui ← design`，這裡屬於 design 這一層）。

## 檔案與 Screen ID

| 檔案 | Screen ID | 模組 |
|---|---|---|
| `Login.dc.html` | `s-login` | F02 |
| `Signup.dc.html` | `s-signup` | F02 |
| `BoardList.dc.html`（含 TopBar 版面） | `s-board-list` | F02 |
| `StateBoardListLoading.dc.html`／`StateBoardListEmpty.dc.html`／`StateBoardListDenied.dc.html` | `s-board-list` 的載入中／空資料／無權限狀態 | F02 |
| `BoardCreateDialog.dc.html` | `s-board-create-dialog` | F02 |
| `BoardDeleteDialog.dc.html` | `s-board-delete-dialog` | F02 |
| `Main.dc.html` | `s-canvas`＋`s-board`（看板主畫面） | F07／F01 |
| `StateBoardLoading.dc.html` | `s-board` 載入中 | F01 |
| `CanvasPanel.dc.html` | `s-canvas` 選取 item 後的面板＋`s-swimlane-list` | F07／F01 |
| `PanelStage.dc.html` | `s-stage-list` | F01 |
| `SwimlaneDeleteDialog.dc.html` | `s-swimlane-delete-dialog` | F01 |
| `StageDeleteDialog.dc.html` | `s-stage-delete-dialog` | F01 |
| `CardAddDialog.dc.html` | `s-card-add-dialog` | F01 |
| `CardDeleteDialog.dc.html` | `s-card-delete-dialog` | F01 |
| `CardDetail.dc.html` | `s-card-detail` | F01 |
| `StateCardDetailLoading.dc.html` | `s-card-detail` 載入中 | F01 |
| `AssigneePicker.dc.html` | `s-card-assignee-picker` | F02 |
| `MemberManagement.dc.html` | `s-member-management` | F02 |

沒有設計稿的畫面（`s-activity-log`、`s-cards-by-assignee`、F03／F04／F05／F06 的 Canvas item 等）：沿用上面這些檔案的視覺語彙（同樣的字體、配色、按鈕／輸入框／對話框樣式）做最簡潔可用的版面，不另外發明風格。
