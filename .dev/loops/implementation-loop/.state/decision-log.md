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

### 2026-09-18 T-10-fe-shell（Review，第 2 輪）
- 決策：附保留核准，狀態從 `review-pending` 改成 `done`，不新增 D-xx。
- 理由：第 1 點，Review 自己重跑 `pnpm install --frozen-lockfile`、`pnpm run test`（2 個檔案、7 個測試全過）、`pnpm run build`、`pnpm run lint`（exit 0），以及 `./gradlew clean build --no-daemon -q`（exit 0）；第 2、6 點，程式碼跟第 1 輪相同，結論不變；第 3 點，grep 確認 core 是乾淨的；第 4 點，diff 只有 `kanban-frontend/**` 和 `.state/**`；第 5 點，D-03（OQ-IMPL-11）、D-04（OQ-IMPL-09 紀錄更正）都已處理，引文逐字核對一致，OQ-IMPL-11 漏引 spec `uc-login` post 與 Scenario，由 Review 追加補充段落，問題本身不變。OQ-IMPL-11 是高風險、不是覆蓋來源，所以附保留，不標 `blocked`。
- 影響：`impl/T-10-fe-shell` 可以合併回 `loop/implementation`，T-11-fe-auth 的依賴滿足；T-12 仍要等 T-02、T-04。OQ-IMPL-11 定案前 TopBar 維持顯示 `user.username`，選 `display-name` 時要回頭改前端和 T-01 的 `SessionResponse`。

### 2026-09-18 T-11-fe-auth（Dev）
- 決策：實作 `s-login`（`LoginPage.tsx`）、`s-signup`（`SignupPage.tsx`）表單本體，取代 T-10 留下的佔位元件；沿用 T-10 已建好的 `AuthProvider`／`useAuth`／`ApiError`／`authApi`，不修改這些檔案。`s-signup` 對「帳號 ID 留空」「密碼超過 40 字」兩個依驗收條件明講「不觸發 `uc-create-user`」的失敗情境，採前端送出前先擋（不打 API），訊息沿用 `uc-create-user` fail-p3／fail-p1 原文（分別跟 `kanban-core` `User.create` 的 `ErrorCode.USERNAME_BLANK`／`PASSWORD_TOO_LONG` 訊息逐字核對一致）；「帳號重複」情境無法只靠前端已知資料判斷，仍送出 API，由 `ApiError.message` 顯示後端回傳訊息（跟 `fail-p2` 訊息「此帳號已被使用」一致）。版面看不到 `Login.dc.html`／`Signup.dc.html` 設計稿內容，依 `dev-prompt.md` 規則記 OQ-IMPL-12，先用純語意 HTML 表單（無額外視覺樣式）並標記「待對照設計稿」。
- 理由：驗收條件文字對三個 `uc-create-user` 失敗情境都寫「不觸發 `uc-create-user`」，但「帳號重複」在技術上只有伺服器知道（前端沒有全體帳號清單），判斷為 ui 檔對三個情境沿用同一句話造成的措辭疊加、不是真的要求前端做不到的事，屬於「技術實作細節不違反 spec」的低風險決定，不另開 OQ；「留空」「密碼過長」兩者前端資料本來就有，直接擋下可以避免不必要的來回並仍完整符合驗收條件字面（不觸發 API）。錯誤訊息optional 全部沿用 spec／後端已定案的逐字文案，不自創新文案。
- 影響：新增／修改 `kanban-frontend/src/pages/LoginPage.tsx`、`SignupPage.tsx`、`LoginPage.test.tsx`（新增）、`SignupPage.test.tsx`（新增）；因為 `App.test.tsx`（T-10 範圍）用文字比對舊佔位內容『登入畫面』，這輪內容替換後改用 `getByRole('heading', { name: '登入' })` 比對，行為斷言（登入態、TopBar、導向）未變動，只改選取器。`pnpm run test`（16 個測試全過）、`pnpm run lint`、`pnpm run build` 皆通過。新增 OQ-IMPL-12（設計稿無法存取）。
- ADR：無（沿用既有 `ApiError`／`ADR-001` 的錯誤處理慣例，未新增跨任務決策）。

