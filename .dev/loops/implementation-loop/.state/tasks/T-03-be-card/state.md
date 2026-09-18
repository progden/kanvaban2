# T-03-be-card state

> 2026-09-19 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

T-03-be-card Review 第 1 輪：退回（doing）
- 重跑 ./gradlew clean build --rerun-tasks：綠燈，71 個測試 0 失敗
- spec 對應：6 uc／8 Scenario 都有 step，feature 檔與 spec 逐字相同
- core 純度、任務邊界：通過
- D-01：removeSwimlane/removeStage 協調流程沒有交易、檢查順序錯，失敗會留下半套卡片異動；目的 Stage 沒驗證
- D-02：跨 Stage 移動沒斷言 fromStageId；createTestCard 沒檢查 201
- D-03：空白留言拒絕、卡片放置 swimlane/stage 未驗證，這兩項 spec 沒定義 fail，要開 OQ（高、不阻塞）
