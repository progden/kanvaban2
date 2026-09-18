# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-02-be-board：Review 第 1 輪**退回**，狀態 `doing`，等 Dev 處理 D-05～D-08（見 `tasks.md` 底部、`review.md` 同日條目）。
- 建置：Review 自己重跑 `./gradlew clean build --no-daemon`，exit 0，54 個測試全部通過；core 是乾淨的，改動範圍沒有越界。
- D-05：刪除 Swimlane／Stage 時要連帶刪除卡片、或把卡片轉移到目的 Stage（spec 第 141、292 行的 post），正式程式碼沒有實作，API 也沒有目的 Stage 的輸入。要新開 OQ（逐字引用 spec、ui、design），寫清楚由誰接手、API 契約怎麼定。
- D-06：`BoardSteps` 的 `whenConfirmDelete`／`thenSwimlaneAndCardsRemoved`／`whenChooseDestinationStage`／`thenCardsMovedTo` 是靠測試自己改 fake 來通過的，要加註解標明它們只是替身，並指向 D-05 開的 OQ。
- D-07：`thenActivityRecorded` 沒有驗證效果（Background 已經有「建立看板」那筆紀錄）。改成比對筆數加 1 和動作內容，`@fail-p1` 的 Scenario 要驗筆數沒變；`BoardTest` 補上其餘寫入方法的活動紀錄斷言。
- D-08：OQ-IMPL-13 的 design 引文改成逐字（第 115 行，並註明是 CR-003 段落），OQ-IMPL-12 的路徑改正並補上行號。
- 未決 OQ（不擋核准，再審時以附保留核准處理）：OQ-IMPL-12（預設 Swimlane／Stage）、OQ-IMPL-13（Owner 權限要等 T-04）、OQ-IMPL-14（Owner membership 要等 T-04）。
- 下一步：Dev 第 2 輪只處理 D-05～D-08，正式程式碼不要求修改；處理完改成 `review-pending`。