### 2026-09-18 T-11-fe-auth（Review 第 1 輪）
- 決策：退回，狀態從 `review-pending` 改回 `doing`，追加 D-05～D-07。
- 理由：第 1 點，Review 自己重跑 `pnpm test`（4 個檔案、16 個測試全過）、`pnpm build`、`pnpm lint`，以及 `./gradlew build -q`，全部 exit 0；第 3、4、6 點都通過。第 2、5 點沒過：(a) `ui-user-membership.md` 第 45 行寫「帳號重複時…不觸發 `uc-create-user`」，Dev 自己把它解讀為措辭疊加並歸為低風險，沒有開 OQ，依 `iteration-prompt.md` 第 5 節這屬於高風險（D-05）；(b) s-login 第 91、92 行驗收條件的「TopBar 不顯示帳號名稱」，以及 fail-p1 的欄位保留／停留本畫面，都沒有測試斷言（D-06）；(c) s-signup 第 43 行的「畫面維持顯示」沒有斷言（D-07）。
- 影響：`impl/T-11-fe-auth` 這輪不合併；下一輪 Dev 處理 D-05～D-07。產品程式碼預期不用改，只要登記 OQ、補測試斷言。

### 2026-09-18 T-11-fe-auth（Dev，第 2 輪，D-05～D-07）
- 決策：D-05 登記 OQ-IMPL-13（情況：兩處矛盾並列），逐字並列 `ui-user-membership.md` 第 45 行『…且不觸發 `uc-create-user`』與 `spec-user-membership.md` 第 97、104、143～147 行 `uc-create-user` 的 `pre p2`／`fail p2`／對應 Scenario，並引 `ui-convention.md` 第 150 行說明第 45 行的斷言主詞是合法的、要當真；OQ 定案前程式碼維持現狀（帳號重複時仍送出 `POST /api/users`，由後端依 `fail p2` 拒絕），不自行在前端加查重機制。D-06 在 `LoginPage.test.tsx` 補上：wrong-password（`fail-p2`）測試新增『TopBar 不顯示帳號名稱』斷言（`queryByRole('button', { name: '登出' })` 為 `null`，因為 `AppShell` 只掛在 `ProtectedRoute` 之下，登入失敗不會進入該路由，這個查詢本身就等於「TopBar 沒有渲染」）；帳號不存在（`fail-p1`）測試補上欄位保留（帳號 ID／密碼值不變）、停留本畫面（`登入` 標題仍在）、TopBar 不顯示帳號名稱三個斷言。D-07 在 `SignupPage.test.tsx`「帳號 ID 為空」測試補上『建立帳號』標題仍在的斷言，對應 `ui-user-membership.md` 第 43 行「畫面維持顯示」。
- 理由：D-05 依 `iteration-prompt.md` 第 5 節，spec 的驗收條件字面與 `pre`／`fail`／Scenario 衝突屬於高風險，不可以自行判定「措辭疊加」就略過，要開 OQ 讓人工選擇 A（前端加查重、需 CR）或 B（修正 ui 檔第 45 行）；D-06／D-07 都是 Review 指出既有測試沒有覆蓋到已定案的驗收條件字面，補斷言即可，不涉及產品行為判斷，不需要另開 OQ。
- 影響：`kanban-frontend/src/pages/LoginPage.test.tsx`（2 個測試新增共 5 個斷言）、`SignupPage.test.tsx`（1 個測試新增 1 個斷言）；`open-questions.md` 新增 OQ-IMPL-13。`kanban-frontend/src/pages/LoginPage.tsx`、`SignupPage.tsx` 產品程式碼未變動。`pnpm test` 4 個檔案、16 個測試全過（新增斷言未改變測試數量，只加斷言）。
- ADR：無（沿用既有結構，未新增跨任務決策）。

