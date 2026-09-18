# review

> 只能追加。Review sub agent 的核准／退回紀錄，核准列必須引用實際跑過的建置／測試指令與結果，不是複述 Dev 的自我回報。

## 2026-09-18 T-00-scaffold：核准

審查對象：`impl/T-00-scaffold` 分支 HEAD `4e4c48a`（相對整合分支 `loop/implementation` 的 merge-base `294dfc3`，含 Dev commit `7125403`、`4fc0f1d`、`4e4c48a`）。

1. **建置／測試（Review 自己重跑，不採信 Dev 的 Check 欄）**
   - `./gradlew clean build --no-daemon`：`BUILD SUCCESSFUL in 40s`，13 個 task 都有執行。測試報告 `kanban-core/build/test-results/test/*.xml`：`NoSpringDependencyTest` tests="1" failures="0" errors="0"；`kanban-spring`：`KanbanApplicationSmokeTest` tests="1" failures="0" errors="0"。
   - `cd kanban-frontend && pnpm install --frozen-lockfile`：`Already up to date`（lockfile 跟 package.json 一致）；`pnpm run build`（`tsc -b && vite build`）：`✓ built in 637ms`；`pnpm run test`（`vitest run`）：`Test Files 1 passed (1)`、`Tests 1 passed (1)`。
2. **spec 對應**：這個任務依 `tasks.md` 的定義是『無業務邏輯』，不涵蓋任何 `uc-xxx`／Scenario，所以沒有 Cucumber step 或 `@fail-pN` 可以抽查（不適用）。`kanban-spring` 已加入 Cucumber 7.20.1 依賴（`cucumber-java`／`cucumber-spring`／`cucumber-junit-platform-engine`），但還沒有 runner，留給第一個有 Scenario 的任務（T-01）建立。
3. **kanban-core 純度**：`grep -rn "springframework\|jakarta" kanban-core/src` 只在 `NoSpringDependencyTest` 的字串常數裡找到一處，main source 沒有這類 import；`kanban-core/build.gradle.kts` 只有 JUnit 測試依賴。目前還沒有 `io.progden.kanban.query.*` 投影（不適用）。
4. **任務邊界**：`git diff --stat 294dfc3..HEAD` 共 35 個檔案，都在根目錄 Gradle 設定（`build.gradle.kts`／`settings.gradle.kts`／`gradle.properties`／`gradlew*`／`gradle/wrapper/**`）、`.gitignore`、`kanban-core/**`、`kanban-spring/**`、`kanban-frontend/**`，以及 `.dev/loops/implementation-loop/.state/**` 三個狀態檔。`git diff 294dfc3..HEAD -- .github .dev scripts CLAUDE.md` 除了 `.state/**` 沒有其他檔案，沒動到 `.dev/conventions/**`、`scripts/**`、spec／ui／design 本體。
5. **待確認事項／OQ**：Dev 交接（`state.md`）沒有留待確認事項；`open-questions.md` 的 OQ-IMPL-01～08 都標「已解除」，沒有影響本任務的未決 OQ。
6. **前端**：只有 Vite react-ts 範本的最小 `App`（`<div>kanban-frontend scaffold</div>`），沒有做任何畫面，不涉及設計稿或 `ui-*.md` 操作表（不適用）。

**不擋核准的觀察（給後續任務參考，不另開 D-xx）**：
- `NoSpringDependencyTest` 的 Javadoc 寫的是『kanban-core 不得依賴 Spring／JPA』，但實際只檢查 `org.springframework.context.ApplicationContext` 能不能載入，**沒有檢查 `jakarta.persistence`**。這一條純度仍靠每輪 Review 手動 grep 把關；T-01 以後如果要補，可以在同一個測試加一個 `jakarta.persistence.Entity` 的斷言。
- `kanban-frontend/src/index.css`、`README.md`、`public/icons.svg` 是 Vite 範本原檔（紫色 accent 等），不是依設計稿做的樣式。T-10-fe-shell 開始做畫面時，要照設計稿換掉，不要沿用範本的視覺。
- 任務定義寫『CI 可跑 build/test』，但 repo 目前沒有任何 CI 設定（沒有 `.github/workflows`）。本輪把它解讀成「指令可以無互動執行」（上面兩組指令都可以），已達成；`scripts/verify.sh` 裡的 build/test TODO 屬於人工權限範圍，本 loop 不能改。

