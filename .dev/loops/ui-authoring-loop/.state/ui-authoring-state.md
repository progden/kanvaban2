# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17
- 目前階段：階段 1（F01 Swimlane／Stage／Card）— 進行中
- 上一輪任務：D-02（修正 `ui-kanban-basic.md` 3 處把業務結果／領域狀態寫進 UI 段落；已修正，狀態不變仍為原「討論中」／「已定案」）
- 下一個任務：D-03（[F01] 補上 T1.06 推翻 OQ-01／OQ-02 的紀錄，需在 OQ 表新增一列並修正 `s-swimlane-list`／`s-stage-list` 的「從哪裡進來」文字）
- 進行中任務的剩餘工作：無
- 已完成的模組：無；F01 骨架已建立，`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list` 已定案；`s-stage-delete-dialog`、`s-board` 討論中（各自有未解 OQ）
- 已定義的共用 ID：`s-swimlane-list`（已定案）、`s-swimlane-delete-dialog`（已定案）、`s-stage-list`（已定案）、`s-stage-delete-dialog`（討論中，OQ-03）、`s-board`（討論中，OQ-04／05／06）、`s-card-add-dialog`、`s-card-detail`、`s-card-delete-dialog`（皆骨架，狀態：未討論）
- 最近 3 條 OQ：OQ-04（`s-board` 進入路徑跨模組依賴 F02 `s-board-list`，尚未定案）；OQ-05（看板成員清單於 `s-board` 的呈現位置未定）；OQ-06（【矛盾】F02 `uc-assign-card-owner-by-drag` 等 usecase 的 `roles: [r-user]` 與 F02 自身角色定義表不一致，需回饋後端 Spec）
- 待注意：D-03 要處理 T1.06 遺留的問題——OQ-01／OQ-02「採用」欄原寫「暫定為模組入口」，但 T1.06 已把 `s-swimlane-list`／`s-stage-list` 的進入路徑改成經 `s-board`，且未在 OQ 表追加「推翻 OQ-01／OQ-02」的紀錄，兩畫面文字也誤稱「已依 `s-board` 定案結果更新」（`s-board` 實為討論中）；`ui-check`（不帶 `--spec`）51 error、（含 `--spec` F02）39 error，皆與本輪開工前持平未增加。
