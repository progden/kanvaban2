# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 3（F03 kanban-widgets）已開始，T3.01（建立檔頭與 4 個畫面標題骨架）完成
- 上一輪任務：T3.01——建立 `ui-kanban-widgets.md`：檔頭 ＋ 4 個畫面標題骨架（`s-cycle-lead-time-dashboard`／`s-wip-dashboard`／`s-throughput-cfd-dashboard`／`s-duedate-reminder`），三行標頭（所屬 Feature、類型、狀態：未討論），八段內容留空，同 T1.01 模式。Feature 名稱取自 `spec-kanban-widgets.md` 四個 `## Feature:` 標題；類型依任務清單既定（三儀表板一列表）。`ui-check` 對本檔 DS-01 無 error。T3.01 已標 `done`
- 下一個任務：T3.02（[F03] 定案 `s-cycle-lead-time-dashboard`（類型：儀表板）：對應 `uc-view-cycle-lead-time`；資料段列出每個數字/圖表的來源 Attribute 或衍生計算方式）
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾；F03 骨架已建立（4 個畫面標題，尚未定案內容）；F04～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-30（`s-card-add-dialog` 的 `uc-add-card`／`uc-member-add-card` 角色互斥問題，見 PDCA Iteration 40）；本輪未新增 OQ
- 待注意：OQ 表新增列時務必檢查『』引用所在行不能同時含有多個模組的 spec 檔名／短名（`verify-quotes.py` 的 `spec_for_line` 用整行文字比對，第一個命中的模組就決定用哪份 spec 驗證全部引用，混寫會導致跨模組引用誤判失敗）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用，工具名稱與 crud 片段要用「」或不加符號，否則觸發 REF-07；單檔 `ui-check` 若涉及跨模組引用務必把全部 ui 檔一起傳入並帶 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`，否則會誤判 REF-07。
