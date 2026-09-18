# 平行 worktree loop — 狀態溝通與驅動腳本教訓

> 來源：`implementation-loop`（`.dev/loops/implementation-loop/`）2026-09-18 第一天實跑的 log review 與修正。
> 單線 loop 的共同規則見 [`無人值守文件-loop-建置與限制規則.md`](無人值守文件-loop-建置與限制規則.md)；本檔只記「平行、每條管線一個 git worktree、核准後 merge 回整合分支」這種跑法多出來的坑。
> 每則格式：現象（附證據）→ 根因 → 規則。

## 1. 跨回合溝通檔不可以是「大家共寫的檔」

- **現象**：T-11-fe-auth 與 T-02-be-board 並行。T-11 先合併成功；T-02 合併時 `.state/decision-log.md`／`open-questions.md`／`review.md`／`state.md`／`tasks.md` 五個檔全部 `CONFLICT (content)`（`pipeline-T-02-be-board-20260918-211720.log` 結尾）。兩邊還各自開了內容完全不同的 `OQ-IMPL-12`、`OQ-IMPL-13`，`D-05` 也兩邊都用。
- **根因**：單線 loop 留下來的習慣——所有回合往同一份紀錄檔尾追加、`state.md` 每輪覆寫、`tasks.md` 改狀態欄、ID 用全域流水號。單線沒事；平行時每條管線在自己的分支上改同一個檔的同一個位置，後合併的必衝突；各 worktree 互相看不到對方剛編的號，流水號必撞。`tasks.md` 就算各改各的列，相鄰列 git 也判衝突。
- **規則**：
  1. **一個 worktree 只寫自己任務的目錄**（`.state/tasks/<task-id>/`）。不同分支永遠不改同一個檔 → `.state/` 不可能衝突。
  2. **ID 以任務為命名空間**（`OQ-<task-id>-nn`、`ADR-<task-id>-nn`、`D-xx` 在任務內遞增），不用跨任務流水號。
  3. **狀態是單行檔** `tasks/<id>/status`，驅動腳本只認它；任務清單 `tasks.md` 只留靜態欄位，執行階段沒有人寫。
  4. 「打開一個檔看全部」的方便用唯讀的彙整腳本補（`scripts/collect.sh`），不要為了方便看而回到共寫檔。
  5. `merge=union` 只能當保險：它解得了「兩邊都追加」，解不了撞號、解不了覆寫型的檔、解不了表格相鄰列。
- **設計新 loop 時的檢查問題**：「如果兩條管線同時跑完，它們各自 commit 的檔案集合有沒有交集？」有交集就會衝突，跟內容寫得多小心無關。

## 2. 合併失敗一定要 `git merge --abort`

- **現象**：上面那次衝突之後，主 repo 停在 `MERGE_HEAD`、四個檔 `UU`；驅動腳本接著的 `git switch -` 報 `fatal: cannot switch branch while merging`，之後任何管線的 commit／merge 都會失敗。
- **根因**：腳本只檢查了合併的回傳碼、把任務標 blocked，沒有還原主 repo。主 repo 是所有管線共用的，壞一次等於全部停擺。
- **規則**：共用工作區上的每個 git 操作，失敗路徑都要把工作區還原成乾淨（`merge --abort`），再記錄、再標 blocked。另外主 worktree 固定停在整合分支，腳本不要替它切分支（`git switch X && … ; git switch -` 在「本來就在 X」時，`switch -` 會跳到不相干的上一個分支）。
- 延伸（同一天稍早、已修）：合併失敗被 `|| true` 吞掉 → worktree 照刪、成果沒進整合分支；主 repo 留著未 commit 的狀態標記 → 別條管線合併被 `local changes would be overwritten` 擋掉。**標記型異動也要 commit，或根本不要放在會被 merge 的路徑上。**

## 3. 提示詞的「禁止」擋不住工具，要在工具層級關掉

