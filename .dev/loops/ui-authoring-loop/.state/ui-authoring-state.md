# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17
- 目前階段：階段 1（F01 Swimlane／Stage／Card）— 進行中
- 上一輪任務：T1.06（定案 `s-board`，對應 `uc-move-card-swimlane`／`uc-move-card-stage`，操作表列出跨模組 `uc-assign-card-owner-by-drag`；狀態：討論中，因 OQ-04／OQ-05／OQ-06）
- 下一個任務：T1.07（[F01] 定案 `s-card-add-dialog`，對應 `uc-add-card`）
- 進行中任務的剩餘工作：無
- 已完成的模組：無；F01 骨架已建立，`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list` 已定案；`s-stage-delete-dialog`、`s-board` 討論中（各自有未解 OQ）
- 已定義的共用 ID：`s-swimlane-list`（已定案）、`s-swimlane-delete-dialog`（已定案）、`s-stage-list`（已定案）、`s-stage-delete-dialog`（討論中，OQ-03）、`s-board`（討論中，OQ-04／05／06）、`s-card-add-dialog`、`s-card-detail`、`s-card-delete-dialog`（皆骨架，狀態：未討論）
- 最近 3 條 OQ：OQ-04（`s-board` 進入路徑跨模組依賴 F02 `s-board-list`，尚未定案）；OQ-05（看板成員清單於 `s-board` 的呈現位置未定）；OQ-06（【矛盾】F02 `uc-assign-card-owner-by-drag` 等 usecase 的 `roles: [r-user]` 與 F02 自身角色定義表不一致，需回饋後端 Spec）
- 待注意：`ui-check`（不帶 `--spec`）對本檔會多出 11 筆 REF-07 error，全部來自 `s-board` 引用 F02 尚未載入的 ID（因 `ui-user-membership.md` 尚未建立，`ui-check` 不會自動帶入 `spec-user-membership.md`）；用 `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec .dev/F02-user-membership/spec-user-membership.md` 驗證，`s-board` 本身已 0 error，這 11 筆會在 T2.01 建立 F02 ui 檔後於全檔掃描時自動消失；`ui-check` 全檔 error 52→51（不含 `--spec`，s-board 骨架填內容抵銷了跨模組 REF-07 增量）
