# T-04-be-board-membership state

> 2026-09-19 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：D-01、D-02 已修好並補測試，build 全綠（113 個測試）。

這輪做了什麼：
- 新增卡片、設定／拖曳負責人（寫入類）拒絕非成員與 Viewer；候選名單、依負責人查詢、成員名單（讀取類）只拒絕非成員（Viewer 可讀）。
- `uc-view-board-list` 依 post 字面排除 Viewer 的看板。
- 新增 OQ-04（成員資格檢查切分／訊息）、OQ-05（Viewer 是否該出現在看板列表，兩處矛盾並列）。

Review 要先看什麼：
- `BoardMembershipApplicationService.ensureCanEdit`／`ensureMember`／`listBoardsForUser` 的改動是否合理。
- `CardApplicationServiceTest`、`BoardMembershipApplicationServiceTest` 兩個新測試檔。
- OQ-04、OQ-05 的推論是否成立。