判定：**核准**，`tasks.md` T-00-scaffold 狀態改成 `done`。

## 2026-09-18 T-00-scaffold：重新驗證（維持核准）

Review 被再次觸發時，任務已經是 `done`（核准 commit `ae66801`）。`git diff --stat 4e4c48a..HEAD` 扣掉 `.state/**` 之後沒有任何異動，所以只重跑建置和測試：
- `./gradlew clean build --no-daemon`：exit 0；`kanban-core`、`kanban-spring` 的測試報告都是 tests="1" failures="0" errors="0"。
- `kanban-frontend`：`pnpm install --frozen-lockfile` 回報 `Already up to date`，`pnpm run build` 顯示 `✓ built`，`pnpm run test` 結果為 `Tests 1 passed (1)`。
- `grep -rn "import org.springframework\|import jakarta" kanban-core/src/main`：沒有結果。

判定：維持核准，狀態仍是 `done`，不新增 D-xx。

## 2026-09-18 T-00-scaffold：第三次重新驗證（維持核准）

Review 第三次被觸發，任務仍是 `done`（HEAD `b9c40a4`）。`git diff --stat 4e4c48a..HEAD` 只有 `.state/**` 四個檔案，程式碼跟第一次核准時一樣，只重跑建置和測試：
- `./gradlew clean build --no-daemon -q`：exit 0；`kanban-core`、`kanban-spring` 的測試報告都是 tests="1" skipped="0" failures="0" errors="0"。
- `kanban-frontend`：`pnpm install --frozen-lockfile` 顯示 `Already up to date`，`pnpm run build` 顯示 `✓ built in 646ms`，`pnpm run test` 結果為 `Test Files 1 passed (1)`、`Tests 1 passed (1)`。
- `grep -rn "import org.springframework\|import jakarta" kanban-core/src/main`：沒有結果（exit 1）。
- `git diff --stat 294dfc3..HEAD -- .dev scripts CLAUDE.md .github`：只有 `.state/**`，沒有範圍外的改動。

判定：維持核准，狀態仍是 `done`，不新增 D-xx。這個任務已經重複觸發 Review 兩次，看起來驅動腳本沒有把 `done` 任務合併回 `loop/implementation`，或合併後沒有停止排程 Review，建議人工確認 `run-loop.sh` 的合併步驟。

## 2026-09-18 T-01-be-user：退回（第 1 輪）

審查對象：`impl/T-01-be-user` 分支 HEAD `4d8afbf`（和整合分支 `loop/implementation` 的 merge-base 是 `9469e5f`，包含 Dev commit `18eba65`、`4d8afbf`）。

1. **建置／測試（Review 自己重跑）**
   - `./gradlew clean build --no-daemon`：`BUILD SUCCESSFUL in 1m 45s`，14 個 task 都有執行，exit 0。
   - 測試報告 `kanban-*/build/test-results/test/*.xml`：`NoSpringDependencyTest` tests="1"、`UserTest` tests="8"、Cucumber「建立使用者帳號」tests="6"、「使用者登入與登出」tests="4"、`KanbanApplicationSmokeTest` tests="1"，全部 failures="0" errors="0" skipped="0"。
