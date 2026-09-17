# UI 撰寫審查紀錄（Review Log）

本檔由審查輪（見 [`ui-authoring-review-prompt.md`](../prompts/ui-authoring-review-prompt.md)）追加，**只能在檔尾追加，不可修改或刪除既有紀錄**。人工也可以在此手動追加審查意見（一樣只能追加）。

---

## Review — 2026-09-17 14:37 — 9bc3403
### 範圍
- commit 區間：`aa39f82`（runtime/baseline，本檔之前沒有審查紀錄）..`9bc3403`
- 任務：T0.01、T1.01、T1.02、T1.03；下一個任務是 T1.04（不是關卡）
- runtime/last-verify.md：PASS，沒有警告；error 數 93 → 80
- 期間新增的 OQ：OQ-01

### 發現
1. **高**：`.dev/F01-basic-kanban/ui-kanban-basic.md` 第 35、36 行（`s-swimlane-list` 操作表）有 2 個 `REF-07` error，原因是「無 `fail` 定義」裡的 `fail` 包了反引號，被當成 Entity ID。T1.02 的驗收條件是「本畫面無 error」，PDCA Iteration 3 的 Check 卻寫「`grep -i swimlane-list` 無任何輸出（本畫面 0 error）」。`ui-check` 的輸出只有行號，這個 grep 本來就抓不到任何東西，所以這個驗收沒有真的達成，T1.02 被標成 `done` 是誤判。Iteration 4（T1.03）也用同樣方式核對，不過 `s-swimlane-delete-dialog`（第 58–103 行）確實沒有 error。→ **D-01**
2. **低**（不開 D）：T1.03 的任務描述寫「含目的泳道選擇」，但 spec `uc-delete-swimlane` 的 post 是「若該 `swimlane` 內有 `card`，一併被刪除」，沒有轉移卡片的行為。執行輪依 spec 做、沒有自行發明「目的泳道」欄位，而且在 PDCA 記了理由，處理正確。T 任務的描述要不要改，由人工決定。
3. **低**（不開 D）：`s-swimlane-delete-dialog` 刪除空泳道時也會跳出對話框，但 spec 的 Scenario 名稱是「刪除包含卡片的 Swimlane 需要確認」，Scenario「刪除空的 Swimlane」並沒有確認步驟。刪除屬於不可逆的 post，一律要求確認是 UI 可以自己決定的範圍，沒有改到業務結果；只是記在這裡，讓人工知道有這個選擇。
4. **低**（不開 D）：骨架的類型跟任務描述不一致：`s-board` 是「儀表板」（任務寫「流程／看板主畫面」，PDCA Iteration 2 有寫理由）；`s-card-add-dialog` 是「對話框」、`s-card-detail` 是「詳情」（任務都寫「表單」，PDCA 沒寫理由）。這三個都在七種合法類型內，也還是「未討論」，交給 T1.06～T1.08 定案時確認。
5. **低**（不開 D）：commit scope 不一致：`badd5ba` 用 `(ui-kanban-basic)`，`cfc175e`、`bd633d8` 用 `(kanban-basic)`。Iteration 0 寫的是 `[spec/design](ui-<模組名>)`，`git-convension.md` 規定用模組名。已經提交的沒辦法改，之後統一即可。

有檢查、沒發現偏差的面向：
- 不定義新概念：來源 `swimlane.name`、`board`／`swimlane`／`card` 的關係都存在於 spec 名詞表；觸發欄的 4 個 uc 都存在；角色只有 `r-user`。「順序」「卡片數」是由關係與 post 推出來的，不是新的 attribute。
- 不寫業務結果：「失敗時」都是「依 `uc-xxx` p1」加上呈現方式，沒有抄錯誤訊息原文。
- 不寫排版視覺：沒有顏色、間距、元件選型。
- 狀態誠實性：`s-swimlane-list` 標「討論中」，`⚠️` 對應 OQ-01；`s-swimlane-delete-dialog` 標「已定案」，八段齊全，沒有 `⚠️`。
- 需確認判斷：新增、重新命名、排序標「否」，都是可逆的；確認刪除標「是」，對應 post 刪除 `card`，是不可逆的。
- OQ 品質：OQ-01 用【推論】寫法，有逐字引用 Background、有 `[Level:]` 標記，沒有說服性字眼。
- 逐字引用：`verify-quotes.py` 通過。
- 跨模組一致：這段期間沒有跨模組引用。
- 任務完成度：T0.01、T1.01、T1.03 達成；T1.02 沒有達成（見第 1 點）。

### 關卡摘要
下一個任務是 T1.04，不是關卡，這次不需要填。

## Review — 2026-09-17 17:10 — dc4cf62
### 範圍
- commit 區間：`9bc3403`（上次審查）..`dc4cf62`，共 9 個 commit
- 任務：D-01、T1.04、T1.05、T1.06；下一個任務是 T1.07（不是關卡）
- runtime/last-verify.md：PASS，沒有警告；error 數 52 → 51
- 期間新增的 OQ：OQ-02～OQ-06

### 發現
1. **中**：T1.06 的驗收條件是「`ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)` 對本畫面無 error」，但 `s-board`（第 209～273 行）在預設指令下有 12 個 REF-07，都是引用 F02 ID 造成的；這個條件照 token 定義沒有達成。PDCA Iteration 8 改用 `--spec .dev/F02-user-membership/spec-user-membership.md` 核對，我重跑一次，`s-board` 確實沒有 error，`uc-assign-card-owner-by-drag`、`card.assignees`、`board-membership` 也都存在於 F02 spec。內容本身沒問題，是驗證工具不支援跨模組引用，所以這項不開 D，列為「需人工事後處理」（見下方）。PDCA 有寫明替代的核對方式，沒有隱瞞。
2. **中**：OQ-01／OQ-02 被推翻，卻沒有留下紀錄；`s-swimlane-list`／`s-stage-list` 寫「已依 `s-board` 定案結果更新」，但 `s-board` 其實是「討論中」，屬於狀態誠實性問題。進入路徑改成從 `s-board` 導覽進入，這個選擇本身在 UI 可以自己決定的範圍內，也對應 OQ-01 的選項 2。→ **D-03**
3. **中**：有些段落寫的是業務結果或領域狀態，不是呈現方式：`s-stage-delete-dialog` 驗收條件寫「卡片已移動到選擇的目的 Stage」；`s-board` 操作表的「成功後」與驗收條件寫「卡片負責人追加該成員（若原本已是負責人則維持不變）」；`s-board` 狀態段「錯誤」前後矛盾。→ **D-02**
4. **低**（不開 D）：PDCA Iteration 8 記的 commit 是「`36...`」，實際是 `22422f9`；時間戳 14:54 比 Iteration 7 的 16:50 還早。PDCA 不可回改，只記在這裡。
5. **低**（不開 D）：`s-board` 新增「管理 Swimlane」「管理 Stage」兩個純導覽操作（觸發欄是 `—`），spec 沒有這兩個操作，但也沒有新增 uc 或業務結果，屬於 UI 導覽可以自己決定的範圍。D-03 會把這個決定寫進 OQ，供人工稽核。
6. **低**（不開 D）：OQ-06 是真的矛盾：F02 `uc-assign-card-owner-by-drag` 的 `roles: [r-user]`，但 F02 角色定義表只有 `r-system-user`／`r-board-owner`／`r-board-member`。我照 spec 核對過，OQ 的寫法合規，三個選項是固定的保留 A、保留 B、缺區分條件。這要回饋 SA 走 CR，loop 處理不了。

有檢查、沒發現偏差的面向：
- 不定義新概念：`stage.name`（spec 沒有非空限制，ui 驗證欄寫「—」，一致）、`stage.role`（enum 與至多一個 START／DONE 的限制都跟 spec 一致）、`swimlane.name`（非空）、`card.title`、`card.due-date`、F02 的 `card.assignees`／`board-membership` 都存在於 spec。
- 不寫排版視覺：沒有顏色、間距、元件選型；OQ-05 特地不預設成員清單要放在哪裡，處理正確。
- 狀態誠實性：`s-stage-delete-dialog`、`s-board` 標「討論中」，`⚠️` 分別對應 OQ-03，以及 OQ-04／05／06；已定案的兩個畫面沒有 `⚠️`，但有第 2 點的問題。
- 需確認判斷：`uc-delete-stage` 標「是」（刪除 `stage` 不可逆）；拖曳移動、拖曳指派、設定角色標「否」（都可以再操作一次還原）。判斷合理。
- 失敗時：`uc-add-stage`／`uc-rename-stage`／`uc-reorder-stage`／`uc-set-stage-role`／`uc-move-card-*`／`uc-assign-card-owner-by-drag` 的 `fail` 確實都是 `{}`；`uc-delete-stage` 引用 p1，沒有抄訊息原文。
- OQ 品質：OQ-02～06 都有 `[Level:]` 標記，用的是【推論】、【引用原文】、【矛盾】三種合法寫法，沒有說服性字眼；OQ-03 把可選的既有類別都列出來了。
- 逐字引用：`verify-quotes.py` 回傳 0。
- 跨模組一致：F02 的 ID 都存在；`s-board-list` 還沒定案，OQ-04 選擇暫不引用，處理正確。沒有重複定義的畫面。
- 任務完成度：D-01 達成（整檔已經沒有 `fail` 被反引號包住）；T1.04、T1.05 本畫面沒有 error、八段齊全；T1.06 見第 1 點，操作表確實有 `uc-assign-card-owner-by-drag` 那一列。

