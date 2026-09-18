# T-03-be-card 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Dev 第 1 輪：實作 Card Aggregate 與 Swimlane/Stage 刪除協調流程

### 第 1 輪（Dev）決策紀錄

#### 範圍涵蓋

- `spec-kanban-basic.md`「Card（卡片）編輯」Feature：`uc-add-card`／`uc-edit-card`／
  `uc-move-card-swimlane`／`uc-move-card-stage`／`uc-add-comment`／`uc-delete-card`
  共 6 個 usecase、8 個 Scenario（新增卡片、標題留空失敗、編輯內容、跨 Swimlane 移動、
  跨 Stage 移動、新增留言、刪除需確認、取消刪除），全數對應 `card-editing.feature` +
  `CardSteps.java`，全綠。
- `kanban-core`：`Card`／`Comment`／`StageTransition`（含 Snapshot）、`CardPlacement`／
  `CardDetails` 參數物件、`CardRepository` port；`CardTest` 涵蓋建立／標題留空／編輯／
  跨 Swimlane／跨 Stage（含 StageTransition）／留言／留言留空／刪除（軟刪除＋活動紀錄）
  8 個單元測試。
- OQ-IMPL-17（人工已解除，選項 A 併入本任務）：`BoardApplicationService.removeSwimlane`／
  `removeStage` 補上協調流程（`confirmed`／`destinationStageId`），`BoardController`
  兩個刪除端點加上對應 query 參數；`BoardSteps.java` 的四個替身步驟
  （`whenConfirmDelete`／`thenSwimlaneAndCardsRemoved`／`whenChooseDestinationStage`／
  `thenCardsMovedTo`）改打正式端點，從 `CardJpaRepository` 驗證卡片真的被刪除／
  `card.stage` 真的更新；`CardLookupPort` 改用 `CardLookupPortAdapter`（查真正的
  `CardJpaRepository`）取代 `NoOpCardLookupPort`；`FakeCardLookupPort` 已刪除。

#### 規格沒寫清楚、依推論處理的地方（低風險技術決定，見 decision-log 慣例，未開 OQ）

1. **Card 刪除採軟刪除**（`deleted: boolean` 欄位）：物理刪除會連帶刪掉
   `uc-delete-card` post 要求的活動紀錄，改標記 `deleted=true`，所有讀取路徑排除
   `deleted=true`。已寫成 ADR-T-03-be-card-01（跨任務查詢都要記得排除）。
2. **CardDetails 不含負責人欄位**：CR-002 已明訂負責人改由 F02
   `uc-member-add-card` 處理，`design-kanban-basic.md` 提到的 `assignee` 欄位是舊版
   （CR-002 之前）設計殘留，本任務依 spec 現況（CR-002 之後）不實作。
3. **路由設計**：建立卡片掛 `/api/boards/{boardId}/cards`（需要 `board: R`）；其餘動作
   （`edit`／`move-swimlane`／`move-stage`／`comments`／`delete`／`get`）直接掛
   `/api/cards/{cardId}`，因為 Card 是獨立 Aggregate。已寫成 ADR-T-03-be-card-01。
4. **`comment.content` 非空驗證**：`uc-add-comment` 的 `fail` 區塊雖為空，但名詞定義
   `comment.content` 欄位表已標「非空」，`Comment.create` 仍依欄位限制驗證並拋
   `EMPTY_COMMENT_CONTENT`（400），對齊既有 Swimlane/Stage/Card 標題的驗證慣例
   （ADR-001 的 fail→HTTP 對照表）。
5. **uc-add-card 不驗證 swimlaneId/stageId 是否真的屬於該 board**：spec 的 `pre` 只要求
   `card.title` 非空，`crud` 的 `board: R` 只用來確認 board 存在，不要求驗證交會格合法性，
   本服務未加這層檢查。
6. **刪除 Swimlane/Stage 時卡片的處理，重用既有 Card 方法**：Swimlane 級聯刪除呼叫
   `Card.delete(operatorId)`（逐張軟刪除＋各自留一筆活動紀錄）；Stage 級聯轉移呼叫
   `Card.moveToStage(operatorId, destinationStageId)`（逐張建立 StageTransition），
   沒有另外設計批次專用方法，行為與逐張手動操作一致。
7. **跨 Feature 共用逐字 Gherkin 步驟文字**（「我確認新增」「我確認刪除」「該操作應該被
   記錄為一筆活動紀錄，包含操作人與操作時間」同時出現在 Swimlane 與 Card 兩個
   Feature）：新增 `CrossAggregateState` 共用 bean 分流，`BoardSteps` 對 `CardSteps`
   的欄位注入需要 `@Lazy` 打破循環初始化。已寫成 ADR-T-03-be-card-02（後續任務改
   `BoardSteps.java` 前應先讀）。

#### 有意識跳過／延後的部分

- `r-board-owner`／`r-user` 權限檢查（是否真的是看板成員）：依既有慣例（OQ-IMPL-15）
  留給 T-04（`BoardMembership`）補上，本任務 Card 的所有 usecase `roles` 皆為
  `r-user`，只要求已登入。
