# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17
- 目前階段：階段 1（F01 Swimlane／Stage／Card）— 進行中
- 上一輪任務：T1.07（定案 `s-card-add-dialog`，類型改為表單，對應 `uc-add-card`）
- 下一個任務：T1.08（[F01] 定案 `s-card-detail`，對應 `uc-edit-card`／`uc-add-comment`；負責人欄位先標 `⚠️` 待確認並記 OQ）
- 進行中任務的剩餘工作：無
- 已完成的模組：無；F01 骨架已建立，`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list`、`s-card-add-dialog` 已定案；`s-stage-delete-dialog`、`s-board` 討論中（各自有未解 OQ）
- 已定義的共用 ID：`s-swimlane-list`（已定案）、`s-swimlane-delete-dialog`（已定案）、`s-stage-list`（已定案）、`s-card-add-dialog`（已定案）、`s-stage-delete-dialog`（討論中，OQ-03）、`s-board`（討論中，OQ-04／05／06）、`s-card-detail`、`s-card-delete-dialog`（骨架，狀態：未討論）
- 最近 3 條 OQ：OQ-05（看板成員清單於 `s-board` 的呈現位置未定）；OQ-06（【矛盾】F02 `uc-assign-card-owner-by-drag` 等 usecase 的 `roles: [r-user]` 與 F02 角色定義表不一致）；OQ-07（【推論】推翻 OQ-01／OQ-02：`s-swimlane-list`／`s-stage-list` 從 `s-board`「管理 Swimlane／Stage」操作進入）
- 待注意：`ui-check`（不帶 `--spec`）38 error（開工前 51，下降因 `s-card-add-dialog` 補齊八段消除原本的引用／格式缺口）；（含 `--spec` F02）26 error；皆未較開工前增加；`verify-quotes.py` 通過。
