# T-05-be-board-clock 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Review 第 1 輪：退回

### 1. 建置與測試（Review 自己跑）
- `./gradlew clean build --no-daemon` → BUILD SUCCESSFUL（2m 31s）。
- 統計 `*/build/test-results/test/*.xml`：kanban-core 47 tests / 0 failures / 0 errors；kanban-spring 46 tests / 0 failures / 0 errors（含 board-clock.feature、card-editing、create-user-account、stage-management、swimlane-management、user-login-logout，以及 smoke、BoardApplicationServiceTest）。
- 結果：綠燈。

### 2. spec 對應
- 把 `board-clock.feature` 跟 spec 的 gherkin 區塊 diff，除了檔頭兩行註解之外逐字一致，8 個 Scenario 都有 step definition。
- 抽查 `@uc-guard-clock-monotonicity @fail-p1`：`BoardClock.recordEventTime` 在檢查不通過時直接丟出例外，不會改 `lastEventAt`；`addCard` 在 `Card.create` 成功後才存 board，所以卡片沒有建立、board 也沒有變，錯誤訊息的「（13:00）」用 systemDefault 時區格式化，跟 step 解析時用的時區一致。→ 符合。
- 抽查 `@uc-adjust-board-clock @fail-p1`：非建立者呼叫會得到 403 和訊息「只有 Owner 可以調整看板時間」，看板時間不變。→ 符合（Owner 判斷怎麼做，見 OQ）。
- 問題 → D-01：`CardApplicationService` 在 card 的 domain 驗證**之前**就把推進後的 `lastEventAt` 存進 board，而且這個類別沒有 `@Transactional`。例如空白留言被拒絕，board 的單調性基準卻已經前進了，之後的 guard 判斷會引用一個不存在的「最後一筆事件」。
- 問題 → D-02：「調整看板時間應記錄一筆活動紀錄」的 Then 步驟沒有斷言日期時間參數，也沒檢查操作人，post 第 2 條實際上沒被驗到。
- 觀察（不列為 D-xx，spec 沒定義）：`pauseClock` 在已經暫停時仍會寫一筆活動紀錄；`resumeClock` 在非暫停狀態時不動作。兩邊處理方式不一致，Dev 修 D-01／D-02 時可以順便讓兩邊一致，並寫進 decision-log。

### 3. kanban-core 純度
- `grep -rnE "import (org\.springframework|jakarta\.persistence)|@Entity|@Autowired" kanban-core/src/main` → 沒有結果。`BoardClock` 是 package-private 的值物件，`Instant.now()` 只在 kanban-spring 呼叫。→ 通過。本任務沒有跨 aggregate 的讀取投影。

### 4. 任務邊界
- `git diff loop/implementation...HEAD --stat`：改動限於 kanban-core 的 domain（Board／BoardClock／BoardClockSnapshot／ClockStatus／Card／Comment／ErrorCode）和對應的測試，kanban-spring 的 Board／Card application、web、persistence 和測試，以及 `.state/tasks/T-05-be-board-clock/**`。Card／Comment 的改動屬於 CR-004「事件時間改用 Board Clock」的範圍（tasks.md：「改寫 board/card 事件的 `occurredAt` 來源」）。沒有動到 spec／ui／conventions／scripts／tasks.md／archive。→ 通過。

### 5. OQ 核對
- OQ-T-05-be-board-clock-01：spec 的 pre／fail 引文和 `BoardApplicationService` 類別註解的引文都跟原文逐字比對過，一致。等級「高、不阻塞」判斷正確：在 `board-membership` 上線前，用 createdBy 代理跟現況等價，不需要違反定稿原文。
- **接手填錯**：tasks.md 的 T-04 依賴只有 T-02／T-01／T-03，不含 T-05；它現在是 doing，與 T-05 並行；備註把可以改動的範圍限定在「T-02 的九個結構調整端點」等三點。所以 T-04 看不到、也不被允許修改看板時鐘的 `requireOwner`。已另開 OQ-T-05-be-board-clock-02（高、不阻塞、接手：人工）取代 01 的接手欄，並補上引用 spec 決議紀錄『權限檢查由呼叫端查「BoardMembership」後決定是否呼叫「Board」的方法』。
- 交接摘要的「跳過／延後」：Owner 正式查詢 → 已開 OQ；前端呈現 → T-17-fe-clock-control（tasks.md 確實有這一列，依賴 T-05）。

### 6. 前端
- 不適用（後端任務）。

### 判定：退回（第 1 輪 < 上限 6）
- D-01：卡片寫入失敗時仍推進並存檔 `lastEventAt`（非原子操作）。
- D-02：調整時鐘的活動紀錄 Then 步驟沒有驗到指定時間和操作人。
- 保留事項：OQ-T-05-be-board-clock-02（接手：人工），核准時必須列入附保留事項。

## 2026-09-19 Review 第 2 輪：附保留核准

### 1. 建置與測試（Review 自己跑）
- `git status --short` → 工作區乾淨。
- `./gradlew clean build --no-daemon` → BUILD SUCCESSFUL（2m 18s，14 tasks executed）。
- 統計 `*/build/test-results/test/*.xml`：kanban-core 47 tests / 0 failures / 0 errors / 0 skipped；kanban-spring 47 tests / 0 failures / 0 errors / 0 skipped（較第 1 輪多出 `CardApplicationServiceTest` 1 個）。
- 結果：綠燈。