- **現象**：規則書明文禁止背景執行與 `Monitor`，但 `pipeline-T-02-be-board-20260918-200022.log` 裡 Dev 還是用了三次 `Monitor until … sleep 3` 加一次 `run_in_background` 的 gradle build，105 個 turn、約 $6，最後一句「I'll wait for the Monitor notification」行程就結束，**零 commit**；接著 Opus Review 花一整輪確認「Dev 什麼都沒交」。
- **根因**：`claude -p` 是一次性行程，沒有「之後的自己」收通知；模型在建置很慢時仍會習慣性背景化。
- **規則**：
  1. 能用旗標關的就不要靠提示詞：`--disallowedTools Monitor`、`CLAUDE_CODE_DISABLE_BACKGROUND_TASKS=1`。
  2. 驅動腳本要驗「這一輪有沒有產出」再往下走：Dev 前後 `git rev-parse HEAD` 沒變 → 不送 Review，直接重跑 Dev 並在 prompt 附註原因。便宜的檢查放在貴的回合前面。

## 4. zero-context 的 agent 不會知道自己第幾輪——要傳進去

- **現象**：Review 提示詞要求「達 `MAX_TASK_ROUNDS` 上限改標 blocked」、紀錄標題要寫第幾輪，但呼叫時只傳了任務 ID，agent 只能去數 `review.md` 裡的標題來猜。
- **規則**：凡是驅動腳本知道、agent 需要的執行期事實（任務 ID、回合數、上限、角色、上一輪出了什麼狀況），都放在 prompt 最前面的固定欄位傳入，並在提示詞寫明「以傳入的為準，不要自己推算」。agent 之間靠檔案溝通；**驅動腳本對 agent 靠 prompt 標頭溝通**。

## 5. 狀態轉移要有「寫了沒」的檢查，兩邊都要

- **風險**：Review 在 `review.md` 寫了判定卻沒改狀態 → 驅動腳本讀到 `review-pending`，白跑一整輪 Dev＋Review（T-00 曾因類似的「讀不到 done」白跑 5 輪、約 $3）。
- **規則**：
  1. 提示詞把判定寫成「三選一 → `status` 寫什麼」的表（完成 `done`／繼續 `doing`／Block `blocked`），收尾加自我檢查（`cat status` 不可以還是 `review-pending`、`git status --short` 為空）。
  2. 驅動腳本也防呆：Review 後仍是 `review-pending` → 只補跑 Review（附註原因），不要進 Dev。
  3. 最後一輪（回合 N == 上限）不能再寫 `doing`，要 `blocked`——這也是為什麼一定要把回合數傳進去。

## 6. 巡視訊息跟著「事件」印，不要跟著「時鐘」印

- **現象**：主迴圈 `sleep 60` 輪詢，每分鐘印一次「本輪並行管線數…」；沒有管線在跑、剩下的 todo 依賴又不滿足時，每 30 秒印一次「有 todo 任務但依賴尚未滿足」直到 `MAX_ITERATIONS`。
- **規則**：
  1. 用 `wait -n "${PIDS[@]}"` 睡到任一管線結束才醒，狀態有變才巡視、才印訊息。
  2. 「沒有管線在跑＋沒有可派的任務」是**死結**，不是等待：不會再有任何事件改變狀態。印一次「誰被哪個依賴（什麼狀態）卡住」就停。
  3. 驅動腳本自己的輸出也要落檔（`exec > >(tee …)`），不然事後沒辦法 review 巡視過程——這次 log review 就只有管線 log、沒有驅動腳本的 log。

## 7. 「必須停下交人工」：能給腳本判斷的就不要給 LLM 判斷

把第一天 log 裡所有「其實該停下／白跑」的情境分三類：

