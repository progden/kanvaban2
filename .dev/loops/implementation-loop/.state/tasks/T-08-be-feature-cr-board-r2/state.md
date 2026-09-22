# T-08-be-feature-cr-board-r2 state

> 2026-09-22 Review 第 1 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

現在狀態：Review 第 1 輪附保留核准，status＝`done`，可合併回 `loop/implementation`。

驗證結果：Review 自行跑 `./gradlew clean build --no-daemon` → BUILD SUCCESSFUL in 2m 55s，34 個
test suite 全部 0 failure／0 error，含 Cucumber「Feature／CR 追蹤表」5 個 Scenario 與
FeatureCrBoardCalculatorTest 8 個單元測試。`.feature` 檔與 spec gherkin 區塊 diff 後僅差檔首兩行
來源註解（既有慣例），CR-013 的 `@CR-013` tag 與新增 Given 皆逐字相符。`FeatureCrBoardCalculator`
已移除佔位 Feature 的 `putIfAbsent`，分組與 orphan 互斥，新測試在舊碼上會失敗（有效測試）。
`kanban-core` 無 Spring／JPA import，投影位於 `io.progden.kanban.query.featurecrboard`。
改動範圍 7 檔，未越界，工作區乾淨。

保留事項：
1. OQ-T-08-be-feature-cr-board-r2-01（多個 affects 目標部分有／部分無 Feature 卡時的互斥判定單位，
   spec 未定義；現行逐目標判定）——接手：人工，不阻塞。
2. `.dev/CR.md` CR-013 狀態欄需由人工改為「處理完成」（`.dev/**` 本 loop 不可改）——接手：人工。
