# LLM 語意 review 清單（llm-review.md）

腳本（`checks.md`）做完參照完整性與格式檢查、全部無 error 之後，才做這份清單。這裡只放**要讀懂內容才能判斷**的項目；腳本已經檢查過的東西（ID 存不存在、tag 順序、欄位齊不齊）LLM 不重做，reviewer 看到腳本能報的問題也不用在這裡挑。

使用方式：規格 PR 的 reviewer（人或 LLM）拿 diff 裡涉及的 Use Case、Scenario、畫面，逐項問下面的問題。每項附一個範例提問，可以直接改 ID 後貼給 LLM。範例沿用 `spec-convention.md` 的 `uc-delete-swimlane`／CR-024 情境。

---

## L-01 `pre` 各句之間是否互斥、沒有重疊或包含

**看什麼**：usecase 區塊同一項的 `pre.p1`、`pre.p2`…。腳本只檢查每句有 ID，不知道兩句是不是講同一件事。重疊會導致 `fail` 兩個 key 對到同一個失敗行為，`@fail-` Scenario 無法分辨。

**範例提問**：

> 讀 `uc-delete-swimlane` 的 `pre`：
> - p1：`swimlane` 存在，且不是看板中唯一的 `swimlane`
> - p2：若該泳道內有 `card`，`card.swimlane` 的目的泳道已指定
>
> 這兩句是否有任何一種狀態同時讓 p1 與 p2 不成立？p1 是否其實是兩個條件（存在、不是唯一）應該拆成 p1、p2？如果拆，`fail` 的行為會不會不同？

---

## L-02 Scenario 的 Given／Then 是否真的體現對應的 `pre`／`post`

**看什麼**：掛 `@uc-x` 的每個成功 Scenario，其 Given 是否建立了 `pre` 全部成立的狀態、Then 是否驗證了 `post` 每一句；掛 `@fail-pN` 的 Scenario，其 Given 是否只讓 `pN` 不成立、Then 是否對應 `fail.pN` 的行為。腳本只知道 tag 對得上，不知道步驟內容。

**範例提問**：

> `uc-delete-swimlane` 的 `post` 有兩句：`swimlane` 不存在、原屬該泳道的 `card.swimlane` 更新為目的泳道。
> 下面這個掛 `@uc-delete-swimlane` 的 Scenario「刪除包含卡片的 Swimlane 需要先轉移卡片」，它的 Then 步驟是否逐句驗證了這兩個 post？有沒有 post 沒被任何 Then 驗證，或 Then 驗證了 post 沒寫的東西（那代表 post 漏了）？
>
> 另外，掛 `@fail-p2` 的「未選擇目的 Swimlane 時不可刪除包含卡片的 Swimlane」，它的 Given 是否確保 p1 成立、只有 p2 不成立？

---

## L-03 不同 Use Case 對同一概念的措辭是否一致

**看什麼**：同一個實體或行為在不同 usecase 的 `pre`／`post`、不同 Scenario 的步驟、ui 檔的操作名稱裡，用詞是否一致（「目的泳道」vs「目標 Swimlane」vs「接收泳道」）。腳本只比對反引號 ID 與引號字串，中文措辭差異看不出來。

**範例提問**：

> 找出 `spec-kanban-basic.md` 與 `ui-kanban-basic.md` 裡所有描述「刪除泳道時卡片要移去的那個泳道」的措辭，列出每一種寫法與出現位置。建議統一成哪一個？是否該把它加進「其他名詞」表？

---

## L-04 CR 驗收標準能否一對一轉成 Then 步驟

**看什麼**：`.dev/CR.md` 該 CR 的「驗收標準」每一條，是否都能在 PR 新增／變更的 Scenario 裡找到一個對應的 Then；反過來，新 Scenario 的 Then 是否有驗收標準沒提到的行為（可能是 SA 自己加的，需要提出人確認）。

**範例提問**：

> CR-024 的驗收標準有三條：(1) 刪除含卡片的泳道時，系統要求選擇目的泳道 (2) 未選目的泳道時顯示 "請選擇接收卡片的目的 Swimlane"，資料不變 (3) 選定後卡片移到目的泳道，原泳道刪除。
> 對照 PR 裡掛 `@CR-024` 的三個 Scenario，每條驗收標準對到哪個 Scenario 的哪一個 Then？有沒有對不到的？Then 裡有沒有驗收標準沒說的行為？

---

## L-05 `fail` 是否遺漏使用者實際會遇到的失敗情況

**看什麼**：從 `pre` 與欄位表的「限制」欄推，哪些前置條件不成立的情況使用者實際碰得到，但 `fail` 沒寫（腳本只檢查 `fail` 的 key 在 `pre` 裡，不檢查 `pre` 的哪些 key 該有 `fail`）。也看 `fail` 寫的行為是否合理（「拒絕，資料不變」是否真的做得到，還是會留下半套資料）。

