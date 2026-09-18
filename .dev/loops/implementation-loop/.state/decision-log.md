# decision-log

> 只能追加。每輪（Dev／Review／安排階段）結束前至少追加一則，記錄「這輪做了什麼判斷、為什麼」，取代文件 loop 慣用的 PDCA 格式——實作階段要留的是**決策與理由**，不是計畫執行的四階段。
> 影響範圍跨任務、或往後的任務都要遵守的結構性選擇，額外在 [`adr.md`](adr.md) 開一筆 ADR，這裡的條目用 `ADR: ADR-xxx` 欄位引用它；只影響單一任務內部的技術選擇，寫在這裡就好，不必開 ADR。

## 格式

```
### <date> T-xx（Dev｜Review｜Planning）
- 決策：<這輪做了什麼判斷／選擇>
- 理由：<為什麼這樣選，若有考慮過其他做法一併寫>
- 影響：<影響到哪些檔案／任務／之後的實作>
- ADR：<ADR-xxx，若有開 ADR；沒有則省略此行>
```

低風險等級的決策（見 `iteration-prompt.md` 第 5 節）也要記，但可以只寫「決策」「理由」兩行，不必展開「影響」。

### 2026-09-18 T-06/T-07/T-08/T-15～T-20/T-18（校正，Planning）
- 決策：把 T-06、T-07、T-08 從 `blocked` 改回 `todo`；原前端 T-15／T-16／T-17（現改編號 T-18／T-19／T-20）從 `blocked` 改為 `todo` 並改依賴（從誤植的 `T-13-fe-board-detail` 改為新設的 `T-13-fe-canvas-shell`）；新增 `T-13-fe-canvas-shell`、`T-16-fe-activity-log`、`T-17-fe-clock-control` 三個原本遺漏的任務；解除 OQ-IMPL-01～06，新增 OQ-IMPL-07。
- 理由：安排階段最初讀到 F03／F05／F06 的「名詞定義」表是空的，誤判成「spec 尚未遷移」（依據錯誤的 CLAUDE.md 舊版敘述）；實際上 `spec-migration-loop` 已完成（`spec-migration-state.md`：『F01～F06 全部完成，全檔 0 error』），三個模組本來就「只做讀取投影，不新增 aggregate」（`CLAUDE.md` 逐字），表格空白是正確定案狀態。同理，`ui-*.md` 也已由 `ui-authoring-loop` 補齊全部七個模組，不再只有 F01。此外 `ui-authoring-loop` 的 OQ-49（人工已確認）把 F01 `s-board`、F03 四個儀表板、F04 時鐘控制、F05 工作量儀表板、F06 追蹤表全部定為 F07 `s-canvas` 上的獨立 item，原本「Canvas 依賴其他畫面」的方向是反的。
- 影響：`tasks.md` 全面重排（見該檔 2026-09-18 校正說明）；`open-questions.md` OQ-IMPL-01～06 標記已解除並附解除說明；新開 OQ-IMPL-07 追蹤 `item.component` 對應值與 `canvas` 建立時機這兩個 spec 本身仍待整合 CR 的缺口，不影響 T-09／T-13 已定義範圍的動工。
- ADR：無（這是任務清單內容的校正，不是跨任務的架構決策；Canvas-centric 架構本身已由 ui-authoring-loop 的 OQ-49 定案，不需要本 loop 重新開 ADR）。

### 2026-09-18 OQ-IMPL-07 佐證與 OQ-IMPL-08 新增（Planning）
- 決策：用使用者提供的前端設計稿（`Main.dc.html`／`CanvasPanel.dc.html`）交叉核對 OQ-IMPL-07，確認沒有遺漏可用的 uc 定義（mockup 引用的十個 `uc-*` 全部存在於 `spec-canvas-layout.md`／F01／F02）；設計稿本身也明確標注「item.component ＝ 看板本體（⚠️ 值未定案，見 spec-canvas-layout 待釐清）」，與 spec、`ui-canvas-layout.md`「待確認事項」三方一致，OQ-IMPL-07 維持待解除。額外發現 `ui-canvas-layout.md`「待確認事項」還有一條角色對應缺口（呼應 ui-authoring-loop 的 OQ-44），原 OQ-IMPL-07 沒涵蓋，新開 OQ-IMPL-08。
- 理由：使用者要求「檢查有沒有缺漏」，設計稿是可核對的具體來源，交叉比對比單看 spec 文字更能抓到遺漏；OQ-44 的角色缺口會直接影響 T-09／T-13 的權限檢查邏輯，屬於同一類「spec 待整合、不可腦補」的缺口，值得獨立追蹤而不是併進 OQ-IMPL-07（兩者選項與影響範圍不同）。
- 影響：`tasks.md` T-09／T-13 備註補充角色對應缺口與 `ui-canvas-layout.md` 現況（狀態仍「討論中」）；`open-questions.md` 新增 OQ-IMPL-08。
- ADR：無。

