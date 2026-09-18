# T-22-fe-restyle-shell-auth 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Review 第 1 輪：退回

### 驗證結果（逐項）

1. 建置與測試：我在 `kanban-frontend/` 下自己跑了一次。
   - `pnpm install --frozen-lockfile --offline`：Already up to date。
   - `pnpm test`（vitest run）：4 個測試檔、16 個測試全部通過。
   - `pnpm lint`（oxlint）：沒有錯誤。
   - `pnpm build`（tsc -b && vite build）：成功。
   - `git status`：乾淨。
2. spec 對應：這個任務只改版面，不改行為。我比對了 `LoginPage.tsx`、`SignupPage.tsx`、`AppShell.tsx` 的 diff，結果如下。
   - 表單送出流程、錯誤處理（`role="alert"`）、前端擋下的兩種情況（帳號 ID 空白、密碼超過 40 字，對應 `uc-create-user` fail p3／p1）、導覽連結、登出按鈕都沒有改。
   - 測試檔沒有改動，也全部通過。
   - 抽查 fail 情境：s-signup 帳號 ID 空白時，訊息仍是『使用者名稱不能為空』，而且不會送出請求（常數 `USERNAME_BLANK_MESSAGE` 與判斷邏輯都沒動）。
3. kanban-core 純度：這個任務沒有動後端，不適用。
4. 任務邊界：`git diff loop/implementation...HEAD --stat` 只列出 `kanban-frontend/` 的 7 個檔，加上 `.state/tasks/T-22-fe-restyle-shell-auth/**`，範圍內，乾淨。
5. OQ：Dev 沒有開任何 OQ，但產品畫面上放了設計稿自己標成「⚠️ 規格未定義」的文案（D-04）。
6. 設計稿對照（`Login.dc.html`／`Signup.dc.html`／`BoardList.dc.html`）：
   - 做對的部分：整體結構正確（TopBar 是品牌＋頭像＋名字＋分隔線＋登出；auth 頁是左側品牌欄＋右側表單）。主色、字體、輸入框、主按鈕、錯誤框的數值都跟設計稿一致。`.id`／`.uc`／`.warn`／`.hd` 這些設計註記都沒有做進畫面。
   - 有差異的部分：Logo 網格配色錯（D-01）；少了 `#3B4756` 這個色票，標籤和次要／小按鈕的顏色用錯（D-02）；auth 頁的尺寸、Logo、標語和插圖跟設計稿不一致，也沒有記下理由（D-03）。

### 判定：退回（第 1 輪，上限 6）

D-01～D-04 都要修。其中 D-01、D-02 會影響之後前端任務要沿用的共用樣式基礎，D-04 是規則要求的 OQ 沒有開。
