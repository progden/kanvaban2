# T-08-be-feature-cr-board-r2 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：附保留核准

判定：附保留核准（`done`）。

### 1. 自行重跑建置與完整測試

`./gradlew clean build --no-daemon` → `BUILD SUCCESSFUL in 2m 55s`（14 actionable tasks，全部 executed，不是 up-to-date 快取）。

彙總 `kanban-*/build/test-results/test/*.xml` 的 testsuite 屬性，34 個 suite 全部 `failures=0 errors=0`，其中與本任務直接相關者：

- Cucumber「Feature／CR 追蹤表」：`tests=5 fail=0 err=0`（spec 的 5 個 Scenario 全數執行）。
- `io.progden.kanban.query.featurecrboard.FeatureCrBoardCalculatorTest`：`tests=8 fail=0 err=0`（含本輪新增的 `crAffectingUnknownFeatureDoesNotCreatePlaceholderFeature`）。
- 其他模組（board／card／board-clock／widgets／user-membership）未受波及，皆 0 failure。

### 2. spec 對應核對（`uc-view-feature-cr-board`）

- `.feature` 檔與 spec 逐字一致：用 `awk` 抽出 spec 的 ```gherkin 區塊與 `kanban-spring/src/test/resources/features/feature-cr-board.feature` 做 `diff`，唯一差異是檔首兩行來源註解（`# 本檔為 …逐字複製`），與其他 10 個 `.feature` 檔的既有慣例相同，其餘逐字相同——包含 CR-013 新增的 `@CR-013` tag 與 `Given 卡片 "basic-kanban" 標籤為 "F01"`。
- CR-013 差異本體：`FeatureCrBoardCalculator.calculate` 的 affects 迴圈已移除 `featuresById.putIfAbsent(target, …)`，改成 `if (featureIdsWithCard.contains(target))` 才 `crsByFeature.computeIfAbsent(...)`、`else` 只設 `isOrphan = true`，確實與 post p2 改寫後的『且該編號存在對應 Feature 卡時』前提一致，且分組與 orphan 互斥。`featureIdsWithCard` 在卡片迴圈**結束後**才快照，因此不受卡片先後順序影響（Feature 卡排在 CR 卡之後也正確）。
- 新測試有效性核對：舊實作會 `putIfAbsent` 建立 F99 佔位 Feature，`view.features()` 非空，`assertTrue(view.features().isEmpty())` 在舊碼上會失敗——這則測試確實鎖住 CR-013 的行為，不是空轉。
- Cucumber step：新增的 `Given 卡片 "basic-kanban" 標籤為 "F01"` 由既有 `givenCardWithLabel`（`@Given("卡片 {string} 標籤為 {string}")`）承接，放在 `anyStageId()`（stages[0]，角色 NONE），與 CR-013 變更內容『不指定 Stage，因為這則 Then 沒有斷言 F01 自身的狀態』一致；Then 只斷言 F01 底下的 CR-004 狀態為「開發中」，未受影響。
- `@fail-pN` 抽查：本 usecase 的 usecase 區塊 `fail: {}` 為空，spec 未定義任何失敗分支，亦無 `@fail-` tag 的 Scenario，故無可抽查對象（非遺漏）。
- 未受本 CR 影響的 post p1／p4／p5 與對應 3 個 Scenario 沿用前一實例實作，本輪測試仍全綠。

### 3. `kanban-core` 純度

`grep -rnE "org\.springframework|jakarta\.persistence" kanban-core/src/main/java/` 無任何命中；`io.progden.kanban.core.domain.NoSpringDependencyTest` `tests=1 fail=0`。本任務的讀取投影位於 `kanban-spring` 的 `io.progden.kanban.query.featurecrboard.*`，未散落到任何 aggregate 內，符合規則。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat` 共 7 檔：
- `kanban-spring/.../query/featurecrboard/FeatureCrBoardCalculator.java`（+6/−9 含 Javadoc）
- `kanban-spring/src/test/java/.../FeatureCrBoardCalculatorTest.java`（+9）
- `kanban-spring/src/test/resources/features/feature-cr-board.feature`（+3/−2）
- `.state/tasks/T-08-be-feature-cr-board-r2/` 底下 4 個檔（decision-log.md／rounds.log／state.md／status）

沒有動到其他 aggregate、`.dev/conventions/**`、`scripts/**`、spec／ui 文件本體、`.state/tasks.md`、`.state/archive/**`、別的任務目錄。`git status --short` 乾淨。範圍與 `.state/tasks.md` 該列「只改這個差異，不動 F06 其他投影邏輯」一致。

### 5. 交接摘要的待確認事項／OQ 核對

Dev 交接摘要「待確認事項」寫「無新增 OQ」，`loopctl show` 亦無 OQ。核對其決策紀錄後，有一項它自己指出但選擇不記錄的規格空白：『一張 CR 卡同時有多個 affects 目標、部分有對應 Feature 卡、部分沒有時的行為，spec／CR-013 都沒有額外定義』。這確實是規格未定義（不是必須違反定稿原文），依規則應開成不阻塞的「高」等級 OQ 留存，而不是只寫在決策紀錄裡消失。Review 已補開 `OQ-T-08-be-feature-cr-board-r2-01`（等級 高、阻塞 no、接手 人工、`spec-ambiguous`），內含 spec post p2／p3、「其他名詞」表與 `.dev/CR.md` CR-013 背景段的逐字引用。此 OQ 不影響 CR-013 本身的驗收（CR-013 的 5 個 Scenario 與 8 個單元測試全綠），故不阻塞。

### 6. 前端核對

本任務為後端任務，無 `.dev/ui-prototype/` 對應設計稿需核對。

### 保留事項（各自接手者）

1. `OQ-T-08-be-feature-cr-board-r2-01`（多個 affects 目標、部分有對應 Feature 卡時的互斥判定要以目標還是以卡片為單位）——**接手：人工**。需人工定案後視情況開新 CR；沒有任何既有任務的產出範圍涵蓋它，故 `--owner 人工`。
2. `.dev/CR.md` CR-013「狀態」欄目前仍是「待處理」，需在本任務驗收後改為「處理完成」——**接手：人工**。此事已載明於 `.state/tasks.md` 本任務列的備註（『完成並驗收後，人工把 `.dev/CR.md` CR-013 改「處理完成」』），且 `.dev/**` 是本 loop 禁止修改的上游依據，Dev／Review 皆不可代勞，故不另開 OQ。
