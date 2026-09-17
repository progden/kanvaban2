# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 3（F03 kanban-widgets）進行中，T3.03（定案 `s-wip-dashboard`）完成
- 上一輪驗證：FAIL（[4] PDCA 標題格式錯誤、[9] D-15 的 `ui-check(all)=0` 因 F03 尚未收尾仍非 0）；本輪先修正 Iteration 43 標題格式，D-15 屬漸進修正中（本檔單檔 error 從 39 降到 26），預期 T3.06 完成後全部歸零
- 上一輪任務：T3.03——定案 `s-wip-dashboard`（類型：儀表板）：目的、進入與離開（⚠️ 沿用 OQ-31，不重開新列）、角色與權限（`r-user`，F01跨模組）、資料（Stage 卡片數／卡片年齡，皆標明來源）、操作（`uc-view-wip`／`uc-view-aging-wip`）、狀態五項、驗收條件、待確認事項。畫面狀態「討論中」（因沿用 OQ-31 的 ⚠️）。`ui-check` 對本畫面無 error（僅 DS-07 warn，同 `s-cycle-lead-time-dashboard` 情形）。T3.03 已標 `done`
- 下一個任務：T3.04（[F03] 定案 `s-throughput-cfd-dashboard`（類型：儀表板）：對應 `uc-view-throughput`／`uc-view-cfd`）
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾；F03：`s-cycle-lead-time-dashboard`／`s-wip-dashboard` 已定案（皆討論中，因沿用 OQ-31），其餘 2 個畫面尚未定案；F04～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-31（F03 四個圖表畫面共通的「從哪裡進來」缺口，F01 `s-board` 操作表尚無「檢視圖表」操作；`s-wip-dashboard` 已沿用，預期 T3.04～T3.05 也會重複引用同一筆，不重開新列）
- 待注意：同 OQ-31 的入口缺口會出現在 `s-throughput-cfd-dashboard`／`s-duedate-reminder`，撰寫時直接引用 OQ-31，不要重新開一筆；操作表「觸發」欄的 uc 名稱必須與「角色與權限」表「做得到」欄的操作名稱逐字相同（DS-05 檢查依此比對），並用「、」分隔多個操作；單檔 `ui-check` 若涉及跨模組引用務必帶 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`，否則會誤判 REF-07。
