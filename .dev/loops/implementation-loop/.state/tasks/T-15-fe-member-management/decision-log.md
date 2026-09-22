# T-15-fe-member-management 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Dev 第 1 輪：實作 s-member-management 與看板成員畫布元件

### 這輪做了什麼

實作 `s-member-management`（Board 成員管理對話框）與 F07 `s-canvas` 上的「看板成員」item（本任務依 tasks.md 註明從此 item 進入，機制細節由本任務決定）。依賴 T-13-fe-canvas-shell、T-04-be-board-membership 皆已合併，`GET/POST/PATCH/DELETE /api/boards/{boardId}/members...` 端點與 `MemberResponse`／`InviteMemberRequest`／`ChangeMemberRoleRequest` 形狀已在 T-04 定案，直接沿用。

### 涵蓋範圍

- `spec-user-membership.md`／`ui-user-membership.md` `s-member-management`：`uc-invite-member`、`uc-reject-invite-by-member`、`uc-change-member-role`、`uc-reject-role-change-by-member`、`uc-remove-member`（含 fail p1「唯一 Owner」與 pre p2「仍是卡片負責人需確認」兩種情境）。
- `spec-canvas-layout.md`／`ui-canvas-layout.md` `s-canvas` 操作表「於『看板成員』item 選擇加入成員」：新增 `item.component === 'board-members'` 的畫布元件（見 `canvas/members/BoardMembersItem.tsx`），顯示成員頭像清單，點擊開啟 `MemberManagementDialog`。
- 未涵蓋：`s-activity-log`（另一任務 T-16）；`s-cards-by-assignee`／`s-card-assignee-picker`（另一任務 T-14）。

### 關鍵判斷

1. **`item.component` 值選定 `"board-members"`**：spec「待釐清」未定案（待整合 CR），`uc-place-item` 對 `item.component` 沒有白名單限制，屬純技術決定，不影響任何 Scenario 行為。已開 **OQ-T-15-fe-member-management-01**（不阻塞），供整合 CR 定案時核對。
2. **「看板成員」item 的掛載機制**：沿用 T-13 提供的 `registerItemComponent`／`resolveItemComponent` 掛載點，透過 `BoardCanvasPage.tsx` 的 side-effect import 註冊；`ItemContentProps` 沒有帶 `boardId`（T-13 設計如此，T-14 應該也會遇到同樣的限制），本任務改用 `useParams<{ boardId }>()`（元件掛在 `/boards/:boardId` route 底下）取得，未改動 `itemComponentRegistry.tsx`／`CanvasStage.tsx` 既有介面。
3. **「移除成員」何時跳出確認**：只有目標成員仍是卡片負責人時才顯示確認卡片（先以 `confirmed=false` 呼叫，後端 409 回覆卡片張數訊息後才顯示確認，確認後以 `confirmed=true` 重打）；一般移除直接呼叫、不另外跳出通用確認。理由：驗收條件只在「仍是卡片負責人」情境明文要求先確認，另一條「移除非唯一 Owner 或 Member」的驗收條件沒有提到確認步驟。已開 **OQ-T-15-fe-member-management-02**（不阻塞）。
4. **`r-board-member` 看不到「移除」按鈕**：角色與權限表「做得到」欄只列「嘗試邀請成員、嘗試變更成員角色」，未列移除；spec 也沒有對應的「非 Owner 嘗試移除成員」usecase（只有 `uc-reject-invite-by-member`／`uc-reject-role-change-by-member` 兩個）。判斷為避免前端自行發明一個規格未定義的拒絕情境，直接隱藏「移除」按鈕（只有 `r-board-owner` 看得到）；同一則 OQ-02 一併記錄。
5. **邀請／變更角色對 `r-board-member` 保持可見可點擊**：角色表明文列出「嘗試邀請成員、嘗試變更成員角色」，對應 `uc-reject-invite-by-member`／`uc-reject-role-change-by-member`；後端 `ensureOwner` 本來就會回 403，前端直接把錯誤訊息顯示在原本的表單／確認區塊，不需要另外攔在前端。
6. **錯誤訊息辨識用文字比對**：`ApiError`（`api/http.ts`）只帶 `status`／`message`，不帶後端的 `ErrorCode`；用正規表示式 `/仍是\s*\d+\s*張卡片的負責人/` 比對移除失敗訊息是否為「需要確認」情境（`CARD_ASSIGNEE_CONFIRMATION_NEEDED` 的訊息格式），比對到才進入確認流程，比對不到（例如唯一 Owner）當一般失敗直接顯示。這個判斷方式沿用既有 `ApiError` 介面，未擴充它的形狀。

### 待確認事項

- OQ-T-15-fe-member-management-01：「看板成員」item 的 `item.component` 值是否採用 `"board-members"`。
- OQ-T-15-fe-member-management-02：一般移除是否也要跳出通用確認；`r-board-member` 是否需要對應的「嘗試移除成員」拒絕情境。

