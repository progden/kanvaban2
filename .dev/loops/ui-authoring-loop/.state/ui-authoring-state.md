# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）已收尾完成（D-14～D-16 全部 done），即將進入階段 3（F03 kanban-widgets）
- 上一輪任務：D-16——修正 `ui-user-membership.md` `s-card-assignee-picker` 兩處失實引用：刪除「空資料」行掛在 `uc-view-card-assignees` post 名下的錯誤歸因（保留畫面推論本身，與驗收條件描述一致，不需另掛 post）；刪除「儲存變更」的「成功後」欄「卡片縮圖同步顯示」（該行為屬於 F01 `s-board`，不在本畫面也不在回到的 `s-card-detail`）。未新增 OQ（純粹移除失實引用，非新的規則衝突）。D-16 已標 `done`
- 下一個任務：T3.01（[F03] 建立 `ui-kanban-widgets.md`：檔頭＋4 個畫面標題骨架 `s-cycle-lead-time-dashboard`／`s-wip-dashboard`／`s-throughput-cfd-dashboard`／`s-duedate-reminder`，同 T1.01 模式）
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾（D-14～D-16 全部完成，F02 真正收尾）；F03～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-30（`s-card-add-dialog` 的 `uc-add-card`／`uc-member-add-card` 角色互斥問題，見 PDCA Iteration 40）；本輪未新增 OQ
- 待注意：OQ 表新增列時務必檢查『』引用所在行不能同時含有多個模組的 spec 檔名／短名（`verify-quotes.py` 的 `spec_for_line` 用整行文字比對，第一個命中的模組就決定用哪份 spec 驗證全部引用，混寫會導致跨模組引用誤判失敗）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用，工具名稱與 crud 片段要用「」或不加符號，否則觸發 REF-07；單檔 `ui-check` 若涉及跨模組引用（如 F02 引用 F01 的 `s-card-detail`）務必把全部 ui 檔一起傳入並帶 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`，否則會誤判 REF-07。
