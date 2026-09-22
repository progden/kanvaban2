# T-19-fe-workload open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-19-fe-workload-01

[Level: s-workload-dashboard/s-cards-by-assignee]
- 等級：高
- 阻塞：否
- 接手：T-14-fe-board-item
- 原因代碼：cross-item-dependency-missing
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】
`ui-workload.md`「操作」表「拖曳成員頭像到卡片追加負責人」列逐字：『觸發 `uc-drag-assign-card-owner`；卡片顯示於同一畫布上 F01 `s-board` item 的縮圖負責人更新（依 OQ-42，本畫面不顯示個別卡片，拖放目標為同時存在於畫布上的 `s-board` item）』；「驗收條件」段逐字：『拖曳成員頭像到同一畫布上 F01 `s-board` item 的卡片後，觸發 `uc-drag-assign-card-owner`』。`ui-user-membership.md`「s-cards-by-assignee」「操作」表逐字：『開啟卡片詳情 | — | 開啟 F01 `s-card-detail`（跨模組） | 不適用 | 否』。`.dev/loops/implementation-loop/.state/tasks.md` T-19 這一列的依賴欄只列『T-13-fe-canvas-shell、T-07-be-workload』，不含 T-14-fe-board-item。
推論：這兩個操作的實際掛載點（F01 `s-board` item 的卡片縮圖、`s-card-detail`）屬於 T-14-fe-board-item 的產出範圍；T-19 開工時 T-14 仍是 `doing`（尚未合併回整合分支），本 worktree 裡不存在任何卡片縮圖節點或 `s-card-detail` 路由，且 T-14 也不是 T-19 在 `tasks.md` 上登記的依賴，無法比照其他任務「依賴未合併就標 blocked」處理。已完成的取捨：(1) 「拖曳成員頭像到卡片」只實作拖曳來源端（`WorkloadDashboard.tsx` 的頭像 `draggable`／`startUserAvatarDrag`），並把拖放協定抽成 `kanban-frontend/src/canvas/cardAssigneeDrag.ts` 共用模組（`CARD_ASSIGNEE_DRAG_MIME`／`acceptsCardAssigneeDrop`／`handleCardAssigneeDrop`），已用假的 `DataTransfer` 測試來源／協定邏輯，但沒有測試到真正掛在卡片縮圖節點上的 `onDrop`。(2) `s-cards-by-assignee` 的「點擊清單中的卡片開啟 F01 `s-card-detail`」目前沒有可導向的路由，卡片列做成純顯示、不可點擊。
問題：這個處理方式（實作來源端＋共用協定模組，留白給 T-14 接上）是否可接受？T-14-fe-board-item 之後合併時，是否需要／應該直接沿用 `cardAssigneeDrag.ts` 的 `handleCardAssigneeDrop`／`acceptsCardAssigneeDrop`，並在卡片詳情就緒後回頭讓 `CardsByAssigneeDialog.tsx` 的卡片列可點擊導向 `s-card-detail`？
選項：A. 維持現況，T-14 合併時沿用 `cardAssigneeDrag.ts` 接上卡片縮圖的 `onDrop`，並讓 T-19（或後續一輪）補上卡片列可點擊導向 `s-card-detail`；B. 把「拖曳追加負責人」「點擊卡片開啟詳情」整段挪出 T-19 範圍，改列為 T-14 的依賴項或另開一個銜接任務；C. 以上皆非。
