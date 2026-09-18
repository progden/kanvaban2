# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 專案現況

這個 repo 有兩部分：`.dev/` 的規格與設計文件（繁體中文），以及 `scripts/` 的規格檢查腳本（Python 3.10+，只依賴 PyYAML）。沒有應用程式程式碼、沒有 build。

指令：

```bash
python3 -m unittest discover -s scripts/tests   # 腳本的測試
./scripts/spec-check                             # 檢查 .dev/F*/spec-*.md（F01～F06 已遷移完成，目前 0 error）
./scripts/ui-check                               # 檢查 .dev/F*/ui-*.md
./scripts/cr-check --base origin/main            # CI 用，比對 .dev/CR.md 影響 ID 與 PR diff
./scripts/spec-check --report                    # 印 CRUD／角色／事件／追溯矩陣，不寫檔
```

文件內提到的實作目標（尚未存在於本 repo）：

- 技術棧：Java 25 / Spring Boot 4.1.1 / Lombok / Spring Data JPA / PostgreSQL
- `kanban-core`：純領域模型（`io.progden.kanban.core.domain`），不依賴 Spring／JPA
- `kanban-spring`：application／web／persistence 層，實作 domain 定義的 port（例如 `CardLookupPort`），以及跨 aggregate 的讀取投影（`io.progden.kanban.query.*`）
- 驗收測試以 Cucumber 執行規格中的 Gherkin Scenario

## 文件結構

`.dev/F<兩位數>-<名稱>/` 一個功能模組一個目錄，編號連續（目前 F01～F07），內含：

- `spec-<模組>.md`：BDD 規格（Gherkin + usecase 區塊），是行為的唯一依據，`spec-check` 解析它
- `ui-<模組>.md`（選填，目前 F01～F07 皆有）：UI 短規格，依 `ui-convention.md`，`ui-check` 解析它
- `legacy-spec-<模組>.md`：遷移前的規格備份（spec 遷移 loop 用來比對行為），不被腳本掃描、不可修改，遷移確認後可刪
- `design-<模組>.md`（F01～F04 有）：**後端**領域模型設計備忘，不被任何腳本解析；spec 內文仍以舊名 `design.md` 稱呼它，遷移時一併改

| 編號 | 模組 | 重點 |
|---|---|---|
| F01 | basic-kanban | Board（含 Swimlane、Stage）與 Card 兩個 Aggregate Root |
| F02 | user-membership | 新增 User、BoardMembership；卡片負責人改為多選（CR-002）；活動紀錄 |
| F03 | kanban-widgets | Cycle/Lead Time、WIP、Throughput/CFD、截止日期提醒；只做讀取投影，不新增 aggregate |
| F04 | board-clock | 每個 Board 自己的時鐘，所有 Board/Card 事件時間取自 Board Clock（CR-004） |
| F05 | workload | 依 Active Card 的負責人統計工作量 |
| F06 | feature-cr-board | 用看板追蹤 Feature／CR 卡的開發狀態 |
| F07 | canvas-layout | 每個 Board 一個 Canvas，上面的 Item（元件放置）可移動／調整大小／排層序／批次操作，Viewport 記錄每位使用者的平移縮放；草稿，尚未與 F01 看板顯示、F03 圖表元件整合 |

跨模組的關鍵關係（需讀多份文件才看得出來）：

- F01 已上線，F02～F05 的需求回頭改動 F01 行為時都透過 CR 處理（CR-001 操作人記錄、CR-002 負責人多選、CR-003 Stage 角色 Start/Done、CR-004 事件時間改用 Board Clock）。
- Stage 角色（CR-003）是 F03 計算 Cycle/Lead Time、F05 判斷卡片是否完成、F06 判斷 Feature／CR 狀態的共同基礎。
- F07（Canvas／Item／Viewport）每個 Board 對應一個 Canvas（`canvas.board` 參照 F01 的 `board`），但目前是獨立草稿，尚未接上 F01 的看板顯示或 F02 的看板建立流程，也還沒定義 F03 圖表元件如何成為 Item；整合時機與方式待後續 CR 決定，見 `spec-canvas-layout.md`「待釐清」。
- Board Clock（F04）是 F03 `asOf`、Aging、逾期判斷的時間基準；User／BoardMembership 事件仍用系統時間。

