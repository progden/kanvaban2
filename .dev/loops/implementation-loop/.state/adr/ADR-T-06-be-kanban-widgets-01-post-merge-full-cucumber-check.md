## ADR-T-06-be-kanban-widgets-01：合併回整合分支後，應補跑一次完整 RunCucumberTest

- 狀態：Accepted
- 日期：2026-09-19
- 提出者：Dev（T-06-be-kanban-widgets）

### 背景（Context）

`ADR-T-03-be-card-02` 已經記錄「跨 Feature 共用逐字 Gherkin 步驟文字時要重用既有 step
definition，不要各自宣告」的原則，但本輪執行 `./gradlew clean build --no-daemon` 時仍
發現 `BoardClockSteps`（T-05-be-board-clock 產出）與 `FeatureCrBoardSteps`
（T-08-be-feature-cr-board 產出）各自獨立宣告了完全相同的步驟文字「我已登入系統，並開啟
Board {string}」（詳見 `OQ-T-06-be-kanban-widgets-01`），導致 `RunCucumberTest` 底下
85 個 kanban-spring 測試有 58 個失敗（涵蓋 F01／F02／F04／F06 全部既有 feature，不限
F03）。原因是 T-05、T-08 分別在各自的 worktree 裡開發，彼此看不到對方尚未合併的程式碼，
ADR-T-03-be-card-02 的原則就算讀過也無從套用（沒有東西可以「重用」）；這個衝突要等兩者都
合併回整合分支、glue package 真的擺在一起時才會出現。

### 決策（Decision）

驅動流程（或 Review 角色）在把一個任務的分支 `merge --no-ff` 回整合分支 `loop/
implementation` 之後，應該在整合分支上補跑一次**完整**的 `./gradlew :kanban-spring:test
--tests "io.progden.kanban.spring.cucumber.RunCucumberTest"`（不是只跑該任務自己新增
的 feature 檔或 scoped 測試），才能抓到「兩個各自獨立開發、當下互相看不到彼此」的 worktree
合併後才會出現的步驟文字撞名。若合併後這個檢查失敗，比照 `ADR-T-03-be-card-02` 的分流模式
處理（其中一方改成呼叫另一方暴露的 package-private 方法，不要兩邊各自保留宣告）。

### 考慮過的替代方案（Alternatives）

- 每個 Dev 輪在自己 worktree 完成時，先手動檢查所有已合併任務的 step definition 清單有沒有
  撞名。放棄原因：worktree 之間互相看不到彼此「尚未合併」的程式碼（本例的 T-05、T-08 屬於這種
  情況），這個檢查在 Dev 輪當下技術上做不到，只有合併後才驗得出來。
- 放著不管，等到剛好有任務需要動到衝突的兩個檔案時再一併修。放棄原因：目前這個衝突已經讓
  `./gradlew build` 全面失敗，任何後續任務（含目前的 T-06）都無法用完整建置驗證自己的改動有
  沒有引入新的迴歸，风险持續累積。

### 後果（Consequences）

- 這個 ADR 本身不強制任何後續任務去修 `BoardClockSteps`／`FeatureCrBoardSteps` 的既有衝突
  （見 `OQ-T-06-be-kanban-widgets-01`，非阻塞，留給人工或下一個會動到這兩個檔案的任務）；
  但任何任務在合併回整合分支後，都應該養成補跑完整 `RunCucumberTest` 的習慣，而不是只信任
  自己 worktree 內的測試結果。
- 驅動腳本 `run-loop.sh` 或 Review 提示詞若之後要調整合併流程，應把「合併後跑一次完整
  RunCucumberTest」納入合併後檢查清單。
