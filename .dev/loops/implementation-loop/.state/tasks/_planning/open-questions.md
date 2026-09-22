# 安排階段 待決事項（`_planning`）

> 安排階段（`planning-prompt.md`）開立的 OQ，ID 格式 `OQ-PLAN-<兩位數>`。任務各自的 OQ 在 `.state/tasks/<task-id>/open-questions.md`。

## OQ-PLAN-01

[Level: implementation-loop/T-14-fe-board-item、T-15-fe-member-management]
- 等級：高
- 阻塞：是（這兩列是目前唯二非 `done` 的任務，且安排階段無權解除）
- 接手：人工
- 原因代碼：merge-conflict
- 開立：安排階段第 3 次重跑（2026-09-22）
- 狀態：**已解除（2026-09-22，人工採選項 A）**

解除說明：人工依序 `git merge impl/T-14-fe-board-item`、`git merge impl/T-15-fe-member-management`，手工解 `boardApi.ts`／`cardApi.ts`／`BoardCanvasPage.tsx` 的新增內容衝突（各自新增不同 API 函式／import／`registerItemComponent` 呼叫，功能無重疊，直接串接），另修正兩處因後續任務改了共用簽章（`ItemContentProps.boardId`、`Card.delete`／`assignTo` 的 `Instant now`）導致的編譯錯誤。`./gradlew clean build`、`pnpm build`／`pnpm test` 皆全綠後把兩個 `status` 改回 `done`，並合併進 `loop/implementation`（commit `2fd755e`、`162d7d3`）。詳見 `.state/tasks/T-14-fe-board-item/review.md`、`.state/tasks/T-15-fe-member-management/review.md` 各自的「人工複核」段。

情況：【推論＋所本原文】

引用一（`.state/tasks/T-14-fe-board-item/driver-note.md` 全文）：
『- 2026-09-22 20:58:20 驅動腳本標 blocked：T-14-fe-board-item worktree 內已判定 done，但合併回 loop/implementation 失敗，需人工介入』

引用二（`.state/tasks/T-15-fe-member-management/driver-note.md` 全文）：
『- 2026-09-22 20:19:25 驅動腳本標 blocked：T-15-fe-member-management worktree 內已判定 done，但合併回 loop/implementation 失敗，需人工介入』

引用三（`iteration-prompt.md` 第 4 節）：
『一條管線內部：Dev 輪 → Review 輪 → 若 Review 核准（狀態轉 `done`）→ 合併 `impl/<task-id>` 回整合分支 `loop/implementation`（合併需序列化：驅動腳本用檔案鎖；合併失敗會 `git merge --abort` 並把任務標 `blocked`，不會把主 repo 留在合併中）→ 移除該 worktree』

引用四（`.state/tasks.md`「規則」段）：
『**`blocked` 解除**：只有人工，或安排階段重跑確認缺的依據已補齊後，才能把 `blocked` 改回 `todo`。』

引用五（`planning-prompt.md` 任務清單格式段）：
『**不可以改已經存在的 `status` 檔**（那是驅動腳本／Dev／Review 的執行進度），除非是把已補齊依據的 `blocked` 改回 `todo`。』

引用六（`.state/adr/ADR-T-17-fe-clock-control-01-item-component-registration-convention.md`「後果（Consequences）」第 1 條）：
『T-14／T-15／T-16／T-18／T-19／T-20 若沿用本慣例，会各自在 `BoardCanvasPage.tsx` 加一行 `registerItemComponent(...)` 呼叫與一行 import，多個平行分支同時改同一個檔案的相鄰位置，合併時大機率需要人工／驅動腳本解衝突（非邏輯衝突，只是同檔案多處新增）。』

推論（以下皆為安排階段的實測與推論，規格／規則書沒有直接寫）：

1. 本輪在暫時 worktree（`git worktree add --detach`，用完已 `git worktree remove`，未動主 repo）對現況 `HEAD`（`bf0a066`）做 `git merge --no-commit --no-ff` 乾跑，兩條分支的衝突檔如下：
   - `impl/T-14-fe-board-item`：`.state/tasks/T-14-fe-board-item/status`、`kanban-frontend/src/api/boardApi.ts`、`kanban-frontend/src/api/cardApi.ts`、`kanban-frontend/src/pages/BoardCanvasPage.tsx`
   - `impl/T-15-fe-member-management`：`.state/tasks/T-15-fe-member-management/status`、`kanban-frontend/src/pages/BoardCanvasPage.tsx`
