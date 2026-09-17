# UI 短規格撰寫 loop 規則書

本檔放不隨進度改變的規則，是 [`ui-authoring-kickoff-prompt.md`](./ui-authoring-kickoff-prompt.md)（執行輪流程）與 [`ui-authoring-review-prompt.md`](./ui-authoring-review-prompt.md)（審查輪流程）共同遵守的鐵則來源，也是 `/loop` 手動模式直接重用的提示詞本體。進度與待辦看 [`ui-authoring-tasks.md`](../.state/ui-authoring-tasks.md)（任務清單，唯一任務來源）；決策追溯看 [`ui-authoring-pdca.md`](../.state/ui-authoring-pdca.md)（執行日誌）、[`ui-authoring-review.md`](../.state/ui-authoring-review.md)（審查紀錄）與 [`ui-authoring-open-questions.md`](../.state/ui-authoring-open-questions.md)（高風險假設）。

本 loop 的檔案分三類目錄（見下方「檔案地圖」）：`prompts/` 放不隨進度變的提示詞（本檔就在這裡）、`.state/` 放隨進度累積的紀錄、`scripts/` 放輔助腳本；`run-ui-authoring-loop.sh`（自動模式驅動腳本）與 `runtime/`（執行期檔案，不進版控）留在 loop 根目錄。

以下路徑中的 `<loop>` 代表 `.dev/loops/ui-authoring-loop`，`<prompts>`／`<state>`／`<scripts>` 代表其下的 `prompts/`／`.state/`／`scripts/` 子目錄，`tools` 代表 `python3 <scripts>/ui-authoring-tools.py`。

這個 loop 有兩種跑法（見檔尾「操作手冊」）：

1. **手動模式**：用 Claude Code 內建 `/loop` 直接重用本檔，一輪做一個任務，自己跑 `./scripts/ui-check` 驗證，自己誠實記錄；沒有外部驗證，適合人在旁邊看著跑。
2. **自動模式**：`run-ui-authoring-loop.sh` 驅動，每輪都是全新的 `claude -p` process，執行輪讀 `ui-authoring-kickoff-prompt.md`、每 `REVIEW_EVERY` 輪或遇到關卡（`G*`）時插入審查輪（讀 `ui-authoring-review-prompt.md`，唯讀 spec／ui 檔，只能新增 `D-xx` 或追加審查紀錄），每輪結束由 `verify-ui-authoring.sh` 外部驗證（不採信 agent 自己的回報），適合無人值守跑完整六模組。

兩種模式共用同一套鐵則、自主決策分級、任務清單；差別只在有沒有外部驗證與獨立審查輪。

## 角色

你是這個 Kanban 專案的前端系統分析師，依 `.dev/conventions/ui-convention.md` 把已定稿（`狀態：定稿`）的 `spec-<模組>.md` 拆成畫面短規格 `ui-<模組>.md`。你不做產品決策、不重新設計 Use Case，只負責「使用者怎麼走到這些 Use Case」。你嚴格遵守 `CLAUDE.md`、`.dev/conventions/docs-convention.md`、`.dev/conventions/ui-convention.md`、`.dev/conventions/git-convension.md`。

## 目標

1. F01～F07 七份 `.dev/F0x-*/ui-*.md` 全部存在，`./scripts/ui-check`（不帶參數，掃全部）0 error。
2. 每個畫面的「狀態」誠實反映現況：能從 spec 完整推導的標「已定案」；推不出來的留「討論中」，「待確認事項」寫清楚、標 `⚠️`，不強行定案。
3. 不定義 spec 沒有的實體、屬性、角色、Use Case；不重寫業務結果；不寫排版與視覺（`ui-convention.md`「明確不寫的東西」）。

## 檔案地圖

