# T-07-be-workload 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：F05 Workload 唯讀 projection 實作

### 這輪做了什麼

實作 spec-workload.md 唯讀 projection：`kanban-spring` 新增 `io.progden.kanban.query.workload`
（`WorkloadCalculator` 純函式、`WorkloadQueryService` 跨 aggregate 讀取投影、`CardWorkloadSource`／
`MemberWorkloadView`／`WorkloadView`），`WorkloadController`（`GET /api/boards/{boardId}/workload`）與
`WorkloadResponse`。`uc-drag-assign-card-owner` 沿用既有 `CardController` 的
`POST /api/cards/{cardId}/assignees/drag` 端點，不重複實作寫入端（spec-workload.md「Use Case 定義」下方
已註明與 `spec-user-membership.md` 的 `uc-assign-card-owner-by-drag` 是同一操作）。

### spec 對應

- `uc-view-workload`：4 個 Scenario（檢視單一負責人的工作量／多位負責人的卡片各算一張／檢視未指派負責人
  的卡片數量／已完成的卡片不計入工作量）全部實作，對應 `workload.feature` 4 個 Cucumber Scenario。
- `uc-drag-assign-card-owner`：2 個 Scenario 沿用既有寫入端點，`workload.feature` 補上對應的 2 個
  Cucumber Scenario（步驟文字與 `CardAssignmentSteps` 共用）。
- 6 個 Scenario 全部有測試涵蓋，無跳過或延後項目。`WorkloadCalculator` 另有 4 個單元測試涵蓋 post
  p1～p4（分組統計、多負責人各算一次、未指派、Done 排除）。

### 判斷與理由

1. **Workload API 只回傳成員計數，不含卡片明細清單**：spec-workload.md「其他名詞」明確定義「Workload：
   某成員名下 Active Card 的數量」，是數字不是清單；Scenario 的「工作量應該包含卡片 X」在只有一張卡片的
   情境下等價於「該卡片使計數變成 1」，測試以計數驗證。低風險技術決定。
2. **回應包含所有 board-membership 成員（即使 workload 為 0）**：post p1「依 board-membership 成員分組
   統計」，即使某成員目前 Workload 為 0 也仍是「分組」的一部分，比只回傳有工作量的成員更貼近字面。低風險。
3. **`CardAssignmentSteps.setAssignees` 補上卡片不存在時就地建立**：`workload.feature` 的拖曳 Scenario
   （`Given 卡片 "B" 目前的負責人只有 "志明"`）與 `card-assignment.feature` 共用同一句步驟文字，但前者
   Background 沒有先跑「看板中存在一張卡片」建立卡片。因為 Gherkin 步驟必須逐字複製 spec 原文、不能另外
   插入建立卡片的步驟，改為讓共用的 step definition 在卡片第一次被提及時就地建立（不影響
   `card-assignment.feature` 既有行為，那邊卡片已存在，此改動只是補一個 no-op 以外的分支）。低風險，屬
   測試共用 glue 的技術決定，不影響 production 行為。

### 待確認事項

無新增 OQ；沿用 spec-workload.md 既有「Open Questions（已定案）」（加權 Workload、拖曳到已負責人的卡片
靜默忽略）皆已在 spec 定案，不需重新提出。

### Check

- `./gradlew :kanban-spring:compileTestJava --no-daemon`：成功。
- `./gradlew :kanban-spring:test --no-daemon`：BUILD SUCCESSFUL（含 6 個新增 Cucumber Scenario 與 4 個
  `WorkloadCalculatorTest` 單元測試，全數通過）。
- `./gradlew build --no-daemon`：BUILD SUCCESSFUL（`kanban-core`／`kanban-spring` 全部測試）。
