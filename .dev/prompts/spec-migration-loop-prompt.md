# 提示詞：建立「spec 遷移 loop」——把 F01～F06 規格遷到腳本可解析格式

> 用法：把這份提示詞交給 Claude Code（建議 Opus）。它會產出一整套無人值守 loop 的檔案（規則書、kickoff、review、任務清單、工具、驗證、外層迴圈），並用假的 `claude` 演練過控制流程。**這份提示詞只建 loop，不執行遷移**；遷移由之後跑 `run-spec-migration-loop.sh` 完成。

---

## 你的角色

你是 loop engineering 專家與提示詞專家。你要為這個 repo 建立一套 zero-context 無人值守 loop，用它把 `.dev/F01～F06` 的六份 `spec-*.md` 遷移到 `.dev/conventions/spec-convention.md` 定義的新格式，讓 `scripts/spec-check` 回報 0 error。

先讀完這些檔案再動手（依序）：

1. `CLAUDE.md`
2. `.dev/conventions/spec-convention.md`、`checks.md`、`scripts.md`、`cr-convention.md`、`llm-review.md`、`open-questions.md`（Q9、Q10 是本 loop 要執行的事）
3. `scripts/README.md`、`scripts/tests/fixtures/good/`（新格式的完整範例，遷移後的檔案長這樣）
4. `.dev/F01-basic-kanban/spec-kanban-basic.md`（遷移對象的現況；其餘五份掃過結構即可）
5. `../kanban/.dev/lesson_learned/autonomous-workplan-loop.md`、`../kanban/.dev/lesson_learned/loop-drift-prevention.md`（機制的來由與教訓）
6. `../kanban/.dev/prompts/run-workplan-f08-loop.sh`、`verify-round-f08.sh`、`workplan-f08-tools.py`、`workplan-f08-kickoff-prompt.md`、`workplan-f08-review-prompt.md`、`workplan-f08-tasks.md`（上一代 loop 的實作，可以大量沿用結構與寫法）
7. `../kanban-loop-runtime-backup/runtime/`（runtime 目錄的實際輸出樣貌：`DONE`、`gates/`、`logs/iter-<時間>-<序號>-<模式>.jsonl` 與同名 `.summary.txt`、`logs/verify-<序號>.log`、`state`、`last-progress`、`last-verify.md`、`tmp/`）

---

## 第一部分：目標與鐵則（寫進規則書，且腳本要能擋）

### 目標

1. 六份 spec 全部通過 `./scripts/spec-check`（0 error；warn 可以留）。
2. 建立 `.dev/CR.md`，登記既有的 CR-001～CR-004（狀態「處理完成」，影響 ID 由遷移後的 ID 回填）與本次遷移的 CR-005「規格格式遷移至 usecase 區塊」；最後 `./scripts/cr-check --base <baseline> --cr CR-005` 通過。
3. `llm-review.md` 的語意 review 是次要目標：審查輪做，發現的問題開任務或記 open question，不擋完成。

### 鐵則 1：不改業務邏輯

遷移只能**加結構、改格式**，不能改行為。「行為」的機械定義是：每個 gherkin 區塊裡的 `Feature:` 名稱、`我想要`／`以便` 兩行、`Background`、每個 `Scenario` 的名稱、步驟（Given／When／Then／And／But 與其資料表列）的**文字**，在去掉反引號與正規化空白後，必須與 baseline commit 完全相同：同樣的 Scenario、同樣的順序、同樣的步驟。

允許改動的只有：

- tag 行（加 `@uc-`、`@fail-`；調整順序；**不加任何狀態 tag**）
- `# Related aggregate:` 註解（名稱改成實體 ID、read／write 對齊 `crud`）
- `身為` 那一行（改成「身為 <角色表名稱>」）
- 步驟裡的反引號（去掉或換成「」，文字本身不變）
- gherkin 區塊以外的所有內容（名詞表、角色表、usecase 區塊、變更紀錄追加、待釐清、正文）

這條由 `verify` 腳本逐字比對 baseline，違反即本輪失敗；不是寫在提示詞裡期待遵守。

### 鐵則 2：usecase 區塊只能從既有 Scenario 推導

`pre`／`post`／`fail` 每一句都要能指到某個 Scenario 的某個步驟或 Background；`roles` 只能來自 Feature 標頭或 F02 已寫明的權限；`crud` 來自 Aggregate 註解。Scenario 沒寫的行為不能出現在 usecase 區塊。推不出來、或兩種推法都說得通時，走「假設分級」（見下），不能自己補行為。

