# T-03-be-card state

> 2026-09-19 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

第 2 輪已修完 Review 第 1 輪退回的 D-01／D-02／D-03，等待 Review 第 2 輪。

- D-01：removeSwimlane／removeStage 改成先做 board 端可刪除檢查（含新增
  `Board.ensureValidDestinationStage`）通過才動卡片，整個標 @Transactional；
  新增 BoardApplicationServiceTest 覆蓋情境 1、3。
- D-02：CardSteps 補 fromStageId 斷言，BoardSteps.createTestCard 補 201 檢查。
- D-03：開了 OQ-T-03-be-card-01（uc-add-comment fail 缺口）、
  OQ-T-03-be-card-02（uc-add-card 等目的交會格驗證缺口），皆等級高、不阻塞。

Review 請先看：`BoardApplicationService.removeSwimlane`／`removeStage` 的新順序、
`Board.ensureValidDestinationStage`、兩則新 OQ 的引文是否對得上源頭文件。

Check：./gradlew clean build --no-daemon → BUILD SUCCESSFUL，73 個測試 0 失敗。
