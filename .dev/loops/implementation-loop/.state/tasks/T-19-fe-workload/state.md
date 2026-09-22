# T-19-fe-workload state

> 2026-09-22 Dev 第 2 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

# T-19-fe-workload state

Dev 第 2 輪已處理 Review 第 1 輪退回的 D-01、D-02，皆為紀錄面問題，程式碼未變動。

- D-01：開立 `OQ-T-19-fe-workload-02`（高／不阻塞），把「Viewer 不可拖曳頭像」的矛盾（ui-workload.md 說無角色差異 vs. spec-user-membership.md 的 r-board-viewer 定義）交給人工裁示。
- D-02：把 `OQ-T-19-fe-workload-01` 拆成 `OQ-T-19-fe-workload-03`（拖放目標端，owner＝T-14-fe-board-item，並註明 T-14 仍 doing 需人工轉達）與 `OQ-T-19-fe-workload-04`（`s-cards-by-assignee` 導向 `s-card-detail`，owner＝人工，屬 T-19 自己範圍內的後續補作）。

Review 要先看：`.state/tasks/T-19-fe-workload/open-questions.md` 新增的 OQ-02／03／04 內容是否符合規則書格式與歸屬要求；程式碼與測試沿用第 1 輪已通過的結果，未重跑建置。