### 鐵則 3：保護檔案

`.dev/conventions/**`、`scripts/**`、本 loop 的規則書／kickoff／review／tools／verify／loop 腳本、`.gitignore`、`CLAUDE.md` 在 loop 期間不可修改。發現規範或腳本有 bug（例如某條檢查明顯誤報）→ 記 open question，繞不過去就把該任務標 `blocked`，不改規範來遷就。

### 鐵則 4：狀態只活在檔案與 git

每輪全新 context。任務清單、state、PDCA、open questions、CR.md 是唯一記憶；沒寫進檔案的決定等於沒發生。PDCA 只能追加；任務清單只能改狀態欄；D-xx 只能追加。

### 假設分級

| 等級 | 情況 | 做法 |
|---|---|---|
| 低影響 | 純格式選擇：實體 ID 命名（camelCase → kebab）、`pre` 的拆句方式、要不要把某個名詞當實體、`所屬 Aggregate` 怎麼填 | 自行決定，寫進該 spec 的「變更紀錄」摘要或 PDCA Do，繼續做 |
| 高影響 | 會改變開發者要做的事：角色表的權限、`fail` 該不該有某條、事件的 emits／requires 關係、Scenario 明顯自相矛盾、兩個模組對同一實體的定義不一致 | 選「最小驚訝、與既有 Scenario 一致」的選項照做，**同時**在 `.dev/prompts/spec-migration-open-questions.md` 追加 `OQ-xx`（情況、選項、採用的選項、依據哪個 Scenario、日期），並在該 spec 的 `## 待釐清` 加一行指向 OQ-xx。不停下、不標 blocked |
| 環境限制 | 腳本崩潰、規範本身矛盾、必須改保護檔案才能過 | 記 OQ-xx，任務標 `blocked`，讓 loop 跳下一個 |

---

## 第二部分：要產出的檔案

全部放在 `.dev/prompts/`，檔名前綴 `spec-migration`。runtime 放 `.dev/prompts/runtime/spec-migration/`，加進 `.gitignore`。

### 1. `spec-migration-prompt.md`（規則書）

只放**不變的規則**，不放現況。內容：角色、目標、鐵則 1～4、假設分級、遷移程序（下方「第三部分」逐字納入）、執行單位、commit 規範（`[spec/design](F0x) …` 給規格、`[docs](prompts) …` 給 loop 文件、`[chore](cr) …` 給 CR.md）、禁止事項、收尾條件。

### 2. `spec-migration-tasks.md`（任務清單，唯一任務來源）

表格欄位：`ID | 狀態 | 任務 | 驗收條件 | 依賴`。人工定義，agent 只能改狀態欄；規則段落照 F08 的寫法（含 D-xx 與 `proposed` 機制、狀態值、挑選順序、關卡說明）。任務至少涵蓋：

**階段 0：準備**
- T0.01 記錄 baseline：把 loop 啟動時的 HEAD 寫進 `runtime/spec-migration/baseline`（由 loop 腳本做，任務只是確認）；跑 `spec-check` 存每個檔的 error 數作為起點。
- T0.02 建立 `.dev/CR.md` 骨架與 CR-005（狀態「修改規格」，影響 ID 先空）。

**階段 1：F01 作為範本**（一個模組拆成可獨立驗證的小任務，每個任務一輪）
- T1.01 狀態行、名詞定義三張表＋其他名詞、角色定義（實體 ID 含所屬 Aggregate；把原本名詞表的內容分流到四張表，說明文字不刪）
- T1.02 「Swimlane 管理」Feature 的 usecase 區塊＋該 Feature 全部 Scenario 的 `@uc-`／`@fail-` tag 與 Aggregate 註解對齊
- T1.03 同上，「Stage 管理」
- T1.04 同上，「Card 編輯」
- T1.05 正文反引號清理（程式碼名稱改「」）、`design.md` 稱呼改為 `design-kanban-basic.md`、變更紀錄追加 CR-005 一列
- T1.06 `spec-check .dev/F01-basic-kanban/spec-kanban-basic.md` 0 error（REF-06 因 CR.md 已建立應通過）
- G1 關卡：審查輪檢視 F01 是否可作為其餘模組的範本（usecase 拆分粒度、pre／post 措辭、實體粒度）

