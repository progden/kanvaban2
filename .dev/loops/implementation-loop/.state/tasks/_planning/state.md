# 安排階段 現況（`_planning`，每輪覆寫）

- 最後一次安排：2026-09-22（第二次重跑，`T-08-be-feature-cr-board-r2` 合併 `574f8f6` 之後）。
- 任務總數 24；狀態：13 `done`、11 `todo`、0 `blocked`、0 `doing`／`review-pending`。
- 本輪變動：**`.state/tasks.md` 零改動**（沒有新增／刪除／重寫任何列，沒有動任何 `status` 檔，沒有開立 `OQ-PLAN-xx`）。上游 spec／ui 自上一輪以來沒有新的 commit，第 5-1／5-4 步的 A−B 比對每個主體都是空集合。
- CR 落後檢查結果：CR-001～CR-013 全部已由某個 `done` 實例涵蓋，包含上一輪追加的 CR-013（`T-08-be-feature-cr-board-r2` 已完成合併）。目前沒有任何主體的程式碼落後 spec。
- 待人工處理（不阻塞本 loop）：
  - `.dev/CR.md` CR-007／CR-009／CR-012／CR-013 狀態欄仍為「待處理」，程式碼皆已落地，需人工翻成「處理完成」（否則 GH-05 持續嚴格擋 F02／F03／F06 的格式性修訂）。
  - `OQ-T-08-be-feature-cr-board-r2-01`（多個 affects 目標時的互斥判定單位）待人工定案；若定案後開新 CR，下一輪安排階段會追加 `T-08-be-feature-cr-board-r3`。
- 下一輪驅動腳本可平行啟動的任務（依賴皆 `done` 且已合併，`MAX_PARALLEL=5` 名額足夠）：
  - `T-07-be-workload`（依賴 T-03、T-04）
  - `T-09-be-canvas-layout`（依賴 T-02、T-04）
  - `T-12-fe-board-list`（依賴 T-10、T-02、T-04、T-22）
- 仍被依賴卡住的：`T-13-fe-canvas-shell` 等 T-12／T-09；`T-14`～`T-20` 等 T-13（加上各自的後端任務）。剩下的路徑是一條以 T-12 → T-13 為瓶頸的前端鏈，後端只剩 T-07、T-09 兩列。
