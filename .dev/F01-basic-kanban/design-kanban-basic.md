# 核心領域模型設計（io.progden.kanban.core.domain）

本文件記錄 `io.progden.kanban.core.domain` 套件的領域模型設計決策，對應 [`spec-kanban-basic.md`](./spec-kanban-basic.md) 描述的使用案例。

## 所屬 Module

本文件描述的類別皆實作於 **`kanban-core`** module（`kanban-core/src/main/java/io/progden/kanban/core/domain`）。

`kanban-core` 是純領域模型 module，不依賴 Spring／JPA／資料庫等框架（詳見「Domain 層定位」），因此可被 `kanban-spring`（application/web/persistence 層）依賴使用，但不反向依賴。`CardLookupPort` 等 domain 層定義的介面，其實作會放在 `kanban-spring` 的 infrastructure 層，透過依賴反轉注入回 `kanban-core` 的 `Board`。

## 技術限制

- Java 25 / Spring Boot 4.1.1 / Lombok / Spring Data JPA / PostgreSQL

## 設計決策

### 1. Domain 層定位

`core.domain` 套件內的類別為**純領域物件**，不依賴 JPA、Spring 或任何框架註解。持久化細節（`@Entity`、`@Id` 等）留給之後的 `infrastructure`/`persistence` 層透過 Entity/Mapper 轉換處理，domain 層只表達商業邏輯與不變條件。

### 2. Aggregate 邊界

只有兩個 Aggregate Root：

- **Board**：內部管理 `Swimlane`、`Stage` 兩種子物件，兩者不是獨立 Aggregate，只能透過 Board 的方法異動。
- **Card**：獨立的 Aggregate，透過 `boardId`／`swimlaneId`／`stageId`（UUID）參照所屬的 Board/Swimlane/Stage，不持有物件參照。

### 3. 識別碼型別

所有 Entity（Board、Swimlane、Stage、Card、Comment、StageTransition）皆使用 **UUID** 作為 ID，由應用程式端產生，不依賴資料庫自增序號。

### 4. Swimlane / Stage 排序

Swimlane 與 Stage 各自帶有**顯式 `order` 數值欄位**，排序由該欄位決定；拖曳調整順序、插入指定位置、新增時預設加到最後（`order = max + 1`），皆透過重新分配 `order` 完成。

### 5. Card 內部小型 Entity

`Comment`（留言）與 `StageTransition`（狀態異動紀錄）皆為 Card 內部的**獨立小型 Entity**，各自擁有自己的 UUID，方便未來單獨查詢或編輯；只能透過 Card 的方法新增（例如 `Card.addComment(...)`、卡片移動 Stage 時自動產生 `StageTransition`）。

### 5a. ActivityRecord（活動紀錄，CR-001）

依 CR-001：`Board` 與 `Card` 的會改變狀態的方法都要記錄「誰做的」。設計如下：

- `ActivityRecord { id, operatorId, action, occurredAt }`：與 `Comment`／`StageTransition` 同樣的模式，package-private 建構子，只能透過 aggregate root 的方法新增。
- `Board` 與 `Card` **各自**持有一份 `activityLog: List<ActivityRecord>`，不共用同一份清單——`Card` 是獨立 aggregate，不持有 `Board` 的參照，兩者只能各自記錄自己身上發生的事。
- `Board` 的寫入方法（`addSwimlane`／`renameSwimlane`／`moveSwimlaneBefore`／`removeSwimlane`／`addStage`／`renameStage`／`moveStageBefore`／`removeStage`）都新增 `operatorId` 為第一個參數，成功後寫入 `activityLog`。
- `Card` 的 `create`／`edit`／`moveToSwimlane` 同樣新增 `operatorId` 參數並寫入 `activityLog`；`moveToStage` 則是把 `operatorId` 直接加進 `StageTransition`（已有時間與前後 Stage，不需要額外一筆 `ActivityRecord`）。
- 為符合 checkstyle 參數數量限制（一般方法 ≤ 4 個），新增兩個公開 `record` 參數物件：`CardPlacement(swimlaneId, stageId)`（給 `Card.create` 用）、`CardDetails(description, assignee, dueDate, labels)`（給 `Card.edit` 用）。這兩個是單純資料傳遞用的參數物件，不是 aggregate 內部子物件，所以用一般 `public record`，不套用 `Comment`/`StageTransition` 的 package-private 建構子限制。
- **卡片刪除的操作人記錄**：`kanban-core` 沒有 repository，`Card` 物件被「刪除」時其實是被呼叫端從外部集合移除，物件本身不會自殺。設計為呼叫端在移除前先呼叫 `card.delete(operatorId)`，讓 Card 把這筆記錄寫進自己的 `activityLog`（此時物件還活著），再由呼叫端保存這份 log（測試中是 step 直接斷言；未來 `kanban-spring` 有 persistence／application 層時，要在真正刪除資料列之前把這份 `activityLog`——包含這筆刪除紀錄——寫進看板的活動紀錄儲存體）。**這是目前未完成的部分，留給 `kanban-spring` 補齊。**

