# T-16-fe-activity-log state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：`s-activity-log` 前端實作已完成並 commit（`kanban-frontend/src/api/activityLogApi.ts`、`canvas/ActivityLogItem.tsx`＋`.css`＋測試、`main.tsx` 註冊）。呼叫 T-04 已合併的 `GET /api/boards/{boardId}/activity-log`，以 Canvas item（`item.component === "activity-log"`）呈現操作人＋動作＋時間，依後端排序原樣顯示。

這輪做了什麼：`ui-user-membership.md` `s-activity-log` 因 OQ-29 標記「未討論」，但其前提（跨 aggregate 投影）已由 T-04 補齊，故依 usecase 區塊與 Gherkin Scenario 推論實作最小可用畫面；進入路徑沿用 T-13 既有的一般化「加入元件」機制。版面細節與 `item.component` 命名是推論決定，已開不阻塞 OQ-T-16-fe-activity-log-01 徵詢是否要回頭補齊 ui 文件。

Review 要先看：(1) OQ-T-16-fe-activity-log-01 的推論是否合理；(2) `ActivityLogItem.tsx` 對後端回應形狀（`operatorUsername`/`operatorDisplayName` 可為 null）的處理；(3) `pnpm exec vitest run` 全綠（47/47）、`pnpm run build`／`lint` 皆過。
