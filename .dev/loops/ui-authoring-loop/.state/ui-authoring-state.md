# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17
- 目前階段：階段 1（F01 Swimlane／Stage／Card）— 進行中
- 上一輪任務：T1.03（定案 `s-swimlane-delete-dialog`：確認刪除／取消，含卡片一併刪除的提示）
- 下一個任務：T1.04（[F01] 定案 `s-stage-list`）
- 進行中任務的剩餘工作：無
- 已完成的模組：無；F01 骨架已建立，`s-swimlane-list`、`s-swimlane-delete-dialog` 已定案／填妥八段
- 已定義的共用 ID：`s-swimlane-list`（討論中，八段已填）、`s-swimlane-delete-dialog`（已定案）、`s-stage-list`、`s-stage-delete-dialog`、`s-board`、`s-card-add-dialog`、`s-card-detail`、`s-card-delete-dialog`（皆骨架，狀態：未討論）
- 最近 3 條 OQ：OQ-01（`s-swimlane-list` 進入路徑未定，暫定模組入口，待 `s-board` 定案時確認）；本輪未新增 OQ
- 待注意：`uc-delete-swimlane` 的 post 是「Swimlane 內的卡片一併被刪除」而非轉移到其他泳道，故 `s-swimlane-delete-dialog` 沒有「目的泳道選擇」欄位（任務描述提到的「目的泳道選擇」與本模組實際 spec 不符，依 spec 為準，未改任務描述）；`ui-check` 全檔 error 93→80