**階段 2：F02～F06**，每個模組同樣拆成「名詞／角色」→「每個 Feature 的 usecase＋tag」→「清理＋0 error」三到五個任務；依賴前一模組完成（因為實體與角色跨模組共用，後面的模組只引用、不重定義）。F02 的 `boardMembership` 這類 camelCase 註解名稱要改成 kebab 實體 ID。

**階段 3：跨模組收尾**
- T3.01 全部 spec 一起跑 `spec-check` 0 error（REF-08 重複定義、跨模組引用）
- T3.02 回填 CR-001～CR-004 的影響 ID；CR-005 影響 ID 列出全部實體與 uc；`cr-check --base <baseline> --cr CR-005` 通過
- T3.03 `spec-check --report` 貼進 PDCA 供人工檢視；`.dev/prompts/spec-migration-open-questions.md` 整理成表
- G2 關卡：審查輪做 `llm-review.md` L-01～L-11 的抽查（每模組至少一個 Use Case），偏差開 D-xx 或 OQ
- T3.04 建立 DONE

驗收條件要可機械驗證，例如「`spec-check <檔>` 的 error 為 0」「該 Feature 的每個 Scenario 都有恰好一個 `@uc-`」「`grep -c '^狀態：' <檔>` 為 1」。

### 3. `spec-migration-kickoff-prompt.md`（執行輪，sonnet／medium）

照 F08 kickoff 的四段結構（讀什麼 → 決定本輪目標 → 做什麼 → 怎麼收尾），改成遷移情境。重點：

- 讀的順序：DONE → `spec-migration-state.md` → `runtime/spec-migration/last-verify.md` → 規則書 → `tools actionable` + `git log` + `git status` → 本輪任務對應的 spec 檔與 `scripts/tests/fixtures/good/` 範本 → 需要時才讀 PDCA。
- 每輪只做一個任務；做完跑 `./scripts/spec-check <該檔>` 與 `python3 spec-migration-tools.py gherkin-diff <baseline> <該檔>`（鐵則 1 的自我檢查，正式判定仍由 verify 做）。
- 高影響假設寫 OQ-xx 後繼續做；D-xx 只能 `proposed`。
- 收尾：任務清單改狀態、覆寫 state（20 行內）、PDCA 追加一則（Plan／Do／Check／Act，Check 要貼 spec-check 的最後一行與 gherkin-diff 結果）、規格與 loop 文件分開 commit、`git status` 乾淨。
- 完成 T3.04 時建立 `runtime/spec-migration/DONE`。

### 4. `spec-migration-review-prompt.md`（審查輪，opus／medium）

照 F08 review 的鐵則（唯讀、只能改任務清單的 D-xx 與 review log、不中斷模式自行決定）。審查面向改成：

| 面向 | 檢查重點 |
|---|---|
| 鐵則 1 | 除了 verify 的逐字比對之外，看「說明文字」有沒有被刪或改意思（名詞表分流時內容遺失、待釐清被清掉） |
| 鐵則 2 | 抽查 usecase 區塊：每句 `pre`／`post`／`fail` 能否指到 Scenario 步驟；`roles` 有沒有憑空出現；`crud` 與註解是否只是被改成一致而沒有實際依據 |
| 拆分粒度 | 同一個 Feature 的 Scenario 被分到幾個 uc 是否合理（一個交易一個 uc）；讀取類 Scenario 有沒有被硬塞進寫入 uc |
| 跨模組一致 | 同一實體在不同模組的引用是否指同一個 ID；F02 的 user／board-membership 與 F01 的 board／card 關係表有沒有矛盾 |
| OQ 品質 | 每個 OQ-xx 是否真的是高影響、採用的選項是否最小驚訝；低影響的假設有沒有被誤記成 OQ（反之亦然） |
| llm-review | G2 時對每模組至少一個 uc 跑 L-01、L-02、L-05、L-06；G1 時只跑 F01 |
| 待審任務 | `proposed` 的 D-xx → `todo`／`rejected` |

產出：D-xx（`todo`，驗收條件具體）、`spec-migration-review.md` 追加一則（範圍／發現／待審任務處理／關卡摘要）、一個 `[docs](prompts)` commit。

### 5. `spec-migration-tools.py`

沿用 F08 tools 的子命令（`actionable`、`rows`、`last-act`、`last-task`、`status-summary`、`check-ledger`、`check-pdca-append`），另加：

