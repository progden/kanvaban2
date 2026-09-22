# T-13-fe-canvas-shell state

> 2026-09-22 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪判定退回，回到 Dev 處理 D-01～D-04。

- 建置／測試／lint 我自己跑過，全綠（build 通過、vitest 36/36、oxlint 僅 1 則非阻斷 warning）。
- 任務邊界乾淨：只動 `kanban-frontend/src/**` 與 `.state/tasks/T-13-fe-canvas-shell/**`。
- 退回理由（`fixes.md`）：
  - D-01 掛載點 `itemId` 傳成 `item.component`，破壞對 T-14～T-20 的對外契約。
  - D-02 畫面 z-index 直接用 `item.z`：負 z 元素消失、「＋ 加入元件」被預設 item 蓋住、浮動工具列被裁掉／被蓋住。
  - D-03 缺 ui 資料表明訂為「輸入」的 `item.x`／`item.y`（放置時）與三項能力（放置、設定能力時）。
  - D-04 spec 與 ui 對角色對應的矛盾沒有依規則開成 OQ。
- 目前沒有任何 OQ（D-04 就是要求 Dev 補開一則不阻塞的 OQ）。
- 保留給其他任務：ui 操作表的新增／管理 Swimlane、Stage 由 T-14-fe-board-item 接手；「看板成員」item 進入 `s-member-management` 由 T-15-fe-member-management 接手。