### 2026-09-18 T-11-fe-auth（Review，第 2 輪）
- 決策：附保留核准，狀態從 `review-pending` 改成 `done`，不新增 D-xx。
- 理由：第 1 點，Review 自己重跑 `pnpm test`（4 個檔案、16 個測試全過）、`pnpm build`、`pnpm lint`，以及 `./gradlew build -q --no-daemon`，全部 exit 0。第 2 點，D-06／D-07 補的斷言對應 `ui-user-membership.md` 第 43、91、92 行；「登出」按鈕不存在的斷言，因為測試掛整個 `App`、`AppShell` 只在 `ProtectedRoute` 底下，確實等於 TopBar 沒有渲染。第 3、4、6 點通過。第 5 點，D-05 的 OQ-IMPL-13 已登記，引文逐字相符。我判斷它是高風險，不是覆蓋來源（ui 檔跟 spec 的文字矛盾，程式碼照 spec 實作，沒有結構衝突），所以附保留核准，不標 `blocked`。
- 影響：`impl/T-11-fe-auth` 可以合併回 `loop/implementation`。保留事項 R1（OQ-IMPL-13，定案為 A 時要走 CR，並回頭改 `SignupPage.tsx`／測試）、R2（OQ-IMPL-12，版面待對照設計稿）。`open-questions.md` 的 OQ-IMPL-13 底下追加了一段 Review 補充，標明其中一句是推論。
### 2026-09-18 T-02-be-board（Dev，第 1 輪）
- 決策：`kanban-core` 新增 `Board`（Aggregate Root，含內部 `Swimlane`／`Stage`／`ActivityRecord`）、`StageRole`、`CardLookupPort`／`CardSummary`（domain 層定義的依賴反轉介面，供 Board 判斷 Swimlane/Stage 是否還有卡片）、`BoardRepository`（port）；`kanban-spring` 新增對應的 persistence（`BoardJpaEntity`＋子集合 `SwimlaneJpaEntity`／`StageJpaEntity`／`ActivityRecordJpaEntity`，存檔採「就地調和」而非清除重建，避免同 UUID 先刪後插衝突）、`application.BoardApplicationService`、`web.BoardController`（`POST /api/boards`、Swimlane/Stage 的新增／改名／搬移／刪除、`PATCH .../role`）。`Board.create` 依 OQ-IMPL-14 的推論，建立時預設帶 1 個 Swimlane「預設泳道」與 3 個 Stage「待辦」「進行中」「完成」，滿足 board→swimlane／board→stage 關係表 min=1 的不變條件。Card Aggregate（T-03）尚未存在，`CardLookupPort` 正式環境用 `NoOpCardLookupPort`（一律回傳空清單）；Cucumber 測試改用 `FakeCardLookupPort`（`@Primary` 測試替身，可手動注入/搬移假卡片資料）驗證 `SWIMLANE_HAS_CARDS`／`STAGE_HAS_CARDS` 兩個保護機制與對應的兩階段刪除流程（見 `swimlane-management.feature`「刪除包含卡片的 Swimlane 需要確認」、`stage-management.feature`「刪除包含卡片的 Stage 需要先轉移卡片」兩個 Scenario）。
- 理由：`design-kanban-basic.md` 已有完整設計備忘（Aggregate 邊界、`CardLookupPort` 依賴反轉機制、`ensureSwimlaneRemovable`/`removeSwimlane` 等方法簽名），本輪依其設計實作，只跳過該文件描述的 Board Clock（CR-004／F04，屬 T-05-be-board-clock 範圍，尚未到）；活動紀錄時間暫用 `Instant.now()`，待 T-05 改為 Board Clock。`r-board-owner`（僅 Owner 可調整看板結構）的權限檢查依賴 F02 `BoardMembership`（T-04 尚未實作），本輪未加，見 OQ-IMPL-15；`uc-create-board`（F02）的 `board-membership: C` 部分（自動成為 Owner）不在 T-02 產出範圍，只實作 `board` 部分，見 OQ-IMPL-16；預設 Swimlane/Stage 的名稱與數量沒有 usecase `post` 逐字依據，改由 Background/Given 文字推論，見 OQ-IMPL-14。另外，因為 Cucumber 的「系統應該顯示錯誤訊息 {string}」步驟已由 `UserSteps`（T-01）註冊，同一段文字不能在 `BoardSteps` 重複宣告（會變成 Ambiguous step），改為在 `UserSteps` 新增一個 package-private 的 `setLastResult(MvcResult)` 方法（純新增、不改變既有行為），`BoardSteps` 執行完自己的 MockMvc 呼叫後同步呼叫它，讓既有的錯誤訊息斷言步驟能重用於 Board 的情境；`UserController.statusFor` 與新增的 `BoardController.statusFor` 因為 `ErrorCode` 列舉值增加，都補上 `default` 分支避免 switch 不窮盡編譯錯誤（`UserController` 這處是本任務唯一觸碰到既有 T-01 檔案的地方，行為不變，只是讓非使用者相關錯誤碼快速失敗而不是編譯不過）。
- 影響：`kanban-core/ErrorCode.java`（新增 9 個值：`BOARD_NAME_BLANK`／`BOARD_NOT_FOUND`／`EMPTY_SWIMLANE_NAME`／`SWIMLANE_NOT_FOUND`／`MINIMUM_SWIMLANE`／`SWIMLANE_HAS_CARDS`／`STAGE_NOT_FOUND`／`MINIMUM_STAGE`／`STAGE_HAS_CARDS`）是共用檔案，後續 T-04 起新增 `ErrorCode` 值要留意合併衝突（沿用 T-01 決策紀錄裡的既有提醒）。T-03-be-card 之後要把 `NoOpCardLookupPort` 換成真正查詢 Card persistence 的實作，屆時 Swimlane/Stage 的刪除保護才會對真實卡片生效。`swimlane-management.feature`／`stage-management.feature` 只涵蓋 `spec-kanban-basic.md`「Swimlane 管理」「Stage（階段）管理」兩個 Feature（F01 本身），不含 F02「Board 建立與成員邀請」整個 Feature（該 Feature 的正式 Cucumber 驗收留給 T-04，見 OQ-IMPL-16）。`./gradlew clean build` 全綠：`kanban-core` 27 個測試（`BoardTest` 16、`UserTest` 10、`NoSpringDependencyTest` 1），`kanban-spring` 4 個 feature 檔（`create-user-account` 7、`user-login-logout` 4、`swimlane-management` 7、`stage-management` 8）全過。
- ADR：無（沒有新的跨任務結構性決策；`CardLookupPort` 的依賴反轉機制與兩階段刪除協定沿用 `design-kanban-basic.md` 既有設計，不是本輪新訂）。

