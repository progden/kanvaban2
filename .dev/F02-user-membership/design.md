# 核心領域模型設計（User／BoardMembership／Card 負責人）

本文件記錄 `io.progden.kanban.core.domain` 套件中，F02 新增／變更部分的設計決策，對應 [`spec-user-membership.md`](./spec-user-membership.md) 描述的使用案例。技術限制、識別碼型別、不變條件回報方式等共用原則見 [`../F01-basic-kanban/design.md`](../F01-basic-kanban/design.md)，本文件只記錄 F02 新增的部分。

## 設計決策

### 1. Aggregate 邊界（新增兩個 Aggregate Root）

- **User**：獨立 Aggregate，`username`（帳號 ID，唯一）、`displayName`（顯示名字，可重複）、`password`（可留白，上限 40 字）。
- **BoardMembership**：獨立 Aggregate，描述某個 `userId`（username）對某個 `boardId` 的角色（`BoardRole`：`OWNER`／`MEMBER`）。與 `Board`／`Card` 一樣，只透過 id／username 參照，不持有物件參照。

至此 `kanban-core` 有四個 Aggregate Root：`Board`、`Card`、`User`、`BoardMembership`。CLAUDE.md 的「領域模型設計原則」已同步更新。

### 2. 為什麼 BoardMembership 是獨立 Aggregate，不是 Board 的子物件

`Board` 已有 `Swimlane`／`Stage` 兩種子物件（package-private 建構子，只能透過 Board 的方法異動）。`BoardMembership` 沒有採用同樣模式，原因：

- 需要「以使用者查詢」的方向（我的看板列表：某 User 在哪些 Board 有身分），這跟 `Swimlane`/`Stage` 只會「以 Board 查詢」的存取模式不同。
- `spec-user-membership.md` 開頭明確定義 `User`、`BoardMembership` 為 Aggregate Root（見文件簡介）。

### 3. 使用者名稱唯一性、密碼驗證——為什麼交給呼叫端

`kanban-core` 沒有 repository，`User` 物件本身無法知道系統裡還有哪些其他 User。`User.create(username, displayName, password, usernameTaken)` 讓呼叫端（測試中是 step，未來是 application 層查 repository）先查好「這個帳號是否已存在」，用一個 `boolean` 傳進來；`User` 仍然是拋出 `DomainException` 的一方，維持「不變條件由 aggregate 方法內部判斷後拋出」的慣例，只是判斷所需的外部資料由呼叫端準備。

登入／登出（查帳號、比對密碼、記住目前使用者）目前整個放在 Cucumber step（`UserSteps`），`User` 只提供 `authenticate(password)` 這個單一物件可以自己判斷的行為；session／目前登入者的概念留給未來 `kanban-spring` 的 application 層。

### 4. Board／BoardMembership 的權限檢查放在哪裡

`spec-user-membership.md`「Board 權限管理」Feature 要求「只有 Owner 能邀請成員／改角色／動看板結構／刪除看板」。這些檢查**沒有**加進 `Board` 或 `BoardMembership` 的方法內部，而是放在呼叫端（`BoardMembershipSteps`，模擬未來 application 層）：先查 `BoardMembership` 判斷角色，通過才呼叫 `Board`／`BoardMembership` 的方法。

理由：`Board` 本身不需要知道「誰是誰、角色是什麼」，維持 `kanban-core` 既有的「跨 aggregate 協調交給呼叫端」設計慣例（同 `CardLookupPort` 的精神——`Board` 不直接依賴 `Card`，也不直接依賴 `BoardMembership`）。

### 5. 「至少保留一位 Owner」的不變條件

`BoardMembership` 本身不知道同一個 Board 還有哪些其他 `BoardMembership`（跟 `User` 不知道其他 User 一樣）。做法是靜態方法 `BoardMembership.ensureAnotherOwnerRemains(List<BoardMembership> boardMemberships, UUID excludingMembershipId)`：呼叫端把同一個 Board 目前所有的 membership 傳進來，這個方法負責判斷「排除掉要移除的這一筆之後，還有沒有其他 Owner」，沒有就拋出 `DomainException(MINIMUM_BOARD_OWNER, ...)`。這是一個跨實例（而非跨 aggregate 型別）的不變條件，用一個附掛在 `BoardMembership` 類別上的靜態方法表達，而不是另開一個 domain service 類別。

### 6. Card 負責人：從自由文字改為多選、參照看板成員（CR-002）

原本 `Card` 用一個自由文字 `assignee` 欄位（F01 上線時的設計）。F02 要求負責人改成「從看板成員中選、可複選」，這是既有行為的變更，已依 cr-convention 開 CR-002。變更後：

