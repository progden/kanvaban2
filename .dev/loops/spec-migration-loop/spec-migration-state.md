# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 4）
- 目前階段：階段 1（F01 遷移）進行中
- 上一輪任務：T1.02｜結果：done
- 下一個任務：T1.03
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：無（F01 名詞定義／角色定義／Feature 標頭已遷移；Swimlane 管理 Feature 的 usecase 區塊已完成；Stage、Card 尚未開始）
- 已定義的共用 ID（實體／角色，供後續模組引用）：board（root）、swimlane（board）、card（root）；r-user；uc-add-swimlane、uc-rename-swimlane、uc-reorder-swimlane、uc-delete-swimlane
- 最近 3 條假設：無高影響假設；低影響：Aggregate 註解改採實體層級（board／swimlane／card）而非僅 board／card，以對齊 GH-06 對 crud 的一致性檢查；「該操作應該被記錄為活動紀錄」併入各 uc 自身 post（不另立 log uc，因一個 Scenario 只能掛一個 `@uc-` tag，且新 uc 無成功 Scenario 會觸發 UC-06）；「刪除空的 Swimlane」情境的 card 註解改為 read, write（與 uc-delete-swimlane 的 crud.card=D 對齊，即使該情境卡片數為 0）
- 待注意：無；目前 error 總數 312（F01 29、其餘未變）
