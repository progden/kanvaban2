# T-21-cr007-topbar-display-name 審查紀錄

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Review 第 1 輪：退回

### 驗證結果（實際跑的指令）

1. 建置／測試：
   - `./gradlew clean build --no-daemon`：BUILD SUCCESSFUL（2m 2s）；`TEST-feature_classpath_features-user-login-logout.feature.xml` tests="5" failures="0" errors="0"，含新 Scenario「登入後 TopBar 顯示的是顯示名字而不是帳號 ID」。
   - `kanban-frontend`：`pnpm test -- --run` 4 files／16 tests 全過；`npx tsc -b` exit 0；`npx oxlint` 無輸出。
2. spec 對應：`uc-login` 四個 Scenario（含 `@CR-007` 新增一則）都有 step definition；新 Scenario 的 Gherkin 與 spec 第 222～229 行逐字一致（只拿掉 `@wip`，依既有 `@CR-003` 慣例，可接受）。抽查 `@fail-p2`「密碼錯誤時登入失敗」：通過，且 `assertSessionUnauthenticated` 以 `GET /api/session` 401 確認資料／登入態不變。前端 `App.test.tsx`／`LoginPage.test.tsx` 改用帳號 ID 與顯示名字不同值並斷言帳號 ID 不出現，能測出差異。
   - **缺口**：CR-007 驗收標準『`GET /api/session` 回應帶顯示名字』沒有任何後端測試守住（新 Scenario 讀的是 `POST /api/login` 回應，前端的 `/api/session` 是 mock）。刪掉 `UserController` 裡 `setAttribute(SESSION_DISPLAY_NAME_ATTRIBUTE, …)` 那行，全部測試仍會通過。→ D-01。
3. `kanban-core` 純度：`grep -rn "springframework\|jakarta.persistence" kanban-core/src/main` 無結果；本任務未動 `kanban-core`。
4. 邊界：`git diff loop/implementation...HEAD --stat` 只有 `kanban-frontend`（auth／AppShell／兩個測試）、`kanban-spring`（SessionResponse／UserController／UserSteps／user-login-logout.feature）與 `.state/tasks/T-21-cr007-topbar-display-name/**`，都在 tasks.md 允許範圍內；未動 spec／ui／conventions／scripts／tasks.md。
5. OQ：Dev 表示無待確認事項，`loopctl show` 亦無 OQ；檢查後同意本任務沒有需要開 OQ 的規格疑義（CR-007 已定案）。
6. 前端視覺：本任務只改 TopBar 顯示欄位；版面依設計稿重做屬 T-22-fe-restyle-shell-auth 範圍，本輪不以設計稿退回。產品畫面未出現 Attribute ID／`uc-` 註記。

### 判定：退回（doing）

理由：CR-007 明列的驗收標準之一（`GET /api/session` 帶顯示名字）沒有測試覆蓋，屬於任務交付內容未完成。修正項見 D-01。其餘項目皆通過，D-01 修好後預期可核准。

## 2026-09-19 Review 第 2 輪：核准

### 驗證結果（本輪實際跑的指令）

1. 建置／測試：
   - `./gradlew clean build --no-daemon`：完成後再跑 `./gradlew build --no-daemon` 顯示 `BUILD SUCCESSFUL in 9s`；`kanban-spring/build/test-results/test/*.xml` 合計 28 個測試、全部 `failures="0"`；`TEST-feature_classpath_features-user-login-logout.feature.xml`：`tests="5" skipped="0" failures="0" errors="0"`。
   - `kanban-frontend`：`pnpm test -- --run`：Test Files 4 passed、Tests 16 passed；`npx tsc -b` exit 0；`npx oxlint` 無輸出（前端本輪未改動，仍重跑確認）。
2. D-01 核對：本輪只改 `UserSteps.thenTopBarShowsName`，在斷言 `POST /api/login` 回應後，用同一個 `MockHttpSession`（`performLogin` 也是 `.session(session)`）呼叫 `GET /api/session`，斷言 200 且 `displayName` 等於預期名稱。
   - 讀 `UserController.currentSession`：`displayName` 只從 `session.getAttribute(SESSION_DISPLAY_NAME_ATTRIBUTE)` 取得，所以刪掉 `login()` 的 `setAttribute(SESSION_DISPLAY_NAME_ATTRIBUTE, …)` 後 `displayName` 會是 null，兩個 Scenario（"user1" 與 "user5"／"王小明"）都會失敗。這和 Dev 回報的迴歸驗證結果一致；這條驗收標準現在有測試守住。
   - "user1" 的 Scenario 驗證了 `GET /api/session` 的 `displayName`＝"user1"，對應 `s-login` 驗收條件『未指定顯示名字的帳號，顯示的值等於 `user.username`』。
   - 和 D-01 字面要求的差異：沒有另外斷言 `GET /api/session` 的 `username`＝"user5"。`username` 欄位在本任務之前就存在，不是 CR-007 驗收標準的內容；另外，如果 `SessionResponse` 兩個欄位順序對調，"王小明" 的 Scenario 也會失敗。判定不影響核准。
3. spec 對應：`user-login-logout.feature` 新 Scenario 和 spec 第 230～234 行逐字一致（只拿掉 `@wip`，第 1 輪已確認可以接受）；`uc-login` 四個 Scenario 都有 step definition。`@fail-p2` 在第 1 輪抽查過，本輪沒有改到相關 step，而且測試仍然全數通過。
4. `kanban-core` 純度：`grep -rn "springframework\|jakarta.persistence" kanban-core/src/main` 無結果；本任務沒有改 `kanban-core`。
5. 邊界：`git diff loop/implementation...HEAD --name-only` 只列出 `kanban-frontend/**`、`kanban-spring/**`（SessionResponse／UserController／UserSteps／user-login-logout.feature）與 `.state/tasks/T-21-cr007-topbar-display-name/**`，範圍外檔案 0 個；沒有改到 spec／ui／conventions／scripts／tasks.md／archive。
6. OQ：`loopctl show` 無 OQ；Dev 兩輪交接摘要的「待確認事項」都是「無」。我檢查後同意：CR-007 已經定案，沒有需要人工決定的規格疑義。
7. 前端視覺：TopBar 只把 `username` 換成 `displayName`，產品畫面沒有出現 Attribute ID／`uc-` 註記。依設計稿重做版面屬於 T-22-fe-restyle-shell-auth 的範圍。

### 判定：核准（done）

D-01 已修好，建置和前後端測試都綠燈，spec 對應沒有缺漏，改動沒有超出任務邊界，也沒有 OQ。

### 後續事項（依 tasks.md 備註，不是本任務的保留事項）

- 把 `.dev/CR.md` CR-007 改成「處理完成」，並拿掉 spec 的 `@added @wip`：接手者＝人工（`tasks.md` T-21 備註『完成並驗收後，人工把 `.dev/CR.md` CR-007 改「處理完成」並清掉 spec 的 `@added @wip`』）。
