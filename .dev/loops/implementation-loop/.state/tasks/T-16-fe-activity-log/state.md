# T-16-fe-activity-log state

> 2026-09-22 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

修正 D-01：`ActivityLogItem` 載入失敗的錯誤段落改用共用 `.form-error` 樣式，`.activity-log__error` 收斂成只管版面。`pnpm exec vitest run`（47/47）、`pnpm run build`、`pnpm run lint`（僅既有 T-13 警告）全綠。D-01 已標 done，OQ-T-16-fe-activity-log-01／02 維持不阻塞、待人工處理。Review 這輪請先看 `ActivityLogItem.tsx`／`.css` 的 diff 是否確實對齊 `.form-error` 語彙。
