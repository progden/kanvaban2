# T-13-fe-canvas-shell state

> 2026-09-22 Review 第 2 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

# T-13-fe-canvas-shell state

Review 第 2 輪：退回，status＝`doing`。

我自己前景跑過（不採信 Dev 的 Check）：`pnpm run build` 通過、`pnpm test` 40/40 通過、`pnpm run lint` 僅 1 則非阻斷 warning。

D-01～D-04 逐條複驗確認修好；`OQ-T-13-fe-canvas-shell-01` 的兩段引文我到 spec:67／ui:81 逐字比對無誤，
等級「高」、不阻塞、接手「人工」標記正確；任務邊界乾淨（只動 `kanban-frontend/src/**` 與本任務 `.state` 目錄）；
設計稿核對通過（工具列拆成三顆能力按鈕是 D-03 要求的結果，樣式仍沿用同一套語彙）。

未過的一項 → D-05：`uc-set-item-anchor` 與 `uc-remove-items`（批次移除）完全沒有前端測試，
兩者都是 `ui-canvas-layout.md`「驗收條件」逐字列出的項目；其餘九個 uc 都有測試。
`uc-set-item-anchor` 還牽涉 `canvas/geometry.ts` `convertBoxForAnchor()` 的座標換算（Dev 自行決定的邏輯），無任何把關。

下一輪 Dev 只要補這兩則測試（含一則失敗路徑斷言）即可；不需要改既有行為，除非補測試時發現 bug。

保留事項（不擋核准）：OQ-01 接手人工；Swimlane／Stage 入口接手 T-14；看板成員 item 接手 T-15；
縮放事前 clamp 使 `uc-set-viewport` p1 在畫面上不可觸發，已列為觀察不列退回理由。
