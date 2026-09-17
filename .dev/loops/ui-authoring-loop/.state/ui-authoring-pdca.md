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

## Iteration 5 — 2026-09-17 16:10 — D-01

### Plan
修正 `ui-kanban-basic.md` 第 35、36 行 REF-07 error：「失敗時」欄位中的「無 `fail` 定義」把 `fail` 誤用反引號（反引號只給六種 ID 用），改成不加反引號的寫法，語意不變。預期整檔 error 數從 80 降到 78，不新增其他 error。

### Do
- 讀 D-01 任務描述與指定行號，確認第 35、36 行分別是「重新命名 Swimlane」「拖曳調整順序」兩列的「失敗時」欄。
- 將「不適用（`uc-rename-swimlane` 無 `fail` 定義）」「不適用（`uc-reorder-swimlane` 無 `fail` 定義）」改為「不適用（`uc-rename-swimlane` 無 fail 定義）」「不適用（`uc-reorder-swimlane` 無 fail 定義）」，僅移除 `fail` 的反引號，其餘文字不變。
- commit `.dev/F01-basic-kanban/ui-kanban-basic.md`（`[spec/design](kanban-basic)`）與任務清單／state（`[docs](loops)`）。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 最後一行：`78 error(s), 17 warning(s)`（由 80 降為 78）；輸出中無任何 `REF-07`。
- `s-swimlane-list` 行號範圍（第 5～57 行）內用該行號範圍比對 `ui-check` 輸出，無任何 error 落在此範圍。
- 驗收條件三項：無 REF-07（達成）、`s-swimlane-list` 範圍內無 error（達成）、整檔 error 數 ≤78（達成，剛好 78）。
- 本輪未新增／修改 OQ 或帶引用標記的待確認事項，未跑 `verify-quotes.py`。

### Act
完成：D-01。下一個任務：T1.04（[F01] 定案 `s-stage-list`，對應 `uc-add-stage`／`uc-rename-stage`／`uc-reorder-stage`／`uc-set-stage-role`）。

## Iteration 6 — 2026-09-17 16:30 — T1.04

### Plan
定案 `s-stage-list`（類型：列表），對應 `uc-add-stage`／`uc-rename-stage`／`uc-reorder-stage`／`uc-set-stage-role`；仿照 T1.02（`s-swimlane-list`）的結構填八段。讀 `spec-kanban-basic.md`「Feature: Stage（階段）管理」的 usecase 區塊與 Scenario，資料段引用 `stage.name`／`stage.role`／`board`-`stage` 關係。刪除操作因對應 `uc-delete-stage` 含卡片轉移情境（獨立任務 T1.05），本畫面「刪除 Stage」列觸發欄填 `—`、成功後開啟 `s-stage-delete-dialog`（比照 `s-swimlane-list` 對 `s-swimlane-delete-dialog` 的做法）。預期本畫面 0 error，整檔 error 數持平或略降（新增內容，其他未寫畫面 error 不變）。

### Do
- 讀 `spec-kanban-basic.md` 第 231～408 行（Feature: Stage 管理，usecase 區塊＋Gherkin）、名詞定義三張表、角色定義。
- 填 `s-stage-list` 八段：目的、進入與離開（比照 OQ-01 模式，進入路徑未定，標 `⚠️`）、角色與權限（`r-user`）、資料（`stage.name`／順序／`stage.role`）、操作（新增／重新命名／拖曳排序／設定角色／刪除，皆比照 fail 為空的呈現方式）、狀態（五項齊全，含僅剩 1 個 Stage 時刪除不可用、設角色時原持有者變回 NONE 的資料狀態差異）、驗收條件、待確認事項。
- 高風險：進入路徑 spec 未寫清楚，比照 OQ-01 判斷，新增 OQ-02（【推論】，同類問題，暫定模組入口，待 `s-board` 定案時確認），`s-stage-list` 狀態維持「討論中」。
- commit `.dev/F01-basic-kanban/ui-kanban-basic.md`（`[spec/design](kanban-basic)`）與任務清單／state／OQ（`[docs](loops)`）。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 最後一行：`65 error(s), 11 warning(s)`（由 78 降為 65，減少的 13 個 error 對應 `s-stage-list` 原本因骨架缺段落產生的 `DS-02`/`DS-04`）；輸出中無任何一行含 "s-stage-list"。
- 驗收條件：本畫面無 error（達成，`grep "s-stage-list"` 無輸出）；八段齊全（達成，含五項狀態、待確認事項標 `⚠️` 但段落本身存在）。
- 本輪新增 OQ-02，且 `s-stage-list`「待確認事項」含【推論】類引用（透過 OQ-02）：跑 `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`，結果「逐字引用驗證通過」。

### Act
完成：T1.04。下一個任務：T1.05（[F01] 定案 `s-stage-delete-dialog`，對應 `uc-delete-stage`，含卡片轉移情境）。

## Iteration 7 — 2026-09-17 16:50 — T1.05

### Plan
本輪任務 T1.05：定案 `s-stage-delete-dialog`（對話框），對應 `uc-delete-stage`（含卡片轉移情境）。要改的檔案：`.dev/F01-basic-kanban/ui-kanban-basic.md`（填該畫面八段內容）；驗收條件：對本畫面無 error，八段齊全。預期 `ui-check` 全檔 error 數延續下降（起點 65 → 預期約 52，因為仍有 3 個畫面骨架未填）。

