# T-14-fe-board-item state

> 2026-09-22 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪判定：退回（doing）。

- 自己跑過：`./gradlew build --no-daemon --rerun-tasks` BUILD SUCCESSFUL（12 task 全部實跑）；`npx tsc -b` 0 錯誤；`npx vitest run` 7 檔 67 測試全過。工作區乾淨。
- 邊界乾淨：只動 `kanban-frontend/src/board/**`、兩支 api、`BoardCanvasPage.tsx` 的 Provider 包裝、`kanban-spring` 的一個讀取端點（比照已合併的 T-12 先例，含成員資格檢查與測試），`.state/` 只動本任務目錄。`kanban-core` 未被動到。
- 退回原因（四條 D-xx 待下一輪 Dev 處理）：
  - D-01：s-card-detail 留言未顯示 `comment.created-at`（ui 資料表與 CardDetail.dc.html 都要求顯示）。
  - D-02：刪除 Swimlane／Stage 對話框在卡片數為 0 時完全不顯示卡片數（ui：無卡片時卡片數顯示為 0）。
  - D-03：9 條 ui 驗收條件無任何測試（uc-move-card-stage、兩個移動的 p2 失敗、rename／reorder swimlane 與 stage、空名稱新增泳道、START/DONE 互斥顯示、uc-add-comment p2）。
  - D-04：`OQ-T-14-fe-board-item-01` 第三段 OQ-19 引文非逐字（「看板成員清單」被寫成「看板成員頭像清單」，且省略關鍵句），要另開逐字版 OQ 取代。
- 未決 OQ：`OQ-T-14-fe-board-item-01`（高／不阻塞／接手＝人工），`uc-assign-card-owner-by-drag` 本輪未實作，等 F07 補「看板成員」item 與跨 item 拖曳機制。
