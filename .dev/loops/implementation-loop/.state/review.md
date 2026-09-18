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
