# T-08-be-feature-cr-board open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-08-be-feature-cr-board-01

[Level: F06-feature-cr-board/uc-view-feature-cr-board]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-19）
- 狀態：待處理

情況：【推論＋所本原文】
引用（spec-feature-cr-board.md usecase 區塊 post）：
p2：『標籤格式為「^CR-\d{3}$」的 `card` 視為 CR 卡；帶有「affects:F\d{2}$」標籤時，顯示在對應 Feature 底下，並依所在 Stage 角色顯示狀態（例如角色為 Start 顯示「開發中」）』
p3：『CR 卡的「affects」標籤指到不存在的 Feature 編號時，該 `card` 列入 orphan CR 清單』

推論：p2 只說「顯示在對應 Feature 底下」，沒有明講這個「對應 Feature」是否必須真的存在一張標籤為該編號的 Feature 卡。但 Gherkin Scenario「檢視 CR 影響哪個 Feature 以及其狀態」只給了一張 CR 卡（標籤 "CR-004"，帶 "affects:F01"），沒有任何標籤為 "F01" 的 Feature 卡存在，卻要求「Feature "F01" 底下應該顯示一筆狀態為「開發中」的 CR "CR-004"」。若「對應 Feature」要求必須有實體 Feature 卡才能顯示，這個 Scenario 會過不了；若不要求，則與 Scenario「CR 指到不存在的 Feature 時列為 orphan」（CR-099 affects:F99，沒有 F99 卡）同時成立會有點微妙：兩者都沒有對應的 Feature 卡，但一個要「顯示在 Feature 底下」、一個要「列入 orphan」。

因此本次實作採用「兩者不互斥」的讀法：affects 標籤一律依目標編號分組顯示（沒有實體卡片時建立一個狀態預設「未開發」的佔位 Feature 群組），同時 orphan 判定仍只看「該編號是否真的有一張 Feature 卡」——沒有的話該 CR 同時出現在 orphan 清單「和」其分組底下。這個讀法能讓兩個 Scenario 都通過，但 spec 原文沒有明講「同時出現在兩處」是否為預期的使用者體驗（例如 UI 要不要在分組下也標記「orphan」提示），ui-feature-cr-board.md 也沒有描述這個組合狀態。
問題：這個「CR 同時出現在對應 Feature 分組底下、又列入 orphan 清單」的雙重顯示，是否為預期行為？如果不是，該用哪一種正確語意（例如：只有真的有 Feature 卡的編號才建立分組，沒有的一律只進 orphan、不建立分組）？
選項：A. 維持目前實作（affects 目標一律建立分組＋額外做 orphan 檢查，兩者不互斥）；B. 改為「沒有 Feature 卡的 affects 目標只進 orphan 清單、不建立/不顯示任何 Feature 分組」（但這樣「檢視 CR 影響哪個 Feature 以及其狀態」這個 Scenario 現有的 Given 資料就過不了，需要同時修正該 Scenario 補一張 F01 Feature 卡，等同於認定該 Scenario 目前少給了一筆前置資料）。