- `Card.assignee`（`String`）移除，改為 `assigneeIds`（`List<String>`，存 username）。
- 新增 `Card.assignTo(operatorId, assigneeIds)`，取代原本透過 `CardDetails`／`edit(...)` 設定負責人的方式；`CardDetails` 移除 `assignee` 欄位。
- 「負責人只能選看板成員」「選單只列出這個看板的成員」的驗證，同第 4 點的理由，放在呼叫端（`CardAssignmentSteps`）比對 `BoardMembership`，`Card` 本身不驗證 `assigneeIds` 是否為真正的看板成員。
- 移除仍是卡片負責人的成員時，`BoardMembershipSteps` 會先計算「該成員身上還掛著幾張卡片的負責人」，需要確認的話丟一個新的 `ErrorCode.CARD_ASSIGNEE_CONFIRMATION_NEEDED`；確認後才呼叫每張卡片的 `assignTo(...)` 把該成員從負責人清單移除，最後才真正移除 `BoardMembership`。

### 7. 活動紀錄（ActivityRecord，CR-001／F02 共用設計）

`Board`、`Card` 各自有一份 `activityLog: List<ActivityRecord>`，設計細節見 [`../F01-basic-kanban/design.md`](../F01-basic-kanban/design.md) 5a 節（CR-001 是因為 F02 需要活動紀錄，才回頭在 F01 補上操作人）。F02 這邊新增的 `BoardMembership`／`Card.assignTo` 事件，直接沿用同一套模式：

- `Board.addSwimlane/renameSwimlane/.../removeStage` 補上操作人記錄（CR-001 已完成）。
- `Card.create/edit/moveToSwimlane/moveToStage/delete/assignTo` 都會寫進 `Card` 自己的 `activityLog`（`moveToStage` 記在 `StageTransition` 裡，不是額外一筆 `ActivityRecord`，同 CR-001 的決定）。
- `BoardMembership` 目前**沒有**自己的 activityLog——邀請／變更角色／移除成員這幾個動作的活動紀錄，目前記錄在測試 step 對 `Board`（`我對這個看板的角色應該是...`)/`Card` 的斷言裡，尚未有一個地方統一收集「這個 Board 的所有活動」（`spec-user-membership.md`「檢視看板活動紀錄」Feature 描述的統一活動列表）。

### 8. 尚未實作：「檢視看板活動紀錄」統一活動列表

`spec-user-membership.md` 的「檢視看板活動紀錄」Feature 要求把 Board／Swimlane／Stage／Card／BoardMembership 的活動紀錄合併成一份、依時間排序顯示。`kanban-core` 目前每個 aggregate 只管自己的 `activityLog`，沒有一個地方可以把 `Board.activityLog`、每張 `Card.activityLog`、以及 `BoardMembership` 的邀請/角色/移除事件合併起來——這件事本質上是跨 aggregate 的查詢／投影，跟 `CardLookupPort` 解決「Board 需要知道卡片分佈」是同一類問題，合理的作法是等 `kanban-spring` 有 application／persistence 層時，做一個查詢服務（或投影表）把各 aggregate 寫下的事件收斂成一份 feed，而不是在 `kanban-core` 裡讓 `Board`／`Card` 互相持有對方的活動紀錄。**這是目前 F02 唯一還沒實作的 Feature，留給 `kanban-spring`。**

## 對應 spec 的 aggregate 讀寫標記

`spec-user-membership.md` 每個 Scenario 的 Aggregate 標記（`user`／`board`／`boardMembership`／`card`）對應本文件的四個 Aggregate Root；讀寫判定原則同 F01 design.md。

## 實作狀態

已完成（`kanban-core/src/main/java/io/progden/kanban/core/domain`）：

- `User`：`create`（含帳號 ID 唯一性、密碼長度驗證）、`authenticate`。
- `BoardRole`、`BoardMembership`：`invite`、`changeRole`、`ensureAnotherOwnerRemains`。
- `Board`：新增 `createdBy` 欄位。
- `Card`：`assignee`（自由文字）移除，改為 `assigneeIds`（`List<String>`）＋ `assignTo(...)`（CR-002）。
- `ErrorCode` 新增：`USERNAME_ALREADY_EXISTS`、`PASSWORD_TOO_LONG`、`ALREADY_BOARD_MEMBER`、`MINIMUM_BOARD_OWNER`、`INVALID_CREDENTIALS`、`FORBIDDEN`、`BOARD_NOT_FOUND`、`CARD_ASSIGNEE_CONFIRMATION_NEEDED`。

對應的 Cucumber 驗收測試涵蓋：建立使用者帳號與登入登出（10 個 Scenario）、Board 建立與成員邀請／權限管理／存取權限（14 個 Scenario，含跨 Card 的兩個確認情境）、卡片負責人指派（5 個 Scenario），共 29 個 Scenario，加上 F01 原有的 22 + 1（CR-002 新版）個，`kanban-core` 全模組共 52 個 Scenario 全數通過。

### 尚未涵蓋

- 「檢視看板活動紀錄」統一活動列表（見第 8 點）。
- Board 刪除的操作人記錄（CR-001 決議：Board 刪除目前走封存、不在 F01 規格範圍內，暫緩）。
- 密碼雜湊／真正的認證安全機制（spec 明確排除，本次疊代範圍不含）。
