# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 8）
- 目前階段：階段 1（F01 遷移）進行中
- 上一輪任務：T1.05｜結果：done
- 下一個任務：T1.06
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：無（F01 名詞定義／角色定義／Feature 標頭、Swimlane、Stage、Card 三個 Feature 的 usecase 區塊、正文反引號清理、變更紀錄追加 CR-005 皆已完成；F01 全檔 error 數已降到 0，剩 T1.06 收尾確認）
- 已定義的共用 ID（實體／角色，供後續模組引用）：board（root）、swimlane（board）、stage（board）、card（root）；r-user；uc-add-swimlane、uc-rename-swimlane、uc-reorder-swimlane、uc-delete-swimlane、uc-add-stage、uc-rename-stage、uc-reorder-stage、uc-delete-stage、uc-set-stage-role、uc-add-card、uc-edit-card、uc-move-card-swimlane、uc-move-card-stage、uc-add-comment、uc-delete-card
- 最近 3 條假設：無新增（本輪為低影響格式清理，未產生新 OQ）；沿用 OQ-01（活動紀錄不另立 uc）、OQ-02（取消刪除卡片歸入 uc-delete-card 的 fail 分支）
- 待注意：F01 本身已 0 error，但因 F06 引用 F01 檔案路徑等其他檔問題，整體 error 仍高；目前 error 總數 283（F01 0、其餘未變）
