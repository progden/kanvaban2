# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 7）
- 目前階段：階段 1（F01 遷移）進行中
- 上一輪任務：T1.04｜結果：done
- 下一個任務：T1.05
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：無（F01 名詞定義／角色定義／Feature 標頭、Swimlane、Stage、Card 三個 Feature 的 usecase 區塊皆已完成；F01 只剩程序 10～11 正文清理與收尾）
- 已定義的共用 ID（實體／角色，供後續模組引用）：board（root）、swimlane（board）、stage（board）、card（root）；r-user；uc-add-swimlane、uc-rename-swimlane、uc-reorder-swimlane、uc-delete-swimlane、uc-add-stage、uc-rename-stage、uc-reorder-stage、uc-delete-stage、uc-set-stage-role、uc-add-card、uc-edit-card、uc-move-card-swimlane、uc-move-card-stage、uc-add-comment、uc-delete-card
- 最近 3 條假設：OQ-02（高影響，已登記）：「取消刪除卡片」Scenario 歸入 uc-delete-card 的 fail 分支（新增 pre.p2／fail.p2，掛 `@fail-p2`），不當成需要 card: write 的成功 Scenario；低影響：uc-move-card-stage 的 crud 補 board: R 以對齊既有 Aggregate 註解「board: read」；低影響：留言（comment）不另立實體，post 句改用 `card` 反引號滿足 ID 規則
- 待注意：無；目前 error 總數 292（F01 9、其餘未變）