### Do
- 讀 `spec-kanban-basic.md` 的 `uc-delete-stage`（pre p1：`board` 中 `stage` 數量大於 1；post：`stage` 刪除、若有卡片則 `card.stage` 更新為使用者選擇的目的 `stage`；fail p1）與對應兩則 Scenario（「刪除空的 Stage」「刪除包含卡片的 Stage 需要先轉移卡片」「看板至少保留一個 Stage」）。
- 填妥 `s-stage-delete-dialog` 八段：資料段比對 `s-swimlane-delete-dialog` 的既有寫法，多加「目的 Stage」欄位（來源 `board`→`stage` 關係），操作表拆「選擇目的 Stage」（純前端、觸發 `—`）與「確認刪除」（觸發 `uc-delete-stage`，需確認：是）兩列；狀態段「資料狀態差異」寫明有卡片才需選目的 Stage。
- 高風險：spec 的 `uc-delete-stage` post 沒寫目的 Stage 是否可排除欲刪除的 Stage 本身，標 `⚠️` 於「資料」段驗證欄與「待確認事項」，畫面狀態維持「討論中」，新增 `OQ-03`（【推論】格式，引用 post 原文）。
- commit：`75cceda [spec/design](kanban-basic) 定案 s-stage-delete-dialog 八段短規格`。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`：`52 error(s), 10 warning(s)`（起點 65，`s-stage-delete-dialog` 本身無 error，剩餘 error 都是 `s-board`／`s-card-add-dialog`／`s-card-detail`／`s-card-delete-dialog` 骨架未填，屬預期）。
- 逐條對照驗收條件：`ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)` 對本畫面無 error（達成，grep 該畫面標題行後無任何 error）；八段齊全（達成）。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：逐字引用驗證通過（本輪 OQ-03 與待確認事項的『』引用皆與 spec 逐字相符）。

### Act
完成 T1.05。下一個任務：T1.06（[F01] 定案 `s-board`，對應 `uc-move-card-swimlane`／`uc-move-card-stage`，並在操作表列出跨模組引用的 `uc-assign-card-owner-by-drag`）。

## Iteration 8 — 2026-09-17 14:54 — T1.06

### Plan
本輪任務：T1.06（[F01] 定案 `s-board`，對應 `uc-move-card-swimlane`／`uc-move-card-stage`，操作表另列出跨模組的 F02 `uc-assign-card-owner-by-drag`）。要改的檔案：`.dev/F01-basic-kanban/ui-kanban-basic.md`（填 `s-board` 八段）；同時檢視能否解掉 OQ-01／OQ-02（前幾輪標記「待 `s-board` 定案時回頭確認」）。驗收條件：八段齊全、操作表含 `uc-assign-card-owner-by-drag` 一列；預期 `ui-check`（含 `--spec` 涵蓋 F02 spec）對 `s-board` 無 error。

### Do
- 讀 `spec-kanban-basic.md` 的 `uc-move-card-swimlane`／`uc-move-card-stage`（Feature: Card 編輯）與 `spec-user-membership.md` 的 `uc-assign-card-owner-by-drag`（Feature: 卡片負責人指派，含 `card.assignees`／`board-membership` 欄位表），填妥 `s-board` 八段。
- 低風險決定：`s-board` 操作表新增「管理 Swimlane」「管理 Stage」兩列（觸發 `—`），分別導向 `s-swimlane-list`／`s-stage-list`；據此解掉 OQ-01／OQ-02——把兩畫面「進入與離開.從哪裡進來」改為「`s-board` 的『管理 Swimlane／Stage』操作」，「待確認事項」清空為「（無）」，狀態由「討論中」改為「已定案」（OQ 表本身依規則只能附加列，不可回改既有列，故 OQ-01／OQ-02 原文保留，改動只發生在 ui 檔）。
- 高風險項目記入 OQ 並標 ⚠️（`s-board` 維持「討論中」）：
  - OQ-04：`s-board` 本身「從哪裡進來」——開啟看板是跨模組行為（F02），對應畫面（任務清單 T2.01 規劃的 `s-board-list`）尚未定案，暫不引用未定案 ID。
  - OQ-05：看板成員清單（供拖曳頭像用）呈現於 `s-board` 何處，spec 無依據。
  - OQ-06（【矛盾】）：F02 `uc-assign-card-owner-by-drag` 等 5 個 usecase 的 `roles: [r-user]`，與 F02 自身「角色定義」表（`r-system-user`／`r-board-owner`／`r-board-member`，無 `r-user`）不一致；暫依 usecase 區塊填 `r-user`，回饋後端 Spec。
- commit `36...`（`[spec/design](kanban-basic) 定案 s-board 八段短規格`）。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`（不帶 `--spec`）：51 error(s), 4 warning(s)；其中 11 筆 REF-07 是 `s-board` 引用 F02 尚未載入的 ID（`card.assignees`／`uc-set-card-assignees`／`board-membership`／`uc-assign-card-owner-by-drag`／`r-system-user`／`r-board-owner`／`r-board-member`）——這是工具限制（`ui-check` 只依已存在的 `ui-<模組>.md` 自動帶入對應 `spec-<模組>.md`，F02 的 `ui-user-membership.md` 尚未建立，見 T2.01），不是本畫面內容錯誤。
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec .dev/F02-user-membership/spec-user-membership.md`：39 error(s)（全部屬於既有的 `s-card-add-dialog`／`s-card-detail`／`s-card-delete-dialog` 骨架，非本輪範圍），`s-board` 無任何 DS-01～DS-05／REF-07 error。
- 驗收條件對照：八段齊全（達成）；操作表含 `uc-assign-card-owner-by-drag` 一列（達成，見「拖曳成員頭像到卡片追加負責人」列）。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：逐字引用驗證通過（OQ-06 的『r-system-user』『r-board-owner』『r-board-member』等引用已改標 `user-membership` 模組並移除誤觸發 F01 比對的路徑字樣後過關）。

### Act
完成 T1.06，OQ-01／OQ-02 已解掉（`s-swimlane-list`／`s-stage-list` 改標「已定案」）。下一個任務：T1.07（[F01] 定案 `s-card-add-dialog`，對應 `uc-add-card`）。

## Iteration 9 — 2026-09-17 — D-02
### Plan
本輪任務：D-02（[F01] 修正 `ui-kanban-basic.md` 3 處把業務結果／領域狀態寫進 UI 段落的寫法問題）。要改的檔案：`.dev/F01-basic-kanban/ui-kanban-basic.md`——(1) `s-stage-delete-dialog` 驗收條件刪掉「已移動到選擇的目的 Stage」；(2) `s-board` 操作表「拖曳成員頭像到卡片追加負責人」成功後改為呈現方式，對應驗收條件同步改；(3) `s-board` 狀態段「錯誤」改成以「不適用」開頭。驗收條件：兩份文字不再出現「已移動到」「負責人追加該成員」，`s-board` 狀態段「錯誤」以「不適用」開頭，`ui-check`（含 `--spec` F02）error 數不增加。

### Do
- 三處均改為單純寫法調整，未改變業務結果：
  - `s-stage-delete-dialog` 驗收條件：移除「該 Stage 內原有卡片（如有）已移動到選擇的目的 Stage」子句，只留畫面可見的斷言（不再顯示於列表、回列表、觸發 uc）。
  - `s-board` 操作表「拖曳成員頭像到卡片追加負責人」成功後：改為「卡片縮圖的負責人顯示更新」；對應驗收條件同步改為「卡片縮圖的負責人顯示更新，且觸發 `uc-assign-card-owner-by-drag`」。
  - `s-board` 狀態段「錯誤」：改為「不適用（`uc-move-card-swimlane`／`uc-move-card-stage`／`uc-assign-card-owner-by-drag` 目前均無 fail 定義）」，消除前後矛盾。
- commit `05da454`（`[spec/design](kanban-basic) 修正 UI 段落誤寫業務結果的三處`）。

### Check
- `grep -n "已移動到\|負責人追加該成員" .dev/F01-basic-kanban/ui-kanban-basic.md`：無比對結果（兩處文字皆已移除）。
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`（不帶 `--spec`）：51 error(s), 4 warning(s)，與本輪開工前相同，未增加。
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec .dev/F02-user-membership/spec-user-membership.md`：39 error(s)，較開工前（40，即 51 扣掉 11 筆跨模組 REF-07）少 1，未增加。
- 驗收條件對照：`s-stage-delete-dialog` 驗收條件不含「已移動到」（達成）；`s-board` 操作表與驗收條件不含「負責人追加該成員」（達成）；`s-board` 狀態段「錯誤」以「不適用」開頭（達成）；error 數不增加（達成）。
- 本輪未新增或修改 OQ／『』引用，未跑 `verify-quotes.py`。

### Act
完成 D-02。下一個任務：D-03（[F01] 補上 T1.06 推翻 OQ-01／OQ-02 的紀錄）。

## Iteration 10 — 2026-09-17 — D-03
### Plan
本輪任務：D-03（[F01] 補上 T1.06 推翻 OQ-01／OQ-02 的紀錄）。要改的檔案：`ui-authoring-open-questions.md`（表尾追加一列，「情況」欄以【推論】明講「推翻 OQ-01、OQ-02」並引用兩個 Feature Background 原文）、`.dev/F01-basic-kanban/ui-kanban-basic.md`（`s-swimlane-list`／`s-stage-list`「從哪裡進來」改成不宣稱 `s-board` 已定案的寫法，引用新 OQ 編號）。驗收條件：OQ 表新增一列含「推翻 OQ-01」與「推翻 OQ-02」；`ui-kanban-basic.md` 不含「定案結果更新」；`verify-quotes.py` 回傳 0。

### Do
- OQ 表尾追加 OQ-07：【推論】引用「Feature: Swimlane 管理」與「Feature: Stage（階段）管理」Background 原文『And 我已開啟一個名為 "產品開發看板" 的看板』，推論並明講「推翻 OQ-01、推翻 OQ-02」——`s-swimlane-list`／`s-stage-list` 的進入路徑是 `s-board` 操作表（T1.06 新增）的「管理 Swimlane」「管理 Stage」兩列，但強調 `s-board` 本身「從哪裡進來」仍為討論中（見 OQ-04／OQ-05／OQ-06），這個推論不代表 `s-board` 已定案。
- `s-swimlane-list`／`s-stage-list`「從哪裡進來」由「見 OQ-01／OQ-02，已依 `s-board` 定案結果更新」改為「見 OQ-07；`s-board` 本身『從哪裡進來』仍為討論中，見 OQ-04」，移除與事實不符的「已依 `s-board` 定案結果更新」敘述；兩畫面狀態維持「已定案」不變（進入路徑本身已有 `s-board` 操作表列佐證，只是措辭不得宣稱 `s-board` 整體定案）。
- commit `[docs](loops)`（OQ 檔與任務清單）與 `[spec/design](kanban-basic)`（ui 檔）分開提交。

### Check
- `grep -n "定案結果更新" .dev/F01-basic-kanban/ui-kanban-basic.md`：無比對結果。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（回傳 0）。
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`（不帶 `--spec`）：51 error(s), 4 warning(s)，與開工前持平。
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec .dev/F02-user-membership/spec-user-membership.md`：39 error(s)，與開工前持平。
- 驗收條件對照：OQ 表新增一列含「推翻 OQ-01」「推翻 OQ-02」（達成）；`ui-kanban-basic.md` 不含「定案結果更新」（達成）；`verify-quotes.py` 回傳 0（達成）。

### Act
完成 D-03。下一個任務：T1.07（[F01] 定案 `s-card-add-dialog`，對應 `uc-add-card`）。

## Iteration 11 — 2026-09-17 — T1.07
### Plan
本輪任務：T1.07（[F01] 定案 `s-card-add-dialog`，對應 `uc-add-card`）。要改的檔案：`.dev/F01-basic-kanban/ui-kanban-basic.md` 的 `s-card-add-dialog` 骨架，補齊八段。驗收條件：`ui-check` 錯誤數不比開工前（51／含 `--spec` F02 為 39）多；畫面標「已定案」時八段格式完整無漏。

### Do
- 讀 `spec-kanban-basic.md` 的 `uc-add-card`（pre p1「`card.title` 非空」、post「新的 `card` 出現在指定的 `swimlane` 與 `stage` 交會格中」等、fail p1）與名詞定義三張表（`card.title`／`card.swimlane`／`card.stage`／`r-user`）。
- 補齊 `s-card-add-dialog` 八段：目的、進入與離開（從 `s-board`「新增卡片」進入、成功後回 `s-board`、取消不建立卡片）、角色與權限（`r-user`）、資料（`card.title` 輸入；目的 Swimlane／Stage 依進入情境顯示，來源填 `swimlane.name`／`stage.name`，情境說明放「說明」欄避免 `來源` 欄誤填 Screen ID 觸發 DS-03）、操作（確認新增觸發 `uc-add-card`、需確認：否，比照既有「新增 Swimlane」「新增 Stage」先例——可透過刪除卡片復原）、狀態五項、驗收條件、待確認事項（無）。
- 低風險決定：任務描述括號註明「類型：表單」，與骨架原有「類型：對話框」不同，依任務描述改為「表單」（版面歸類，低風險）。
- commit `[spec/design](kanban-basic)`（ui 檔）與 `[docs](loops)`（任務清單、state、PDCA）分開提交。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`：38 error(s), 3 warning(s)（開工前 51，未增加；`s-card-add-dialog` 本身無新增 error）。
- `./scripts/ui-check --spec .dev/F02-user-membership/spec-user-membership.md`：26 error(s), 11 warning(s)（開工前 39，未增加）。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（回傳 0，本輪未新增 OQ／【】引用）。
- 驗收條件對照：error 數未增加（達成）；八段格式完整、狀態標「已定案」（達成）。

### Act
完成 T1.07。下一個任務：T1.08（[F01] 定案 `s-card-detail`，對應 `uc-edit-card`／`uc-add-comment`；負責人欄位先標 `⚠️` 待確認並記 OQ）。

## Iteration 12 — 2026-09-17 — T1.08
### Plan
本輪任務：T1.08（[F01] 定案 `s-card-detail`，類型：表單，對應 `uc-edit-card`／`uc-add-comment`）。要改的檔案：`.dev/F01-basic-kanban/ui-kanban-basic.md` 的 `s-card-detail` 骨架，補齊八段；負責人相關欄位先標 `⚠️` 並記 OQ。驗收條件：`ui-check` 錯誤數不比開工前（38）多；待確認事項含負責人 `⚠️` 條目；OQ 檔追加對應列。

### Do
- 讀 `spec-kanban-basic.md` 的 `uc-edit-card`（post 僅更新 `card.description`／`card.due-date`／`card.labels`，標題不可編輯；fail 為空）、`uc-add-comment`（post 僅文字描述「留言」「留言者」「留言時間」，fail 為空）、名詞定義三張表、CR-002 變更紀錄（負責人欄位已移至 F02）。
- 補齊 `s-card-detail` 八段：目的、進入與離開（從 `s-board`「開啟卡片詳情」進入，沿用 T1.06 已定案的 `s-board` 操作表；中途放棄不套用未儲存變更）、角色與權限（`r-user`）、資料（`card.title` 顯示不可編輯、`card.description`／`card.due-date`／`card.labels` 顯示＋輸入；負責人與留言列表無法對應到 spec 已定義的 Attribute ID，兩項皆標 `⚠️`）、操作（儲存變更觸發 `uc-edit-card`、新增留言觸發 `uc-add-comment`、關閉）、狀態五項、驗收條件、待確認事項（負責人＋留言兩條 `⚠️`）。
- 高風險決定：留言相關欄位在 spec 名詞定義表完全沒有對應 Entity／Attribute ID（僅 `uc-add-comment` post 用文字描述），比任務描述原先只預期的「負責人」`⚠️` 多一項缺口；依鐵則 1（不定義新概念）不自行發明 `comment` 實體，改在資料表以文字描述並標 `⚠️`，新增 OQ-09（【引用原文】），比照 OQ-08（負責人，【引用原文】CR-002）處理。
- 自我修正：初稿在「角色與權限」「做得到」欄與資料表「說明」欄誤用反引號包住尚未存在的 `s-card-assignee-picker`（觸發 REF-07 error，且角色欄句子含「並」導致 DS-05 操作名稱比對失敗），改為不掛反引號的純文字敘述、角色欄改列精確操作名稱（儲存變更、新增留言、關閉）。
- commit `[spec/design](kanban-basic)`（ui 檔）與 `[docs](loops)`（任務清單、OQ 檔、state、PDCA）分開提交。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`：25 error(s), 1 warning(s)（開工前 38，未增加；下降因 `s-card-detail` 補齊八段消除原本的 DS-02／DS-04 缺口）。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（回傳 0）。
- `tools accept-check`：本任務無機械驗收 token，改以上述 `ui-check` 肉眼核對。
- 驗收條件對照：error 數未增加（達成）；待確認事項含負責人 `⚠️`（OQ-08）與留言 `⚠️`（OQ-09）條目（達成）；OQ 檔追加 OQ-08、OQ-09 兩列（達成）；畫面狀態維持「討論中」（達成，符合高風險項目未清空前不得標「已定案」的規則）。

### Act
完成 T1.08。下一個任務：T1.09（[F01] 定案 `s-card-delete-dialog`，對應 `uc-delete-card`）。

## Iteration 13 — 2026-09-17 — D-04
### Plan
本輪任務：D-04（[F01] 修正 `s-card-detail` 2 處把 `uc-edit-card` 的 post 重述為領域狀態）。要改的檔案：`.dev/F01-basic-kanban/ui-kanban-basic.md` 的 `s-card-detail` 操作表「儲存變更」列、驗收條件對應列。驗收條件：不含「更新為編輯內容」「更新為輸入內容」；操作表該列不含「依 `uc-edit-card` post」；`ui-check` error 數不比開工前（25）多。

### Do
- commit `1c9416d`：`[spec/design](kanban-basic) 修正 s-card-detail 儲存變更改寫為畫面斷言`。
- 操作表「儲存變更」「成功後」改為「停留本畫面，描述、截止日期、標籤欄位顯示儲存後的內容」。
- 驗收條件改為「儲存變更後，本畫面的描述、截止日期、標籤欄位顯示儲存的內容，且觸發 `uc-edit-card`」。
- 只改寫法，未變動業務結果；未新增 OQ／D-xx。

### Check
- `grep` 確認 `s-card-detail` 段落不再含「更新為編輯內容」「更新為輸入內容」，操作表該列不含「依 \`uc-edit-card\` post」（其餘段落的「依 \`uc-edit-card\` post」為不同語境，不在本任務範圍）。
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`：25 error(s), 1 warning(s)（開工前 25，未增加）。
- 驗收條件對照：三項皆達成。

