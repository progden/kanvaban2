# T-08-be-feature-cr-board open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-08-be-feature-cr-board-01

[Level: F06-feature-cr-board/uc-view-feature-cr-board]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-19）
- 狀態：**已由 OQ-T-08-be-feature-cr-board-02 取代並解除（2026-09-22）**，見該則解除說明與 CR-013。

情況：【推論＋所本原文】
引用（spec-feature-cr-board.md usecase 區塊 post）：
p2：『標籤格式為「^CR-\d{3}$」的 `card` 視為 CR 卡；帶有「affects:F\d{2}$」標籤時，顯示在對應 Feature 底下，並依所在 Stage 角色顯示狀態（例如角色為 Start 顯示「開發中」）』
p3：『CR 卡的「affects」標籤指到不存在的 Feature 編號時，該 `card` 列入 orphan CR 清單』

推論：p2 只說「顯示在對應 Feature 底下」，沒有明講這個「對應 Feature」是否必須真的存在一張標籤為該編號的 Feature 卡。但 Gherkin Scenario「檢視 CR 影響哪個 Feature 以及其狀態」只給了一張 CR 卡（標籤 "CR-004"，帶 "affects:F01"），沒有任何標籤為 "F01" 的 Feature 卡存在，卻要求「Feature "F01" 底下應該顯示一筆狀態為「開發中」的 CR "CR-004"」。若「對應 Feature」要求必須有實體 Feature 卡才能顯示，這個 Scenario 會過不了；若不要求，則與 Scenario「CR 指到不存在的 Feature 時列為 orphan」（CR-099 affects:F99，沒有 F99 卡）同時成立會有點微妙：兩者都沒有對應的 Feature 卡，但一個要「顯示在 Feature 底下」、一個要「列入 orphan」。

因此本次實作採用「兩者不互斥」的讀法：affects 標籤一律依目標編號分組顯示（沒有實體卡片時建立一個狀態預設「未開發」的佔位 Feature 群組），同時 orphan 判定仍只看「該編號是否真的有一張 Feature 卡」——沒有的話該 CR 同時出現在 orphan 清單「和」其分組底下。這個讀法能讓兩個 Scenario 都通過，但 spec 原文沒有明講「同時出現在兩處」是否為預期的使用者體驗（例如 UI 要不要在分組下也標記「orphan」提示），ui-feature-cr-board.md 也沒有描述這個組合狀態。
問題：這個「CR 同時出現在對應 Feature 分組底下、又列入 orphan 清單」的雙重顯示，是否為預期行為？如果不是，該用哪一種正確語意（例如：只有真的有 Feature 卡的編號才建立分組，沒有的一律只進 orphan、不建立分組）？
選項：A. 維持目前實作（affects 目標一律建立分組＋額外做 orphan 檢查，兩者不互斥）；B. 改為「沒有 Feature 卡的 affects 目標只進 orphan 清單、不建立/不顯示任何 Feature 分組」（但這樣「檢視 CR 影響哪個 Feature 以及其狀態」這個 Scenario 現有的 Given 資料就過不了，需要同時修正該 Scenario 補一張 F01 Feature 卡，等同於認定該 Scenario 目前少給了一筆前置資料）。

## OQ-T-08-be-feature-cr-board-02

[Level: F06-feature-cr-board/uc-view-feature-cr-board]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-conflict
- 開立：Review 第 1 輪（2026-09-19）
- 狀態：**已解除（2026-09-22，人工決策，採選項 B：互斥、只列入 orphan）**

解除說明：人工開 CR-013，`uc-view-feature-cr-board` post p2 補上「該編號存在對應 Feature 卡時」前提，讓「顯示在對應 Feature 底下」與「列入 orphan」互斥，與 `ui-feature-cr-board.md`「改列入」的語意一致。Scenario「檢視 CR 影響哪個 Feature 以及其狀態」的 Given 補上一張標籤 "F01" 的 Feature 卡（不指定 Stage 角色，因為這則 Then 沒有斷言 F01 自身的狀態，只要它存在即可，避免混入跟本則測試目標無關的細節）——原 Scenario 少給了這筆前置資料，不是語意需要改。本輪只處理 CR 與 spec 修正；`kanban-spring` 的 `FeatureCrBoardCalculator`（或對應查詢邏輯）尚未同步改成沒 Feature 卡時只列入 orphan、不建佔位 Feature，程式碼變更留待後續任務／D-xx 處理。詳見 `.dev/CR.md` CR-013。

