# T-03-be-card 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Review 第 1 輪：退回

### 第 1 輪（Review）驗證結果

1. **建置與測試（自己重跑）**：`./gradlew clean build --no-daemon --rerun-tasks` → BUILD SUCCESSFUL（2m24s）。彙總 test-results XML：71 個測試，0 失敗、0 錯誤、0 略過（kanban-core：BoardTest 17、CardTest 8、UserTest 10、NoSpringDependencyTest 1；kanban-spring：card-editing 8、swimlane-management 7、stage-management 8、create-user-account 7、user-login-logout 4、SmokeTest 1）。
2. **spec 對應**：`card-editing.feature` 與 spec「Feature: Card（卡片）編輯」Gherkin 用 diff 比對，除檔頭兩行註解外逐字相同；6 個 uc、8 個 Scenario 都有 step definition 且打正式端點、從 JPA 驗證。抽查 `@uc-add-card @fail-p1`：`UserSteps.thenErrorMessage` 比對回應 message＝"卡片標題不可為空"，`thenNoNewCardCreated` 比對未刪除卡片數不變，成立；`@uc-delete-card @fail-p2`：取消後沒有呼叫 DELETE，`deleted=false`，成立。問題：`uc-move-card-stage` 的「異動前後的 Stage」只驗了後（D-02）。
3. **kanban-core 純度**：`grep -rnE "import (org\.springframework|jakarta\.persistence|lombok)" kanban-core/src/main` 無結果；NoSpringDependencyTest 通過。本任務沒有跨 aggregate 投影。
4. **任務邊界**：`git diff loop/implementation...HEAD --name-status` 程式碼改動在 kanban-core／kanban-spring 的 card 相關檔，外加 tasks.md 明文允許的 BoardController／BoardApplicationService／BoardSteps（OQ-IMPL-17 範圍）與刪除 NoOpCardLookupPort／FakeCardLookupPort；`.state/` 只有本任務目錄與兩則新增 ADR。邊界乾淨。
5. **OQ**：Dev 沒有開任何 OQ（`loopctl show` 無 OQ）；交接摘要第 4、5 點其實是 spec 沒定義 fail 的高風險事項（D-03）。
6. 非前端任務，不適用。

**主要問題（D-01）**：專案內沒有任何 `@Transactional`，`removeSwimlane(confirmed=true)`／`removeStage(destinationStageId)` 先逐張改卡片並各自提交，之後才做 board 的可刪除檢查。只剩 1 個 Swimlane 時確認刪除，卡片會被刪掉但 Swimlane 被 `MINIMUM_SWIMLANE` 拒絕而留下；目的 Stage 不屬於該 board 時卡片會被搬到不存在的 Stage。違反 `uc-delete-swimlane` fail p1『拒絕，該 `swimlane` 不被刪除』與 `uc-delete-stage` post。

### 判定：退回（doing）

D-01（協調流程非原子／未驗證目的 Stage）、D-02（Cucumber 斷言不足）、D-03（規格未定義的失敗路徑要開 OQ）交下一輪 Dev 處理。