### 2026-09-18 OQ-IMPL-07／OQ-IMPL-08 人工決策，直接補規格（Planning）
- 決策：人工決策兩件事並直接寫回 `spec-canvas-layout.md`（草稿階段，不需 CR）：(1) canvas 於使用者第一次開啟 Board 時自動建立，同時若無任何 item 則自動放置看板本體 item（`item.component`＝`board`，新增 `uc-init-canvas`／Feature「看板畫布初始化」）；(2) `r-canvas-editor`／`r-canvas-viewer` 對應 `board-membership.role` Owner/Member／Viewer（即 F02 `r-board-owner`／`r-board-member`／`r-board-viewer`）。`open-questions.md` 的 OQ-IMPL-07／08 標記已解除，`tasks.md` T-09／T-13 備註同步更新。
- 理由：人工判斷 item.component 該用哪個值時，一開始提議沿用 ui 層的 `s-board`，但 `spec-check` 直接報錯（REF-01：spec 不能引用 ui 的 Screen ID，違反 `docs-convention.md` 第 3 節單向引用規則），改用 F01 已定義的實體 ID `board` 解決；角色對應則是人工直接判斷 Owner/Member＝editor、Viewer＝viewer，且因為 F02 在稍早（2026-09-18 稍早的 commit）已新增 `r-board-viewer` 角色，映射可以是正式的三對三對應，不用再標「暫定」。
- 影響：`spec-canvas-layout.md` 兩則變更紀錄；T-09、T-13 兩個任務原本備註的缺口全部解除，T-14～T-20 間接受益（依賴的 T-13 不再卡在缺規格）。
- ADR：無（規格本身的決定記在 `spec-canvas-layout.md` 變更紀錄，這裡只記 loop 如何回應）。

### 2026-09-18 T-00-scaffold（Dev）
- 決策：`kanban-frontend` 選用 pnpm + Vite + React + TypeScript（`pnpm create vite@latest kanban-frontend --template react-ts`），並加裝 vitest + @testing-library/react 作為測試框架；`kanban-spring` 依賴 Spring Boot 4.1.1 實際發佈到 Maven Central 的模組化 autoconfigure 套件（`spring-boot-jdbc`／`spring-boot-hibernate`，類別套件改為 `org.springframework.boot.jdbc.autoconfigure`／`org.springframework.boot.hibernate.autoconfigure`，不是舊版 `org.springframework.boot.autoconfigure.jdbc`／`orm.jpa`）；kanban-core／kanban-spring 皆用 Java 25 toolchain。
- 理由：CLAUDE.md／spec／ui-*.md 都沒有指定前端框架，這屬於「技術實作細節、不違反 spec」的低風險決定（iteration-prompt.md 第 5 節），pnpm 是唯一被指定的套件管理工具；Spring Boot 4.1.1 的 autoconfigure 套件路徑用 `unzip -l` 實際核對 Maven Central 下載下來的 jar 內容才發現已模組化搬遷，不是憑記憶假設舊版路徑，避免腦補。
- 影響：後續所有前端任務（T-10 起）都建立在 Vite + React + TS 之上；後續後端任務若用到 `DataSourceAutoConfiguration`／`HibernateJpaAutoConfiguration` 等 Boot 4.1 autoconfigure 類別，要注意套件已搬到 `org.springframework.boot.<starter>.autoconfigure`，不是舊路徑。`kanban-core` 新增 `NoSpringDependencyTest` 作為架構守門測試，往後任務若不慎在 `kanban-core` 引入 Spring 依賴會被這個測試擋下。
- ADR：無（純技術選型，未違反 spec，不影響 aggregate 邊界或 port 設計）。