改功能編號時，要同步更新目錄名、文件內文字與相對路徑引用；範例資料中的標籤（例如 F06 規格裡的 `"F01"`、`"F02"`）不是功能引用，不要跟著改。

## 討論規格問題時

回答規格相關問題（行為、名詞、CRUD、角色、事件等）時，要引用對應 `spec-<模組>.md`／`ui-<模組>.md` 裡具體寫出的內容（名詞表、usecase 區塊、Scenario、變更紀錄等），不要用自己推測或一般看板系統的常識填補；規格沒寫到的，要明講「規格未定義」而不是腦補。

描述情境時用 ontology 的方式：先講清楚情境中出現的實體（entity，對應名詞表的 ID）與它們的欄位（`entity.attr`），再講實體之間的關係（`r-`，含來源／目標／min-max），最後才是事件（`ev-`）與角色（Role）如何透過 usecase（`uc-`）改變這些實體與關係的狀態。避免只用敘述性文字帶過，讓實體、關係、事件的對應在回答中是清楚可追溯的。

## 修復 OQ／open question 時

不管是哪一份 open-questions／open questions 清單（`.dev/open-questions.md`、各 loop 的 `open-questions.md` 等），修一則 OQ 或新增一則 OQ 時：

- 一定要去源頭文件把具體文字拿出來，逐字引用用『』包住；**不要自己縮寫、改寫、摘要成一句話**——縮寫過的版本我看不出來你到底讀到了什麼，等於沒有依據。
- 如果兩處文字互相矛盾或衝突，兩段原文都要逐字列出來並列呈現（不是只列一段、口頭帶過另一段說「另外還有一種說法」），讓我可以直接比對。
- 每一句斷言都要清楚標示這是「規格裡面寫的內容」（逐字引用）還是「你的推論」（由引用推導出來、規格沒有直接寫的結論），兩者不可以混在一起講、也不可以把推論寫得像是規格原文。
- 詳細格式規則（情況欄四選一、Level 標記、查重）見 [`與專家協作的提問規則-本體論分析.md`](.dev/lesson-learned/與專家協作的提問規則-本體論分析.md)，這節是它的白話版重點提醒。

## 撰寫規範（`.dev/conventions/`）

修改規格前先讀對應規範。2026-09-16 依 `.dev/prompts/improve-convention-prompt.md` 大改過一次，目的是讓腳本能解析 spec／ui 檔；2026-09-17 又依 `docs-convention.md` 訂出的三層邊界，改寫 spec／ui／cr 三份 convention。**F01～F06 的 spec 已於 2026-09-18 遷移完成**（`.dev/loops/spec-migration-loop/spec-migration-state.md`：「F01～F06 全部完成，全檔 0 error」，`runtime/DONE` 已建立），`ui-*.md` 也已由 `ui-authoring-loop` 補齊 F02～F07；遷移過程中的決議見 `spec-migration-open-questions.md`（Q1～Q10）與 `ui-authoring-open-questions.md`。