### 6. 不變條件的錯誤回報方式

Domain 層以**自定義 Unchecked Exception** 表達不變條件違反（例如刪到只剩一個 Stage、名稱為空、Swimlane/Stage 仍有卡片），由 Board/Card 的方法內部判斷後拋出；應用層（application/web）負責攔截並轉換成對使用者的錯誤訊息或確認流程。

### 7. Board 與 Card 的關聯機制（跨 Aggregate 讀取）

Board 需要知道「目前有哪些卡片、分別在哪個 Swimlane/Stage」，才能判斷刪除 Swimlane/Stage 時是否需要走確認/轉移流程。設計如下：

```
CardSummary {                          // 唯讀投影，不含卡片完整欄位
  cardId: UUID
  swimlaneId: UUID
  stageId: UUID
}

CardLookupPort {                       // domain 層定義的介面（依賴反轉）
  List<CardSummary> findByBoardId(boardId: UUID)
}

Board {
  id, name, swimlanes, stages
  cardLookupPort: CardLookupPort       // 建構/還原 Board 時注入，不持久化
  cardSummaries: List<CardSummary>     // 內部快取，不持久化

  refreshCardSummaries()               // 呼叫 port 重新查詢，更新內部快取
  countCardsIn(swimlaneId): int
  countCardsIn(stageId): int

  ensureSwimlaneRemovable(swimlaneId)  // 不改狀態前置檢查：找不到拋 SWIMLANE_NOT_FOUND，只剩 1 個拋 MINIMUM_SWIMLANE，不檢查卡片數
  removeSwimlane(id)   // 至少保留 1 個；countCardsIn(id) > 0 時拋出 DomainException(SWIMLANE_HAS_CARDS, count)
  removeStage(id)      // 至少保留 1 個；countCardsIn(id) > 0 時拋出 DomainException(STAGE_HAS_CARDS, count)
}
```

運作方式：

- `CardLookupPort` 是 domain 層定義的介面，實際查詢邏輯在 infrastructure 層實作（查詢 Card 的 repository）。
- 應用層在載入 Board、且即將執行「可能受卡片影響」的操作前，主動呼叫 `board.refreshCardSummaries()`——這就是「通知 Board 重讀」的具體時機。
- `cardLookupPort` 與 `cardSummaries` 都**不持久化**，只在單次操作的記憶體物件生命週期內有效。
- `removeSwimlane`/`removeStage` 遇到還有卡片時**拋出例外並附上數量**，由應用層攔截後轉為「確認訊息」或「選擇轉移目的 Stage」的流程；卡片實際的轉移/刪除是操作 Card 聚合完成，完成後應用層再重新呼叫一次 Board 的刪除方法（此時 `countCardsIn` 應為 0，可順利完成）。
- `ensureSwimlaneRemovable` 供應用層在「確認後刪除」流程中，於實際刪卡片之前先確認 Swimlane 存在且不是最後一個；避免「先刪卡片、再呼叫 `removeSwimlane` 才發現只剩一個」留下卡片已刪但 Swimlane 還在的半套狀態。`removeSwimlane` 內部重用它，行為與例外訊息不變。