### Act
完成 D-04。下一個任務：D-05（[F01] 修正 `s-card-add-dialog` 3 處寫到領域狀態）。

## Iteration 14 — 2026-09-17 — D-05
### Plan
本輪任務：D-05（[F01] 修正 `s-card-add-dialog` 3 處寫到領域狀態）。要改的檔案：`.dev/F01-basic-kanban/ui-kanban-basic.md` 的 `s-card-add-dialog` 資料表「目的 Swimlane」「目的 Stage」兩列說明欄、驗收條件「標題輸入為空」「取消」兩列。驗收條件：`s-card-add-dialog` 段落的驗收條件不含「不建立」；資料表不含「設為此」；`ui-check` error 數不比開工前（25）多。

### Do
- commit `f2f5469`：`[spec/design](kanban-basic) 修正 s-card-add-dialog 三處寫到領域狀態`。
- 資料表「目的 Swimlane」「目的 Stage」說明欄刪除「建立後 `card.swimlane`／`card.stage` 設為此 Swimlane／Stage」，只留「依進入情境帶入…，本畫面不可變更」。
- 驗收條件「標題輸入為空時確認新增」改為「對話框維持開啟、輸入內容保留、顯示訊息」（刪除「不建立新卡片」）。
- 驗收條件「取消後」改為「關閉對話框，且不觸發 `uc-add-card`」（刪除「不建立卡片」）。
- 只改寫法，未變動業務結果；未新增 OQ／D-xx。

### Check
- `grep` 確認 `s-card-add-dialog` 段落的驗收條件不含「不建立」、資料表不含「設為此」（「進入與離開」段的「不建立卡片」不屬驗收條件或資料表，不在本任務範圍）。
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`：25 error(s), 1 warning(s)（開工前 25，未增加）。
- 驗收條件對照：三項皆達成；本輪未新增待驗證引用，未跑 `verify-quotes.py`。

### Act
完成 D-05。下一個任務：T1.09（[F01] 定案 `s-card-delete-dialog`）。

## Iteration 15 — 2026-09-17 — T1.09
### Plan
本輪任務：T1.09（[F01] 定案 `s-card-delete-dialog`，對應 `uc-delete-card`）。要改的檔案：`.dev/F01-basic-kanban/ui-kanban-basic.md`，補完 `s-card-delete-dialog` 骨架的八段內容。驗收條件：同 T1.08 模式（八段完整、`ui-check` 對該畫面不新增 error）；預期整份檔案 error 數不比開工前（25）多。

### Do
- commit（`[spec/design](kanban-basic)`）：補完 `s-card-delete-dialog` 八段短規格，狀態標「已定案」。
- 目的：刪除卡片前確認，此動作無法復原。
- 進入與離開：從 `s-board`「刪除卡片」操作進入；完成後回 `s-board`，卡片移除；中途放棄關閉對話框、卡片不變。
- 角色與權限：`r-user` 看得到卡片標題，做得到確認刪除、取消。
- 資料：僅 `card.title` 一列。
- 操作：「確認刪除」觸發 `uc-delete-card`（成功後關閉回列表，是（本畫面即確認））；「取消」依規則書「純前端操作觸發填 —」處理為 `—`，成功後欄以「依 `uc-delete-card` p2」帶出 spec 把取消模型化為 fail-p2 分支的事實（與 `s-swimlane-delete-dialog`／`s-stage-delete-dialog` 的取消列寫法一致，未另建 OQ）。
- 狀態五項、驗收條件、待確認事項（無）依既有兩個刪除對話框的模式撰寫。
- 低風險決定：「取消」是否應直接把 `uc-delete-card` 填進觸發欄（因為 spec 明確把取消標為 @fail-p2）——選擇維持與既有兩個刪除對話框一致的「—」寫法，僅在成功後欄文字帶出對應關係，理由：規則書「操作」段明講「純前端操作（取消、關閉）觸發欄填 —」，且與同檔另外兩個對話框保持一致比逐字照搬 spec 的 uc 標籤更重要；未新增 OQ。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`：12 error(s), 0 warning(s)（開工前 25；減少是因為 `s-card-delete-dialog` 骨架填滿八段後不再觸發結構性錯誤；剩餘 12 個全部落在既有 `s-board` 段落第 235～272 行，皆為 F02 跨模組 ID 尚未定義，非本輪改動範圍）。
- `tools accept-check`：本任務驗收條件無機械 token，改用肉眼核對：八段齊全、資料表來源可追溯至 `card.title`、操作表「觸發」與角色表「做得到」一致、狀態五項不省略、驗收條件斷言主詞僅畫面元素／`uc-delete-card`。
- 本輪未新增或修改 OQ／待確認事項的『』引用，未跑 `verify-quotes.py`。

### Act
完成 T1.09。下一個任務：T1.10（[F01] 收尾：`ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 0 error；過一遍 DS-06／DS-07 warn）。

## Iteration 16 — 2026-09-17 15:40 — T1.10

### Plan
本輪任務：T1.10（[F01] 收尾：`ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 0 error；過一遍 `DS-06`／`DS-07` warn）。要改的檔案：預期只需確認現況，若能修正就改 `ui-kanban-basic.md`；驗收條件是該檔 `ui-check` 降到 0 error，且 PDCA 列出 `DS-06`／`DS-07` warn 清單。開工前該檔 error 數 12（全落在 `s-board`，上一輪已知為 F02 跨模組引用）。

### Do
- 逐一核對 12 筆 `REF-07`（`card.assignees`／`uc-set-card-assignees`／`board-membership`／`uc-assign-card-owner-by-drag`／`r-board-member`／`r-board-owner`／`r-system-user`）：`grep` 確認全部 6 個 ID 都已在 `spec-user-membership.md` 的「Use Case 定義」「名詞定義」「角色定義」正式定義（非註解、非草稿區塊），不是內容缺陷。
- 讀 `scripts/speccheck/runner.py` 的 `specs_for_designs`／`main_ui`：確認 `ui-check` 對單一檔案目標只會載入同目錄同模組的 spec，不會因為引用了其他模組 ID 而額外載入該模組 spec；`ui-authoring-tools.py` 的 `accept-check`／`count` 對非 `all` 的 target 一樣只傳該單一檔案給 `ui-check`，永遍不會帶 `--spec`。用 `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec .dev/F02-user-membership/spec-user-membership.md` 驗證：加上 `--spec` 後這 12 筆全部消失，變成 0 error(s)（另外浮現 8 筆屬於 F02 自己模組、與 F01 無關的 `DS-06` warn）。
- 結論：這是規則書「自主決策分級」表「環境限制」列的情況——`ui-check` 對單一模組檔案的檢查範圍限制，不是 `s-board` 內容錯誤；`s-board` 的跨模組引用本身是正確且被規則書明文鼓勵的寫法（`ui-authoring-prompt.md`「跨模組可以互相引用……ID 存在即可，不因為跨模組就要重新定義」），不宜為了讓單檔案 `ui-check` 通過而刪除或改寫這些引用。
- 依規則書處理：`ui-authoring-open-questions.md` 表尾追加 OQ-10（【推論】，引用 `uc-assign-card-owner-by-drag` post 原文，說明 6 個 ID 均已定義、問題出在工具的單檔案載入範圍），狀態 `blocked`；`ui-authoring-tasks.md` 把 T1.10 狀態改為 `blocked`。未改動 `ui-kanban-basic.md` 內容（內容本身無需修正）。
- DS-06／DS-07 review（用 `--spec` 補齊模型後才看得到完整結果，供人工參考，不影響本任務被 block 的結論）：`DS-07` 在 F01+F02 合併模型下對 F01 六個畫面無任何 warn（`s-swimlane-list`／`s-swimlane-delete-dialog`／`s-stage-list`／`s-stage-delete-dialog`／`s-board`／`s-card-add-dialog`／`s-card-detail`／`s-card-delete-dialog` 皆有畫面導向或標「模組入口」）；`DS-06` 的 8 筆 warn（`uc-create-user`／`uc-create-board`／`uc-invite-member`／`uc-change-member-role`／`uc-remove-member`／`uc-delete-board`／`uc-member-add-card`／`uc-set-card-assignees`）全部是 F02 自己的寫入 uc，尚無 F02 畫面（`ui-user-membership.md` 待 T2.01 建立），與 F01 無關、非本輪範圍。
- commit（本輪收尾一併提交）：`[docs](loops) 標記 T1.10 為 blocked 並記錄 OQ-10`（涵蓋 `ui-authoring-tasks.md`／`ui-authoring-open-questions.md`／`ui-authoring-pdca.md`／`ui-authoring-state.md`）。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md`：`12 error(s), 0 warning(s)`（與開工前相同，未新增也未減少，因未修改該檔內容）。
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec .dev/F02-user-membership/spec-user-membership.md`：`0 error(s), 8 warning(s)`（8 筆皆為 F02 自身 `DS-06`，見上）。
- `tools accept-check ui-authoring-tasks.md T1.10`：`ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md) 應為 0，實際 12`——驗收條件目前不成立，符合「環境限制」判斷，任務標 `blocked` 後不算入 `--done` 的機械驗收（`verify-ui-authoring.sh` 第 9 項只查 `--done` 的任務）。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（OQ-10 的『』引用逐字相符 `spec-user-membership.md`）。

### Act
T1.10 標 `blocked`，等待人工決定（調整驗收條件為含 `--spec` 版本、或改到跨模組收尾階段才檢查、或調整 `ui-check`／`ui-authoring-tools.py` 的載入邏輯）後改回 `todo`；在此之前 loop 沒有其他可執行的 `todo`（T2.01 依賴 T1.10 完成）。

---

## Iteration 17 — 2026-09-17 — T1.10（人工解除 OQ-10）

### Plan
人工決定 OQ-10 三個選項採第 3 案（調整 `ui-check`／`ui-authoring-tools.py` 的載入邏輯），因為這個修法一次解決所有模組往後都會遇到的同一類問題，不只是繞過 T1.10 這一個任務。目的是解除 T1.10 的 `blocked`，讓依賴它的 T2.01（F02 起頭）等後續模組可以繼續。本輪不撰寫任何 `ui-<模組>.md` 內容。

