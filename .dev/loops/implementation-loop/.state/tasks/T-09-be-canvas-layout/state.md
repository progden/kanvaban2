# T-09-be-canvas-layout state

> 2026-09-22 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

2026-09-22 Review 第 1 輪：退回，status＝`doing`。

`./gradlew clean build --no-daemon` BUILD SUCCESSFUL（14 tasks executed，全部測試綠燈）；
43 個 canvas Scenario 與 spec 的 43 個一一對上，feature 檔與 spec gherkin 區塊逐字相符；
`kanban-core` 無 Spring／JPA 依賴；改動範圍乾淨（只有 `kanban-core`／`kanban-spring` 與本任務
`.state` 目錄，共用檔僅 `ErrorCode` 新增列舉值）。

退回原因（三條，見 fixes.md）：
- D-01：`拒絕，訊息為 "..."，且資料不變` 的 step 只驗 status 與訊息，沒驗資料不變，12 個
  `@fail-pN` Scenario 的後半段斷言等於沒測。要改測試（前後快照比對）。
- D-02：`item.z` 唯一鍵用 `(canvas_id, anchor, z)`，與欄位表『同一 canvas 內唯一』原文不同，
  只寫在 Java 註解，沒開 OQ。補開 OQ 即可，實作不必改。
- D-03：Background「畫布已由系統建立」用直接寫 DB 的空白 canvas，繞過 `uc-init-canvas`
  （spec post 要求一定帶 z=1 的看板本體 item），無任何紀錄。補開 OQ 即可。

`OQ-T-09-be-canvas-layout-01`（HTTP 狀態碼）核對通過：引文逐字相符、等級「高」不阻塞正確、
接手「無」可接受，不影響後續核准。

下一輪 Dev：處理 D-01～D-03 後重跑 `./gradlew clean build --no-daemon`。