需人工事後處理：
- `ui-check <檔>` 只會自動載入同目錄的 spec，所以 F01 引用 F02 ID 一定會報 REF-07。依目前的 token 定義，T1.10 的 `ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0` 和 T2.11 的同一條件，在 `s-board`／`s-card-detail` 引用 F02 的情況下都達不成。要嘛讓 `ui-check` 支援跨模組 spec，要嘛人工把這兩個任務的驗收條件改成帶 `--spec`。審查輪不能改腳本，也不能改 T 任務。
- OQ-06（F02 `roles` 與角色表矛盾）要走 CR 修正 F02 spec。

### 關卡摘要
下一個任務是 T1.07，不是關卡，這次不需要填。

## Review — 2026-09-17 15:09 — ed3c8d8
### 範圍
- commit 區間：`dc4cf62`（上次審查）..`ed3c8d8`，共 9 個 commit
- 任務：D-02、D-03、T1.07、T1.08；下一個任務是 T1.09（不是關卡）
- runtime/last-verify.md：PASS，沒有警告；error 數 38 → 25
- 期間新增的 OQ：OQ-07～OQ-09

### 發現
1. **中**：`s-card-detail` 操作表「儲存變更」的「成功後」（第 352 行）寫「依 `uc-edit-card` post：`card.description`、`card.due-date`、`card.labels` 更新為編輯內容」，驗收條件（第 365 行）寫「描述、截止日期、標籤更新為輸入內容」。這兩處都是在重述 post 的資料更新，跟 D-02 是同一類問題。→ **D-04**
2. **中**：`s-card-add-dialog` 已標「已定案」，但驗收條件第 314、316 行用「不建立新卡片」「不建立卡片」當斷言，這是領域狀態（`uc-add-card` fail p1），不是畫面元素；資料表第 296、297 行的說明欄寫「建立後 `card.swimlane`／`card.stage` 設為此…」，也是在重述業務結果。→ **D-05**
3. **低**（不開 D）：PDCA Iteration 12 寫「沿用 T1.06 已定案的 `s-board` 操作表」，但 `s-board` 的狀態仍是「討論中」（D-03 才剛修過同一類措辭）。這只出現在 PDCA，ui 檔本身沒有這樣寫；PDCA 不可回改，只記在這裡。
4. **低**（不開 D）：`s-card-detail` 的「新增留言」需確認標「否」。spec 沒有刪除留言的 uc，所以這個 post 實際上沒辦法還原；不過 spec 的 Scenario「為卡片新增留言」也沒有確認步驟，而 `ui-convention.md` 對「送出」類操作要不要確認的寫法有解讀空間（之前的新增類操作也一律標「否」）。這屬於 UI 可以自己決定的範圍，交由人工判斷。
5. **低**（不開 D）：`s-card-detail` 的「卡片標題」寫「標題不可於此編輯」。這是從 `uc-edit-card` post 只列 `card.description`／`card.due-date`／`card.labels`，以及 F01 沒有任何更新 `card.title` 的 uc 推出來的，說明欄有寫依據，沒有發明行為。
6. **低**（不開 D）：`s-card-add-dialog`／`s-card-detail` 的類型依任務描述從「對話框」「詳情」改成「表單」，回應了上次審查第 4 點；T1.07 的 PDCA 有寫理由，T1.08 沒有，但兩者都是合法類型。

有檢查、沒發現偏差的面向：
- 不定義新概念：`card.title`、`card.description`、`card.due-date`、`card.labels`、`swimlane.name`、`stage.name` 都存在於 spec 欄位表；留言沒有對應的 Attribute ID，執行輪沒有發明 `comment` 實體，而是標 `⚠️` 並開 OQ-09，處理正確；負責人欄位標 `⚠️`（OQ-08），也沒有提前引用 F02 的 `s-card-assignee-picker`。
- 不寫業務結果（失敗時）：`uc-add-card` 引用 p1，沒有抄「卡片標題不可為空」原文；`uc-edit-card`／`uc-add-comment` 的 `fail` 確實是 `{}`，寫「不適用」正確。
- 不寫排版視覺：沒有顏色、間距、元件選型。
- 狀態誠實性：`s-card-add-dialog` 標「已定案」，八段齊全、沒有 `⚠️`（寫法問題見第 2 點）；`s-card-detail` 標「討論中」，兩個 `⚠️` 都對應 OQ-08／OQ-09。D-03 已經拿掉「定案結果更新」的措辭，改成引用 OQ-07，並明講 `s-board` 仍是討論中。
- 需確認判斷：「確認新增」「儲存變更」標「否」，後者可以再編輯還原；「新增留言」見第 4 點。
- OQ 品質：OQ-07 用【推論】寫法，明講「推翻 OQ-01、推翻 OQ-02」，【覆蓋】以外的列都不需要「暫不覆蓋」；OQ-08／OQ-09 用【引用原文】寫法，逐字引用 CR-002 與 `uc-add-comment` post；三列都有 `[Level:]` 標記，沒有說服性字眼。
- 逐字引用：`verify-quotes.py` 回傳 0。
- 跨模組一致：這段期間沒有新增跨模組 ID 引用；`s-card-detail` 刻意用純文字提 `s-card-assignee-picker`，避免引用還不存在的 ID。沒有重複定義的畫面。
- 任務完成度：D-02、D-03 的驗收字串都核對過，確實達成；T1.07、T1.08 的畫面範圍（第 274～320 行、第 322～372 行）沒有 error，剩下的 25 個 error 分別是 `s-board` 的 12 個跨模組 REF-07，以及還沒動工的 `s-card-delete-dialog`（第 373 行）的 13 個；T1.08 待確認事項有負責人的 `⚠️`，OQ 檔也追加了 OQ-08。

需人工事後處理：
- 上次審查列的「`ui-check` 不支援跨模組 spec，T1.10／T2.11 的 `ui-check(...)=0` 達不成」仍然存在，T1.10 快到了，要優先處理。

### 關卡摘要
下一個任務是 T1.09，不是關卡，這次不需要填。

## Review — 2026-09-17 18:00 — 106b812
### 範圍
- commit 區間：`ed3c8d8`（上次審查）..`106b812`，共 22 個 commit
- 任務：D-04、D-05、T1.09、T1.10（含 OQ-10 blocked 及人工解除）、T2.01、T2.02、T2.03；下一個任務是 T2.04，不是關卡
- 另有人工 commit：F07 spec 重寫、F07 納入 loop 範圍、`ui-authoring-tools.py` 的 `findings()` 改成一律帶全部 spec。這些不屬於 ui 檔內容，本次只確認它們沒有動到 ui 檔
- runtime/last-verify.md：PASS，沒有警告；F02 的 error 數 104 → 92
- 期間新增的 OQ：OQ-10～OQ-13

### 發現
1. **高**：`ui-user-membership.md` 第 84 行（`s-login` 操作表「登出」）的「失敗時」欄寫「（`uc-logout` 無 `fail`）」，觸發 `REF-07` error，和 D-01 是同一個錯誤。T2.03 的 PDCA Check 寫「`s-login`／`s-signup` 兩者皆 0 筆 error」，這和實際結果不符：它用畫面名稱篩選 `ui-check` 輸出，但輸出只有行號，所以抓不到這一行。D-01 已經寫明要改用行號範圍比對，這次又沒照做，T2.03 標 `done` 的依據不成立。last-verify 只看總數有沒有下降，所以也沒抓到。→ **D-06**
2. **中**：`s-signup` 驗收條件第 43～45、47 行用領域狀態當斷言：「建立新帳號」「該帳號不應該被建立」「不應該建立新的帳號」「該帳號的顯示名字等於帳號 ID」。`s-login` 第 95、96 行的「且不視為登入成功」也是同類問題，而且沒辦法對著畫面打勾。這和 D-02／D-04／D-05 是同一類，執行輪還沒把這條規則內化。→ **D-07**
3. **中**：`s-card-delete-dialog`（已定案）的「中途放棄會怎樣」、操作表「取消」的「成功後」、驗收條件「取消後…」三處都寫「卡片不變」。這是 `uc-delete-card` fail p2 的業務結果（「不刪除，該 `card` 仍存在於看板中，資料不變」），不是呈現方式。→ **D-08**
4. **低**（不開 D）：`s-signup`「確認建立帳號」的需確認標「是」，理由是「`user` 目前無刪除 Use Case，建立後無法復原」。`s-card-detail`「新增留言」的情況相同（沒有刪除留言的 uc），卻標「否」（上次審查第 4 點）。兩處判準不一致，而且 spec 的 Scenario「建立帳號時密碼可以留白」也沒有確認步驟。這屬於 UI 可以自己決定的範圍，交由人工統一。
5. **低**（不開 D）：OQ-13 的選項 1 寫「任務清單 T2.02 尚未定案」，但 `s-board-list` 對應的是 T2.04，任務編號引用錯誤。OQ 檔不可由審查輪修改，記在這裡。
6. **低**（不開 D）：OQ-10 的「採用」欄仍寫「T1.10 標 `blocked`，等待人工決定」，「決議方式」欄也還是 `blocked`。人工在 Iteration 17 已經解除，但 OQ 表沒有更新。另外 OQ-10 的「模組」欄填 `user-membership`，實際受影響的是 F01 `s-board` 的檢查。
7. **低**（不開 D）：PDCA 的編號從 Iteration 19 直接跳到 21，缺少 20。T2.01 的 PDCA 寫「Commit：`e...`」，沒有填實際的 hash（應為 `809fa0d`）。PDCA 不可回改，記在這裡。
8. **低**（不開 D）：`s-login` 的「登出」實際上是從其他已登入畫面觸發，不在本畫面顯示，但任務 T2.03 明確要求併入本畫面的操作表，執行輪也在「看得到」欄說明了，所以不算偏差。到 T2.04 以後，已登入的畫面（例如 `s-board-list`）是否要導向 `s-login`，需要在 T8.01 的 DS-07 一起確認。