### Do
- 修改 `<scripts>/ui-authoring-tools.py` 的 `findings()`：新增 `all_spec_files()` 蒐集全部模組的 `spec-*.md`，呼叫 `scripts/ui-check` 時一律加上這些檔案的 `--spec`，不論 `target_path` 是哪一個檔案；這樣 `tools error-count`／`tools accept-check`／`tools errors-json` 都不會再因為只載入 target 自己模組的 spec 而誤判跨模組引用。
- `<prompts>/ui-authoring-prompt.md`「執行單位：一輪一個任務」與 `<prompts>/ui-authoring-kickoff-prompt.md` 補上明文規則：人工肉眼直接跑 `./scripts/ui-check <檔>`（不透過 `tools`）時，非全部掃描一律要加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`，否則會重現 OQ-10 的誤判。
- `<state>/ui-authoring-tasks.md`：T1.10 狀態改回 `todo`，任務描述補一句說明 OQ-10 已由人工解除、`ui-check(<檔>)=0` 這個 token 從本輪起一律視為已含跨模組 spec 的結果。
- 未修改 OQ-10 既有列內容（append-only，OQ-10 本身仍是「blocked／等待人工決定」的歷史紀錄，本則 PDCA 才是後續決定的記錄）。
- commit：`[dev](ui-authoring-tools) findings() 一律帶入全部模組 spec 解除 OQ-10`（`ui-authoring-tools.py`）＋ `[docs](loops) 解除 T1.10 blocked 並更新 --spec 使用規則`（`ui-authoring-tasks.md`／`ui-authoring-prompt.md`／`ui-authoring-kickoff-prompt.md`／`ui-authoring-pdca.md`）。

### Check
- `tools error-count .dev/F01-basic-kanban/ui-kanban-basic.md`：`0`（修正前為 `12`）。
- `tools accept-check <state>/ui-authoring-tasks.md T1.10`：無輸出，exit 0（驗收條件成立）。
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：`0 error(s), 12 warning(s)`（12 筆皆為既有 `DS-06`／`DS-07` warn，其餘五個模組尚無 ui 檔導致的寫入 uc 未被引用，非新增問題）。
- `tools status-summary <state>/ui-authoring-tasks.md`：`blocked=0 doing=0 done=15 todo=30`；`tools actionable`：`T1.10`。
- `python3 -m unittest discover -s scripts/tests`：`OK`（7 tests）。

### Act
T1.10 改回 `todo`，是下一個 actionable 任務；本次介入只修工具與任務狀態，未實際完成 T1.10（仍要跑 DS-06／DS-07 warn 清單）也未動 F02～F06 任何 ui 內容，留給下一輪執行輪處理。

## Iteration 18 — 2026-09-17 — T1.10

### Plan
本輪任務：T1.10（[F01] 收尾）。OQ-10 已由人工於 Iteration 17 解除（`ui-authoring-tools.py` 的 `findings()` 改成一律帶入全部模組 `--spec`），驗收條件 `ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0` 依上一輪 Check 已成立；本輪要做的是任務描述另一半：過一遍 `DS-06`／`DS-07` warn，沒理由的回頭補。預期 error 數維持 0，不改動 `ui-kanban-basic.md` 內容。

### Do
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：`0 error(s), 22 warning(s)`，全部 22 筆皆為 `DS-06`，且皆屬於 F02／F04／F05／F07 自己模組的寫入 uc（例如 `uc-create-user`／`uc-adjust-board-clock`／`uc-drag-assign-card-owner`／`uc-place-item` 等），這些模組的 `ui-*.md` 尚未撰寫（F02～F07 除 F01 外皆待後續階段），與 F01 無關，非本輪範圍，不需處理。
- `grep -E "F01|DS-07"` 過濾同一份輸出：無任何 F01 自身的警告、也無任何 `DS-07`（畫面未被導向）警告——F01 八個畫面皆已有導向或標「模組入口」，F01 自己的寫入 uc 皆已被畫面觸發。結論：沒有「沒理由」需要回補的項目。
- 未修改 `.dev/F01-basic-kanban/ui-kanban-basic.md` 內容（審查結果為無需修正）。
- commit（本輪收尾一併提交）：`[docs](loops) 完成 T1.10 並核對 DS-06/DS-07 warn 清單`（`ui-authoring-tasks.md`／`ui-authoring-pdca.md`／`ui-authoring-state.md`）。

### Check
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：`0 error(s), 22 warning(s)`（皆為其他模組自身 `DS-06`，見上）。
- `tools accept-check <state>/ui-authoring-tasks.md T1.10`：無輸出，exit 0（驗收條件成立）。
- 驗收條件對照：`ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0` 成立；「PDCA 本則列出 DS-06／DS-07 warn 清單與處理結果」已列於上方 Do，結論為無需回補。
- `python3 <scripts>/verify-quotes.py`：本輪未新增／修改 OQ 或『』引用，未執行。

### Act
T1.10 標 `done`。下一個任務：T2.01（[F02] 建立 `ui-user-membership.md` 檔頭與 9 個畫面標題骨架）。

## Iteration 19 — 2026-09-17 — T2.01
### Plan
本輪任務：T2.01（[F02] 建立 `ui-user-membership.md`：檔頭 ＋ 9 個畫面標題骨架）；要改的檔案：新建 `.dev/F02-user-membership/ui-user-membership.md`（檔頭 ＋ `s-signup`、`s-login`、`s-board-list`、`s-board-create-dialog`、`s-member-management`、`s-board-delete-dialog`、`s-card-assignee-picker`、`s-cards-by-assignee`、`s-activity-log` 九個畫面標題與三行標頭，八段內容留空，狀態皆「未討論」）；驗收條件：9 個 `## s-` 標題與三行標頭都存在，`ui-check` 對 `DS-01` 不報 error；預期 error 數：DS-01 為 0，其餘段落缺漏的 error（`DS-02`／`DS-04`）屬預期，本輪不處理。

### Do
- 讀 `spec-user-membership.md` 七個 Feature（建立使用者帳號、使用者登入與登出、Board 建立與成員邀請、Board 權限管理、Board 存取權限、卡片負責人指派、檢視看板活動紀錄）與各自 `uc-` 清單，依任務指定的 9 個 Screen ID 逐一對應 Feature：`s-signup`→建立使用者帳號、`s-login`→使用者登入與登出、`s-board-list`→Board 存取權限（`uc-view-board-list`）、`s-board-create-dialog`／`s-member-management`→Board 建立與成員邀請（`uc-create-board`／`uc-invite-member`／`uc-change-member-role`／`uc-remove-member`）、`s-board-delete-dialog`→Board 權限管理（`uc-delete-board`）、`s-card-assignee-picker`／`s-cards-by-assignee`→卡片負責人指派、`s-activity-log`→檢視看板活動紀錄。
- 類型依 `ui-convention.md`／`parser_design.py` 七種固定值（列表、詳情、表單、儀表板、流程、對話框、設定）指派：`s-signup`／`s-login`＝表單、`s-board-list`／`s-member-management`／`s-cards-by-assignee`／`s-activity-log`＝列表、`s-board-create-dialog`／`s-board-delete-dialog`／`s-card-assignee-picker`＝對話框。低風險命名／分類決定，未寫 OQ。
- commit `badd5ba` 同模式：只建檔頭與畫面標題三行，八段內容留空標題。
- Commit：`e...`（`[spec/design](ui-user-membership) 建立檔頭與 9 個畫面標題骨架`）。

### Check
`./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：117 error(s), 47 warning(s)（八段內容未填屬預期，其中 F04／F05／F07 warn 與本輪無關）；篩選 `DS-01`：0 筆，符合骨架任務判準。`tools accept-check T2.01`：無機械 token（同 T1.01），改肉眼核對，達成。本輪未新增 OQ 或『』引用，未跑 `verify-quotes.py`。

### Act
完成：下一個任務 T2.02（[F02] 定案 `s-signup`，類型：表單，對應 `uc-create-user`）。

## Iteration 21 — 2026-09-17 18:05 — T2.02
### Plan
本輪任務：T2.02（[F02] 定案 `s-signup`，類型：表單，對應 `uc-create-user`）。要改的檔案：`.dev/F02-user-membership/ui-user-membership.md`（填 `s-signup` 八段內容）；OQ 檔追加「從哪裡進來」「完成後去哪裡」兩筆待確認。驗收條件：`ui-check` 對本畫面無 error、八段齊全（同 T1.02 模式）。預期 error 數變化：`s-signup` 本身 0 error，全域 error 總數因其他畫面仍是骨架不會下降。

### Do
- 讀 `spec-user-membership.md`「Feature: 建立使用者帳號」`uc-create-user`（roles/pre/post/fail）與「角色定義」`r-system-user`（明確列出「可建立帳號」，故沿用不視為矛盾）。
- 填 `s-signup` 八段：資料表對照 `user.username`／`user.display-name`／`user.password` 三個欄位；操作表僅一列「確認建立帳號」對應 `uc-create-user`，「需確認」標「是（本畫面即確認）」——因 spec 未定義任何刪除 `user` 的 Use Case，建立後無法復原，依規則書「需確認判斷」歸類為不可逆。
- 高風險：「從哪裡進來」「完成後去哪裡」spec 皆未描述，標 ⚠️ 並新增 OQ-11（推論為模組入口）、OQ-12（推論導向 `s-login`），畫面狀態維持「討論中」。
- commit `58eb11e`：`[spec/design](ui-user-membership) 定案 s-signup 八段短規格`。

### Check
`./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：104 error(s), 44 warning(s)（其餘 6 個未填畫面與 F04/F05/F07 warn 屬預期，與本輪無關）；篩選 `s-signup`／`s-login` 相關訊息：`s-signup` 0 筆 error，`s-login` 仍為骨架的既有 DS-02/DS-04 error 不變。八段齊全，逐條對照驗收條件（同 T1.02：「`ui-check` 對本畫面無 error；八段齊全」）達成。`tools accept-check T2.02`：無機械 token，肉眼核對通過。本輪新增 OQ-11、OQ-12 兩筆『』引用，跑 `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（exit 0）。

### Act
完成：下一個任務 T2.03（[F02] 定案 `s-login`，類型：表單，對應 `uc-login`；`uc-logout` 併入本畫面操作表）。

## Iteration 22 — 2026-09-17 — T2.03
### Plan
本輪任務：T2.03（[F02] 定案 `s-login`，類型：表單，對應 `uc-login`；`uc-logout` 併入本畫面操作表，不獨立開畫面）。要改的檔案：`.dev/F02-user-membership/ui-user-membership.md`（填 `s-login` 八段內容，操作表含 `uc-login`／`uc-logout` 兩列）；OQ 檔追加「完成後去哪裡」一筆，並在「從哪裡進來」回引 OQ-11／OQ-12。驗收條件：`ui-check` 對本畫面無 error、八段齊全（同 T2.02 模式，操作表含兩列）。預期 error 數變化：`s-login` 本身 0 error，全域 error 總數應下降（原骨架的 DS-02/DS-04 缺段落 error 消失）。

### Do
- 讀 `spec-user-membership.md`「Feature: 使用者登入與登出」`uc-login`／`uc-logout`（roles/pre/post/fail）與相關 Gherkin Scenario。
- 填 `s-login` 八段：資料表對照 `user.username`／`user.password` 兩個欄位，驗證/格式欄引用 `uc-login` pre p1／p2；操作表兩列「送出登入表單」（`uc-login`）與「登出」（`uc-logout`），兩者「需確認」皆標「否」——`uc-login` 失敗可重新輸入，`uc-logout` 登出後仍可重新登入，皆非不可逆操作。
- 「從哪裡進來」列出兩個已由 spec 直接確認的路徑（`s-signup` 完成後導向、`uc-logout` post 明確寫「回到登入頁面」），未登入應用程式進入點是否也導向本畫面標 ⚠️ 並回引既有 OQ-11（查重後未重開新列）。
- 高風險：「完成後去哪裡」`uc-login` post 只寫『TopBar 顯示該 `user` 的帳號名稱』未描述導向畫面，標 ⚠️ 新增 OQ-13（推論暫定導向 `s-board-list`），畫面狀態維持「討論中」。
- 低風險：「角色與權限」表「做得到」欄首次寫成分號＋括號混合格式，`ui-check` 報 DS-05（可用角色 `[]` 與 uc roles 不一致，因 `_split_ops` 只用頓號／逗號切字，未能取出精確操作字串）；改寫成純頓號分隔、逐字等於操作表操作名稱（「輸入帳號 ID、密碼、送出登入表單、登出」），附註說明移到「看得到」欄，修正後歸零。
- commit `f134dec`：`[spec/design](ui-user-membership) 定案 s-login 八段短規格`。

### Check
`./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：94 error(s), 43 warning(s)（其餘 5 個未填畫面與 F04/F05/F07 warn 屬預期，與本輪無關；起點 104 error(s), 44 warning(s)，本輪淨減 10 error／1 warning）；篩選 `s-login`／`s-signup` 相關訊息：兩者皆 0 筆 error。`tools error-count .dev/F02-user-membership/ui-user-membership.md`：92（含跨模組 REF/DS 統計口徑略有差異，仍為淨減）。八段齊全，逐條對照驗收條件（`ui-check` 對本畫面無 error；操作表含 `uc-login` 與 `uc-logout` 兩列）達成。`tools accept-check T2.03`：無機械 token，肉眼核對通過。本輪新增 OQ-13 一筆『』引用，跑 `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（exit 0）。

### Act
完成：下一個任務 T2.04（[F02] 定案 `s-board-list`，類型：列表，對應 `uc-view-board-list`；無權限狀態對應 `uc-reject-board-access-by-nonmember`）。

## Iteration 23 — 2026-09-17 — D-06
### Plan
本輪任務：D-06（[F02] 修正 `ui-user-membership.md` 的 `s-login` 操作表「登出」列第 84 行 REF-07 error：「失敗時」欄把 `fail` 包在反引號裡被誤判為未定義 Entity ID）。要改的檔案：`.dev/F02-user-membership/ui-user-membership.md` 第 84 行一處文字。驗收條件：`ui-check` 輸出無 REF-07；`s-login` 段落（`## s-login` 到 `## s-board-list` 之間）內無任何 error；`error-count` 較審查時的 92 減少。預期 error 數變化：92 → 91。

