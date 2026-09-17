# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 3（F03 kanban-widgets）進行中，T3.05（定案 `s-duedate-reminder`）完成
- 上一輪驗證：FAIL（[9] D-15 的 `ui-check(all)=0` 因 F03 尚未收尾仍非 0，屬預期中的漸進修正，非本輪造成，繼續完成 state 記載的下一步 T3.05；上上輪誤改 PDCA 舊標題的問題已不再重犯，本輪未動任何既有 PDCA 內容）
- 上一輪任務：T3.05——定案 `s-duedate-reminder`（類型：列表）：目的、進入與離開（⚠️ 沿用 OQ-31）、角色與權限（`r-user`，F01跨模組）、資料（門檻天數／已逾期／即將到期用「清單」欄合併在同一張表）、操作（`uc-view-duedate-reminder`）、狀態五項、驗收條件、待確認事項。門檻天數型別／範圍 spec 未定義，新開 OQ-32（高風險，非入口類問題，另開一列而非沿用 OQ-31）。畫面狀態「討論中」。本檔單檔 error 從 13 降到 0（`tools error-count` 確認）。T3.05 已標 `done`
- 下一個任務：T3.06（[F03] 收尾：`ui-check .dev/F03-kanban-widgets/ui-kanban-widgets.md` 0 error；過一遍 DS-06／DS-07 warn）
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾；F03：4 個畫面（`s-cycle-lead-time-dashboard`／`s-wip-dashboard`／`s-throughput-cfd-dashboard`／`s-duedate-reminder`）皆已定案（皆討論中，因沿用 OQ-31），僅剩 T3.06 收尾任務；F04～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-32（`s-duedate-reminder` 門檻天數型別／範圍 spec 未定義，僅有範例值 3 天）
- 待注意：**PDCA 只能在檔尾追加，連格式錯誤的舊標題也不可回頭修正**；T3.06 收尾時單檔 `ui-check` 需帶 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"` 才不會誤判跨模組 `s-board` 引用為 REF-07（`tools error-count` 已內建此邏輯，直接用即可）；DS-06（F01/F02/F04/F05/F07 寫入 uc 未被任何畫面觸發）與 DS-07（F03 四個圖表畫面無入口導向）warn 是預期中的已知缺口（源自 OQ-31 入口未定案），T3.06「過一遍」只需確認沒有新增非預期 warn，不必也不能消除這些既有 warn（消除需先解決 OQ-31，非本 loop 職權）。
