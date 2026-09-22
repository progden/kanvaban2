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

## OQ-T-15-fe-member-management-03

[Level: user-membership/s-member-management]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-conflict
- 開立：Review 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【兩處矛盾並列】
`.dev/F02-user-membership/spec-user-membership.md` 欄位表第 33 行：『| board-membership.role | enum(Owner, Member, Viewer) | | 該 `user` 對該 `board` 的角色 |』
`.dev/F02-user-membership/ui-user-membership.md` `s-member-management`「資料」表：『| 成員角色 | `board-membership.role` | 顯示 | enum(Owner, Member)，依欄位表限制 | — |』
同檔「角色與權限」表只列出兩列：『| `r-board-owner` | 完整成員清單（帳號、顯示名稱、角色） | 邀請成員、變更成員角色、移除成員 |』與『| `r-board-member` | 完整成員清單（同上） | 嘗試邀請成員、嘗試變更成員角色 |』，沒有 `r-board-viewer` 這一列。
`spec-user-membership.md` 變更紀錄 2026-09-18 一列：『新增唯讀角色 `r-board-viewer`（ui-authoring-loop OQ-44 發現：F07 `spec-canvas-layout.md` 的 `r-canvas-viewer` 找不到對應的看板角色），`board-membership.role` enum 新增 Viewer 值；本次僅新增角色定義與欄位值，既有 use case 的 roles 欄位是否要一併加入 `r-board-viewer`（例如各種檢視類 use case）尚未逐一檢視，見「待釐清」』。
`spec-user-membership.md` `uc-change-member-role` pre：『p2: "目標使用者是該 `board` 的 Member"』。
推論：ui 的資料表寫「enum(Owner, Member)，依欄位表限制」，但它指向的欄位表實際上是 enum(Owner, Member, Viewer)，兩者對這個畫面該顯示哪些角色值的斷言不一致。引用方向為 `spec ← ui`，以 spec 為準時 `s-member-management` 的成員清單可能出現角色為 Viewer 的列；此時「設為 Owner」操作依 `uc-change-member-role` pre p2 不適用於該列，而 ui 的角色與權限表也沒有定義 `r-board-viewer` 在本畫面看得到什麼、做得到什麼。本輪已就「Viewer 列不應顯示設為 Owner」開立 D-xx；Viewer 身分「看得到／做得到」什麼仍未定義。
問題：`s-member-management` 是否需要顯示角色為 Viewer 的成員？若要，ui 的資料表與角色與權限表是否要補上 Viewer／`r-board-viewer`（走 CR）？
選項：A. ui 資料表與角色與權限表補上 Viewer 與 `r-board-viewer`（顯示完整清單、不可邀請／變更角色／移除），走 CR 修訂 `ui-user-membership.md`；B. 確認本畫面只管理 Owner／Member，Viewer 由別的畫面處理，改 spec 或另立畫面規格
