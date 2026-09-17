# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17
- 目前階段：階段 1（F01 Swimlane／Stage／Card）— 進行中
- 上一輪任務：T1.04（定案 `s-stage-list`，對應 `uc-add-stage`／`uc-rename-stage`／`uc-reorder-stage`／`uc-set-stage-role`，狀態：討論中）
- 下一個任務：T1.05（[F01] 定案 `s-stage-delete-dialog`，對應 `uc-delete-stage`，含卡片轉移情境）
- 進行中任務的剩餘工作：無
- 已完成的模組：無；F01 骨架已建立，`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list` 已填妥八段
- 已定義的共用 ID：`s-swimlane-list`（討論中）、`s-swimlane-delete-dialog`（已定案）、`s-stage-list`（討論中）、`s-stage-delete-dialog`、`s-board`、`s-card-add-dialog`、`s-card-detail`、`s-card-delete-dialog`（皆骨架，狀態：未討論）
- 最近 3 條 OQ：OQ-01（`s-swimlane-list` 進入路徑未定，待 `s-board` 定案時確認）；OQ-02（`s-stage-list` 進入路徑未定，同類問題，待 `s-board` 定案時確認）；本輪無其他新增
- 待注意：`uc-delete-stage` 的 post 是「若該 stage 內有 card，card.stage 更新為使用者選擇的目的 stage」（與 swimlane 刪除的「一併刪除」不同），故 T1.05 的 `s-stage-delete-dialog` 需有「目的 Stage 選擇」欄位；`ui-check` 全檔 error 78→65（新增 `s-stage-list` 內容，其餘未寫畫面仍缺段落，屬預期）