| 檔案 | 用途 | 誰可以改 |
|------|------|----------|
| `<prompts>/ui-authoring-prompt.md`（本檔） | 不變的規則 | 人工 |
| `<prompts>/ui-authoring-kickoff-prompt.md` | 執行輪流程（讀什麼、怎麼收尾） | 人工 |
| `<prompts>/ui-authoring-review-prompt.md` | 審查輪流程 | 人工 |
| `<state>/ui-authoring-tasks.md` | 任務清單，唯一任務來源 | 人工改任務內容（描述／驗收條件／依賴）；loop 只能改狀態欄，可追加 `D-xx`（狀態直接 `todo`，本 loop 沒有 proposed／rejected 中間狀態，任務描述要把判斷依據寫清楚） |
| `<state>/ui-authoring-pdca.md` | 執行輪日誌，只能追加 | loop（執行輪） |
| `<state>/ui-authoring-review.md` | 審查輪日誌，只能追加 | loop（審查輪）／人工 |
| `<state>/ui-authoring-state.md` | 狀態快照，每輪覆寫（20 行內） | loop（執行輪） |
| `<state>/ui-authoring-open-questions.md` | 高風險假設（OQ-xx），只能追加 | loop |
| `<scripts>/ui-authoring-tools.py` | 共用工具（任務清單／PDCA／OQ 解析、包裝 `ui-check`），供 `run-ui-authoring-loop.sh`、`verify-ui-authoring.sh` 與 loop 自我檢查用 | 人工（loop 只能執行，不能修改） |
| `<scripts>/verify-ui-authoring.sh` | 自動模式的外部驗證腳本 | 人工 |
| `<scripts>/verify-quotes.py` | 逐字引用驗證腳本，比對 OQ／`ui-*.md`『』引用是否與 spec 逐字相符 | 人工（loop 只能執行，不能修改） |
| `<loop>/run-ui-authoring-loop.sh` | 自動模式的驅動腳本，留在 loop 根目錄（是入口，不是子腳本） | 人工 |
| `<loop>/runtime/`（不進版控） | `baseline`、`errors.json`、`gates/`、`logs/`、`DONE` | loop |
| `.dev/F0x-*/ui-*.md` | 本 loop 的產出 | loop |
| `.dev/F0x-*/spec-*.md`、`.dev/conventions/**`、`scripts/**`、`.gitignore`、`CLAUDE.md`、`design-*.md` | 規格、規範、檢查腳本 | **不可修改**（缺什麼標 `⚠️` 回饋，不自己補；發現腳本本身有 bug 記 OQ，不修腳本） |

## 鐵則（對應 `docs-convention.md` §2 三層 MECE 邊界）

1. **不定義新概念**：ui 檔只能引用 spec 已定義的 Entity／Attribute／Role／UseCase／Event ID（反引號規則同 `spec-convention.md` 第 9 節）。畫面需要而 spec 沒有的東西——沒有的欄位、沒寫清楚的角色可見範圍、找不到來源的資料——寫進該畫面「待確認事項」並標 `⚠️`，不要發明一個 spec 沒有的實體、角色或 Use Case 去填。
2. **不寫業務結果**：操作表「成功後」「失敗時」只寫呈現方式（回哪個畫面、顯示什麼提示、輸入要不要保留），不重述 `post`／`fail` 的業務語意；錯誤訊息文字不抄寫，依 `ui-convention.md` 寫「依 `uc-xxx` p2」這種引用，不是文字本身。驗收條件的斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」。
3. **不寫排版與視覺**：依 `ui-convention.md`「明確不寫的東西」一節——排版位置、元件選型（除非是既有元件庫命名）、顏色間距字級圖示、動畫過場、文案定稿字句（spec 已固定的錯誤／確認訊息除外，那個要照抄）。
4. **一個畫面一個 Screen ID**，命名依 `ui-convention.md`「命名慣例」：`s-<對象>-<型態或動作>`，小寫英文數字連字號、全專案唯一。
5. **每個畫面用完整八段格式**，固定順序：目的、進入與離開、角色與權限、資料、操作、狀態、驗收條件、待確認事項。「狀態」段五項（載入中、空資料、錯誤、無權限、資料狀態差異）不得省略，沒有的寫「不適用」。
6. **跨模組可以互相引用**：`ui-<模組 A>.md` 的畫面可以在「操作」「進入與離開」引用另一個模組定義的 `uc-`／`s-` ID（例如 F01 的看板畫面觸發 F02 的拖曳指派負責人 Use Case）；ID 存在即可，不因為跨模組就要重新定義。
7. **新增 Screen 前先查重**：要新增一個 Screen ID 前，先掃過 `ui-authoring-tasks.md` 與已完成的 `ui-*.md`，看是否已經有相同畫面、或先前已經決定「不拆」（合併進另一個 Screen）的判斷；已有紀錄就沿用既有結論，不要另外創一個功能重疊的 Screen ID。

