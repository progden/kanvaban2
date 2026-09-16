# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 18）
- 目前階段：階段 2（F02 遷移）進行中
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.07（[F02]「卡片負責人指派」usecase 區塊＋tag＋Aggregate 註解）｜結果：done
- 下一個任務：T2.08（[F02]「檢視看板活動紀錄」usecase 區塊＋tag＋Aggregate 註解；若有 emits 事件用 requires 接上）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02 已完成「建立使用者帳號」「使用者登入與登出」「Board 建立與成員邀請」「Board 權限管理」「Board 存取權限」「卡片負責人指派」，F02 error 數由 39 降為 26（全部 spec 由 197 降為 184）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）；F02 已定義實體 user、board-membership，角色 r-system-user、r-board-owner、r-board-member；本輪新增 uc：uc-set-card-assignees、uc-list-card-assignee-candidates、uc-view-card-assignees、uc-list-cards-by-assignee、uc-assign-card-owner-by-drag，roles 皆為 [r-user]（依 D-03／OQ-04／OQ-05 決議），僅屬「卡片負責人指派」Feature
- 最近 3 條假設：低影響假設（本輪）：依「同一個 When 動作」原則把 7 條 Scenario 拆成 5 個 uc（編輯畫面設定/移除歸同一 uc、拖曳新增/idempotent 歸同一 uc、選單列表／未指派顯示／依負責人查卡片各自獨立為讀取 uc）；post 涉及 card.assignees 的讀取型 uc 改用「未指派負責人的 card」「以指定成員為負責人的 card」等描述避免觸發 UC-07（entity.attr 只能出現在含 C/U 的 uc post）；Aggregate 註解 boardMembership → board-membership，且 Scenario「卡片可以沒有負責人」的 card 標記由 write 改為 read 以對齊新的讀取 uc
- 待注意：全部 spec error 總數 184；Iteration 11 標題含兩個任務編號、不符 HEADING 正則，依鐵則 4 不可再修改；F02 尚有 T2.08～T2.09 待做
