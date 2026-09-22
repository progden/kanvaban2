# T-16-fe-activity-log state

> 2026-09-22 Review 第 2 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

Review 第 2 輪：**附保留核准**（`done`），可合併回 `loop/implementation`。

自行重跑（`kanban-frontend/`）：`pnpm exec vitest run` 47/47 全過、`pnpm run build` 成功（53 modules，802ms）、`pnpm run lint` 僅 `itemComponentRegistry.tsx:21:10` 既有警告。工作區乾淨。

D-01 確認修好：`ActivityLogItem.tsx:46` 改用 `form-error activity-log__error`，`.activity-log__error` 收斂為 `margin: 0; padding: 12px;`，顏色全部繼承 `src/index.css:148` 的 `.form-error`，與專案其餘 10 處錯誤呈現一致。

spec 對應複核無缺漏（`uc-view-board-activity-log` 的 `fail` 為空，無 `@fail-pN` 可抽，改以兩條 post 複核，皆由 `ActivityLogItem.test.tsx` 覆蓋）；未觸及 Java 檔，`kanban-core` 純度不受影響；diff 12 檔全落在 T-16 範圍與自己的 `.state/tasks/T-16-fe-activity-log/`。

保留事項（兩則皆不阻塞，非「覆蓋」等級）：
- OQ-T-16-fe-activity-log-01（接手：無）：`ui-user-membership.md` `s-activity-log` 仍「未討論」，版面細節係本任務推論決定，待人工決定是否另開 ui-authoring 任務回填。
- OQ-T-16-fe-activity-log-02（接手：人工）：`spec-user-membership.md:855`『本情境目前尚未實作。』合併後將落後於實作，是否走 CR 待人工決定。