### 2. spec 對應與 D-xx 核對
- D-01（卡片寫入非原子操作）→ 已修好。看了 commit 0ef0492 的 diff：`CardApplicationService` 的 `editCard`／`moveCardSwimlane`／`moveCardStage`／`addComment`／`deleteCard` 都改成先 `loadBoard`、`board.newEventTime(...)`，等 `Card` 的 domain 方法成功之後才 `boardRepository.save(board)`、`cardRepository.save(card)`；原本會提前存 board 的私有方法 `newEventTimeFor` 已刪除；六個寫入方法（含 `addCard`）都標了 `@Transactional`。`DomainException extends RuntimeException`，所以預設就會回滾。`BoardRepositoryAdapter.save` 是把 domain 快照寫回 JpaEntity，domain 物件跟 JPA managed entity 是分開的，沒有呼叫 save 就不會因為 dirty checking 被寫回去。新增的 `CardApplicationServiceTest.留言內容為空白被拒絕時board的最後事件基準不推進` 會斷言：空白留言丟出 `EMPTY_COMMENT_CONTENT` 之後，重新讀出來的 `lastEventAt` 等於失敗之前的值、卡片沒有留言、之後還能建立卡片。修正前 `addComment` 會把 `lastEventAt` 推進到真實的 now 並存檔，這個等值斷言在修正前一定會失敗，所以測試確實驗到了缺陷。D-01 條件 2 原本寫的是「把看板時間調早之後建立卡片」這個流程，Dev 改成直接斷言 guard 用來比較的 `lastEventAt` 沒有前進；這是同一個不變量，而且斷言更直接，接受。
- D-02（調整時鐘活動紀錄的 Then 步驟太弱）→ 已修好。`thenActivityRecordedForAdjust` 現在用 `parseNarrative(date, time)` 解析步驟參數，再用 `assertCloseTo` 比對最新一筆活動紀錄的 `occurredAt`，並用 `assertEquals` 比對 `operatorId` 等於 `boardSteps.getCurrentUserId()`；暫停／恢復兩個 Then 也補上了 `operatorId` 斷言。調整動作的活動紀錄走 `recordClockActivity`（時間取 `clock.now()`，也就是調整之後的新時間），所以用 `occurredAt` 比對到的就是「被調整成的時間」。Dev 決定比對結構化欄位、不解析 `action` 字串，已寫進 decision-log，屬於低風險技術決定，接受。
- 8 個 Scenario 第 1 輪已經逐字比對過 spec（沒有變動），這一輪都跑過、綠燈。`@uc-guard-clock-monotonicity @fail-p1`（拒絕、資料不變）第 1 輪已經抽查過，這一輪的改動沒有碰到 `addCard` 的呼叫順序（`Card.create` 還是在 `boardRepository.save` 之前），結論不變。
- 第 1 輪提到的觀察（重複暫停會記錄活動、非暫停狀態呼叫恢復則不做事）：`design-board-clock.md` 第 6 節逐字寫著『`Board.resumeClock` 改為時鐘不是暫停狀態時直接返回，不呼叫 `BoardClock.resume`、不新增活動紀錄；`pauseClock` 在已暫停時的行為維持不變（`BoardClock.pause` 本身不會出錯）』。現在的程式碼跟設計備忘一致，spec 的 `uc-pause-resume-board-clock` `fail: {}` 也沒有定義這個情境，所以不列為保留事項。

### 3. kanban-core 純度
- `grep -rnE "import (org\.springframework|jakarta\.persistence)|@Entity|@Autowired" kanban-core/src/main` → 沒有結果。本任務沒有跨 aggregate 的讀取投影。→ 通過。

### 4. 任務邊界
- `git diff loop/implementation...HEAD --name-only`，排除 `kanban-core/`、`kanban-spring/`、`.state/tasks/T-05-be-board-clock/` 之後 → 沒有任何檔案。第 2 輪只改了 `CardApplicationService.java`、新增 `CardApplicationServiceTest.java`、改了 `BoardClockSteps.java`，都在本任務範圍內（CR-004 改寫 card 事件的時間來源）。沒有動到 spec／ui／design／conventions／scripts／tasks.md／archive／其他任務目錄。→ 通過。

### 5. OQ 核對
- Dev 第 2 輪交接摘要寫「沒有新發現需要另開 OQ 的事項」，`loopctl show` 列出的 OQ 只有 01、02，跟摘要一致。
- OQ-T-05-be-board-clock-01／02：第 1 輪已經逐字核對過引文，02 取代了 01 錯填的接手欄。等級「高、不阻塞」成立：用 `board.createdBy` 代理 Owner，不用違反任何定稿原文就能完成本任務。這兩則都沒有「覆蓋」或「環境」等級的阻塞事項。

### 6. 前端
- 不適用（後端任務）。

### 判定：附保留核准（done）
保留事項與接手者：
1. OQ-T-05-be-board-clock-02（接手：人工）：T-04 和 T-05 都合併之後，要把 `uc-adjust-board-clock`／`uc-pause-resume-board-clock` 的 Owner 檢查從 `board.createdBy` 代理改成依 spec 決議紀錄查 `board-membership`。目前沒有任何任務的產出範圍涵蓋這件事，需要人工補任務或調整範圍。
2. OQ-T-05-be-board-clock-01（接手欄原本寫 T-04-be-board-membership，已由 OQ-02 取代，實際接手：人工）：跟 02 是同一個問題，人工處理 02 的時候一併關閉。