### 2026-09-18 T-02-be-board（Review，第 1 輪）
- 決策：退回，狀態從 `review-pending` 改回 `doing`，追加 D-05～D-08。
- 理由：第 1 點，Review 自己重跑 `./gradlew clean build --no-daemon`，exit 0，54 個測試全部通過；第 3、4 點，core 是乾淨的，改動範圍只有 `kanban-core`／`kanban-spring`／`.state`，碰到 T-01 的兩處都是必要的最小改動。第 2 點不通過：`uc-delete-swimlane`／`uc-delete-stage` post 第 2 條（卡片一併刪除、轉移到目的 Stage）正式程式碼沒有實作，API 也沒有目的 Stage 的輸入，而且沒有登記 OQ，也沒有任務描述接手（D-05）；對應的兩個 Scenario 靠測試步驟自己改 fake 來通過（D-06）；13 處活動紀錄斷言因為 Background 已經有一筆「建立看板」紀錄，所以不管被測操作有沒有寫紀錄都會過（D-07）。第 5 點：OQ-IMPL-15 的 design 引文不是逐字，OQ-IMPL-14 的路徑寫錯（D-08）。
- 影響：`impl/T-02-be-board` 不合併，T-03／T-04／T-05／T-09／T-12 繼續等待。下一輪 Dev 處理 D-05～D-08，其中 D-06、D-07 只改測試，正式程式碼不要求修改。OQ-IMPL-14～14 不擋核准，但再審時會以附保留核准的方式處理。

