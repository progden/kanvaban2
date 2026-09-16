# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 5）
- 目前階段：階段 1（F01 遷移）進行中
- 上一輪任務：T1.03｜結果：done
- 下一個任務：T1.04
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：無（F01 名詞定義／角色定義／Feature 標頭已遷移；Swimlane、Stage 管理 Feature 的 usecase 區塊已完成；Card 尚未開始）
- 已定義的共用 ID（實體／角色，供後續模組引用）：board（root）、swimlane（board）、stage（board）、card（root）；r-user；uc-add-swimlane、uc-rename-swimlane、uc-reorder-swimlane、uc-delete-swimlane、uc-add-stage、uc-rename-stage、uc-reorder-stage、uc-delete-stage、uc-set-stage-role
- 最近 3 條假設：無高影響假設；低影響：Aggregate 註解由原本沿用的 `board:` 改為對應的實體層級（`stage:`／`card:`），與 uc 的 crud 對齊（同 T1.02 的做法）；「刪除空的 Stage」情境的 card 註解由 read 改為 read, write（與 uc-delete-stage 的 crud.card=U 對齊，比照 T1.02 的 Swimlane 案例）；「該操作應該被記錄為活動紀錄」併入各 uc 自身 post，不另立 log uc（同 T1.02 理由）
- 待注意：無；目前 error 總數 302（F01 19、其餘未變）
