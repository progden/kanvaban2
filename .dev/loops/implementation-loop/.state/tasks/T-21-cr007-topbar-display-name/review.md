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
