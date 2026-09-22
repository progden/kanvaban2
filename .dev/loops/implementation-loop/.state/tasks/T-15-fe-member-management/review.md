# T-15-fe-member-management 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-22 Review 第 1 輪：退回

判定：退回（`doing`）。建置與測試全綠、任務邊界乾淨，但 `s-member-management`「移除成員」的確認行為與 ui 操作表「需確認？」欄不符，另有一處會觸發違反 `uc-change-member-role` pre p2 的入口。開 `D-01`、`D-02`，另開 `OQ-T-15-fe-member-management-03`。

### 1. 自己跑的建置與完整測試（在 `kanban-frontend/`）

- `pnpm test`（vitest 5.0.1）：`Test Files 8 passed (8)`、`Tests 54 passed (54)`、`Duration 56.35s`，exit code 0。
- `pnpm run build`（`tsc -b && vite build`）：`✓ 54 modules transformed.`、`✓ built in 927ms`，通過。
- `pnpm run lint`（oxlint）：只有 1 則 warning — `src/canvas/itemComponentRegistry.tsx:21:10: warning react(only-export-components)`，該檔本輪未改動（T-13 既有），非本任務新增。

三項結果與 Dev 交接摘要「Check」欄一致。

### 2. spec 對應核對（`ui-user-membership.md` `s-member-management` 八條驗收條件）

| 驗收條件 | 對應測試 | 結果 |
|---|---|---|
| Owner 邀請存在且非現有成員的帳號後，清單新增一筆、成員數加 1 | `MemberManagementDialog.test.tsx:40` | 通過 |
| 邀請已是成員的帳號時，輸入內容保留、顯示訊息，清單不變 | 同檔 `:63` | 通過 |
| 非 Owner 嘗試邀請成員時顯示訊息，清單不變（`uc-reject-invite-by-member`） | 同檔 `:84` | 通過 |
| Owner 將 Member 變更為 Owner 後角色顯示為 Owner | 同檔 `:105` | 通過 |
| 非 Owner 嘗試變更成員角色時顯示訊息、角色顯示不變（`uc-reject-role-change-by-member`） | 同檔 `:134` | 通過 |
| 移除非唯一 Owner 或 Member 後，該成員自清單移除 | 同檔 `:156` | 行為對，但少了確認步驟，見 `D-01` |
| 移除看板唯一 Owner 時顯示訊息，該成員仍留在清單中 | 同檔 `:176` | 通過 |
| 移除仍是卡片負責人的成員時，先顯示確認並告知張數，確認後才觸發 | 同檔 `:195` | 通過 |

`@fail-pN` 抽查：`spec-user-membership.md` `@uc-remove-member @fail-p1`「Board 至少保留一個 Owner」。測試 `:176` 以 409 `{"message":"看板至少需要保留一位 Owner"}` 回應 `DELETE /api/boards/board-a/members/user1`，斷言 `within(row).findByText('看板至少需要保留一位 Owner')` 且 `expect(screen.getByText('陳柏翰')).toBeInTheDocument()`——「拒絕，顯示錯誤訊息…該 `board-membership` 不被移除」成立（清單未重新載入、未移除該列）。訊息字串與 `uc-remove-member` fail p1 的原文『看板至少需要保留一位 Owner』逐字相符，後端 `BoardMembershipApplicationService` 與 `BoardMembershipController.statusFor`（`MINIMUM_BOARD_OWNER -> CONFLICT`）也對得上。

API 契約核對：前端 `boardMembershipApi.ts` 的 `POST /api/boards/{id}/members`、`PATCH .../members/{username}/role`、`DELETE .../members/{username}?confirmed=true` 與 T-04 `BoardMembershipController` 的 `@PostMapping`／`@PatchMapping("/{username}/role")`／`@DeleteMapping("/{username}")` + `@RequestParam confirmed` 一致；`CARD_ASSIGNEE_CONFIRMATION_PATTERN`（`/仍是\s*\d+\s*張卡片的負責人/`）比對的訊息在 `BoardMembershipApplicationService:99` 組成為 `targetName + " 仍是 " + assignedCards.size() + " 張卡片的負責人，移除後這些卡片會變成未指派"`，比對得上。

### 3. `kanban-core` 純度

本輪 diff 沒有任何 `kanban-core`／`kanban-spring` 檔案（見第 4 點），不涉及。

### 4. 任務邊界

`git diff loop/implementation...HEAD --stat`：13 個檔、+968/-4。`.state/` 只動 `.state/tasks/T-15-fe-member-management/`（`decision-log.md`／`open-questions.md`／`rounds.log`／`state.md`／`status`），沒有碰 `.state/tasks.md`、`.state/archive/**` 或別的任務目錄。程式碼只在 `kanban-frontend/src/canvas/members/**`（新增 6 檔）、`src/api/boardMembershipApi.ts`（純追加 `inviteMember`／`changeMemberRole`／`removeMember` 三個函式，未改既有 `listMembers`）、`src/pages/BoardCanvasPage.tsx`（加一行 side-effect import 與註解）。沒有動 `.dev/conventions/**`、`scripts/**`、spec／ui 文件本體，也沒有動 `itemComponentRegistry.tsx`／`CanvasStage.tsx` 的既有介面。邊界乾淨。