## 對應 spec 的 aggregate 讀寫標記

`spec-kanban-basic.md` 中每個 Scenario 已用註解標記會用到的 aggregate 與讀寫類型（`# Related aggregate: board: read, write` / `card: read` 等），對應本文件的設計：

- 純粹操作 Swimlane/Stage（新增、命名、排序）：只涉及 `board`。
- 刪除 Swimlane/Stage：`board`（read + write）與 `card`（read，count 判斷；有卡片時還需 `card` write 做轉移/刪除)。
- 新增/移動卡片：`card`（write）與 `board`（read，驗證 swimlaneId/stageId 存在)。
- 卡片本身欄位編輯、留言、刪除：只涉及 `card`。

## 實作狀態

已完成（`kanban-core/src/main/java/io/progden/kanban/core/domain`）：

- `Board`、`Swimlane`、`Stage` 類別與其不變條件方法
- `Card`、`Comment`、`StageTransition` 類別與其行為方法
- `CardLookupPort` 介面定義（domain 層）與對應的 `CardSummary`
- 單一具體例外類別 `DomainException`（`RuntimeException`，帶 `ErrorCode code` 欄位）取代原本每種規則各自一個子類別的設計；`ErrorCode` enum 列舉 10 種情境：`EMPTY_SWIMLANE_NAME`、`EMPTY_STAGE_NAME`、`EMPTY_CARD_TITLE`、`MINIMUM_SWIMLANE`、`MINIMUM_STAGE`、`SWIMLANE_HAS_CARDS`、`STAGE_HAS_CARDS`、`SWIMLANE_NOT_FOUND`、`STAGE_NOT_FOUND`、`CARD_NOT_FOUND`（目前尚未被任何流程拋出，保留給未來用到 `Card` id 查找的情境）。呼叫端需要依情境分支時比對 `getCode()`，不再用 `instanceof` 判斷具體型別。

對應的 Cucumber 驗收測試（`kanban-core/src/test/...`，見 [`README.md`](../../kanban-core/src/test/resources/features/README.md)）涵蓋 `spec-kanban-basic.md` 三個 Feature 共 22 個 Scenario，全數通過（`Tests run: 22, Failures: 0, Errors: 0`）。

CR-001（見 5a 節）完成後新增：`ActivityRecord`、`CardPlacement`、`CardDetails` 三個類別；`StageTransition` 補上 `operatorId` 欄位；`Board`／`Card` 的寫入方法都加了 `operatorId` 參數。既有 22 個 Scenario 維持全數通過，只是 Then 多驗證了操作人記錄。

CR-003 完成後新增：`Stage` 新增 `role` 欄位（`StageRole` enum：`NONE`/`START`/`DONE`，預設 `NONE`）；`Board.setStageRole(operatorId, stageId, role)` 負責指派角色，並在指派 `START`/`DONE` 前先把同一 Board 中原本持有該角色的 Stage 改回 `NONE`（同一時間至多各一個），完成後記一筆 `ActivityRecord`。權限（僅 Owner 可設定）由呼叫端先查 `BoardMembership` 後才呼叫，`kanban-core` 本身不驗證。

