# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 3）
- 目前階段：階段 1（F01 遷移）進行中
- 上一輪任務：T1.01｜結果：done
- 下一個任務：T1.02
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：無（F01 名詞定義／角色定義／Feature 標頭已遷移，usecase 區塊未開始）
- 已定義的共用 ID（實體／角色，供後續模組引用）：board（root）、swimlane（board）、card（root）；r-user
- 最近 3 條假設：無高影響假設；低影響：留言／活動紀錄不建實體（沿用 board/card aggregate 的 crud，仿照 fixture 範本）、角色只設一個 r-user（三個 Feature 的「身為」都相同）、card.due-date 用 kebab-case（欄位 ID 不可含大寫）
- 待注意：無；目前 error 總數 321（F01 38、F02 139、F03 62、F04 63、F05 44、F06 41）
