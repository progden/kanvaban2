# T-04-be-board-membership open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-04-be-board-membership-01

[Level: F02-user-membership/uc-set-card-assignees]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-19）
- 狀態：待處理

情況：【推論＋所本原文】
引用一（`spec-user-membership.md` `uc-set-card-assignees` post）：『該操作被記錄為 `card` 的一筆活動紀錄，記錄操作人與異動後的負責人名單（例如「將卡片負責人設定為 雅婷、建宏」、「將 建宏 從卡片負責人中移除」）』
引用二（Scenario「指派多位負責人給卡片」）：『應該產生一筆活動紀錄：操作人 "user1"、動作為「將卡片負責人設定為 雅婷、建宏」』
引用三（Scenario「從卡片移除其中一位負責人」）：『應該產生一筆活動紀錄：操作人 "user1"、動作為「將 建宏 從卡片負責人中移除」』
推論：同一個 `uc-set-card-assignees` 底下兩條 Scenario 的活動紀錄措辭不同（新增／整批設定 vs 純粹移除），但規格沒有明講「什麼情況該用哪一種措辭」這條切換規則本身。implementation-loop T-04 實作時採用「本次異動若只有移除、沒有新增，就列出被移除者姓名；其餘情況（含新增、混合新增與移除、整批重設）一律列出異動後的完整負責人名單」這條規則，理由是這樣兩條既有 Scenario 的字面期望都能滿足，但規格沒有驗證過「同時新增與移除某成員」這類未覆蓋情境下的正確措辭。
問題：這個「純粹移除時列被移除者姓名、其餘情況列完整名單」的措辭切換規則，是否為規格真正意圖？若不是，正確規則是什麼？
選項：A. 維持現有實作（以「本次異動是否為純粹移除」判斷措辭，見 `CardApplicationService.applyAssignment`）；B. 一律使用「將卡片負責人設定為 <完整清單>」，不特別處理純移除情境（但這樣會與「從卡片移除其中一位負責人」這條已定案 Scenario 的逐字期望不符，除非另外修改該 Scenario）；C. 修 `spec-user-membership.md` 明確補上這條切換規則的文字定義（需要開 CR）。

## OQ-T-04-be-board-membership-02

[Level: F02-user-membership/uc-delete-board]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-19）
- 狀態：待處理

情況：【推論＋所本原文】
引用一（`spec-user-membership.md` `uc-delete-board` usecase 區塊）：『crud: {board: D, card: D, board-membership: R}』
引用二（`uc-delete-board` post）：『"該 `board` 不再存在"』『"該 `board` 底下的所有 Swimlane、Stage 與 `card` 都一併被刪除"』
推論：`crud` 只把 `board-membership` 標成 R（讀取），沒有標 D（刪除），implementation-loop T-04 據此推論：刪除 Board 時規格不要求真的把該 Board 底下的 `board-membership` 資料列實體刪除。實作上保留這些孤兒 `board-membership` 列，只在 `uc-view-board-list`（`BoardMembershipApplicationService.listBoardsForUser`）查詢時，用「對應的 Board 是否還存在」過濾掉，不在刪除 Board 當下清掉這些資料列本身。這條推論有風險：`crud` 沒標 D 也可能只是規格撰寫時的遺漏，而非刻意排除。
問題：刪除 Board 後，是否應該一併刪除該 Board 底下所有 `board-membership` 資料列（即使 `uc-delete-board` 的 `crud` 只標了 R）？
選項：A. 維持現況（保留孤兒列，查詢時過濾，見 `BoardMembershipApplicationService.listBoardsForUser`）；B. 改為刪除 Board 時一併實體刪除對應的 `board-membership` 資料列（等於認定目前 `crud` 標記是遺漏，需視情況補 CR 修正 `crud` 欄位）。

## OQ-T-04-be-board-membership-03

[Level: F02-user-membership/uc-remove-member]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-19）
- 狀態：待處理

情況：【推論＋所本原文】
引用一（Scenario 標題，`spec-user-membership.md`「Board 權限管理」Feature）：『Scenario: Member 無法移除或升級成員』
引用二（該 Scenario 唯一的 When/Then）：『When "雅婷" 嘗試將自己升級為 "Owner" / Then 系統應該顯示錯誤訊息 "只有 Owner 可以變更成員角色"』
推論：這條 Scenario 標題同時提到「移除」與「升級」，但 Gherkin 步驟本身只測試了「升級」（角色變更），沒有任何步驟測試「非 Owner 嘗試移除成員」該顯示什麼錯誤訊息。implementation-loop T-04 實作 `BoardMembershipApplicationService.removeMember` 時，讓非 Owner 呼叫移除成員也回同一句「只有 Owner 可以變更成員角色」（重用 `uc-reject-role-change-by-member` 的訊息），理由是 Scenario 標題把「移除」也算在同一個情境裡；但規格沒有逐字驗收過這個假設，也可能是規格漏寫了移除專屬的錯誤訊息與 Scenario。
問題：非 Owner 嘗試移除成員時，錯誤訊息是否也應該是「只有 Owner 可以變更成員角色」？還是應該有獨立的訊息（例如「只有 Owner 可以移除成員」）？
選項：A. 維持現況，移除與角色變更共用同一句錯誤訊息（見 `BoardMembershipApplicationService.removeMember` 呼叫 `ensureOwner` 時傳入的訊息）；B. 另訂移除專屬的錯誤訊息，並補上對應的 fail Scenario（需要開 CR 補齊 `spec-user-membership.md`）。