### 2026-09-18 T-00-scaffold（Review）
- 決策：核准，狀態從 `review-pending` 改成 `done`。
- 理由：Review 自己重跑 `./gradlew clean build` 和前端的 `pnpm install --frozen-lockfile`／`pnpm run build`／`pnpm run test`，全部通過；任務不涵蓋 uc／Scenario，spec 對應不適用；`kanban-core` main source 沒有 Spring／JPA import；diff 範圍只有骨架檔案和 `.state/**`；沒有未決 OQ。`NoSpringDependencyTest` 沒檢查 JPA、前端還留著 Vite 範本樣式、repo 沒有 CI 設定，這三件事判定不影響「骨架可建置、無業務邏輯」這個驗收範圍，記在 `review.md` 給後續任務參考，沒有開 D-xx。
- 影響：觸發 `impl/T-00-scaffold` 合併回 `loop/implementation`；T-01-be-user（以及依賴鏈上的其他任務）的依賴解除。沒有留給 Dev 的 D-xx。

### 2026-09-18 T-00-scaffold（Review 重新驗證）
- 決策：維持核准，狀態不動（仍是 `done`）。
- 理由：上次核准後程式碼沒有異動；重跑 Gradle 建置和前端的 build／test 都通過，`kanban-core` 也仍然沒有 Spring／JPA import。
- 影響：跟上次核准相同（等待合併回 `loop/implementation`），沒有新的 D-xx。

### 2026-09-18 T-00-scaffold（Review 第三次重新驗證）
- 決策：維持核准，狀態不動（仍是 `done`）。
- 理由：程式碼從核准後沒有異動；重跑 Gradle 建置和前端 build／test 都通過，`kanban-core` main source 仍然沒有 Spring／JPA import，diff 範圍也沒有變。
- 影響：仍在等合併回 `loop/implementation`，沒有新的 D-xx。已經是 `done` 的任務又被觸發 Review，可能是驅動腳本合併步驟沒有執行，請人工檢查。

### 2026-09-18 T-01-be-user（Dev）
- 決策：範圍只做 `spec-user-membership.md`「建立使用者帳號」「使用者登入與登出」兩個 Feature（`uc-create-user`／`uc-login`／`uc-logout`），不碰 `board`／`board-membership`／`card.assignees`（任務定義明講「不含 board-membership」，且 `board`、`board-membership` 是 T-02／T-04 的範圍）；`design-user-membership.md` 描述的 `BoardMembership`／`Card.assignTo` 等「實作狀態」是跨多個未來任務的整體設計備忘，本輪未實作。
- 理由：依 `tasks.md` 任務顆粒度規則（一列＝一個 Aggregate Root），`user` 是獨立 Aggregate；`design-user-membership.md` 的「實作狀態」段落雖然寫得像已完成，但 `kanban-core`／`kanban-spring` 目前除了 scaffold 只有 `package-info.java`／`NoSpringDependencyTest.java`，本輪確認過那段文字是設計預告，不是既成程式碼，不可以照抄成「已完成」。
- 影響：`kanban-core` 新增 `User`／`DomainException`／`ErrorCode`（3 個值：`USERNAME_ALREADY_EXISTS`／`PASSWORD_TOO_LONG`／`INVALID_CREDENTIALS`）／`UserRepository`（port）；`kanban-spring` 新增 `application.UserApplicationService`、`persistence.{UserJpaEntity,UserJpaRepository,UserRepositoryAdapter}`、`web.UserController`（`POST /api/users`／`POST /api/login`／`POST /api/logout`／`GET /api/session`，以 `HttpSession` 保存登入狀態）。後續 T-02／T-04 若要新增 `ErrorCode` 值或 `Board`／`BoardMembership`，會編輯同一個 `ErrorCode.java`，合併時留意衝突。
- ADR：無（單一任務內的技術選型，未跨 aggregate）。

