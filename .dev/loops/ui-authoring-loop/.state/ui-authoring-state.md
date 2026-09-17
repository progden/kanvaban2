# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）已收尾完成，審查輪新增 D-14～D-16 待處理，處理完才進入階段 3（F03）
- 上一輪任務：D-14——F01 `ui-kanban-basic.md` `s-card-detail` 補上 F02 `s-cards-by-assignee`（跨模組）作為進入來源；「完成後去哪裡」、操作表「關閉」、驗收條件關閉條目改成「回到進入前的畫面（`s-board` 或 `s-cards-by-assignee`）」，回到 `s-board` 時仍保留看板交會格內容更新的敘述。只改導覽用語，未新增業務結果。D-14 已標 `done`
- 下一個任務：D-15（[F02] 處理 `uc-member-add-card` 沒有畫面觸發的 DS-06 warning：在 F01 `s-card-add-dialog` 標出 `r-board-member` 跨模組觸發，或若判斷不出關係則追加 OQ 並把該畫面狀態改回「討論中」）
- 已完成的模組：F01 全部 8 個畫面已定案（`s-card-detail` 本輪微調導覽敘述，狀態仍為「已定案」）；F02 全部 9 個畫面已收尾；D-15、D-16 仍待處理，處理完才算 F02 真正收尾
- 已定義的共用 ID：無新增
- 最近 OQ：無新增（本輪未新增 OQ）
- 待注意：F02 剩餘 DS-06／DS-07 warning 多為既有項目（`s-member-management`／`s-board-delete-dialog`／`s-cards-by-assignee`／`s-activity-log` 入口未定、`s-signup` 靠操作表而非「進入與離開」nav 連結、`uc-member-add-card` DS-06 待 D-15 處理）；PDCA 每則 Check 段落務必貼 `./scripts/ui-check <本輪檔案> --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 最後一行；跨模組驗證（如 D-14）要把兩份 ui 檔一起帶給 `ui-check`，單獨對一份跑會把跨模組合法引用誤判成 REF-07；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用。
