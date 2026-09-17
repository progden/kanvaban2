# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）已收尾完成，即將進入階段 3（F03 kanban-widgets）
- 上一輪任務：D-10／T2.11——D-10（`s-login`/`s-signup` 的 OQ-11／12／13 移除）內容已在前幾輪完成，本輪銜接完成 T2.11 收尾：`s-card-detail` 補上負責人欄位（來源 `card.assignees`，F02 跨模組）、新增角色列 `r-board-member`（F02，跨模組）、新增「開啟負責人選取」操作導向 `s-card-assignee-picker`，狀態轉「已定案」，OQ-08 解除；同時修正 `s-card-assignee-picker`「狀態」段 `roles` 反引號誤用。D-10、T2.11 皆已標 `done`
- 下一個任務：T3.01（[F03] 建立 `ui-kanban-widgets.md` 檔頭＋4 個畫面標題骨架：`s-cycle-lead-time-dashboard`／`s-wip-dashboard`／`s-throughput-cfd-dashboard`／`s-duedate-reminder`，模式同 T1.01）
- 已完成的模組：F01 全部 8 個畫面已定案；F02 全部 9 個畫面已收尾（`s-signup`／`s-login`／`s-board-list`／`s-board-create-dialog`／`s-board-delete-dialog`／`s-card-assignee-picker`／`s-cards-by-assignee`／`s-member-management`／`s-activity-log`）；`ui-check`（F01＋F02 合併，帶 `--spec` 全模組）0 error。`s-member-management`（OQ-22）、`s-board-delete-dialog`（OQ-26）、`s-cards-by-assignee`（OQ-28）入口未定維持「討論中」；`s-activity-log`（OQ-29）因後端尚未實作維持「未討論」
- 已定義的共用 ID：無新增
- 最近 OQ：無新增（本輪未新增 OQ，OQ-08／OQ-11／OQ-12／OQ-13 已解除）
- 待注意：F02 剩餘 DS-06／DS-07 warning 皆為既有項目（`s-member-management`／`s-board-delete-dialog`／`s-cards-by-assignee`／`s-activity-log` 入口未定、`s-signup` 靠操作表而非「進入與離開」nav 連結、F02 一個 dangling uc），非本輪新增，留給對應 OQ 後續處理；`parser_design.py` 的 `BULLET_RE` 要求「從哪裡進來」「完成後去哪裡」等 key 與內容同一行含冒號才會登記 nav，多重去向請用單行「；」分隔（勿用巢狀項目符號，否則 DS-07 不會消除，見 PDCA Iteration 38）；PDCA 每則 Check 段落務必貼 `./scripts/ui-check <本輪檔案> --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 最後一行；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用，`roles`／`pre`／`post` 等 usecase 欄位名不可套反引號。