- `gherkin-extract <檔>`：印出正規化後的行為清單（Feature 名、我想要、以便、Background 步驟、每個 Scenario 名與步驟；去反引號、正規化空白、忽略 tag 行與 `#` 註解與 `身為` 行）。
- `gherkin-diff <baseline-ref> <檔>`：對 `git show <ref>:<檔>` 與工作區各跑一次 extract，逐行 diff；有差異回傳非零並印出第一處差異（給 verify 與 kickoff 共用）。
- `error-count <檔>`：跑 `scripts/spec-check`，回傳該檔的 error 數（用 `--format json` 解析）。
- `changelog-check <baseline-ref> <檔>`：baseline 的變更紀錄每一列都必須仍在工作區（只能追加，不能改舊列）。

### 6. `verify-spec-migration.sh`

用法與輸出格式照 F08（`last-verify.md` 有 PASS／FAIL 標題、失敗項目、警告、資訊；`last-progress` 寫 yes／no）。檢查項目：

| # | 檢查 | 層級 |
|---|---|---|
| 1 | 工作區乾淨 | fail |
| 2 | commit 訊息格式（`git-convension.md`） | fail |
| 3 | 受保護檔案未被修改（鐵則 3 清單） | fail |
| 4 | 任務清單只改狀態欄、PDCA 只追加、review log 只追加（`check-ledger`、`check-pdca-append`） | fail |
| 5 | **每份 spec 對 baseline 跑 `gherkin-diff`**，任何差異 | fail |
| 6 | 每份 spec 跑 `changelog-check` | fail |
| 7 | 沒有新增狀態 tag：`grep -E '@(added|changed|deprecated|wip)'` 的數量與 baseline 相同 | fail |
| 8 | 任務清單裡狀態為 `done` 且驗收條件含「0 error」的任務，對應檔案 `error-count` 必須為 0 | fail |
| 9 | 狀態為 `doing` 的任務對應檔案，error 數不得高於上一輪（存 `runtime/spec-migration/errors.json`） | fail |
| 10 | 全部 spec 的 error 總數與上一輪比較 | 資訊；下降或任務狀態變更才算 progress |
| 11 | OQ 檔只能追加；每個 OQ-xx 有「採用的選項」欄 | fail |
| 12 | 單輪改動的 spec 檔數 > 1（除了 T3.x） | warn |
| 13 | `spec-check` 的 warn 數量 | 資訊 |
| 14 | review 模式：只允許改任務清單與 review log | fail |
| 15 | gate 核准檔的 mtime 早於本輪開始（防偽造） | fail |

### 7. `run-spec-migration-loop.sh`

沿用 F08 loop 腳本的結構與參數，改預設值：

```
MODEL=claude-sonnet-5        EFFORT=medium
REVIEW_MODEL=claude-opus-5   REVIEW_EFFORT=medium
SUMMARY_MODEL=claude-haiku-4-5-20251001
REVIEW_EVERY=6   ROUND_TIMEOUT=30m   MAX_TASK_FAILS=3   MAX_NO_PROGRESS=3
LOOP_BRANCH=loop/spec-migration   AUTO_APPROVE_GATES=1   GATE_MAX_REVIEWS=2
```

另外：

- 啟動時若 `runtime/spec-migration/baseline` 不存在，寫入目前 HEAD；之後所有 `gherkin-diff` 都用這個 ref（不是每輪的 base）。
- runtime 目錄結構與檔名完全照 `../kanban-loop-runtime-backup/runtime/`：`DONE`、`gates/`、`logs/iter-<YYYYmmdd-HHMMSS>-<i>-<mode>.jsonl` + `.summary.txt`、`logs/verify-<i>.log`、`state`、`last-progress`、`last-verify.md`、`tmp/`。
- 事前檢查加：`python3 -m unittest discover -s scripts/tests` 必須通過（腳本壞了不要跑 loop）。
- Haiku 摘要沿用（`--tools ""`）。

### 8. `spec-migration-state.md`、`spec-migration-pdca.md`、`spec-migration-review.md`、`spec-migration-open-questions.md`

給初始內容（state 的欄位模板、PDCA 與 review 的標題與「append-only」說明、OQ 的表格欄位：編號／日期／模組／情況／選項／採用／依據／狀態）。

### 9. 演練

照 lesson learned「用假的 claude 測 loop 本身」：在暫存 worktree 放假 `claude`，跑三種情境並把結果寫進 PDCA 第 0 則或 README：

