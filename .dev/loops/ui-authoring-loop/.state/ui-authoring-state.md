# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 3（F03 kanban-widgets）進行中，T3.02（定案 `s-cycle-lead-time-dashboard`）完成
- 上一輪驗證：FAIL（D-15 的 `ui-check(all)=0` 因 T3.01 骨架暫時破功，52 error）；本輪依規則先修正——完成 T3.02 後本檔 error 降到 39（單檔）／41（含跨模組 --spec），屬漸進修正中，預期 T3.05 完成後全部歸零
- 上一輪任務：T3.02——定案 `s-cycle-lead-time-dashboard`（類型：儀表板）：目的、進入與離開（⚠️ OQ-31）、角色與權限（`r-user`，F01跨模組）、資料（Lead Time／Cycle Time／完成時間／排除計算卡片數／統計摘要，皆標明衍生計算來源）、操作（`uc-view-cycle-lead-time`）、狀態五項、驗收條件、待確認事項。畫面狀態「討論中」（因有 ⚠️）。`ui-check` 對本畫面無 error（僅 DS-07 warn，同 `s-board` 情形）。T3.02 已標 `done`
- 下一個任務：T3.03（[F03] 定案 `s-wip-dashboard`（類型：儀表板）：對應 `uc-view-wip`／`uc-view-aging-wip`）
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾；F03：`s-cycle-lead-time-dashboard` 已定案（討論中，因 OQ-31／百分位 ⚠️），其餘 3 個畫面尚未定案；F04～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-31（F03 四個圖表畫面共通的「從哪裡進來」缺口，F01 `s-board` 操作表尚無「檢視圖表」操作；預期 T3.03～T3.05 會重複引用同一筆，不重開新列）
- 待注意：同 OQ-31 的入口缺口會出現在 `s-wip-dashboard`／`s-throughput-cfd-dashboard`／`s-duedate-reminder`，撰寫時直接引用 OQ-31，不要重新開一筆；OQ 表新增列時務必檢查『』引用所在行不能同時含有多個模組的 spec 檔名／短名；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用；單檔 `ui-check` 若涉及跨模組引用務必帶 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`，否則會誤判 REF-07。
