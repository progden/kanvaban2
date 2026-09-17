# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17
- 目前階段：階段 1（F01 Swimlane／Stage／Card）— 進行中
- 上一輪任務：D-05（修正 `s-card-add-dialog` 3 處寫到領域狀態，已 done）
- 下一個任務：T1.09（[F01] 定案 `s-card-delete-dialog`，對應 `uc-delete-card`）
- 進行中任務的剩餘工作：無
- 已完成的模組：無；F01 骨架已建立，`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list`、`s-card-add-dialog`、`s-card-detail` 已定案（`s-card-detail` 仍有 2 條 ⚠️ 待確認）；`s-stage-delete-dialog`、`s-board` 討論中（各自有未解 OQ）
- 已定義的共用 ID：`s-swimlane-list`（已定案）、`s-swimlane-delete-dialog`（已定案）、`s-stage-list`（已定案）、`s-card-add-dialog`（已定案）、`s-card-detail`（討論中，OQ-08／09）、`s-stage-delete-dialog`（討論中，OQ-03）、`s-board`（討論中，OQ-04／05／06）、`s-card-delete-dialog`（骨架，狀態：未討論）
- 最近 3 條 OQ：OQ-07（【推論】推翻 OQ-01／OQ-02：`s-swimlane-list`／`s-stage-list` 從 `s-board`「管理 Swimlane／Stage」操作進入）；OQ-08（負責人欄位與指派入口待 F02 `s-card-assignee-picker` 定案後回填，依 CR-002）；OQ-09（留言相關欄位缺乏可引用 Attribute ID，spec 未定義「留言」實體或欄位）
- 待注意：`ui-check`（不帶 `--spec`）25 error（開工前 38，未較開工前增加）；本輪未新增待驗證引用，未跑 `verify-quotes.py`；下一輪 T1.09 需完整撰寫 `s-card-delete-dialog` 八段。
