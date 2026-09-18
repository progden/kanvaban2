# open-questions

> 格式沿用 [`與專家協作的提問規則-本體論分析.md`](../../lesson-learned/與專家協作的提問規則-本體論分析.md)：情況欄只能是「引用原文」「兩處矛盾並列」「推論＋所本原文」「覆蓋」四選一並標明種類，逐字引用一律用『』包住，不用「應該」「建議這樣比較好」等說服性字眼代替引用；矛盾類固定給「保留A／保留B／缺區分條件」三個選項；每列開頭固定加 `[Level: <模組>/<任務 ID>]` 標記；新增前先查重，推翻先前結論須明講「推翻 OQ-IMPL-xx」。
>
> 注意：這裡的「情況」四分類跟 `iteration-prompt.md` 第 5 節「自主決策分級」（低風險／高風險／覆蓋來源／環境限制）是兩套不同的分類——分級表講的是「這個判斷有多危險」，這裡的四選一講的是「這句斷言的來源是什麼」。
>
> **狀態欄可以更新（唯一允許回頭改動既有條目的欄位）**：原始斷言、引用、推論不可改；新增「解除說明」小節記錄後續發現的證據與結論，不覆蓋原文。2026-09-18 校正：OQ-IMPL-01～06 的原始判斷有誤（依據當時讀到的資訊做的推論，前提後來被證實不成立），依下方「解除說明」全部解除；正確結論見 `decision-log.md` 對應條目與 `tasks.md` 2026-09-18 校正說明。

## OQ-IMPL-01

[Level: F03-kanban-widgets/T-06-be-kanban-widgets]
情況：【推論＋所本原文】
spec原文：`.dev/F03-kanban-widgets/spec-kanban-widgets.md`「名詞定義」§實體／§欄位／§關係三張表，逐字內容為『| ID | 名詞 | 所屬 Aggregate | 說明 |』`\n`『|---|---|---|---|』，表頭下沒有任何資料列。
推論（當時，已證實錯誤）：由上述原文推論該模組「尚未定義」`entity`／欄位／`r-` ID，故判斷 T-06 找不到依據、排不出範圍。
問題：T-06 是否可以動工？
選項：A／B／C（見原提問）。
狀態：**已解除（2026-09-18）**。
解除說明：推論的前提錯了——三張表是空的，但這是**正確的定案狀態**，不是「尚未遷移」。依據二：(1) `.dev/loops/spec-migration-loop/spec-migration-state.md` 逐字：『已遷移完成的模組：F01～F06 全部完成，全檔 0 error；`./scripts/spec-check` 總計 0 error(s)、0 warning(s)』；(2) `CLAUDE.md`「文件結構」F03 那列逐字：『F03 | kanban-widgets | Cycle/Lead Time、WIP、Throughput/CFD、截止日期提醒；只做讀取投影，不新增 aggregate』——F03 本來就不該有自己的實體，三張表空白是正確結果，它只引用 F01（`board`／`card`）、F04（Board Clock／`asOf`）已定義的實體，加上自己「其他名詞」段落（Lead Time／Cycle Time／WIP／Aging／Throughput／CFD／asOf）與六個已定案的 `usecase` 區塊（`uc-view-cycle-lead-time` 等，逐字可見於 spec 正文）即可實作。T-06 改回 `todo`。

## OQ-IMPL-02

[Level: F05-workload/T-07-be-workload]
情況：【推論＋所本原文】（同 OQ-IMPL-01 的錯誤前提）
狀態：**已解除（2026-09-18）**。
解除說明：同 OQ-IMPL-01。`CLAUDE.md` F05 那列逐字：『F05 | workload | 依 Active Card 的負責人統計工作量』，同樣「只做讀取投影，不新增 aggregate」；spec-migration-state.md 證實 F05 已遷移完成、0 error；`uc-view-workload` 已是定案的 `usecase` 區塊。T-07 改回 `todo`。

## OQ-IMPL-03

[Level: F06-feature-cr-board/T-08-be-feature-cr-board]
情況：【推論＋所本原文】（同 OQ-IMPL-01 的錯誤前提）
狀態：**已解除（2026-09-18）**。
解除說明：同 OQ-IMPL-01。F06 已遷移完成、0 error，「Feature／CR 追蹤表」usecase 區塊已定案。T-08 改回 `todo`。

## OQ-IMPL-04

