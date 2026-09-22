# T-20-fe-feature-cr-board state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：`s-feature-cr-board` Canvas item 已實作並 commit，測試／build／lint 全綠。

這輪做了什麼：新增 `featureCrBoardApi.ts`、`FeatureCrBoardItem.tsx`／`.css`／`.test.tsx`，於 `main.tsx` 註冊 `item.component === 'feature-cr-board'`；沿用 T-13 一般化掛載機制與 T-16 的實作結論。開立不阻塞 OQ-T-20-fe-feature-cr-board-01 說明 `item.component` 命名與入口機制為推論決定。

Review 要先看什麼：
1. `FeatureCrBoardItem.tsx` 是否忠實對應 `uc-view-feature-cr-board` post p1～p5（Feature 狀態、CR 分組、orphan、警告、大小寫由後端負責）。
2. `FeatureCrBoardItem.test.tsx` 五個 Scenario 對應測試是否涵蓋到位。
3. OQ-T-20-fe-feature-cr-board-01 的推論是否合理（比照 T-16 前例）。
