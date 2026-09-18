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

## 2026-09-19 Dev 第 2 輪：修正 D-01~D-04

### 這輪處理的 D-xx

- D-01：`kanban-frontend/src/index.css` 的 `.app-topbar__logo span:nth-child(3n + 2)` 選錯格位（2、5、8）。改成明確列出 `:nth-child(3)`／`:nth-child(4)`／`:nth-child(8)`，對齊 `BoardList.dc.html`／`Login.dc.html`／`Signup.dc.html` 逐格顏色（三份設計稿的 3x3 網格配色一致）。TopBar、s-login、s-signup 共用同一個 class，一次修好三處。
- D-02：`:root` 新增 `--color-text-label: #3b4756`，把 `.field-label`、`.btn-secondary`、`.btn-sm`、`.auth-brand__note-title` 由誤用的 `--color-text-muted`（`#5c6878`，設計稿裡是副標／說明文字色）改成這個新色票。逐項核對設計稿三份 `<style>` 後，其餘顏色變數（`--color-primary`、`--color-text`、`--color-text-muted`、`--color-text-subtle`、`--color-bg*`、`--color-border*`、`--color-error-*`）數值都與設計稿一致，沒有再發現誤用。
- D-03：
  - `.auth-brand` 寬度 `380px→540px`、`padding: 40px 36px→56px 48px`，對齊 `Login.dc.html`／`Signup.dc.html`。
  - `.auth-form` `padding: 0 64px→0 88px`，移除 `max-width: 420px`（設計稿沒有這個限制）。
  - 新增 `.auth-brand__logo .app-topbar__logo`（26px 網格）、`.auth-brand__logo .app-topbar__product-name`（15px、letter-spacing 0.01em）這兩個限定在 auth 品牌欄內的尺寸覆寫；TopBar 本身的 20px／13px 不變（Review 已確認 TopBar 版面正確，只有 auth 頁的品牌欄用大一號的 Logo／字級）。
  - `.auth-brand__tagline` 字級 `24px→30px`、補上 `margin: 0 0 28px`。
  - `s-login` 補回看板示意插圖（3 個標題條＋2x3 卡片格，其中第 1、5 格為選取態），對應元件放在 `LoginPage.tsx`，樣式為新增的 `.auth-brand__illustration*` 系列 class，數值（寬 360px、圓角、陰影、卡片顏色 `#dfe9fb`／`#f1f3f7`）逐項取自 `Login.dc.html`。這塊插圖跟品牌標語、產品名稱一樣落在設計稿標「規格未定義」的範圍內，是否保留交給 OQ-T-22-fe-restyle-shell-auth-01 決定，本輪先照設計稿做出來，不阻塞任務。

### 沒有偏離設計稿的部分
除上述四項外，逐項核對過的顏色、字級、間距、Logo／頭像／按鈕尺寸都跟三份 `.dc.html` 一致，沒有其他有意偏離。

### D-04：開 OQ，並先修正一句沒有依據的宣稱
- `s-signup` 說明卡「帳號 ID 全系統唯一，之後不能改。」這句，`spec-user-membership.md` 欄位表 `user.username` 只寫『非空、全系統不可重複』，沒有任何 use case 定義帳號 ID 更新／不可更新；照 Review 指示，先把這句改成貼近欄位表原文的『帳號 ID 全系統不可重複。』，移除「之後不能改」這個沒依據的宣稱。
- 其餘落在設計稿 `.warn` 範圍內、`ui-user-membership.md` 沒有依據的文案（產品名稱占位字『[產品名稱]』、s-login 品牌標語與插圖、s-signup 說明卡標題與清單其餘兩項）維持顯示，開立 OQ-T-22-fe-restyle-shell-auth-01（等級：高，不阻塞，owner：人工），逐字引用兩份設計稿 `.warn` 註記與 `ui-user-membership.md` 對應段落，列出三個選項（維持現狀／先拿掉待補正式文案／現在請人工訂出正式文案並走 CR）。

### Check（本輪實跑）
- `cd kanban-frontend && pnpm test`：4 個測試檔、16 個測試全部通過。
- `pnpm lint`（oxlint）：無錯誤。
- `pnpm build`（tsc -b && vite build）：成功。
- `git status`：只有這輪要 commit 的 4 個檔案異動，已用 `git add` 指名檔案 commit，範圍內乾淨。

### 待確認事項
- OQ-T-22-fe-restyle-shell-auth-01（高，不阻塞，owner：人工）：品牌名稱占位字、s-login 標語與插圖、s-signup 說明卡文案要保留哪一版。

