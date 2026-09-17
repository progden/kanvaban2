# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：F01～F04 全部收尾完成；F05 workload：T5.01、T5.02 已完成，剩 T5.03 收尾
- 上一輪驗證：FAIL（範圍 e160302..38ab9da，任務 T5.01；D-15 的 `ui-check(all)=0` 因 T5.01 骨架未填八段內容而破功）
- 本輪先修正失敗項目：T5.02 定案 `s-workload-dashboard` 八段內容，補齊後 `ui-check(all)` 回到 0 error，D-15 的機械驗收條件重新成立
- 本輪任務：T5.02——定案 `s-workload-dashboard`（類型：儀表板）：對應 `uc-view-workload`／`uc-drag-assign-card-owner`；畫面狀態維持「討論中」（進入路徑與拖曳目標卡片呈現方式未定，標 ⚠️，見 OQ-41、OQ-42）
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾；F03 全部 4 個畫面已收尾（討論中）；F04 全部 1 個畫面已收尾（討論中）；F05 骨架＋1 畫面已定案內容（討論中，剩 T5.03 收尾），F06～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-41（Workload 表進入路徑未定義，比照 OQ-31／OQ-34／OQ-35／OQ-40 同類問題）、OQ-42（Workload 表是否顯示個別卡片供拖曳目標，spec 未定義）
- 下一個任務：T5.03（收尾：`ui-check .dev/F05-workload/ui-workload.md` 0 error，目前已是 0 error，僅需跑一次確認並收尾）
- 待注意：OQ 檔任何時候都只能追加，PDCA 也只能追加，發現舊紀錄有缺漏一律用本輪補記說明，不回頭編輯舊 Iteration。`uc-drag-assign-card-owner`（`spec-workload.md`）與 `uc-assign-card-owner-by-drag`（`spec-user-membership.md`）同義但分屬不同 Feature（OQ-09 已定案），`s-workload-dashboard` 操作表只引用本模組的 `uc-drag-assign-card-owner`。
