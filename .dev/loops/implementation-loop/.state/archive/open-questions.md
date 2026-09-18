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

## OQ-IMPL-11

[Level: F02-user-membership/T-10-fe-shell]
情況：【推論＋所本原文】
spec原文：`.dev/F02-user-membership/ui-user-membership.md` 第 78 行操作表逐字：『送出登入表單 | `uc-login` | 依「完成後去哪裡」導向下一畫面，TopBar 顯示帳號名稱 | 依 `uc-login` p1／p2：帳號 ID 與密碼欄位不變，顯示訊息，停留本畫面 | 否（帳號密碼錯誤可重新輸入再次嘗試，非不可逆操作） |』；同檔第 90 行驗收條件逐字：『帳號密碼正確時送出登入表單，觸發 `uc-login`，成功後 TopBar 顯示該使用者名稱』。
同檔第 26～27 行資料表逐字：『| 帳號 ID | `user.username` | 輸入 | 非空、全系統不可重複，依 `uc-create-user` pre p2 | 登入用 |』『| 顯示名字 | `user.display-name` | 輸入（選填） | 未指定時預設等於 `user.username` | — |』。
`.dev/F02-user-membership/spec-user-membership.md` 第 28～29 行欄位表逐字：『| user.username | string | 非空、全系統不可重複 | 帳號 ID，登入用 |』『| user.display-name | string | 未指定時預設等於 `user.username` | 顯示名字，看板上顯示用，可以與其他帳號重複 |』。
推論（目前程式碼已採用，非定案）：`ui-user-membership.md` 第 78、90 行的「帳號名稱」「該使用者名稱」都沒有明確指到 `user.username`（欄位表標為「帳號 ID」）或 `user.display-name`（欄位表標為「顯示名字，看板上顯示用」）其中一個。`kanban-frontend/src/layout/AppShell.tsx` 的 TopBar 目前顯示 `useAuth()` 回傳的 `username`（來自 `kanban-spring` 的 `GET /api/session`，其 `SessionResponse` 只有 `username` 一個欄位，見 `kanban-spring/src/main/java/io/progden/kanban/spring/web/SessionResponse.java`），這是 Dev 依「帳號名稱」字面較接近「帳號 ID／username」做的選擇，不是 spec 逐字規定。
問題：TopBar 顯示的「帳號名稱」／「該使用者名稱」應該是 `user.username` 還是 `user.display-name`？若是後者，`GET /api/session` 的 `SessionResponse` 需要一併補上 `displayName` 欄位（屬於 T-01-be-user 範圍的後端修改）。
選項：A. 維持目前對應（`user.username`），因為 ui 檔用詞是「帳號名稱」而非「顯示名字」；B. 改為 `user.display-name`，因為欄位表明講 `user.display-name` 才是「看板上顯示用」，TopBar 屬於全域顯示情境，且需同步修改 `SessionResponse`／`UserResponse`；C. 以上皆非。
狀態：待處理。在本 OQ 有結論前，TopBar 維持顯示 `user.username`，不自行改成 `user.display-name`。
狀態：**已解除（2026-09-18，人工決策，採選項 B）**。
解除說明：TopBar 顯示 `user.display-name`。依據（人工判斷，非規格原文）：欄位表第 29 行逐字『顯示名字，看板上顯示用，可以與其他帳號重複』，與成員清單（`ui-user-membership.md` 第 216 行 `user.display-name`）、查詢對象（第 365 行）的顯示一致。這要動到已定稿的 `uc-login` post 與 `s-login` 操作表／驗收條件，已走 CR-007：`uc-login` post 改為『"登入成功，TopBar 顯示該 `user` 的顯示名字"』，新增 Scenario「登入後 TopBar 顯示的是顯示名字而不是帳號 ID」（帳號 "user5"、顯示名字 "王小明"，用來區分兩個欄位——既有 Scenario 的 "user1" 未指定顯示名字，兩欄同值）。程式碼修改由任務 `T-21-cr007-topbar-display-name` 接手（`SessionResponse` 補顯示名字、TopBar 改顯示它）；在該任務合併前，程式碼現狀仍是顯示 `user.username`。
Review 補充（2026-09-18，T-10-fe-shell Review 第 2 輪追加，上面 Dev 寫的內容未改）：上面的引文只有 ui 檔與欄位表，漏了 spec 本身對 TopBar 的兩處描述，補上供人工判斷。`.dev/F02-user-membership/spec-user-membership.md` 第 189 行 `uc-login` post 逐字：『"登入成功，TopBar 顯示該 `user` 的帳號名稱"』；同檔第 219～223 行 Scenario「使用正確帳號密碼登入」逐字：『Given 系統中存在帳號 "user1"，密碼為 "correct-password"』…『And TopBar 應該顯示我的名稱 "user1"』。【Review 的推論】這個 Scenario 沒有指定 `user.display-name`，依欄位表『未指定時預設等於 `user.username`』，兩個欄位的值都是 "user1"，所以這個 Scenario 無法區分選項 A 和 B；spec 的「帳號名稱」跟 ui 檔第 78 行用詞一樣，問題本身不變。

