# T-08-be-feature-cr-board 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Dev 第 1 輪：實作 Feature／CR 追蹤表唯讀投影

### 這一輪做了什麼

實作 spec-feature-cr-board.md 唯一的 usecase `uc-view-feature-cr-board`（Feature／CR 追蹤表），依 CLAUDE.md 慣例把跨 aggregate 讀取投影放在 `kanban-spring` 的 `io.progden.kanban.query.featurecrboard`：

- `FeatureCrBoardCalculator`：純函式，輸入 `CardLabelProjection`（卡片標籤＋所在 Stage 角色）清單，解析 Feature 卡（`^F\d{2}$`）／CR 卡（`^CR-\d{3}$`）／affects 標籤（`^affects:F\d{2}$`），皆不分大小寫，輸出 `FeatureCrBoardView`（features／orphanCrIds／warnings）。
- `FeatureCrBoardQueryService`：`@Transactional(readOnly = true)`，讀 `BoardJpaRepository`／`CardJpaRepository`（未刪除卡片）組出 `CardLabelProjection` 交給 calculator。
- `FeatureCrBoardController`：`GET /api/boards/{boardId}/feature-cr-board`，登入驗證沿用 `BoardController` 慣例；看板不存在回 404、未登入回 401（`uc-view-feature-cr-board` 本身 `fail: {}` 無定義失敗情境）。
- `KanbanApplication` 補上 `@ComponentScan(basePackages = {"io.progden.kanban.spring", "io.progden.kanban.query"})`：預設 `@SpringBootApplication` 只掃描 `io.progden.kanban.spring`，不加這行整個 Spring context 會因為找不到 `FeatureCrBoardQueryService` bean 而啟動失敗（已在本輪建置中實測到這個錯誤才發現，見 Check）。

### 規格沒寫清楚之處的處理

1. **CR 沒有 affects 標籤、或一張卡同時有兩個 CR 標籤**：spec 未定義。選擇：沒有 affects 標籤的 CR 不出現在任何清單（不算 orphan，因為 orphan 定義明確要求「affects 指到不存在的 Feature 編號」）；一張卡多個 CR 標籤時逐一視為獨立 CR 處理。低風險技術決定，寫在 `FeatureCrBoardCalculator` 類別註解。
2. **CR 的 affects 目標沒有對應 Feature 卡時，要不要仍然「顯示在該 Feature 底下」**：這是本輪唯一開的 OQ（`OQ-T-08-be-feature-cr-board-01`，高／不阻塞）。Gherkin Scenario「檢視 CR 影響哪個 Feature 以及其狀態」只給一張 CR 卡（affects:F01），沒給任何 F01 Feature 卡，卻要求輸出要顯示「Feature F01 底下有 CR-004」；同時 Scenario「CR 指到不存在的 Feature 時列為 orphan」的 F99 也沒有 Feature 卡，卻要求 CR-099 列入 orphan。兩個 Scenario 用的是同一種「沒有 Feature 卡」情境，但要求的行為看似互斥。本輪採「不互斥」讀法：affects 目標一律建立（或沿用）一個 Feature 分組（沒有實體卡片時狀態預設「未開發」），同時 orphan 判定只看該編號是否真的有一張 Feature 卡——沒有的話該 CR 同時出現在分組底下「和」orphan 清單。這個讀法讓兩個 Scenario 都能通過，已寫入 OQ 供人工確認是否為預期語意。

### spec 對應與涵蓋範圍

- 實體／usecase：F06 spec 沒有實體表（純衍生，見「其他名詞」），只有 `uc-view-feature-cr-board`；本輪完整覆蓋其 5 個 Scenario（post p1～p5）。
- 未涵蓋／延後：無——F06 usecase 只有一個，本任務範圍已全部做完。

### 待確認事項

- `OQ-T-08-be-feature-cr-board-01`（高／不阻塞）：CR 的 affects 目標沒有 Feature 卡時，是否應該「同時」出現在對應 Feature 分組底下與 orphan 清單，還是應該改成互斥（只進 orphan、不建立分組），需人工確認 spec 原意。

### Check（實際跑的指令與結果）