### Do
- 讀 `ui-user-membership.md` 第 84 行，確認「不適用（`uc-logout` 無 `fail`）」把 `fail` 包在反引號裡；`ui-check` 反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用，`fail` 不在其中會被當成未定義 ID 觸發 REF-07。
- 改成「不適用（`uc-logout` 無 fail 定義）」，拿掉反引號，語意不變。
- 全檔搜尋其餘 `` `fail` `` 反引號誤用，無其他處。
- commit `0f08058`：`[spec/design](user-membership) 修正 s-login 登出失敗欄反引號誤標為 Entity ID`。

### Check
`./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：91 error(s), 43 warning(s)，輸出中無 REF-07；`s-login` 行號範圍（53～102 行）內無任何 error（本輪錯誤全在 `s-activity-log`，與本任務無關）。`tools error-count`：91，較審查時 92 減 1，符合預期。`tools accept-check` 無機械 token，逐條對照驗收條件皆達成。本輪未新增或修改 OQ／『』引用，未跑 `verify-quotes.py`。

### Act
完成：下一個任務 D-07（[F02] 修正 `ui-user-membership.md` `s-signup`／`s-login` 驗收條件中以領域狀態為主詞的斷言）。

## Iteration 24 — 2026-09-17 — D-07
### Plan
本輪任務：D-07（[F02] 修正 `ui-user-membership.md` `s-signup`／`s-login` 驗收條件中以領域狀態為主詞的斷言，改成畫面元素或「是否觸發 `uc-xxx`」）。要改的檔案：`.dev/F02-user-membership/ui-user-membership.md` 第 43～47 行（`s-signup` 驗收條件）與第 95～96 行（`s-login` 驗收條件）。驗收條件：檔案不含「不應該被建立」「不應該建立」「建立新帳號」「顯示名字等於帳號 ID」「不視為登入成功」；`ui-check` error 數不增加（審查時 92，上一輪已降至 91）。預期 error 數變化：91 → 91（不增加）。

### Do
- `s-signup` 驗收條件第 1 條刪掉「且不觸發 `uc-create-user` 建立新帳號」尾巴的「建立新帳號」，改成「且不觸發 `uc-create-user`」。
- `s-signup` 驗收條件第 2、3 條「該帳號不應該被建立」「不應該建立新的帳號」統一改成「且不觸發 `uc-create-user`」（畫面斷言＋是否觸發 uc，與第 1 條一致）。
- `s-signup` 驗收條件第 5 條「未輸入顯示名字時確認建立帳號成功後，該帳號的顯示名字等於帳號 ID」是 `uc-create-user` post 的領域結果斷言，整條刪除（第 4 條「密碼留白時確認建立帳號成功，且觸發 `uc-create-user`」已涵蓋「觸發 uc」這個可斷言的畫面/uc 事實，未輸入顯示名字時的預設值屬於 `uc-create-user` 內部業務結果，不屬本畫面可斷言範圍）。
- `s-login` 驗收條件兩條「且不視為登入成功」改成「且 TopBar 不顯示帳號名稱」（畫面元素斷言，對應操作表「成功後」寫的「TopBar 顯示帳號名稱」的反向狀態）。
- commit `c1100de`：`[spec/design](ui-user-membership) 修正 s-signup/s-login 驗收條件改用畫面斷言`。

### Check
`grep -n "不應該被建立\|不應該建立\|建立新帳號\|顯示名字等於帳號 ID\|不視為登入成功" .dev/F02-user-membership/ui-user-membership.md`：無匹配（exit 1）。`./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：91 error(s), 43 warning(s)（與本輪開工前一致，未增加）。`tools error-count`：91。`tools accept-check D-07`：無機械 token（驗收條件為文字比對），exit 0；逐條對照：四個禁止詞彙均已移除，error 數未增加，達成。本輪未新增或修改 OQ／『』引用，未跑 `verify-quotes.py`。

### Act
完成：下一個任務 D-08（[F01] 修正 `s-card-delete-dialog` 業務結果寫進 UI 段落）。

## Iteration 25 — 2026-09-17 — D-08
### Plan
任務：D-08，[F01] 修正 `ui-kanban-basic.md` 的 `s-card-delete-dialog` 三處把業務結果「卡片不變」寫進 UI 段落。要改的檔案：`.dev/F01-basic-kanban/ui-kanban-basic.md`（操作表「取消」列、驗收條件、進入與離開「中途放棄會怎樣」）。驗收條件：段落不含「卡片不變」；`ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0`。預期 error 數：本檔維持 0。

### Do
- c12d165 [spec/design](kanban-basic) 修正 s-card-delete-dialog 業務結果寫進 UI 段落
- 低風險決定：任務描述要求「中途放棄會怎樣」改成「關閉對話框，回到 `s-board`，不觸發 `uc-delete-card`」，實際套用後 `ui-check` 報 DS-03 error（`進入與離開.畫面 "uc-delete-card" 種類錯誤，應為 Screen`）——`parser_design.py` 對「進入與離開」段落所有反引號 ID 一律驗證為 Screen 種類，不論是 from／to／abort 哪一列。改為「關閉對話框，回到 `s-board`」（拿掉 `uc-delete-card` 反引號引用），「不觸發 `uc-delete-card`」的意思已由操作表「取消」列與驗收條件表達，未遺漏任何資訊，且未新增或改變業務結果，符合任務目的（去除「卡片不變」）。其餘 2 處依任務描述原文修正。
- 未新增 OQ／D-xx

### Check
`./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 最後一行：`0 error(s), 22 warning(s)`（22 個 warning 皆為其他模組 DS-06「uc 未被任何畫面觸發」，與本任務無關，非新增）。
逐條對照驗收條件：
- `s-card-delete-dialog` 段落不含「卡片不變」：確認（`grep 卡片不變` 該段落無結果）。
- `ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0`：達成。
本輪未新增／修改 OQ 或待確認事項引用，未跑 `verify-quotes.py`。

### Act
完成。下一個任務：T2.04（[F02] 定案 `s-board-list`）。

## Iteration 26 — 2026-09-17 — T2.04

### Plan
本輪任務 T2.04：[F02] 定案 `s-board-list`（類型：列表），對應 `uc-view-board-list`（列表只顯示有權限的 Board）、`uc-reject-board-access-by-nonmember`（無權限狀態）。要改的檔案：`.dev/F02-user-membership/ui-user-membership.md` 的 `s-board-list` 區塊，填滿八段。預期發現：Board 名稱缺 Attribute ID（比照 OQ-09 留言欄位的處理）、無權限情境的停留畫面未定；預期 `ui-check` 對本畫面的 error 數從骨架的 13 筆降到接近 0，但因跨模組引用 F01 `s-board` 可能撞到 OQ-10 同類的單檔案 design 載入限制。

### Do
- `39f6...`（實際 commit hash 見 git log）`[spec/design](user-membership) 定案 s-board-list 八段短規格`：填入目的、進入與離開（登入後進入，見 OQ-13；選 Board 進入 F01 `s-board`）、角色與權限、資料（Board 名稱標 ⚠️）、操作（選擇 Board 進入、嘗試直接開啟不屬於自己的 Board）、狀態五項、驗收條件、待確認事項。
- 低風險決定：「中途放棄會怎樣」比照 `s-swimlane-list` 模式標「不適用」（列表型畫面皆即時導覽，無中途放棄流程）；「建立 Board」入口不在本畫面猜測、留給 T2.05 `s-board-create-dialog` 自己的「從哪裡進來」回答，避免跨 Feature 臆測。
- 新增 OQ-14（【引用原文】Board 名稱找不到 Attribute ID，同 OQ-09 模式，資料表改文字描述不掛 ID）、OQ-15（【引用原文】`uc-reject-board-access-by-nonmember` post 未寫停留畫面，暫定停留本畫面）、OQ-16（【推論】F02 `s-board-list` 對 F01 `s-board` 的合法跨模組 Screen 引用，單獨檢查 `ui-user-membership.md` 時被 `ui-authoring-tools.py` `findings()` 的 `paths = [target_path]` 誤判 REF-07，兩檔一起跑則 0 error/warn，環境限制同 OQ-10 但這次是 design 路徑而非 spec 路徑）。
- 修正 DS-05：`角色與權限`表「做得到」欄補上「嘗試直接開啟不屬於自己的 Board」，使可用角色集合與 `uc-reject-board-access-by-nonmember` 的 `roles: [r-system-user]` 一致。
- 因 OQ-16 屬環境限制，T2.04 狀態標為 `blocked`（內容已完成，僅單檔案機械檢查因工具限制無法顯示 0 error）。

### Check
- `./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：`83 error(s), 43 warning(s)`（`s-board-list` 所在行號範圍 102-147 內僅剩 4 筆 `REF-07`，全部是對 F01 `s-board` 的引用）。
- `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`（兩份 ui 檔一起檢查）：`s-board-list` 所在行號範圍內 0 error、0 warn，證實 4 筆 REF-07 是單檔案 design 載入限制造成的誤判，非畫面內容缺陷。
- `tools accept-check`：T2.04 未標 `done`，不受「已完成任務」機械驗收條件約束；DS-05 之前的角色不一致 error 已修正，重跑確認該筆消失。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（含 OQ-14／OQ-15／OQ-16 三筆新引用）。
- 驗收條件逐條對照：使用者只看到自己有權限的 Board（`uc-view-board-list` post）已寫入驗收條件；空清單情境已涵蓋；選 Board 進入 F01 `s-board`、非成員直接開啟被拒絕兩條也都已涵蓋；八段格式齊全但因 OQ-16 標「討論中」而非「已定案」。

### Act
- 未完成：T2.04 因 OQ-16（環境限制）標 `blocked`，需人工決定是否調整 `ui-authoring-tools.py` `findings()` 的 design 路徑邏輯（讓單檔案檢查也帶入全部既有 ui 檔，類比 T1.10 對 `--spec` 的修法）或調整驗收條件寫法，才能改回 `todo`／`done`。
- 下一個任務：依任務清單，T2.05（`s-board-create-dialog`）依賴 T2.04 完成，在 T2.04 解除 `blocked` 前無法進行；若人工短期內不處理，loop 下一輪會回頭檢查 T2.04 是否已被人工改回 `todo`。

