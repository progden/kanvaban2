# T-19-fe-workload state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現在狀態：`s-workload-dashboard`（Canvas item，`item.component === 'workload-dashboard'`）與 `s-cards-by-assignee` 已實作，測試／build／lint 皆過（見 decision-log Check）。

這輪做了什麼：新增 workloadApi／cardApi／WorkloadDashboard／CardsByAssigneeDialog／cardAssigneeDrag 共用拖放協定，並在 BoardCanvasPage 註冊 item 元件；拖曳目標（F01 s-board 卡片縮圖，屬 T-14-fe-board-item）與卡片詳情導覽因 T-14 尚未合併而留白，已開 `OQ-T-19-fe-workload-01`（不阻塞）與 `ADR-T-19-fe-workload-01`（拖放協定）。

Review 要先看什麼：
1. `WorkloadDashboard.tsx` 的 Viewer 角色不可拖曳判斷（decision-log 第 1 點）是否合理。
2. `cardAssigneeDrag.ts` 的協定設計（ADR）是否可接受，T-14 合併時能否直接沿用。
3. `OQ-T-19-fe-workload-01` 的不阻塞判斷是否成立（未違反任何定稿 spec 原文，只是掛載目標不存在）。