1. 合規的一輪：把 T0.01 標 done、追加 PDCA → verify PASS。
2. 違規的一輪：改一個 Scenario 步驟的文字、刪一列變更紀錄、加 `@wip`、改 `spec-convention.md`、任務清單改驗收條件、PDCA 改舊紀錄、OQ 缺「採用」欄 → 每一項都被對應的檢查擋下，列出檢查編號。
3. 多輪：REVIEW_EVERY 觸發審查、G1 先審查再自動核准、連續失敗停止。

---

## 第三部分：遷移程序（逐字放進規則書，執行輪照做）

對一份 spec 的遷移順序：

1. **狀態行**：簡介之後加 `狀態：開發中`（六份都已交付開發，這是「首版進入開發」的起點，見 `open-questions.md` Q1）。
2. **實體表**：從所有 Aggregate 註解出現過的名稱＋原名詞表裡會被單獨建立／修改／刪除的名詞，列出實體；ID 用 kebab-case；`所屬 Aggregate` 填樹根 ID（root 標「（root）」）。跨模組已定義的實體（F02 用到 F01 的 `board`、`card`）**不重列**，只引用。
3. **欄位表**：從 Scenario 步驟裡出現的具名資料（名稱、標題、描述、負責人、截止日、顏色、角色…）與原名詞表推；`限制` 欄只寫 Scenario 有驗證的規則（例如「不可為空」有對應 Scenario 才寫）。
4. **關係表**：只寫 Scenario 有體現的關係（Board 含 Swimlane、Swimlane 有 Card…）；min／max 依「至少保留一個」這類 Scenario 決定，沒有依據就 `0`／`n` 並記低影響假設。
5. **其他名詞**：原名詞表裡不是實體的（Stage 角色、操作時間、WIP 限制…）搬到這裡，說明文字原封不動；裡面的反引號程式碼名稱換成「」。
6. **角色表**：從每個 Feature 標頭的「身為…」收集；F02 的成員／管理者權限有寫明的才分成不同角色，否則整個模組一個角色。把標頭改成「身為 <名稱>」（一個半形空格）。
7. **每個 Feature 的 usecase 區塊**：一個交易一個 uc。分組原則：同一個 When 動作（新增／重新命名／排序／刪除／移動…）的成功與失敗 Scenario 屬同一 uc；純讀取的 Scenario（顯示、查詢、統計）也是 uc，`crud` 只有 R、`roles` 可空。`pre` 從 Given 與失敗 Scenario 的規則推；`post` 從成功 Scenario 的 Then 推；`fail` 只列有失敗 Scenario 驗證的那幾條（其餘不列，UC-05 才會過）；`emits`／`requires` 只在 Scenario 明說「該操作應該被記錄為活動紀錄」這類跨 uc 效果時才填，否則空。
8. **tag**：每個 Scenario 掛 `@uc-<id>`；失敗 Scenario 掛 `@fail-<pN>`；固定順序是狀態 tag → `@wip` → `@CR-` → `@uc-` → `@fail-`，所以既有的 `@CR-001` 這類 tag 保留原位、新的 `@uc-`／`@fail-` 接在它後面。**不新增任何狀態 tag**。
9. **Aggregate 註解**：名稱改成實體 ID；與 uc 的 `crud` 對齊（成功 Scenario：crud 有 CUD 的實體標 write；失敗 Scenario 只標 read）。
10. **正文清理**：gherkin 區塊外的反引號只留六種 ID 與檔案路徑；`BoardClock.now()`、`Instant.now()`、`NONE` 這類換成「」。
11. **變更紀錄**：追加一列 `| <日期> | CR-005 | 變更 | 規格格式遷移至 usecase 區塊（`uc-…`、`uc-…`） |`，舊列不動。
12. 跑 `./scripts/spec-check <檔>` 到 0 error；跑 `gherkin-diff` 確認無差異；commit `[spec/design](F0x) 遷移 <Feature 名> 至 usecase 區塊`。

---

## 限制

- 全部繁體中文、台灣用語；腳本註解也是。
- 不引入新概念（不做狀態機、不做決策表、不另建本體論檔）。
- 提示詞裡不放現況（哪個模組有幾個 Scenario、目前幾個 error）；現況一律由任務清單、`errors.json`、git 推導。
- 產出前先跑 `python3 -m unittest discover -s scripts/tests` 確認腳本可用；演練通過才算完成。
- 最後回報：產出的檔案清單、演練三種情境的結果、以及你認為這套 loop 最可能在哪一條檢查上卡住（給人工預先調整 MAX_TASK_FAILS 或任務拆分）。