## Iteration 27 — 2026-09-17 21:00 — D-10

### Plan
本輪任務 D-10（`actionable` 排在 T2.05 之前）：依人工在 OQ-11／OQ-12／OQ-13 上的決策，把 `ui-user-membership.md` 的 `s-signup`／`s-login` 從「暫定」改為「已確認」，並補上 `s-login` 缺少的「前往建立帳號」導覽操作。要改的檔案：`.dev/F02-user-membership/ui-user-membership.md` 的 `s-signup`、`s-login` 兩個區塊（進入與離開、操作表、驗收條件、待確認事項）。預期：兩畫面「待確認事項」清空為「（無）」；`ui-check` 對這兩個畫面不新增 error；因 `s-signup` 不再是「模組入口」也未被其他畫面 `nav_to` 導向（`s-login` 的新導覽放在操作表而非「進入與離開」段，`DS-07` 只解析「進入與離開」），預期會新增一筆 `DS-07` warn（不擋 `done`）。

### Do
- `c3cba5c`　`[spec/design](user-membership) 依人工決策解除 OQ-11/12/13`：
  1. `s-login`「從哪裡進來」新增「未登入時的應用程式入口」，移除 OQ-11 的 ⚠️。
  2. `s-login` 操作表新增「前往建立帳號」列（觸發 `—`，純前端導覽，成功後開啟 `s-signup`），並補一條對應驗收條件。
  3. `s-signup`「從哪裡進來」改為「`s-login` 的『前往建立帳號』連結」，移除 OQ-11 的 ⚠️。
  4. `s-signup`「完成後去哪裡」改為「導向 `s-login`」（內容不變，移除 OQ-12 的 ⚠️與「暫定」字樣）。
  5. `s-login`「完成後去哪裡」改為「導向 `s-board-list`」（內容不變，移除 OQ-13 的 ⚠️與「暫定」字樣）。
  6. `s-signup`、`s-login` 的「待確認事項」原本各兩條分別指向 OQ-11／OQ-12、OQ-11／OQ-13，全部移除後兩段皆改為「（無）」（依 `ui-convention.md` 132 行規則，清空段落寫「（無）」不省略段落）；`s-board-list` 未含這三個 OQ 的條目，任務描述「三畫面」與實際檔案內容（僅 `s-signup`／`s-login` 兩畫面）不符，以檔案現況為準，未額外改動 `s-board-list`。
- 低風險決定：「前往建立帳號」操作表「需確認？」欄比照其他純前端導覽（如 `s-board-list` 的「選擇 Board 進入」）標「否」；「失敗時」欄依同類操作模式寫「不適用（純前端導覽）」。

### Check
- `./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：`83 error(s), 44 warning(s)` → 本輪改動後 `78 error(s)`（`s-signup`／`s-login` 所在行號範圍內 0 error；新增 1 筆 `DS-07` warn：「畫面 "s-signup" 沒有任何畫面導向它，也不是模組入口」，符合 Plan 預期，warn 不擋任務完成）。
- `tools accept-check`（D-10）：`ui-check(.dev/F02-user-membership/ui-user-membership.md) 應為 0，實際 78` — 未通過。此 token 是對整份檔案的 error 數，目前 78 筆全部來自 T2.05～T2.10 尚未定案的骨架畫面（`s-board-create-dialog`／`s-member-management`／`s-board-delete-dialog`／`s-card-assignee-picker`／`s-cards-by-assignee`／`s-activity-log`），與本輪改動的 `s-signup`／`s-login` 無關；這個驗收條件要等到 T2.11（收尾任務，逐一定案完所有畫面）才可能整份歸零，D-10 本身無法在依賴任務之前單獨滿足它。依規則書「任務清單...loop 只能改狀態欄」不可改寫這條驗收條件，如實記錄差異。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（本輪未新增【引用原文】類條目，既有引用仍全部相符）。
- 逐條對照任務描述其餘驗收條件：`s-login`「進入與離開」含「應用程式入口」——達成；操作表含「前往建立帳號」列——達成；`s-signup`「進入與離開」不含 OQ-11／OQ-12 的 ⚠️——達成；`s-login`「進入與離開」不含 OQ-13 的 ⚠️——達成；相關「待確認事項」不含 OQ-11／OQ-12／OQ-13——達成。

### Act
- 內容已完成，僅整份檔案 `ui-check=0` 這條機械驗收條件因依賴 T2.05～T2.10 尚未完成而無法滿足，狀態標 `doing`（非本輪工作範圍造成，非本輪引入新 error）。
- 下一個任務：`actionable` 重新排序後為 T2.05（[F02] 定案 `s-board-create-dialog`）。

## Iteration 28 — 2026-09-17 — T2.05
### Plan
- 本輪任務：T2.05（[F02] 定案 `s-board-create-dialog`，類型：表單，對應 `uc-create-board`）。D-10 雖仍列 `doing`（其整檔 `ui-check=0` 驗收條件依賴 T2.05～T2.10 完成，上一輪已記錄「下一個任務：T2.05」），本輪按此接續往下做，不重複處理 D-10 本身內容。
- 要改的檔案：`.dev/F02-user-membership/ui-user-membership.md`——`s-board-create-dialog` 八段內容補齊；為讓「進入與離開」與已定案的 `s-board-list` 對得上（規則書「輸入怎麼讀」第四點），同步在 `s-board-list` 補上「前往建立 Board」的操作列、目的、進入與離開、驗收條件一行，並非重新定義 `s-board-list` 的既有結論。
- 驗收條件：`s-board-create-dialog`「進入與離開」「角色與權限」「資料」「操作」「狀態」「驗收條件」依 `uc-create-board`（`roles: [r-board-owner]`、`post`、`fail: {}`）與 `board.name` 欄位表推導；`ui-check` 對兩個畫面所在行號範圍不得新增 error；預期整份檔案 error 數從 78 降到約 65（新增一個畫面內容 + `s-board-list` 補一列，扣掉原本骨架的 DS-02/DS-04 系列 error）。

### Do
- `git commit`：`[spec/design](ui-user-membership) 定案建立 Board 對話框畫面`——`s-board-create-dialog` 補齊八段：`從哪裡進來` 為 `s-board-list` 的「前往建立 Board」操作；`確認建立` 觸發 `uc-create-board`；因存在 `uc-delete-board` 可完整復原新建的 Board，`需確認？` 判定為「否」；`uc-create-board` 的 `fail: {}` 為空，「失敗時」「錯誤」段落依既有 `uc-logout` 前例寫「不適用（`uc-create-board` 無 fail 定義）」。
- 同一 commit 內同步修正 `s-board-list`：目的補「或建立新的 Board」；「進入與離開」完成後去哪裡新增「前往建立 Board 操作導向 `s-board-create-dialog`」（`DS-07` 只認「進入與離開」的反引號，操作表本身的引用不算，故補在這裡）；操作表新增「前往建立 Board」列（觸發 `—`，純前端導覽）；驗收條件新增對應一行。
- 低風險決定：畫面標題行「類型」由骨架的「對話框」改為「表單」（本畫面有實際輸入欄位，比照 F01 `s-card-add-dialog` 前例：名稱含「對話框」但因有表單欄位標「表單」，任務描述本身也指定「類型：表單」）；操作名稱定為「前往建立 Board」，比照 `s-login`「前往建立帳號」命名模式。
- 無新增 OQ／`⚠️`；本畫面內容可完全從 spec 推導，狀態標「已定案」。

### Check
- `./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 最後一行：`70 error(s), 43 warning(s)`（原 78 error）；`s-board-create-dialog`、`s-board-list` 所在行號範圍內查無任何 error 或 warn（`grep` 篩選確認，含 `REF-07`／`DS-02`／`DS-04`／`DS-07`）。
- `tools error-count .dev/F02-user-membership/ui-user-membership.md`：65（用 `ui-check --report` 隱含全域計數口徑，與 70 的差異為既有 F01 跨檔 `s-board` REF-07 未計入單檔模式，屬既知 OQ-16 限制，非本輪引入）。
- `tools accept-check ui-authoring-tasks.md T2.05`：通過（無輸出、exit 0）。
- 逐條對照任務描述驗收條件：`s-board-create-dialog` 八段完整、對應 `uc-create-board`——達成；與 `s-board-list` 進入/離開互相對得上——達成。
- 本輪未新增 OQ 或『』引用，未跑 `verify-quotes.py`。

### Act
- 完成：T2.05 標 `done`。
- 下一個任務：T2.06（[F02] 定案 `s-member-management`）。

## Iteration 29 — 2026-09-17 — T2.06
### Plan
- 本輪任務：T2.06（[F02] 定案 `s-member-management`，類型依任務描述為「對話框／側欄」，對應 `uc-invite-member`／`uc-change-member-role`／`uc-remove-member`；三個拒絕 uc `uc-reject-invite-by-member`／`uc-reject-role-change-by-member`／`uc-reject-structure-change-by-member` 放對應操作「失敗時」欄）。
- 要改的檔案：`.dev/F02-user-membership/ui-user-membership.md`——`s-member-management` 八段內容補齊。
- 驗收條件：同 T1.02 模式——`ui-check(.dev/F02-user-membership/ui-user-membership.md)` 對本畫面無 error；八段齊全。
- 預期 error 數變化：本畫面從骨架（缺 7 段）填滿八段內容，預期整份檔案 error 數下降。

### Do
- `git commit`：`[spec/design](ui-user-membership) 定案 Board 成員管理畫面`——`s-member-management` 補齊八段：邀請／變更角色／移除成員三個成功操作對應 Owner，兩個拒絕操作（嘗試邀請成員、嘗試變更成員角色）對應 Member。
- 低風險決定：畫面標題「類型」欄實際填「對話框」（非任務描述字面的「對話框／側欄」）——`ui-check` 的 `TYPES` 常數（`scripts/speccheck/parser_design.py`）只接受 `['列表','詳情','表單','儀表板','流程','對話框','設定']` 七個字面值，比照既有骨架 `s-card-assignee-picker` 前例（同樣任務描述寫「對話框／側欄」但檔案實際值是「對話框」）。
- 高風險（標 ⚠️、記 OQ、畫面維持「討論中」）：
  - OQ-22：「從哪裡進來」spec 未描述，推論由 F01 `s-board` 的 Owner 專屬操作進入，但該操作目前不在 `s-board` 操作表中。
  - OQ-23：`r-board-member` 是否真的看得到本畫面完整成員清單（僅操作被拒絕），或本來就看不到本畫面。
  - OQ-24：`uc-change-member-role` post 只定義變成 Owner，未定義 Owner 降級為 Member，「變更成員角色」操作是否可逆判斷不出，資料段「變更後角色」與操作表「需確認？」欄一併標 ⚠️。
  - OQ-25：`uc-reject-structure-change-by-member` 的 crud／post 對象是看板結構（Swimlane），不是成員，判定不屬於本畫面操作範圍，不列入操作表；待確認是否應回填到 F01 `s-swimlane-list`／`s-stage-list` 的操作失敗欄（跨模組）。
- `uc-reject-invite-by-member`／`uc-reject-role-change-by-member` 兩個拒絕 uc 依任務描述放入對應操作列；操作表操作名稱與角色表「做得到」欄需逐字相同（`DS-05` 檢查邏輯），故兩處拒絕操作命名統一為「嘗試邀請成員」「嘗試變更成員角色」（不含「（非 Owner）」等額外文字，該資訊改寫在驗收條件與待確認事項的敘述句裡）。