有檢查、沒發現偏差的面向：
- 不定義新概念：`user.username`、`user.display-name`、`user.password`、`r-system-user`、`uc-create-user`、`uc-login`、`uc-logout` 都存在於 F02 spec；「TopBar」逐字出自 `uc-login` post，不是發明的元件；`s-card-delete-dialog` 只引用 `card.title`、`r-user`、`uc-delete-card`。
- 不寫業務結果（失敗時）：`uc-create-user` 與 `uc-login` 都用「依 p1／p2」引用，沒有抄「帳號或密碼錯誤」原文。
- 不寫排版視覺：沒有顏色、間距、元件選型。
- 狀態誠實性：`s-signup`／`s-login` 標「討論中」，所有 `⚠️` 都對應到 OQ-11～OQ-13；`s-card-delete-dialog` 標「已定案」，八段齊全、沒有 `⚠️`（寫法問題見第 3 點）；D-04、D-05 的修正都核對過，驗收字串確實達成。
- 需確認判斷：`uc-delete-card` 刪除後無法復原，標「是」正確；`uc-login`／`uc-logout` 標「否」正確。
- 角色一致（DS-05）：兩個畫面的角色都是 `r-system-user`，和 uc 的 `roles` 一致。
- OQ 品質：OQ-11、OQ-13 用【引用原文】，OQ-10、OQ-12 用【推論】，都有 `[Level:]` 標記，沒有說服性字眼。
- 逐字引用：`verify-quotes.py` 回傳 0。
- 跨模組一致：`s-login` 導向 `s-board-list`（F02 已有這個標題），T1.10 帶全部 spec 後 F01 為 0 error、22 個 warning，全部是其他模組的 DS-06；沒有重複定義的畫面。
- 任務完成度：T1.09、T1.10、T2.01、T2.02 符合驗收條件；T2.03 見第 1 點。

需人工事後處理：
- 「同 T1.02 模式」的驗收條件（「本畫面無 error」）沒有機械 token，`accept-check` 驗不了，執行輪已經兩次用畫面名稱篩選而漏掉錯誤（D-01、D-06）。建議在 `ui-authoring-tools.py` 新增依畫面行號範圍計算 error 的 token，或讓 verify 檢查「已定案／討論中畫面的行號範圍內沒有 error」。
- 第 4 點（新增類操作的需確認判準要統一）、第 5、6 點（OQ-10／OQ-13 的內容要更正）。

### 關卡摘要
下一個任務是 T2.04，不是關卡，這次不需要填。

## Review — 2026-09-17 19:45 — a78d5af
### 範圍
- commit 區間：`106b812`（上次審查）..`a78d5af`，共 24 個 commit
- 任務：D-06、D-07、D-08、T2.04、D-10（`doing`）、T2.05、T2.06、T2.07；`actionable` 下一個是 D-10（`doing`），之後是 T2.08，都不是關卡
- 人工 commit（依人工討論進行）：`ea76a2f`（F01 spec 補 `comment` 實體）、`7e0a0a0`（F02 spec 補 `board.name`、修正角色筆誤）、`e7f6ee8`／`e845de5`（依討論結果更新 ui 檔）、`fa9d93a`／`754bcc1`（工具修正）、`b66d256`（解決多個 OQ）。spec 的修改都有寫進變更紀錄，本次不審查 spec 本身
- runtime/last-verify.md：PASS，沒有警告；`error-count(all)` 從 52 降到 39，剩下的 error 全部來自還沒動工的 `s-card-assignee-picker`、`s-cards-by-assignee`、`s-activity-log` 骨架
- 期間新增的 OQ：OQ-14～OQ-26

### 發現
1. **中**：`s-member-management`（討論中）的操作表「成功後」「失敗時」重述了業務結果，包括「成員數增加 1」、「這些 `card` 的負責人欄位移除該成員」、「不建立新的 `board-membership`」、「`board-membership` 角色不變」，驗收條件也有「角色不變」。資料表「邀請對象帳號」的驗證寫「須為系統中已存在帳號」，但 `uc-invite-member` pre 只有 p1、p2，spec 沒有這條限制，卻沒有標 ⚠️。「移除成員」的需確認欄說「待確認事項提醒需要一則確認提示」，但待確認事項段落裡沒有這一條。→ **D-11**
2. **中**：`s-board-delete-dialog` 的「成功後」、「完成後去哪裡」、驗收條件都寫了「不再存在」，「中途放棄」和驗收條件寫了「Board 與其資料不變」。`s-board-create-dialog`（**已定案**）的「成功後」和驗收條件寫了「操作者對該 Board 角色為 Owner」。`s-board-list` 的「無權限」和驗收條件寫了「操作者仍無法存取該 Board」。這些都是 post 的領域結果。這是同一類錯誤第六次出現（D-02／D-04／D-05／D-07／D-08），執行輪還沒把這條規則內化。→ **D-12**
3. **低**：`s-board-list` 待確認事項的第一條 ⚠️ 自己寫著 OQ-16「已由人工修正…解除」，但仍然標 ⚠️，狀態不誠實。→ 併入 **D-12**
4. **低**：`s-signup`／`s-login` 的 D-10 內容已經完成，沒有 ⚠️，待確認事項也是「（無）」，但狀態還是「討論中」。→ **D-13**
5. **低**（不開 D）：D-10 的驗收條件 `ui-check(F02)=0` 實際上要等 T2.08～T2.10 完成才能達成，但它的依賴欄是「—」、狀態是 `doing`，所以 `actionable` 每一輪都會先回傳 D-10。Iteration 28 之後，執行輪都是自己判斷跳過它。這不是執行輪的錯，但每一輪都會浪費判斷，也可能被誤當成卡住。**需人工事後處理**：把 D-10 改成 `done`，或把依賴改成 T2.10（D 列只有人工能改）。
6. **低**（不開 D）：PDCA 的 error 數口徑不一致。Iteration 29 寫「ui-check 52（原 65）」和「error-count 52」，Iteration 30 寫「ui-check 52 → 52」和「error-count 39（原 52）」。實際上，單檔 `ui-check` 包含 13 筆跨模組 `s-board`／`s-swimlane-list`／`s-stage-list` 的 REF-07，`error-count` 則會帶入全部 ui 檔，所以不包含這 13 筆。Iteration 30 的「52 → 52」看起來像填完八段卻沒有減少 error，容易誤讀。PDCA 不可回改，這裡只做記錄。
7. **低**（不開 D）：`s-board-delete-dialog` 顯示 Swimlane 數、Stage 數、卡片數，spec 的 Scenario 只在 Given 裡提到數量，沒有要求要顯示。不過 `ui-convention.md` 允許寫衍生計數，而且和 F01 `s-swimlane-delete-dialog` 的做法一致，所以不算發明新概念。「空資料：不適用」依據的是 F01 關係表中 `board`→`swimlane`／`stage` 的 min=1，已核對正確。

有檢查、沒發現偏差的面向：
- 不定義新概念：`board.name`（人工補進 spec 第 31 行）、`board-membership.role`、`user.username`、`user.display-name`，以及 `uc-create-board`、`uc-invite-member`、`uc-change-member-role`、`uc-remove-member`、`uc-reject-invite-by-member`、`uc-reject-role-change-by-member`、`uc-delete-board`、`uc-view-board-list`、`uc-reject-board-access-by-nonmember`，都存在於 F02 spec。「邀請對象帳號」的例外見第 1 點。
- 不寫排版視覺：沒有顏色、間距、元件選型。
- 狀態誠實性：`s-member-management`、`s-board-delete-dialog` 的 ⚠️ 都對應到 OQ-22～OQ-26；`s-board-create-dialog` 標「已定案」，八段齊全、沒有 ⚠️（寫法問題見第 2 點）；F01 `s-stage-delete-dialog` 改成已定案，是人工依 OQ-03 決議處理的。
- 需確認判斷：`uc-delete-board` 刪除後無法復原，標「是」正確。`uc-create-board` 可以用 `uc-delete-board` 復原，標「否」合理。`uc-change-member-role` 的 post 沒有定義降級，標 ⚠️（OQ-24）是誠實的處理。
- 角色一致（DS-05）：`uc-create-board`／`uc-delete-board` 的角色是 `r-board-owner`，兩個拒絕 uc 的角色是 `r-board-member`，和各畫面的角色表一致。
- OQ 品質：OQ-22～OQ-26 都用【推論】或【引用原文】，都有 `[Level:]`，沒有說服性字眼；OQ-17～OQ-21 標明是人工決策，而且寫明推翻了哪些 OQ。
- 逐字引用：`verify-quotes.py` 回傳 0。
- 跨模組一致：`s-board`、`s-swimlane-list`、`s-stage-list` 都存在於 F01。帶入全部 ui 檔後，這 13 筆 REF-07 會消失。沒有重複定義的畫面。
- 任務完成度：D-06（沒有 REF-07）、D-07（禁用字串都已移除）、D-08（「卡片不變」已移除，F01 為 0 error）都已核對，確實達成。T2.05～T2.07 的八段都齊全，error 也都落在尚未動工的畫面。D-10 的內容驗收都達成，只剩整檔歸零這一項（見第 5 點）。