## OQ-IMPL-12

[Level: F02-user-membership/T-11-fe-auth]
情況：【引用原文】
`dev-prompt.md`「你要做什麼」第 2 點逐字：『前端任務：依 `ui-*.md` 操作表（觸發的 `uc-xxx`、需確認？、失敗時呈現方式）與驗收條件實作互動；版面依 `planning-prompt.md` 附的設計稿畫面清單，若手上看不到實際設計稿內容，先讀 `.state/tasks.md` 該任務列有沒有附更細的版面摘要，沒有就記 OQ、用最簡潔可用的版面先做，標記「待對照設計稿」，不可以自己發明視覺風格當作定案。』
`planning-prompt.md`「前端設計稿畫面清單」逐字列出『`Login.dc.html` | F02：登入』『`Signup.dc.html` | F02：註冊』，來源標注『來源：claude.ai Design Artifact，35 個檔案，擷取自 `project/*.dc.html`』。
`.state/tasks.md` T-11-fe-auth 該列「備註」欄逐字為空（僅 `| T-11-fe-auth | \`s-login\`、\`s-signup\` | T-10-fe-shell | doing | |`），沒有附加版面摘要。
問題：Dev sub agent（`-p` 一次性行程，無法開啟 claude.ai Design 類型 Artifact）看不到 `Login.dc.html`／`Signup.dc.html` 實際版面，依規則已用 `ui-user-membership.md` s-login／s-signup 的操作表、資料表、驗收條件實作純語意版面（標準 HTML 表單元素：帳號 ID／密碼／顯示名字輸入框、送出按鈕、連結、錯誤訊息 `role="alert"`），未對照設計稿視覺風格（配色、間距、元件庫）。
狀態：待處理。本任務程式碼（`LoginPage.tsx`／`SignupPage.tsx`）標記「待對照設計稿」，之後若有人工或有設計稿存取權的 agent 對照 `Login.dc.html`／`Signup.dc.html`，可能需要回頭調整版面與樣式（不影響已驗證的操作／驗收條件行為）。

## OQ-IMPL-13

