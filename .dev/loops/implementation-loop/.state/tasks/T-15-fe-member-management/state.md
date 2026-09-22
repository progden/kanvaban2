# T-15-fe-member-management state

> 2026-09-22 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪判定退回。自己跑的驗證：`pnpm test` 8 檔 54 測試全過、`pnpm run build` 通過、`pnpm run lint` 只剩 T-13 既有的 1 則 fast-refresh warning。任務邊界乾淨（只動 `kanban-frontend/src/canvas/members/**`、`boardMembershipApi.ts` 追加三個函式、`BoardCanvasPage.tsx` 一行 import，`.state/` 只動自己的任務目錄）。八條驗收條件都有對應測試，`@uc-remove-member @fail-p1` 抽查成立（訊息逐字相符、清單不變）。設計稿 `MemberManagement.dc.html` 的版面／字級／色票相符，灰色註記沒做進產品畫面。

待下一輪 Dev 處理：
- `D-01`：「移除成員」要一律先顯示確認（ui 操作表「需確認？」欄＝是，與「變更成員角色」同值），現況只在卡片負責人情境確認；確認後才打 API，後端回卡片張數訊息時再要求二次確認並以 `confirmed=true` 重打。測試標題與斷言同步改。
- `D-02`：「設為 Owner」的顯示條件由 `member.role !== 'OWNER'` 改成 `member.role === 'MEMBER'`，避免對 Viewer 列提供違反 `uc-change-member-role` pre p2 的入口，並補一個 VIEWER 列無此按鈕的測試。

OQ 現況（皆不阻塞）：`OQ-01`（`item.component` 值，接手人工／F07 整合 CR）維持；`OQ-02` 問題 (1) 改由 `D-01` 承接、問題 (2) 維持待處理（接手人工）；本輪新開 `OQ-03`（`board-membership.role` 的 Viewer 值在 spec 欄位表與 ui 資料表兩處矛盾，接手人工，需走 CR）。沒有等級「覆蓋」或「環境」的阻塞 OQ。