**範例提問**：

> `uc-delete-swimlane` 的 `pre` 有 p1、p2，`fail` 也有 p1、p2。從欄位表 `swimlane.name` 「同看板內唯一」與關係表 `board`→`swimlane` min=1 來看，還有哪些使用者操作時會碰到的失敗情況沒有被 `pre` 涵蓋（例如目的泳道選到自己、目的泳道在操作中途被別人刪掉）？這些該加成新的 `pre`＋`fail`，還是屬於系統層錯誤不進規格？

---

## L-06 `post` 與關係表 cardinality 的語意衝突

**看什麼**：腳本 UC-10 只做字面 warn（post 句含關係兩端 ID 且含「新增」）。這裡看的是語意：`post` 描述的結果是否會讓某個 `max`=1 的關係出現第二筆、或讓 `min`=1 的關係變成 0 筆（例如刪除最後一個泳道）。

**範例提問**：

> 關係表：`board`→`swimlane` min=1 max=n；`swimlane`→`card` min=0 max=n；`card`→`swimlane`（透過 `card.swimlane`）恰好 1。
> `uc-delete-swimlane` 的 `post`「`swimlane` 不存在」與「原屬該泳道的 `card.swimlane` 更新為目的泳道」執行後，是否有任何瞬間或最終狀態違反上述 cardinality？`pre.p1` 的「不是唯一的 swimlane」是否足以保護 min=1？

---

## L-07 Scenario 名稱是否描述行為與結果、而非 UI 操作

**看什麼**：`spec-convention.md` §3.3 的規則。腳本只能檢查名稱非空。

**範例提問**：

> 列出 PR 新增的 Scenario 名稱。哪些是在描述 UI 操作（點擊、開啟、拖曳）而不是行為與結果？各給一個改寫建議。

---

## L-08 錯誤／確認訊息是否為完整固定文字

**看什麼**：Then 步驟裡「顯示錯誤訊息」「顯示確認訊息」後面是否是可以直接拿去驗收的完整字串，而不是概述（「顯示錯誤」「提示使用者」）。腳本 GH-07 只比對已存在的引號字串是否一致，不知道沒寫引號的步驟是不是漏了。

**範例提問**：

> 找出 PR diff 裡所有含「錯誤訊息」「確認訊息」「提示」的 Then／And 步驟。哪些沒有用 `"..."` 給出完整固定文字？

---

## L-09 ui 檔的「失敗時」是否對應 spec 的 `fail`，「成功後」是否對應 `post`

**看什麼**：操作表每一列的「成功後」「失敗時」文字，跟觸發的 Use Case 的 `post`／`fail` 是否一致；錯誤訊息是否照抄 spec 的固定文字。腳本 DS-03 只檢查「觸發」欄的 ID。

**範例提問**：

> `s-swimlane-delete-dialog` 的操作「確認刪除」觸發 `uc-delete-swimlane`。它的「失敗時」欄寫的內容，是否涵蓋 `fail.p1` 與 `fail.p2` 兩種情況？錯誤訊息是否與 Scenario「未選擇目的 Swimlane 時不可刪除包含卡片的 Swimlane」的 Then 一字不差？

---

## L-10 「其他名詞」表裡的東西是否其實是實體

**看什麼**：「其他名詞」表沒有 ID、腳本不解析。如果某個名詞會被單獨建立、修改、刪除，或有自己的欄位，它應該是實體。

**範例提問**：

> 「其他名詞」表有「WIP 限制」。整份規格裡，WIP 限制是否會被單獨設定、修改（例如「設定 Stage 的 WIP 上限」）？如果是，它應該是 `stage.wip-limit` 欄位還是獨立實體？

---

## L-11 `calls-sync` 的例外是否有正當理由

**看什麼**：`calls-sync` 代表交易內同步呼叫另一個 Use Case，規範說是例外。腳本只檢查無循環。這裡看的是：為什麼不能用 `emits`／`requires` 非同步？如果同步呼叫的那個 Use Case 失敗，本 Use Case 的 `fail` 有沒有寫？

**範例提問**：

> `uc-xxx` 的 `calls-sync` 列了 `uc-yyy`。說明為什麼這裡必須同步而非事件；`uc-yyy` 的每一種 `fail` 在 `uc-xxx` 的 `fail` 裡是否有對應的處理？

---

## 使用時的分工

| 誰 | 做什麼 |
|---|---|
| 腳本 | `checks.md` 全部 |
| LLM | L-01～L-11，輸出「疑點清單」，每點附出處行號 |
| 人 | 看 LLM 的疑點清單與 diff，決定要不要改；不重看腳本與 LLM 已經看過的項目 |