[Level: F02-user-membership/T-11-fe-auth]
情況：【兩處矛盾並列】
`.dev/F02-user-membership/ui-user-membership.md` 第 45 行驗收條件逐字：『帳號 ID 與系統中既有帳號重複時確認建立帳號，輸入內容保留、顯示訊息，且不觸發 `uc-create-user`』。
`.dev/conventions/ui-convention.md` 第 150 行逐字：『驗收條件是元件測試與 E2E 的來源，斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」，不寫領域狀態（那是 spec Scenario 的事）』——依此，第 45 行「不觸發 `uc-create-user`」是這份 ui 檔合法、且應被當真的斷言主詞，不是隨手帶過的措辭。
`.dev/F02-user-membership/spec-user-membership.md` 第 97 行 `uc-create-user` 的 `pre` 逐字：『p2: "`user.username` 在系統中不可重複"』；第 104 行 `fail` 逐字：『p2: "拒絕，不建立新的 `user`"』；第 143～147 行 Scenario「帳號 ID（username）不可重複」逐字：『Given 系統中已存在帳號 "user1"』『When 我嘗試建立另一個帳號 "user1"』『Then 系統應該顯示錯誤訊息 "此帳號已被使用"』『And 不應該建立新的帳號』——這個 Scenario 描述 `uc-create-user` 被觸發、系統依 `pre p2` 判斷、依 `fail p2` 拒絕的流程，前提是請求已送達（`uc-create-user` 有被觸發），系統才能判斷「在系統中不可重複」。
問題：`ui-user-membership.md` 第 45 行「且不觸發 `uc-create-user`」與 `spec-user-membership.md` 的 `uc-create-user` `pre p2`／`fail p2`／對應 Scenario 兩者矛盾——前者要求前端在帳號重複時完全不送出建立帳號請求（即不觸發該 usecase），後者的設計前提是請求已送達、由後端依 `pre p2` 判斷重複並依 `fail p2` 拒絕。前端目前沒有全體帳號清單，spec 也沒有另一個查重用的 usecase／API，若要滿足「不觸發 `uc-create-user`」，前端要如何在送出前就判斷帳號 ID 重複？
選項：A. 保留 A（依 ui 檔第 45 行字面）——前端必須在送出前自行判斷重複並擋下，代表要新增一個查重用的 API／usecase（spec 目前未定義），屬於新增行為，需要 CR；B. 保留 B（依 spec 的 `pre p2`／`fail p2`／Scenario）——ui 檔第 45 行「不觸發 `uc-create-user`」是措辭疊加或誤寫，實際行為應是「送出後由後端依 `pre p2` 拒絕，前端顯示 `fail p2` 對應訊息、保留輸入」，ui 檔這行文字要修正（需經 `ui-authoring-loop` 或人工改 ui 檔）；C. 缺區分條件（以上皆非，需要人工另外定義判斷方式）。
事實（程式碼現狀，非定案）：目前 `SignupPage.tsx` 在帳號重複時仍會呼叫 `POST /api/users`（也就是觸發 `uc-create-user`），由後端依 `fail p2` 拒絕（409，訊息「此帳號已被使用」），前端收到後保留輸入、顯示訊息；`SignupPage.test.tsx`「帳號 ID 與系統中既有帳號重複」的測試沒有斷言「不觸發 `uc-create-user`」，跟目前實際行為（會觸發）一致，但跟 ui 檔第 45 行字面矛盾。
狀態：待處理。在本 OQ 有結論前，程式碼維持現狀（帳號重複時仍送出 `POST /api/users`，由後端拒絕），不自行在前端加一個查重機制去符合 ui 檔第 45 行字面。

Review 補充（2026-09-18，T-11-fe-auth Review 第 2 輪追加，上面 Dev 寫的內容未改）：上面五段引文我已到源頭逐字核對，`ui-user-membership.md` 第 45 行、`ui-convention.md` 第 150 行、`spec-user-membership.md` 第 97、104、143～147 行都跟原文一致。有一處需要標清楚：「情況」段最後一句『這個 Scenario 描述 `uc-create-user` 被觸發、系統依 `pre p2` 判斷、依 `fail p2` 拒絕的流程，前提是請求已送達…』是**推論**，不是 spec 原文。Scenario 本身只寫『When 我嘗試建立另一個帳號 "user1"』，沒寫前端是否送出請求。這個推論不影響問題本身，所以只在這裡補註，不再退回。

## OQ-IMPL-14