### 2026-09-18 T-01-be-user（Dev，低風險技術決定）
- 決策：登入狀態用 Spring 內建 `HttpSession`（無 Spring Security），不做密碼雜湊；`kanban-spring` 資料庫層第一次出現，主要設定：正式環境 `spring.jpa.hibernate.ddl-auto=update`（暫無 Flyway／DDL 腳本）、測試改用 `src/test/resources/application.yml` 覆蓋成 H2（`MODE=PostgreSQL`），取代原本 `KanbanApplicationSmokeTest` 排除 DataSource／Hibernate 自動組態的寫法。
- 理由：spec／CLAUDE.md 都沒指定要用 Spring Security 或密碼雜湊（spec 明確排除「真正的認證安全機制」），HttpSession 足以支撐「TopBar 顯示帳號名稱」「登出後無法存取 Board」兩個驗收條件；`ddl-auto=update` 是過渡手段，之後導入 Flyway 時要換掉。
- 影響：`kanban-spring/src/main/resources/application.yml` 新增 datasource／jpa 設定（env var 預設值，未連真正 Postgres）；`kanban-spring/src/test/resources/application.yml` 新增 H2 測試設定；`KanbanApplicationSmokeTest` 移除原本的自動組態排除（該排除是 T-00 特意留給「第一個定義 persistence 的任務」處理，見該測試 Javadoc）；`build.gradle.kts` 新增 `com.h2database:h2`（testRuntimeOnly）。
- ADR：無。

### 2026-09-18 T-01-be-user（Dev，環境限制發現）
- 決策：記錄一個跟本任務範圍無關但影響後續所有 `kanban-spring` 任務的環境事實：Spring Boot 4.1.1 除了先前已知的 autoconfigure 套件模組化（見 T-00 決策），MockMvc 測試支援也搬到新模組 `org.springframework.boot:spring-boot-webmvc-test`（類別套件 `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`，不是舊版 `org.springframework.boot.test.autoconfigure.web.servlet`），而且**預設 Jackson 是 Jackson 3**（`tools.jackson.databind.ObjectMapper`，groupId `tools.jackson.core`，不是舊版 `com.fasterxml.jackson.databind`）；兩者都是實際解壓 jar 內容核對出來的，不是憑記憶假設。
- 理由：撰寫 Cucumber step definitions 時直接手寫 import 導致編譯失敗，逐一核對 Maven Central 下載下來的 jar 內容才找到正確套件路徑；記錄下來避免後續任務（T-02 起）重複踩同樣的坑。
- 影響：所有需要 MockMvc／`@AutoConfigureMockMvc` 的測試都要 `testImplementation("org.springframework.boot:spring-boot-webmvc-test")`；所有需要手動操作 JSON（`ObjectMapper`／`readValue`／`writeValueAsString`）的程式碼都要 import `tools.jackson.*`，不是 `com.fasterxml.jackson.*`（`readValue`／`writeValueAsString` 拋的是 unchecked 的 `tools.jackson.core.JacksonException`，不再是 checked `IOException`）。
- ADR：無（技術環境事實記錄，非架構決策；但影響範圍夠廣，Review 或後續任務讀 decision-log 時應留意）。

### 2026-09-18 T-01-be-user（Review）
- 決策：退回，狀態從 `review-pending` 改回 `doing`，追加 D-01（HTTP 狀態碼選擇要登記成高風險 OQ）、D-02（`user.username`『非空』限制沒落實，要登記 OQ）。
- 理由：第 1 點，Review 自己重跑 `./gradlew clean build --no-daemon`，建置成功，5 個測試報告共 20 個測試全過；第 2 點，兩份 feature 檔和 spec 的 Gherkin `diff` 過是逐字一致，抽查的 fail-p1／fail-p2 都確實做到「拒絕、訊息、資料不變」，但欄位表的『非空』沒有對應實作，也沒有 OQ；第 3 點，`kanban-core` 沒有 Spring／JPA import；第 4 點，diff 範圍乾淨；第 5 點不通過，失敗情境的 HTTP 狀態碼正是 `iteration-prompt.md` 第 5 節點名的高風險例子，Dev 卻回報「沒有待確認事項」，OQ 登記有遺漏；第 6 點不適用。
- 影響：不合併回 `loop/implementation`；T-02／T-04／T-10 繼續等待。下一輪 Dev 在同一個 worktree 處理 D-01、D-02，不需要改動既有業務邏輯，除非 D-02 的 OQ 另有結論。