[Level: F03-kanban-widgets/T-15-fe-widgets（現為 T-18-fe-widgets）]
情況：【引用原文】（原引用的 `CLAUDE.md` 那句『目前僅 F01 有』本身已過期）
狀態：**已解除（2026-09-18）**。
解除說明：`ui-authoring-loop` 已補齊全部模組的 `ui-*.md`；`.dev/F03-kanban-widgets/ui-kanban-widgets.md` 現有 203 行，含 `s-cycle-lead-time-dashboard`／`s-wip-dashboard`／`s-throughput-cfd-dashboard`／`s-duedate-reminder` 四個已定案畫面的完整資料表／操作表／角色表。額外發現：該檔「進入與離開」段逐字寫『不適用——內容以 F07 item 形式顯示於 `s-canvas`，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）』——因此任務依賴要從「T-13-fe-board-detail」改成「T-13-fe-canvas-shell」，任務編號也改為 T-18-fe-widgets（見 `tasks.md` 2026-09-18 校正）；`item.component` 對應值本身仍未定案，見新開的 OQ-IMPL-07。

## OQ-IMPL-05

[Level: F05-workload/T-16-fe-workload（現為 T-19-fe-workload）]
情況：【引用原文】（同 OQ-IMPL-04 的過期前提）
狀態：**已解除（2026-09-18）**。
解除說明：`.dev/F05-workload/ui-workload.md` 已有 `s-workload-dashboard`；`.dev/F02-user-membership/ui-user-membership.md` 也新增了 `s-cards-by-assignee`（從 `s-workload-dashboard` 點擊進入，依 ui-authoring-loop OQ-51）。同 OQ-IMPL-04，改依賴 T-13-fe-canvas-shell，任務編號改為 T-19-fe-workload。

## OQ-IMPL-06

[Level: F06-feature-cr-board/T-17-fe-feature-cr-board（現為 T-20-fe-feature-cr-board）]
情況：【引用原文】（同 OQ-IMPL-04 的過期前提）
狀態：**已解除（2026-09-18）**。
解除說明：`.dev/F06-feature-cr-board/ui-feature-cr-board.md` 已有 `s-feature-cr-board`。同 OQ-IMPL-04，改依賴 T-13-fe-canvas-shell，任務編號改為 T-20-fe-feature-cr-board。

## OQ-IMPL-07

[Level: F07-canvas-layout/T-09-be-canvas-layout、T-13-fe-canvas-shell]
情況：【引用原文】
spec原文：`.dev/F07-canvas-layout/spec-canvas-layout.md`「待釐清」段落逐字：『看板本體（F01 的看板主畫面）如何成為某個 Canvas 上的 `item`（`item.component` 填什麼值代表「看板本體」、預設的位置與大小為何），待整合時另開 CR。』同段另一行：『`canvas` 的建立時機：是隨 `uc-create-board`（F02）建立 Board 時自動產生，還是使用者第一次開啟該 Board 時才建立；本版尚未定義建立 Canvas 的 Use Case，待整合時另開 CR。』
問題：T-09（後端 canvas/item/viewport CRUD）與 T-13（前端 Canvas 容器）能不能動工？`item.component` 實際對應值（例如「看板本體」該填什麼字串）與 `canvas` 建立時機這兩件事要怎麼處理？
選項：A. T-09／T-13 先做「容器」本身（`item` 的移動／調整大小／排層序／錨定／Viewport CRUD，這些在 spec 名詞定義已有完整定義，不受影響），`item.component` 的實際對應值與 `canvas` 建立時機留白／用暫定字串，等整合 CR 定案後再補一輪；B. 等 CR 先定案這兩件事，T-09／T-13 全部延後；C. 以上皆非。
事實：影響任務 T-09、T-13、以及依賴 T-13 的 T-14～T-20（共 9 個任務）；這是 spec 本身標記的待釐清事項，不是實作可以腦補的範圍。
狀態：**已解除（2026-09-18，人工決策）**。
解除說明：人工確認：(1) Canvas 建立時機＝使用者第一次開啟該 Board 時，由系統自動初始化，不是建立 Board 當下；(2) 開啟當下，若該 Canvas 還沒有任何 `item`，系統自動放置一個代表看板本體的 `item`（`item.component` 為 `board`，取 F01 已定義的實體 ID，不用 ui 層 Screen ID——`s-board` 這個值因為違反 `docs-convention.md`「spec 不引用 ui」規則被 spec-check 擋下，改用 `board`）；(3) 看板本體以外的元件（F03 圖表等）仍要使用者手動點畫布左側工具列、觸發 `uc-place-item` 才會加入。`spec-canvas-layout.md` 狀態為「草稿」，已直接改規格本文（新增 Feature「看板畫布初始化」與 `uc-init-canvas`，移除對應「待釐清」），不需開 CR，`./scripts/spec-check` 0 error。T-09／T-13 改回 `todo`（若原本因此被標 blocked）。

## OQ-IMPL-08