需人工事後處理：
- 第 5 點：D-10 的依賴或狀態需要人工調整。
- 上次審查第 4 點（新增類操作的需確認判準要統一）仍然沒有處理。

### 關卡摘要
下一個任務是 D-10（`doing`），之後是 T2.08，不是關卡，這次不需要填。

## Review — 2026-09-17 20:10 — 9992fd2
### 範圍
- commit 區間：`a78d5af`（上次審查）..`9992fd2`，共 8 個 commit（含上次審查的 commit `ebc3a36`）
- 任務：D-11、D-12、D-13（都已標 `done`）；`actionable` 下一個是 D-10（`doing`），之後是 T2.08，都不是關卡
- runtime/last-verify.md：PASS，沒有警告；`error-count(F02)` 維持 39，其他檔都是 0
- 期間新增的 OQ：OQ-27

### 發現
未發現需要開 D-xx 的偏差。以下是記錄用的低嚴重度事項：

1. **低**（不開 D）：`ui-convention.md` 第 99 行的範例本身就寫「中途放棄會怎樣：關閉對話框，泳道與卡片不變」，可見規範允許「中途放棄」用「X 不變」描述。D-08（刪掉 `s-card-delete-dialog` 的「卡片不變」）和 D-12（把 `s-board-delete-dialog` 的「Board 與其資料不變」改成「回到進入前的畫面」）的要求，比規範範例還嚴格。F01 的 `s-swimlane-delete-dialog`／`s-stage-delete-dialog`、F02 的 `s-signup`／`s-board-create-dialog`／`s-member-management` 還保留這種寫法，所以兩份 ui 檔的寫法不一致。改寫沒有改變業務語意，不算錯誤。**需人工事後處理**：決定「中途放棄」段是否允許寫「X 不變」，再決定要不要統一；前幾次審查把它當成偏差，判準偏嚴。
2. **低**（不開 D）：`s-board-create-dialog`（已定案）的「目的」寫「並自動成為該 Board 的 Owner」，內容出自 `uc-create-board` post 第 2 條。D-12 已經從操作表和驗收條件移除同樣的敘述，但「目的」段沒有被驗收字串「角色為 Owner」涵蓋，所以留下來了。規範沒有限制「目的」段的斷言主詞，這句也只是說明畫面用途，所以不開 D，只做記錄。
3. **低**（不開 D）：`s-member-management` 的「移除成員」成功後仍然以「依 post：」開頭，但後面的內容「目標成員自清單移除」已經是畫面呈現，只是前綴用詞不精確。
4. **低**（不開 D）：`s-board-list` 的「無權限」情況，是使用者「直接開啟不屬於自己的 Board」。這時使用者可能不是從本畫面出發（例如直接輸入網址），寫「停留本畫面」不一定成立。這和 D-09（F07 導向）、DS-07 的導覽確認有關，建議在 T8.01 一起看。
5. **低**（不開 D）：PDCA 的時間順序不一致。Iteration 33（D-12）記錄的時間是 20:05，但下一則 Iteration 34（D-13）是 19:47。PDCA 不能回頭修改，這裡只做記錄。
6. **低**（不開 D）：OQ-27 的選項 2 用「例如以 Email 邀請」舉例，spec 裡沒有 Email 這個概念。不過這只是選項中的舉例，而且沒有被採用，畫面上也沒有寫進去，所以不算發明新概念。

有檢查、沒發現偏差的面向：
- 不定義新概念：這次的修改沒有新增 attribute、角色或 uc；OQ-27 引用的 `uc-invite-member` pre p1／p2 和 spec 一致。
- 不寫業務結果：D-11、D-12 列出的禁用字串都已經不在檔案裡。`s-board-create-dialog` 的「成功後」改成「`s-board-list` 顯示新建立的 Board」，和同一個畫面「完成後去哪裡」的內容一致。
- 不寫排版視覺：沒有新增顏色、間距或元件選型。
- 狀態誠實性：`s-signup`（第 5～50 行）、`s-login`（第 51～101 行）已改成「已定案」，範圍內沒有 `⚠️`，待確認事項都是「（無）」，D-13 確實達成。`s-member-management` 新增的 ⚠️ 有對應到 OQ-27，而且「資料」段和「待確認事項」各有一處，前後一致。`s-board-list` 已刪除過時的 OQ-16 ⚠️，剩下的 ⚠️ 對應 OQ-17／OQ-18／D-09。
- 需確認判斷：「移除成員」刪掉「待確認事項提醒」之後，仍保留「依 pre p2 需先顯示確認訊息並告知張數」，這和 `uc-remove-member` pre p2「若目標成員仍是 `card` 負責人，使用者已確認移除」相符。
- OQ 品質：OQ-27 使用【引用原文】，有 `[Level:]`，沒有說服性字眼，「採用」是維持討論中、標 ⚠️。
- 逐字引用：`verify-quotes.py` 回傳 0。
- 跨模組一致：這次沒有新增跨模組引用，也沒有重複定義的畫面。
- 任務完成度：已用 grep 核對 D-11、D-12、D-13 的驗收字串，都確實達成；`error-count(F02)`=39，沒有增加。Iteration 32 補上 Iteration 31 漏掉的 ui-check 結果行，Check 和實際結果相符。

需人工事後處理：
- 第 1 點：「中途放棄」段是否允許寫「X 不變」，需要決定判準，並統一 F01／F02 的寫法。
- 上次審查第 5 點仍然沒有處理：D-10 的狀態或依賴需要人工調整，目前每一輪 `actionable` 都會先回傳 D-10。
- 前兩次審查提出的「新增類操作的需確認判準要統一」仍然沒有處理。

### 關卡摘要
下一個任務是 D-10（`doing`），之後是 T2.08，不是關卡，這次不需要填。

## Review — 2026-09-17 22:30 — 5b2d292
### 範圍
- commit 區間：`9992fd2`（上次審查）..`5b2d292`，共 9 個 commit（含上次審查的 commit `8f03d9f`）
- 任務：T2.08、T2.09、T2.10、T2.11、D-10（都已標 `done`）；`actionable` 下一個是 T3.01，不是關卡
- runtime/last-verify.md：**FAIL**，有 2 項：commit `67e4907` 的訊息少了 `(ui-<模組名>)` scope；PDCA Iteration 38 的標題寫成「D-10／T2.11」，不符合格式。沒有警告。`ui-check(all)`=0 error、20 warning
- 期間新增的 OQ：OQ-28、OQ-29

### 發現
1. **中**：`s-card-detail`（F01，已定案）的「從哪裡進來」只寫了 `s-board`，「關閉」也固定寫回到 `s-board`。但 T2.09 的 `s-cards-by-assignee` 操作表會開啟 `s-card-detail`，兩份 ui 檔的導覽對不上。DS-07 只檢查畫面有沒有被導向，所以抓不到這個問題。→ **D-14**
2. **中**：`uc-member-add-card` 仍然沒有任何畫面觸發（DS-06）。T2.07 任務欄寫「改在 T2.08／T1.06 標註引用」，但後續任務都沒有處理。T2.11 的 PDCA Check 說剩下的 20 個 warning「皆對應既有 OQ」，這個說法和實際不符：這一筆沒有對應的 OQ。→ **D-15**
3. **中**：`s-card-assignee-picker`（已定案）的「空資料」寫「依 `uc-view-card-assignees` post，候選清單全部維持未勾選」，但 post 原文沒有提到勾選狀態，屬於把推論掛在 post 名下。「儲存變更」的「成功後」寫「卡片縮圖同步顯示」，但卡片縮圖不在這個畫面，也不在它回到的畫面。→ **D-16**
4. **低**（不開 D）：Iteration 35（T2.08）的 Check 寫「`s-card-assignee-picker` 本身僅剩 1 個 warn」，但這個畫面當時還有一筆 `roles` 加反引號造成的 REF-07 error。Iteration 36 才承認這筆 error 是「T2.08 遺留」，T2.11 也已經修正。Check 和實際結果不符，而且又是 D-01／D-06 記錄過的陷阱：用名稱篩選 `ui-check` 輸出，會漏掉只有行號的 error。PDCA 不可回改，這裡只做記錄。
5. **低**（不開 D）：`s-card-detail` 的角色表同時有 F01 `r-user` 和 F02 `r-board-member`。F02 的三個負責人 uc 的 roles 只有 `r-board-member`，但「負責人」欄位放在 `r-user` 可見的「卡片完整內容」裡。這是 F01 和 F02 角色體系並存造成的，spec 沒有定義兩者的對應關係，目前不開 D。
6. **低**（不開 D）：`s-cards-by-assignee` 的「卡片標題」是唯讀欄位，但「驗證／格式」欄寫了「非空」。這是欄位表的限制，不是畫面輸入驗證，寫在這裡不算錯，但沒有必要。
7. **低**（不開 D）：`s-card-assignee-picker`「中途放棄會怎樣」寫「卡片負責人維持進入前的樣子」，是「X 不變」的寫法。上次審查第 1 點已經把這種寫法列為需要人工判斷，這次不重複開 D。

