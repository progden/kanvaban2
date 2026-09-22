# T-16-fe-activity-log open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-16-fe-activity-log-01

[Level: user-membership/s-activity-log]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】
`ui-user-membership.md` `s-activity-log` 段落逐字寫『依 T2.10 分支規則，本畫面暫緩定案，僅保留八段標題與簡短說明，不產出完整內容，見 OQ-29』，「目的」「進入與離開」等八段皆填『不適用，理由同上』；`ui-authoring-open-questions.md` OQ-29「採用」欄逐字寫『`s-activity-log` 狀態維持「未討論」，八段標題各填「不適用，理由同上」並於「待確認事項」標 ⚠️；待「kanban-spring」補上跨 aggregate 查詢投影、spec 更新後回頭定案』。查 T-04-be-board-membership 已合併並補上該投影（`BoardActivityLogQueryService`、`GET /api/boards/{boardId}/activity-log`、`ActivityLogEntryResponse{operatorUsername, operatorDisplayName, action, occurredAt}`），OQ-29 所設的「回頭定案」前提已成立，但 `ui-user-membership.md` 本身與 OQ-29 皆未實際回頭補上操作表／資料表／進入路徑／驗收條件等內容。
推論：既然 usecase 區塊（`uc-view-board-activity-log` 的 roles/pre/post）與 Gherkin Scenario「活動紀錄依時間新到舊排序，並顯示操作人與動作」已完整定義行為本體，且 T-13 已把「進入方式」抽象成一般化機制（`ui-canvas-layout.md`／`PlaceItemDialog`：使用者在畫布上輸入任意 `item.component` 識別碼即可掛載該元件，無需各模組另訂專屬入口），本任務可依此推論出最小可用畫面，不必等待 ui 文件回頭定案；但版面細節（欄位呈現方式、排序展示、空狀態文字、`item.component` 識別碼命名）是我方推論決定，不是 ui 檔逐字定案的內容。
問題：`s-activity-log` 目前的實作（Canvas item，`item.component` 固定命名為 "activity-log"；內容為操作人＋動作＋時間的簡單列表，依後端已排序結果原樣顯示；查無操作人時顯示「未知使用者」；載入失敗顯示後端訊息）是否符合預期，是否需要回頭把 `ui-user-membership.md` `s-activity-log` 段落與 OQ-29 補齊定案（八段標題、操作表、資料表、驗收條件），或維持現狀由後續補完？
選項：A. 維持現狀，`ui-user-membership.md` 的 `s-activity-log` 仍標記「未討論」，本次實作視為先行版本，待日後另開任務／CR 依 ui-authoring-loop 流程正式補齊 ui 文件並與本實作核對；B. 立即另開 ui-authoring 任務，依本次實作反推 `s-activity-log` 的完整內容並回填 `ui-user-membership.md`（含 OQ-29 的後續決議列），確保程式碼與 ui 文件同步。
