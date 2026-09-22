# T-12-fe-board-list state

> 2026-09-22 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

Dev 第 2 輪收尾，status＝`doing`，交回 Review。

這輪處理 Review 第 1 輪退回的 D-01～D-03，全部完成：
- D-01：補一則測試（點擊 Board 名稱後導覽到 `/boards/<id>`），現有 26 則測試全過。
- D-02：開立 `OQ-T-12-fe-board-list-02`（接手 T-14-fe-board-item），追蹤 `uc-reject-board-access-by-nonmember` 歸屬。
- D-03：`OQ-T-12-fe-board-list-01` 引文與反證有缺，改開 `OQ-T-12-fe-board-list-03` 取代（逐字引文＋並列設計稿反證，情況改標「兩處矛盾並列」）。

Review 這輪請先看：`fixes.md` 三則是否修好、`OQ-T-12-fe-board-list-03` 的並列是否足夠讓人工判斷、`BoardListPage.test.tsx` 新增測試是否符合驗收條件字面意思。

Check：`pnpm run build`／`pnpm run test`（26/26）／`pnpm exec oxlint` 皆綠燈；本輪未動後端，未重跑 gradle。
