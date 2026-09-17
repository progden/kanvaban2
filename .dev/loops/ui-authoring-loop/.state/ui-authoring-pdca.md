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
