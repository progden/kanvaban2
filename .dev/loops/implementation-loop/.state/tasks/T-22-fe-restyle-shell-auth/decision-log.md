# T-22-fe-restyle-shell-auth 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Dev 第 1 輪：依設計稿改版 TopBar／登入／註冊頁

### 這輪做了什麼

依 `.dev/ui-prototype/BoardList.dc.html`（TopBar）、`Login.dc.html`（`s-login`）、`Signup.dc.html`（`s-signup`）補做 T-10／T-11 當時沒有設計稿的版面，並建立前端共用樣式基礎：

- `kanban-frontend/src/index.css`：改成設計稿的色票（主色 `#1F4BD8`、文字 `#16202C`、邊框 `#E2E6EE` 等）與字體（IBM Plex Sans／Noto Sans TC／IBM Plex Mono，`index.html` 加 Google Fonts 連結），新增共用類別：`.btn`／`.btn-secondary`／`.btn-sm`（按鈕）、`.field`／`.field-label`／`.field-input`／`.field-hint`（輸入框）、`.form-error`（錯誤訊息）、`.dialog-backdrop`／`.dialog-panel`（對話框，供之後有對話框的任務沿用）、`.avatar`、`.app-topbar__logo`／`.app-topbar__product-name`（品牌標記，TopBar 與登入／註冊頁共用）。
- `kanban-frontend/src/layout/AppShell.tsx`＋新增 `AppShell.css`：TopBar 改為品牌標記＋使用者頭像（顯示名字首字）＋顯示名字＋登出按鈕，版面依 `BoardList.dc.html` 上方 TopBar。
- `kanban-frontend/src/pages/LoginPage.tsx`／`SignupPage.tsx`＋新增 `AuthPage.css`：改為左側品牌欄（含標語／`s-signup` 的規則說明卡）＋右側表單的雙欄版面，依 `Login.dc.html`／`Signup.dc.html`。

### 決策與理由（低風險，未違反任何定案內容）

- 設計稿的 `[產品名稱]` 是設計工具的占位字（`ui-prototype/README.md`：⚠️ 規格未定義：登入頁的品牌敘述、產品名稱），不是 `.id`／`.uc`／`.warn` 之類的設計註記，屬於畫面本身的品牌標記區塊，所以原樣保留這個占位字串在 TopBar／登入／註冊頁，不自行發明產品名稱。
- 設計稿裡的 `.id`／`.uc`／`.warn` 灰字標註（例如「TopBar 本身 ⚠️ 規格未定義其版面」「成功後導向 s-board-list」）依 README 規則不做進畫面。
- `顯示名字`／`密碼` 欄位的「・選填」提示文字，為了不改變 `getByLabelText('密碼')` 等既有測試依賴的 accessible name，改成獨立 `<label>` 只包住欄位本名，提示文字放在 `<label>` 外的相鄰 `<span>`（並非用 `aria-hidden` 蓋在 label 內——testing-library 的 `getByLabelText` 用 `label.textContent`，不吃 `aria-hidden`，試過會抓不到）。純技術實作細節，不影響行為。
- 只調整版面與樣式（class 名稱、CSS、版面結構），沒有新增／刪除任何欄位、按鈕、連結或行為分支；原本的 `role="alert"`、按鈕／連結的可及名稱（accessible name）全部維持不變。

### 涵蓋範圍

- 對應 `s-login`／`s-signup`（`ui-user-membership.md`）與「全域導覽列」TopBar：本輪只動版面與樣式，未新增 Use Case 或修改任何 `uc-login`／`uc-create-user`／`uc-logout` 的行為。
- 沒有新增測試（任務說明「只改版面與樣式，不改行為；既有測試必須維持通過」，沒有新行為可測）。

### 待確認事項

無；沒有開 OQ。設計稿的產品名稱／登入頁品牌敘述屬於既有 OQ-IMPL-12 範圍內的已知規格未定義項目，沿用設計稿占位字即可，不算新的不確定性。

### Check（實際跑的指令與結果）

- `pnpm test`（`vitest run`）：4 個測試檔、16 個測試全數通過。
- `pnpm lint`（`oxlint`）：無錯誤。
- `pnpm build`（`tsc -b && vite build`）：型別檢查與打包成功。