[Level: F01-basic-kanban/T-02-be-board]
情況：【推論＋所本原文】
spec原文：`.dev/F02-user-membership/spec-user-membership.md` 第 260～269 行 `uc-create-board` 的 `post` 逐字只有三條（第 267～269 行）：『"新的 `board` 建立成功，`board.name` 為指定名稱，`board.created-by` 為建立者"』『"建立者自動成為該 `board` 的 `board-membership`，角色為 Owner"』『"該 `board` 產生一筆活動紀錄，操作人為建立者、動作為「建立看板」"』，沒有提到預設 Swimlane／Stage。
`.dev/F01-basic-kanban/spec-kanban-basic.md`「關係」表逐字：『| board | swimlane | 1 | n | 看板至少保留一個 Swimlane |』『| board | stage | 1 | n | 看板至少保留一個 Stage |』，代表這個不變條件必須隨時成立，含剛建立完成的當下。
同檔「Feature: Swimlane 管理」Scenario「新增一個 Swimlane」的 Given 逐字：『看板目前有 1 個 Swimlane "預設泳道"』；「Feature: Stage（階段）管理」的 Background 逐字：『看板目前的 Stage 依序為 "待辦"、"進行中"、"完成"』。
推論（已採用為實作依據）：由於 `uc-create-board` 的 `post` 沒有明講預設值，但關係表的 min=1 不變條件與上述兩處 Background/Given 文字一致指向「剛開啟的看板」帶有 1 個名為「預設泳道」的 Swimlane、3 個依序為「待辦」「進行中」「完成」的 Stage，`Board.create(...)`（`kanban-core`）採用這組具體名稱與數量作為建立時的預設值。
問題：這組預設 Swimlane／Stage 名稱與數量是否為正式定案？若非，正確定案內容為何？
選項：A. 維持目前實作（1 個「預設泳道」＋ 3 個「待辦」「進行中」「完成」，皆依 Background/Given 文字逐字採用）；B. `uc-create-board` 的 `post` 應明確補上這條規則（走 CR，因為 `spec-user-membership.md` 狀態為「定稿」）；C. 以上皆非（例如預設值應可由使用者在建立當下自訂，不該寫死在 domain 層）。
狀態：待處理。在本 OQ 有結論前，`kanban-core` 的 `Board.create` 維持目前實作（選項 A 的內容），`kanban-spring`／Cucumber 驗收測試皆以此為準。

## OQ-IMPL-15

[Level: F01-basic-kanban/T-02-be-board]
情況：【推論＋所本原文】
spec原文：`.dev/F01-basic-kanban/spec-kanban-basic.md` 2026-09-18 變更紀錄逐字：『修正 9 個結構調整 usecase 的角色…`uc-add-swimlane`／`uc-rename-swimlane`／`uc-reorder-swimlane`／`uc-delete-swimlane`／`uc-add-stage`／`uc-rename-stage`／`uc-reorder-stage`／`uc-delete-stage`／`uc-set-stage-role` 的 roles 改為 r-board-owner』；`.dev/F02-user-membership/spec-user-membership.md` 角色定義表逐字：『r-board-owner | Board 擁有者 | Board 的管理角色：可邀請／移除／升級成員、可新增／重新命名／刪除 Swimlane 與 Stage…』。
`design-kanban-basic.md` 第 115 行逐字（出自「CR-003 完成後新增」段落，講的是 `Board.setStageRole` 這一個方法）：『權限（僅 Owner 可設定）由呼叫端先查 `BoardMembership` 後才呼叫，`kanban-core` 本身不驗證。』——這句原文只講 `setStageRole`，不是 9 個結構調整 uc 的共同依據；本 OQ 把它推廣套用到其餘 8 個 uc（`uc-add-swimlane` 等），是【推論】的延伸，不是這句原文本身的範圍。
推論：`BoardMembership`（F02）是 T-04-be-board-membership 的範圍，尚未實作；T-02 的 `BoardController`／`BoardApplicationService` 目前只要求「已登入」（session 有效）即可呼叫 Swimlane／Stage 的所有結構調整端點，未依 `r-board-owner` 限制「僅 Owner 可操作」。
問題：T-02 的 web 端點是否應該在 T-04 完成前就先擋掉非 Owner（例如回一個暫時的 403），還是維持目前「已登入即可操作」到 T-04 補上權限檢查？
選項：A. 維持現狀，T-04 完成後再對這些既有端點補上 `BoardMembership` 查詢與 `r-board-owner` 檢查（本 OQ 解除時機＝T-04 完成）；B. T-02 先加一個「一律要求 Owner」的暫時檢查機制（例如查詢一個尚不存在的 membership 表會導致找不到而全部拒絕），阻擋所有操作直到 T-04 補齊；C. 以上皆非。
狀態：待處理。在本 OQ 有結論前，採選項 A 的行為（未加權限檢查），因為選項 B 會讓 T-02 自身的 Swimlane／Stage 功能完全無法使用，防禦過度。

## OQ-IMPL-16