2. **spec 對應**
   - 把 `spec-user-membership.md` 兩個 Feature 的 Gherkin 抽出來，和 `kanban-spring/src/test/resources/features/{create-user-account,user-login-logout}.feature`（扣掉第一行說明註解）做 `diff`，結果沒有差異，是逐字複製。10 個 Scenario 都有對應的 step definition（`UserSteps.java`），而且都經由 MockMvc 打到真正的 web → application → persistence 路徑。
   - 抽查 `@uc-create-user @fail-p1`（密碼 41 字）：`User.create` 先檢查長度，丟出 `PASSWORD_TOO_LONG`，沒有呼叫 `save`；step 驗證了訊息 `"密碼長度不可超過 40 個字"`，也驗證 `userJpaRepository.count()` 和動作前一樣，「拒絕、訊息、資料不變」三件事都有斷言。
   - 抽查 `@uc-create-user @fail-p2`：先 `existsByUsername`，再由 `User.create` 丟 `USERNAME_ALREADY_EXISTS`，也不會 `save`；count 不變，行為成立。
   - `uc-login` p1／p2 都回 `"帳號或密碼錯誤"`，而且 `/api/session` 仍然回 401，符合 fail 描述。`uc-login` post『TopBar 顯示該 `user` 的帳號名稱』：回應的 `username` 對得上。
   - **問題（D-02）**：欄位表 `user.username` 是『非空、全系統不可重複』，但 `User.create` 只檢查「不可重複」，沒檢查「非空」。`uc-create-user` 的 pre 也沒有這一條，所以規格本身有缺口，Dev 卻沒有登記 OQ。
3. **kanban-core 純度**：`grep -rn "import org.springframework\|import jakarta" kanban-core/src/main` 沒有結果。`User`／`DomainException`／`ErrorCode`／`UserRepository` 都放在 `io.progden.kanban.core.domain`，只用到 `java.util`。這個任務沒有跨 aggregate 的讀取投影（不適用）。
4. **任務邊界**：`git diff --stat 9469e5f..HEAD` 共 27 個檔案，除了 `.state/**` 三個狀態檔，都在 `kanban-core/**`、`kanban-spring/**`。沒動到 `.dev/conventions/**`、`scripts/**`、spec／ui／design 本體和 `CLAUDE.md`，也沒碰 `board`／`board-membership`。
5. **待確認事項／OQ**：**不通過（D-01、D-02）**。交接摘要寫『沒有待確認事項／沒有新開 OQ』，但實際上有兩件事屬於 `iteration-prompt.md` 第 5 節的「高風險」：(a) 失敗情境的 HTTP 狀態碼是自己決定的（400／409／401／204），第 5 節逐字把『某個失敗情境該回什麼 HTTP 狀態碼』當成高風險的例子；(b) 上面第 2 點的 username 非空缺口。這兩件事都沒有寫進 `open-questions.md`，也沒有寫在交接摘要。
6. **前端**：不適用（純後端任務）。

**不擋這次判定的觀察（不另開 D-xx，給 Dev／後續任務參考）**：
- `uc-logout` post 是『不重新登入即無法存取任何 `board`』。目前還沒有 board 端點，所以 step「我應該無法在不重新登入的情況下存取 Board」是用 `GET /api/session` 回 401 間接驗證的。T-02-be-board 實作 board 端點時，要讓未登入的請求被拒絕，並把這個 step 改成真的去打 board 端點。
- 兩個請求同時用同一個 username 建帳號時，`existsByUsername` 檢查完到寫入之間有時間差，後寫入的那筆會撞到 DB unique constraint，丟出 `DataIntegrityViolationException`（應該會變成 500），不會回 `"此帳號已被使用"`。這是低風險，可以之後再補轉換。
- `decision-log.md`「低風險技術決定」那則寫『spec 明確排除「真正的認證安全機制」』，但這句話的出處是 `design-user-membership.md` 第 78 行『密碼雜湊／真正的認證安全機制（spec 明確排除，本次疊代範圍不含）』。spec 本身的排除清單逐字是『本次疊代範圍排除：帳號註冊流程的其他細節（例如信箱驗證）、拒絕邀請情境（邀請即生效，沒有「待接受」的中間狀態）。』，裡面沒有提到密碼雜湊。密碼用明碼儲存這件事，依據是 design 文件，不是 spec。建議 Dev 在處理 D-01／D-02 時順便更正這則的引用出處（design 和 spec 之間的落差要人工判斷，本 loop 不能改這兩份文件）。

判定：**退回**。`tasks.md` 的 T-01-be-user 狀態改回 `doing`，並追加 D-01、D-02（這是第 1 輪退回，`MAX_TASK_ROUNDS=6`）。程式碼本身的行為和測試沒有發現錯誤，D-01 只要求補 OQ 登記；D-02 要求補 OQ，並且不可以自己編錯誤訊息。之後 OQ 如果仍未決，依規則只能附保留核准，不能核准成不帶保留的 `done`。