有檢查、沒發現偏差的面向：
- 不定義新概念：`card.assignees`、`board-membership`、`user.display-name`、`card.title`（F01），以及 `uc-set-card-assignees`、`uc-list-card-assignee-candidates`、`uc-view-card-assignees`、`uc-list-cards-by-assignee`、`uc-view-board-activity-log`，都存在於 spec。
- 不寫排版視覺：沒有新增顏色、間距或元件選型。
- 狀態誠實性：`s-card-assignee-picker`、`s-card-detail` 標「已定案」，範圍內沒有 ⚠️，但寫法有問題，見第 1、3 點。`s-cards-by-assignee` 標「討論中」，兩處 ⚠️ 都對應 OQ-28。`s-activity-log` 維持「未討論」，並依 T2.10 分支規則引用 spec 的「本情境目前尚未實作」，這個處理是誠實的。
- 需確認判斷：`uc-set-card-assignees` 的 post 只有覆寫 `card.assignees` 和活動紀錄，可以重新設定回原狀，標「否」合理。
- 角色一致（DS-05）：三個畫面的角色都是 `r-board-member`，和 uc roles 一致（spec 第 78 行的變更紀錄已經修正先前的筆誤）。
- OQ 品質：OQ-28 使用【推論】，OQ-29 使用【引用原文】，都有 `[Level:]`，沒有說服性字眼。
- 逐字引用：`verify-quotes.py` 回傳 0。
- 跨模組一致：`s-card-detail`／`s-card-assignee-picker` 雙向導覽一致；沒有重複定義的畫面。`s-cards-by-assignee` 的導覽問題見第 1 點。
- 任務完成度：T2.11 的三項機械驗收（F01=0、F02=0、`s-card-detail` 沒有 OQ-08 ⚠️）都已達成。上次審查第 5 點（D-10 卡在 `doing`）已經隨 D-10 標 `done` 解除。「過一遍 DS-06／DS-07 warn」的結論不準確，見第 2 點。

需人工事後處理：
- last-verify 的 FAIL [2]：commit `67e4907` 已經進入歷史，不能靠改寫歷史修正訊息格式，需要人工決定是否接受。FAIL [4] 的 PDCA 標題也不能回改，同樣需要人工確認驅動腳本會怎麼處理。
- 上次審查第 1 點（「中途放棄」段是否允許寫「X 不變」）和「新增類操作的需確認判準要統一」仍然沒有處理。

### 關卡摘要
下一個任務是 T3.01，不是關卡，這次不需要填。

## Review — 2026-09-17 23:55 — 14ab3ca
### 範圍
- commit 區間：`7bc5847`（上次審查）..`14ab3ca`，共 8 個 commit
- 任務：D-14、D-15、D-16、T3.01（都已標 `done`）；`actionable` 下一個是 T3.02，不是關卡
- runtime/last-verify.md：**FAIL**，有 1 項：`[9] D-15：ui-check(all) 應為 0，實際 52`。沒有警告
- 期間新增的 OQ：OQ-30
- 目前 `ui-check`（全部 ui 檔，帶 `--spec`）：52 error、23 warning，52 個 error 都在 F03 骨架（DS-02／DS-04）；`verify-quotes.py` 回傳 0

### 發現
1. **高**（不開 D，需人工事後處理）：last-verify FAIL [9] 不是 D-15 的內容退步，而是 D-15 的驗收條件寫了 `ui-check(all)=0`，T3.01 依規定建立 F03 骨架後，骨架階段本來就會有 DS-02／DS-04 error，所以 `all` 變成 52。F01、F02 的 error 數仍然都是 0，D-15 的實質修正（OQ-30 加上 `s-card-add-dialog` 的 ⚠️）還在。這個 FAIL 在 T3.02～T3.05 把 F03 四個畫面填完之前都會持續出現，執行輪收到「先修正失敗項目」時沒有合規的修法：審查輪不能改 D-15，執行輪也不應該為了讓 FAIL 消失而改已完成任務的驗收條件，或一次趕填 F03。這個判準是上次審查（D-15 由上次審查開立）寫得不夠精確造成的，責任在審查端。建議人工把 D-15 的 `ui-check(all)=0` 改成 `ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0` 和 `ui-check(.dev/F02-user-membership/ui-user-membership.md)=0`；或讓 `accept-check --done` 對 `all` 排除仍在骨架階段的檔案。在人工處理之前，執行輪應該照順序做 T3.02，並在 PDCA 註明這個 FAIL 是骨架階段的預期結果。之後開 D-xx 時，驗收條件不要再用 `ui-check(all)`。
2. **低**（不開 D）：Iteration 40（D-15）的 Check 寫「`0 error(s), 5 warning(s)`」，但前後兩輪（Iteration 39、41）用同樣範圍跑出來的是 19 個 warning，數字對不上，推測這輪沒有帶全部 ui 檔或 `--spec`。error 數都是 0，不影響結論。PDCA 不能回頭修改，這裡只做記錄。
3. **低**（不開 D）：OQ-30 標的是【引用原文】，但描述 F01 `uc-add-card` 的部分自己註明「不逐字引用，僅描述既有定義」。我已對照 spec 確認內容正確（`roles: [r-user]`、`crud: {board: R, card: C}`），不過同一列混用引用原文和轉述，不符合四種寫法擇一的精神。原因是 `verify-quotes.py` 的 `spec_for_line` 一行只能對應一份 spec（Iteration 40 有記錄），屬於工具限制。另外，`[Level: kanban-basic/...]` 和「模組」欄的 user-membership 不一致。
4. **低**（不開 D）：OQ-30 選項 2（新增 `r-board-member` 角色列，另拆一列操作觸發 `uc-member-add-card`）在 DS-05 下其實可行，Iteration 40 試過的是「同一列掛兩個 uc」，不是選項 2。採用選項 3「維持 ⚠️ 等人工決定」仍然誠實，因為兩個 uc 是不是同一個動作，spec 確實看不出來。不過 `s-card-add-dialog`「待確認事項」寫的「無法同列同時觸發」只說明了失敗的做法，人工決定時要知道選項 2 可行。另外，這段待確認事項用工具規則（「ui-check」的 DS-05）當理由，ui 檔內容混入工具機制，建議人工處理 OQ-30 時一併改寫。
5. **低**（不開 D）：PDCA 的時間又不連續：Iteration 42（T3.01）記錄 20:35，前一則 Iteration 41 是 23:40。只做記錄。

有檢查、沒發現偏差的面向：
- 不定義新概念：D-14 只新增導覽引用（`s-cards-by-assignee` 在 F02 真實存在）；F03 骨架的四個「所屬 Feature」和 `spec-kanban-widgets.md` 的四個 `## Feature:` 標題（第 60、122、182、241 行）逐字相符；T3.02～T3.05 會用到的六個 uc 都存在於 spec。
- 不寫業務結果：D-16 已經刪掉「卡片縮圖同步顯示」和掛錯 post 的歸因；`s-card-detail`「關閉」的「回到 `s-board` 時，看板交會格內容更新」是畫面呈現。
- 不寫排版視覺：沒有新增顏色、間距或元件選型。
- 狀態誠實性：`s-card-add-dialog` 已改回「討論中」，兩處 ⚠️ 都對應 OQ-30；`s-card-detail`、`s-card-assignee-picker` 維持「已定案」，範圍內沒有 ⚠️；F03 四個畫面都是「未討論」。
- 需確認判斷：這次沒有改動「需確認？」欄。
- OQ 品質：OQ-30 有 `[Level:]`，沒有說服性字眼，「採用」是維持現狀並標 ⚠️（其餘見第 3、4 點）。
- 逐字引用：`verify-quotes.py` 回傳 0。
- 跨模組一致：`s-card-detail` 和 `s-cards-by-assignee` 的雙向導覽已經對上，上次審查第 1 點解除；沒有重複定義的畫面。
- 任務完成度：D-14（段落含 `s-cards-by-assignee`）、D-16（「空資料」行已經不含「`uc-view-card-assignees` post」、「儲存變更」列已經不含「卡片縮圖同步顯示」）、D-15（走 (b) 分支：OQ-30 加上 ⚠️）的實質條件都已達成，只有 `ui-check(all)` 受 F03 骨架影響，見第 1 點；T3.01 的四個標題和三行標頭都存在，而且沒有 DS-01 error。

需人工事後處理：
- 第 1 點：修正 D-15 的 `ui-check(all)` 判準，或調整 `accept-check --done` 對骨架檔的處理。
- OQ-30：決定 `uc-add-card` 和 `uc-member-add-card` 的關係（開 CR 改 roles，或拆成兩列操作）。
- 先前遺留：「中途放棄」段是否允許寫「X 不變」、新增類操作的需確認判準要統一、commit `67e4907` 缺少 scope。

### 關卡摘要
下一個任務是 T3.02，不是關卡，這次不需要填。

## Review — 2026-09-17 20:45 — a7b411a
### 範圍
- commit 區間：`14ab3ca`（上次審查）..`a7b411a`，共 9 個 commit（含上次審查 commit `9d0becf`）
- 任務：T3.02、T3.03、T3.04、T3.05（都已標 `done`）；`actionable` 下一個是 T3.06，不是關卡
- runtime/last-verify.md：PASS，沒有失敗或警告；`ui-check(all)` 的 error 已降到 0，上次審查第 1 點（D-15 的 `all` 判準）這次不再觸發
- 期間新增的 OQ：OQ-31、OQ-32
- 目前 `./scripts/ui-check`：0 error、9 warning（DS-06 1 筆、DS-07 8 筆，其中 4 筆是 F03 四個畫面）；`verify-quotes.py` 回傳 0

