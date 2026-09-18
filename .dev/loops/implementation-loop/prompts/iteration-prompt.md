# implementation-loop 規則書

> 目的：依已定稿的 `.dev/F0x-*/spec-*.md`／`ui-*.md`／`design-*.md`，把 `kanban-core`／`kanban-spring`／`kanban-frontend` 三個尚未存在的程式碼庫實作出來。
> 這是「開發」loop，跟 `spec-migration-loop`／`ui-authoring-loop`（文件 loop）不同：本 loop 的產出對象是程式碼，且以**平行、worktree 隔離的開發／審查 sub agent 配對**執行，不是單一 agent 逐輪執行。
> 適用位置：`.dev/loops/implementation-loop/`；共同建置規則見 [`無人值守文件-loop-建置與限制規則.md`](../../lesson-learned/無人值守文件-loop-建置與限制規則.md)（本檔沿用其第 1～3、6～10 節，第 4～5 節改寫為第 4～5 節「平行執行模型」）。

## 0. 三個階段

| 階段 | 提示詞 | 產出 | 執行方式 |
|---|---|---|---|
| 安排（Planning） | `planning-prompt.md` | `.state/tasks.md` 的任務列（依主體依賴排序）＋各任務初始 `status` | 單一 agent、單輪或少數幾輪，比照既有兩個文件 loop 的執行輪模式 |
| 開發（Dev） | `dev-prompt.md` | 該任務對應的程式碼＋測試，commit 在專屬 worktree 分支 | 每個 `todo`→`doing` 任務各自一個 worktree，最多 5 個並行 |
| 審查（Review） | `review-prompt.md` | 審查紀錄／`D-xx` 修正任務／核准或退回 | 與對應 Dev 同一個 worktree，讀取＋可新增審查紀錄，不可改動產出程式碼本體 |

安排階段先跑完、產生任務清單，才進入開發／審查的平行階段；`.state/tasks/<task-id>/fixes.md` 裡新出現的 `D-xx`（審查退回的修正項）由**同一個任務的 Dev 輪**處理，不算新任務、不佔用新的並行名額。

## 1. 角色

- **Dev sub agent**：在自己的 git worktree 裡，針對**一個**任務（一個主體／aggregate，或一個前端畫面群組）完成實作＋測試，commit，然後把任務狀態改成 `review-pending` 並寫交接摘要。**不能把任務標成 `done`**——那是 Review 的權限，Dev 自己宣稱「完成」不算數。
- **Review sub agent**：在同一個 worktree 裡（同分支、同 commit），**必須自己跑一次建置／測試**（不採信 Dev 的自我回報），核對程式碼是否忠實對應 spec 的 Scenario／`crud`／`pre`／`post`，核對 `kanban-core` 是否真的不依賴 Spring。有問題 → 在該任務的 `fixes.md` 追加 `D-xx` 修正項並把任務狀態（`status` 檔）改回 `doing`（等下一輪 Dev 處理），**自己不動程式碼**；沒問題 → 把任務狀態改成 `done` 並在該任務的 `review.md` 記一則核准紀錄，才觸發合併回整合分支。
- 兩個角色互相是對方的守門人：Dev 不能自我認證完成，Review 不能不驗證就放行，也不能越權直接修 Dev 的程式碼。任何一輪如果做完事沒有依規則轉移任務狀態、沒有留下規則要求的紀錄，視為本輪未完成，外部驗證會擋下。

## 2. 檔案地圖（誰可以改）

| 分類 | 路徑 | 誰可以改 |
|---|---|---|
| 不變規則 | `prompts/*.md` | 人工 |
| 任務清單（靜態） | `.state/tasks.md`（ID／產出範圍／依賴／備註，**沒有狀態欄**） | 人工或安排階段；Dev／Review **不可改** |
| 任務目錄 | `.state/tasks/<task-id>/`：`status`／`fixes.md`／`decision-log.md`／`review.md`／`open-questions.md`／`state.md`／`rounds.log`（收尾紀錄）／`driver-note.md`（驅動腳本標 blocked 的原因） | **只有這個任務自己的 Dev／Review，而且只能透過 `scripts/loopctl`**（依各自權限，見第 1 節）；別的任務只讀。安排階段自己的紀錄寫 `.state/tasks/_planning/` |
| ADR | `.state/adr/ADR-<task-id>-<兩位數>-<slug>.md`，一則一檔 | Dev 只能**新增**檔；既有 ADR 只能由人工改狀態欄 |
| 舊紀錄 | `.state/archive/**`（2026-09-18 前的共用 `decision-log.md`／`review.md`／`open-questions.md`／`state.md`／`adr.md`，`OQ-IMPL-01～17`、`ADR-001` 在這裡） | 只讀（人工可在既有 OQ 底下補解除說明） |
| 輔助腳本 | `scripts/verify.sh` 等 | 人工；loop 只能執行 |
| 執行期檔案 | `runtime/`（不進版控） | 腳本／loop |
| 產出對象（後端） | `kanban-core/**`、`kanban-spring/**`（repo 根目錄新建） | Dev（依任務） |
| 產出對象（前端） | `kanban-frontend/**`（repo 根目錄新建） | Dev（依任務） |
| 上游依據 | `.dev/F0x-*/spec-*.md`／`ui-*.md`／`design-*.md`、`.dev/conventions/**`、`CLAUDE.md`、既有 `scripts/**` | **不可修改**：規格沒寫到的行為不能腦補，缺什麼記 OQ；`scripts/**` 是規格檢查腳本，跟本 loop 的程式碼實作無關，禁止觸碰 |
| 前端視覺依據 | 設計稿（見 `planning-prompt.md` 附的畫面清單，claude.ai Design 類型 Artifact） | 只讀引用；Dev 無法直接開啟該 Artifact 時，以 `.state/tasks.md` 裡該任務列附的畫面結構摘要為準，不得自行發明版面 |