## OQ-T-04-be-board-membership-04

[Level: F02-user-membership/uc-member-add-card]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 2 輪（2026-09-19）
- 狀態：待處理

情況：【推論＋所本原文】
引用一（`spec-user-membership.md` `uc-member-add-card`）：『pre: p1: "操作者是該 `board` 的 `board-membership` 成員"』『fail: {}』
引用二（`spec-user-membership.md` `uc-set-card-assignees`／`uc-assign-card-owner-by-drag`／`uc-list-card-assignee-candidates`／`uc-list-cards-by-assignee`）：『roles: [r-board-member]』
引用三（角色表 `r-board-viewer`）：『被邀請加入 Board 的唯讀角色，可檢視看板與相關統計圖表，不能新增／編輯／移動／刪除任何內容，也不能碰成員管理、看板結構或刪除 Board』
推論：這幾個 uc 的 `fail` 都是空的，沒有定義非成員／Viewer 呼叫時要回什麼訊息與 HTTP 狀態碼。implementation-loop T-04 這一輪修正 D-01 時，在 `BoardMembershipApplicationService` 新增 `ensureCanEdit`／沿用既有 `ensureMember`：新增卡片、設定／拖曳負責人（寫入類）拒絕非成員與 Viewer，訊息分別為「你沒有權限存取這個看板」（非成員）與「唯讀成員不能新增或編輯卡片」（Viewer），對應 HTTP 403；候選名單、依負責人查詢、成員名單（讀取類）只拒絕非成員（Viewer 可讀），訊息「你沒有權限存取這個看板」、HTTP 403。這個「讀取類允許 Viewer、寫入類排除 Viewer」的切分，以及訊息文字本身，規格都沒有逐字定義，是本輪依角色表字面意思做的推論。
問題：上述「讀取類 uc 允許 Viewer、寫入類 uc（新增卡片／設定負責人）拒絕 Viewer」的切分是否為規格真正意圖？非成員／Viewer 被拒絕時的訊息文字與 HTTP 狀態碼是否需要另外在 spec 定義？
選項：A. 維持現況（讀取類含 Viewer、寫入類排除 Viewer，訊息見 `BoardMembershipApplicationService.ensureMember`／`ensureCanEdit`）；B. 這幾個 uc 一律只檢查「是否為成員」，不細分 Viewer 能否寫入（等於恢復角色表對 Viewer 的限制只適用於看板結構／成員管理，不含卡片負責人）；C. 修 `spec-user-membership.md` 明確補上這幾個 uc 的 `fail` 定義（需要開 CR）。

## OQ-T-04-be-board-membership-05

[Level: F02-user-membership/uc-view-board-list]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 2 輪（2026-09-19）
- 狀態：待處理

情況：【兩處矛盾並列】
引用一（`spec-user-membership.md` `uc-view-board-list` post）：『列表只顯示我是 Owner 或 Member 的 `board`，不顯示我沒有權限的 `board`』
引用二（角色表 `r-board-viewer`）：『被邀請加入 Board 的唯讀角色，可檢視看板與相關統計圖表，不能新增／編輯／移動／刪除任何內容，也不能碰成員管理、看板結構或刪除 Board』
引用三（變更紀錄，2026-09-18）：『新增唯讀角色 `r-board-viewer`（ui-authoring-loop OQ-44 發現…）…既有 use case 的 roles 欄位是否要一併加入 `r-board-viewer`（例如各種檢視類 use case）尚未逐一檢視，見「待釐清」』
引用二說 Viewer「可檢視看板」，引用一卻把 `uc-view-board-list` 的顯示範圍限定在 Owner／Member；引用三本身也承認這件事尚未逐一檢視過。implementation-loop T-04 這一輪修正 D-02，選擇照引用一的字面把 Viewer 排除在看板列表之外（`BoardMembershipApplicationService.listBoardsForUser` 過濾掉 `role == VIEWER`），理由是這是目前唯一逐字定案的 post；但這會讓 Viewer 沒有列表管道找到自己被邀請的看板（除非另有直接網址或其他畫面），與引用二「可檢視看板」的敘述有落差。
問題：`uc-view-board-list` 是否也應該把 Viewer 的看板列進去？如果不列，Viewer 要怎麼「檢視看板」（引用二）？
選項：A. 維持現況，照 post 字面排除 Viewer（見 `BoardMembershipApplicationService.listBoardsForUser`），Viewer 找看板的方式留待其他 CR／畫面決定；B. 把 Viewer 併入列表（等於認定 post 字面是 `r-board-viewer` 角色新增前的舊文字、需要補 CR 修正 `uc-view-board-list` 的 post）。
