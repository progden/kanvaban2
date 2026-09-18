# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-10-fe-shell：Review 第 1 輪**退回**，狀態改回 `doing`，等 Dev 處理 D-03、D-04（見 `tasks.md` 底部）。
- Review 重跑結果：`pnpm run test` 7 個測試全過、`pnpm run build`／`pnpm run lint` 通過、`./gradlew clean build` exit 0；diff 只在 `kanban-frontend/**`＋`.state/**`，範圍乾淨。
- 退回原因（都不需要改程式碼）：D-03，TopBar 顯示 `user.username`，但 ui 檔只寫『TopBar 顯示帳號名稱』，沒有指定欄位，要登記 OQ；D-04，交接紀錄說 OQ-IMPL-09 待處理，實際上已經由 ADR-001 解除，要追加更正。
- 下一步：Dev 補 OQ 與更正紀錄，轉 `review-pending` 後由 Review 第 2 輪審查（D-03 的 OQ 如果還沒定案，只能附保留核准）。
