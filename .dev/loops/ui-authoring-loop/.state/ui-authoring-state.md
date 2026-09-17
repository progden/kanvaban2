# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 3（F03 kanban-widgets）進行中，T3.04（定案 `s-throughput-cfd-dashboard`）完成
- 上一輪驗證：FAIL（[4] PDCA 舊內容被修改——上上輪誤把 Iteration 43 標題格式修正當成合法修改，其實 append-only 規則不允許回頭改任何已提交內容，即使只改格式；[9] D-15 的 `ui-check(all)=0` 因 F03 尚未收尾仍非 0）。本輪不再嘗試編輯歷史 PDCA 內容（Iteration 43 標題永久維持原樣，不會再被檢查，因為它已在本輪 BASE 之前）；D-15 屬預期中的漸進修正，繼續完成 state 記載的下一步 T3.04
- 上一輪任務：T3.04——定案 `s-throughput-cfd-dashboard`（類型：儀表板）：目的、進入與離開（⚠️ 沿用 OQ-31）、角色與權限（`r-user`，F01跨模組）、資料（Throughput／CFD 兩區塊用「圖表」欄合併在同一張表，不拆兩畫面）、操作（`uc-view-throughput`／`uc-view-cfd`）、狀態五項、驗收條件、待確認事項。畫面狀態「討論中」（因沿用 OQ-31）。本檔單檔 error 從 26 降到 13（剩 `s-duedate-reminder` 骨架與既有 `s-board` REF-07／DS-07 warn）。T3.04 已標 `done`
- 下一個任務：T3.05（[F03] 定案 `s-duedate-reminder`（類型：列表），對應 `uc-view-duedate-reminder`）
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾；F03：`s-cycle-lead-time-dashboard`／`s-wip-dashboard`／`s-throughput-cfd-dashboard` 已定案（皆討論中，因沿用 OQ-31），僅 `s-duedate-reminder` 尚未定案；F04～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-31（F03 圖表畫面共通的「從哪裡進來」缺口，`s-throughput-cfd-dashboard` 已沿用，預期 T3.05 也會重複引用同一筆，不重開新列）
- 待注意：**PDCA 只能在檔尾追加，連格式錯誤的舊標題也不可回頭修正**（會觸發 verify [4]，且改了也還是違規，等於白改）；同 OQ-31 的入口缺口會出現在 `s-duedate-reminder`，撰寫時直接引用 OQ-31；操作表「觸發」欄的 uc 名稱必須與「角色與權限」表「做得到」欄的操作名稱逐字相同（DS-05）；單檔 `ui-check` 涉及跨模組引用務必帶 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；「資料」段的表格解析器只讀該標題後第一張表，若一個畫面要呈現多個資料區塊，用同一張表加一個分類欄（例如「圖表」）區分，不要寫成兩張獨立表格（第二張會被忽略）。
