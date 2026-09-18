# T-04-be-board-membership 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Review 第 1 輪：退回

### 1. 建置與測試（自己重跑）
- `./gradlew build --no-daemon` 第一次全部 up-to-date，所以改跑 `./gradlew clean build --no-daemon --rerun-tasks`：BUILD SUCCESSFUL（2m 21s）。
- 彙整 `build/test-results` 的 JUnit XML：共 108 個測試，skipped 0、failures 0、errors 0。`kanban-spring` 有 10 個 feature 檔，其中 T-04 新增 5 個（board-access／board-activity-log／board-membership／board-permission／card-assignment）。`@added @wip` 的 CR-009 Scenario「建立 Board 後帶有預設的 Swimlane 與 Stage」有實際執行並通過。

### 2. spec 對應
- 用腳本比對 `spec-user-membership.md` 所有 ```gherkin 區塊的步驟／tag 行，和 feature 檔的步驟：只有 T-01 已知的 `@CR-007` 那行少了 `@wip` 這一處差異，其餘逐字一致，F02 的 Scenario 全部都有對應。
- 抽查的 `@fail-p1`「Board 至少保留一個 Owner」：step 同時驗證錯誤訊息和 `boardMembershipJpaRepository.findByBoardIdAndUserId(...).isPresent()`，資料不變這點成立。另外抽查 `uc-reject-structure-change-by-member`：九個結構端點在 `BoardApplicationService` 都先呼叫 `ensureOwner`，訊息「只有 Owner 可以調整看板結構」和 post 逐字相符。`uc-create-board` 加了 `@Transactional`，在同一次交易內建立 Owner membership（交接事項 2）。
- **沒過（D-01）**：T-04 新增的卡片負責人端點、候選名單、依負責人查詢、成員列表，以及 `uc-member-add-card` 的 pre『操作者是該 `board` 的 `board-membership` 成員』，都沒有檢查操作者的成員資格，非成員也能呼叫。Dev 把這件事列為低風險、沒開 OQ，不符合規則書第 5 節。
- **沒過（D-02）**：`uc-view-board-list` post『列表只顯示我是 Owner 或 Member 的 `board`』，實作卻把 Viewer 的看板也列進去，而且沒開 OQ。

### 3. kanban-core 純度
- 在 `kanban-core/src/main` 搜尋 `import org.springframework`／`jakarta`／`lombok`，沒有結果；`NoSpringDependencyTest` 通過。跨 aggregate 的活動紀錄合併放在 `io.progden.kanban.query.BoardActivityLogQueryService`，並新增 ADR-T-04-be-board-membership-01 說明 ComponentScan 的做法，合理。

### 4. 任務邊界
- `git diff loop/implementation...HEAD --stat`：改動只有 `kanban-core`／`kanban-spring`、`.state/tasks/T-04-be-board-membership/**`，以及新增的 ADR 檔；沒有動 spec／ui／conventions／scripts／`tasks.md`／`CR.md`。動到 T-02／T-03 的檔案，都落在任務備註允許的範圍（Owner 檢查、同交易建 Owner），或屬於 F02 卡片負責人指派（本任務的 F02 範圍，`card.assignees` 之前沒有任務實作過）。

### 5. OQ 核對
- OQ-01（活動紀錄措辭）：到源頭比對，引文逐字正確。等級「高」、不阻塞，判斷正確。
- OQ-02（刪除 Board 後留下的 membership）：`crud` 與 post 的引文逐字正確。等級「高」、不阻塞，判斷正確。
- OQ-03（移除成員被拒時的訊息）：引文正確，只是把兩行 Gherkin 用「 / 」接在一起，不影響判讀。等級「高」、不阻塞，判斷正確。
- 三則的接手都填「無」，和實情一致（要等人工或 CR 裁決）。
- 缺漏：D-01、D-02 這兩件事應該開 OQ 卻沒開，已經寫進對應的 D-xx。

### 6. 前端：不適用（純後端任務）

### 判定：退回（第 1 輪，上限 6）
理由：建置綠燈、Scenario 也齊全，但 T-04 自己新增的端點沒有落實 pre／roles 規定的成員資格檢查（D-01），而且兩處規格上的疑點沒有依規則開 OQ（D-01、D-02）。

## 2026-09-19 Review 第 2 輪：附保留核准

### 1. 建置與測試（自己重跑）
- `./gradlew clean build --no-daemon --rerun-tasks`：BUILD SUCCESSFUL（2m 18s，14 個 task 全部執行）。
- 用 Python 彙整所有 `build/test-results` 的 JUnit XML：共 113 個測試，failures 0、errors 0、skipped 0。和第 1 輪的 108 個相比多了 5 個，也就是 `CardApplicationServiceTest` 的 3 個與 `BoardMembershipApplicationServiceTest` 的 2 個。

### 2. spec 對應與 D-01／D-02 修正
- D-01：`git show 8fea08c` 顯示，`CardApplicationService.addCard`／`setAssignees`／`dragAssign` 在寫入之前會先呼叫 `ensureCanEdit`，擋下非成員與 Viewer。`listAssigneeCandidates`／`listCardsByAssignee` 與 `BoardMembershipController.listMembers` 則呼叫 `ensureMember`。`CardController` 把 FORBIDDEN 對應成 HTTP 403。第 1 輪列出的端點都已補上檢查，`uc-member-add-card` 的 pre『操作者是該 `board` 的 `board-membership` 成員』也已經落實。
- 抽查「拒絕而且資料不變」：`非成員新增卡片時被拒絕且不建立卡片`、`Viewer新增卡片時被拒絕且不建立卡片` 都會斷言 `findActiveByBoardId` 為空；`非成員設定負責人時被拒絕且負責人不變` 會重新讀取卡片，斷言 `assigneeIds` 仍為空。三個測試都成立。
- D-02：`listBoardsForUser` 會過濾掉 `role == VIEWER`，符合 post『列表只顯示我是 Owner 或Member 的 `board`，不顯示我沒有權限的 `board`』（第 594 行）。測試 `看板列表不包含我只是Viewer的board` 驗證了 Viewer 拿到的是空列表、Owner 拿得到自己的看板。
- 第 1 輪已經逐條比對過 F02 的 Scenario，本輪只改 application／web，feature 檔沒有變動；60 個 Cucumber Scenario 在上面的建置裡照樣通過。

### 3. kanban-core 純度
- 本輪沒有動到 `kanban-core`；`NoSpringDependencyTest` 在這次建置裡通過。

### 4. 任務邊界
- `git diff loop/implementation...HEAD --stat`：共 57 個檔。本輪新增的改動只有 6 個 application／web／test 檔，外加 `.state/tasks/T-04-be-board-membership/**`；沒有碰到 spec／ui／conventions／scripts／`tasks.md`／archive，也沒有動到別的任務目錄。

### 5. OQ 核對
- OQ-04：到 `spec-user-membership.md` 逐字比對，pre 在第 514 行、`fail: {}` 在第 517 行、四個 uc 的 `roles: [r-board-member]` 在第 651／665／687／698 行、角色表 `r-board-viewer` 在第 56 行，全部一致。等級「高」、不阻塞，判斷正確：這些都是 `fail` 沒有定義的補位，不必違反任何定稿原文。
- OQ-05：post 在第 594 行、角色表在第 56 行，逐字一致。引用三（變更紀錄第 80 行）用「…」省略了中間一段，不過保留下來的文字都是原文，不影響判讀。情況選「兩處矛盾並列」是正確的；等級「高」、不阻塞也正確，因為照 post 字面實作。
- OQ-01～03 第 1 輪已經核對過，本輪沒有變動。01～05 的接手都填「無」，符合實情：都要等人工或 CR 裁決。
- 本輪新開 OQ-06（等級高、不阻塞、接手：人工）：T-03 既有的卡片端點（getCard／edit／move／comment／delete）還是只檢查「已登入」，Viewer 仍然可以寫入，非成員也能讀取。F01 這些 uc 的 roles 是 `r-user`，而且沒有成員 pre，所以不在 T-04 的範圍內；但任務清單裡沒有任何任務會處理這件事，因此交給人工。

### 6. 前端：不適用（純後端任務）

### 判定：附保留核准
建置與測試都是綠燈，D-01／D-02 已經確實修好並有測試，邊界也乾淨，沒有阻塞的 OQ。
保留事項與接手者：
- R1 OQ-T-04-be-board-membership-01（活動紀錄措辭的切換規則）：接手＝無（等人工／CR 裁決）
- R2 OQ-T-04-be-board-membership-02（刪除 Board 後留下的孤兒 membership）：接手＝無（等人工／CR 裁決）
- R3 OQ-T-04-be-board-membership-03（非 Owner 移除成員時沿用的訊息）：接手＝無（等人工／CR 裁決）
- R4 OQ-T-04-be-board-membership-04（Viewer 讀取／寫入的切分、403 與訊息文字）：接手＝無（等人工／CR 裁決）
- R5 OQ-T-04-be-board-membership-05（看板列表要不要列出 Viewer 的看板）：接手＝無（等人工／CR 裁決）；前端 T-12 會直接沿用現況
- R6 OQ-T-04-be-board-membership-06（F01 卡片端點沒有成員／Viewer 檢查）：接手＝人工