### 2026-09-18 T-01-be-user（Dev，處理 D-01／D-02）
- 決策：只補登記兩則 OQ（`open-questions.md` OQ-IMPL-09、OQ-IMPL-10），不改動任何 `kanban-core`／`kanban-spring` 程式碼；`user.username` 為空的防護維持現狀（不加暫行防護），因為還沒有依據可以決定拒絕訊息文字，怕先寫一個訊息反而變成既成事實，之後 OQ 有結論時還要再改一次。
- 理由：D-01 本身已明講「程式碼可以不改，這一項是補登記」；D-02 允許先做暫行防護但非必須，且 review 特別強調「不可以自己編一個錯誤訊息當成定案」——目前兩個候選方向（新增 `pre p3`／或解讀成僅資料庫層 NOT NULL）對「使用者看到什麼訊息」的答案完全不同，任何暫行寫法都等於替 OQ 預設了一個答案，選擇不動code風險更低。OQ-IMPL-09 的情況分類用【推論＋所本原文】（HTTP 狀態碼是程式碼既有推論、spec 沒訂），OQ-IMPL-10 用【兩處矛盾並列】（欄位表『非空』vs. `pre p2` 沒有非空、且 ui 檔的引用本身對不上它引用的 `pre p2` 原文）。
- 影響：`open-questions.md` 新增 OQ-IMPL-09、OQ-IMPL-10（皆「待處理」）；`tasks.md` D-01／D-02 改為 `done`，母任務 T-01-be-user 轉回 `review-pending`；`state.md` 覆寫本輪摘要。下一輪 Review 依規則只能附保留核准（兩則 OQ 皆待處理），不能核准成不帶保留的 `done`。
- ADR：無（單一任務內的登記補正，非架構決策）。

### 2026-09-18 T-01-be-user（Review 第 2 輪）
- 決策：附保留核准，狀態從 `review-pending` 改成 `done`，保留事項是 OQ-IMPL-09、OQ-IMPL-10 兩則都還待處理。
- 理由：第 1 點，Review 自己重跑 `./gradlew clean build --no-daemon`，exit 0，5 份報告共 20 個測試全部通過；第 2 點，兩份 feature 檔跟 spec 的 Gherkin 程式比對後相同，fail 情境的「資料不變」有斷言；第 3 點，`kanban-core/src/main` 沒有 Spring／JPA import；第 4 點，範圍外只有 `.state/**`；第 5 點，D-01／D-02 已處理，OQ 引文逐字核對跟源頭一致，兩則都判定為高風險（不是覆蓋來源），所以附保留核准，不標 blocked；第 6 點不適用。
- 影響：`impl/T-01-be-user` 可以合併回 `loop/implementation`，T-02／T-04／T-10 的依賴解除。沒有新的 D-xx。OQ-IMPL-09／10 定案後，如果要改狀態碼或加 username 非空檢查，要回頭修 `user` aggregate（OQ-10 選 A 要先走 CR，因為 F02 spec 已定稿）。

### 2026-09-18 OQ-IMPL-09 定案，升級為 ADR-001（Planning）
- 決策：人工決策維持 T-01 目前採用的 HTTP 狀態碼對應（選項 A），並把它從單一任務的 OQ 解除說明升級成 `adr.md` 的 ADR-001，補上完整的語意分類表（400/401/403/404/409/204），不是只記錄「T-01 用了什麼」，而是「以後所有任務都要照這張表」。
- 理由：這件事的影響範圍不是 T-01 自己——T-02～T-09 每個 web 端點都要決定 `fail` key 對應什麼狀態碼，如果每個任務各自開 OQ 各自決定，會出現同語意不同狀態碼的不一致；升級成 ADR 讓後續 Dev／Review 有一張表可以直接查，不用每次重新討論。
- 影響：`open-questions.md` OQ-IMPL-09 標記已解除並指向 ADR-001；`adr.md` 新增 ADR-001。
- ADR：ADR-001。