### 發現
1. **中**：`s-cycle-lead-time-dashboard`（「討論中」）的資料表「統計摘要」列和「待確認事項」第 2 行都有 ⚠️「百分位數 spec 未定義」，但 OQ 檔沒有對應的列（OQ-31 只處理進入路徑），不符合狀態誠實性的要求。→ **D-17 (1)**
2. **中**：同一列的「來源」寫「Lead Time／Cycle Time 的統計計算，排除 Cycle Time 為「無」的卡片」，把排除規則擴大到 Lead Time 統計。Scenario 原文只有『統計摘要的 Cycle Time 平均值與百分位計算應該排除卡片 "B"』，這是超出 spec 的推論，而且沒有記錄成 OQ。→ **D-17 (2)**
3. **中**：OQ-31 的「採用」是「暫定從 F01 `s-board` 的操作進入」，並寫「待 `s-board` 收尾任務補上該操作」，但沒有任何任務承接：T1.10 已完成，`s-board` 已定案。這個方向也和 D-09（看板本體改成 `s-canvas` 上的 `item`）以及 F07 spec「元件由所屬模組定義」的方向衝突。四個 F03 畫面的 ⚠️ 雖然都有對應到 OQ-31，但 OQ-31 的結論會讓人以為問題已經有人處理，實際上沒有。→ **D-18**
4. **低**：`s-wip-dashboard` 的「空資料」寫「某 Stage 目前卡片數為 0……圖表顯示為空」，但 `uc-view-wip` post 是依 Stage 顯示數量，單一 Stage 為 0 時應顯示 0。Iteration 44 把這項列為「低風險決定」，但推論有誤。→ **D-17 (3)**
5. **低**（不開 D，需人工事後處理）：spec 自己前後不一致。F03「其他名詞」表的 WIP 定義是『目前不在 Done 角色 Stage 的卡片數量』，但 Scenario「檢視各 Stage 目前的卡片數量」要求顯示 Stage "完成"（Done 角色）的卡片數 5。ui 照 post p1 寫，不算 ui 的偏差；這個矛盾要由人工透過 CR 處理 spec。
6. **低**（不開 D）：Iteration 45、46 的 Check 把單檔 `ui-check ... --spec` 出現的 `s-board` REF-07 歸為「OQ-31 同款」，這個說法不正確。這些 error 出現的原因是單檔檢查沒有帶入 F01 ui 檔，因此找不到跨模組 Screen ID，和 OQ-31 無關。Iteration 46 後段已經寫出正確原因（`tools error-count` 會帶入全部 ui 檔）。PDCA 不能回頭修改，這裡只做記錄。
7. **低**（不開 D）：`s-throughput-cfd-dashboard` 和 `s-duedate-reminder` 的資料表在範本五欄之前多加一欄（「圖表」／「清單」），`ui-check` 可以接受，T3.04 的任務欄也要求在同一個 Screen ID 內分開說明，所以這次不開 D。CFD「日期」列寫「資料範圍內的每一天」，但 spec 沒有定義資料範圍，而 `ui-convention.md` 的儀表板提問「時間範圍怎麼選」也還沒回答。建議 T3.06 或人工一併確認是否需要標 ⚠️。
8. **低**（不開 D）：四個畫面的 commit 摘要都寫「定案」，但畫面狀態都是「討論中」。畫面狀態本身沒有造假（⚠️ 都還在），只是 commit 用語和 T 任務措辭不一致。

有檢查、沒發現偏差的面向：
- 不定義新概念：六個 uc（`uc-view-cycle-lead-time`、`uc-view-wip`、`uc-view-aging-wip`、`uc-view-throughput`、`uc-view-cfd`、`uc-view-duedate-reminder`）都存在；`card.title`、`card.due-date`、`stage` 都是 F01 的真實 ID；Lead／Cycle Time、年齡、完成數都標為「衍生」，並引用 post；角色只用 uc roles 的 `r-user`。
- 不寫業務結果：操作表「失敗時」都寫「不適用（無 fail 定義）」，和 `fail: {}` 一致。
- 不寫排版視覺：沒有顏色、間距、元件選型；「散佈圖」這類圖形用語沒有寫進 ui 檔。
- 狀態誠實性：四個畫面都是「討論中」，⚠️ 都在（缺 OQ 的情況見第 1 點）。
- 需確認判斷：全部是讀取 uc，「需確認？」都標「否」，判斷合理。
- OQ 品質：OQ-31、OQ-32 都使用【引用原文】，也都有 `[Level:]`，引用可以逐字比對，沒有說服性字眼。OQ-31 的 Level 只列 `s-cycle-lead-time-dashboard`，但實際涵蓋四個畫面（小瑕疵，由 D-18 新列處理）。
- 逐字引用：`verify-quotes.py` 回傳 0。
- 跨模組一致：沒有重複定義的畫面；F03 沒有引用不存在的 `s-` ID。
- 任務完成度：T3.02～T3.05 的「八段齊全、本畫面無 error」都已達成；T3.04 的「同一 Screen ID、不拆兩畫面」已達成。

需人工事後處理：
- 第 5 點：F03 WIP 名詞定義和 Scenario 矛盾（需要 CR）。
- D-09、OQ-30 仍待人工決定；D-18 會把 F03 的進入路徑也掛到 D-09／F07 整合的決定上。
- 先前遺留：「中途放棄」段是否允許寫「X 不變」、新增類操作的需確認判準要統一、commit `67e4907` 缺少 scope。

### 關卡摘要
下一個任務是 T3.06，不是關卡，這次不需要填。

## Review — 2026-09-17 21:01 — 8216f2d
### 範圍
- commit 區間：`a7b411a`（上次審查）..`8216f2d`，共 8 個 commit（含上次審查 commit `57d886d`）
- 任務：D-17、D-18、T3.06、T4.01（都已標 `done`）；`actionable` 下一個是 T4.02，不是關卡
- runtime/last-verify.md：FAIL，共兩項。(a) Iteration 50 的 Check 沒有貼 `ui-check` 結果行，下一輪執行時會被強制補上，所以這次不另開 D。(b) D-15 的 `ui-check(all)` 又因為 F04 骨架出現 13 個 error，情況和前次審查第 1 點相同，T4.02 填完八段就會消失。
- 期間新增的 OQ：OQ-33、OQ-34
- 目前 `./scripts/ui-check`：13 error（全部是 F04 骨架的 DS-02／DS-04，屬於預期），13 warning；`verify-quotes.py` 回傳 0

### 發現
1. **中**：OQ-34 的「情況」欄寫「人工已於 OQ-17／OQ-18／OQ-19 確認看板本體與其他元件改以 F07 `item` 形式呈現於 `s-canvas`」，把人工決定的範圍說大了。這三列實際只涵蓋 Swimlane／Stage 的觸發位置、`s-board` 本體、成員頭像清單，沒有任何一列提到圖表。這樣寫等於把「圖表會成為 Canvas 元件」包裝成人工已經決定的事。「採用」欄選「缺區分條件、不預設」，這部分是誠實的，問題只出在「情況」欄的敘述。→ **D-19**
2. **低**（不開 D，需人工事後處理）：OQ-34 的「模組」欄填 `canvas-layout`，Level 也標 `canvas-layout/s-cycle-lead-time-dashboard…`，但這四個畫面其實在 F03。Iteration 48 有明講，這是為了繞過 `verify-quotes.py` 的 `spec_for_line`（一行只能對應一個模組的限制），才刻意避開「kanban-widgets」這個字面。這等於配合工具限制而改寫資料，屬於輕度的自欺。正確的做法是修工具，讓它支援跨模組引用，這件事和上次審查第 3 點（OQ-30）是同一個根因。
3. **低**（不開 D）：OQ-34 標【矛盾】，但 A 方是 OQ-31 的暫定決定，不是 spec 原文，只有 B 方逐字引用了 spec。嚴格來說，這兩方不是「兩處 spec 互相矛盾」。由於「採用」欄沒有因此偏向任何一方，這次只記錄，不開 D。
4. **低**（不開 D）：OQ-33 標【引用原文】，但內文又夾了【推論】，同一列混用兩種寫法（與 OQ-30 同類）。兩段引文我都已經對照 spec，確認逐字相符。另外，D-17 的三項修正都確認已落實：「來源」欄只寫 Cycle Time 的排除規則，Lead Time 部分標 ⚠️ 並指向 OQ-33；驗收條件改成「Cycle Time 平均值與百分位」。
5. **低**（不開 D）：T4.01 把 `s-board-clock-control` 的類型定為「對話框」，理由只有「七種固定類型裡沒有側欄」，沒有從 spec 推導。T4.02 定案時要重新判斷：這個控制項比較像「設定」還是「對話框」，要以 `uc-adjust-board-clock`／`uc-pause-resume-board-clock` 的使用情境為準。
6. **低**（不開 D）：D-15 的 `ui-check(all)` 判準每次遇到骨架任務都會讓驗證 FAIL，這是第二次發生（F03、F04）。後面 F05～F07 還會再遇到。這一項維持「需人工事後處理」。

