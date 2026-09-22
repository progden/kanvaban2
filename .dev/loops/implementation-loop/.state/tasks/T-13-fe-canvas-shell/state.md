# T-13-fe-canvas-shell state

> 2026-09-22 Dev 第 3 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

Dev 第 3 輪收尾，status＝`review-pending`。

本輪只處理 D-05：`BoardCanvasPage.test.tsx` 新增三則測試，涵蓋 `uc-set-item-anchor`
（含 `convertBoxForAnchor()` 換算值的斷言）與 `uc-remove-items`（批次移除確認流程
＋失敗路徑）。D-01～D-04 維持第 2 輪已修好的狀態，未再變動。

`OQ-T-13-fe-canvas-shell-01` 維持既有標記（等級高、不阻塞、接手人工），未新增 OQ。

Check：`pnpm test` 43/43 通過、`pnpm run build` 通過、`pnpm run lint` 僅 1 則原有非阻斷 warning。

Review 這輪請先看新增的三則測試是否確實驗到 D-05 要求的三點（換算後座標、
確認對話框、失敗路徑訊息＋元件不變）。
