# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 6）
- 目前階段：階段 1（F01 遷移）進行中
- 上一輪任務：D-01｜結果：done
- 下一個任務：T1.04
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：無（F01 名詞定義／角色定義／Feature 標頭已遷移；Swimlane、Stage 管理 Feature 的 usecase 區塊已完成，且活動紀錄已在 crud／Aggregate 註解補回 board 寫入；Card 尚未開始）
- 已定義的共用 ID（實體／角色，供後續模組引用）：board（root）、swimlane（board）、stage（board）、card（root）；r-user；uc-add-swimlane、uc-rename-swimlane、uc-reorder-swimlane、uc-delete-swimlane、uc-add-stage、uc-rename-stage、uc-reorder-stage、uc-delete-stage、uc-set-stage-role
- 最近 3 條假設：OQ-01（高影響，已登記）：活動紀錄不另立 uc、不 emits 事件，改由各 uc 的 post 與 `crud.board=U`／Aggregate 註解 `board: read, write` 表達，依據 F01 9 個 uc 成功 Scenario 與 F02 legacy 實作備註；T2.01 定義 ActivityRecord 時要回頭檢視此假設是否仍適用；無其他新的低/高影響假設
- 待注意：無；目前 error 總數 302（F01 19、其餘未變）
