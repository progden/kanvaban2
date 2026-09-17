# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：F01～F05 全部收尾完成；F06、F07 尚未開始
- 上一輪驗證：PASS（範圍 1076505..4c43e1d，任務 D-22）
- 本輪任務：T5.03——收尾確認 `ui-check .dev/F05-workload/ui-workload.md` 為 0 error（延續 D-22 已修好的內容，未再改動 `ui-workload.md`），任務清單將 T5.03 標為 done
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾；F03 全部 4 個畫面已收尾（討論中）；F04 全部 1 個畫面已收尾（討論中）；F05 全部 1 個畫面已收尾（討論中，T5.01～T5.03 全部 done）；F06～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-41（Workload 表進入路徑未定義）、OQ-42（Workload 表是否顯示個別卡片供拖曳目標，spec 未定義）；本輪未新增 OQ
- 下一個任務：T6.01（[F06] 建立 `ui-feature-cr-board.md`：檔頭 ＋ 1 個畫面標題骨架 `s-feature-cr-board`，同 T1.01 模式）
- 待注意：OQ 檔任何時候都只能追加，PDCA 也只能追加，發現舊紀錄有缺漏一律用本輪補記說明，不回頭編輯舊 Iteration。`uc-drag-assign-card-owner`（`spec-workload.md`）與 `uc-assign-card-owner-by-drag`（`spec-user-membership.md`）同義但分屬不同 Feature（OQ-09 已定案），`s-workload-dashboard` 操作表只引用本模組的 `uc-drag-assign-card-owner`。