### 2026-09-18 OQ-IMPL-10 定案，走 CR-006（Planning）
- 決策：人工決策採選項 A：`uc-create-user` 補上 `pre.p3`（`user.username` 非空）與對應 `fail.p3`，新增 Scenario；因為 F02 spec 是「定稿」狀態，正式開 **CR-006** 走完整流程（記錄→修改規格→處理完成），不是直接改。同時訂正 OQ-IMPL-10 原本「兩處矛盾並列」的分類——人工確認 spec 裡「可留白」只針對 `user.password`，`user.username` 沒有任何一處講允許留空，這其實是覆蓋範圍缺口（欄位表定了規則、`pre`/`fail` 沒操作化），不是真的矛盾。
- 理由：F02 已定稿，依 `cr-convention.md` 第 1 節「規格已定稿，要修改 usecase 區塊（含 pre/post/fail）」一律要開 CR，即使只是補一條前置條件也算；T-01-be-user 已經合併完成，不透過 worktree pipeline 重開這個已完成的任務，改由人工直接完成規格＋程式碼的小型修正（CR-006 影響範圍小，明細直接寫在 `.dev/CR.md` 總表，沒開明細檔）。
- 影響：`.dev/CR.md` 新增 CR-006（處理完成）；`spec-user-membership.md` 的 `uc-create-user` 與變更紀錄；`kanban-core`（`ErrorCode.USERNAME_BLANK`、`User.create` 新增檢查、2 個新單元測試）；`kanban-spring`（`UserController.statusFor` 新增對應、Cucumber feature／step）；`open-questions.md` OQ-IMPL-10 標記已解除並訂正分類。`./gradlew clean build`、`spec-check`、`cr-check --cr CR-006` 皆已重新驗證通過。
- ADR：無（沿用既有 ADR-001 的 400 分類，不是新的跨任務決策）。

### 2026-09-18 T-10-fe-shell（Dev）
- 決策：範圍只做「app shell」本身——路由骨架（React Router 7）、API client（`src/api/http.ts`＋`src/api/authApi.ts`）、登入態管理（`AuthProvider`／`useAuth`，掛載時打 `GET /api/session` 判斷登入態）、`ProtectedRoute`／`GuestOnlyRoute` 兩種路由守衛、`AppShell`（全域 TopBar，顯示帳號名稱＋登出，依 `ui-user-membership.md` 「全域導覽列」的描述）。`s-login`／`s-signup`／`s-board-list`／F07 `s-canvas` 四個畫面本體都只放最小佔位元件（`LoginPage`／`SignupPage`／`BoardListPage`／`BoardCanvasPage`，各自標註「待 T-11／T-12／T-13 實作」），不自行發明任何表單欄位或版面，留給對應任務依 `ui-*.md` 補上。
- 理由：任務定義明講「路由、API client、登入態管理」三件事，且 T-11／T-12／T-13 是各自獨立任務、有自己的畫面群組範圍，本任務不應該搶做它們的畫面內容（顆粒度規則）；但路由需要有東西可以渲染才能測試導向行為，所以用最小佔位頁面撐住骨架，避免之後被誤讀成已定案的畫面設計。API client 用 `credentials: 'include'` 帶 `HttpSession` cookie，並在 `vite.config.ts` 加 `/api` proxy 轉發到 `http://localhost:8080`（開發模式跨 port 也能帶 cookie）；這是技術實作細節、未違反 spec，屬低風險決定。
- 影響：`kanban-frontend/package.json` 新增 `react-router-dom`；新增 `src/api/**`、`src/auth/**`、`src/layout/**`、`src/pages/**`；`src/main.tsx` 包一層 `BrowserRouter`；`src/App.tsx` 改為路由定義；`src/setupTests.ts` 補上 `afterEach(cleanup)`（`vite.config.ts` 的 `test.globals` 未開啟，`@testing-library/react` 偵測不到全域 `afterEach`，不手動註冊會導致多次 `render` 殘留 DOM，測試互相污染）。T-11～T-13 之後接手對應佔位頁面時，只需要替換 `pages/*.tsx` 內容並視需要調整 `AppShell`／路由參數，不需要改動 `AuthContext`／API client。OQ-IMPL-09（HTTP 狀態碼未定案）目前不影響本任務：`ApiError` 只帶狀態碼與訊息，呼叫端（T-11 等）預期依訊息內容而非狀態碼判斷分流，狀態碼定案後不需要改這層。
- ADR：無（單一任務內的技術選型，未跨 aggregate／未違反既有 ADR-001）。

