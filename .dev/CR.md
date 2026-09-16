# CR 總表

| 編號 | 標題 | 類型 | 提出人 | 提出日期 | 影響模組 | 影響 ID | 狀態 | 完成日期 | 明細 |
|------|------|------|--------|----------|----------|---------|------|----------|------|
| CR-001 | Board/Card 補上操作人記錄 | 變更 | SA | 2026-09-12 | spec-kanban-basic、spec-user-membership | | 處理完成 | 2026-09-12 | |
| CR-002 | 卡片負責人改為多選 | 變更 | SA | 2026-09-12 | spec-kanban-basic、spec-user-membership | | 處理完成 | 2026-09-12 | |
| CR-003 | Stage 新增角色標記（Start／Done） | 新增 | SA | 2026-09-13 | spec-kanban-basic | | 處理完成 | 2026-09-13 | |
| CR-004 | 事件時間改用 Board Clock | 變更 | SA | 2026-09-13 | spec-kanban-basic、spec-user-membership、spec-board-clock | | 處理完成 | 2026-09-13 | |
| CR-005 | 規格格式遷移至 usecase 區塊 | 變更 | SA | 2026-09-16 | spec-kanban-basic、spec-user-membership、spec-kanban-widgets、spec-board-clock、spec-workload、spec-feature-cr-board | | 修改規格 | | |

### CR-001：Board/Card 補上操作人記錄
- 背景：Swimlane／Stage／Card 會改變狀態的情境，原本沒有記錄是誰做的操作，F02 要做活動紀錄需要這份資料。
- 變更內容：Swimlane、Stage、Card 會改變狀態的既有情境，補上操作人記錄，供活動紀錄使用（見 `spec-user-membership.md`）。
- 驗收標準：Swimlane／Stage／Card 的既有寫入情境（新增、命名、排序、刪除、移動等）都記錄操作人。

### CR-002：卡片負責人改為多選
- 背景：F02 引入 BoardMembership 後，卡片負責人需要參照看板成員並支援多人。
- 變更內容：原本「編輯卡片詳細內容」情境含單一負責人欄位 → 移除該欄位，負責人改為多選、參照看板成員，改由 F02「卡片負責人指派」情境處理。
- 驗收標準：「編輯卡片詳細內容」情境不再包含負責人欄位；負責人指派／移除改由 F02 新增情境處理，且參照的對象必須是該看板成員。

### CR-003：Stage 新增角色標記（Start／Done）
- 背景：F03 標準圖表要計算 Cycle/Lead Time，需要知道哪個 Stage 代表「開始」、哪個代表「完成」。
- 變更內容：新增 Stage 角色標記（Start／Done），供 F03 標準圖表計算 Cycle/Lead Time 使用。
- 驗收標準：Stage 可設定角色為 Start、Done 或無角色；F03 的 Cycle/Lead Time 計算以此為依據。

### CR-004：事件時間改用 Board Clock
- 背景：F04 引入每個 Board 自己的時鐘後，Board/Card 事件時間需要改用 Board Clock，而非系統時間，才能支援調整／暫停／恢復。
- 變更內容：名詞定義補上「操作時間（occurredAt）」，明訂 Board/Card 事件時間一律取自 Board Clock（見 `spec-board-clock.md`）；User／BoardMembership 事件維持系統時間。既有 Scenario 文字不需修改，故不掛 Scenario 層級 tag。
- 驗收標準：`kanban-core` 的 `Board`／`Card` 事件時間全面改用 Board Clock，不再直接取用系統時間；User／BoardMembership 事件不受影響。

### CR-005：規格格式遷移至 usecase 區塊
- 背景：`.dev/conventions/` 已依 `spec-migration-prompt.md` 改為腳本可解析格式，但 F01～F06 六份既有規格尚未遷移，`spec-check` 目前報大量 error。
- 變更內容：六份 spec 依新格式補上狀態行、實體／欄位／關係表、角色表、每個 Feature 的 usecase 區塊、tag（`@uc-`／`@fail-`）、Aggregate 註解與變更紀錄格式；不改變既有 Scenario 的行為。
- 驗收標準：F01～F06 六份 `spec-*.md` 全部通過 `./scripts/spec-check`（0 error）；`cr-check --base <baseline> --cr CR-005` 通過。