## 2026-09-18 T-01-be-user：附保留核准（第 2 輪）

審查對象：`impl/T-01-be-user` 分支 HEAD `c1d0afa`（merge-base `9469e5f`）。`git diff --stat 4d8afbf..HEAD` 只有 `.state/**` 五個檔案，程式碼跟第 1 輪審查時相同。

1. **建置／測試（Review 自己重跑）**
   - `./gradlew clean build --no-daemon`：`BUILD SUCCESSFUL in 1m 45s`，`14 actionable tasks: 14 executed`，exit 0。
   - 測試報告 `kanban-*/build/test-results/test/*.xml`：`NoSpringDependencyTest` tests="1"、`UserTest` tests="8"、「建立使用者帳號」tests="6"、「使用者登入與登出」tests="4"、`KanbanApplicationSmokeTest` tests="1"，全部 skipped="0" failures="0" errors="0"。
2. **spec 對應**：用 Python 從 `spec-user-membership.md` 抽出 `Feature: 建立使用者帳號`、`Feature: 使用者登入與登出` 兩個 gherkin 區塊，跟 `kanban-spring/src/test/resources/features/{create-user-account,user-login-logout}.feature`（扣掉開頭註解行）比對，兩份都相同（Scenario 數 6／4）。fail 情境的「資料不變」仍由 `UserSteps.java` 第 156、161 行的 `assertEquals(countBeforeAction, userJpaRepository.count())` 斷言。第 1 輪的抽查結論（`@uc-create-user @fail-p1`／`@fail-p2`、`uc-login` p1／p2）因為程式碼沒變，所以仍然成立。
3. **kanban-core 純度**：`grep -rn "import org.springframework\|import jakarta\|@Entity\|@Autowired" kanban-core/src/main` 沒有結果（exit 1）。
4. **任務邊界**：`git diff --stat 9469e5f..HEAD -- .dev scripts CLAUDE.md .github` 只有 `.state/**` 五個檔案；其餘 24 個檔案都在 `kanban-core/**`、`kanban-spring/**`。
5. **待確認事項／OQ**：D-01／D-02 已經處理。我到源頭文件逐字核對 OQ-IMPL-09、OQ-IMPL-10 的引文：`spec-user-membership.md` 第 28 行欄位表、第 95～102 行 `uc-create-user` pre／fail、第 180～181 行 `uc-login` fail、第 191 行 `uc-logout` post，以及 `ui-user-membership.md` 第 26、33、78 行，都跟 OQ 裡的引文一致。OQ 寫的程式碼事實也核對過：`UserController.statusFor` 是 409／400／401，logout 回 `noContent()`，`/api/session` 未登入回 401；`User.create` 沒有檢查 username 是否為空。
   - 等級判斷（這是我的推論）：兩則 OQ 都屬於 `iteration-prompt.md` 第 5 節的「高風險」（『spec 的 `pre`／`post`／Scenario 沒講清楚該怎麼實作』），不屬於「覆蓋來源」。OQ-IMPL-10 是 spec 內部的欄位表和 `pre` 沒對齊，不是『spec 定案內容跟現有程式碼結構衝突』；不管最後結論是哪一個選項，都只是在 `User.create` 加一個檢查、加一則訊息，不會推翻既有結構。所以這個任務不標 `blocked`，但只能附保留核准。
6. **前端**：不適用。

**保留事項（核准附帶條件，定案前不可當成已完成的規格行為）**：
- (R1) OQ-IMPL-09 待處理：`POST /api/users`／`POST /api/login`／`POST /api/logout`／`GET /api/session` 的 HTTP 狀態碼（400／409／401／204／401）是實作推論，還沒定案。T-10-fe-shell 串 API 時，請用回應內容判斷錯誤，不要寫死依賴特定狀態碼。
- (R2) OQ-IMPL-10 待處理：目前 `username: ""` 會建立成功，`username: null` 會在寫入 DB 時變成未轉換的例外，這跟欄位表『非空』有已知落差。F02 spec 檔頭是 `狀態：定稿`（第 15 行），如果 OQ 選 A（補 `pre p3`／`fail p3`），要先開 CR 才能改 spec，之後回到本 aggregate 補程式碼和 Scenario。
- (R3) 第 1 輪提過的觀察還沒處理，但都不擋核准：`uc-logout` 的「無法存取 board」目前是用 `/api/session` 間接驗證的，T-02-be-board 要改成真的去打 board 端點；同名同時建立帳號的競態會變成 500；decision-log 裡「spec 明確排除認證安全機制」的出處其實是 `design-user-membership.md`，不是 spec。另外，`/api/session` 未登入時回的 `"尚未登入"` 這段訊息 spec 沒有定義，一併歸到 OQ-IMPL-09 處理。