[Level: F02-user-membership/T-02-be-board]
情況：【推論＋所本原文】
spec原文：`uc-create-board` 屬於 `.dev/F02-user-membership/spec-user-membership.md`「Feature: Board 建立與成員邀請」，`crud` 欄逐字：『{board: C, board-membership: C}』；對應 Scenario「建立 Board 的人自動成為 Owner」逐字斷言：『我對該 Board 的角色應該是 "Owner"』。
推論：`board-membership` 是 T-04-be-board-membership 的 Aggregate（見 `tasks.md`），T-02 的產出範圍是「F01 board Aggregate（board＋swimlane＋stage）」，不含 `board-membership`。T-02 的 `BoardApplicationService.createBoard` 只實作 `uc-create-board` 的 `board` 部分（`board.name`／`board.created-by`／活動紀錄「建立看板」），未建立對應的 `board-membership`（Owner），因此 `spec-user-membership.md`「Feature: Board 建立與成員邀請」整個 Feature（含此 Scenario 逐字斷言的 Owner 角色部分）未被 T-02 的 Cucumber 驗收測試涵蓋——這個 Feature 的正式驗收覆蓋留給 T-04。
問題：T-04 開發時，是否要修改 `BoardApplicationService.createBoard`（在同一次呼叫內接著建立 `board-membership`），還是另外新增一個協調兩個 Aggregate 的上層服務？
選項：A. T-04 直接修改／擴充 `BoardApplicationService.createBoard`（或新增一個依賴它的協調方法），在建立 `board` 成功後接著建立 Owner `board-membership`；B. 新增一個獨立的協調層（例如 application 層的 façade），呼叫 `BoardApplicationService.createBoard` 與 `BoardMembershipApplicationService` 兩者；C. 以上皆非。
狀態：待處理，不阻塞 T-02（T-02 產出範圍本就不含 `board-membership`），留給 T-04 決定並解除。

## OQ-IMPL-17