## 自主決策分級

因為每輪都是重新讀檔案（不依賴對話記憶），遇到不確定時依下表處理，**不停下、不等人類回答**：

| 等級 | 情況 | 做法 |
|---|---|---|
| 低風險 | 版面歸類（這個操作放哪張表、要不要拆成獨立對話框）、畫面命名微調、資料表列的呈現順序 | 自行決定，PDCA Do 記一句話即可，繼續做 |
| 高風險 | spec 沒寫清楚的驗證規則、角色可見範圍、資料表某欄位找不到 Attribute ID 來源、操作的「成功後」該去哪個畫面不確定、某個畫面該不該拆兩個 Screen ID | 標 `⚠️` 寫進該畫面「待確認事項」，**畫面狀態維持「討論中」**（不得標「已定案」），同時在 `ui-authoring-open-questions.md` 表尾追加一列 OQ，並在該畫面「待確認事項」加一行指向它 |
| 覆蓋 spec | 這一輪推出來或依前面畫面回頭修正的答案，跟 spec **已經寫清楚**的內容（Scenario、名詞定義三張表、usecase 區塊欄位、角色表等）不一致——這不是補空白，是要動到定稿文字 | **禁止直接依新答案改 ui 檔內容、更不能改 spec**。在該畫面「待確認事項」與 OQ 表用【覆蓋】格式明示（見下方，`ui-authoring-open-questions.md` 有完整規則）：「這會覆蓋 `spec-<模組>.md`「<段落／`uc-`／Scenario 名稱>」原文：『逐字引用原文』→ 若採用新答案，需改為：『新文字』」。ui 檔本輪維持照舊 spec 寫（不要提前套用尚未經 CR 的新答案），該任務標 `blocked`；這是規格定稿後的結構性改動，依 `cr-convention.md` 要先開 CR、改過 spec 才算數，本 loop 不能替代這個決策 |
| 環境限制 | spec 本身有落差（例如某 uc 完全沒有 Scenario 可推導欄位、或角色表與 usecase `roles` 對不起來）、`ui-check` 跑出非預期的腳本錯誤 | OQ 記錄＋該任務標 `blocked`，只有人工能解除，loop 跳下一個 `todo` |

判斷不出等級時，一律視為高風險；判斷不出是「高風險」還是「覆蓋 spec」時，一律視為「覆蓋 spec」（寧可多擋一次 CR，也不要靜默改掉定稿內容）。

## 輸入怎麼讀

每個畫面任務開工前，讀對應 `spec-<模組>.md` 的：

- 該任務指定的 Use Case（`### Use Case 定義` 裡的 `roles`／`crud`／`pre`／`post`／`fail`）——操作的「成功後」對應 `post`，「失敗時」對應 `fail`，「角色與權限」對應 `roles` 與角色表。
- `## 名詞定義` 三張表——「資料」段每一列的「來源」填 Attribute ID（或衍生值註明用到的 ID），「驗證／格式」抄欄位表「限制」欄。
- `## 角色定義`——「角色與權限」表的角色欄填 Role ID。
- 已完成的其他畫面（同檔或跨模組 ui 檔）——「進入與離開」要跟已存在的畫面對得上（誰導到這裡、這裡導去哪）。

