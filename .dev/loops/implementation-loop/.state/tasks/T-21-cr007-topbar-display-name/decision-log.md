# T-21-cr007-topbar-display-name 決策紀錄與交接摘要

> 由 `loopctl log` 追加，不要直接編輯。

## 2026-09-19 Dev 第 1 輪：CR-007 TopBar 改顯示 display-name

### 這輪做了什麼

依 CR-007（`.dev/CR.md`、`.dev/F02-user-membership/spec-user-membership.md` `uc-login` post、`ui-user-membership.md` `s-login`）把 TopBar 顯示的欄位從 `user.username` 改成 `user.display-name`：

- 後端（`kanban-spring`）：`SessionResponse` 新增 `displayName` 欄位；`UserController` 登入成功時把 `user.getDisplayName()` 一併存進 `HttpSession`（新增 `SESSION_DISPLAY_NAME_ATTRIBUTE`），`GET /api/session` 一併回傳。`POST /api/login` 回應（`UserResponse`）本來就有 `displayName`，未改。
- 前端（`kanban-frontend`）：`authApi.SessionResponse` 補 `displayName`；`AuthState`／`AuthContext` 新增 `displayName` 並在 `fetchSession`／`login`／`logout` 三處同步設定；`AppShell` TopBar 改顯示 `displayName` 而非 `username`。
- 測試：
  - `kanban-spring/src/test/resources/features/user-login-logout.feature` 補上 spec 第 227～234 行的新 Scenario「登入後 TopBar 顯示的是顯示名字而不是帳號 ID」（逐字複製，tag 依既有實作慣例（參照 `stage-management.feature` `@added @CR-003 @uc-set-stage-role`）拿掉 `@wip`、保留 `@added @CR-007 @uc-login`；spec 本體不動，`@wip` 何時拿掉由人工在 CR-007 處理完成後決定）。
  - `UserSteps.java` 新增對應 Given／When step definition（`系統中存在帳號 {string}，顯示名字為 {string}，密碼為 {string}`、`我以帳號 {string} 與密碼 {string} 登入`），`thenTopBarShowsName` 斷言鍵從 `username` 改成 `displayName`（既有 Scenario "user1" 未指定顯示名字，兩欄同值，不受影響，仍通過）。
  - 前端 `App.test.tsx`／`LoginPage.test.tsx` 的 mock 改用「帳號 ID 與顯示名字不同值」（"alice"/"小美"、"user5"/"王小明"），並加 `queryByText(username)).not.toBeInTheDocument()` 斷言，避免測試在改動前後都通過而測不出差異。

### 涵蓋範圍

- `entity`：`user`（唯讀）。`uc-`：`uc-login`（本次變更的 post）、`uc-logout`（未變動，維持既有行為）。
- Scenario：spec 第 217～253 行四個 Scenario 全數覆蓋，含新增的 CR-007 Scenario；`ui-user-membership.md` `s-login` 三條涉及 TopBar 的驗收條件（正確登入顯示 `display-name`、帳號不存在/密碼錯誤時不顯示任何使用者）皆有對應測試。
- 沒有延後或跳過的項目——這個任務範圍明確（只改 `SessionResponse`／TopBar 顯示欄位），無 OQ。

### 待確認事項

無新增 OQ。CR-007 本身的決策（顯示 `display-name`）已由人工在 OQ-IMPL-11 解除說明定案，本輪只是照做。

### Check（實際跑的指令與結果）

- `./gradlew clean build --no-daemon`：BUILD SUCCESSFUL（含 `kanban-core`／`kanban-spring` 單元測試與 Cucumber，`user-login-logout.feature` 報告確認新 Scenario 有執行且無 failed）。
- `kanban-frontend`：`pnpm test -- --run`（`vitest run`）：4 test files、16 tests 全過；`npx tsc -b`：exit 0；`npx oxlint`：exit 0。
