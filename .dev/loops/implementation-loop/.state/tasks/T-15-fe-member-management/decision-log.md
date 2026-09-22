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
