# T-08-be-feature-cr-board-r2 open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-08-be-feature-cr-board-r2-01

[Level: F06 / uc-view-feature-cr-board post p2,p3]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-ambiguous
- 開立：Review 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】

`.dev/F06-feature-cr-board/spec-feature-cr-board.md` `uc-view-feature-cr-board` post p2（CR-013 後）：
『標籤格式為「^CR-\d{3}$」的 `card` 視為 CR 卡；帶有「affects:F\d{2}$」標籤且該編號存在對應 Feature 卡時，顯示在對應 Feature 底下，並依所在 Stage 角色顯示狀態（例如角色為 Start 顯示「開發中」）』

同檔 post p3：
『CR 卡的「affects」標籤指到不存在的 Feature 編號時，該 `card` 列入 orphan CR 清單』

同檔「其他名詞」表：
『affects 標籤 | 格式為「affects:F01」，只在 CR 卡上有意義，標示此 CR 影響哪個 Feature，可多個』

`.dev/CR.md` CR-013 背景段：
『2026-09-22 人工定案：CR 應該跟著真的存在的 Feature 卡顯示；沒有連到真的 Feature 卡時要清楚標記為 orphan，兩者互斥，採 ui 檔「改列入」的語意』

推論：以上四段都只描述「單一 affects 目標」的情形。affects 標籤明寫「可多個」，但一張 CR 卡同時有多個 affects 目標、其中部分有對應 Feature 卡、部分沒有時，「兩者互斥」要以每個目標各自判定（同一張 CR 卡既出現在有卡片的 Feature 底下、又出現在 orphan 清單）還是以整張卡判定（只要有任一目標缺卡片就整張只進 orphan、不顯示在任何 Feature 底下），規格與 CR-013 都未定義。本輪實作「FeatureCrBoardCalculator.calculate」採前者（逐目標判定，有卡片的目標照常掛入該 Feature 分組，同時只要有任一目標缺卡片就整張 CR 卡也列入 orphan），Review 已跑 `./gradlew clean build --no-daemon` 確認測試全綠，此行為沿用自前一實例，本輪未改動；Dev 第 1 輪決策紀錄也記下此點但選擇不開 OQ，Review 補開以留存。

問題：一張 CR 卡有多個 affects 目標、部分有對應 Feature 卡部分沒有時，「顯示在 Feature 底下」與「列入 orphan」的互斥判定要以目標為單位還是以卡片為單位？

選項：A. 維持現行實作，逐目標判定（有卡片的目標照常分組，同時整張卡進 orphan），並在 spec post p2／p3 補一句說明多目標情形；B. 改成以卡片為單位，只要有任一目標缺卡片就整張 CR 卡只進 orphan、不掛入任何 Feature 分組，另開 CR 修改 post p2／p3 並補一則 Scenario。