2. 兩條分支各自的 `status` 檔在分支側是 `done`（`git show impl/<branch>:…/status` 皆為 `done`），在 `loop/implementation` 側被驅動腳本改成 `blocked`，屬雙邊修改同一行，必然衝突；這是合併失敗的**結果**被記回來造成的二次衝突，不是原始原因。
3. 原始原因應是 `BoardCanvasPage.tsx`（以及 T-14 的 `boardApi.ts`／`cardApi.ts`）：T-14～T-20 七條前端管線平行執行，每條都要在同一個檔案加 import 與 `registerItemComponent(...)`；T-16／T-17／T-18／T-19／T-20 先合併成功，T-15（20:19）、T-14（20:58）落後而撞上。ADR-T-17 的「後果」第 1 條已預測到這個情形。
4. 兩條分支的 Review 都已在 worktree 內核准（`852aa29`「T-14-fe-board-item Review 第 2 輪：收尾，status=done」、`c2bce05`「T-15-fe-member-management Review 第 2 輪：收尾，status=done」），程式碼產出存在且已通過該任務自己的驗證，**不是缺規格依據**。因此不符合引用四／五允許安排階段自行解除 `blocked` 的條件（「缺的依據已補齊」）——缺的是一次人工解衝突合併，不是依據。
5. 把 `status` 改回 `todo`會有實質損害：驅動腳本會重新建一個全新 worktree 從 Dev 第 1 輪重跑，丟掉 `impl/T-14-fe-board-item`／`impl/T-15-fe-member-management` 上已核准的成果。故本輪不改這兩個 `status` 檔。

問題：`T-14-fe-board-item`／`T-15-fe-member-management` 這兩條已在 worktree 內核准、但合併回 `loop/implementation` 失敗的分支，要用哪種方式收尾？

選項：
- A. 人工在 `loop/implementation` 上依序 `git merge impl/T-15-fe-member-management`、`git merge impl/T-14-fe-board-item`，手工解 `BoardCanvasPage.tsx`（併入雙方的 import 與 `registerItemComponent` 呼叫）、`boardApi.ts`／`cardApi.ts`（併入雙方新增的 API 函式），`status` 檔取分支側的 `done`；合併後跑一次 `pnpm test` 與 `./gradlew clean build` 確認，再把兩個 `status` 改成 `done`。（不需新增任務列。）
- B. 為這兩條各開一列修正任務（例如 `T-14-fe-board-item-r2`），讓 loop 自己在新 worktree 上重做合併與解衝突。（代價：修訂實例的定義是「spec 又因 CR 變動」，這裡 spec 沒變動，產出範圍寫不出來，與 `iteration-prompt.md` 第 3 節第 4 條不符。）
- C. 依本 OQ 的推論 3，把 Canvas item 註冊機制改成 ADR-T-17「考慮過的替代方案」裡被否決的集中式註冊表，或其他不需多分支共寫同一檔的機制，再重跑這兩列。（這是 OQ-49 整合機制的範圍，屬結構性決定，要先走 CR。）

安排階段建議：選項 A。兩條分支的衝突都是同檔案多處新增的文字衝突（ADR-T-17 已預判為「非邏輯衝突」），人工一次合併即可結案；選項 C 的機制改動可另案評估，不必卡住這兩列。

## 附記：2026-09-22 前兩次安排階段沒有開立 OQ

第 1、2 次重跑時沒有任務因缺依據而標 `blocked`——F01～F06 的 spec 三張表與 F01～F07 的 `ui-*.md` 皆已齊備，`.dev/ui-prototype/` 的設計稿也已匯出；當時唯一追加的修訂實例 `T-08-be-feature-cr-board-r2` 的 `@CR-013` tag 掛在 spec 中存在的 `uc-view-feature-cr-board` 上，範圍有明確依據，不適用第 5-3 步。詳見 `decision-log.md`。
