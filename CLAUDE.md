# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 專案現況

這個 repo 目前**只有規格與設計文件**（`.dev/`），沒有程式碼、建置設定或測試，因此沒有 build／lint／test 指令；目錄也還不是 git repository。文件內容以繁體中文撰寫。

文件裡提到的實作目標（尚未存在於本 repo）：

- 技術棧：Java 25 / Spring Boot 4.1.1 / Lombok / Spring Data JPA / PostgreSQL
- `kanban-core`：純領域模型（`io.progden.kanban.core.domain`），不依賴 Spring／JPA
- `kanban-spring`：application／web／persistence 層，實作 domain 定義的 port（例如 `CardLookupPort`），以及跨 aggregate 的讀取投影（`io.progden.kanban.query.*`）
- 驗收測試以 Cucumber 執行規格中的 Gherkin Scenario

## 文件結構

`.dev/F<兩位數>-<名稱>/` 一個功能模組一個目錄，編號連續（目前 F01～F06），內含：

- `spec-<模組>.md`：BDD 規格（Gherkin），是行為的唯一依據
- `design.md`（選填）：對應規格的設計決策與實作狀態

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

修改規格前先讀對應規範，以下是最常用的規則：

- **spec-convention.md**
  - 每個 Scenario 上方要有 `# Related aggregate:` 註解，標記 read／write。
  - Feature 標頭固定三行：身為／我想要／以便。
  - Scenario 名稱描述行為與結果，不描述 UI 操作。
  - 錯誤訊息寫出完整固定文字。
  - 規格進入開發後的變更要掛 tag：`@wip`、`@added`、`@changed` + `@deprecated`、`@CR-xxx`，並在「變更紀錄」追加一行（類型：新增／變更／移除）。
- **cr-convention.md**
  - 規格已進入開發後，只要會改變開發者要做的事，就要先開 CR（`CR-<三位數>`）；尚未開發的規格可以直接改。
  - CR 生命週期：記錄 → 修改規格 → 待處理 → 處理完成（或駁回）。
  - 規範要求 CR 登記在總表 `.dev/CR.md`，但本 repo 目前沒有這份檔案。
- **git-convension.md**
  - commit 格式：`[類型](scope) 摘要 (#票號)`，類型為 `spec/design`、`dev`、`test`、`docs`、`chore`、`revert`。
  - 摘要用中文祈使句，50 字內，句尾不加標點。
- **ui-design-convention.md**：從後端 Spec 推導畫面清單（S01、S02…），逐一討論短規格，再產出 Claude Design 提示詞。
