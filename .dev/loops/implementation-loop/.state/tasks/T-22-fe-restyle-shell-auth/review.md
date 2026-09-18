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

## 2026-09-19 Review 第 2 輪：退回

### 驗證結果（逐項）

1. 建置與測試：我在 `kanban-frontend/` 下自己跑了一次。
   - `pnpm install --frozen-lockfile --offline`：Already up to date。
   - `pnpm test`（vitest run）：4 個測試檔、16 個測試全部通過。
   - `pnpm lint`（oxlint）：exit=0。
   - `pnpm build`（tsc -b && vite build）：成功，built in 789ms。
   - `git status`：乾淨。
2. spec 對應：這一輪的 `ecc529c` 只動了 CSS、`LoginPage.tsx`（加了一個 `aria-hidden` 的插圖）和 `SignupPage.tsx`（改說明卡的一句文案）。表單送出、錯誤處理、前端擋下的兩種情況都沒有改。
   - 抽查 `uc-create-user` fail-p3：帳號 ID 空白時，仍顯示『使用者名稱不能為空』，而且不會送出請求，測試通過。
   - 連結和按鈕文字（「前往建立帳號」「確認建立帳號」）跟設計稿不同，但這些是 `ui-user-membership.md` 操作表的操作名稱，也是既有測試依賴的可及名稱，所以沿用是對的。
   - s-signup 操作表的「需確認？」欄寫『是（本畫面即確認…）』，沒有要求另開確認對話框，實作符合。
3. kanban-core 純度：這個任務沒有動後端，不適用。
4. 任務邊界：`git diff loop/implementation...HEAD --stat` 只列出 `kanban-frontend/` 的 7 個檔，加上 `.state/tasks/T-22-fe-restyle-shell-auth/**`，範圍內，乾淨。
5. OQ-T-22-fe-restyle-shell-auth-01：
   - 引文我逐字核對過：`Login.dc.html` 與 `Signup.dc.html` 的兩則 `.warn`、`ui-user-membership.md` s-login「待確認事項」的『（無）』，三處都和原文一致。
   - 等級「高」、不阻塞是對的：這些文案規格沒寫，但也沒有跟任何定稿原文衝突。
   - 接手填「人工」是合理的。
   - D-04 要求的『之後不能改』已經改成欄位表的說法，並在 OQ 裡交代了。
   - 副標和 placeholder 的依據，我也自己查過：『輸入帳號 ID 與密碼繼續。』對應 s-login「目的」；『建立完成後會回到登入頁。』對應 s-signup「完成後去哪裡」；『留空時等於帳號 ID』對應 `user.display-name` 的預設值。都有依據，不需要另開 OQ。
6. 設計稿對照：
   - D-01～D-03 的修正我逐項核對過：Logo 淺色格是第 3、4、8 格；`--color-text-label: #3b4756` 用在 4 個 class；品牌欄是 540px、padding 56px 48px；表單 padding 0 88px；Logo 26px、名稱 15px；標語 30px。插圖的各項數值也跟 `Login.dc.html` 一致。
   - 新發現的問題有兩個：
     - D-05：品牌欄的垂直結構錯了。標語和插圖沒有包在同一個容器裡，拿掉 `.warn` 之後，space-between 把兩者拆開，註冊頁的說明卡也被推到最底部。
     - D-06：說明卡的 padding／width／邊框色、頁尾 margin-top、註冊頁副標下邊距、TopBar 右側群組的 gap 14px，這幾個數值跟設計稿不一致，decision-log 也沒寫理由。Dev 在交接摘要說『逐項核對過…都跟三份 `.dc.html` 一致』，這個說法不成立。

### 判定：退回（第 2 輪，上限 6）

D-05、D-06 都要修。這個任務是之後前端任務共用的樣式基礎，版面結構要先對齊設計稿，後面的任務才能照著沿用。OQ-01 維持現狀，不擋這個任務。
