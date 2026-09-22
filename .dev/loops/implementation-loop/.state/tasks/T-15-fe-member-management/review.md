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

## 2026-09-22 Review 第 2 輪：附保留核准

判定：附保留核准（`done`）。建置／測試全綠，`D-01`、`D-02` 兩條都真的修好，任務邊界乾淨，沒有等級「覆蓋」或「環境」的阻塞 OQ。

### 1. 自己跑的建置與完整測試（在 `kanban-frontend/`，非採信 Dev 交接摘要）

- `pnpm test`（vitest 5.0.1）：`Test Files 8 passed (8)`、`Tests 55 passed (55)`、`Duration 66.20s`，exit code 0。比第 1 輪多 1 個測試（新增 Viewer 列無「設為 Owner」）。
- `pnpm run build`（`tsc -b && vite build`）：`✓ 54 modules transformed.`、`✓ built in 915ms`，通過。
- `pnpm run lint`（oxlint）：只剩 `src/canvas/itemComponentRegistry.tsx:21:10: warning react(only-export-components)` 一則，該檔本任務兩輪都未改動（T-13 既有），非本任務新增。

### 2. `D-01`／`D-02` 逐條驗收

**`D-01`（移除成員一律先確認）＝修好。**

- 條件 1、2：`MemberManagementDialog.tsx:106` `handleRemoveClick` 只 `setRemoveTarget({ username, message: '確定要移除 ' + displayName + '？', confirmed: false })`，不呼叫任何 API；確認卡按「移除」才進 `handleConfirmRemove`（`:111`），以 `removeTarget.confirmed`（初值 `false`）呼叫 `membershipApi.removeMember`。後端回 409 且訊息符合 `CARD_ASSIGNEE_CONFIRMATION_PATTERN` 時（`:123`）把確認卡訊息換成後端的張數訊息、`confirmed` 改 `true` 並保持開啟，再次確認才以 `confirmed=true` 重打（`boardMembershipApi.ts:29` 組出 `?confirmed=true`）。
- 條件 3：其他失敗走 `:126` 分支——關閉確認卡、`setRemoveError({ username, message })` 在該列下方顯示，不重新載入清單，成員留在原地。
- 條件 4：`MemberManagementDialog.test.tsx:156`「移除非唯一 Owner 或 Member 後，先顯示確認，確認後該成員才自清單移除」——按「移除」後先 `findByText('確定要移除 雅婷？')`，並斷言 `expect(screen.getByText('雅婷')).toBeInTheDocument()`（確認前沒被移除），確認後才 `queryByText('雅婷')` 不存在。兩條驗收條件都仍有測試：`:156`（一般移除）與 `:204`（卡片負責人先一般確認 → 再依張數確認 → 才觸發）。
- 條件 5：`decision-log.md` 第 2 輪「D-01」段已註明 `OQ-02` 問題 (1) 由本條處理、問題 (2) 維持待處理。

**`D-02`（Viewer 列不顯示「設為 Owner」）＝修好。** `MemberManagementDialog.tsx:210` 條件已由 `member.role !== 'OWNER'` 改為 `member.role === 'MEMBER'`；新增測試 `:237`「角色為 Viewer 的成員列沒有設為 Owner 按鈕」以 `role: 'VIEWER'` 的成員渲染並斷言該列 `queryByRole('button', { name: '設為 Owner' })` 不存在。後端補 pre p2 檢查屬 T-04 範圍，本輪未越界動它，符合 `D-02` 條件 3。

### 3. spec 對應（`ui-user-membership.md` `s-member-management` 八條驗收條件）

八條全部有對應測試：`:40`、`:63`、`:84`、`:105`、`:134`、`:156`、`:182`、`:204`，加上兩條防呆測試 `:237`（Viewer 無升級入口）、`:247`（`r-board-member` 看不到移除）。第 1 輪標為「行為對但少了確認步驟」的那條（`:156`）本輪已補上確認斷言。

`@fail-pN` 抽查：`spec-user-membership.md` `@uc-remove-member @fail-p1`。測試 `:182`「移除看板唯一 Owner 時，確認後顯示訊息，該成員仍留在清單中」：`DELETE /api/boards/board-a/members/user1` 回 409 `{"message":"看板至少需要保留一位 Owner"}`，斷言 `within(row).findByText('看板至少需要保留一位 Owner')` 且 `expect(screen.getByText('陳柏翰')).toBeInTheDocument()`。程式碼路徑對得上：該訊息不符 `CARD_ASSIGNEE_CONFIRMATION_PATTERN`，走 `:126` 一般失敗分支，`setRemoveTarget(null)` 後**不呼叫** `refreshMembers()`，清單維持原狀——「拒絕，訊息為 "看板至少需要保留一位 Owner"，且資料不變」成立。訊息字串與 spec `fail p1` 原文逐字相符。

### 4. `kanban-core` 純度

本任務兩輪 diff（`git diff loop/implementation...HEAD --stat`：15 檔、+1128/-4）沒有任何 `kanban-core`／`kanban-spring` 檔案，不涉及；無跨 aggregate 讀取投影。

### 5. 任務邊界

