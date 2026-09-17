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

## Iteration 3 — 2026-09-17 15:30 — T1.02

### Plan
定案（依短規格完整度）`s-swimlane-list`（.dev/F01-basic-kanban/ui-kanban-basic.md）：填齊八段，對應 `uc-add-swimlane`／`uc-rename-swimlane`／`uc-reorder-swimlane`，刪除操作導向 `s-swimlane-delete-dialog`。預期本畫面 `ui-check` 0 error；整檔 error 數應從 104 下降。

### Do
- 讀 `spec-kanban-basic.md`「Feature: Swimlane 管理」usecase 區塊（`uc-add-swimlane`／`uc-rename-swimlane`／`uc-reorder-swimlane`／`uc-delete-swimlane`）、Gherkin Scenario、名詞定義三張表（`swimlane`、`swimlane.name`、`board`-`swimlane` 關係）、角色定義（僅 `r-user`）。
- 填入目的、進入與離開、角色與權限、資料、操作、狀態、驗收條件、待確認事項八段；畫面狀態設為「討論中」（因進入路徑未定）。
- 高風險：spec 未描述如何從看板畫面前往 Swimlane 管理（`s-board` 尚未定案），依「自主決策分級」標 `⚠️`，新增 `OQ-01`（【推論】，依據 Feature: Swimlane 管理 Background），暫定「模組入口」以維持八段格式完整。
- 低風險決定：新增／重新命名／拖曳排序皆判定為可逆操作（需確認＝否）；刪除操作在本畫面僅為導向 `s-swimlane-delete-dialog` 的前端動作（觸發＝—，需確認＝否，實際刪除確認在該對話框畫面）；角色表「做得到」欄逐字寫操作名稱以滿足 `DS-05` 比對。
- commit `.dev/F01-basic-kanban/ui-kanban-basic.md`（`[spec/design](kanban-basic)`）與 OQ 檔（`[docs](loops)`）。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`：`grep -i swimlane-list` 無任何輸出（本畫面 0 error）；整檔最後一行 `93 error(s), 18 warning(s)`（較上一輪 104 error 下降，其餘 error/warn 皆屬其餘 7 個尚未定案的骨架畫面，預期範圍）。
- `tools accept-check ... T1.02`：exit 0，無機械 token 需檢查（驗收條件為肉眼核對格式）。
- 逐條對照驗收條件：`ui-check` 對 `s-swimlane-list` 無 error（達成）；八段齊全（`### 目的`～`### 待確認事項` 依序存在，達成）。
- `python3 .../scripts/verify-quotes.py`：通過（`逐字引用驗證通過`）。

### Act
完成：T1.02。下一個任務：T1.03（[F01] 定案 `s-swimlane-delete-dialog`，對應 `uc-delete-swimlane`）。

## Iteration 4 — 2026-09-17 15:50 — T1.03

### Plan
定案 `s-swimlane-delete-dialog`（.dev/F01-basic-kanban/ui-kanban-basic.md）：填齊八段，對應 `uc-delete-swimlane`，含「該 Swimlane 內有卡片時一併刪除」的提示與確認、「僅剩最後一個 Swimlane」的失敗情境。預期本畫面 `ui-check` 0 error；整檔 error 數應從 93 下降。

### Do
- 讀 `spec-kanban-basic.md`「Feature: Swimlane 管理」`uc-delete-swimlane`（`pre.p1`／`post`／`fail.p1`）與三則相關 Scenario（刪除空的 Swimlane、刪除包含卡片的 Swimlane 需要確認、看板至少保留一個 Swimlane）；`post` 明確寫「若該 swimlane 內有 card，一併被刪除」，沒有轉移到其他泳道的行為。
- 低風險：任務清單描述提到「含目的泳道選擇的成功／失敗情境」，但比對 spec 後該 uc 是刪除卡片而非轉移，故本畫面不含「目的泳道」欄位；未改任務描述，僅在 state／PDCA 記錄依據為 spec 而非任務描述字面。
- 填入目的、進入與離開（`s-swimlane-list` 進來，完成回 `s-swimlane-list`）、角色與權限（僅 `r-user`）、資料（`swimlane.name` 顯示；`swimlane`→`card` 關係計數顯示）、操作（確認刪除→`uc-delete-swimlane`，需確認＝是（本畫面即確認）；取消→`—`，需確認＝否）、狀態五項、驗收條件、待確認事項（無）；畫面狀態標「已定案」（spec 資訊足夠完整推導，無 `⚠️`）。
- 「失敗時」欄引用 `uc-delete-swimlane` p1（僅剩最後一個 Swimlane），供併發刪除情境使用；不重述業務結果文字。
- commit `.dev/F01-basic-kanban/ui-kanban-basic.md`（`[spec/design](kanban-basic)`）與任務清單／state（`[docs](loops)`）。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`：輸出中無任何提及 `s-swimlane-delete-dialog` 的 error（本畫面 0 error）；整檔最後一行 `80 error(s), 17 warning(s)`（較上一輪 93 error 下降，其餘 error/warn 屬其餘尚未定案的骨架畫面與既有 `REF-07`/`DS-06` 項目，非本輪範圍）。
- `tools accept-check ... T1.03`：無輸出（驗收條件「同上，本畫面無 error」非機械 token），改用 `ui-check` 輸出肉眼核對：無 error 通過。
- 逐條對照驗收條件：開啟顯示 Swimlane 名稱（資料表已列）、有卡片時顯示卡片數（資料表已列）、確認刪除後回 `s-swimlane-list` 且觸發 `uc-delete-swimlane`（操作表已列）、取消後關閉對話框資料不變（操作表已列）——皆達成。
- 本輪未新增／修改 OQ 或帶【引用原文】等標記的待確認事項，未跑 `verify-quotes.py`。

### Act
完成：T1.03。下一個任務：T1.04（[F01] 定案 `s-stage-list`，對應 `uc-add-stage`／`uc-rename-stage`／`uc-reorder-stage`／`uc-set-stage-role`）。
