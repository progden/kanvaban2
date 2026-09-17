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