### 2.1 為什麼 `.state/` 是「一個任務一個目錄」

Dev／Review 跟驅動腳本、跟下一輪的自己，都是透過 `.state/` 溝通（zero-context）。但每條管線在自己的 worktree／分支上寫 `.state/`，核准後才 `git merge` 回整合分支——**只要兩條並行管線改到同一個檔，後合併的那條就會衝突**。2026-09-18 之前用共用檔（所有任務往同一份 `decision-log.md`／`review.md`／`open-questions.md` 檔尾追加、`state.md` 每輪覆寫、`tasks.md` 改狀態欄），T-11 與 T-02 並行時五個檔全部衝突、兩邊還各自開了 `OQ-IMPL-12`／`13`（撞號），主 repo 卡在合併到一半。所以規則是：

- **一個 worktree 只寫自己的 `.state/tasks/<task-id>/`**（外加在 `.state/adr/` 新增檔案）。不同分支永遠不會改到同一個檔，合併不可能在 `.state/` 衝突。
- **ID 以任務為命名空間**：`OQ-<task-id>-<兩位數>`、`ADR-<task-id>-<兩位數>`、`D-xx` 在該任務 `fixes.md` 內遞增。不可以用跨任務的全域流水號——並行的 worktree 互相看不到對方剛編的號。
- **`.state/tasks/<task-id>/` 只能用 `scripts/loopctl` 寫**（`oq add`／`fix add`／`fix done`／`log`／`finish`，用法見 dev／review 提示詞）：格式、編號、commit 都由它負責，agent 不直接編輯。每一輪一定要以 `loopctl finish` 收尾——驅動腳本不讀 agent 的最後回覆，只看收尾紀錄（`rounds.log`）；沒有收尾（以提問結尾、逾時、被中斷、沒 commit 都一樣）＝這一輪失敗，連續兩輪失敗就標 `blocked`。**想問人類問題的唯一方式是 `loopctl oq add`**。
- **狀態是 `status` 單行檔**（`todo`／`doing`／`review-pending`／`done`／`blocked`，不存在＝`todo`），驅動腳本只認這個檔。
- 要一次看全部：`scripts/collect.sh status|oq|review|decision|fixes|state`（只印不寫）。

`kanban-core`／`kanban-spring`／`kanban-frontend` 目前都不存在，第一個對應任務要建 Gradle／pnpm 專案骨架；骨架本身也是一個任務（見任務清單 `T-00-*`），其他任務都依賴它。

## 3. 任務排序原則（本體論依賴，對應使用者鐵則）

