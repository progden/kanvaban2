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
