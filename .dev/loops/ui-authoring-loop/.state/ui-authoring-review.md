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
