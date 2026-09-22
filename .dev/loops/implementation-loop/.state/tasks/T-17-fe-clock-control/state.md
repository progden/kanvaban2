# T-17-fe-clock-control state

> 2026-09-22 Review 第 1 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪：**附保留核准**，status＝`done`，可合併回 `loop/implementation`。

本輪自己跑的驗證：`npm run build` 成功（tsc + vite）、`npx vitest run` 7 檔 49 測試全過、`npm run lint` 只剩 T-13 既有的 1 個 fast-refresh warning。後端未改動，未跑 Gradle。

spec／ui 對應：`ui-board-clock.md` 五條驗收條件逐條對上測試並核對過原始碼行為；抽查 `uc-adjust-board-clock` `@fail-p1`，403 時顯示訊息、看板時間與已輸入值都不變，「資料不變」成立。API 形狀與後端 `BoardController` 的 clock 三個端點一致。

邊界：13 檔全在 `kanban-frontend/src/**`、`.state/tasks/T-17-fe-clock-control/**` 與新增的 ADR，沒有動 `.state/tasks.md`、別的任務目錄、conventions、scripts、spec／ui 本體。

保留事項（皆不阻塞）：
1. `OQ-T-17-fe-clock-control-01` canvas item 識別碼／註冊位置慣例＋合併衝突風險 → 接手：人工（隨 OQ-49 整合 CR）。
2. `OQ-T-17-fe-clock-control-02`（本輪新開）ui 檔「類型：對話框」＋操作表「關閉」列 與「以 F07 item 形式顯示」兩處矛盾，現行實作無「關閉」操作 → 接手：人工（走 CR 改 ui 或另開修正任務）。
3. `ADR-T-17-fe-clock-control-01` 有一個簡體字「会」→ 接手：人工（既有 ADR 僅人工可改）。

下一步：驅動腳本合併本分支；人工處理上述兩則 OQ。
