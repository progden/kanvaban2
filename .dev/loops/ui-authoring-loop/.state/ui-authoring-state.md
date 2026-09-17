# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：F01～F04 全部收尾完成；F05 workload：T5.01、T5.02、D-22 已完成，剩 T5.03 收尾
- 上一輪驗證：PASS（範圍 f997b9a..1076505，任務 T5.02）
- 本輪任務：D-22——修正 `s-workload-dashboard`「驗收條件」段與操作表「成功後」欄違反 ui-convention「斷言主詞只能是畫面元素或是否觸發 uc-xxx」規則；刪除 3 行照抄自 uc post 的領域規則，改寫 2 處為畫面元素／觸發 uc 斷言（拖曳那行加註 ⚠️ 見 OQ-42，呼應「資料」段卡片清單呈現方式未定）
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾；F03 全部 4 個畫面已收尾（討論中）；F04 全部 1 個畫面已收尾（討論中）；F05 骨架＋1 畫面已定案內容並修正驗收條件段落（討論中，剩 T5.03 收尾），F06～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-41（Workload 表進入路徑未定義）、OQ-42（Workload 表是否顯示個別卡片供拖曳目標，spec 未定義）；本輪未新增 OQ
- 下一個任務：T5.03（收尾：`ui-check .dev/F05-workload/ui-workload.md` 0 error，目前已是 0 error，僅需跑一次確認並收尾）
- 待注意：OQ 檔任何時候都只能追加，PDCA 也只能追加，發現舊紀錄有缺漏一律用本輪補記說明，不回頭編輯舊 Iteration。`uc-drag-assign-card-owner`（`spec-workload.md`）與 `uc-assign-card-owner-by-drag`（`spec-user-membership.md`）同義但分屬不同 Feature（OQ-09 已定案），`s-workload-dashboard` 操作表只引用本模組的 `uc-drag-assign-card-owner`。