- **docs-convention.md**（入口）：定義 spec／ui／design／CR 四種文件各自的斷言主詞與邊界（MECE）、引用方向只能單向（`spec ← ui ← design`、`CR → spec/ui`）、誰在什麼時候讀哪份。規則衝突時以各自 convention 為準，邊界該寫在哪份文件的爭議以本文件為準。
- **spec-convention.md**（規格檔怎麼寫）
  - 第一個 H2 之前必有一行 `狀態：草稿` 或 `狀態：定稿`；「定稿」之後的結構性改動都要走 CR。「開發中」不是檔頭值，是衍生狀態：某模組在 `.dev/CR.md` 有 CR 狀態為「待處理」才算——只有這時候 `cr-check` 的 GH-05 才會嚴格擋「PR diff 動到卻沒有 `@CR-`」的 Scenario。**CR 狀態改成「待處理」／改回「處理完成」是保護開關，不是文書作業**：進入真正開發、驗收完成這兩個時間點都要同步更新 `.dev/CR.md` 的狀態欄，漏改會讓這個模組失去 GH-05 保護，或誤擋其他模組的格式性修訂。
  - 段落標題是固定字串（`## 名詞定義`、`## 角色定義`、`## Aggregate 標記說明`、`## 變更紀錄`、`## Feature:`、`### Use Case 定義`、`## 待釐清`），以前綴比對，後面可接括號。
  - 名詞定義拆三張表：實體（ID 純小寫，含 aggregate 內部實體，加「所屬 Aggregate」欄）、欄位（`entity.attr`）、關係（來源／目標／min／max）；非實體名詞放「其他名詞」表，沒有 ID。
  - 每個 Feature 在 Gherkin 之前有一個 ```usecase 區塊（YAML）：`id`／`name`／`roles`／`crud`／`pre`／`post` 必填，`fail`／`emits`／`requires`／`calls-sync` 欄位要在可空。
  - Gherkin 步驟用意圖與領域結果語言，不用 UI 操作語言：Given/When/Then/And 不得出現「點擊、輸入、拖曳、顯示、畫面、按鈕、視窗、對話框、提示我」；一個 Scenario 只有一個 When（一次交易），確認／取消等互動改由 `ui-<模組>.md` 操作表的「需確認？」欄承接。
  - 每個 Scenario 必掛 `@uc-<id>`，失敗情境加 `@fail-<pN>`；tag 順序固定：狀態 tag → `@wip` → `@CR-xxx` → `@uc-` → `@fail-`。
  - Aggregate 註解名稱必須是實體 ID，且與 usecase 的 `crud` 一致（以 usecase 為準）。
  - **反引號只給六種 ID 用**（entity、`entity.attr`、`r-`、`uc-`、`ev-`、`s-`）；程式碼名稱改用「」，檔案路徑是唯一例外。Gherkin 步驟裡不用反引號。
  - 失敗結果寫成 `Then 拒絕，訊息為 "..."，且資料不變`；Feature 標頭「身為 <角色名稱>」的名稱必須在角色表。
- **ui-convention.md**（`ui-<模組>.md` 怎麼寫）：畫面標題 `## s-<id>：<名稱>`，八個固定段落，資料表來源填 Attribute ID、操作表觸發填 UseCase ID、角色表填 Role ID、導覽用 Screen ID；操作表「失敗時」欄只寫呈現方式（依 `uc-xxx` p2 引用 fail key，不重述業務結果）；驗收條件斷言主詞只能是畫面元素或是否觸發 `uc-xxx`，不寫領域狀態；ui 檔不定義新概念；畫面總表與追溯矩陣由 `--report` 生成，不手寫。
- **cr-convention.md**（流程）：規格定稿後，改 Scenario、名詞表、角色表、usecase 區塊、ui 的操作／角色／導覽／驗收條件都要先開 CR（`CR-<三位數>`）；總表 `.dev/CR.md` 的「影響 ID」欄列受影響的 Entity／UseCase／Screen，`cr-check` 用它對照 PR diff；`design-<模組>.md` 的改動不算，不需 CR。`.dev/CR.md` 的「狀態」欄不只是紀錄：改成「待處理」代表這個模組真的有人在開發，`cr-check` 的 GH-05 才會嚴格保護它（§1.1）；忘記更新等於沒保護到，或誤擋別的模組。
- **checks.md** / **scripts.md** / **llm-review.md** / **open-questions.md**：檢查清單（ID 對應 `scripts/speccheck/checks/`）、腳本規格、LLM 語意 review 清單、待決事項。
- **spec 遷移 loop**（`.dev/loops/spec-migration-loop/`）：無人值守把六份 spec 遷到新格式。規則書 `spec-migration-prompt.md`、任務清單 `spec-migration-tasks.md`，啟動 `./.dev/loops/spec-migration-loop/run-spec-migration-loop.sh`（自動切到 `loop/spec-migration` 分支），執行期檔案在同目錄 `runtime/`（不進版控）；改 loop 腳本後用 `rehearsal/rehearse.sh` 以假 claude 演練。
- **git-convension.md**：commit 格式 `[類型](scope) 摘要 (#票號)`，類型為 `spec/design`、`dev`、`test`、`docs`、`chore`、`revert`；摘要用中文祈使句，50 字內，句尾不加標點。

## 腳本（`scripts/`）

- 解析層（`parser_*.py` → `model.py`）與檢查層（`checks/*.py`）分開；新增檢查只加一個 `@check("XX-nn", ...)` 函式，並在 `checks.md` 加一列，測試會核對兩邊 ID 一致。
- 程式碼註解與輸出訊息一律繁體中文。
- `scripts/tests/fixtures/good/` 是依新規範寫的範例 spec／ui／CR.md，改規範時先讓它通過，再改既有規格。
