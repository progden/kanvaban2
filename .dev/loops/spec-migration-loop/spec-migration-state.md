# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 10）
- 目前階段：階段 1（F01 遷移）已完成，等待 G1 關卡核准
- 上一輪任務：D-02｜結果：done
- 下一個任務：G1（關卡，等審查輪；審查通過後下一個執行輪把它標 done，再進入 T2.01）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（名詞定義／角色定義／Feature 標頭、Swimlane、Stage、Card 三個 Feature 的 usecase 區塊、正文反引號清理、變更紀錄追加 CR-005、uc-delete-card 的 pre/post 措辭修正皆已完成；F01 全檔 0 error，剩 1 筆 UC-13 warn：實體 "board" 沒有任何 Use Case 建立它，屬正常提示，未記 OQ）
- 已定義的共用 ID（實體／角色，供後續模組引用）：board（root）、swimlane（board）、stage（board）、card（root）；r-user；uc-add-swimlane、uc-rename-swimlane、uc-reorder-swimlane、uc-delete-swimlane、uc-add-stage、uc-rename-stage、uc-reorder-stage、uc-delete-stage、uc-set-stage-role、uc-add-card、uc-edit-card、uc-move-card-swimlane、uc-move-card-stage、uc-add-comment、uc-delete-card
- 最近 3 條假設：新增 OQ-03（uc-delete-card 的 pre.p2／fail.p2／post 措辭方向修正，因 OQ-02 原文寫反）；沿用 OQ-01（活動紀錄不另立 uc）、OQ-02（取消刪除卡片歸入 uc-delete-card 的 fail 分支）
- 待注意：F01 本身已 0 error，整體 error 總數仍 283（F01 0、其餘 F02～F06 未變）；G1 需審查輪先跑
