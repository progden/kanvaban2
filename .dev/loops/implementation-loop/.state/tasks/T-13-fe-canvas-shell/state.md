# T-13-fe-canvas-shell state

> 2026-09-22 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪退回的 D-01～D-04 已在第 2 輪全數修好並標記 done。

- D-01：掛載內容 itemId 改傳 item.id，另傳 component 供顯示。
- D-02：畫面 z-index 改用相對名次分層，chrome（加入元件／浮動工具列／縮放控制／錯誤訊息）恆高於 item；工具列 top 夾住不被裁掉。
- D-03：PlaceItemDialog 補 X／Y／三項能力輸入；浮動工具列拆成三顆能力切換按鈕。
- D-04：開立 OQ-T-13-fe-canvas-shell-01（高／不阻塞），列出 spec 與 ui 兩處矛盾原文。

建置／lint／測試本輪都跑過且全綠（tsc -b、oxlint、vitest 40/40、vite build）。

Review 這輪請先看：CanvasStage.tsx 的 z-index 分層邏輯（CANVAS_ITEM_Z_BASE／SCREEN_ITEM_Z_BASE／CHROME_Z）
是否符合「畫面固定元素永遠繪於畫布元素之上」；PlaceItemDialog 新增欄位是否符合 uc-place-item 預設值；
OQ-T-13-fe-canvas-shell-01 的兩處引用是否逐字正確。
