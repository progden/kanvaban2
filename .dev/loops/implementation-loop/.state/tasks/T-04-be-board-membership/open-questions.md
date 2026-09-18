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