### Check

- `pnpm test`（vitest）：8 個測試檔、54 個測試全過（含新增的 `MemberManagementDialog.test.tsx` 9 個、`BoardMembersItem.test.tsx` 2 個）。
- `pnpm run lint`（oxlint）：僅既有 1 則非阻斷 warning（`itemComponentRegistry.tsx` fast-refresh 建議搬檔，非本任務新增）。
- `pnpm run build`（`tsc -b && vite build`）：通過。

## 2026-09-22 Dev 第 2 輪：D-01／D-02 修正：移除成員先確認、Viewer 排除設為 Owner

### 本輪處理範圍

只處理 Review 第 1 輪留下的 `D-01`、`D-02`，不動其他範圍。

### D-01：「移除成員」改成一律先確認

`ui-user-membership.md` `s-member-management` 操作表「移除成員」「變更成員角色」兩列「需確認？」欄同為『是』，原實作只在「變更成員角色」落實無條件確認，「移除成員」則先以 `confirmed=false` 打 API、僅在後端回卡片負責人樣式的 409 時才補顯示確認卡，不符合操作表。

修法：`MemberManagementDialog.tsx` 的「移除」按鈕改成先顯示確認卡（不先呼叫 API），確認卡文字為「確定要移除 {displayName}？」；使用者按確認才以 `confirmed=false` 呼叫 `removeMember`。若後端回 409 且訊息符合卡片負責人樣式（`CARD_ASSIGNEE_CONFIRMATION_PATTERN`），把確認卡訊息換成該則張數訊息並保持開啟、要求再次確認，確認後以 `confirmed=true` 重打（`uc-remove-member` pre p2 流程不變）；其他失敗（如 fail p1「看板至少需要保留一位 Owner」）關閉確認卡、在該列下方顯示訊息，成員仍留在清單中。

一般確認卡的訊息文字（「確定要移除 {displayName}？」）規格與 ui 檔都未定義字面文字（`.dev/ui-prototype/MemberManagement.dc.html` 對確認卡的註記是『規格未定義：確認小卡的樣式與所有訊息文字』），比照既有變更角色確認卡的簡潔語氣自行擬定，不需另開 OQ。

`OQ-T-15-fe-member-management-02` 問題 (1)（一般移除是否需確認）由本條處理：操作表該欄本來就寫「是」，不是規格未定義，已在此註明；問題 (2)（`r-board-member` 嘗試移除成員的拒絕情境）維持待處理、接手人工，本輪未動。

`MemberManagementDialog.test.tsx` 三個既有測試（非唯一成員移除、唯一 Owner 移除、卡片負責人移除）同步改成「先確認、確認後才打 API」的斷言與標題。

### D-02：「設為 Owner」排除 Viewer

`uc-change-member-role` pre p2『目標使用者是該 `board` 的 Member』，原條件 `member.role !== 'OWNER'` 會讓 Viewer 列也出現「設為 Owner」，點擊會對不滿足 pre p2 的目標觸發用例（後端 T-04 `changeMemberRole` 未檢查 pre p2，會直接放行）。

修法：條件改成 `member.role === 'MEMBER'`。新增測試「角色為 Viewer 的成員列沒有設為 Owner 按鈕」。後端補 pre p2 檢查不在本任務範圍，`OQ-T-15-fe-member-management-03`（Viewer 角色在本畫面的顯示與 spec／ui 資料表矛盾）維持待處理、接手人工、需走 CR。

### 涵蓋範圍

- Scenario／驗收條件：`s-member-management` 八條驗收條件本輪未新增，維持第 1 輪的對應測試；「移除非唯一 Owner 或 Member」「移除看板唯一 Owner」「移除仍是卡片負責人」三條的測試斷言與標題已依確認流程更新。
- 新增測試：Viewer 列無「設為 Owner」按鈕（對應 `uc-change-member-role` pre p2，防呆而非驗收條件本身）。

### 待確認事項

- `OQ-T-15-fe-member-management-01`（`item.component` 值）：維持，接手人工／F07 整合 CR。
- `OQ-T-15-fe-member-management-02` 問題 (2)（`r-board-member` 嘗試移除成員的拒絕情境）：維持，接手人工，需先補 usecase。
- `OQ-T-15-fe-member-management-03`（Viewer 角色在 spec／ui 兩處矛盾）：維持，接手人工，需走 CR。

無新開 OQ、無等級「覆蓋」或「環境」的阻塞項目。

### Check（在 kanban-frontend/ 執行）

- `pnpm test`：`Test Files 8 passed (8)`、`Tests 55 passed (55)`。
- `pnpm run build`（`tsc -b && vite build`）：通過，`✓ 54 modules transformed.`。
- `pnpm run lint`（oxlint）：僅剩 `src/canvas/itemComponentRegistry.tsx:21:10` 既有 warning（T-13，本輪未動）。
