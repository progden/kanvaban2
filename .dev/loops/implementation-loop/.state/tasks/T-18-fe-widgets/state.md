# T-18-fe-widgets state

> 2026-09-22 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

2026-09-22 Review 第 1 輪：退回，status＝`doing`。

自己跑過的驗證（kanban-frontend/）：
- npm run build（tsc -b && vite build）：成功
- npm run test（vitest run）：10 個測試檔、48 個測試全過
- npm run lint（oxlint）：僅 1 則既有 warning（T-13 的 itemComponentRegistry.tsx）

通過的項目：建置／測試綠燈；六個 uc-view-* 皆有整合測試斷言「開啟畫面即觸發」；
抽查 uc-view-duedate-reminder fail p1（輸入 0 → 輸入保留、訊息與後端一致、不再呼叫 API）成立；
任務邊界乾淨（只動 kanban-frontend/src 與自己的 .state 任務目錄）；
無設計稿的四個畫面沿用既有色票變數與 .field-input／.btn-sm，未發明風格，
產品畫面無規格註記外洩；需確認？欄皆「否」，實作也無二次確認。

退回原因（D-01）：OQ-T-18-fe-widgets-01 引 spec-canvas-layout.md 第 31 行時，
刪掉括號『不用 UI 層的 Screen ID，spec 不引用 ui』卻未標刪節——而本任務正是把
item.component 定成四個 ui 層 Screen ID，被刪的那句是唯一反向先例。
等級／阻塞／接手三欄判定本身正確，問題只在引文不逐字。

下一輪 Dev 要做：依 D-01 另開一則取代用的 OQ（完整逐字引文、補第三個命名選項），
並在 decision-log 正面回應那句先例；決定改名的話四個測試的 component 值同步、維持全綠。