1. **用既有 ID 確保完整**：任務清單的每一列必須能指回 `.dev/F0x-*/spec-*.md` 的實體 ID（`board`／`card`／…）或 `ui-*.md` 的 Screen ID（`s-*`），不可以無中生有一個規格沒定義的主體。跨模組關聯要看 `r-` 關係表（例如 `card.assignees ref user` → `card` 依賴 `user`）與「其他名詞」裡點名的跨模組依據（例如 F03「asOf 取自 Board Clock」→ widgets 依賴 board-clock）。
2. **依主體（aggregate）依賴關係排序**：一個任務等於一個 Aggregate Root（含它的內部實體，例如 `board` 任務包含 `swimlane`／`stage`）或一個前端畫面群組；任務 A 依賴任務 B，若 A 的實體欄位 `ref` 到 B 的實體，或 A 是唯讀 projection 而其計算依據來自 B（F03／F05／F06 之於 F01／F02／F04）。前端畫面群組依賴它呼叫的 `uc-xxx` 所屬的後端任務。
3. **顆粒度：一個主體一次做完，不可切更細**：後端任務範圍 = 該 Aggregate 的 `kanban-core` domain model（entity／值物件／不變量）＋ `kanban-spring` 的 port 實作／application service／web endpoint／persistence（JPA repository、Flyway/DDL）＋ 該模組 Scenario 對應的 Cucumber step definitions，一次做完，不能拆成「這輪只做 domain，下輪做 web」。前端任務範圍 = 該畫面群組的元件、狀態管理、API 串接、`ui-*.md` 操作表列出的所有 UseCase 觸發與驗收條件，一次做完。反面例子：不可以把 `card` 拆成「新增 card domain」「新增 card API」兩個任務——那是同一個任務裡的子步驟，不是任務清單的列。
4. **CR 是既有 Aggregate 的追加任務，不是新主體**：CR-001～004 已經併入 F01／F02 現在的名詞定義（例如 `card.assignees`、`comment.author`、Board Clock 的 `occurredAt`），任務清單不用照 CR 演進順序重放，直接依「規格現在定案的樣子」實作；但 `board-clock`（F04）因為會改寫 `board`／`card` 事件的時間來源，實作上仍必須排在 `board`／`card` 任務之後（見任務清單 `BE-board-clock` 的依賴）。
5. **規格未遷移／未定義時標 blocked，不可腦補**：F03～F06 的 `spec-*.md` 目前「名詞定義」三張表尚未遷移（見 `spec-migration-loop`），没有 `entity`／欄位／`r-` 可引用；這幾個模組對應的任務在任務清單裡先標 `blocked`，附 OQ 指向 `spec-migration-loop` 進度，等該模組遷移完成（三張表補齊）才能改 `todo`。同理，F03／F05／F06 目前沒有 `ui-*.md`，對應前端任務也標 `blocked`。

## 4. 平行執行模型：worktree × 最多 5 個並行配對

- 驅動腳本 `run-loop.sh` 每次巡視任務清單，挑出所有依賴皆 `done` 且狀態為 `todo` 的任務，最多同時啟動 5 條「Dev→Review」管線（`MAX_PARALLEL=5`）。
- 每條管線一個獨立 git worktree：`git worktree add ../kanban2-impl-<task-id> -b impl/<task-id> <integration-branch>`，Dev／Review 都在這個 worktree 裡工作，彼此的檔案異動不會互相干擾，也不會互相看到對方任務的未合併改動（依賴任務必須先 `done` 並合併回整合分支，下游任務的 worktree 才會分支自帶依賴的程式碼）。
- 一條管線內部：Dev 輪 → Review 輪 → 若 Review 核准（狀態轉 `done`）→ 合併 `impl/<task-id>` 回整合分支 `loop/implementation`（合併需序列化：驅動腳本用檔案鎖；合併失敗會 `git merge --abort` 並把任務標 `blocked`，不會把主 repo 留在合併中）→ 移除該 worktree；若 Review 退回（留有未處理 `D-xx`）→ 回到 Dev 輪處理 `D-xx`，同一個 worktree 繼續用，循環直到核准或達 `MAX_TASK_ROUNDS`（達上限視為 `blocked`，記 OQ 等人工介入，worktree 保留供人工檢查）。
- 驅動腳本每次呼叫 Dev／Review 都會在提示詞最前面傳入 `任務 ID`／`回合：第 N 輪（上限 M）`／`角色`，必要時加 `驅動腳本附註`；回合數以傳入的為準。Dev 沒有用 `loopctl finish` 正常收尾 → 不送 Review，直接算一輪重跑 Dev（連續兩輪就 `blocked`）；Review 沒有正常收尾 → 只補跑一次 Review，再失敗就 `blocked`。
- 一個任務的 worktree／分支只服務這一個任務，不可以在裡面同時動另一個任務的範圍；任務完成合併後才刪除 worktree。
- 下游任務要等上游任務**合併回整合分支**（不是只到 `review-pending`）才能開始，因為它的程式碼要建立在上游已核准的實作上。

## 5. 自主決策分級（沿用文件 loop 的三級 + 新增「程式碼特有」判斷）

同 [`無人值守文件-loop-建置與限制規則.md`](../../lesson-learned/無人值守文件-loop-建置與限制規則.md) 第 6 節的四級（低風險／高風險／覆蓋來源／環境限制），額外補充程式碼場景：