| 類別 | 情境（log 裡都實際發生過） | 做法 |
|---|---|---|
| 腳本直接判斷 | worktree／合併失敗、達回合上限、回合以提問結尾（「要我…嗎？」「let me know」）、被中斷／逾時／`Exit code 137`、整輪沒 commit、Review 沒寫判定、agent 把檔案改到主 repo（三個任務都犯過，是 T-10 合併被擋的真正原因） | 驅動腳本看 git 與收尾紀錄，不讀 agent 的最後回覆 |
| 結構化之後腳本可判斷 | OQ 會不會擋任務、保留事項有沒有人接手（四次「附保留核准」留下 10 條保留，只有 1 條點名接手者）、同一種原因反覆開 OQ（設計稿打不開，每個前端任務都會撞一次）、同一個問題反覆退回 | OQ／D-xx 由工具寫入，等級、阻塞、接手、原因代碼、延續 變成必填參數 |
| 只能靠 LLM | 規格沒寫或含糊、兩段定稿文字矛盾時該不該停、上游程式碼跟預期不符、任務範圍切錯、測試存在但沒驗證力（替身自我驗證） | 提示詞給一句可操作的檢驗；Review 逐則核對 Dev 標的等級 |

規則：

1. **agent 改 `.state/` 只有一個入口（`scripts/loopctl`）**。只「規定格式」還是要另寫檢查器、agent 也會寫錯；由工具寫，格式只存在於工具裡，編號、標題、commit 都不會錯。工具拒絕時要把「該怎麼修正」講清楚，agent 會照訊息重試。
2. **每輪必須以 `loopctl finish` 收尾，驅動腳本只認收尾紀錄**。以提問結尾、逾時、被砍、沒 commit、漏改狀態，全部收斂成同一種結果（沒有收尾＝這輪失敗），腳本不用分辨原因、也不用解析自然語言。連續兩輪失敗就 blocked（多半是環境問題，再跑只是燒錢）。
3. **「詢問」只有一條路：開 OQ**。提示詞明講「沒有人會讀你的最後回覆、也沒有人會回答」；不擋任務的 OQ 照推論做完，擋任務的 OQ 配 `finish --verdict blocked`。
4. **狀態轉移的規則寫進工具，不是寫進提示詞**：Dev 不能給 `done`；`doing` 一定要有本輪新的 D-xx；最後一輪不能 `doing`；`blocked` 一定要有阻塞的 OQ；有 todo 的 D-xx 不能交件／核准；同一個問題第 3 次退回必須 blocked。
5. 真正只能靠 LLM 判斷「要不要停」的，給一句檢驗：**「把這個任務做完，是否必須違反某一段已定稿的原文？」**是 → 覆蓋等級、阻塞；否（沒寫，或兩份矛盾但照較上游那份做就不用動定稿文字）→ 高風險、不阻塞、開 OQ 照做。
6. 工具保證的是格式與必填，保證不了內容對（把該阻塞的標成不阻塞）；這一段仍要靠 Review 逐則核對，而且要寫進 Review 的檢查項。
7. 驅動腳本另外檢查「繞過工具」：回合內動到 `.state/tasks/` 的 commit 若沒有工具的 trailer，這一輪算失敗。

## 8. 其他

- **輸出語言要寫進提示詞**：沒寫時 17 個回合的最後回覆是英文。全域 CLAUDE.md 的語言規則對 `-p` 行程的「最後回覆」約束力不夠，Dev／Review／Planning 提示詞各自明講 zh-TW。
- **並行 worktree 共用 `~/.gradle`**：daemon 互搶（`1 busy Daemon could not be reused`），規則書統一 `./gradlew … --no-daemon`。
- **agent 不碰分支**：log 裡有 agent 在 worktree 內 `git checkout loop/implementation`。明文禁止 `switch`／`checkout` 其他分支、`merge`／`rebase`／`stash`；要看整合分支用 `git show <branch>:<path>`。
- **驅動腳本標的 blocked 也要留原因**（`tasks/<id>/driver-note.md`），不然外部驗證與人工都只看到 blocked、不知道為什麼。
- **改驅動腳本後用假 `claude` 演練**：拋棄式 repo＋PATH 前面放一支假 `claude`（依 prompt 標頭的任務／回合／角色寫檔、commit），至少涵蓋：兩個相鄰列任務並行合併、Dev 零產出、Review 漏改狀態、程式碼真衝突、死結。這次五個情境十幾秒跑完，不花 token。