- `./gradlew --no-daemon compileJava compileTestJava` → BUILD SUCCESSFUL
- `./gradlew --no-daemon build`：第一次因 `@ComponentScan` 缺漏，Spring context 啟動失敗（`NoSuchBeanDefinitionException`），43 個測試（含既有 F01 模組）全部連帶失敗；補上 `@ComponentScan` 後重跑，only F06 兩個 Scenario 因角色字串大小寫（"Start"/"Done" 需轉大寫才是合法 `StageRole` 值）與「CR 分組」判斷失敗；修正後最終 `./gradlew --no-daemon build` → **BUILD SUCCESSFUL**，48 個測試（含新增 F06 5 個 Cucumber Scenario＋5 個 `FeatureCrBoardCalculatorTest` 單元測試）全綠。

## 2026-09-19 Dev 第 2 輪：修正 D-01／D-02／D-03

### 處理範圍
只處理 `fixes.md` 裡三條 `D-xx`，沒有新增功能，沒有動 spec／ui／conventions。

### D-01：orphan CR 清單重複列出同一張 CR
`FeatureCrBoardCalculator.calculate` 原本在 affects 迴圈裡，每個「找不到 Feature 卡」的 affects
目標都會 `orphanCrIds.add(cr.crId())` 一次。改成每張 CR 卡先用一個 `boolean isOrphan` 累積「這張卡
的 affects 目標裡有沒有任何一個指到不存在的 Feature」，迴圈跑完後只 `add` 一次。理由：post p3 主詞
是「該 `card`」，一張卡只該出現一次，不管它有幾個指到不存在 Feature 的 affects 標籤。
補測試 `crAffectingMultipleUnknownFeaturesIsOrphanOnlyOnce`：一張 CR 卡帶兩個不存在的 affects 目標，
斷言 `orphanCrIds` 恰好一筆。

### D-02：p4 警告文字與行為不一致、缺「不影響其他卡片」測試
兩個子問題分開處理：
1. 文字與行為二選一：選擇**改文字**、不改行為（CR 標籤仍照常統計）。理由：post p4 只講「同一張卡片
   帶兩個 Feature 標籤」時忽略「其」Feature／CR 統計，字面上「其」指的是這張卡片本身的 Feature 標籤
   造成的統計混亂（兩個 Feature 選哪個？），沒有明文要求連這張卡片上獨立的 CR 標籤也一併忽略；而且
   若把 CR 標籤也一起丟掉，等於讓一張卡片的 Feature 標籤格式錯誤連帶影響它的 CR 記錄，屬於範圍更大
   的行為變更，spec 沒有依據。所以把警告文字改成「已忽略其 Feature 標籤（該卡片上的 CR 標籤仍照常
   統計）」，如實描述目前的行為，不改行為本身。
2. 補測試 `cardWithTwoFeatureLabelsDoesNotAffectOtherCards`：輸入雙 Feature 標籤卡＋一張正常 F03
   （DONE）＋一張正常 CR-004（affects:F03），斷言 F03 狀態、CR-004 歸屬與狀態都正確、warnings 只有
   一筆、orphan 清單是空的。Cucumber step `thenOtherCardsUnaffected` 補了一段 Javadoc 註解：這個
   Scenario 的 Background 只建立一張卡，這一步只能驗到「這張卡片自己的 F01／F02 沒出現」，「其他正
   常卡片不受影響」實際由上面那個單元測試負責，避免斷言看起來在驗別的東西。

### D-03：`@ComponentScan` 改成 `scanBasePackages`
拿掉獨立的 `@ComponentScan(basePackages = {...})`，改成
`@SpringBootApplication(scanBasePackages = "io.progden.kanban")`（共同父套件，涵蓋
`io.progden.kanban.spring` 與 `io.progden.kanban.query`）。避免直接宣告的 `@ComponentScan` 取代掉
`@SpringBootApplication` 內建那份、連帶拿掉 Spring Boot 預設的 `TypeExcludeFilter`／
`AutoConfigurationExcludeFilter`。

### OQ 核對
沒有新開 OQ；`OQ-T-08-be-feature-cr-board-01`／`-02` 是上一輪留下的（ui／spec 對「orphan 與
affects 同時成立」的矛盾），這輪修的三條 D-xx 都不影響那個判斷，維持原狀，交給人工處理。

### Check（實際跑的建置與測試）
`./gradlew clean build --no-daemon`：**BUILD SUCCESSFUL in 2m 24s**。
`kanban-spring` 測試結果 XML 彙總：`FeatureCrBoardCalculatorTest` 從 5 筆增加到 7 筆（新增
`crAffectingMultipleUnknownFeaturesIsOrphanOnlyOnce`、`cardWithTwoFeatureLabelsDoesNotAffectOtherCards`），
全部 0 failure／error；Cucumber「Feature／CR 追蹤表」5 個 Scenario 仍全綠。
