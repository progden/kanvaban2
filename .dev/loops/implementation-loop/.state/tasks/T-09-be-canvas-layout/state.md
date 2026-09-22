# T-09-be-canvas-layout state

> 2026-09-22 Dev 第 4 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

第 4 輪已修好 D-05：`moveItems`／`removeItems` 在 `itemIds` 為空陣列或缺欄位（null）時
改為 no-op（先做權限檢查，再直接回傳空結果），不再丟 IndexOutOfBoundsException／
NullPointerException／HTTP 500。決策見 decision-log：選 no-op 而非拒絕，因空集合下
`uc-move-items`／`uc-remove-items` 的 `pre` 皆真空成立。

新增 `CanvasApplicationServiceTest`（kanban-spring 整合測試，4 案例：move/remove ×
空陣列/null），未動 canvas-*.feature。

Check：`./gradlew clean build --no-daemon` BUILD SUCCESSFUL，43 份測試結果檔全數
`failures=0 errors=0`（含新增 4 個測試），canvas 五個 feature 檔仍為 43 個 Scenario。
`git status --porcelain` 於 commit 後為空。

Review 這輪請先看：`CanvasApplicationService.java` 的 moveItems／removeItems diff，
與新增的 `CanvasApplicationServiceTest.java`。其餘 D-01～D-04 與四則 OQ 上一輪已核准，
本輪未變動。