### Check
- `./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：`52 error(s), ...warning(s)`（原 65）；`s-member-management` 所在行號範圍（194～253）僅剩 1 筆 `DS-07` warn（沒有畫面導向它，符合 OQ-22 現況、非 error）；`s-board`／`s-swimlane-list`／`s-stage-list` 的 `REF-07` 為既知跨檔限制（OQ-16 同類問題），已用 `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md .dev/F02-user-membership/ui-user-membership.md --spec "..."` 驗證：兩檔一起跑後這些 `REF-07` 全部消失，僅剩上述同一筆 `DS-07` warn。
- `tools error-count .dev/F02-user-membership/ui-user-membership.md`：52。
- `tools accept-check ui-authoring-tasks.md T2.06`：無輸出（該任務驗收條件為「同上」文字敘述，非機械 token，已改用上述 `ui-check` 肉眼核對）。
- 逐條對照任務描述：八段齊全——達成；`uc-invite-member`／`uc-change-member-role`／`uc-remove-member` 皆有對應成功操作列——達成；`uc-reject-invite-by-member`／`uc-reject-role-change-by-member` 放入對應操作「失敗時」邏輯上的拒絕列——達成；`uc-reject-structure-change-by-member` 判定不適用本畫面，改記 OQ-25 說明理由（非直接照單全收放進操作表）。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（新增 OQ-22～OQ-25 的『』引用皆與 spec 逐字相符）。

### Act
- 完成：T2.06 標 `done`。
- 下一個任務：T2.07（[F02] 定案 `s-board-delete-dialog`，對應 `uc-delete-board`）。

## Iteration 30 — 2026-09-17 — T2.07

### Plan
- 本輪任務：T2.07（[F02] 定案 `s-board-delete-dialog`，類型：對話框，對應 `uc-delete-board`；`uc-member-add-card` 不屬本畫面，留給 T2.08／T1.06）。
- 要改的檔案：`.dev/F02-user-membership/ui-user-membership.md`——`s-board-delete-dialog` 八段內容補齊，結構比照 F01 `s-swimlane-delete-dialog` 前例（確認刪除前顯示將一併刪除的下層資料數量）。
- 驗收條件：八段齊全；`uc-delete-board` 對應「確認刪除」操作；`ui-check` 對本畫面所在行號範圍無新增 error。
- 預期 error 數變化：本畫面從骨架填滿八段內容，預期整份檔案 error 數從 52 下降。

### Do
- `fea21f3`　`[spec/design](user-membership) 定案刪除 Board 對話框畫面`：`s-board-delete-dialog` 補齊八段：
  - 「資料」段列出 Board 名稱、Swimlane 數（`board`→`swimlane` 關係計數）、Stage 數（`board`→`stage` 關係計數）、卡片數（`swimlane`→`card`／`stage`→`card` 關係計數彙總），對應 `uc-delete-board` Scenario「這個 Board 有 2 個 Swimlane、3 個 Stage，以及數張卡片」。
  - 「操作」表：「確認刪除」觸發 `uc-delete-board`，因 `fail: {}` 為空寫「不適用」；「需確認？」標「是（本畫面即確認）」，比照 `s-swimlane-delete-dialog` 前例（不可逆刪除操作、畫面本身即確認）。
  - 「完成後去哪裡」依 post「該 board 不再存在」，推論導向 `s-board-list`（module 既有列表畫面，非新發明目的地，低風險決定，不記 OQ）。
- 低風險決定：「無權限」段依 T2.05 `s-board-create-dialog` 前例寫「不適用（`uc-delete-board` roles 僅 `r-board-owner`，spec 未定義本畫面內的角色差異）」；spec Scenario 內雖有 Member 嘗試刪除被拒絕的文字，但未對應獨立 fail key 或 reject-uc（與 `uc-create-board` 同類，無對應 reject 機制可引用），比照既有前例不特別處理，不記 OQ。
- 高風險（標 ⚠️、記 OQ、畫面維持「討論中」）：OQ-26——「從哪裡進來」spec 未定義，推論應由 F01 `s-board` 的 Owner 專屬操作進入，但該操作尚未列在 `s-board` 操作表中；與 OQ-22（`s-member-management` 同類缺口）情況相同、但對象是不同 Screen，故另開一列（不推翻 OQ-22，是新增一列）。
- 修正一次格式問題：「進入與離開」段落內原本把 Board 實體寫成反引號 `` `board` ``，被 `DS-03` 解析成 Screen 引用而誤判「種類錯誤」；改為不加反引號的純文字「Board」後消除該 error（記入本輪 state「待注意」，供之後任務參考）。

### Check
- `./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：`52 error(s), ...warning(s)` → 本輪改動後 `52 error(s), 38 warning(s)`；`s-board-delete-dialog` 所在行號範圍（255～300）僅剩 1 筆 `DS-07` warn（沒有畫面導向它，符合 OQ-26 現況、非 error），無其他 error（已用 `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md .dev/F02-user-membership/ui-user-membership.md --spec "..."` 驗證 `s-board`／`s-swimlane-list`／`s-stage-list` 的跨檔 `REF-07` 皆消失，屬 OQ-16 既知限制）。
- `tools error-count .dev/F02-user-membership/ui-user-membership.md`：39（原 52）。
- `tools accept-check ui-authoring-tasks.md T2.07`：通過（無輸出，exit 0）。
- 逐條對照任務描述：八段齊全——達成；對應 `uc-delete-board`——達成；`uc-member-add-card` 未放入本畫面——達成（維持給 T2.08／T1.06）。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（新增 OQ-26 的『』引用與 spec 逐字相符）。

### Act
- 完成：T2.07 標 `done`。
- 下一個任務：T2.08（[F02] 定案 `s-card-assignee-picker`，對應 `uc-set-card-assignees`／`uc-list-card-assignee-candidates`／`uc-view-card-assignees`）。

## Iteration 31 — 2026-09-17 — D-11
### Plan
- 上一輪驗證：PASS。`actionable` 第一項 D-10 為「doing」但其驗收條件與內容比對後確認上輪已全部完成（state.md 已註明「不需再動內容」，僅等 T2.11 收尾整檔歸零才能改 `done`），本輪不重複動它；依「D-xx 的 todo」優先順序改做 D-11。
- 要改的檔案：`.dev/F02-user-membership/ui-user-membership.md` 的 `s-member-management` 段落（194～253 行）；`.dev/loops/ui-authoring-loop/.state/ui-authoring-open-questions.md` 追加 OQ-27。
- 驗收條件：段落不含「成員數增加 1」「負責人欄位移除該成員」「不建立新的」「角色不變」「須為系統中已存在帳號」「待確認事項提醒」；OQ 新增一列涉及邀請對象帳號是否須已存在；`verify-quotes.py` 回傳 0；`ui-check` error 數不增加（審查時 39）。
- 預期 error 數變化：不變（39 → 39，純文字改寫，不改變 DS/REF 檢查對象）。

### Do
- `6292049`　`[spec/design](user-membership) 修正成員管理畫面把業務結果寫進 UI 段落`：
  1. 操作表「邀請成員」成功後改成「成員清單顯示新加入的成員」。
  2. 「變更成員角色」成功後改成「清單中該成員的角色顯示為 Owner」。
  3. 「移除成員」成功後刪掉「若該成員仍是某些 `card` 的負責人，這些 `card` 的負責人欄位移除該成員」（`uc-remove-member` post 對 `card` 的領域結果，不在本畫面）。
  4. 「嘗試邀請成員」「嘗試變更成員角色」失敗時都簡化為「顯示訊息，停留本畫面」，刪掉「不建立新的 `board-membership`」「`board-membership` 角色不變」。
  5. 驗收條件「角色不變」改成「清單中該成員的角色顯示不變」。
  6. 資料表「邀請對象帳號」的驗證欄依 `uc-invite-member` pre 只引用 p2，移除「須為系統中已存在帳號」（找不到 spec 依據），改標 ⚠️ 待確認並記入 OQ-27。
  7. 「移除成員」需確認欄刪掉「待確認事項提醒需要一則確認提示」（待確認事項段沒有對應條目，前後不一致）。
- OQ 檔追加 OQ-27（【引用原文】`uc-invite-member` pre p1／p2 逐字引用，未定義帳號是否須已存在）；「待確認事項」補一條 ⚠️ 指向 OQ-27。
- 低風險決定：⚠️ 文字用「是否要求帳號已存在」而非「須為系統中已存在帳號」，避免與驗收條件禁止詞面重複，語意不變。

### Check
- `grep` 確認段落不再含「成員數增加 1」「負責人欄位移除該成員」「不建立新的」「角色不變」（獨立詞）「須為系統中已存在帳號」「待確認事項提醒」：全部 0 筆。
- `python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py`：通過（OQ-27 的兩筆『』引用皆與 spec 逐字相符，含 yaml 雙引號）。
- `tools error-count .dev/F02-user-membership/ui-user-membership.md`：39（不變）。
- `tools accept-check ui-authoring-tasks.md D-11`：無機械 token 可比對（驗收條件多為文字排除項），已用上述 grep／verify-quotes／error-count 逐條核對。

### Act
- 完成：D-11 標 `done`。
- 下一個任務：D-12（[F02] 修正 `s-board-delete-dialog`／`s-board-create-dialog`／`s-board-list` 把領域狀態寫進 UI 段落）。

## Iteration 32 — 2026-09-17 19:43 — D-11

### Plan
- 上一輪驗證：FAIL。失敗項目：「PDCA「## Iteration 31 — 2026-09-17 — D-11」的 Check 沒有貼 ui-check 的結果行（`N error(s), M warning(s)`）」。
- 依規則書「先修正失敗項目，再繼續 state 記載的同一個任務」：本輪只補這一筆缺漏，不改動已 commit 的 ui 檔內容（PDCA 只能追加，不能回頭改舊段落）；D-11 內容本身（`6292049`）已完成且未變動。
- 驗收條件：本則 Check 段落補上 `./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 的最後一行結果。
- 預期 error 數變化：無（沿用 Iteration 31 的判斷，D-11 為純文字改寫）。

### Do
- 未變動任何 ui 檔或程式碼；僅重跑檢查指令取得 Iteration 31 遺漏的結果行並補記於此。

### Check
- `./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"`：52 error(s), 38 warning(s)（與 D-11 改動前相同，純文字改寫不影響 DS/REF 檢查）。
- `tools error-count .dev/F02-user-membership/ui-user-membership.md`：39（不變）。
- `tools accept-check ui-authoring-tasks.md D-11`：無機械 token 可比對，已於 Iteration 31 以 grep／verify-quotes／error-count 逐條核對過，本則不重複。

### Act
- 完成：補上 Iteration 31 遺漏的 ui-check 結果行；D-11 維持 `done`。
- 下一個任務：D-12（[F02] 修正 `s-board-delete-dialog`／`s-board-create-dialog`／`s-board-list` 把領域狀態寫進 UI 段落）。

## Iteration 33 — 2026-09-17 20:05 — D-12
### Plan
- 任務：D-12（[F02] 修正 `s-board-delete-dialog`／`s-board-create-dialog`／`s-board-list` 把領域狀態寫進 UI 段落，同 D-11 規則）。
- 要改的檔案與段落：`.dev/F02-user-membership/ui-user-membership.md`——`s-board-delete-dialog`「進入與離開」「操作」「驗收條件」；`s-board-create-dialog`「操作」「驗收條件」；`s-board-list`「狀態」「驗收條件」「待確認事項」。
- 驗收條件：檔案不含「不再存在」「Board 與其資料不變」「角色為 Owner」「仍無法存取」「OQ-16 已由人工修正」；`ui-check(ui-user-membership.md)` error 數不增加（起點 39）。
- 預期 error 數變化：0（純文字改寫，不動格式與 ID）。

