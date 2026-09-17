# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中
- 上一輪任務：T2.10——`s-activity-log` 因 spec 明文標注跨 aggregate 查詢投影「本情境目前尚未實作」，走「尚未實作」分支：狀態維持「未討論」，八段標題各填簡短說明理由，記入 OQ-29；`s-activity-log` 本身 0 error，單檔 `ui-check`（不含其他 ui 檔）仍有 20 個既有跨模組引用／`roles` 誤用 error（皆非本輪新增，留給 T2.11 收尾）
- 下一個任務：T2.11（[F02] 跨模組收尾：`ui-user-membership.md` 與 `ui-kanban-basic.md` 一起跑 `ui-check` 都需 0 error；回頭補 F01 `s-card-detail` 的負責人欄位來源、指派入口，解掉 T1.08 留的 ⚠️；順便清掉 `s-card-assignee-picker` 既有的 `roles` 反引號誤用）
- D-10 已完成內容不需再動，狀態仍列 `doing`，待 T2.11 整份歸零後改 `done`
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`／`s-board-list`／`s-board-create-dialog`／`s-board-delete-dialog`／`s-card-assignee-picker`／`s-cards-by-assignee`；`s-member-management`（OQ-22）、`s-board-delete-dialog`（OQ-26）、`s-cards-by-assignee`（OQ-28）入口未定維持「討論中」；`s-activity-log`（OQ-29）因後端尚未實作維持「未討論」
- 已定義的共用 ID：無新增
- 最近 4 條 OQ：OQ-26／OQ-27／OQ-28（見上）／OQ-29（`s-activity-log` 對應 `uc-view-board-activity-log` spec 明文尚未實作，暫緩定案，待「kanban-spring」補跨 aggregate 查詢投影後回頭處理）
- 待注意：`s-card-assignee-picker`「狀態」段有一處 `` `roles` `` 反引號誤用（REF-07 error，T2.08 遺留，留給 T2.11 收尾）；PDCA 每則 Check 段落務必貼 `./scripts/ui-check <本輪檔案> --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 的最後一行；跑該指令務必加 `--spec`；單檔測試若跨模組引用（例如 `s-card-detail`／`s-board`）務必連同其他已存在的 ui 檔一起餵給 `ui-check`，否則會誤判 REF-07（`tools error-count` 已內建）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用，「roles」「pre」「post」等 usecase 欄位名不可套反引號。
