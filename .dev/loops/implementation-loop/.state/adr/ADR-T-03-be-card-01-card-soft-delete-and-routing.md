## ADR-T-03-be-card-01：Card 採軟刪除，讀取一律排除 deleted=true；路由設計

- 狀態：Accepted
- 日期：2026-09-19
- 提出者：Dev（T-03-be-card）

### 背景（Context）

`uc-delete-card` post 要求「該操作被記錄為 `card` 的一筆活動紀錄，包含操作人與操作時間」。
`kanban-core` 的 `Card` 沒有 repository、刪除是呼叫端從外部集合移除物件本身（見
`design-kanban-basic.md` 5a 節「卡片刪除的操作人記錄」）；若 `kanban-spring` 對應實作為
物理刪除資料列，級聯刪除也會把這筆活動紀錄一起刪掉，違反 post。

### 決策（Decision）

`CardJpaEntity` 新增 `deleted: boolean` 欄位，`Card.delete(operatorId)` 只標記
`deleted=true` 並照常寫入 `activityLog`，不清除其他欄位、資料列不刪除。所有讀取路徑
（`CardLookupPort` 的 `CardLookupPortAdapter`、`CardRepository` 的
`findActiveBySwimlaneId`／`findActiveByStageId`、`CardApplicationService.loadCard`）
一律以 `deleted=false` 為條件，讓卡片對外呈現「已從看板移除」。

路由設計：建立卡片掛 `/api/boards/{boardId}/cards`（`uc-add-card` 的 `board: R` 需要先
確認 board 存在）；其餘動作（`GET`／`PATCH`／`move-swimlane`／`move-stage`／`comments`／
`DELETE`）直接掛 `/api/cards/{cardId}`，因為 `Card` 是獨立 Aggregate，不透過 board 巢狀。

### 考慮過的替代方案（Alternatives）

物理刪除＋由呼叫端在刪除前把 `card.delete(operatorId)` 產生的最後一筆 `ActivityRecord`
另外寫進 `Board` 的活動紀錄（design 文件原本暗示的方向）。放棄原因：`Board`／`Card` 是
兩個獨立 Aggregate，`BoardApplicationService`／`CardApplicationService` 互不持有對方的
写入方法（`Board.recordActivity` 是 private），要做到這件事得在 `Board` 開一個「代寫外部
活動紀錄」的公開方法，等於為了刪除這一種情境放寬 Aggregate 邊界，影響面比軟刪除更大。

### 後果（Consequences）

- 之後任何查詢 Card 的程式碼（T-04 的活動紀錄整合、T-06 widgets、T-07 workload、
  T-08 feature-cr-board 的唯讀 projection）都必須記得排除 `deleted=true` 的卡片，
  不能直接對 `cards` 資料表做 `SELECT *`；建議透過 `CardJpaRepository` 既有的
  `findByBoardIdAndDeletedFalse` 或新增同樣命名慣例（`...AndDeletedFalse`）的查詢方法。
- `cards` 資料表的列不會因刪除而減少，之後若有資料量考量（大量刪除卡片），需要另外規劃
  封存或清理機制（spec 的 Open Questions 已列封存為「本次不實作」的範圍）。
