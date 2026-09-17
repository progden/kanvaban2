# UI 撰寫待決事項（OQ）

撰寫過程中遇到的**高風險決策**（見 [`ui-authoring-prompt.md`](../prompts/ui-authoring-prompt.md)「自主決策分級」）。loop 不停下等人：遇到 spec 沒寫清楚、推不出來的資訊，選「最小驚訝、與 spec 現有內容一致」的處理方式，標 `⚠️` 留在該畫面「待確認事項」，並在下表末尾追加一列；對應畫面的狀態維持「討論中」，不標「已定案」。人工事後檢視，不同意的再回頭修正對應 ui 檔或（若是 spec 本身缺東西）回饋給 SA 走 CR。

規則：

- **只能在表格末尾追加列**，不可修改或刪除既有列，表格之後不可再加其他內容。
- 編號 `OQ-01` 起流水號；「採用」寫實際採用的處理方式，「依據」寫依據哪個 Use Case／欄位／Scenario（模組、Feature、uc 或欄位 ID），兩欄都不可空白。
- 狀態：`自動決議`（loop 已照採用的選項做，畫面留在「討論中」）／`blocked`（環境限制或「覆蓋 spec」，對應任務已標 `blocked`）。
- **「情況」欄禁止只寫抽象結論**，必須是下列四種寫法之一，並在欄位開頭標明種類：
  1. 【引用原文】直接複製 spec 對應段落的原句（Scenario 步驟、欄位表「限制」欄、usecase 區塊值），用『』包住逐字原文，附出處（`uc-`／`entity.attr`／Feature 名稱）。
  2. 【矛盾】並列兩處互相衝突的 spec 原文，各自用『』包住並附出處；「選項」欄固定給三個、不得增減：**保留 A**（沿用第一處原文的結論）／**保留 B**（沿用第二處原文的結論）／**缺區分條件**（兩處都不採用，維持討論中，待補充能區分適用情境的規則）。
  3. 【推論】先用『』引用推論所本的原始 spec 句子＋出處，再用一句話寫從它推出的結論；不得只寫結論、省略所本原文。
  4. 【覆蓋】這一輪的答案跟 spec 已寫清楚的內容不一致，等於要動到定稿文字時用這種——固定格式「這會覆蓋 `spec-<模組>.md`「<段落／`uc-`／Scenario 名稱>」原文：『逐字引用原文』→ 若採用新答案，需改為：『新文字』」。用【覆蓋】的列，「採用」欄一律填「暫不覆蓋，維持 spec 原文；待 CR 核准後再改」，「狀態」欄一律是 `blocked`，對應任務同步標 `blocked`——**不可以自動決議掉一個覆蓋 spec 的答案**。
  目的是讓人工事後一眼分辨這是 spec 寫的、spec 自相矛盾、loop 推出來的，還是要靠 CR 才能定案的覆蓋，不得用「應該」「我認為」「比較好」等說服性字眼取代引用。
  **凡是『』包住的文字，都必須與對應 `spec-<模組>.md` 逐字相符**（【覆蓋】只要求「原文：」之後、「→」之前那段逐字相符，「新文」不受此限）；收尾前執行 `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py` 驗證，有任何一筆比對不到就視為本輪未完成，要嘛修正引用使其逐字相符，要嘛承認引用錯誤重寫該列。
- **每一列「情況」欄開頭固定加 `[Level: <模組>/<uc 或 Screen ID>]` 標記**（有具體 Scenario 名稱時可再加一層 `/<Scenario 名稱>`），例如 `[Level: user-membership/uc-invite-member] 【引用原文】...`，讓人工不用回頭查任務清單就知道這筆問題屬於哪個顆粒度。
- **新增一列前先查重**：掃過本表既有列與各 `ui-<模組>.md`「待確認事項」，同一個 `uc-`／entity／Screen 已經有紀錄時不要重開一筆；若這輪的結論要推翻先前答案，「情況」欄要明講「推翻 OQ-xx」並說明差異來源，不能悄悄新增一筆看似獨立的問題。
- **「選項」欄若牽涉挑選既有類別**（角色、Use Case、Screen 等）：把該類別在 spec／本檔目前的**完整清單**列出（例如問角色就把角色表全部角色列出，不是只列候選的兩個），讓人工看得到還有沒有其他既有選項可用；清單裡真的沒有合適項目時，才能額外加一個「新增」選項並註明是新提案，不是既有項目。

| 編號 | 日期 | 模組 | 情況 | 選項 | 採用 | 依據 | 狀態 |
|---|---|---|---|---|---|---|---|
| OQ-01 | 2026-09-17 | kanban-basic | [Level: kanban-basic/s-swimlane-list] 【推論】`spec-kanban-basic.md`「Feature: Swimlane 管理」Background 僅寫『And 我已開啟一個名為 "產品開發看板" 的看板』，未描述如何從看板畫面前往 Swimlane 管理；推論 `s-swimlane-list` 的進入路徑需待 `s-board` 定案時一併確認。 | 1. 模組入口（獨立入口，不經看板） 2. 從 `s-board` 的某個操作進入 3. 併入 `s-board` 內嵌顯示，`s-swimlane-list` 不需獨立成畫面 | 暫定為模組入口，維持八段格式完整；待 `s-board` 定案時回頭確認或更正 | Feature: Swimlane 管理 Background | 自動決議 |
| OQ-02 | 2026-09-17 | kanban-basic | [Level: kanban-basic/s-stage-list] 【推論】`spec-kanban-basic.md`「Feature: Stage（階段）管理」Background 僅寫『And 我已開啟一個名為 "產品開發看板" 的看板』『And 看板目前的 Stage 依序為 "待辦"、"進行中"、"完成"』，未描述如何從看板畫面前往 Stage 管理；與 OQ-01 同類問題，推論 `s-stage-list` 的進入路徑同樣需待 `s-board` 定案時一併確認。 | 1. 模組入口（獨立入口，不經看板） 2. 從 `s-board` 的某個操作進入 3. 併入 `s-board` 內嵌顯示，`s-stage-list` 不需獨立成畫面 | 暫定為模組入口，維持八段格式完整；待 `s-board` 定案時回頭確認或更正 | Feature: Stage（階段）管理 Background | 自動決議 |
| OQ-03 | 2026-09-17 | kanban-basic | [Level: kanban-basic/uc-delete-stage] 【推論】`spec-kanban-basic.md` `uc-delete-stage` post 僅寫『若該 `stage` 內有 `card`，`card.stage` 更新為使用者選擇的目的 `stage`』，未寫目的 `stage` 是否可以是欲刪除的 `stage` 本身；推論 `s-stage-delete-dialog` 的目的 Stage 選擇欄位是否需排除該 Stage 本身待確認。 | 1. 排除欲刪除的 Stage 本身（清單只列其他 Stage） 2. 不排除，允許選擇同一個 Stage（形同不轉移） 3. 沿用 spec 原文，維持討論中不預設 | 暫定排除欲刪除的 Stage 本身（`s-stage-delete-dialog` 資料段驗證欄標 ⚠️），維持該畫面「討論中」；待補充規則後確認 | uc-delete-stage post | 自動決議 |