### 2026-09-18 T-02-be-board（Dev，第 2 輪，D-05～D-08）
- 決策（D-05）：新開 `OQ-IMPL-17`，逐字引用 `spec-kanban-basic.md` 第 136、141、287、292 行（`uc-delete-swimlane`／`uc-delete-stage` 的 `crud`／post 第 2 條）、`ui-kanban-basic.md` 第 85、183、189、197 行（確認刪除操作列、目的 Stage 輸入、資料狀態差異）、`design-kanban-basic.md` 第 90 行（`removeSwimlane`/`removeStage` 拋例外、應用層協調轉移/刪除的設計說明），寫清楚：(a) 目前 409 回應是依 design 推論出的暫時性行為，不在 spec 定義的 `fail` 裡；(b) 誰負責補上「協調刪除／轉移卡片」與「刪除端點如何帶已確認／目的 Stage 參數」——選項列 T-03 擴充範圍、新增獨立協調任務、或以上皆非三種；(c) 定案前對外 API 的已知限制。不要求本輪改動正式程式碼，`Board`／`BoardController`／`BoardApplicationService` 維持原狀。
- 決策（D-06）：在 `BoardSteps.whenConfirmDelete`、`thenSwimlaneAndCardsRemoved`、`whenChooseDestinationStage`、`thenCardsMovedTo` 四個 step 方法各加一段「替身警告」註解，逐一說明該步驟操作／讀取的是 `fakeCardLookupPort` 而非正式端點，並指向 `OQ-IMPL-17`；沒有刪除或修改既有 Scenario 本身（`swimlane-management.feature`／`stage-management.feature` 是 spec 的逐字複製，不能改）。
- 決策（D-07）：`BoardSteps` 新增 `activityCountBeforeAction`／`expectedActivityKeyword` 兩個欄位與 `captureActivityBaseline(String)` 私有方法，在所有會寫入活動紀錄的動作（新增/重新命名/搬移/刪除 Swimlane、新增/重新命名/搬移/刪除 Stage、設定 Stage 角色）呼叫正式端點之前，先記錄目前活動紀錄筆數與預期的動作關鍵字；`thenActivityRecorded` 改成斷言「筆數剛好加 1」「最新一筆操作人是目前使用者」「若有設定關鍵字則動作內容需包含該關鍵字」；`thenNoNewSwimlaneCreated`／`thenSwimlaneNotDeleted`／`thenStageNotDeleted`（兩個 `@fail-p1` Scenario 用到的 Then）都新增「活動紀錄筆數沒變」的斷言。`BoardTest` 補上 `renameSwimlane`／`moveSwimlaneBefore`／`removeSwimlane`／`addStage`（含插入位置）／`renameStage`／`moveStageBefore`／`removeStage`（含卡片轉移後成功刪除）／`setStageRole` 共 8 個寫入方法呼叫前後的活動紀錄筆數與操作人斷言，新增 `should_renameStage_when_stageExists` 一個測試方法，其餘在既有測試方法內追加斷言。
- 決策（D-08）：`OQ-IMPL-14` 的來源路徑改成直接寫正確路徑 `.dev/F02-user-membership/spec-user-membership.md` 並補上行號（第 260～269 行），拿掉原本「寫錯後用括號更正」的寫法；`OQ-IMPL-15` 的 design 引文改成逐字『權限（僅 Owner 可設定）由呼叫端先查 `BoardMembership` 後才呼叫，`kanban-core` 本身不驗證。』（第 115 行），並加一句明講這句原文只涵蓋 CR-003 的 `setStageRole`，套用到其餘 8 個結構調整 uc 是本 OQ 的推論延伸、不是原文本身的範圍。
- 理由：D-05、D-08 屬於「登記／更正 OQ」層級的修正，不涉及覆蓋來源或結構衝突，不需要新開 CR；D-06、D-07 屬於測試程式碼的驗證力修正，Review 已明確指出「正式程式碼不要求修改」，故 `Board`／`BoardController`／`BoardApplicationService` 本輪未變動。`./gradlew clean build --no-daemon` 全綠：`kanban-core` 28 個測試（`BoardTest` 17、`UserTest` 10、`NoSpringDependencyTest` 1，新增 1 個測試方法＋既有 7 個方法補充斷言），`kanban-spring` 4 個 feature 檔（`create-user-account` 7、`user-login-logout` 4、`swimlane-management` 7、`stage-management` 8）全過。
- 影響：`.dev/loops/implementation-loop/.state/open-questions.md`（新增 OQ-IMPL-17，訂正 OQ-IMPL-14／13 引文）、`kanban-spring/src/test/java/.../BoardSteps.java`、`kanban-core/src/test/java/.../BoardTest.java`。不影響 `kanban-core`／`kanban-spring` 的正式程式碼（main source 未變動）。`tasks.md` 的 D-05～D-08 改為 `done`，T-02-be-board 狀態改回 `review-pending`，交由 Review 第 2 輪判定；OQ-IMPL-17 待處理、不阻塞本任務核准（性質同 OQ-IMPL-14～14，是延後給後續任務接手的協調工作）。
- ADR：無（本輪皆為 OQ 登記／更正與測試驗證力修正，沒有新的跨任務結構性決策）。

### 2026-09-18 T-02-be-board（Review，第 2 輪）
- 決策：附保留核准，狀態從 `review-pending` 改成 `done`，不新增 D-xx。
- 理由：第 1 點，Review 自己重跑 `./gradlew clean build --no-daemon`，exit 0，test-results 統計 55 個測試全部通過（core 28、spring 27）；第 2 點，D-06 的替身註解、D-07 的活動紀錄筆數／操作人／關鍵字斷言都逐一核對過，每個 When 都在自己的 API 呼叫前記錄基準，三個 `@fail-p1` 都驗證了訊息逐字一致、資料與活動紀錄筆數不變；第 3 點，grep 確認 core 是乾淨的；第 4 點，diff 只有 `kanban-core/**`、`kanban-spring/**`、`.state/**`；第 5 點，D-05（OQ-IMPL-17）、D-08 引文都到源頭逐字核對過，一致。OQ-IMPL-17 漏列四個替身步驟，由 Review 追加補充段落。OQ-IMPL-14～15 都是高風險或任務順序造成的延後，不是覆蓋來源，所以附保留，不標 `blocked`。
- 影響：`impl/T-02-be-board` 可以合併回 `loop/implementation`，T-03／T-04 的依賴滿足（T-05／T-09／T-12 還要等其他依賴）。保留事項 R1：OQ-IMPL-17（刪除 Swimlane／Stage 時卡片的連帶刪除／轉移）目前沒有任務接手，要請 Planning 或人工在 T-03 開工前決定；R2～R4 分別是 OQ-IMPL-15、14、12。