### 5. 交接摘要的待確認事項／OQ 核對

Dev「待確認事項」兩項都有對應 OQ，`loopctl show` 列得出 `OQ-01`、`OQ-02`，沒有只寫在摘要裡就消失的項目。

- `OQ-01`（`item.component` 值選 `"board-members"`）：引文到 `.dev/F07-canvas-layout/ui-canvas-layout.md:80`（待確認事項）與 `spec-canvas-layout.md:163`（`uc-place-item` post 第 1 條）逐字比對，相符。等級「高」／不阻塞正確——`uc-place-item` 沒有限制 `item.component` 的可能值，選一個字串不必違反任何定稿原文。`接手：無` 屬實：任務清單裡沒有任務負責 F07 的整合 CR。**維持**。
- `OQ-02`：引文與 `ui-user-membership.md` 操作表、驗收條件、角色與權限表逐字比對，相符。但問題 (1)（一般移除是否需確認）不是規格未定義——同一張操作表「需確認？」欄對「移除成員」寫的就是『是』，跟「變更成員角色」那列同值，而 Dev 對後者做了無條件確認、對前者沒有。這一半已由 `D-01` 承接，不該留成 OQ。問題 (2)（`r-board-member` 是否需要「嘗試移除成員」的拒絕情境）確實是規格未定義，等級「高」／不阻塞／`接手：無` 正確，**維持**。
- 新開 `OQ-T-15-fe-member-management-03`（等級 高／不阻塞／接手 人工／`spec-conflict`）：`spec-user-membership.md` 欄位表 `board-membership.role` 為 `enum(Owner, Member, Viewer)`，`ui-user-membership.md` 本畫面資料表卻寫『enum(Owner, Member)，依欄位表限制』，兩處矛盾並列於該則內文。這是文件層的事（要走 CR），不擋本任務；前端不該提供違反 `uc-change-member-role` pre p2 的入口那一半由 `D-02` 承接。

沒有等級「覆蓋」或「環境」的阻塞 OQ，所以不標 `blocked`。

### 6. 設計稿核對（`.dev/ui-prototype/MemberManagement.dc.html`）

版面結構相符：標題「看板成員」＋人數副標、右上「關閉」、邀請列（帳號輸入框＋角色單選＋「邀請」主按鈕）、成員清單（頭像＋顯示名稱＋帳號＋角色徽章＋「設為 Owner」／「移除」小按鈕）、附掛於列上的確認小卡（取消／動作兩顆）。字級與設計稿對得上（標題 17px、副標 12.5px、顯示名稱 13px、帳號 11px、角色徽章 10.5px），色票與字體沿用 T-22 建立的共用 token（`--color-primary` 等）而非自訂，頭像沿用共用 `.avatar`。確認卡文字『把 {displayName} 設為 Owner？這個動作沒辦法改回來。』與設計稿『把蔡依帆設為 Owner？』『這個動作沒辦法改回來。』一致；設計稿該處本就標註『⚠️ 規格未定義：確認小卡的樣式與所有訊息文字』，移除確認直接顯示後端訊息可接受。

設計稿的灰色註記（`.id`／`.uc`／`.warn`，例如 `user.username／user.display-name／board-membership.role`、`uc-invite-member`、『⚠️ 規格未定義：訊息文字內容』）沒有出現在實作裡，已逐一確認。

「需確認？」欄落實情形：「變更成員角色」＝是，已落實；「移除成員」＝是，**未落實**（只在卡片負責人情境確認），見 `D-01`。設計稿註記『兩者都需確認』也指向同一件事。「邀請成員」＝否，實作直接送出，相符。「失敗時」欄：邀請失敗保留輸入內容並在表單下方顯示訊息（`inviteError`，`setInviteUsername` 只在成功後清空）、移除失敗在該列下方顯示訊息，皆只寫呈現方式，相符。

### 判定與理由

**退回（`doing`）**，第 1 輪（上限 6），留下 `D-01`、`D-02` 兩條給下一輪 Dev。第 6 點的「需確認？」欄未落實是對定稿 ui 操作表的偏離，不是規格未定義；`D-02` 是會實際造成違反 `uc-change-member-role` pre p2 的入口。兩者都在一輪 Dev 內修得完，不需 `blocked`。

保留事項與接手者（供下一輪 Review 續看）：
- `OQ-01`（`item.component` 值）：接手＝人工／F07 整合 CR，本任務不再處理。
- `OQ-02` 問題 (2)（`r-board-member` 嘗試移除成員的拒絕情境）：接手＝人工，需先補 usecase 才有實作依據。
- `OQ-03`（Viewer 角色在本畫面的顯示與權限）：接手＝人工，需走 CR 修訂 `ui-user-membership.md`。