判定：**附保留核准**。`tasks.md` 的 T-01-be-user 狀態改成 `done`，可以合併回 `loop/implementation`；不新增 D-xx。

## 2026-09-18 T-10-fe-shell：退回（第 1 輪）

審查對象：`impl/T-10-fe-shell` 分支 HEAD `050324b`（merge-base `8e8432b`）。

1. **建置／測試（Review 自己重跑）**
   - `cd kanban-frontend && pnpm install --frozen-lockfile`：`Already up to date`。
   - `pnpm run test`（`vitest run`）：`Test Files 2 passed (2)`、`Tests 7 passed (7)`，exit 0。
   - `pnpm run build`（`tsc -b && vite build`）：`✓ built in 879ms`。
   - `pnpm run lint`（`oxlint`）：exit 0。
   - 根目錄 `./gradlew clean build --no-daemon`：exit 0（確認後端沒被影響）。
2. **spec 對應**：這個任務不包含完整的 `uc-`／Scenario，但 AppShell 的 TopBar 會觸發 `uc-logout`。對照 `ui-user-membership.md` 第 79 行操作表：『登出 | `uc-logout` | 回到本畫面（`s-login`） | 不適用（`uc-logout` 無 fail 定義） | 否』。程式碼中登出不會跳確認框（「需確認？」＝否），登出後 `ProtectedRoute` 導向 `/login`，`App.test.tsx`「已登入時…登出後回到登入畫面」有測到這段。第 93 行驗收條件『已登入狀態下觸發登出動作，觸發 `uc-logout`，並回到本畫面』也成立。未登入導向 `/login`、已登入時擋掉 `/login` 也都有測試。這個任務沒有 `@fail-pN` 可以抽查，因為 `uc-logout` 沒有 fail；登入的 fail 屬於 T-11。`authApi.ts` 的四個端點與欄位，逐一對過 `UserController` 和 `LoginRequest`／`CreateUserRequest`／`UserResponse`／`SessionResponse` 四個 record，都一致。
3. **kanban-core 純度**：本任務沒動 `kanban-core`；`grep -rn "import org.springframework\|import jakarta\|@Entity\|@Autowired" kanban-core/src/main` 沒有結果（exit 1）。
4. **任務邊界**：`git diff --stat loop/implementation...HEAD` 共 23 個檔案，除了 `.state/**` 三個檔，其餘都在 `kanban-frontend/**`。沒動 `.dev/conventions/**`、`scripts/**`、spec／ui／design 本體。
5. **待確認事項／OQ**：**不通過（D-03、D-04）**。
   - (a) TopBar 顯示的名稱沒有依據。`ui-user-membership.md` 第 78 行逐字寫『TopBar 顯示帳號名稱』，第 90 行寫『成功後 TopBar 顯示該使用者名稱』。同一份檔案第 26 行把 `user.username` 標為『帳號 ID』，第 27 行把 `user.display-name` 標為『顯示名字』。spec 第 29 行 `user.display-name` 的說明是『顯示名字，看板上顯示用』。【我的推論】「帳號名稱／使用者名稱」都沒有明確對應到其中一個欄位，Dev 自己選了 `username`，屬於 `iteration-prompt.md` 第 5 節的「高風險」：『spec 的 `pre`／`post`／Scenario 沒講清楚該怎麼實作』。而且如果結論是 `display-name`，還要改 T-01 的 `SessionResponse`，但交接摘要寫『待確認事項／新開 OQ：無』。這跟 T-01 第 1 輪被退回的原因相同。
   - (b) 交接摘要、`tasks.md` 備註、`decision-log.md` 都說 OQ-IMPL-09『仍待處理／未定案』。實際上 OQ-IMPL-09 在 `06c0a14` 已經解除並升級為 ADR-001，時間早於本任務的 Dev commit `4614e17`。紀錄跟事實不符。程式碼本身沒問題（`ApiError` 有保留 `status`），但「呼叫端依訊息內容判斷」這個說法，要跟 ADR-001『前端／API 使用者可以只看狀態碼就做基本分流』的立場對齊。
   - 【我的推論】這兩件都屬於「高風險／紀錄正確性」，不是「覆蓋來源」，所以不標 `blocked`。
