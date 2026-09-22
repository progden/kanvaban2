# T-15-fe-member-management open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-15-fe-member-management-01

[Level: canvas-layout/uc-place-item]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-undefined
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】
`ui-canvas-layout.md` 待確認事項寫『⚠️ 依 OQ-49，F03 四個圖表畫面、F04 時鐘控制畫面、F05 工作量儀表板、F06 追蹤表畫面，人工已確認也是 `s-canvas` 上可放置的獨立 `item`（跟看板本體一樣，不透過其他畫面的操作列進入）；但這些各自的 `item.component` 對應值、如何從既有畫布新增這類元件（使用者操作流程）spec「待釐清」尚未定義，待整合 CR 定案』；同檔「看板成員」item 的敘述（操作表「於『看板成員』item 選擇加入成員」一列）也只描述行為，不含 `item.component` 識別碼的值。`spec-canvas-layout.md` `uc-place-item` 的 `pre`／`post` 只要求『`item` 存在，`item.component` 為指定的元件識別碼』，未限制可能值的集合。
推論：因為 `item.component` 在 spec 中是自由字串（`PlaceItemDialog` 也是讓使用者自行輸入，沒有白名單），要讓「看板成員」item 能掛上內容，必須先選定一個具體字串。本任務選定 `"board-members"`，在 `registerItemComponent('board-members', BoardMembersItem)` 註冊；這是純技術實作細節（不影響任何 Scenario 的業務行為），不是規格斷言。
問題：整合 CR 定案「看板成員」item 的 `item.component` 值時，是否採用本任務選定的 `"board-members"`？
選項：A. 採用 `"board-members"`，維持本任務程式碼不變；B. 定案為其他字串，回頭改 `kanban-frontend/src/canvas/members/BoardMembersItem.tsx` 的 `registerItemComponent` 呼叫（以及使用者實際透過「加入元件」輸入的識別碼）