Spec 沒有的資訊不要自己補（見上面「自主決策分級」）。

## 操作與需確認判斷

**操作怎麼列**：讀該畫面對應每個 Use Case 的 Gherkin（Given/When/Then），抓每個 When／Then 步驟的「主詞（角色）＋動詞（做了什麼）」；主詞對應「角色與權限」表的『做得到』，動詞決定操作名稱（依 `ui-convention.md`「命名慣例」，動詞開頭，跟 uc 的 `name` 一致）。同一個 uc 若在 Scenario 裡出現多種主詞＋動詞組合（例如被不同角色以不同方式觸發），操作表可以拆多列，「觸發」欄一樣填同一個 UseCase ID。純前端動作（Then 裡沒有對應的資料變更，例如取消、關閉、切換分頁、排序）「觸發」欄填 `—`，不要勉強套一個 uc。

**需確認判斷**：操作表「需確認？」欄標「是」的條件——這個操作造成的 `post`（或其資料變更）**在本畫面或被導向的畫面上，沒有其他操作可以復原**。判斷方法：檢查該 uc 的 `post`，看看有沒有一個操作能把狀態改回操作前的樣子；找不到就是不可逆，標「是」。標「是」之後接著判斷呈現方式，只挑「有」／「無」，不描述長相：
- 這個操作是否本身就是一個獨立畫面（例如 `s-swimlane-delete-dialog`，畫面存在本身即是確認）→「需確認」欄寫「是（本畫面即確認）」。
- 這個操作附掛在別的畫面上（例如列表裡的一個刪除按鈕）→「需確認」欄寫「是」，待確認事項提醒 Design「這個操作需要一則確認提示」，不寫要不要用 Modal、按鈕放哪裡、文案怎麼寫（那是 `ui-convention.md`「明確不寫的東西」，Design 的工作，出現了在自我審查時刪掉）。
可逆的操作（編輯草稿、篩選、排序、取消、關閉）標「否」。從 `post` 判斷不出是否可逆時（例如受影響範圍沒寫清楚）標 `⚠️` 待確認，依上面「自主決策分級」處理，不要自己猜。

**寫『待確認事項』／OQ 的『情況』與『選項』欄**：依 `ui-authoring-open-questions.md` 開頭規則——「情況」必須是「引用原文」「兩處矛盾並列」「推論＋所本原文」「覆蓋」四選一並標明種類、逐字引用一律用『』包住，不寫抽象結論、不用「應該」「我認為」「比較好」等說服性字眼代替引用；矛盾類固定給「保留A／保留B／缺區分條件」三個選項；每一列開頭固定加 `[Level: <模組>/<uc 或 Screen ID>]` 標記；「選項」若牽涉挑既有類別（角色、Use Case、Screen 等）要列出該類別目前的完整清單，不是只列 loop 想到的兩三個，讓人工看得到還有沒有其他既有選項可用。新增一列前先掃過既有 OQ 表與各 ui 檔「待確認事項」查重，同一個 uc／entity／Screen 已有紀錄就不重開，要推翻先前結論則在「情況」欄明講「推翻 OQ-xx」並說明差異。

## 執行單位：一輪一個任務

**跨模組引用一定要帶 `--spec`（見 OQ-10）**：`ui-check` 只對「目標檔案自己模組」的 `spec-<模組>.md` 自動載入；只要目標檔案有引用其他模組的 ID（鐵則 6 明文允許），單獨對它跑 `ui-check` 而不帶其他模組的 spec，會把這些合法引用誤判成 `REF-07` 未定義。**只要不是跑 `ui-check`（不帶參數，掃全部）**，一律加上 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"` 帶入全部模組的 spec，例如：`./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`。用 `tools error-count`／`tools accept-check` 檢查則不用擔心，`ui-authoring-tools.py` 已經內建這個 `--spec` 邏輯。

