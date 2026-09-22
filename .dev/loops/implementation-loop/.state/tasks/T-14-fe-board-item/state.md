# T-14-fe-board-item state

> 2026-09-22 Review 第 2 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

# T-14-fe-board-item state

> 2026-09-22 Review 第 2 輪 收尾，status＝`done`（附保留核准）。

D-01～D-04 逐條核對皆成立：留言顯示並排序 `comment.created-at`、兩個刪除對話框卡片數 0 也顯示、九條 ui 驗收條件各有對應測試（失敗情境同時斷言訊息與資料未變）、OQ 第三段引文已由 `OQ-T-14-fe-board-item-02` 逐字取代。Dev 補上的 StagePanel「重新命名」按鈕是 `uc-rename-stage` 能被觸發的必要修正，在範圍內。

Review 自己跑的驗證：`./gradlew test --no-daemon --rerun-tasks` BUILD SUCCESSFUL（9 task 實際執行）、`./gradlew build --no-daemon` BUILD SUCCESSFUL、`npx tsc -b` exit 0、`npx vitest run` 7 檔 80 測試全過、`git status --porcelain` 乾淨。`kanban-core` 無 Spring／JPA import；邊界只動 `kanban-frontend/src/board/**`、兩支 api、`BoardCanvasPage.tsx` 與第 1 輪已核准的後端端點，`.state/` 只動自己的任務目錄。

保留事項（不阻塞，接手者皆為人工）：OQ-T-14-fe-board-item-02（`uc-assign-card-owner-by-drag` 未實作，待人工在 A／B 兩案擇一）、OQ-T-14-fe-board-item-01（引文有誤，請人工標記為已被 02 取代）、OQ-T-12-fe-board-list-02（沿用 T-12 既有 OQ，本任務未接手）。
