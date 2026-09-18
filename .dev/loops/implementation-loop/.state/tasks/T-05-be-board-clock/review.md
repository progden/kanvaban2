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