[Level: F01-basic-kanban/T-02-be-board]
情況：【推論＋所本原文】
spec原文：`.dev/F01-basic-kanban/spec-kanban-basic.md` 第 136 行 `uc-delete-swimlane` 的 `crud` 逐字：『crud: {board: U, swimlane: D, card: D}』，第 141 行 `post` 第 2 條逐字：『"若該 `swimlane` 內有 `card`，一併被刪除"』；第 287 行 `uc-delete-stage` 的 `crud` 逐字：『crud: {board: U, stage: D, card: U}』，第 292 行 `post` 第 2 條逐字：『"若該 `stage` 內有 `card`，`card.stage` 更新為使用者選擇的目的 `stage`"』。
`.dev/F01-basic-kanban/ui-kanban-basic.md` 第 85 行操作表逐字：『確認刪除 | `uc-delete-swimlane` | 關閉對話框，回列表 | 依 `uc-delete-swimlane` p1：保留對話框，顯示訊息 | 是（本畫面即確認） |』；第 183 行逐字：『目的 Stage | `board`→`stage` 關係（同一 `board` 中的其他 Stage） | 輸入（卡片數大於 0 時必選） | 排除欲刪除的 Stage 本身，清單只列其他 Stage | 依 `uc-delete-stage` post，接收該 Stage 內的卡片 |』；第 189 行逐字：『確認刪除 | `uc-delete-stage` | 關閉對話框，回列表 | 依 `uc-delete-stage` p1：保留對話框，顯示訊息 | 是（本畫面即確認） |』；第 197 行逐字：『資料狀態差異：該 Stage 內有卡片時，需先選擇目的 Stage 才能確認刪除；無卡片時可直接確認刪除，不需選擇目的 Stage』。
`.dev/F01-basic-kanban/design-kanban-basic.md` 第 90 行逐字：『`removeSwimlane`/`removeStage` 遇到還有卡片時**拋出例外並附上數量**，由應用層攔截後轉為「確認訊息」或「選擇轉移目的 Stage」的流程；卡片實際的轉移/刪除是操作 Card 聚合完成，完成後應用層再重新呼叫一次 Board 的刪除方法（此時 `countCardsIn` 應為 0，可順利完成）。』
推論：(a) 目前 `BoardController` 的 `DELETE .../swimlanes/{id}`、`DELETE .../stages/{id}` 在有卡片時只回 409（`SWIMLANE_HAS_CARDS`／`STAGE_HAS_CARDS`），這個 409 回應是把 design 原文講的「應用層攔截後轉為確認訊息／選擇轉移目的 Stage」當成觸發點來實作，但這條轉換規則本身不在 spec 的 `fail`（`uc-delete-swimlane`／`uc-delete-stage` 的 `fail` 都只有 `p1`：「`board` 中的 swimlane/stage 數量大於 1」，沒有「有卡片時回 409」這一條），完全是依 design 文件的協調流程描述推論出來的行為，spec 本身沒有定義這個 fail 分支。(b) T-03-be-card 的任務範圍（`tasks.md` 第 22 行）只寫「F01 `card` Aggregate（`card`＋`comment`）」，沒有任何字提到「刪除 Swimlane／Stage 時協調刪除／轉移卡片」或「刪除端點如何帶『已確認』與目的 `stage`」；目前任務清單裡沒有任何一列會接手這個協調流程與 API 設計。
問題：(a) 目前的 409 回應（附卡片數量的訊息）算不算 spec 定義行為之外的、依 design 推論出的暫時性 fail？(b) 「刪除 Swimlane／Stage 時協調刪除／轉移卡片」的應用層邏輯，以及「刪除端點如何帶『已確認』／目的 `stage` 參數」，由哪個任務負責——T-03（連帶擴充範圍）、新增一個任務、還是回頭修 T-02？(c) 在這件事定案前，對外 API 目前的限制（有卡片時只能回 409、無法真的完成刪除／轉移）要不要另外記錄成已知限制，供前端（T-14）與其他呼叫方知悉？
選項：A. 由 T-03 的任務範圍追加「協調 Board 刪除 Swimlane／Stage 時的卡片刪除／轉移，並擴充 `BoardController` 刪除端點以接受『已確認』／目的 `stage` 參數」；B. 新增一個獨立任務（例如 `T-0X-be-board-card-coordination`），依賴 T-02 與 T-03，專門處理這個跨 aggregate 協調；C. 以上皆非。
狀態：待處理，不阻塞 T-02（T-02 本身的 Swimlane／Stage 結構調整、以及「無卡片時可刪除」的行為已完整實作並通過測試），但在此 OQ 有結論並由對應任務接手前，對外 API 只能做到「有卡片時回 409、不完成刪除／轉移」，`uc-delete-swimlane`／`uc-delete-stage` 的 post 第 2 條尚未被任何正式程式碼路徑滿足。

Review 補充（2026-09-18，T-02-be-board Review 第 2 輪追加，上面 Dev 寫的內容未改）：D-06 要求『同一則 OQ 也列出這四個步驟，等協調流程補上後改成打正式端點驗證』，上面的 OQ 本文沒有列出，補上供接手任務對照。目前是替身、只能算「Scenario 綠燈但 post 第 2 條未驗證」的 step definition 共四個，都在 `kanban-spring/src/test/java/io/progden/kanban/spring/cucumber/BoardSteps.java`：`whenConfirmDelete`（「我確認刪除」，直接呼叫 `fakeCardLookupPort.removeAllCardsInSwimlane`）、`thenSwimlaneAndCardsRemoved`（「該 Swimlane 與其所有卡片都應該被移除」，只檢查 Swimlane 不存在、不檢查卡片）、`whenChooseDestinationStage`（「我選擇目的 Stage 為 {string}」，直接呼叫 `fakeCardLookupPort.moveAllCardsToStage`）、`thenCardsMovedTo`（「這 {int} 張卡片應該被移動到 {string}」，讀的是同一個 fake 的狀態）。四個方法內都已有指向本 OQ 的「替身警告」註解。【Review 的推論】本 OQ 解除時，接手任務除了補應用層協調流程與刪除端點參數，還要把這四個步驟改成透過正式端點送出「已確認」／目的 `stage`，並從 Card 的 persistence 驗證卡片被刪除／`card.stage` 已更新；否則 `uc-delete-swimlane`／`uc-delete-stage` post 第 2 條仍然沒有被驗證。