CR-004（處理完成，見 F04 spec-board-clock.md）第一步完成：`Board` 內部新增 package-private 值物件 `BoardClock`（`mode: REALTIME/PAUSED`、`offsetMillis`、`pausedAt`、`lastEventAt`），只由 `Board` 持有與呼叫，不對外公開、不是獨立 aggregate。`Board.now()` 回傳目前看板時間；`adjustClock(operatorId, newTime)`／`pauseClock(operatorId)`／`resumeClock(operatorId)` 三個方法對外提供時鐘操作，皆呼叫新的 `recordClockActivity` 寫入活動紀錄——這個路徑刻意**不**經過單調性檢查，因為調整/暫停/恢復時鐘本身允許把時間調回過去（用於檢視歷史），只是每次時鐘變更都會記錄一筆活動。既有的 `recordActivity`（`addSwimlane`／`addStage`／`setStageRole` 等既有寫入方法共用的私有方法）改為透過 `clock.recordEvent(now())` 取得 `occurredAt`，這裡才是真正的單調性守門：若計算出的看板時間早於 `lastEventAt`，丟出 `DomainException(BOARD_CLOCK_BEHIND_LAST_EVENT, "看板時間早於最後一筆事件（HH:mm），無法建立新事件")`，訊息格式對齊 spec 逐字。權限檢查（僅 Owner 可調整時鐘）依既有慣例由呼叫端先查 `BoardMembership` 再呼叫，`kanban-core` 本身不驗證，測試中以 `BoardClockSteps` 的 `ensureOwner` 模擬。CR-004 第二步完成：`Card` 的所有寫入方法（`create`／`edit`／`assignTo`／`moveToSwimlane`／`moveToStage`／`delete`／`addComment`）不再直接呼叫 `Instant.now()`，改為接收呼叫端傳入的 `OperationContext(operatorId, now)` 參數物件（新增此 record 是為了讓 `Card.create` 的參數數量符合 checkstyle `ParameterNumber`（≤4）限制，比照既有 `CardPlacement`/`CardDetails` 的做法）；`Board` 新增公開方法 `newEventTime()`，內部呼叫 `clock.recordEvent(now())`，讓 `Card` 這類獨立 aggregate 在寫入事件前也能取得經過單調性檢查、且會更新 `lastEventAt` 的看板時間（`now()` 本身維持唯讀、不做檢查、不更新 `lastEventAt`，供顯示或時鐘管理動作使用）。`board-clock.feature` 原本用「新增 Swimlane」代打驗證「不可寫入新事件」的情境，已改為用「建立卡片」驗證，呼應 spec 原始情境用詞。`kanban-core` 端 CR-004 範圍已全部完成並測試全綠；`kanban-spring` 目前尚無 application 層程式碼呼叫 `kanban-core` 寫入方法，故無呼叫點可供串接 `Board.now()`/`newEventTime()`——此事項不阻塞 CR-004 結案，留待 `kanban-spring` 實際開發 application 層時處理。

F03（見 `spec-kanban-widgets.md`）籌備 `CardTimeline` 投影時發現：`Card` 沒有任何欄位或事件記錄建立時間，`activityLog` 只有自由文字描述（無法穩定辨識「這是建立事件」），不適合作為 `createdAt` 的資料來源。屬於既有規格實作補齊（非行為變更、不需開 CR）：`Card` 新增 `createdAt: Instant` 欄位，`create(...)` 時由 `context.now()` 寫入、之後不可變；沿用既有 `@Getter` 產生 `getCreatedAt()`。對應的 domain 單元測試 `CardTest.should_recordCreatedAt_when_creatingCard` 是本專案第一個非 Cucumber 的 `kanban-core` 單元測試，因此同步在 `checkstyle-suppressions.xml` 補上 `Test\.java$` 的 `MethodNameCheck` 排除（domain 單元測試採 `should_預期_when_情境` 命名，此前只有 Cucumber step 有對應排除）。

### 與初版設計的落差

- `Board.refreshCardSummaries()` 目前由呼叫端（測試中為 step definition，代表未來的 application 層）在「刪除 Swimlane/Stage 前」與「新增/移動卡片後」主動呼叫，`Board` 本身不會自動同步，維持設計中「不持久化、單次操作生命週期有效」的原則。
- `removeSwimlane`/`removeStage` 對於「確認後刪除」採「應用層刪除卡片 → 重新呼叫一次刪除方法（此時 count 為 0）」的模式，與設計文件所述一致；測試中以 `ScenarioContext` 模擬這個應用層協調角色。

### 尚未涵蓋（依 spec 的 Open Questions，本次不實作）

顏色標記、Checklist/附件、封存（Archive）、即時同步、WIP limit。