## 2026-09-19 Dev 第 3 輪：修正 D-05、D-06

### 這輪處理的 D-xx

- D-05：`s-login`／`s-signup` 品牌欄垂直結構跟設計稿不一致的問題。
  - `LoginPage.tsx`：把標語 `<p className="auth-brand__tagline">` 和插圖 `<div className="auth-brand__illustration">` 包進新增的 `<div className="auth-brand__mid">`，對齊 `Login.dc.html` 品牌欄第二個子元素（把標語與插圖包成一組，間距維持 28px 不變，因為 28px 是 `.auth-brand__tagline` 的 `margin-bottom`，本來就在這組容器內部）。
  - `LoginPage.tsx`／`SignupPage.tsx`：拿掉 `.warn` 之後，`.auth-brand` 只剩兩個 `space-between` 子元素（logo、中段內容），會被推到頂／底兩端。補一個空的 `<div className="auth-brand__spacer" aria-hidden="true" />` 當第三個子元素取代原本 `.warn` 佔的位置，讓 `space-between` 照設計稿的三段式分佈（logo 頂端、標語＋插圖或說明卡在中段、佔位元素在底部），不需要另外設定固定高度或絕對位置。
  - `AuthPage.css` 新增 `.auth-brand__mid`（`display:flex;flex-direction:column`）與 `.auth-brand__spacer`（`flex-shrink:0`，無內容無高度）。
- D-06：數值跟設計稿不一致的部分，逐項照 `Signup.dc.html`／`Login.dc.html`／`BoardList.dc.html` 改：
  - `.auth-brand__note`：`padding: 18px 20px→20px 22px`，邊框色 `var(--color-border-strong)`（`#C9D0DC`）→ 直接寫 `#CBD2DF`（設計稿這個框是專用色票，不是共用邊框色），補上 `width: 400px`。
  - `.auth-brand__note-title`：`margin: 0 0 12px→0 0 14px`。
  - `.auth-form__footer`：預設值改成登入頁的 `34px`（原本兩頁共用 `26px`），新增 `.auth-form__footer--signup`（`margin-top: 30px`）給 `SignupPage.tsx` 疊加使用，兩頁數值分開對齊各自設計稿。
  - `.auth-form__subtitle`：登入頁維持 `26px`（原本就對，不動），新增 `.auth-form__subtitle--signup`（`margin-bottom: 24px`）給 `SignupPage.tsx` 疊加。
  - TopBar：`AppShell.tsx` 新增 `.app-topbar__actions` 包住「頭像＋名字」「分隔線」「登出鍵」三者，`margin-left:auto` 從原本的 `.app-topbar__user` 移到這個新容器；`.app-topbar__actions` 的 `gap` 設 `14px`，對齊 `BoardList.dc.html` 那個 `margin-left:auto;display:flex;gap:14px` 的群組。`.app-topbar` 本身的 `gap:12px`維持不變（品牌區到右側群組之間的間距，設計稿本來就是同一個 12px 的外層 flex row）。

### 沒有偏離設計稿的部分

以上兩則 D-xx 涵蓋的數值與結構，改完後逐項跟 `Login.dc.html`／`Signup.dc.html`／`BoardList.dc.html` 的 inline style 核對過，其餘部分（D-01～D-03 修好的項目）維持不變，沒有再發現新的落差。

### 涵蓋範圍

- 對應 `s-login`／`s-signup`（`ui-user-membership.md`）與「全域導覽列」TopBar：本輪只調整版面結構（DOM 巢狀）與 CSS 數值，沒有新增／刪除任何欄位、按鈕、連結或行為分支，也沒有改變任何可及名稱（accessible name）。
- 沒有新增測試：這兩則 D-xx 都是純樣式／DOM 巢狀調整，不影響任何既有行為或可及性斷言。

### 待確認事項

- OQ-T-22-fe-restyle-shell-auth-01（高，不阻塞，owner：人工）維持現狀，本輪沒有新發現需要開的 OQ。

### Check（本輪實跑）

- `cd kanban-frontend && pnpm test`（vitest run）：4 個測試檔、16 個測試全部通過。
- `pnpm lint`（oxlint）：exit=0，無錯誤。
- `pnpm build`（tsc -b && vite build）：成功，built in 785ms。
- `git status`：只有這輪要 commit 的 5 個檔案異動（`AppShell.css`／`AppShell.tsx`／`AuthPage.css`／`LoginPage.tsx`／`SignupPage.tsx`），已用 `git add` 指名檔案 commit（`7272c5b`），範圍內乾淨。
