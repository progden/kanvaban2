# T-15-fe-member-management open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-15-fe-member-management-01

[Level: canvas-layout/uc-place-item]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-undefined
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】
`ui-canvas-layout.md` 待確認事項寫『⚠️ 依 OQ-49，F03 四個圖表畫面、F04 時鐘控制畫面、F05 工作量儀表板、F06 追蹤表畫面，人工已確認也是 `s-canvas` 上可放置的獨立 `item`（跟看板本體一樣，不透過其他畫面的操作列進入）；但這些各自的 `item.component` 對應值、如何從既有畫布新增這類元件（使用者操作流程）spec「待釐清」尚未定義，待整合 CR 定案』；同檔「看板成員」item 的敘述（操作表「於『看板成員』item 選擇加入成員」一列）也只描述行為，不含 `item.component` 識別碼的值。`spec-canvas-layout.md` `uc-place-item` 的 `pre`／`post` 只要求『`item` 存在，`item.component` 為指定的元件識別碼』，未限制可能值的集合。
推論：因為 `item.component` 在 spec 中是自由字串（`PlaceItemDialog` 也是讓使用者自行輸入，沒有白名單），要讓「看板成員」item 能掛上內容，必須先選定一個具體字串。本任務選定 `"board-members"`，在 `registerItemComponent('board-members', BoardMembersItem)` 註冊；這是純技術實作細節（不影響任何 Scenario 的業務行為），不是規格斷言。
問題：整合 CR 定案「看板成員」item 的 `item.component` 值時，是否採用本任務選定的 `"board-members"`？
選項：A. 採用 `"board-members"`，維持本任務程式碼不變；B. 定案為其他字串，回頭改 `kanban-frontend/src/canvas/members/BoardMembersItem.tsx` 的 `registerItemComponent` 呼叫（以及使用者實際透過「加入元件」輸入的識別碼）

## OQ-T-15-fe-member-management-02

[Level: user-membership/uc-remove-member]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】
`ui-user-membership.md` `s-member-management` 操作表『移除成員 | `uc-remove-member` | 依 post：目標成員自清單移除 | 依 `uc-remove-member` fail p1：不移除，顯示訊息 | 是（附掛於清單列上；若該成員仍為 `card` 負責人，依 pre p2 需先顯示確認訊息並告知張數）』；驗收條件只有兩條相關：『移除非唯一 Owner 或 Member 後，觸發 `uc-remove-member`，該成員自清單移除』（未提及任何確認步驟）與『移除仍是卡片負責人的成員時，先顯示確認訊息並告知卡片張數，確認後才觸發 `uc-remove-member`』。同一份文件「角色與權限」表『`r-board-member` | 完整成員清單（同上） | 嘗試邀請成員、嘗試變更成員角色』未列出「移除成員」或「嘗試移除成員」；`spec-user-membership.md`「Board 權限管理」Feature 只定義了 `uc-reject-invite-by-member` 與 `uc-reject-role-change-by-member` 兩個非 Owner 拒絕類 usecase，沒有對應「非 Owner 嘗試移除成員」的 usecase。
推論：(1) 「需確認？」欄的「是」只在括號內具體描述了「該成員仍為 card 負責人」這個情境要先顯示確認並告知張數；驗收條件也只在這個情境要求確認、一般移除的驗收條件沒有提到確認步驟，故本任務判斷：一般移除（成員目前不是任何卡片的負責人）點擊「移除」後直接呼叫後端（`confirmed=false`），後端若判斷需要確認（該成員仍是卡片負責人）才會回覆錯誤並附上卡片張數訊息，前端據此才顯示確認卡片，使用者確認後才以 `confirmed=true` 重打；不是每次移除都先跳出一個通用的「確定要移除？」對話框。(2) 因為 spec 沒有「非 Owner 嘗試移除成員」對應的 usecase，且角色表「做得到」欄未列出移除，本任務判斷 `r-board-member` 不應該看到「移除」按鈕，只有 `r-board-owner` 才會顯示，避免前端自己發明一個規格沒有定義的拒絕情境。
問題：(1) 一般移除（非卡片負責人）是否也需要先跳出通用的「確定要移除？」確認，而不是直接呼叫 API？(2) `r-board-member` 是否也應該能看到「移除」按鈕並觸發某種拒絕情境（目前缺一個對應的 usecase）？
選項：A. 維持本任務推論（一般移除不額外跳出確認、`r-board-member` 看不到移除按鈕）；B. 一般移除也要先跳出通用確認，且需另開 usecase 定義 `r-board-member` 嘗試移除成員時的拒絕情境