`git diff loop/implementation...HEAD --stat`：程式碼只動 `kanban-frontend/src/canvas/members/**`（新增 6 檔）、`src/api/boardMembershipApi.ts`（純追加三個函式）、`src/pages/BoardCanvasPage.tsx`（3 行 side-effect import 與註解）。本輪（`git diff 7cb992a~5 HEAD --stat`）只動 `MemberManagementDialog.tsx`／`.test.tsx` 與 `.state/`。`.state/` 只有 `.state/tasks/T-15-fe-member-management/`，沒有 `.state/tasks.md`、`.state/archive/**`、別的任務目錄。沒有碰 `.dev/conventions/**`、`scripts/**`、spec／ui 文件本體。`git status --short` 空白，工作區乾淨。

### 6. 設計稿核對（`.dev/ui-prototype/MemberManagement.dc.html`）

本輪新增的「一般移除確認卡」沿用既有 `member-management-dialog__confirm` 樣式，附掛於清單列上，結構為「訊息 + 取消／移除兩顆按鈕」，與設計稿 `:110-117` 的移除確認小卡（標題行＋說明＋右下 取消／移除，移除為紅字）一致。設計稿 `:150` 本就註記『⚠️ 規格未定義：確認小卡的樣式與所有訊息文字』，故「確定要移除 {displayName}？」屬可自行擬定範圍，不需開 OQ。設計稿灰色註記（`.id`／`.uc`／`.warn`，例如 `:102` 『設為 Owner → uc-change-member-role　移除 → uc-remove-member』、`:118`、`:150`）沒有出現在實作中。

「需確認？」欄落實情形：「變更成員角色」＝是（無條件確認卡）、「移除成員」＝是（無條件確認卡，本輪修好）、「邀請成員」＝否（直接送出），三者與操作表相符。「失敗時」欄：邀請失敗保留輸入（`setInviteUsername` 只在成功後清空）並在表單下方顯示訊息；移除失敗在該列下方顯示訊息；變更角色失敗在確認卡內顯示訊息——都只寫呈現方式，未重述業務結果。

### 7. OQ 逐則核對

- `OQ-T-15-fe-member-management-01`（`item.component` 選 `"board-members"`）：第 1 輪已逐字比對過引文；`uc-place-item` 未限制 `item.component` 可能值，做完本任務不必違反任何定稿原文 → 等級「高」／不阻塞正確，`接手：無` 屬實（任務清單無任務負責 F07 整合 CR）。**維持**。
- `OQ-T-15-fe-member-management-02`：問題 (2)（`r-board-member` 是否需要「嘗試移除成員」的拒絕情境）確為規格未定義（`spec-user-membership.md` 只有 `uc-reject-invite-by-member`／`uc-reject-role-change-by-member` 兩個拒絕類 usecase），等級「高」／不阻塞／`接手：無` 正確。問題 (1) 已由 `D-01` 落實成「一律先確認」，該則內文的推論段（『一般移除…直接呼叫後端』）**已與現行實作不符**，需人工在該則底下補解除說明；`loopctl` 無法改寫既有 OQ 內文，Dev 已在 `decision-log.md` 註明，符合 `D-01` 條件 5。列為保留事項。
- `OQ-T-15-fe-member-management-03`（`board-membership.role` 在 spec 欄位表為 `enum(Owner, Member, Viewer)`、ui 資料表寫『enum(Owner, Member)，依欄位表限制』兩處矛盾）：本輪重新檢驗等級——依引用方向 `spec ← ui`，以 spec（較上游）為準即可把 Viewer 列顯示出來並隱藏升級入口，不需要改動任何定稿文字，故等級「高」／不阻塞正確，`接手：人工`（需走 CR 修訂 `ui-user-membership.md`）屬實。**維持**。

Dev 第 2 輪交接摘要「待確認事項」三項與 `loopctl show` 列出的三則 OQ 一一對應，沒有只寫在摘要裡就消失的項目，也沒有該開而未開的新 OQ。

### 判定與保留事項

**附保留核准（`done`）**，第 2 輪（上限 6）。保留事項與接手者：

1. `OQ-01`（「看板成員」item 的 `item.component` 值是否採用 `"board-members"`）：接手＝**人工**（F07 整合 CR 定案時核對）。若定案為別的字串，改 `kanban-frontend/src/canvas/members/BoardMembersItem.tsx` 的 `registerItemComponent` 呼叫即可，不影響任何 Scenario。
2. `OQ-02` 問題 (2)（`r-board-member` 嘗試移除成員的拒絕情境）：接手＝**人工**，需先在 `spec-user-membership.md` 補一個對應 usecase 才有實作依據；現況前端對 `r-board-member` 隱藏「移除」按鈕。
3. `OQ-02` 問題 (1)：行為面已由 `D-01` 結案（一律先確認），僅剩 OQ 內文推論過時，接手＝**人工**（在該則底下補解除說明並把狀態改為已解除）。
4. `OQ-03`（Viewer 角色在本畫面的顯示與 ui 資料表／角色權限表缺漏）：接手＝**人工**，需走 CR 修訂 `ui-user-membership.md`（補 Viewer 與 `r-board-viewer`，或確認本畫面不管理 Viewer）。另，後端 `BoardMembershipApplicationService.changeMemberRole` 缺 `uc-change-member-role` pre p2 檢查屬 **T-04-be-board-membership** 範圍，本任務只在前端不提供違反 pre p2 的入口。