6. **前端設計稿**：`planning-prompt.md` 的畫面清單中沒有獨立的 shell 或 TopBar 畫面（Login／Signup 等屬於 T-11 以後的任務）。Dev 沒有加任何樣式，四個佔位頁也都註明由哪個任務接手，沒有自己發明視覺風格。通過。

**不擋這次判定的觀察（不另開 D-xx）**：
- `AppShell.tsx` 用 `void logout()` 呼叫登出。如果 `POST /api/logout` 發生網路錯誤，會變成 unhandled rejection，畫面也沒有任何反應。`uc-logout` 沒有定義 fail，所以不算違反規格，之後可以考慮補上。
- `AuthProvider` 在 `fetchSession` 遇到任何錯誤（包括 500 或網路錯誤）都當成未登入，這是合理的預設。
- `open-questions.md` 的 OQ-IMPL-10 結尾還留著一行舊的『狀態：待處理。…』，跟上面的『狀態：已解除』互相矛盾。這是 CR-006 處理時留下的，不在本任務範圍內，請人工或 Planning 清掉。

判定：**退回**。`tasks.md` 的 T-10-fe-shell 狀態改回 `doing`，追加 D-03、D-04（第 1 輪退回，`MAX_TASK_ROUNDS=6`）。程式碼不需要修改；D-03 只要求登記 OQ，D-04 只要求更正紀錄。

## 2026-09-18 T-10-fe-shell：附保留核准（第 2 輪）

審查對象：`impl/T-10-fe-shell` 分支 HEAD `bca59de`（merge-base `8e8432b`，也就是 `loop/implementation` 目前的 HEAD）。`git diff --stat 89d416e..HEAD` 只有 `.state/**` 四個檔案（decision-log、open-questions、state、tasks），程式碼跟第 1 輪審查時相同。

1. **建置／測試（Review 自己重跑）**
   - `cd kanban-frontend && pnpm install --frozen-lockfile`：`Already up to date`。
   - `pnpm run test`（`vitest run`）：`Test Files 2 passed (2)`、`Tests 7 passed (7)`，exit 0。
   - `pnpm run build`（`tsc -b && vite build`）：`✓ built in 1.17s`。
   - `pnpm run lint`（`oxlint`）：exit 0。
   - 根目錄 `./gradlew clean build --no-daemon -q`：exit 0。