有檢查、沒發現偏差的面向：
- 不定義新概念：F04 骨架的「所屬 Feature：看板時間管理」和 `spec-board-clock.md` 第 55 行 `## Feature: 看板時間管理` 逐字相符。F03 這次的修改沒有新增任何 ID。
- 不寫業務結果：F03 只改了進入路徑和統計摘要的描述，沒有重述 post 或 fail。
- 不寫排版視覺：沒有出現相關內容。
- 狀態誠實性：F03 四個畫面維持「討論中」，每個 ⚠️ 都對應到 OQ-31／OQ-33／OQ-34／OQ-32。F04 畫面是「未討論」。
- 需確認判斷：這次沒有改動「需確認？」欄。
- OQ 品質：OQ-33、OQ-34 都有 `[Level:]`，也沒有說服性字眼（其餘問題見第 1～4 點）。
- 逐字引用：`verify-quotes.py` 回傳 0。另外，OQ-34 的兩段 F07 引文，我已經對照 `spec-canvas-layout.md` 第 12 行，確認逐字相符。
- 跨模組一致：F03 不再暗示要替 `s-board` 補操作列，上次審查第 3 點已解除。`ui-kanban-basic.md` 在這段期間沒有變更。
- 任務完成度：D-17 的 (1)～(3)、D-18（OQ-34 含【矛盾】和 OQ-31，四個畫面都引用 OQ-34）、T3.06（F03 0 error）、T4.01（標題和三行標頭都在，DS-01 沒有 error）都已達成。PDCA 的 Check 和實際結果一致，唯一例外是 Iteration 50 缺少結果行，驗證已經抓到。

需人工事後處理：
- 第 2 點：`verify-quotes.py` 要支援同一行引用多份 spec，之後再把 OQ-30、OQ-34 的模組欄／Level 改回正確值。
- 第 6 點：調整 D-15 的 `all` 判準，或讓 `accept-check --done` 排除骨架檔。
- 延續上次：F03 的 WIP 名詞定義和 Scenario 互相矛盾（要走 CR）；D-09、OQ-30、OQ-34 待人工決定；「中途放棄」段是否允許寫「X 不變」；新增類操作的需確認判準要統一；commit `67e4907` 缺 scope。

### 關卡摘要
下一個任務是 T4.02，不是關卡，這次不用填。

## Review — 2026-09-17 21:27 — 0d6cf1e
### 範圍
- commit 區間：`8216f2d`（上次審查）..`0d6cf1e`，共 5 個 commit（含上次審查 commit `bd4b5d3`）
- 任務：T4.02、T4.03（`done`），D-19（`doing`）；`actionable` 目前是 D-19 → T5.01，都不是關卡
- runtime/last-verify.md：PASS，沒有警告
- 期間新增的 OQ：OQ-35～OQ-39
- 目前 `./scripts/ui-check`：0 error、11 warning；`verify-quotes.py` 回傳 0

### 發現
1. **高**（流程卡住）：D-19 的驗收條件要求「OQ-34 列不再含……字樣」，但 OQ 檔只能追加，執行輪不能改 OQ-34。這是上次審查開任務時出的錯。Iteration 53 改成追加 OQ-39，我逐項對照 OQ-17／18／19，確認 OQ-39 對三列範圍的描述正確，也寫明「F03 圖表是否成為 F07 `item`，人工尚未決定」，D-19 的目的已經達成。執行輪沒有自己宣稱達標，而是把任務留在 `doing`，這樣處理是誠實的。不過 `actionable` 會優先挑 `doing` 的任務，D-19 不結案，每一輪都會重選它，loop 會空轉。我接受 OQ-39 作為 D-19 的完成方式。→ **D-20**
2. **中**：`s-board-clock-control` 的操作表「失敗時」寫「`uc-guard-clock-monotonicity` 為共同前置條件」。spec 裡兩個 uc 的 `requires` 都是 `[]`，guard 的 pre 說的是建立新事件時的條件。「共同前置條件」這個說法是 T4.02 的任務欄先寫的，spec 沒有這個依據，ui 檔卻把它寫成事實。→ **D-21 (1)**
3. **中**：驗收條件第 3 行「非 Owner 嘗試調整時，畫面顯示訊息」預設非 Owner 看得到本畫面，也能操作調整，但同一畫面的「角色與權限」「無權限」都用 ⚠️ 指向 OQ-37，表示這件事還沒決定。畫面內前後矛盾。→ **D-21 (2)**
4. **中**：OQ-35 的「情況」欄說 OQ-31「已被 OQ-34 推翻為『進入路徑由 F07 Canvas 機制決定』」，但 OQ-34 採用的是「缺區分條件、不預設」。這和 D-19 是同一類問題：把尚未決定的事寫成已經決定。OQ-35 的「採用」本身沒有問題。→ **D-21 (3)**
5. **低**（不開 D）：Iteration 52 為了修正 OQ-36 的欄數，直接改寫既有的 OQ 列，違反「只能追加」。verify 已經抓到，Iteration 53 也說明了原因，確認 16d0cf9 的版本不必再改。PDCA 無法回頭修改，這裡只做記錄。
6. **低**（不開 D，需人工事後處理）：`uc-guard-clock-monotonicity` 的 DS-06 warn 目前掛在 F04。依 spec Scenario，這項限制是在建立卡片等寫入動作時觸發，照理應由 F01 `s-card-add-dialog` 這類畫面在「失敗時」欄引用。這是跨模組補引用，和 D-15 同類，建議在收尾階段一起確認。
7. **低**（不開 D）：上次審查第 5 點請 T4.02 依 uc 使用情境重新判斷「對話框」這個類型，但 PDCA 沒有記錄判斷過程，畫面維持「對話框」。T4.02 的任務欄本來就允許「對話框」，所以這次不另開任務。
8. **低**（不開 D）：「資料狀態差異」寫「PAUSED 下顯示『恢復』、REALTIME 下顯示『暫停』」，這是畫面決定，spec 裡找不到依據，但沒有改變業務結果，可以接受。

有檢查、沒發現偏差的面向：
- 不定義新概念：看板時間的三個欄位都沒有掛 ID，也都用 ⚠️ 指向 OQ-36，這和 spec 實體表、欄位表都是空表的情況一致，處理方式誠實。`r-board-owner` 在 F02、`r-user` 在 F01，兩個角色都存在，標為跨模組也正確。「所屬 Feature」和目的都對得上 Feature 標頭。
- 不寫業務結果：「成功後」只寫顯示變化，失敗時用「依 p1」引用，沒有抄錄錯誤訊息（第 2 點例外）。
- 不寫排版視覺：沒有發現相關內容。
- 狀態誠實性：畫面是「討論中」，4 個 ⚠️ 分別對應 OQ-35～OQ-38。
- 需確認判斷：調整、暫停、恢復都可以用反向操作或再調整還原，標「否」合理。
- OQ 品質：OQ-35～OQ-39 都有 `[Level:]`，也沒有說服性字眼。OQ-37 採用「不預設」，並說明它和 OQ-23 不同的地方，這個判斷有依據。OQ-39 的欄位格式正確（誤述問題見第 4 點）。
- 逐字引用：`verify-quotes.py` 回傳 0。另外人工比對了 OQ-38 的 When 步驟和 OQ-36 的其他名詞原文，都和 spec 一致。
- 跨模組一致：F04 沒有重複定義其他模組的畫面，引用的 uc ID 都存在。
- 任務完成度：T4.02 的八段齊全、0 error，T4.03 也是 0 error，都已達成。PDCA 的 Check 和實際結果一致。

需人工事後處理：
- 第 6 點：`uc-guard-clock-monotonicity` 應該由哪個寫入畫面引用。
- 延續上次：`verify-quotes.py` 要支援同一行引用多份 spec（OQ-34／OQ-39 的「模組」欄仍填 canvas-layout）；D-15 的 `all` 判準遇到骨架檔時會 FAIL（下一個 T5.01 會再遇到）；F03 的 WIP 名詞定義和 Scenario 矛盾，要走 CR；D-09、OQ-30、OQ-34 待人工決定；「中途放棄」段是否允許寫「X 不變」；新增類操作的需確認判準要統一；commit `67e4907` 缺少 scope。

### 關卡摘要
下一個任務是 D-19，接著是 T5.01，都不是關卡，這次不用填。

## Review — 2026-09-17 21:41 — f997b9a
### 範圍
- commit 區間：`0d6cf1e`（上次審查）..`f997b9a`，共 8 個 commit（含上次審查 commit `af36da0`）
- 任務：D-19、D-20、D-21、T5.01、T5.02（皆 `done`）；`actionable` 目前是 T5.03，不是關卡
- runtime/last-verify.md：PASS，沒有警告（範圍 38ab9da..f997b9a）。Iteration 55／57 各自修正了前一輪的 FAIL（Iteration 54 缺 ui-check 結果行、T5.01 骨架讓 D-15 的 `ui-check(all)` 破功），兩次都用追加的方式處理，沒有回頭改舊的 PDCA
- 期間新增的 OQ：OQ-40～OQ-42
- 目前 `./scripts/ui-check`：0 error、12 warning；F05 單檔 0 error（`s-workload-dashboard` 有 DS-07 warn，對應 OQ-41）；`verify-quotes.py` 回傳 0

