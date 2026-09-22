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

## OQ-T-16-fe-activity-log-02

[Level: user-membership/uc-view-board-activity-log]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-outdated
- 開立：Review 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】
本則補正 OQ-T-16-fe-activity-log-01「情況」欄的一句推論措辭，該則的等級（高）／阻塞（否）／接手（無）經核對無誤，維持有效，本則不取代它，只修正其中一項事實敘述。
OQ-T-16-fe-activity-log-01 逐字寫『查 T-04-be-board-membership 已合併並補上該投影（`BoardActivityLogQueryService`、`GET /api/boards/{boardId}/activity-log`、`ActivityLogEntryResponse{operatorUsername, operatorDisplayName, action, occurredAt}`），OQ-29 所設的「回頭定案」前提已成立』。
但 `ui-authoring-open-questions.md` OQ-29「採用」欄的原文是『`s-activity-log` 狀態維持「未討論」，八段標題各填「不適用，理由同上」並於「待確認事項」標 ⚠️；待「kanban-spring」補上跨 aggregate 查詢投影、spec 更新後回頭定案』，設的是「補上投影」與「spec 更新」兩個前提。
`spec-user-membership.md`「Feature: 檢視看板活動紀錄」的說明段落至今仍逐字寫『這件事本質上是跨 aggregate 的查詢投影，留給「kanban-spring」有 application／persistence 層時再做（見 `design-user-membership.md` 第 8 點）。本情境目前尚未實作。』
推論：OQ-29 的兩個前提目前只滿足第一個（投影已由 T-04 補上，本輪已驗證 `BoardActivityLogQueryService` 與 `BoardController` 的 `GET /{boardId}/activity-log` 存在且依 `occurredAt` 反序排列），第二個「spec 更新」尚未發生——spec 原文仍宣稱本情境尚未實作，與 T-04、T-16 的實際產出不一致。這句原文是實作現況的陳述、不是禁止實作的規定，且 `.state/tasks.md` 第 44 列已把 `s-activity-log` 排為 T-16 的產出範圍，所以不構成「必須違反定稿原文」，本任務不因此阻塞；但這段 spec 文字與 `ui-user-membership.md` 的「未討論」狀態會在程式碼合併後同時落後於實作，且本 loop 禁止修改 spec／ui 文件本體，沒有任何既有任務會處理它。
問題：`spec-user-membership.md`「檢視看板活動紀錄」Feature 結尾『本情境目前尚未實作』這句（以及連帶的 `ui-user-membership.md` `s-activity-log`「未討論」狀態、OQ-29 的待辦）要由誰、在什麼時機更新為已實作？是否需要開 CR 處理（`cr-convention.md` 規定定稿後改 Scenario／名詞表／ui 操作表要先開 CR，但本處是 Feature 說明段的現況敘述，是否屬於需 CR 的結構性改動亦待確認）？
選項：A. 由人工在 T-16 合併後直接修訂該段落的現況敘述（視為非結構性的事實更新，不開 CR），ui 文件另循 ui-authoring-loop 補齊（即 OQ-T-16-fe-activity-log-01 的選項 B）；B. 開一則新 CR 一併處理 spec 說明段落更新與 `ui-user-membership.md` `s-activity-log` 八段定案，由該 CR 對應的修訂任務實例（`T-16-fe-activity-log-r2` 之類）回頭與本次實作核對。
