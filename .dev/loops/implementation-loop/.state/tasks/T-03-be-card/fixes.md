# T-03-be-card 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | 【uc-delete-swimlane／uc-delete-stage 協調流程非原子，失敗時會留下半套資料】 檔案：kanban-spring/src/main/java/io/progden/kanban/spring/application/BoardApplicationService.java 的 `removeSwimlane(..., confirmed)`／`removeStage(..., destinationStageId)`。整個 kanban-spring 沒有任何 `@Transactional`（`grep -rn Transactional kanban-spring/src/main` 無結果），而且兩個方法都是「先逐張 `cardRepository.save` 改卡片，最後才呼叫 `board.removeSwimlane`／`board.removeStage` 做 `ensureSwimlaneRemovable`／`ensureStageRemovable` 檢查」。每次 `save` 各自提交，後面的檢查一丟例外，前面改掉的卡片不會回滾。 具體會出錯的情境（依程式碼推得）： 1. 看板只剩 1 個 Swimlane 且內有卡片，呼叫 `DELETE .../swimlanes/{id}?confirmed=true`：卡片先被軟刪除，接著 `MINIMUM_SWIMLANE` 拒絕 → Swimlane 還在、卡片卻已經不見。spec `uc-delete-swimlane` fail p1 寫的是『拒絕，該 `swimlane` 不被刪除』，拒絕時卡片不應該被動到。 2. `swimlaneId` 不屬於該 board（或 `stageId` 不屬於該 board）：`findActiveBySwimlaneId` 只用 swimlaneId 查，會先刪掉／搬走別的看板的卡片，然後才丟 NOT_FOUND。 3. `destinationStageId` 等於來源 Stage、不存在、或屬於別的看板：前兩者會寫入 StageTransition 後被拒絕；後者會把卡片搬到不存在的 Stage，然後來源 Stage 因卡片數歸零而被成功刪除，留下孤兒卡片（違反 `uc-delete-stage` post『`card.stage` 更新為使用者選擇的目的 `stage`』——目的必須是這個 board 的 `stage`）。 修好的標準： - 先完成 board 端的可刪除檢查（最少數量、swimlane/stage 屬於該 board、目的 Stage 存在於同一 board 且不等於來源），檢查通過才動卡片；並且讓「卡片刪除／轉移＋board 刪除 Swimlane/Stage」在同一個交易裡（例如 application service 方法加 `@Transactional`），任一步失敗全部回滾。 - 目的 Stage 不合法時要回應什麼錯誤、訊息為何，spec 沒定義——照 ADR-001 的 fail→HTTP 慣例自行決定並記 decision-log；若認為需要業務確認，另開 OQ。 - 補測試（Cucumber 以外的 Spring 整合測試即可，不要改 feature 檔）至少覆蓋情境 1 與情境 3（目的 Stage 不屬於該 board），斷言拒絕後卡片的 `deleted`／`stage`／StageTransition 數量都不變。 |