### 發現
1. **中**：`s-workload-dashboard` 的「驗收條件」有三行把 spec post 照抄成領域規則（多位負責人各算一張、Done 不計入、不重複新增），斷言主詞不是畫面元素，違反 `ui-convention.md`「驗收條件……不寫領域狀態」。Iteration 57 的 Do 自己寫了「逐條對照 spec 四個 view Scenario」，這其實是把 Scenario 搬進 ui 檔。另外，「拖曳成員頭像到卡片後，該卡片負責人清單更新」預設本畫面會顯示卡片與負責人清單，但這件事在同一畫面已經用 ⚠️ 指向 OQ-42，表示尚未決定，前後矛盾，和上次審查 D-21 (2) 是同一類問題。→ **D-22**
2. **低**（不開 D）：F03 已定案畫面的驗收條件也有類似的計算規則敘述（例如「統計摘要的 Cycle Time 平均值與百分位計算排除……」），但那些句子的主詞是畫面上的統計摘要，而且 D-17 已經審過，所以這次不追溯。建議收尾階段（G1 L-09）統一判斷。
3. **低**（不開 D）：操作表的拖曳列、「角色與權限」都寫到「成員頭像」，但 spec（F02／F05）沒有頭像欄位，「資料」段也沒有頭像這一列。F01 `s-board` 的同類操作也是這樣寫，而且「頭像」是 Scenario 原文用語，不算發明新概念，這次只做記錄。
4. **低**（不開 D）：拖曳列的「需確認？」欄寫「否（可透過 F02 `uc-set-card-assignees` 調整……）」，但 `uc-set-card-assignees` 的 roles 是 `r-board-member`，本畫面的角色是 `r-user`，兩者不一定是同一群人。「追加負責人可以還原」這個判斷本身合理，只是引用的還原路徑跨了角色，建議 G1 抽查時一併確認。
5. **低**（不開 D）：OQ-42 選項 2（拖曳目標在 F01 `s-board`）如果被採用，本畫面的拖曳列會移到別的畫面，觸發的 uc 也可能要和 F02 `uc-assign-card-owner-by-drag` 合併（spec 的 OQ-09）。這要由人工決定，現在的「不預設」是正確做法。

有檢查、沒發現偏差的面向：
- 不定義新概念：`user.display-name`（F02）、`card.title`（F01）、`card.assignees`、`board-membership` 都確實存在；兩個衍生欄位都附上 post 依據；`r-user` 沿用 F03／F04 的跨模組寫法；「所屬 Feature：人員工作量檢視」和 spec 的 `## Feature:` 逐字相符。
- 不寫業務結果：「失敗時」兩列都寫「無 fail 定義」，符合 spec 的 `fail: {}`（「成功後」的問題見第 1 點）。
- 不寫排版視覺：沒有發現相關內容。
- 狀態誠實性：F05 畫面是「討論中」，兩個 ⚠️ 分別對應 OQ-41、OQ-42。F04 `s-board-clock-control` 維持「討論中」，D-21 修正後，驗收條件那一行已加上 OQ-37。
- 需確認判斷：F05 兩列都標「否」，檢視和追加負責人都可以還原，判斷合理（第 4 點例外）。
- OQ 品質：OQ-40～42 都有 `[Level:]`，也沒有說服性字眼。OQ-40 標【推論】，正確指出 OQ-34 實際採用「進入路徑不預設」，D-21 (3) 已達成。OQ-41、OQ-42 的引文我已經對照 `spec-workload.md`，確認逐字相符。
- 逐字引用：`verify-quotes.py` 回傳 0。
- 跨模組一致：F05 沒有重複定義其他模組的畫面。F01 `s-board` 觸發的是 F02 `uc-assign-card-owner-by-drag`，F05 觸發的是自己的 `uc-drag-assign-card-owner`，兩個 ID 都存在，和 spec 的說明（OQ-09）一致。
- 任務完成度：D-19／D-20 的結案符合上次審查的指示；D-21 的 (1)～(3) 我逐項核對，都已落實（不含「共同前置條件」、驗收條件那一行含 OQ-37、OQ-40 同時含「OQ-35」和「不預設」、OQ 舊列沒有被修改）；T5.01 的標題和三行標頭都在；T5.02 八段齊全、0 error。PDCA 的 Check 和實際結果一致（第 1 點是內容品質問題，不是 Check 不實）。

需人工事後處理：
- 第 2、4 點：G1 抽查時，一併決定驗收條件裡的「計算規則敘述」可以寫到什麼程度，以及需確認欄的還原路徑是否必須屬於同一角色。
- 延續上次：`uc-guard-clock-monotonicity` 應該由哪個寫入畫面引用；`verify-quotes.py` 要支援同一行引用多份 spec（OQ-34／OQ-39 的「模組」欄仍填 canvas-layout）；D-15 的 `all` 判準遇到骨架檔時會 FAIL（Iteration 57 已再次發生，F06／F07 還會遇到）；F03 的 WIP 名詞定義和 Scenario 矛盾，要走 CR；D-09、OQ-30、OQ-34、OQ-41、OQ-42 待人工決定；「中途放棄」段是否允許寫「X 不變」；新增類操作的需確認判準要統一；commit `67e4907` 缺少 scope。

### 關卡摘要
下一個任務是 T5.03，不是關卡，這次不用填。

## Review — 2026-09-17 22:05 — f1a6520
### 範圍
- commit 區間：`f997b9a`（上次審查）..`f1a6520`，共 8 個 commit（含上次審查 commit `1076505`）
- 任務：D-22、T5.03、T6.01、T6.02（皆 `done`）；`actionable` 目前是 T6.03，不是關卡
- runtime/last-verify.md：FAIL，失敗項目是 Iteration 61 的標題缺 `HH:MM`（Iteration 60 也一樣）。Iteration 61 本身是在修正 T6.01 骨架讓 D-15 `ui-check(all)=0` 破功的 FAIL，處理方式正確
- 期間新增的 OQ：OQ-43
- 目前 `./scripts/ui-check`：0 error、13 warning；`verify-quotes.py` 回傳 0

### 發現
1. **中**：`s-feature-cr-board` 的「驗收條件」有四行在重述 `uc-view-feature-cr-board` post p1～p4 的分類與對應規則（Feature 狀態怎麼對應 `stage.role`、CR 掛在哪個 Feature 底下、什麼算 orphan、警告不影響其他卡片統計），這和上次審查 D-22 是同一類問題。Iteration 61 的 Do 寫「驗收條件 5 條」，沒有對照 D-22 的教訓。→ **D-23 (1)**
2. **低**：同一畫面「角色與權限」只寫 `r-user`，沒有標「F01，跨模組」，和 F03～F05 的寫法不一致；「無權限」寫「spec 僅定義 `r-user` 一種角色」，但 F06 spec 的角色表是空的，這句話與事實不符。順便併入 D-23。→ **D-23 (2)**
3. **低**（不開 D）：PDCA Iteration 60／61 的標題缺 `HH:MM`，verify 已經抓到。PDCA 只能追加，下一輪用正確格式寫標題即可，不需要回頭改。請執行輪注意不要為了這件事改寫舊的 PDCA。
4. **低**（不開 D）：D-22 修正後，F05 拖曳列的「成功後」變成「觸發 `uc-drag-assign-card-owner`（⚠️ 見 OQ-42）」，這寫的是觸發動作，不是呈現方式。但拖曳目標在 OQ-42 還沒決定，目前也寫不出呈現方式，可以接受，等 OQ-42 決定後再回頭補。
5. **低**（不開 D）：F06「Feature 狀態」列用「其他名詞」表的「NONE＝未開發」補上 post p1 沒寫到的第三種狀態。Iteration 61 在 Do 裡寫了判斷依據，而且「其他名詞」表確實逐字定義了三者，所以不算發明新概念。

有檢查、沒發現偏差的面向：
- 不定義新概念：`card.labels`、`stage.role` 都是既有欄位；Feature 卡、CR 卡、affects 標籤、orphan CR 都出自 F06「其他名詞」表；`r-user` 存在於 F01（標示問題見第 2 點）；「所屬 Feature：Feature／CR 追蹤表」和 spec 的 `## Feature:` 逐字相符。
- 不寫業務結果：「失敗時」寫「無 fail 定義」，符合 `fail: {}`；沒有抄錄錯誤訊息。
- 不寫排版視覺：沒有發現相關內容。
- 狀態誠實性：F06 畫面是「討論中」，唯一的 ⚠️ 對應 OQ-43；F05 維持「討論中」，⚠️ 對應 OQ-41／OQ-42。
- 需確認判斷：F06 只有一個檢視操作，標「否」合理。
- OQ 品質：OQ-43 有 `[Level:]`，用【引用原文】，Background 引文已對照 spec 確認逐字相符，「採用」是不預設，沒有說服性字眼。
- 逐字引用：`verify-quotes.py` 回傳 0。
- 跨模組一致：F06 沒有重複定義其他模組的畫面，引用的 uc ID 存在。
- 任務完成度：D-22 的三項字樣條件我逐一核對，都已落實；T5.03、T6.01、T6.02 的機械條件都已達成，PDCA 的 Check 和實際結果一致（第 1 點是內容品質問題，不是 Check 不實）。

需人工事後處理：
- 延續上次：驗收條件裡的「計算規則敘述」可以寫到什麼程度（F03 已定案畫面仍有類似句子），以及需確認欄的還原路徑是否必須屬於同一角色，留給 G1 抽查時一併決定；`uc-guard-clock-monotonicity` 應該由哪個寫入畫面引用；`verify-quotes.py` 要支援同一行引用多份 spec；D-15 的 `all` 判準遇到骨架檔時會 FAIL（Iteration 60 又發生一次，T7.01 還會再遇到）；F03 的 WIP 名詞定義和 Scenario 矛盾，要走 CR；D-09、OQ-30、OQ-34、OQ-41、OQ-42、OQ-43 待人工決定；「中途放棄」段是否允許寫「X 不變」；新增類操作的需確認判準要統一；commit `67e4907` 缺少 scope。

### 關卡摘要
下一個任務是 T6.03，不是關卡，這次不用填。
