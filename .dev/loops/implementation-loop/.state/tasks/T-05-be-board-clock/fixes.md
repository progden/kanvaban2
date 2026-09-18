# T-05-be-board-clock 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | `CardApplicationService` 的卡片寫入流程不是原子操作：寫入失敗時，看板的 `lastEventAt` 基準仍然被推進並存檔了，違反 `uc-guard-clock-monotonicity` pre p1『看板時間不早於該 `board` 最後一筆事件的發生時間』裡「最後一筆事件」的定義。 位置：`kanban-spring/.../application/CardApplicationService.java` 的 `newEventTimeFor`，以及 `editCard`／`moveCardSwimlane`／`moveCardStage`／`addComment`／`deleteCard`；這個類別沒有標 `@Transactional`。 重現：`addComment` 先呼叫 `newEventTimeFor`，裡面的 `board.newEventTime(...)` 把 `lastEventAt` 推進到目前的看板時間，然後立刻執行 `boardRepository.save(board)`；接著 `card.addComment` 在內容空白時丟出 `EMPTY_COMMENT_CONTENT`。留言被拒絕、沒有新增任何事件，但 board 的 `lastEventAt` 已經存檔。後續 Owner 把看板時間調到這個時間點之前、再建立卡片，就會被擋下，錯誤訊息引用的「最後一筆事件（HH:mm）」時間點其實並不存在。`addCard` 和 `BoardApplicationService` 的 `removeSwimlane`／`removeStage` 目前沒有這個問題（前者在 `Card.create` 成功後才存 board，後者有 `@Transactional`），可以拿來當修正的參考。 算修好的條件： 1. 任一卡片寫入方法只要在 domain 驗證階段失敗（或 card 存檔失敗），board 的 `lastEventAt` 不可以被推進、也不可以存檔（例如標 `@Transactional`，並且/或改成先完成 card 的操作、最後才存 board）。 2. 補一個自動化測試（Cucumber 或 Spring 整合測試都可以）：在已知的看板時間送出空白留言 → 被拒絕 → 再把看板時間調到比剛才更早、但仍晚於上一筆真實事件的時間 → 建立卡片要成功。 |
