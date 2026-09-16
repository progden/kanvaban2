# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 專案現況

這個 repo 有兩部分：`.dev/` 的規格與設計文件（繁體中文），以及 `scripts/` 的規格檢查腳本（Python 3.10+，只依賴 PyYAML）。沒有應用程式程式碼、沒有 build。

指令：

```bash
python3 -m unittest discover -s scripts/tests   # 腳本的測試
./scripts/spec-check                             # 檢查 .dev/F*/spec-*.md（目前既有規格尚未遷移，會報大量 error）
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

`.dev/F<兩位數>-<名稱>/` 一個功能模組一個目錄，編號連續（目前 F01～F06），內含：

- `spec-<模組>.md`：BDD 規格（Gherkin + usecase 區塊），是行為的唯一依據，`spec-check` 解析它
- `ui-<模組>.md`（選填，目前尚無）：UI 短規格，依 `ui-design-convention.md`，`ui-check` 解析它
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

跨模組的關鍵關係（需讀多份文件才看得出來）：

- F01 已上線，F02～F05 的需求回頭改動 F01 行為時都透過 CR 處理（CR-001 操作人記錄、CR-002 負責人多選、CR-003 Stage 角色 Start/Done、CR-004 事件時間改用 Board Clock）。
- Stage 角色（CR-003）是 F03 計算 Cycle/Lead Time、F05 判斷卡片是否完成、F06 判斷 Feature／CR 狀態的共同基礎。
- Board Clock（F04）是 F03 `asOf`、Aging、逾期判斷的時間基準；User／BoardMembership 事件仍用系統時間。

改功能編號時，要同步更新目錄名、文件內文字與相對路徑引用；範例資料中的標籤（例如 F06 規格裡的 `"F01"`、`"F02"`）不是功能引用，不要跟著改。

## 撰寫規範（`.dev/conventions/`）

修改規格前先讀對應規範。2026-09-16 依 `.dev/prompts/improve-convention-prompt.md` 大改過一次，目的是讓腳本能解析 spec／ui 檔；**既有 F01～F06 的 spec 尚未遷移到新格式**（遷移順序見 `open-questions.md` Q10，建議先遷 F01；其餘決定已寫回各規範，`open-questions.md` 開頭有已決定表）。

- **spec-convention.md**（規格檔怎麼寫）
  - 第一個 H2 之前必有一行 `狀態：草稿` 或 `狀態：開發中`；「開發中」之後的改動都要走 CR。
  - 段落標題是固定字串（`## 名詞定義`、`## 角色定義`、`## Aggregate 標記說明`、`## 變更紀錄`、`## Feature:`、`### Use Case 定義`、`## 待釐清`），以前綴比對，後面可接括號。
  - 名詞定義拆三張表：實體（ID 純小寫，含 aggregate 內部實體，加「所屬 Aggregate」欄）、欄位（`entity.attr`）、關係（來源／目標／min／max）；非實體名詞放「其他名詞」表，沒有 ID。
  - 每個 Feature 在 Gherkin 之前有一個 ```usecase 區塊（YAML）：`id`／`name`／`roles`／`crud`／`pre`／`post` 必填，`fail`／`emits`／`requires`／`calls-sync` 欄位要在可空。
  - 每個 Scenario 必掛 `@uc-<id>`，失敗情境加 `@fail-<pN>`；tag 順序固定：狀態 tag → `@wip` → `@CR-xxx` → `@uc-` → `@fail-`。
  - Aggregate 註解名稱必須是實體 ID，且與 usecase 的 `crud` 一致（以 usecase 為準）。
  - **反引號只給六種 ID 用**（entity、`entity.attr`、`r-`、`uc-`、`ev-`、`s-`）；程式碼名稱改用「」，檔案路徑是唯一例外。Gherkin 步驟裡不用反引號。
  - 錯誤訊息寫出完整固定文字；Feature 標頭「身為 <角色名稱>」的名稱必須在角色表。
- **ui-design-convention.md**（`ui-<模組>.md` 怎麼寫）：畫面標題 `## s-<id>：<名稱>`，八個固定段落，資料表來源填 Attribute ID、操作表觸發填 UseCase ID、角色表填 Role ID、導覽用 Screen ID；ui 檔不定義新概念；畫面總表與追溯矩陣由 `--report` 生成，不手寫。
- **cr-convention.md**（流程）：規格進入開發後，改 Scenario、名詞表、角色表、usecase 區塊、ui 的操作／角色／導覽都要先開 CR（`CR-<三位數>`）；總表 `.dev/CR.md` 的「影響 ID」欄列受影響的 Entity／UseCase／Screen，`cr-check` 用它對照 PR diff。**本 repo 目前沒有 `.dev/CR.md`**，既有規格裡的 `@CR-001`～`@CR-004` 會被 REF-06 報未登記，遷移時要一併建立。
- **checks.md** / **scripts.md** / **llm-review.md** / **open-questions.md**：檢查清單（ID 對應 `scripts/speccheck/checks/`）、腳本規格、LLM 語意 review 清單、待決事項。
- **spec 遷移 loop**（`.dev/loops/spec-migration-loop/`）：無人值守把六份 spec 遷到新格式。規則書 `spec-migration-prompt.md`、任務清單 `spec-migration-tasks.md`，啟動 `./.dev/loops/spec-migration-loop/run-spec-migration-loop.sh`（自動切到 `loop/spec-migration` 分支），執行期檔案在同目錄 `runtime/`（不進版控）；改 loop 腳本後用 `rehearsal/rehearse.sh` 以假 claude 演練。
- **git-convension.md**：commit 格式 `[類型](scope) 摘要 (#票號)`，類型為 `spec/design`、`dev`、`test`、`docs`、`chore`、`revert`；摘要用中文祈使句，50 字內，句尾不加標點。

## 腳本（`scripts/`）

- 解析層（`parser_*.py` → `model.py`）與檢查層（`checks/*.py`）分開；新增檢查只加一個 `@check("XX-nn", ...)` 函式，並在 `checks.md` 加一列，測試會核對兩邊 ID 一致。
- 程式碼註解與輸出訊息一律繁體中文。
- `scripts/tests/fixtures/good/` 是依新規範寫的範例 spec／ui／CR.md，改規範時先讓它通過，再改既有規格。