[Level: F07-canvas-layout/T-09-be-canvas-layout、T-13-fe-canvas-shell]
情況：【引用原文】
spec原文（逐字）：`.dev/loops/ui-authoring-loop/.state/ui-authoring-open-questions.md` OQ-44：『`spec-canvas-layout.md`「角色定義」表定義 `r-canvas-editor`（說明『可放置與排列畫布元素，並平移與縮放自己的檢視區』）與 `r-canvas-viewer`（說明『只能平移與縮放自己的檢視區，不可改動元素』），但本文件（含「待釐清」）未描述這兩個角色如何對應到另一模組「使用者與看板成員」規格「角色定義」表定義的 `r-system-user`／`r-board-owner`／`r-board-member`』；`ui-canvas-layout.md`「待確認事項」逐字：『`r-canvas-editor`／`r-canvas-viewer`／`r-board-owner`（F02）三者的對應關係 spec 未定義，見 OQ-44』。OQ-44 目前採用的處理方式（ui-authoring-loop 已自動決議，但問題本身未解）：『不預設對應關係，`r-canvas-editor`／`r-canvas-viewer`／`r-user` 視為各模組獨立角色，暫不建立跨模組對應』。
問題：T-09（後端 item CRUD 的權限檢查）與 T-13（前端依角色顯示／隱藏操作）要用哪個角色判斷使用者能不能編輯 Canvas？`r-canvas-editor`／`r-canvas-viewer` 目前沒有對應的資料來源（`board-membership.role` 是 Owner／Member／Viewer，不是 `r-canvas-editor`／`r-canvas-viewer`）。
選項：A. T-09／T-13 先用 `board-membership.role`（Owner／Member＝可編輯、Viewer＝唯讀）直接對應 `r-canvas-editor`／`r-canvas-viewer` 的行為邊界，並標記「暫定對應，待 CR 正式定義後修正」；B. 暫不做權限區分，Canvas 內全部操作對所有看板成員開放，等 CR 定案再收斂；C. 以上皆非。
事實：影響 T-09、T-13（權限檢查邏輯），間接影響 T-14～T-20（凡是「編輯 vs 唯讀」要區分操作可見性的畫面）。
狀態：**已解除（2026-09-18，人工決策）**。
解除說明：人工確認採選項 A 的映射方向，且不是「暫定」——`.dev/F02-user-membership/spec-user-membership.md` 角色定義表逐字已有『r-board-viewer | Board 唯讀成員 | 被邀請加入 Board 的唯讀角色，可檢視看板與相關統計圖表，不能新增／編輯／移動／刪除任何內容…』（2026-09-18 稍早的變更紀錄新增，本 OQ 原引用只寫到 `r-system-user`／`r-board-owner`／`r-board-member` 三個角色，遺漏了這個，一併更正）。對應關係：`board-membership.role` 為 Owner 或 Member（即 `r-board-owner`／`r-board-member`）對應 `r-canvas-editor`；為 Viewer（即 `r-board-viewer`）對應 `r-canvas-viewer`。已直接寫入 `spec-canvas-layout.md` 角色定義表（草稿階段，不需 CR），`./scripts/spec-check` 0 error。T-09／T-13 的權限檢查依此實作。

## OQ-IMPL-09