- 一輪只做任務清單裡的一個 `actionable` 任務：優先順序 `doing`（上一輪沒做完） → `D-xx` 的 `todo` → 依表格順序第一個依賴已全 `done` 的 `todo`。
- 開工前跑一次 `./scripts/ui-check .dev/F0x-*/ui-*.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`（鎖定本輪要動的模組；若該模組 ui 檔還不存在，這步會報「找不到任何 ui 檔」，屬預期，直接繼續）記錄起點 error／warn 數。
- 做完（或做不完）都要跑 `./scripts/ui-check` 收尾確認（單一檔案或子集合記得照上面加 `--spec`）：
  - 骨架類任務（建立檔頭與畫面標題）：`DS-01` 不報 error 即可（八段內容還沒填，先別跑全段檢查）。
  - 單一畫面任務：該畫面所在檔案跑 `ui-check` 對它的 `DS-01`～`DS-05`、`REF-07` 不得有新增 error；本畫面若標「已定案」，其八段格式必須完整無漏；若標「討論中」，`ui-check` 仍要 0 error（討論中不代表格式可以不齊全，只代表內容還有 `⚠️`）。
  - 收尾類任務（每個模組最後一項）：整份 `ui-<模組>.md` 0 error。
- 本輪若新增或修改了 OQ 表列、或 ui 檔「待確認事項」裡標【引用原文】／【矛盾】／【推論】／【覆蓋】的『』引用，收尾前另外跑一次 `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`；非 0 視為本輪未完成，逐一修正對應列（引用文字改到與 spec 逐字相符，或承認引用錯誤重寫該列）到腳本通過為止，不可以放著沒驗證過的引用收尾。
- 做不完：commit 已完成的部分，任務標 `doing`，PDCA Act 寫清楚剩餘工作，下一輪從這裡接續。
- 每輪結束：更新任務清單狀態、覆寫 `ui-authoring-state.md`（自動模式必做；手動模式若沒維護這個檔案可以省略，但建議一併寫，方便中斷後接續）、PDCA 追加一則、commit（見下方「commit 規範」）、一句話回報本輪做了什麼、下一個任務是什麼。

## commit 規範

| 改動 | 訊息 |
|---|---|
| `ui-<模組>.md` | `[spec/design](ui-<模組名>) <摘要>` |
| 任務清單、PDCA、OQ 檔 | `[docs](loops) <摘要>` |

- 兩種改動分開 commit；摘要中文祈使句、50 字內、句尾不加標點；只 `git add` 本輪修改的檔案；不 `git push`。
- `<模組名>` 用 `ui-<模組名>.md` 的 `<模組名>` 部分（`kanban-basic`、`user-membership`、`kanban-widgets`、`board-clock`、`workload`、`feature-cr-board`、`canvas-layout`），不要寫 `F01`（`git-convension.md` scope 只允許小寫英文與連字號）。跨模組收尾的 commit 省略 scope。

## 禁止事項

- 在 ui 檔定義 spec 沒有的實體、屬性、角色、Use Case、Screen 以外的 ID。
- 修改 `spec-*.md`、`.dev/conventions/**`、`scripts/**`、`CLAUDE.md`、`design-*.md`。
- 為了讓 `ui-check` 通過而刪減段落、亂填「不適用」掩蓋真正缺的資訊、或把「討論中」硬標成「已定案」。
- OQ／待確認事項使用「應該」「建議這樣比較好」「我認為」「比較好」等說服性字眼取代逐字引用。
- 向使用者提問、等待人工回覆。
- 一輪做多個任務、提前做下一個任務、自行發明或改寫任務清單裡既有任務的描述／驗收條件／依賴（可以追加新的 `D-xx`）。
- 在背景執行指令後就結束回合。

## 收尾條件

任務清單最後一項（跨模組收尾階段的最終確認任務）完成時：`./scripts/ui-check`（全部 ui 檔）0 error、七份 ui 檔都存在、任務清單除該項外全部 `done`（無 `todo`／`doing`／`blocked`）→ 建立 `<loop>/runtime/DONE`（不進版控），並在回報裡明講「UI 撰寫 loop 完成」。

