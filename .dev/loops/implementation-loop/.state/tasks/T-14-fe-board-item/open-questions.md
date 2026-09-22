# T-14-fe-board-item open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-14-fe-board-item-01

[Level: kanban-basic/s-board]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：upstream-missing
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【引用原文】

引用 `.dev/F01-basic-kanban/ui-kanban-basic.md` s-board「待確認事項」：
『⚠️ 成員頭像清單已改為 F07 `s-canvas` 上另一個獨立 `item`，跨 `item` 拖曳機制待 F07 補充，見 OQ-19』

引用 `.dev/F01-basic-kanban/ui-kanban-basic.md` s-board 操作表：
『| 拖曳看板成員頭像到卡片追加負責人 | `uc-assign-card-owner-by-drag`（F02） | 卡片縮圖的負責人顯示更新 | 不適用（`uc-assign-card-owner-by-drag` 無 fail 定義） | 否 |』

引用 `.dev/loops/ui-authoring-loop/.state/ui-authoring-open-questions.md` OQ-19：
『「看板成員頭像清單」改為 F07 `s-canvas` 上另一個獨立 `item`⋯跨 `item` 拖曳的機制 `spec-canvas-layout.md` 目前未定義，須留待 F07 補充或視為前端層級的技術實作細節』

推論：任務清單 `.state/tasks.md` 第 42 行 T-14-fe-board-item 的產出範圍只列 `s-board`／`s-swimlane-list`／`s-swimlane-delete-dialog`／`s-stage-list`／`s-stage-delete-dialog`／`s-card-add-dialog`／`s-card-detail`／`s-card-delete-dialog`／`s-card-assignee-picker` 九個 Screen ID，並未列出「看板成員」這個獨立 item 的畫面；OQ-19 也載明跨 item 拖曳機制待 F07（canvas-layout）補充、目前尚無此 item 存在。本任務因此無法實作 s-board 操作表「拖曳看板成員頭像到卡片追加負責人」這一列（沒有拖曳來源可用），已實作 s-board 其餘全部操作（跨 Swimlane／跨 Stage 拖曳卡片、新增／開啟詳情／刪除卡片）與 F02 `uc-set-card-assignees`（透過 `s-card-detail` → `s-card-assignee-picker` 的整批設定負責人達成同等效果，只是入口不是拖曳頭像）。

問題：「看板成員」item（拖曳負責人頭像的來源）由哪個任務負責建立、`uc-assign-card-owner-by-drag` 的跨 item 拖曳機制何時補上？

選項：A. 待 F07 canvas-layout 補上「看板成員」item 定義與跨 item 拖曳機制後，由後續任務（例如 T-14 的修訂實例，或新排的 F07 相關任務）實作；B. 維持現狀，`uc-assign-card-owner-by-drag` 長期只能透過 `s-card-assignee-picker` 手動勾選達成，不再要求拖曳頭像這個互動方式（需走 CR 修改 ui-kanban-basic.md 操作表）。
