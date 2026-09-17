# UI 撰寫工作日誌（PDCA）

本檔是 [`ui-authoring-prompt.md`](../prompts/ui-authoring-prompt.md) 的執行日誌，供每一輪重置 context 的 `/loop` 迭代追溯決策。**只能在檔案最後追加新紀錄，不可覆寫或刪除舊紀錄**。每則標題格式：`## Iteration <n> — <YYYY-MM-DD HH:MM> — <任務編號>`，內含 `### Plan`／`### Do`／`### Check`／`### Act` 四段，Check 必須貼 `./scripts/ui-check` 的結果行。

---

## Iteration 0 — 2026-09-17（建立 loop）

### Plan
依人工提出的需求（六個模組的 spec 已定稿，要依 `docs-convention.md` 的 spec/ui 邊界寫出 `ui-<模組>.md`，並用 Claude Code 內建 `/loop` 指令自我調節驅動）建立這個 loop，放在 `.dev/loops/ui-authoring-loop/`。參考已完成的 `.dev/loops/spec-migration-loop/`（規則書結構、任務清單格式、OQ 機制、PDCA 格式），但刻意精簡：不寫外層 bash 驅動腳本、不寫外部驗證腳本、不分執行輪／審查輪、不需要「保留既有行為」的 diff 檢查（這是全新撰寫，沒有 legacy 版本可比對）。本則不做任何畫面撰寫。

### Do
- 已用 Explore agent 讀過六份 spec 的 Feature／UseCase 清單，畫面盤點一次到位寫進 `ui-authoring-tasks.md`（不留給執行輪動態盤點），共 6 個模組、24 個畫面、7 個階段、38 個任務（含 1 個關卡）。
- 驗證改成每輪自己跑 `./scripts/ui-check` 並誠實記錄，不做外部不信任式驗證；任務清單規模不大（相對 spec 遷移的 ~50 項），人工/agent 肉眼挑選 `actionable` 任務可行，暫不寫 `ui-authoring-tools.py`。
- 「自主決策分級」取代 spec 遷移的「假設分級」：低風險（版面歸類）自行決定；高風險（spec 沒寫清楚的規則、角色、欄位來源）標 `⚠️` 並記 OQ，畫面維持「討論中」；環境限制記 OQ＋任務 `blocked`。
- commit 規範沿用 `git-convension.md`：`ui-<模組>.md` 用 `[spec/design](ui-<模組名>)`，loop 文件用 `[docs](loops)`。

### Check
四個檔案已建立：`ui-authoring-prompt.md`、`ui-authoring-tasks.md`、`ui-authoring-open-questions.md`、`ui-authoring-pdca.md`（本檔）。`./scripts/ui-check` 尚未執行（T0.01 會做）。

### Act
下一個任務：T0.01。

---

## Iteration 1 — 2026-09-17 09:00 — T0.01

### Plan
確認起點：跑 `./scripts/ui-check`（預期印「找不到任何 ui 檔」，因六個模組目錄下都還沒有 `ui-*.md`）；建立 `runtime/` 目錄，把 `git rev-parse HEAD` 寫入 `runtime/baseline`。驗收條件：`runtime/baseline` 存在且是合法 commit hash；本則 Check 貼上述兩個指令的輸出。

### Do
- 執行 `mkdir -p .dev/loops/ui-authoring-loop/runtime`。
- 執行 `git rev-parse HEAD > .dev/loops/ui-authoring-loop/runtime/baseline`。

### Check
- `./scripts/ui-check` 輸出：`找不到任何 ui 檔`（符合預期，因六個模組尚無 `ui-*.md`）。
- `cat .dev/loops/ui-authoring-loop/runtime/baseline` 輸出：`aa39f8215b7439ad9fd62ed1935e9d01c9281152`（等於 `git rev-parse HEAD`，是合法 commit hash）。

### Act
完成：T0.01。下一個任務：T1.01（[F01] 建立 `ui-kanban-basic.md` 檔頭與 8 個畫面標題骨架）。

## Iteration 2 — 2026-09-17 15:00 — T1.01

### Plan
建立 `.dev/F01-basic-kanban/ui-kanban-basic.md`：檔頭 ＋ 8 個畫面標題骨架（`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list`、`s-stage-delete-dialog`、`s-board`、`s-card-add-dialog`、`s-card-detail`、`s-card-delete-dialog`），每個畫面只填標題下三行（所屬 Feature、類型、狀態：未討論），八段內容先留空。預期 `ui-check` 對 `DS-01` 無 error；`DS-02`／`DS-04`（八段未填）與 `DS-06`／`DS-07`（尚無操作表／導覽）會報 error/warn，屬骨架任務預期範圍。

### Do
- 讀 `spec-kanban-basic.md` 名詞定義與三個 `## Feature:` 標題（Swimlane 管理、Stage（階段）管理、Card（卡片）編輯），對照任務清單指定的 8 個 Screen ID。
- 低風險決定：`s-swimlane-list`／`s-swimlane-delete-dialog` 歸 Swimlane 管理；`s-stage-list`／`s-stage-delete-dialog` 歸 Stage（階段）管理；`s-board`／`s-card-add-dialog`／`s-card-detail`／`s-card-delete-dialog` 歸 Card（卡片）編輯。`s-board` 類型定為「儀表板」（spec 無獨立看板總覽 Feature，畫面是卡片跨 Swimlane×Stage 的總覽，非單純列表）。
- 建立檔案，commit `badd5ba`。
- 無新增 OQ、無新增 D-xx。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 最後一行：`104 error(s), 23 warning(s)`；逐條確認全部為 `DS-02`（八段未填）、`DS-04`（狀態五項未填）、`DS-06`（spec uc 尚無畫面觸發，屬 warn）、`DS-07`（尚無導覽關係，屬 warn），無 `DS-01` error（`grep -c DS-01` = 0）。
- `tools accept-check`：本任務驗收條件無機械 token，改肉眼核對：8 個 `## s-` 標題與三行標頭皆存在，格式符合 `ui-convention.md`；`ui-check` 對 `DS-01` 0 error，達成。
- 未新增 OQ／『』引用，未跑 `verify-quotes.py`。

### Act
完成：T1.01。下一個任務：T1.02（[F01] 定案 `s-swimlane-list`，類型：列表，對應 `uc-add-swimlane`／`uc-rename-swimlane`／`uc-reorder-swimlane`）。