### 2026-09-18 T-10-fe-shell（Review）
- 決策：退回，狀態從 `review-pending` 改回 `doing`，追加 D-03（TopBar 顯示 `username` 還是 `display-name`，要登記 OQ）、D-04（更正 OQ-IMPL-09「仍待處理」這個過期紀錄，跟 ADR-001 對齊）。
- 理由：第 1 點，Review 自己重跑 `pnpm install --frozen-lockfile`、`pnpm run test`（2 個檔案、7 個測試全過）、`pnpm run build`、`pnpm run lint`（exit 0），以及 `./gradlew clean build --no-daemon`（exit 0）；第 2 點，`uc-logout` 的操作表列（需確認＝否、回到 `s-login`）和驗收條件都有落實並有測試，`authApi.ts` 也跟後端 record 一致；第 3 點不適用（沒動 core），grep 確認 core 是乾淨的；第 4 點，diff 只有 `kanban-frontend/**` 和 `.state/**`；第 5 點不通過，`ui-user-membership.md`『TopBar 顯示帳號名稱』沒有指定是哪個欄位，Dev 自己選了 `username` 卻沒登記 OQ，另外交接紀錄引用了已解除的 OQ-IMPL-09；第 6 點通過，沒有自己發明樣式。
- 影響：不合併回 `loop/implementation`，T-11／T-12 繼續等待。下一輪 Dev 在同一個 worktree 處理 D-03、D-04，預期不需要改程式碼。D-03 的 OQ 如果到下一輪 Review 都還沒定案，只能附保留核准。

### 2026-09-18 T-10-fe-shell（Dev，第 2 輪，D-03／D-04 更正紀錄）
- 更正（不改動上面既有條目，本則為新增）：上面「2026-09-18 T-10-fe-shell（Dev）」一則寫『OQ-IMPL-09（HTTP 狀態碼未定案）目前不影響本任務』，`tasks.md` 對應備註與 `state.md` 也沿用「OQ-IMPL-09 仍待處理／未定案」這個說法——這跟事實不符。OQ-IMPL-09 已經在本任務 Dev commit（`4614e17`）之前的 `06c0a14`（[docs](loops) 解除 OQ-IMPL-09，升級為 ADR-001）解除，並升級記錄為 [`ADR-001`](adr.md#adr-001usecase-fail-對應-http-狀態碼的慣例)。`tasks.md`、`state.md` 的過期字樣已依此次更正同步修改（見兩檔本輪異動）。
- 決策：`kanban-frontend/src/api/http.ts` 的 `ApiError` 帶 `status`（HTTP 狀態碼）欄位這個設計，跟 ADR-001 的分類表方向一致——例如未登入查 `GET /api/session` 回 401（ADR-001「身分驗證失敗或未登入」那一列），`ApiError.status` 能原封不動承接這個分類，不需要另外解析回應內容才能知道是不是未登入。後續 T-11（登入／註冊畫面）等任務要依 ADR-001 的狀態碼分類做基本分流（例如 401 統一導向 `s-login`、409／400 停留原表單顯示訊息），至於同一狀態碼內要顯示的精確文字訊息，依 ADR-001「回應內容一律帶錯誤代碼…狀態碼判斷語意分類，錯誤代碼判斷精確情境」這句，仍要讀回應內容的錯誤代碼／訊息决定顯示哪一句，不是單靠狀態碼決定文案。
- 理由：D-04 要求把「呼叫端依訊息內容判斷」這個說法跟 ADR-001 對齊；`ApiError` 目前的欄位（`status`＋訊息）本來就同時支援「狀態碼分流、訊息內容決定文案」兩層，不需要改程式碼，只需要更正文件敘述與釐清後續任務的用法。
- 影響：本檔（新增更正條目）、`tasks.md`（T-10 備註更正）、`state.md`（更正）。不涉及程式碼改動。
- ADR：無（沿用既有 ADR-001，本則是對齊既有分類表的用法說明，不是新的結構性決策）。