情況：【兩處矛盾並列】
本則取代 OQ-T-08-be-feature-cr-board-01：該則說 ui 檔『也沒有描述這個組合狀態』，但 ui 檔其實有寫，而且跟 spec 範例情境衝突。

引用一（.dev/F06-feature-cr-board/ui-feature-cr-board.md，s-feature-cr-board「資料」表「CR 所屬 Feature」列）：
『依 post p2；指到不存在的 Feature 編號時改列入 orphan CR 清單（post p3）』

引用二（.dev/F06-feature-cr-board/spec-feature-cr-board.md，Scenario「檢視 CR 影響哪個 Feature 以及其狀態」）：
『Given 卡片 "看板時間" 標籤為 "CR-004"，並帶有 "affects:F01" 標籤，目前在角色為 Start 的 Stage』
『Then Feature "F01" 底下應該顯示一筆狀態為「開發中」的 CR "CR-004"』

引用三（同 spec，Scenario「CR 指到不存在的 Feature 時列為 orphan」）：
『Given 卡片 "某 CR" 標籤為 "CR-099"，並帶有 "affects:F99" 標籤』
『Then "CR-099" 應該出現在 orphan CR 清單中』

引用四（同 spec，檔頭說明）：
『資料來源是看板卡片上既有的「標籤」欄位（既有欄位，零 CR），不讀 `.dev/` 檔案、不另開 aggregate。』

引用五（同 ui 檔，「Feature 狀態」列）：
『衍生：該 Feature 卡所在 Stage 的 `stage.role`，對應「其他名詞」表「狀態」定義（NONE＝未開發、START＝開發中、DONE＝已完成）』

推論：依引用四，判斷 Feature 編號「存不存在」的唯一資料來源是卡片標籤，也就是有沒有 Feature 卡。引用二（F01）和引用三（F99）的 Given 都沒有對應的 Feature 卡，spec 卻要求一個「顯示在 Feature 底下」、另一個「列入 orphan」。引用一的「改列入」是二選一（互斥），照它做的話，引用二的 Scenario 會失敗。目前實作讓沒有 Feature 卡的 CR 同時出現在兩處，雖然符合 spec（上游），卻違反引用一的「改列入」。
推論：目前實作也替沒有 Feature 卡的編號建了一個狀態為「未開發」的佔位 Feature。引用五說狀態來自「該 Feature 卡所在 Stage」，沒有卡片時這個「未開發」是實作自己給的，規格沒寫。
推論：依規則書「引用方向 spec ← ui ← design，以 spec 為準」，後端先照 spec 做（兩個 Scenario 都要過），所以不阻塞本任務。但 T-20-fe-feature-cr-board 會依 ui 檔的「改列入」呈現，結果會跟後端不一致，需要人工開 CR 統一。
問題：affects 指到「沒有 Feature 卡」的編號時，CR 應該（a）只列入 orphan、（b）只顯示在該編號的 Feature 底下，還是（c）兩處都出現？另外，沒有 Feature 卡的佔位 Feature 要顯示什麼狀態？
選項：A. 維持兩處都出現（目前實作），開 CR 把 ui 檔「改列入」改成「同時列入」，並定義佔位 Feature 的狀態；B. 互斥、只列入 orphan：開 CR 在 spec Scenario「檢視 CR 影響哪個 Feature 以及其狀態」的 Given 補一張標籤 "F01" 的 Feature 卡，後端改成沒有 Feature 卡就不建分組；C. 另外定義「Feature 編號存在」的判斷依據（例如固定清單），開 CR 同時修改 spec 與 ui。
