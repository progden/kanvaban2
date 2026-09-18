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
