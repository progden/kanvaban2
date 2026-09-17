# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17
- 目前階段：階段 1（F01 Swimlane／Stage／Card）— 進行中
- 上一輪任務：T1.05（定案 `s-stage-delete-dialog`，對應 `uc-delete-stage`，狀態：討論中）
- 下一個任務：T1.06（[F01] 定案 `s-board`，對應 `uc-move-card-swimlane`／`uc-move-card-stage`；操作表需含跨模組引用的 `uc-assign-card-owner-by-drag`）
- 進行中任務的剩餘工作：無
- 已完成的模組：無；F01 骨架已建立，`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list`、`s-stage-delete-dialog` 已填妥八段
- 已定義的共用 ID：`s-swimlane-list`（討論中）、`s-swimlane-delete-dialog`（已定案）、`s-stage-list`（討論中）、`s-stage-delete-dialog`（討論中）、`s-board`、`s-card-add-dialog`、`s-card-detail`、`s-card-delete-dialog`（皆骨架，狀態：未討論）
- 最近 3 條 OQ：OQ-01（`s-swimlane-list` 進入路徑未定，待 `s-board` 定案時確認）；OQ-02（`s-stage-list` 進入路徑未定，同類問題）；OQ-03（`s-stage-delete-dialog` 目的 Stage 是否排除欲刪除的 Stage 本身，spec `uc-delete-stage` post 未寫明）
- 待注意：T1.06 的 `s-board` 是跨模組畫面（同時引用 F02 的 `uc-assign-card-owner-by-drag`），且是 OQ-01／OQ-02 提到「待 `s-board` 定案時確認」的對象，本輪需回頭檢視能否解掉那兩條 OQ；`ui-check` 全檔 error 65→52（新增 `s-stage-delete-dialog` 內容，其餘未寫畫面仍缺段落，屬預期）
