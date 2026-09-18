# T-04-be-board-membership state

> 2026-09-19 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現況：spec-user-membership.md 除 T-01 已完成的「使用者帳號」「登入登出」外全部功能已實作完成，第 1 輪即完成，未觸及 D-xx（尚無 Review 紀錄）。

這輪做了什麼：新增 `BoardMembership`／`BoardRole` Aggregate；`uc-create-board` 同交易建立 Owner membership；九個結構調整端點補上 Owner 檢查；新增 `uc-delete-board`／`uc-view-board-list`／`uc-reject-board-access-by-nonmember`；`Card` 新增負責人（`assigneeIds`／`assignTo`）並實作「卡片負責人指派」5 個 uc；新增跨 Aggregate 查詢 `io.progden.kanban.query.BoardActivityLogQueryService` 實作「檢視看板活動紀錄」。新增 25 個 Cucumber Scenario，連同既有共 60 個全數通過；`kanban-core` 單元測試（含新增 `BoardMembershipTest`）全過；`./gradlew build` 成功。已開 3 則非阻塞 OQ（活動紀錄措辭切換規則、刪除 Board 後 board-membership 孤兒列、移除成員的錯誤訊息重用），已寫 1 則 ADR（`io.progden.kanban.query` 需手動加進 `@ComponentScan`）。

Review 要先看什麼：
1. 三則 OQ 的判斷是否合理（decision-log 有完整說明）。
2. `BoardController`／`CardController` 擴充的端點範圍是否在任務授權範圍內（decision-log「低風險技術決定」最後一點有說明理由）。
3. 實際跑一次 `./gradlew build --no-daemon` 確認建置與測試綠燈。
