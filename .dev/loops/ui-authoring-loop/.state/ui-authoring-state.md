# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中
- 上一輪任務：T2.09——定案 `s-cards-by-assignee`（列表），對應 `uc-list-cards-by-assignee`；入口未定（OQ-28），畫面標「討論中」；`ui-check` 本檔（含 F01 跨模組檔）error 數 15（原 s-card-assignee-picker 既有 1 個 `roles` 反引號誤用未動）→ 14（本畫面本身 0 error，僅遺留 s-activity-log 骨架與既有 1 筆 roles 誤用）
- 下一個任務：T2.10（[F02] 定案 `s-activity-log`：對應 `uc-view-board-activity-log`，先讀 spec 該 uc 現況決定走「已定案」或「尚未實作」分支）
- D-10 仍列 `doing`：其整檔 `ui-check=0` 驗收條件依賴 T2.05～T2.10 全部完成，內容本身已完成，待 T2.11 收尾任務整份歸零後可直接改 `done`，不需再動內容
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`／`s-board-list`／`s-board-create-dialog`／`s-board-delete-dialog`／`s-card-assignee-picker`／`s-cards-by-assignee`；`s-member-management` 因入口未定（OQ-22）、`s-board-delete-dialog` 因入口未定（OQ-26）、`s-cards-by-assignee` 因入口未定（OQ-28）皆維持「討論中」；`s-activity-log` 骨架未討論
- 已定義的共用 ID：無新增
- 最近 4 條 OQ：OQ-25（`uc-reject-structure-change-by-member` 不屬 `s-member-management`，待確認是否該回填 F01 兩個列表畫面）／OQ-26（`s-board-delete-dialog` 進入路徑推論待 F01 `s-board` 補操作）／OQ-27（`uc-invite-member` pre 未定義邀請對象帳號是否須已存在）／OQ-28（`s-cards-by-assignee` 進入路徑不明，待 `s-member-management` 或 F05 `ui-workload.md` 回頭確認）
- 待注意：`s-card-assignee-picker`「狀態」段有一處 `` `roles` `` 反引號誤用（REF-07 error，T2.08 遺留，本輪未動，留給收尾任務處理）；PDCA 每則 Check 段落務必貼 `./scripts/ui-check <本輪檔案> --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 的最後一行；跑該指令務必加 `--spec`；單檔測試若跨模組引用（例如 `s-card-detail`）務必連同其他已存在的 ui 檔一起餵給 `ui-check`，否則會誤判 REF-07（`tools error-count` 已內建）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用，「roles」「pre」「post」等 usecase 欄位名不可套反引號。
