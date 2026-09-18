# T-21-cr007-topbar-display-name 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | done | — | 後端缺少驗證 `GET /api/session` 回應帶 `displayName` 的自動化測試。 - 依據：`.dev/CR.md` CR-007 驗收標準『新增的 Scenario 由 Cucumber 驗證通過；`GET /api/session` 回應帶顯示名字，`kanban-frontend` 的 TopBar 顯示它』。 - 現況：`UserController.session()` 改成從 `HttpSession` 讀 `SESSION_DISPLAY_NAME_ATTRIBUTE` 回傳，但 `kanban-spring/src/test/**` 只有 `UserSteps.assertSessionUnauthenticated()` 呼叫 `/api/session`（只斷言 401）；新 Scenario 的 `thenTopBarShowsName` 讀的是 `POST /api/login` 的回應（`UserResponse`，本來就有 `displayName`）。前端 `App.test.tsx` 的 `/api/session` 是 mock。也就是說：把 `session.setAttribute(SESSION_DISPLAY_NAME_ATTRIBUTE, ...)` 那行刪掉，後端與前端測試全部仍會通過，但重新整理頁面後 TopBar 會是空白——這條驗收標準沒有被任何測試守住。 - 怎樣算修好：在 `kanban-spring` 補一個測試（可以是 `UserSteps` 裡 `TopBar 應該顯示我的名稱` 同時對 `GET /api/session` 斷言 `displayName`，或另寫 MockMvc／WebMvc 測試；不可改 spec 的 Gherkin 文字），登入 "user5"／"王小明" 後以同一 session 呼叫 `GET /api/session`，斷言 200 且 `displayName` 為 "王小明"、`username` 為 "user5"；並確認未指定顯示名字的 "user1" 取得的 `displayName` 等於 "user1"（對應 `s-login` 驗收條件『未指定顯示名字的帳號，顯示的值等於 `user.username`』）。`./gradlew clean build --no-daemon` 綠燈。 |