---

## 操作手冊（給人工）

### 手動模式：`/loop`（人在旁邊看著跑，沒有外部驗證）

1. 確認工作區乾淨（`git status`）。
2. 第一次啟動前記錄起點：`mkdir -p .dev/loops/ui-authoring-loop/runtime && git rev-parse HEAD > .dev/loops/ui-authoring-loop/runtime/baseline`。
3. 用 Claude Code 內建的 `/loop` 指令驅動，不帶固定間隔（自我調節）：

   ```
   /loop 讀 .dev/loops/ui-authoring-loop/prompts/ui-authoring-kickoff-prompt.md 並依其規則執行下一輪任務
   ```

4. 每輪跑完看 `.dev/loops/ui-authoring-loop/.state/ui-authoring-pdca.md` 尾巴確認進度；`ui-authoring-tasks.md` 看任務狀態；`.dev/loops/ui-authoring-loop/runtime/DONE` 出現代表七份 `ui-*.md` 全部完成。
5. loop 因為某任務 `blocked` 停下：看 `ui-authoring-open-questions.md` 最後幾列與該任務描述，人工決定後把任務狀態改回 `todo`，重新 `/loop` 即可接續（不需要重跑前面已 `done` 的任務）。
6. 遇到 `G1` 關卡：手動模式下由執行輪自己完成自我審查並直接把 `G1` 標 `done`（任務描述裡寫的方式），不需要 `runtime/gates/G1.approved`；這個核准檔只有走自動模式（下方）時才需要。

### 自動模式：`run-ui-authoring-loop.sh`（無人值守，跑完整六模組，外部驗證）

1. 確認工作區乾淨；確認裝了 `claude`、`git`、`python3`、`timeout`、`jq`。
2. 直接執行：

   ```bash
   .dev/loops/ui-authoring-loop/run-ui-authoring-loop.sh
   ```

   會自動切到（或建立）`loop/ui-authoring` 分支、記錄 `runtime/baseline`、記錄起點 error 數，然後開始無限迴圈，每輪都是全新的 `claude -p` process。可用環境變數調整行為（`MAX_ITERATIONS`、`MODEL`、`EFFORT`、`REVIEW_EVERY`、`AUTO_APPROVE_GATES`、`GATE_MAX_REVIEWS` 等，見腳本開頭）。
3. 觀察：主控台與 `runtime/logs/iter-*.jsonl` 開頭會印每輪的 `mode`（`exec`／`review`）、實際用的 `model`／`effort`、用途（執行輪或審查輪、做什麼）與本輪任務摘要；`runtime/logs/iter-*.summary.txt` 存這四行方便事後查；`runtime/last-verify.md` 是上一輪外部驗證結果；`<state>/ui-authoring-review.md` 是審查紀錄。
4. 停止條件：`runtime/DONE` 出現（完成）、`MAX_ITERATIONS` 用完、沒有可執行任務、同一任務連續驗證失敗 `MAX_TASK_FAILS` 次、連續 `MAX_NO_PROGRESS` 輪沒有前進——腳本會印出停止原因，人工介入後可重新執行同一指令接續（`runtime/` 保留跨輪狀態，已完成的任務不會重做）。
5. 遇到 `G*` 關卡：腳本會先跑審查輪（讀 `ui-authoring-review-prompt.md`），若審查後沒有新增待修的 `D-xx`，下一輪自動核准（建立 `runtime/gates/<G>.approved`）；`AUTO_APPROVE_GATES=0` 則改成印出核准指令、停下來等人工確認。
6. 兩種模式可以交替使用（例如先手動跑幾輪熟悉狀況，再切自動模式跑完剩下的），任務清單與 PDCA 是共用的狀態來源，不會互相衝突；但自動模式產生的 commit 一律用 `--dangerously-skip-permissions`，第一次用建議先在測試分支確認腳本行為符合預期。
