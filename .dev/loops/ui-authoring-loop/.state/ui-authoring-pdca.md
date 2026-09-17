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
