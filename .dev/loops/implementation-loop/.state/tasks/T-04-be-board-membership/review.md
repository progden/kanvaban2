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
