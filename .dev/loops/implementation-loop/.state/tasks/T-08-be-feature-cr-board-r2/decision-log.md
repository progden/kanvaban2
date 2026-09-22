# T-08-be-feature-cr-board-r2 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：CR-013 差異修正

### 這一輪做了什麼

本任務是 T-08-be-feature-cr-board 的修訂實例，範圍限定為 CR-013 的差異（`.dev/CR.md` CR-013：「本 CR 只涵蓋 spec 修正，程式碼變更留待後續任務／D-xx 處理」）。spec 端在 T-08 合併之後已由人工定案並修改完成（commit `aadc005`），本輪只需同步程式碼：

1. `FeatureCrBoardCalculator.calculate`：原本 affects 目標找不到對應 Feature 卡時，會 `putIfAbsent` 建立一個狀態「未開發」的佔位 `FeatureEntry`，同時仍把該 CR 掛進 `crsByFeature`。改成：只有 `featureIdsWithCard` 真的含有該編號時才掛進 `crsByFeature`；找不到就只標記 `isOrphan = true`，迴圈結束後加入 orphan 清單一次（沿用既有 D-01 修正的「一張 CR 卡只列一次」邏輯）。不再有任何佔位 Feature 產生，Feature 分組與 orphan 判定互斥，符合 CR-013 改寫後的 `uc-view-feature-cr-board` post p2「該編號存在對應 Feature 卡時」前提。
2. 類別 Javadoc 同步更新，移除舊版「不互斥、建立佔位 Feature」的說明，改寫成 CR-013 後的互斥語意。
3. `kanban-spring/src/test/resources/features/feature-cr-board.feature`：依 convention「本檔為 spec Feature 的逐字複製」，補上 Scenario「檢視 CR 影響哪個 Feature 以及其狀態」新增的 `Given 卡片 "basic-kanban" 標籤為 "F01"` 與 `@CR-013` tag（原檔跟 spec 已經不同步，這次一併補齊，不是本任務新增的差異之外變更——只跟這個 Scenario 有關）。既有 step `givenCardWithLabel`（`@Given("卡片 {string} 標籤為 {string}")`）已經能處理新增的 Given，不需要新增 step definition。
4. `FeatureCrBoardCalculatorTest` 新增 `crAffectingUnknownFeatureDoesNotCreatePlaceholderFeature`：驗證 affects 指到不存在 Feature 編號時，`view.features()` 是空的（沒有佔位 Feature）。

### 規格沒寫清楚之處的處理

無新增。CR-013 本身已經是人工定案的結論（見 `.dev/CR.md` CR-013、`T-08-be-feature-cr-board/open-questions.md` 的 `OQ-T-08-be-feature-cr-board-02` 解除說明），本輪照做，不需另開 OQ。

一張 CR 卡同時有多個 affects 目標、部分有對應 Feature 卡、部分沒有時的行為，spec／CR-013 都沒有額外定義；沿用上一實例既有邏輯（有卡片的目標仍照常掛入該 Feature 分組，同時只要有任一目標缺卡片就整張 CR 卡也列入 orphan），這不是本次 CR 的範圍，維持原狀，不另開 OQ。

### spec 對應與涵蓋範圍

- `uc-view-feature-cr-board`（唯一 usecase）：本輪只改 post p2／p3 互斥判定這一個差異，其餘 4 條 post（p1 狀態、p4 雙 Feature 標籤警告、p5 大小寫不分）未受影響，沿用原實例既有實作與測試，未重新驗證邏輯（原有測試持續通過）。
- Scenario 對應：5 個 Scenario 全部沿用，`.feature` 檔已同步到目前 spec 逐字內容（含 `@CR-013` tag）。

### 待確認事項

無新增 OQ。

### Check（實際跑的建置與測試）

`./gradlew clean build --no-daemon` → **BUILD SUCCESSFUL in 2m 54s**。
彙總 `kanban-*/build/test-results/test/*.xml`：kanban-core 6 個測試類別全綠；kanban-spring 全部測試類別／feature 檔 0 failure／0 error，其中 `FeatureCrBoardCalculatorTest` 8 個測試（原 7 個＋本輪新增 1 個）全綠，Cucumber `feature-cr-board.feature` 5 個 Scenario 全綠（含更新後的「檢視 CR 影響哪個 Feature 以及其狀態」）。