2. **spec 對應**：程式碼沒變，第 1 輪的結論仍然成立：`uc-logout` 對應 `ui-user-membership.md` 第 79 行操作表（需確認＝否、回到 `s-login`）與第 93 行驗收條件，有 `App.test.tsx` 覆蓋；`uc-logout` 沒有 fail，登入的 `@fail-p1`／`@fail-p2` 屬於 T-11，本任務沒有 `@fail-pN` 可以抽查。
3. **kanban-core 純度**：本任務沒動 `kanban-core`；`grep -rn "import org.springframework\|import jakarta\|@Entity\|@Autowired" kanban-core/src/main` 沒有結果（exit 1）。
4. **任務邊界**：`git diff --stat loop/implementation...HEAD` 共 25 個檔案，除了 `.state/**` 五個檔，其餘 20 個都在 `kanban-frontend/**`。沒動 `.dev/conventions/**`、`scripts/**`、spec／ui／design 本體、`CLAUDE.md`、`kanban-core/**`、`kanban-spring/**`。
5. **待確認事項／OQ**：D-03、D-04 已處理。
   - D-03：OQ-IMPL-11 已登記。我到源頭逐字核對引文：`ui-user-membership.md` 第 26、27、78、90 行，`spec-user-membership.md` 第 28、29 行，都跟 OQ 裡的引文一致。程式碼事實也核對過：`SessionResponse` 只有 `username`，`AppShell.tsx` 顯示 `useAuth()` 的 `username`。**缺漏**：OQ 沒有引用 spec 本身的 `uc-login` post（第 189 行『"登入成功，TopBar 顯示該 `user` 的帳號名稱"』）和 Scenario 第 223 行『And TopBar 應該顯示我的名稱 "user1"』。我已在 OQ-IMPL-11 底下追加一段「Review 補充」，逐字補上這兩處（Dev 寫的內容沒改）。【我的推論】補上的原文沒有改變問題本身，Scenario 也區分不了 A／B（`display-name` 沒指定時等於 `username`），所以不為這個缺漏再退回一輪。
   - D-04：`decision-log.md` 新增的更正條目已核對：`adr.md` 第 44 行『身分驗證失敗或未登入 | 401 Unauthorized』、第 49 行『狀態碼判斷語意分類，錯誤代碼判斷精確情境』，跟更正內容一致；`tasks.md` T-10 備註、`state.md` 的過期字樣已改掉。
   - 等級判斷（這是我的推論）：OQ-IMPL-11 屬於 `iteration-prompt.md` 第 5 節的「高風險」（『spec 的 `pre`／`post`／Scenario 沒講清楚該怎麼實作』），不屬於「覆蓋來源」。不管選 A 或 B，都只是換一個顯示欄位，最多在 `SessionResponse` 加一個欄位，不會推翻既有結構。所以不標 `blocked`，只能附保留核准。
6. **前端設計稿**：同第 1 輪，沒有自己發明樣式，通過。

**保留事項（核准附帶條件）**：
- (R1) OQ-IMPL-11 待處理：TopBar 目前顯示 `user.username`，是實作推論，還沒定案。如果定案為 `user.display-name`，要改 `kanban-frontend/src/layout/AppShell.tsx`、`auth` 相關型別，以及 T-01 的 `SessionResponse`（已合併，需要另開修正）。
- (R2) 第 1 輪提過的觀察還沒處理，但不擋核准：`AppShell.tsx` 的 `void logout()` 在網路錯誤時會變成 unhandled rejection。
- (R3) 範圍外的紀錄問題，請人工或 Planning 清理：`open-questions.md` OQ-IMPL-10 結尾還有一行過期的『狀態：待處理。…』；`tasks.md` 的 T-01-be-user 備註還寫著『OQ-IMPL-09（HTTP 狀態碼）、OQ-IMPL-10（`user.username` 非空）仍待處理』，但兩則都已經解除。

判定：**附保留核准**。`tasks.md` 的 T-10-fe-shell 狀態改成 `done`，可以合併回 `loop/implementation`；不新增 D-xx。

## 2026-09-18 T-11-fe-auth：退回（第 1 輪）

審查對象：`impl/T-11-fe-auth` 分支 HEAD `73c07f0`（merge-base `f057f94`，程式碼 commit `e8eb7dd`）。

1. **建置／測試（Review 自己重跑）**
   - `cd kanban-frontend && pnpm install --frozen-lockfile`：`Already up to date`。
   - `pnpm test`（`vitest run`）：`Test Files 4 passed (4)`、`Tests 16 passed (16)`，exit 0。
   - `pnpm build`（`tsc -b && vite build`）：`✓ built in 777ms`，exit 0。
   - `pnpm lint`（`oxlint`）：exit 0。
   - 根目錄 `./gradlew build -q`：exit 0。
