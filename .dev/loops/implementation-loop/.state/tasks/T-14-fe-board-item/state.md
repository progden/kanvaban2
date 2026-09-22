# T-14-fe-board-item state

> 2026-09-22 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

D-01～D-04 全部修好：留言顯示 created-at 並排序、刪除對話框卡片數 0 也顯示、補齊 9 條 ui 驗收條件的測試（過程中發現並修好 StagePanel 重新命名按鈕缺失的死碼 bug）、OQ-T-14-fe-board-item-01 第三段引文改開逐字版 OQ-T-14-fe-board-item-02。

Check：`npx tsc -b` 0 錯誤、`npx vitest run` 7 檔 80 測試全過、`./gradlew build --no-daemon` BUILD SUCCESSFUL、工作區乾淨已 commit。

Review 這輪請先看：新增的測試是否真的覆蓋 D-03 列出的 9 條驗收條件字面意思；StagePanel 補的重新命名按鈕是否符合 ui 規格（位置沿用 SwimlanePanel 慣例）；OQ-T-14-fe-board-item-02 的逐字引文是否正確取代第一則的問題。
