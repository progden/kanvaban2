# T-09-be-canvas-layout state

> 2026-09-22 Review 第 4 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

2026-09-22 Review 第 4 輪：附保留核准，status＝`done`。

自行執行 `./gradlew clean build --no-daemon` → BUILD SUCCESSFUL；43 份 TEST-*.xml 彙總
tests=246 skipped=0 failures=0 errors=0；工作區乾淨。

spec 對應：11 個 `uc-` 全數有 `@uc-` tag，spec 43 個 Scenario 標題與 feature 檔逐字一致
（缺 0），10 句 fail 訊息與實作字面相同；抽查「且資料不變」步驟（D-01）確認 12 個 When
全部先拍快照、Then 逐欄比對；D-05 的 no-op 修正已確認消除 HTTP 500 路徑且權限檢查未跳過。

`kanban-core` 純度：core/domain 無 Spring／JPA import；未新增 query 投影。

邊界：48 檔改動皆屬 canvas 範圍，`.state/` 只動本任務目錄。

保留事項（不阻塞，詳見 review.md）：OQ-01 接手人工（狀態碼，T-13 動工前確認）、OQ-02／OQ-04
接手人工（spec 草稿文字要定案，若選 B 需追加 -r2 修訂實例）、OQ-03 已由 OQ-04 取代。
