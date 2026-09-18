# T-05-be-board-clock open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-05-be-board-clock-01

[Level: uc-adjust-board-clock,uc-pause-resume-board-clock]
- 等級：高
- 阻塞：否
- 接手：T-04-be-board-membership
- 原因代碼：upstream-missing
- 開立：Dev 第 1 輪（2026-09-19）
- 狀態：待處理

情況：【推論＋所本原文】
`spec-board-clock.md` 的 `uc-adjust-board-clock`／`uc-pause-resume-board-clock` 兩個 usecase 逐字寫著：

```
  roles: [r-board-owner]
  ...
  pre:
    p1: "我是該 `board` 的 Owner"
```

`uc-adjust-board-clock` 的 `fail` 也逐字寫著：

```
  fail:
    p1: "拒絕，顯示錯誤訊息「只有 Owner 可以調整看板時間」，`board.clock-time` 維持不變"
```

`BoardApplicationService` 類別註解（T-02 既有程式碼，本任務未改動這段文字）逐字寫著：
『{@code r-board-owner} 權限檢查（僅 Owner 可調整看板結構）依賴 F02 的 {@code BoardMembership}
（T-04 尚未實作），本服務目前只要求呼叫端已登入即可操作，尚未強制「僅 Owner」——見
implementation-loop OQ-IMPL-15，待 T-04 補上。』

推論：T-05 的依賴只有 T-02-be-board、T-03-be-card（見 `.state/tasks.md`），不含 T-04，也就是說
`BoardMembership`（誰是 Owner 的正式來源）在這個 worktree 裡不存在。但 `uc-adjust-board-clock` 的
fail p1 是這個 usecase 自己逐字定義、明確要測的情境（不像 F01 的 9 個結構調整 uc 是把「非 Owner」
整個獨立成 F02 的 `uc-reject-structure-change-by-member`、暫時整段不測），所以我沒有直接比照
OQ-IMPL-15 完全不做權限檢查，而是用 `board.createdBy`（`Board.create` 的建立者，`kanban-core` 既有
欄位）暫代「Owner」：因為目前系統裡唯一保證是 Owner 的人就是建立者，這個代理判斷不會誤判「真正
的 Owner」為非 Owner，但等 T-04 的 `BoardMembership` 上線、Owner 可能轉移或有多個協作者角色後，
這個判斷就不再等價於正式的 `r-board-owner`，需要回頭改成查 `BoardMembership`。

問題：`uc-adjust-board-clock`／`uc-pause-resume-board-clock` 的 Owner 權限檢查，現在用
`board.createdBy` 代理是否可以接受，還是應該比照 OQ-IMPL-15 完全不檢查、把「非 Owner」Scenario
也延後給 T-04？

選項：A. 維持本輪做法（`board.createdBy` 代理 Owner，`BoardApplicationService.requireOwner`），
等 T-04 完成 `BoardMembership` 後再改成正式查詢；B. 改回比照 OQ-IMPL-15 完全不檢查，本任務不實作
`board-clock.feature` 的「非 Owner 嘗試調整看板時間」Scenario，一併記錄延後給 T-04。
