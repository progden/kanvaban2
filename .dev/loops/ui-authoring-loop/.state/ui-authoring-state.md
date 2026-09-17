# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：F01～F04 全部收尾完成；F05 workload 開始，T5.01 已完成
- 上一輪驗證：PASS（範圍 320f2df..e160302，任務 D-21）
- 上一輪任務：T5.01——建立 `ui-workload.md`：檔頭 ＋ 1 個畫面標題骨架（`s-workload-dashboard`）
- 本輪任務：T5.02——定案 `s-workload-dashboard`（類型：儀表板）：對應 `uc-view-workload`／`uc-drag-assign-card-owner`（拖曳成員頭像到卡片），依規則書「輸入怎麼讀」讀 `spec-workload.md` 名詞定義三張表、角色定義，八段內容逐一定案，推不出來的標 ⚠️ 記 OQ
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾；F03 全部 4 個畫面已收尾（討論中）；F04 全部 1 個畫面已收尾（討論中）；F05 骨架已建立（1 畫面，未討論），F06～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-40（修正 OQ-35「情況」欄對 OQ-34 結論的誤述，對應 D-21，追加而非編輯）
- 下一個任務：T5.02（定案 `s-workload-dashboard`，完成後 T5.03 收尾 F05）
- 待注意：OQ 檔任何時候都只能追加，PDCA 也只能追加，發現舊紀錄有缺漏一律用本輪補記說明，不回頭編輯舊 Iteration。`uc-drag-assign-card-owner`（`spec-workload.md`）與 `uc-assign-card-owner-by-drag`（`spec-user-membership.md`）同義但分屬不同 Feature（OQ-09 已定案），寫 `s-workload-dashboard` 操作表時留意只引用本模組的 `uc-drag-assign-card-owner`。
