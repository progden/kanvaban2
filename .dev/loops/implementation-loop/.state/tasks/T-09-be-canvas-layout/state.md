# T-09-be-canvas-layout state

> 2026-09-22 Review 第 3 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

Review 第 3 輪判定退回（`doing`），等下一輪 Dev 處理 D-05。

已自行驗證通過的部分（不需重做）：

- `./gradlew clean build --no-daemon` BUILD SUCCESSFUL，42 份測試結果檔 failures=0 errors=0，canvas 五個 feature 合計 43 scenario 全綠。
- 五個 `canvas-*.feature` 與 spec gherkin 區塊逐行比對，只多出中文註解，無遺漏；11 個 uc 的 tag 齊備。
- 突變測試（暫移除 `Item.move` 的 `ensureMovable()`）確認 `@fail-pN` 的「且資料不變」斷言會真的轉紅；驗證後已還原。
- `kanban-core` 無 Spring／JPA import；任務邊界乾淨（只動 canvas 相關新檔＋`ErrorCode` 追加項＋自己的 `.state` 目錄）。
- D-04 已達成：OQ-04 三段引文逐字核對與 spec 相符；OQ-01／02／04 的等級「高、不阻塞」標記正確（spec 為草稿）。

待處理：

- **D-05**：批次端點 `itemIds` 為空或缺欄位時丟 `IndexOutOfBoundsException`／`NullPointerException`，穿透成 HTTP 500。需改成明確定義的回應＋記決策＋補測試＋重跑 build。

不阻塞的保留事項（接手見 `review.md` 第 3 輪末段）：OQ-01 接手實質為 T-13-fe-canvas-shell；OQ-02、OQ-04 接手人工；OQ-03 已由 OQ-04 取代。