- **技術實作細節（套件命名、DTO 欄位命名、SQL 型別選擇）不違反 spec 的地方**＝低風險，自行決定，該任務的 `decision-log.md` 記一句話（決策／理由兩行即可，不必開 ADR）。
- **spec 的 `pre`／`post`／Scenario 沒講清楚該怎麼實作（例如某個失敗情境該回什麼 HTTP 狀態碼）**＝高風險，寫進交接摘要「待確認事項」＋ OQ，該任務狀態可以是 `review-pending` 但 Review 要一併標記「有未決 OQ」，不能核准成不帶保留的 `done`。
- **spec 定案內容跟現有程式碼結構衝突（例如兩個模組對同一欄位的型別定義不一致）**＝覆蓋來源等級，禁止自行決定用哪個，記 OQ＋任務標 `blocked`，這是結構性問題要先走 CR，不是實作可以決定的事。
- **上游規格缺依據（F03～F06 未遷移、無 ui-*.md）**＝環境限制，該任務 `status` 直接寫 `blocked`，見第 3 節第 5 點。

## 6. commit 規範

| 改動對象 | 訊息前綴 |
|---|---|
| `kanban-core`／`kanban-spring` 程式碼 | `[dev](<scope>) <摘要>`，`<scope>` 用 aggregate 的小寫 ID（例如 `card`、`board-clock`） |
| `kanban-frontend` 程式碼 | `[dev](<scope>) <摘要>`，`<scope>` 用畫面群組的小寫代稱（例如 `board-detail`） |
| 測試 | `[test](<scope>) <摘要>` |
| loop 自己的任務清單／Decision Log／ADR／OQ／審查紀錄 | `[docs](loops) <摘要>` |

程式碼註解、commit 訊息、`.state/` 紀錄、agent 每一輪的最後回覆一律繁體中文 zh-TW（見全域 CLAUDE.md）。Gradle 一律用 `./gradlew <task> --no-daemon`：並行的 worktree 共用 `~/.gradle`，daemon 會互搶（`1 busy Daemon could not be reused`）。每個 worktree 只 commit 該任務範圍內的檔案；不 `git push`；合併回整合分支用 `git merge --no-ff`保留任務邊界，不 squash（方便 Review 紀錄對得回單一 commit 範圍）。

## 7. 禁止事項

- Dev 不可以把任務標成 `done`；Review 不可以不跑建置／測試就核准。
- Review 不可以直接修改 Dev 的程式碼（只能寫 `D-xx`／審查紀錄，改動權在 Dev）。
- 不可以無視第 3 節的依賴順序提前開工下游任務（依賴任務要「已合併」不是「已完成本輪」）。
- 不可以把 F03～F06、對應前端任務的 `blocked` 狀態自行改成 `todo`——除非該模組的 spec／ui 遷移已完成（三張表／畫面規格齊備）。
- 不可以修改 `.dev/conventions/**`、`scripts/**`（既有規格檢查腳本）、`CLAUDE.md`，也不可以修改 spec／ui／design 文件本體去配合程式碼實作上的方便。
- 一個 worktree 不可以同時處理多個任務；一輪不可以做多個任務。
- `.state/` 底下不可以寫自己任務目錄以外的地方（見第 2.1 節）；不可以改 `.state/tasks.md`。
- 不可以 `git switch`／`git checkout` 其他分支、`git merge`／`rebase`／`stash`／`push`——分支與合併是驅動腳本的事；不可以改主 repo 目錄底下的檔案。
- **不可以**在背景執行指令後就結束回合；每輪必須真正跑完建置／測試（或審查驗證），看到結果之後再收尾。
- **不可以使用 `Monitor` 工具、或任何「先背景啟動、之後再收通知」的模式**（例如 Bash 工具的 `run_in_background: true` 搭配「稍後查看」）。每一輪 Dev／Review 都是驅動腳本開的**一次性 `claude -p` 行程**：這一輪結束、行程就終止了，不會有「下一輪的自己」來接收任何背景工作的完成通知——`Monitor` 與背景任務通知是設計給持續互動的 session 用的，在 `-p` 模式下背景工作等於直接遺棄。驅動腳本已用 `--disallowedTools Monitor` 與 `CLAUDE_CODE_DISABLE_BACKGROUND_TASKS=1` 在工具層級關掉，不要再試。所有指令（含 `./gradlew build`、`pnpm test` 這類較慢的建置）都要在同一次 Bash 工具呼叫裡**同步、前景**執行到有結果為止；`ROUND_TIMEOUT=45m` 已經給了很寬的時間，不需要為了「怕等太久」而背景化。

## 8. 收尾條件（單一任務）

任務從 `todo` 到真正 `done`（已合併回整合分支）才算完成；驅動腳本的整體停止條件（`DONE`／`MAX_ITERATIONS`／無可執行任務／連續失敗）比照 [`無人值守文件-loop-建置與限制規則.md`](../../lesson-learned/無人值守文件-loop-建置與限制規則.md) 第 8 節，差異在「可執行任務」判定要同時檢查依賴是否**已合併**、以及並行名額（同時 `doing` 的任務數 < `MAX_PARALLEL`）是否還有空位。