[Level: F02-user-membership/T-01-be-user]
情況：【推論＋所本原文】
spec原文：`.dev/F02-user-membership/spec-user-membership.md` `uc-create-user` 的 `fail` 逐字：『p1: "拒絕，不建立新的 `user`"』『p2: "拒絕，不建立新的 `user`"』；`uc-login` 的 `fail` 逐字：『p1: "拒絕，顯示錯誤訊息 \"帳號或密碼錯誤\"，`user.username` 與 `user.password` 不變，我仍停留在登入頁面"』『p2: 同上』；`uc-logout` 的 `post` 逐字：『"登出成功，回到登入頁面，且不重新登入即無法存取任何 `board`"』。以上都沒有提到 HTTP 狀態碼。`ui-user-membership.md` 的操作表對 `uc-create-user`／`uc-login` 失敗時也只寫『依 `uc-create-user` p1／p2：輸入內容保留，顯示訊息』一類的呈現方式，同樣沒有訂狀態碼。
推論（目前程式碼已採用，非定案）：`kanban-spring/src/main/java/io/progden/kanban/spring/web/UserController.java` 的 `statusFor` 把 `uc-create-user` fail p1（`PASSWORD_TOO_LONG`）對到 400、fail p2（`USERNAME_ALREADY_EXISTS`）對到 409；`uc-login` fail p1／p2（`INVALID_CREDENTIALS`）對到 401；`GET /api/session` 未登入時回 401；`POST /api/logout` 成功回 204。這是依 REST 慣例（輸入不合法用 400、資源衝突用 409、未認證用 401、成功無回應內容用 204）做的推論，不是 spec 逐字規定。
問題：`uc-create-user`／`uc-login`／`uc-logout`／`GET /api/session` 各情境的 HTTP 狀態碼，要不要定案為目前程式碼採用的對應？
選項：A. 維持目前對應（400／409／401／204／401，如上）；B. 改用其他對應（例如所有拒絕情境統一回 400，由回應內容的錯誤代碼區分細節）；C. 以上皆非，另訂對應規則並記錄在 `design-user-membership.md` 或另一份技術備忘。
狀態：**已解除（2026-09-18，人工決策，採選項 A）**。
解除說明：維持 T-01 目前採用的對應。這件事跨任務、影響 T-02～T-09 之後所有 web 端點怎麼對應 `fail`，屬於結構性決策，已升級記錄為 [`ADR-001`](adr.md#adr-001usecase-fail-對應-http-狀態碼的慣例)（含完整的語意分類表：400/401/403/404/409/204），不只是這則 OQ 的解除說明。後續任務直接照 ADR-001 的表分類，不用再個別開 OQ。

## OQ-IMPL-10

[Level: F02-user-membership/T-01-be-user]
情況：【兩處矛盾並列】
spec原文：`.dev/F02-user-membership/spec-user-membership.md`「名詞定義」§欄位表逐字：『| user.username | string | 非空、全系統不可重複 | 帳號 ID，登入用 |』。
同檔 `uc-create-user` 的 `pre` 逐字：『p1: "`user.password` 長度不可超過 40 字"』『p2: "`user.username` 在系統中不可重複"』——只有「不可重複」，沒有「非空」這一條，`fail` 也只有對應 p1／p2 兩則，沒有第三則。
`ui-user-membership.md` 資料表逐字：『| 帳號 ID | `user.username` | 輸入 | 非空、全系統不可重複，依 `uc-create-user` pre p2 | 登入用 |』——這裡把「非空」也標成依 `pre p2`，但 `pre p2` 原文只講「不可重複」，沒有講「非空」，ui 檔這個標註本身跟它引用的 `pre p2` 原文對不上。
問題：`user.username` 為空字串（或未提供）時，`uc-create-user` 應該如何拒絕？訊息是什麼？是否要新增一條 `pre p3`／對應 `fail p3`？
選項：A. 補一條 `uc-create-user` 的 `pre p3`（例如「`user.username` 不可為空」）與對應 `fail p3`，訊息比照現有兩則的語氣另訂；B. 維持現狀，把欄位表的「非空」解讀為僅要求資料庫層 NOT NULL 約束，應用層不需要額外拒絕與訊息；C. 以上皆非。
事實（程式碼推論，未實際送過請求）：`kanban-core` 的 `User.create` 目前沒有檢查 `username` 是否為空字串；`kanban-spring` 的 `UserJpaEntity.username` 只標了 `nullable = false`。因此 `POST /api/users` 送 `username: ""` 會建立成功（201），跟欄位表「非空」矛盾；送 `username: null` 會在寫入資料庫時丟出未轉換的例外，不會回 `ErrorResponse`。
狀態：**已解除（2026-09-18，人工決策，採選項 A）**。
解除說明：人工確認 `user.username` 本來就是「非空、不可重複」兩條規則都要——欄位表的「非空」跟 `pre` 沒有列出來是**覆蓋範圍缺口**，不是真的互相矛盾（原本情況欄標「兩處矛盾並列」是我判斷過重了：spec 裡「可留白」講的是 `user.password`，`user.username` 沒有任何一處講允許留空，兩處說法方向一致，只是 `pre`／`fail` 沒有把欄位表已經定的規則操作化，訂正這則分類）。因為 F02 已「定稿」，依 `cr-convention.md` 走了 **CR-006**：`uc-create-user` 新增 `pre.p3`「`user.username` 非空」與對應 `fail.p3`，新增 Scenario「帳號 ID（username）不可留空」（掛 `@CR-006 @uc-create-user @fail-p3`）；`kanban-core` 的 `User.create` 新增檢查（`ErrorCode.USERNAME_BLANK`，訊息「使用者名稱不能為空」，依 ADR-001 對到 400）；新增 2 個 `UserTest` 單元測試與 1 個 Cucumber Scenario（`create-user-account.feature` 現在 7 個 Scenario 全過）。`./scripts/spec-check`／`cr-check --cr CR-006`／`./gradlew clean build` 皆通過。
狀態：待處理。在本 OQ 有結論前，`kanban-core`／`kanban-spring` 未新增任何防護或錯誤訊息，避免自行編一個訊息當成定案。
