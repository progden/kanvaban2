# T-09-be-canvas-layout state

> 2026-09-22 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

2026-09-22 Dev 第 2 輪收尾，status＝`doing`（等待 Review）。

處理了 Review 第 1 輪退回的 D-01～D-03：
- D-01：`CanvasSteps.thenRejected` 補上資料不變快照比對（全庫 canvas_items／viewports
  逐欄比對），12 個 `@fail-pN` Scenario 現在真的會驗到「且資料不變」，已改測試並全部通過。
- D-02／D-03：依規則補開 `OQ-T-09-be-canvas-layout-02`（item.z 唯一鍵範圍）、
  `OQ-T-09-be-canvas-layout-03`（Background 繞過 uc-init-canvas），實作維持現況未改。

`./gradlew clean build --no-daemon` BUILD SUCCESSFUL，43 個 canvas Scenario 與其餘既有測試全綠。

Review 這輪要先看：`CanvasSteps.java` 的快照比對邏輯是否真的會抓到「資料被寫入才拒絕」的迴歸；
兩則新 OQ 的引文與選項是否合理。