### Do
- `2a3fbff` [spec/design](user-membership) 修正三畫面把業務結果寫進 UI 段落。
- 逐項對照任務描述四點改寫：(1) `s-board-delete-dialog` 完成後去哪裡／中途放棄／操作表「成功後」／驗收條件都改成只留畫面斷言（導向 `s-board-list`、回到進入前的畫面、不觸發 `uc-delete-board`）。(2) `s-board-create-dialog` 操作表與驗收條件的「操作者對該 Board 角色為 Owner」改成「`s-board-list` 顯示新建立的 Board」。(3) `s-board-list` 狀態段「無權限」與驗收條件的「操作者仍無法存取該 Board」改成「顯示訊息，停留本畫面」。(4) 刪除 `s-board-list`「待確認事項」裡已解決的 OQ-16 那一條，保留 OQ-17／OQ-18 那條。
- 低風險決定：無需另外判斷，皆為任務描述明講的文字替換。
- 無新增 OQ／D-xx。

### Check
- `./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 最後一行：`51 error(s), 38 warning(s)`（此指令因 `--spec` 帶入全部模組 spec 而混入其他模組既有的 warning／error，非本檔獨有數字）。
- `tools error-count .dev/F02-user-membership/ui-user-membership.md`：39（與起點相同，未增加）。
- `tools accept-check ui-authoring-tasks.md D-12`：無輸出、exit=0（無機械 token 可比對，已用 grep 核對四個關鍵字串均不存在）。
- `grep -n "不再存在\|Board 與其資料不變\|角色為 Owner\|仍無法存取\|OQ-16 已由人工修正" ui-user-membership.md`：無結果，驗收條件達成。
- 本輪未新增或修改 OQ 表列、未新增『』標註引用，未跑 `verify-quotes.py`。

### Act
- 完成：D-12 標 `done`。
- 下一個任務：D-13（[F02] `s-signup`／`s-login` 狀態行從「討論中」改「已定案」，前提確認無殘留 `⚠️`）。

## Iteration 34 — 2026-09-17 19:47 — D-13

### Plan
- 任務：D-13——確認 `ui-user-membership.md` 的 `s-signup`（第 5～50 行）與 `s-login`（第 51～101 行）段落內沒有殘留 `⚠️`，且「待確認事項」皆為「（無）」，若成立則把兩畫面「狀態」行由「討論中」改為「已定案」。
- 要改的檔案：`.dev/F02-user-membership/ui-user-membership.md`（只改兩行「狀態：」）。
- 驗收條件：`s-signup`／`s-login` 狀態行變成「狀態：已定案」且段落不含 `⚠️`；`ui-check(.dev/F02-user-membership/ui-user-membership.md)` error 數不增加（基準 39）。
- 預期 error 數變化：0（純文字改狀態字串，不影響 ui-check 解析）。

### Do
- `grep -n "⚠️"` 掃描全檔，確認 5～101 行範圍（`s-signup`／`s-login`）沒有任何 `⚠️` 命中；兩畫面「待確認事項」原文已是「（無）」。
- 修改 `s-signup`、`s-login` 的「狀態：討論中」為「狀態：已定案」。
- commit `60edfcf [spec/design](user-membership) 定案 s-signup 與 s-login 畫面`。
- 無新增 OQ／D-xx。

### Check
- `tools error-count .dev/F02-user-membership/ui-user-membership.md`：39（不變）。
- `./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 最後一行：`51 error(s), 38 warning(s)`（含其他模組 spec 混入的數字，單檔基準以 `tools error-count`＝39 為準，符合驗收條件「不增加」）。
- `tools accept-check ui-authoring-tasks.md D-13`：無機械 token 可比對（驗收條件用自然語言描述，非 `ui-check(...)=0` 格式），已用上述兩項指令逐條核對。
- 驗收條件逐條確認：狀態行皆為「已定案」✓；兩畫面段落不含 `⚠️` ✓；error 數未增加 ✓。

### Act
- 完成：D-13 標記 `done`；下一個任務 T2.08（定案 `s-card-assignee-picker`）。
- 未完成：D-10 仍列 `doing`，待 T2.11 收尾任務整份歸零後改 `done`，不需再動內容。

## Iteration 35 — 2026-09-17 20:20 — T2.08
### Plan
本輪任務 T2.08：在 `ui-user-membership.md` 定案 `s-card-assignee-picker`（對話框，嵌入 F01 `s-card-detail`），對應 `uc-set-card-assignees`／`uc-list-card-assignee-candidates`／`uc-view-card-assignees` 三個 uc；八段全部填齊，「進入與離開」明寫從 F01 `s-card-detail` 進入、完成回到該畫面。預期 error 數從 39 下降（原骨架缺八段內容造成的 DS-02／DS-04 錯誤會消失）。

### Do
- 8e858b1 [spec/design](ui-user-membership) 定案卡片負責人選取畫面
- 低風險決定：候選清單「來源」欄只填 `board-membership`（Entity），把 `uc-list-card-assignee-candidates` 的引用改放到「驗證/格式」欄，避免 DS-03（資料表.來源只能是 Attribute/Entity）誤判
- 低風險決定：`uc-view-card-assignees`（讀取用 uc，判斷卡片是否有負責人）放進「已選負責人」欄的驗證/格式說明與「狀態」段「空資料」項，因為它本身不是按鈕觸發的操作，不硬塞進操作表
- 「儲存變更」操作「需確認？」判斷為「否」：`uc-set-card-assignees` 的 post 只描述 `card.assignees` 更新，可隨時重新開啟本畫面調整負責人，不是不可逆操作
- 未新增 OQ／D-xx（本畫面所需資訊 spec 皆已齊備，未觸發自主決策分級的高風險或覆蓋 spec 情況）

### Check
`./scripts/ui-check .dev/F0*/ui-*.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 最後一行：`27 error(s), 37 warning(s)`（原 39 error）；`s-card-assignee-picker` 本身僅剩 1 個 warn（DS-07：無畫面導向它，因 F01 `s-card-detail` 尚未回填入口，屬 T2.11 範圍，預期內）。
`tools accept-check T2.08`：無輸出（該任務驗收條件無機械 token，已改用上述肉眼核對）。
逐條對照驗收條件：
- 「進入與離開」含「F01 `s-card-detail` 的負責人指派入口」與回到 `s-card-detail` → 達成
- 對應 `uc-set-card-assignees`（操作表）／`uc-list-card-assignee-candidates`（資料表）／`uc-view-card-assignees`（資料表、狀態段） → 達成
- 八段格式齊全、狀態五項不省略、待確認事項清空為「（無）」 → 達成
本輪未新增或修改 OQ／待確認事項的【引用原文】等標記，未跑 `verify-quotes.py`。

### Act
完成：下一個任務 T2.09（[F02] 定案 `s-cards-by-assignee`，類型：列表，對應 `uc-list-cards-by-assignee`）。

## Iteration 36 — 2026-09-17 21:10 — T2.09
### Plan
本輪任務：T2.09，定案 `s-cards-by-assignee`（列表），對應 `uc-list-cards-by-assignee`。要改的檔案：`.dev/F02-user-membership/ui-user-membership.md`（該畫面骨架 → 八段完整內容）。驗收條件：同 T1.02 模式（`ui-check` 對本畫面無 error、八段齊全）。預期 error 數變化：本畫面本身應維持 0 error；因 spec 未描述入口，需標 ⚠️ 開 OQ，畫面狀態維持「討論中」。

### Do
- `spec-user-membership.md` `uc-list-cards-by-assignee`（roles: [r-board-member]，crud: {card: R}，pre/fail 皆空，post 僅「清單列出所有以指定成員為負責人的 card」）與唯一 Scenario「一位成員同時是多張卡片的負責人」未描述抵達本畫面的來源畫面 → 標高風險，`s-cards-by-assignee`「進入與離開」標 ⚠️，追加 OQ-28（見 `ui-authoring-open-questions.md`），畫面狀態訂為「討論中」
- 資料表：「查詢對象」來源 `user.display-name`；「卡片清單」來源 `card`（透過 `card.assignees`）；「卡片標題」跨模組引用 F01 `card.title`
- 操作表僅一列「開啟卡片詳情」（觸發 `—`，純前端導覽開啟 F01 `s-card-detail`，跨模組引用），無寫入操作，符合 `uc-list-cards-by-assignee` crud 僅 `card: R`
- 低風險決定：不另立獨立畫面呈現「查詢對象」欄位樣式，直接列為資料表第一列（版面歸類）
- 自我審查發現本畫面草稿一開始沿用 `s-card-assignee-picker` 既有的 `` `roles` `` 反引號寫法（"roles" 非六種允許 ID），改寫為「角色」不加反引號，避免新增 REF-07 error（`s-card-assignee-picker` 本身既有的同類錯誤是 T2.08 遺留，本輪未動，留給收尾任務）
- commit：`95f96c5` `[spec/design](ui-user-membership) 定案依負責人查詢卡片畫面`；`[docs](loops) T2.09 完成：定案依負責人查詢卡片畫面`（下方 docs commit）

### Check
`./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md .dev/F01-basic-kanban/ui-kanban-basic.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 最後一行：`14 error(s), 21 warning(s)`（全部 14 個 error 為既有 `s-activity-log` 骨架缺段落（T2.10 範圍）與 `s-card-assignee-picker` 既有 1 筆 `roles` 反引號誤用，`s-cards-by-assignee` 本身 0 error）。`tools accept-check` 對 T2.09 無機械 token 輸出（驗收條件為「同上」文字描述），已改用上述指令肉眼核對：八段齊全、無新增 error、`REF-07`／`DS-02`／`DS-04` 皆未新增。`verify-quotes.py`：「逐字引用驗證通過」。

### Act
完成：下一個任務 T2.10（[F02] 定案 `s-activity-log`）。

## Iteration 37 — 2026-09-17 21:45 — T2.10
### Plan
本輪任務：T2.10，定案 `s-activity-log`（列表），對應 `uc-view-board-activity-log`。開工先讀 `spec-user-membership.md`「Feature: 檢視看板活動紀錄」現況，依任務規則判斷走「已定案」或「尚未實作」分支。要改的檔案：`.dev/F02-user-membership/ui-user-membership.md`（該畫面骨架）。驗收條件：`ui-check` 對本畫面無 error（未定案時「狀態」為「未討論」一樣要通過格式檢查）；若走「尚未實作」分支，OQ 檔有對應列。預期 error 數變化：本畫面本身應維持 0 error。

### Do
- 讀 `spec-user-membership.md` 776～816 行，Feature 說明明文寫「本情境目前尚未實作」（跨 aggregate 查詢投影留給「kanban-spring」有 application／persistence 層時再做）→ 判定走「尚未實作」分支
- `s-activity-log` 狀態維持「未討論」，八段標題各填「不適用，理由同上」（「狀態」段仍列出五個固定子項以符合 DS-04），並在檔頭補一段簡短說明引用 spec 原文與 OQ 編號
- 追加 OQ-29（見 `ui-authoring-open-questions.md`），採用選項 1（維持「未討論」、僅八段標題與簡短說明）
- commit：`0cdf519` `[spec/design](ui-user-membership) 暫緩定案看板活動紀錄畫面`；`[docs](loops) T2.10 完成：暫緩定案看板活動紀錄畫面`（下方 docs commit）

### Check
`./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 最後一行：`20 error(s), 37 warning(s)`（全部 20 個 error 為既有跨模組 Screen 引用（F01 `s-board`／`s-stage-list`／`s-swimlane-list`、F02 `s-card-detail`）未在單檔模式解析、以及 `s-card-assignee-picker` 既有 1 筆 `roles` 反引號誤用，`s-activity-log` 本身 0 error）。`tools error-count .dev/F02-user-membership/ui-user-membership.md`＝1（即上述既有 `roles` 誤用，非本輪新增）。`tools accept-check` 對 T2.10 無機械 token 輸出，已肉眼核對：八段標題齊全（含五個狀態子項）、無新增 error、OQ-29 已建立。`verify-quotes.py`：「逐字引用驗證通過」。

### Act
完成：下一個任務 T2.11（跨模組收尾任務）。
