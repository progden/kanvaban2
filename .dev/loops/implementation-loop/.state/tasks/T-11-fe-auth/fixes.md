# T-11-fe-auth 修正任務（D-xx）

> Review 退回時追加；編號在本任務內遞增。遷移前的舊編號（全域流水號）原樣保留。

## 2026-09-18 Review 第 1 輪退回

| ID | 母任務 | 狀態 | 描述 |
|---|---|---|---|
| D-05 | T-11-fe-auth | done | 登記 OQ（高風險，`iteration-prompt.md` 第 5 節），不要只寫在 `decision-log.md`。`ui-user-membership.md` 第 45 行驗收條件寫『帳號 ID 與系統中既有帳號重複時確認建立帳號，輸入內容保留、顯示訊息，且不觸發 `uc-create-user`』，但 `SignupPage.tsx` 在帳號重複時仍會呼叫 `POST /api/users`（也就是觸發 `uc-create-user`，由後端依 fail p2 拒絕），`SignupPage.test.tsx` 的「帳號 ID 與系統中既有帳號重複」測試也沒有斷言「不觸發」。Dev 在 `decision-log.md` 自行判定這是「措辭疊加」、歸為低風險、不開 OQ，這是在詮釋驗收條件的字面意思，屬於第 5 節『spec 的 `pre`／`post`／Scenario 沒講清楚該怎麼實作』，要開 OQ。OQ 情況用「兩處矛盾並列」或「推論＋所本原文」，至少逐字引用：`ui-user-membership.md` 第 45 行（上面那句）、`.dev/conventions/ui-convention.md` 第 150 行『斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」』、`spec-user-membership.md` 第 97 行 pre p2 與第 104 行 fail p2、第 143～147 行 Scenario「帳號 ID（username）不可重複」。`state.md` 補「待確認事項」。OQ 定案前程式碼維持現狀（送 API），不要自行改成前端查重。 |
| D-06 | T-11-fe-auth | done | `LoginPage.test.tsx` 沒有完整覆蓋 `ui-user-membership.md` 第 91、92 行驗收條件。(a) 「帳號不存在時送出登入表單，顯示訊息 "帳號或密碼錯誤"」（對應 `uc-login` @fail-p1）只斷言訊息，沒有斷言『帳號 ID 與密碼欄位保留』『停留本畫面』；(b) 兩個失敗測試（fail-p1、fail-p2）都沒有斷言『TopBar 不顯示帳號名稱』。請補上這些斷言，例如 `queryByRole('button', { name: '登出' })` 為 null，或 TopBar 內沒有該 username，選法由 Dev 決定。 |
| D-07 | T-11-fe-auth | done | `SignupPage.test.tsx` 的「帳號 ID 為空時確認建立帳號」測試，沒有斷言 `ui-user-membership.md` 第 43 行的『畫面維持顯示』：送出後沒有確認「建立帳號」標題仍在，也沒有確認沒有導向 `s-login`。請補上這個斷言。 |