- `card.assignees`（F02 CR-002 負責人多選）：不在本任務範圍，`Card` 未加這個欄位，
  留給對應的 F02 任務新增。

#### Check（實際跑的指令與結果）

```
./gradlew :kanban-core:test --no-daemon        → BUILD SUCCESSFUL（8 個 CardTest 皆過，含既有 BoardTest/UserTest/NoSpringDependencyTest）
./gradlew :kanban-spring:test --no-daemon      → BUILD SUCCESSFUL，35 個 Cucumber Scenario 全綠（含新增 8 個 Card Scenario）
./gradlew build --no-daemon                    → BUILD SUCCESSFUL（kanban-core + kanban-spring 全部 check 通過）
```

## 2026-09-19 Dev 第 2 輪：修正 D-01/D-02/D-03

### 第 2 輪（Dev）決策紀錄：修正 Review 第 1 輪退回的 D-01／D-02／D-03

#### D-01：Swimlane/Stage 刪除協調流程非原子、目的 Stage 未驗證

- `BoardApplicationService.removeSwimlane`／`removeStage` 改成先呼叫
  `Board.ensureSwimlaneRemovable`／`ensureStageRemovable`（既有方法，驗證
  swimlaneId／stageId 屬於這個 board、且未低於數量下限）通過才動卡片；`removeStage`
  另外新增 `Board.ensureValidDestinationStage(sourceStageId, destinationStageId)`，
  驗證目的 Stage 不等於來源、且真的屬於這個 board（沿用 `findStage` 的
  `STAGE_NOT_FOUND`），不合法時新增 `ErrorCode.INVALID_DESTINATION_STAGE`（400，
  依 ADR-001「輸入內容不合法」分類）。
- 兩個方法整個標 `@Transactional`（`kanban-spring` 已有 `spring-boot-starter-data-jpa`
  帶入 `spring-tx`，`DomainException` 是 `RuntimeException`，符合 Spring 預設回滾規則），
  檢查通過後若卡片異動或最終刪除任一步失敗，整段（含已異動的卡片）一起回滾。
- 補測試：新增 `BoardApplicationServiceTest`（Spring 整合測試，不動 feature 檔），覆蓋
  D-01 描述的情境 1（只剩 1 個 Swimlane 時確認刪除仍拒絕，卡片不被刪除）與情境 3
  （目的 Stage 屬於別的 board 時拒絕，卡片的 `stage`／`deleted`／StageTransition 都不變）。

#### D-02：Cucumber 斷言補強

- `CardSteps.whenDragCardToStage` 移動前先記下 `stageIdBeforeMove`，
  `thenStageTransitionRecorded` 新增斷言 `transition.getFromStageId()` 等於移動前的
  Stage，不再只驗 `toStageId`。
- `BoardSteps.createTestCard` 補上 201 狀態斷言（失敗時帶出回應內容），避免前置卡片
  建立失敗時後續刪除情境在「0 張卡片」下假綠燈。

#### D-03：規格未定義失敗路徑補開 OQ

- 依 fixes.md 指示，把交接摘要原本列為「低風險技術決定」但實際屬於高風險（規格沒有
  fail 定義，Dev 自行決定行為）的兩點，逐字引用 spec／ui 原文後開成 OQ：
  - `OQ-T-03-be-card-01`：`comment.content` 欄位表寫「非空」但 `uc-add-comment` 的
    `fail: {}`、ui 「新增留言」操作表寫「不適用（`uc-add-comment` 無 fail 定義）」，
    問是否要正式補 fail 分支；程式碼目前依欄位限制拒絕空白留言（`EMPTY_COMMENT_CONTENT`）
    的做法先維持不變。
  - `OQ-T-03-be-card-02`：`card.swimlane`／`card.stage` 欄位表寫「ref swimlane」
    「ref stage」「建立時必填」，但 `uc-add-card`／`uc-move-card-swimlane`／
    `uc-move-card-stage` 都沒有把「目的 swimlane/stage 不存在或不屬於該 board」定義成
    fail，問是否要補；程式碼目前維持不驗證（沿用第 1 輪的決定，未新增檢查），OQ 內已
    註明現況。
  - 兩則都是等級「高」、`--blocking no`，owner 填「人工」（要走 CR 決定 spec／ui 是否
    補 fail，不是某個既有任務可以接手的範圍）。

#### Check（實際跑的指令與結果）

```
./gradlew compileJava compileTestJava --no-daemon   → BUILD SUCCESSFUL（先確認新增程式碼可編譯）
./gradlew clean build --no-daemon                    → BUILD SUCCESSFUL（2m21s）
test-results 彙總：73 個測試，0 失敗、0 錯誤、0 略過（比第 1 輪多 2 個，
  即本輪新增的 BoardApplicationServiceTest 兩個情境）
```

#### 待確認事項

- `OQ-T-03-be-card-01`（`uc-add-comment` 空白留言的 fail 定義缺口）
- `OQ-T-03-be-card-02`（`uc-add-card`／`uc-move-card-swimlane`／`uc-move-card-stage`
  目的 swimlane/stage 合法性驗證的 fail 定義缺口）