2. **spec 對應**（對照 `ui-user-membership.md` s-login 第 51～97 行、s-signup 第 5～50 行，以及 `spec-user-membership.md` 的 `uc-create-user`／`uc-login`）
   - 行為本身：`LoginPage` 登入失敗時只設定錯誤訊息，欄位 state 不清空、不導頁，`AuthProvider` 的登入態不變，符合 `uc-login` fail p1／p2 的『`user.username` 與 `user.password` 不變，我仍停留在登入頁面』。`SignupPage` 對空帳號（`trim() === ''`）、密碼超過 40 字在送出前擋下，訊息跟 spec Scenario 的『"使用者名稱不能為空"』『"密碼長度不可超過 40 個字"』逐字一致，也跟 `kanban-core` `User.java` 第 31～32 行的 `isBlank()` 判斷一致（空白字串兩邊都拒絕）。顯示名字留白時送空字串，由後端 `User.java` 第 41 行補成 `username`，符合 post『`user.display-name` 未指定時預設為 `user.username`』。
   - 抽查 @fail-p2（`uc-login`，密碼錯誤）：測試斷言訊息『帳號或密碼錯誤』、兩個欄位值保留、「登入」標題還在，成立。後端 `UserApplicationService.java` 第 31、33 行的訊息跟 spec 逐字一致。
   - **缺漏 1（D-05）**：`ui-user-membership.md` 第 45 行寫『帳號 ID 與系統中既有帳號重複時確認建立帳號，輸入內容保留、顯示訊息，且不觸發 `uc-create-user`』，實作仍會打 `POST /api/users`。Dev 在 decision-log 自己把它解讀成「措辭疊加」並歸為低風險，但這是在詮釋驗收條件的字面意思。依 `iteration-prompt.md` 第 58～59 行，這屬於高風險，要開 OQ，不能自己定案。【我的推論】Dev 的解讀可能是對的，因為前端沒有全體帳號清單，但這要人工確認，不是 Dev 自己說了算。
   - **缺漏 2（D-06）**：第 91、92 行驗收條件的『TopBar 不顯示帳號名稱』兩個失敗測試都沒有斷言；fail-p1（帳號不存在）的測試也沒有斷言欄位保留、停留本畫面。
   - **缺漏 3（D-07）**：第 43 行『畫面維持顯示』在「帳號 ID 為空」的測試裡沒有斷言。
   - 其他驗收條件（登入成功後 TopBar 顯示名稱並導向 Board 列表、前往建立帳號不打 API、密碼留白建立成功並導向 `s-login`、密碼超過 40 字不打 API）都有測試覆蓋，行為也相符。
3. **kanban-core 純度**：本任務沒動 `kanban-core`；`grep -rn "import org.springframework\|import jakarta\|@Entity\|@Autowired" kanban-core/src/main` 沒有結果。
4. **任務邊界**：`git diff --stat f057f94..HEAD` 共 9 個檔案，4 個在 `.state/**`，5 個在 `kanban-frontend/src/**`（`pages/LoginPage*`、`pages/SignupPage*`、`App.test.tsx`）。`App.test.tsx` 屬於 T-10 的範圍，但只把選取器從『登入畫面』改成 heading 選取器，因為本任務取代了佔位元件，行為斷言沒變。我接受這個改動。沒動 `.dev/conventions/**`、`scripts/**`、spec／ui／design 本體、`CLAUDE.md`、後端。
5. **待確認事項／OQ**：OQ-IMPL-12（設計稿無法存取）已登記在 `open-questions.md`，引文我到 `dev-prompt.md`、`planning-prompt.md` 第 19～20 行核對過，跟原文一致；屬於環境限制，不是覆蓋來源。D-05 所說的解讀問題沒有登記成 OQ，見上面第 2 點。
6. **前端設計稿**：`planning-prompt.md` 第 19～20 行列出 `Login.dc.html`／`Signup.dc.html`，Dev 看不到實際設計稿，也沒有自己發明樣式：用的是純語意 HTML，在檔頭註明「待對照設計稿」，並登記了 OQ-IMPL-12，符合 `dev-prompt.md` 的規則。「需確認？」欄：s-signup 的「確認建立帳號」寫『是（本畫面即確認…）』，按鈕文字就是「確認建立帳號」，符合；s-login 兩個操作都是「否」，符合。「失敗時」欄：用 `role="alert"` 顯示訊息、輸入內容保留，符合。

判定：**退回**。`tasks.md` 的 T-11-fe-auth 狀態改回 `doing`，追加 D-05～D-07（第 1 輪退回，`MAX_TASK_ROUNDS=6`）。D-05 只要求登記 OQ，程式碼不改；D-06、D-07 只要求補測試斷言，產品程式碼預期不用改。
