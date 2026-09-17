# UI 撰寫任務清單（Task Ledger）

本檔是 UI 撰寫 loop **唯一的任務來源**。規則：

- **T／G 任務只有人工可以新增、刪除或改寫**任務描述、驗收條件、依賴；loop（不論執行輪或審查輪）只能改「狀態」欄。
- loop 發現計畫外的必要工作：在「發現的任務」表追加 `D-xx`，狀態直接 `todo`（本 loop 沒有 proposed／rejected 中間狀態，執行輪與審查輪都可以直接新增；任務描述要把判斷依據寫清楚，供人工事後稽核；描述與驗收條件寫好之後同樣不可再改，只能改狀態）。
- 狀態：`todo` 待做／`doing` 進行中（上一輪未完成）／`done` 完成／`blocked` 只用於環境限制（見規則書「自主決策分級」，只有人工可以解除）。
- 挑選順序：`doing` → `D-xx` 的 `todo` → 依表格順序第一個 `todo`；依賴必須全部 `done`。
- `G*` 是關卡。**手動模式**（`/loop`）：不單獨佔一輪，跟前一個任務的收尾一起做完，或作為某輪的本體任務執行（同一輪內完成自我審查並把關卡標 `done`）。**自動模式**（`run-ui-authoring-loop.sh`）：由獨立審查輪把關，只有 `.dev/loops/ui-authoring-loop/runtime/gates/<G>.approved` 存在（驅動腳本或人工建立，loop 本身不可自建）之後，下一個執行輪才能把該關卡標 `done`。
- 「任務」欄開頭的 `[F0x]` 標示任務對應的模組。
- 驗收條件裡的 `ui-check(<檔>)=0` 代表 `./scripts/ui-check <檔>` 的 error 數為 0；`ui-check(all)=0` 代表 `./scripts/ui-check`（不帶參數，掃全部）error 數為 0。warn 不擋 `done`，但收尾任務要逐項看過。這個 token 語法也是 `ui-authoring-tools.py accept-check` 機械驗證用的格式，新增 D-xx 時若驗收條件能寫成這個格式就盡量寫，方便自動模式外部驗證。

---

## 階段 0：準備

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T0.01 | done | 確認起點：`./scripts/ui-check` 可執行（目前應印「找不到任何 ui 檔」，六個模組目錄下都還沒有 `ui-*.md`）；建立 `runtime/` 目錄，`git rev-parse HEAD` 寫入 `runtime/baseline`；PDCA 本則 Check 貼上述兩個指令的輸出 | `runtime/baseline` 存在且是合法 commit hash；PDCA 本則含 `ui-check` 的輸出行 | — |

## 階段 1：F01 basic-kanban（`.dev/F01-basic-kanban/spec-kanban-basic.md` → `ui-kanban-basic.md`）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T1.01 | done | [F01] 建立 `ui-kanban-basic.md`：檔頭（依 `ui-convention.md`「檔案與固定格式」）＋ 8 個畫面標題骨架（`s-swimlane-list`、`s-swimlane-delete-dialog`、`s-stage-list`、`s-stage-delete-dialog`、`s-board`、`s-card-add-dialog`、`s-card-detail`、`s-card-delete-dialog`），每個畫面只填標題下三行（所屬 Feature、類型、狀態：未討論），八段內容先留空標題 | 8 個 `## s-` 標題與三行標頭都存在；`ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 對畫面標題格式（`DS-01`）不報 error | T0.01 |
| T1.02 | done | [F01] 定案 `s-swimlane-list`（類型：列表）：對應 `uc-add-swimlane`／`uc-rename-swimlane`／`uc-reorder-swimlane`（新增、重新命名、拖曳排序都在同一張列表上操作） | `ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)` 對本畫面無 error；八段齊全 | T1.01 |
| T1.03 | done | [F01] 定案 `s-swimlane-delete-dialog`（類型：對話框）：對應 `uc-delete-swimlane`（含目的泳道選擇的成功／失敗情境） | 同上，本畫面無 error | T1.02 |
| T1.04 | done | [F01] 定案 `s-stage-list`（類型：列表）：對應 `uc-add-stage`／`uc-rename-stage`／`uc-reorder-stage`／`uc-set-stage-role` | 同上 | T1.03 |
| T1.05 | done | [F01] 定案 `s-stage-delete-dialog`（類型：對話框）：對應 `uc-delete-stage`（含卡片轉移情境） | 同上 | T1.04 |
| T1.06 | done | [F01] 定案 `s-board`（類型：流程／看板主畫面）：對應 `uc-move-card-swimlane`／`uc-move-card-stage`（拖曳卡片跨泳道／跨階段）；同時在「操作」表列出 F02 的 `uc-assign-card-owner-by-drag`（拖曳成員頭像到卡片指派負責人），跨模組引用、不重新定義 | 同上，且操作表含 `uc-assign-card-owner-by-drag` 一列 | T1.05 |
| T1.07 | done | [F01] 定案 `s-card-add-dialog`（類型：表單）：對應 `uc-add-card` | 同上 | T1.06 |
| T1.08 | done | [F01] 定案 `s-card-detail`（類型：表單）：對應 `uc-edit-card`／`uc-add-comment`；負責人相關欄位（顯示目前負責人、開啟指派）先標 `⚠️` 待確認、待確認事項寫「負責人欄位與指派入口待 F02 `s-card-assignee-picker` 定案後回填」，記入 OQ | 同上；待確認事項含上述 `⚠️` 條目；OQ 檔追加對應列 | T1.07 |
| T1.09 | done | [F01] 定案 `s-card-delete-dialog`（類型：對話框）：對應 `uc-delete-card` | 同上 | T1.08 |
| T1.10 | done | [F01] 收尾：`ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 0 error；過一遍 `DS-06`（寫入 uc 有沒有被引用）／`DS-07`（畫面有沒有被導向）warn，沒理由的回頭補。OQ-10 已由人工解除：`ui-authoring-tools.py` 的 `findings()` 改成一律對所有既有 ui 檔的 `error-count`／`accept-check` 都帶入全部模組的 `spec-*.md`（`--spec`），不再只載入 target 自己模組的 spec，跨模組引用不再被 `REF-07` 誤判；本輪起 `ui-check(<檔>)=0` 這個 token 一律視為已含跨模組 spec 的結果。人工肉眼核對用原始 `./scripts/ui-check` 指令時，也要記得加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`，否則會重現同一個誤判 | `ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0`；PDCA 本則列出 `DS-06`／`DS-07` warn 清單與處理結果 | T1.09 |

## 階段 2：F02 user-membership（`.dev/F02-user-membership/spec-user-membership.md` → `ui-user-membership.md`）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T2.01 | done | [F02] 建立 `ui-user-membership.md`：檔頭 ＋ 9 個畫面標題骨架（`s-signup`、`s-login`、`s-board-list`、`s-board-create-dialog`、`s-member-management`、`s-board-delete-dialog`、`s-card-assignee-picker`、`s-cards-by-assignee`、`s-activity-log`） | 同 T1.01 模式 | T1.10 |
| T2.02 | done | [F02] 定案 `s-signup`（類型：表單）：對應 `uc-create-user` | 同 T1.02 模式 | T2.01 |
| T2.03 | done | [F02] 定案 `s-login`（類型：表單）：對應 `uc-login`；`uc-logout` 併入本畫面的操作表（例如全域導覽列的登出動作，「觸發」欄仍填 `uc-logout`），不獨立開一個畫面 | 同上，操作表含 `uc-login` 與 `uc-logout` 兩列 | T2.02 |
| T2.04 | done | [F02] 定案 `s-board-list`（類型：列表）：對應 `uc-view-board-list`；無權限狀態對應 `uc-reject-board-access-by-nonmember` | 同上 | T2.03 |
| T2.05 | done | [F02] 定案 `s-board-create-dialog`（類型：表單）：對應 `uc-create-board` | 同上 | T2.04 |
| T2.06 | done | [F02] 定案 `s-member-management`（類型：對話框／側欄）：對應 `uc-invite-member`／`uc-change-member-role`／`uc-remove-member`；三個 `uc-reject-invite-by-member`／`uc-reject-role-change-by-member`／`uc-reject-structure-change-by-member` 的拒絕情境放對應操作的「失敗時」欄 | 同上 | T2.05 |
| T2.07 | todo | [F02] 定案 `s-board-delete-dialog`（類型：對話框）：對應 `uc-delete-board`；`uc-member-add-card` 屬於卡片操作，不在本畫面，改在 T2.08／T1.06 標註引用 | 同上 | T2.06 |
| T2.08 | todo | [F02] 定案 `s-card-assignee-picker`（類型：對話框／側欄，嵌入 F01 `s-card-detail`）：對應 `uc-set-card-assignees`／`uc-list-card-assignee-candidates`／`uc-view-card-assignees`；「進入與離開」寫明從 F01 `s-card-detail` 進入、完成回到該畫面 | 同上 | T2.07 |
| T2.09 | todo | [F02] 定案 `s-cards-by-assignee`（類型：列表）：對應 `uc-list-cards-by-assignee` | 同上 | T2.08 |
| T2.10 | todo | [F02] 定案 `s-activity-log`（類型：列表／側欄）：對應 `uc-view-board-activity-log`。開工先讀 spec 該 uc 現況；若 spec 明確標注「尚未實作」一類字樣，畫面狀態維持「未討論」，待確認事項寫「依 spec 標注尚未實作，暫緩定案」，記入 OQ，不產出完整八段內容（仍要有八段標題，內容可簡短說明原因）；若 spec 已可正常推導，比照其他畫面正常定案 | `ui-check` 對本畫面無 error（未定案時「狀態」為「未討論」一樣要通過格式檢查）；若走「尚未實作」分支，OQ 檔有對應列 | T2.09 |
| T2.11 | todo | [F02] 收尾：`ui-check .dev/F02-user-membership/ui-user-membership.md` 0 error；回頭補 `.dev/F01-basic-kanban/ui-kanban-basic.md` 的 `s-card-detail`（T1.08 留的 `⚠️`）——負責人欄位來源改填 `card.assignees`，「進入與離開」補上指派入口導向 `s-card-assignee-picker`；兩個檔案一起跑 `ui-check` 確認都 0 error；過一遍 `DS-06`／`DS-07` warn | `ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0`；`ui-check(.dev/F02-user-membership/ui-user-membership.md)=0`；`s-card-detail` 待確認事項不再含 T1.08 那條 `⚠️` | T2.10 |

## 階段 3：F03 kanban-widgets（`.dev/F03-kanban-widgets/spec-kanban-widgets.md` → `ui-kanban-widgets.md`，全部讀取類 Use Case）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T3.01 | todo | [F03] 建立 `ui-kanban-widgets.md`：檔頭 ＋ 4 個畫面標題骨架（`s-cycle-lead-time-dashboard`、`s-wip-dashboard`、`s-throughput-cfd-dashboard`、`s-duedate-reminder`） | 同 T1.01 模式 | T2.11 |
| T3.02 | todo | [F03] 定案 `s-cycle-lead-time-dashboard`（類型：儀表板）：對應 `uc-view-cycle-lead-time`；資料段列出每個數字/圖表的來源 Attribute 或衍生計算方式 | 同 T1.02 模式 | T3.01 |
| T3.03 | todo | [F03] 定案 `s-wip-dashboard`（類型：儀表板）：對應 `uc-view-wip`／`uc-view-aging-wip` | 同上 | T3.02 |
| T3.04 | todo | [F03] 定案 `s-throughput-cfd-dashboard`（類型：儀表板）：對應 `uc-view-throughput`／`uc-view-cfd`；兩個資料區塊（Throughput 圖、CFD 圖）在同一個 Screen ID 內用「資料」段分開說明，不拆兩個畫面 | 同上 | T3.03 |
| T3.05 | todo | [F03] 定案 `s-duedate-reminder`（類型：列表）：對應 `uc-view-duedate-reminder` | 同上 | T3.04 |
| T3.06 | todo | [F03] 收尾：`ui-check .dev/F03-kanban-widgets/ui-kanban-widgets.md` 0 error；過一遍 `DS-06`／`DS-07` warn | `ui-check(.dev/F03-kanban-widgets/ui-kanban-widgets.md)=0` | T3.05 |

## 階段 4：F04 board-clock（`.dev/F04-board-clock/spec-board-clock.md` → `ui-board-clock.md`）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T4.01 | todo | [F04] 建立 `ui-board-clock.md`：檔頭 ＋ 1 個畫面標題骨架（`s-board-clock-control`） | 同 T1.01 模式 | T3.06 |
| T4.02 | todo | [F04] 定案 `s-board-clock-control`（類型：對話框／側欄，例如頂列的時鐘控制項）：對應 `uc-adjust-board-clock`／`uc-pause-resume-board-clock`；`uc-guard-clock-monotonicity` 是共同前置條件，寫在 `uc-adjust-board-clock` 操作的「失敗時」欄，不獨立成一個操作列 | 同 T1.02 模式 | T4.01 |
| T4.03 | todo | [F04] 收尾：`ui-check .dev/F04-board-clock/ui-board-clock.md` 0 error | `ui-check(.dev/F04-board-clock/ui-board-clock.md)=0` | T4.02 |

## 階段 5：F05 workload（`.dev/F05-workload/spec-workload.md` → `ui-workload.md`）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T5.01 | todo | [F05] 建立 `ui-workload.md`：檔頭 ＋ 1 個畫面標題骨架（`s-workload-dashboard`） | 同 T1.01 模式 | T4.03 |
| T5.02 | todo | [F05] 定案 `s-workload-dashboard`（類型：儀表板）：對應 `uc-view-workload`／`uc-drag-assign-card-owner`（拖曳成員頭像到卡片） | 同 T1.02 模式 | T5.01 |
| T5.03 | todo | [F05] 收尾：`ui-check .dev/F05-workload/ui-workload.md` 0 error | `ui-check(.dev/F05-workload/ui-workload.md)=0` | T5.02 |

## 階段 6：F06 feature-cr-board（`.dev/F06-feature-cr-board/spec-feature-cr-board.md` → `ui-feature-cr-board.md`）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T6.01 | todo | [F06] 建立 `ui-feature-cr-board.md`：檔頭 ＋ 1 個畫面標題骨架（`s-feature-cr-board`） | 同 T1.01 模式 | T5.03 |
| T6.02 | todo | [F06] 定案 `s-feature-cr-board`（類型：儀表板／列表）：對應 `uc-view-feature-cr-board` | 同 T1.02 模式 | T6.01 |
| T6.03 | todo | [F06] 收尾：`ui-check .dev/F06-feature-cr-board/ui-feature-cr-board.md` 0 error | `ui-check(.dev/F06-feature-cr-board/ui-feature-cr-board.md)=0` | T6.02 |

## 階段 7：F07 canvas-layout（`.dev/F07-canvas-layout/spec-canvas-layout.md` → `ui-canvas-layout.md`）

F07 目前狀態：草稿，且「待釐清」段落明講 Canvas 的建立時機、看板本體如何成為一個 `item`、`item.component`／`viewport.user` 的 ref 化都還沒定案（見 spec 檔尾）。依規則書「輸入怎麼讀」，這些缺口一律走「自主決策分級」標 `⚠️` 記 OQ，不要因為 spec 是草稿就放寬不寫，也不要越權替 SA 把「待釐清」的項目定案。

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T7.01 | todo | [F07] 建立 `ui-canvas-layout.md`：檔頭 ＋ 1 個畫面標題骨架（`s-canvas`） | 同 T1.01 模式 | T6.03 |
| T7.02 | todo | [F07] 定案 `s-canvas`（類型：流程／主畫面）：對應本模組全部 10 個 uc（`uc-place-item`／`uc-remove-item`／`uc-move-item`／`uc-resize-item`／`uc-set-item-capabilities`／`uc-set-item-anchor`／`uc-reorder-item`／`uc-move-items`／`uc-remove-items`／`uc-set-viewport`）；「進入與離開」需說明這是使用者從 F02 `s-board-list` 點擊某個 Board 進入後看到的畫面（見 spec 簡介「使用者開啟某個 Board 時，看到的就是該 Board 的 Canvas」），離開回到 `s-board-list`；`uc-place-item`／`uc-set-item-capabilities`／`uc-set-item-anchor` 這類非「拖曳／調整大小」的操作，觸發方式（工具列按鈕、右鍵選單等）不寫（排版與視覺），但仍要在操作表列出對應列。**人工決策（OQ-17／OQ-18）**：畫布中間預設有一個「看板本體」`item`；選中該 `item` 後顯示的屬性／操作面板（畫布左側）要在操作表另外列出 F01 的 `uc-add-swimlane`／`uc-add-stage`（跨模組引用，觸發欄填該 uc ID），並判斷 `s-swimlane-list`／`s-stage-list`／`s-swimlane-delete-dialog`／`s-stage-delete-dialog` 這幾個 F01 已定案的獨立 Screen 是否仍要存在（例如改成從這個面板彈出的小清單，還是維持獨立 Screen 但改標「側欄，嵌入 `s-canvas`」，類比 F02 `s-card-assignee-picker` 嵌入 F01 `s-card-detail` 的模式）；判斷結果記錄後直接建立或更新 D-09 | 同 T1.02 模式 | T7.01 |
| T7.03 | todo | [F07] 收尾：`ui-check .dev/F07-canvas-layout/ui-canvas-layout.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 0 error；過一遍 `DS-06`／`DS-07` warn | `ui-check(.dev/F07-canvas-layout/ui-canvas-layout.md)=0` | T7.02 |

## 階段 8：跨模組收尾

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T8.01 | todo | 七份 `ui-*.md` 一起跑 `./scripts/ui-check` 到 0 error；`./scripts/ui-check --report` 看畫面總表與追溯矩陣，`DS-06`（寫入 uc 沒被任何畫面引用）／`DS-07`（畫面沒被任何畫面導向，也不是模組入口）warn 逐項確認有理由（例如背景作業、確實是模組入口）或回頭補 | `ui-check(all)=0`；PDCA 本則列出 `DS-06`／`DS-07` 完整 warn 清單與逐項處理結果 | T7.03 |
| G1 | todo | 關卡：自我審查——依 `.dev/conventions/llm-review.md` L-09（「失敗時」是否對應 `fail`、「成功後」是否對應 `post`）與 `checks.md` DS-05（操作可用角色與 uc `roles` 是否一致）各抽查七個模組至少一個畫面；有偏差在對應 ui 檔直接修正並記錄。**手動模式**（`/loop`）：同一輪內完成，不開 D-xx 拖到下一輪，直接把本關卡標 `done`。**自動模式**（`run-ui-authoring-loop.sh`）：由獨立審查輪（`ui-authoring-review-prompt.md`）執行，偏差開成 `D-xx`（`todo`），審查後無待修項目才由驅動腳本建立 `runtime/gates/G1.approved`、下一輪才能把本關卡標 `done` | PDCA 或 `ui-authoring-review.md` 本則列出抽查的 7 個畫面與檢查結果；若有修正，修正後 `ui-check(all)=0` | T8.01 |
| T8.02 | todo | 最終確認並建立 `runtime/DONE`：`ui-check(all)=0`；七份 `ui-*.md` 都存在；任務清單除本任務外無 `todo`／`doing`／`blocked` | `runtime/DONE` 存在（不進版控）；PDCA 本則貼 `ui-check` 最終結果行 | G1 |

## 發現的任務（D-xx）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| D-01 | done | [F01] 修正 `ui-kanban-basic.md` 的 `s-swimlane-list` 操作表第 35、36 行的 REF-07 error：「失敗時」欄寫成「無 `fail` 定義」，把 `fail` 包在反引號裡，被 `ui-check` 當成未定義的 Entity ID（規範：反引號只給六種 ID 用）。改成不加反引號的寫法（例如「無 fail 定義」或「fail 為空」），語意不變。T1.02 的 PDCA 用 `grep -i swimlane-list` 核對，但 `ui-check` 的輸出只有行號、沒有 Screen ID，所以沒抓到這兩行；之後核對「本畫面無 error」請改用該畫面的行號範圍比對 | `./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 輸出中沒有 `REF-07`；`s-swimlane-list` 所在行號範圍（`## s-swimlane-list` 到下一個 `## s-`）內沒有任何 error；整檔 error 數 ≤ 78 | — |
| D-02 | done | [F01] 修正 `ui-kanban-basic.md` 有 3 處把業務結果或領域狀態寫進 UI 段落（`ui-convention.md`：驗收條件的斷言主詞只能是畫面元素，或寫成「是否觸發 `uc-xxx`」；操作表「成功後」只寫呈現方式）。(1) `s-stage-delete-dialog` 驗收條件「確認刪除後，該 Stage 內原有卡片（如有）已移動到選擇的目的 Stage…」：「卡片已移動」是 `uc-delete-stage` post 的領域狀態，要刪掉這段或改寫成畫面元素的斷言。(2) `s-board` 操作表「拖曳成員頭像到卡片追加負責人」的「成功後」寫「卡片負責人追加該成員（若原本已是負責人則維持不變）」，這是在重述 `uc-assign-card-owner-by-drag` 的 post，要改成呈現方式，例如「卡片縮圖的負責人顯示更新」。同一個畫面的驗收條件「卡片負責人追加該成員」也改成畫面元素的斷言。(3) `s-board` 狀態段「錯誤」寫「依上方操作表顯示對應訊息」，括號裡卻說三個 uc 都沒有 fail 定義，前後矛盾，要改成「不適用（…無 fail 定義）」。只改寫法，不可新增或改變業務結果 | `s-stage-delete-dialog` 驗收條件不含「已移動到」；`s-board` 操作表與驗收條件不含「負責人追加該成員」；`s-board` 狀態段的「錯誤」以「不適用」開頭；`./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md --spec .dev/F02-user-membership/spec-user-membership.md` 的 error 數不增加 | — |
| D-03 | done | [F01] 補上 T1.06 推翻 OQ-01／OQ-02 的紀錄。OQ-01／OQ-02 的「採用」寫的是「暫定為模組入口」，T1.06 卻把 `s-swimlane-list`／`s-stage-list` 的進入路徑改成「`s-board` 的『管理 Swimlane／Stage』操作」，並把兩個畫面標成「已定案」，OQ 檔沒有追加任何一列。這違反 OQ 規則「結論要推翻先前答案，『情況』欄要明講『推翻 OQ-xx』」。另外，兩個畫面寫「已依 `s-board` 定案結果更新」，但 `s-board` 的狀態其實是「討論中」，這句話與事實不符。處理：(1) OQ 檔末尾追加一列，「情況」欄以【推論】寫法明講「推翻 OQ-01、OQ-02」，並引用兩個 Feature Background 的原文『And 我已開啟一個名為 "產品開發看板" 的看板』；「採用」欄寫從 `s-board` 進入。(2) 兩個畫面的「從哪裡進來」改成不宣稱 `s-board` 已定案的寫法，並引用新的 OQ 編號 | OQ 檔新增一列，「情況」欄含「推翻 OQ-01」與「推翻 OQ-02」；`ui-kanban-basic.md` 不含「定案結果更新」；`python3 .dev/loops/ui-authoring-loop/scripts/verify-quotes.py` 回傳 0 | — |
| D-04 | done | [F01] 修正 `ui-kanban-basic.md` 的 `s-card-detail` 有 2 處把 `uc-edit-card` 的 post 寫成領域狀態（跟 D-02 同一類問題；`ui-convention.md`：「成功後」只寫呈現方式，驗收條件的斷言主詞只能是畫面元素，或寫成「是否觸發 `uc-xxx`」）。(1) 操作表「儲存變更」的「成功後」寫「依 `uc-edit-card` post：`card.description`、`card.due-date`、`card.labels` 更新為編輯內容，停留本畫面」，這是在重述 post 的資料更新，要改成呈現方式，例如「停留本畫面，描述、截止日期、標籤欄位顯示儲存後的內容」。(2) 驗收條件「儲存變更後，描述、截止日期、標籤更新為輸入內容」要改成以畫面欄位為主詞的斷言，例如「儲存變更後，本畫面的描述、截止日期、標籤欄位顯示儲存的內容，且觸發 `uc-edit-card`」。只改寫法，不可新增或改變業務結果 | `ui-kanban-basic.md` 不含「更新為編輯內容」與「更新為輸入內容」；`s-card-detail` 操作表「儲存變更」列不含「依 `uc-edit-card` post」；`./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 的 error 數不增加（審查時為 25） | — |
| D-05 | done | [F01] 修正 `ui-kanban-basic.md` 的 `s-card-add-dialog`（狀態「已定案」）有 3 處寫到領域狀態。(1) 驗收條件「標題輸入為空時確認新增，輸入內容保留、顯示訊息，不建立新卡片」：「不建立新卡片」是 `uc-add-card` fail p1 的業務結果，要改成畫面元素的斷言，例如「對話框維持開啟、輸入內容保留、顯示訊息」。(2) 驗收條件「取消後關閉對話框，不建立卡片」要改成「取消後關閉對話框，且不觸發 `uc-add-card`」。(3) 資料表「目的 Swimlane」「目的 Stage」兩列的「說明」欄寫「建立後 `card.swimlane`／`card.stage` 設為此 Swimlane／Stage」，這是在重述業務結果，要刪掉這句，只留「依進入情境帶入…，本畫面不可變更」。只改寫法，不可新增或改變業務結果 | `s-card-add-dialog` 段落（`## s-card-add-dialog` 到下一個 `## s-`）的驗收條件不含「不建立」；資料表不含「設為此」；`./scripts/ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 的 error 數不增加（審查時為 25） | — |
| D-06 | done | [F02] 修正 `ui-user-membership.md` 的 `s-login` 操作表「登出」列（第 84 行）的 REF-07 error：「失敗時」欄寫「不適用（`uc-logout` 無 `fail`）」，把 `fail` 包在反引號裡，被 `ui-check` 當成未定義的 Entity ID。這和 D-01 是同一個錯誤。改成「不適用（`uc-logout` 無 fail 定義）」，語意不變。T2.03 的 PDCA Check 寫「篩選 `s-login`／`s-signup` 相關訊息：兩者皆 0 筆 error」，但 `ui-check` 輸出只有行號、沒有 Screen ID，用名稱篩選抓不到這一行，這正是 D-01 已經記錄過的陷阱。之後核對「本畫面無 error」一律用該畫面的行號範圍比對 | `./scripts/ui-check .dev/F02-user-membership/ui-user-membership.md --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 輸出中沒有 `REF-07`；`s-login` 所在行號範圍（`## s-login` 到下一個 `## s-`）內沒有任何 error；`python3 .dev/loops/ui-authoring-loop/scripts/ui-authoring-tools.py error-count .dev/F02-user-membership/ui-user-membership.md` 比審查時的 92 少 | — |
| D-07 | done | [F02] 修正 `ui-user-membership.md` 的 `s-signup`／`s-login` 驗收條件中以領域狀態為主詞的斷言（`ui-convention.md`：斷言主詞只能是畫面元素，或寫成「是否觸發 `uc-xxx`」，不寫領域狀態；和 D-02／D-04／D-05 是同一類問題）。(1) `s-signup`「該帳號不應該被建立」「不應該建立新的帳號」改成「且不觸發 `uc-create-user`」，或只保留畫面元素的斷言（畫面維持顯示、輸入內容保留、顯示訊息）。(2) `s-signup`「該帳號的顯示名字等於帳號 ID」是 `uc-create-user` post 的領域結果，要刪掉，或改成畫面元素／觸發 `uc-create-user` 的斷言。(3) `s-signup` 第一條「且不觸發 `uc-create-user` 建立新帳號」刪掉尾巴的「建立新帳號」。(4) `s-login` 兩條「且不視為登入成功」改成「且 TopBar 不顯示帳號名稱」這類畫面斷言，或直接刪除。只改寫法，不可新增或改變業務結果 | `ui-user-membership.md` 不含「不應該被建立」「不應該建立」「建立新帳號」「顯示名字等於帳號 ID」「不視為登入成功」；`ui-check(.dev/F02-user-membership/ui-user-membership.md)` 的 error 數不增加（審查時為 92） | — |
| D-08 | done | [F01] 修正 `ui-kanban-basic.md` 的 `s-card-delete-dialog`（狀態「已定案」）有 3 處把 `uc-delete-card` fail p2 的領域結果「卡片不變」寫進 UI 段落（`ui-convention.md`：操作表只寫呈現方式、以「依 `uc-xxx` p2」引用，不重述業務結果；驗收條件不寫領域狀態）。(1) 操作表「取消」列的「成功後」寫「依 `uc-delete-card` p2：關閉對話框，卡片不變」，刪掉「，卡片不變」。(2) 驗收條件「取消後關閉對話框，卡片不變」改成「取消後關閉對話框，且不觸發 `uc-delete-card`」。(3) 「中途放棄會怎樣」寫「關閉對話框，卡片不變」，改成「關閉對話框，回到 `s-board`，不觸發 `uc-delete-card`」。只改寫法，不可新增或改變業務結果 | `s-card-delete-dialog` 段落（`## s-card-delete-dialog` 到檔尾）不含「卡片不變」；`ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0` | — |
| D-09 | todo | [F01] 人工決策（推翻 OQ-01／OQ-02／OQ-04／OQ-07，見 OQ-17／OQ-18）：F07 是類 Miro 畫布，看板本體固定顯示在 `s-canvas` 中間的一個 `item`，新增 Swimlane／新增 Stage 改由選中該 `item` 後、畫布左側的屬性／操作面板觸發（`uc-add-swimlane`／`uc-add-stage` 觸發畫面改為 `s-canvas`），不是 F01 `s-board` 操作表的「管理 Swimlane」／「管理 Stage」兩列。修正 `ui-kanban-basic.md`：(1) `s-board` 操作表移除「管理 Swimlane」「管理 Stage」兩列（連同其「觸發 `—`」「成功後」欄）。(2) `s-board`「從哪裡進來」依 OQ-18 改寫為「不適用——內容以 F07 `item` 形式顯示於 `s-canvas`，見 `spec-canvas-layout.md`；機制仍待該 spec「待釐清」定案」，不再標「待 F02 `s-board-list` 定案後回填」。(3) `s-swimlane-list`／`s-stage-list`「從哪裡進來」依 OQ-17 改為指向 `s-canvas`（選中看板 `item` 後的屬性／操作面板），不再指向 `s-board` 的操作列；並依 T7.02 判斷這兩個 Screen 是否仍需獨立存在，若判斷不需要則改記錄如何拆解／合併，不直接刪除既有內容。(4) 同步修正 `ui-user-membership.md` 的 `s-board-list`：「完成後去哪裡」與操作表「選擇 Board 進入」列，把 F01 `s-board` 改為 F07 `s-canvas`，並移除該畫面「待確認事項」裡指向這件事的 ⚠️。(5) 依 OQ-19（推翻 OQ-05）：`s-board` 資料段移除「看板成員清單」列（成員頭像清單改為 F07 `s-canvas` 上另一個獨立 `item`）；「拖曳成員頭像到卡片追加負責人」操作列維持不變（拖放目標仍是 `s-board` 上的卡片）；待確認事項移除 OQ-05 的 ⚠️，改標註「成員頭像清單已改為 F07 獨立 item，跨 item 拖曳機制待 F07 補充」。開工前重新讀一次 `ui-authoring-tasks.md` T7.02 的完成結果，確認 `s-canvas` 實際定案內容再動手，避免兩邊寫法對不上 | `s-board` 操作表不含「管理 Swimlane」「管理 Stage」；`s-board`「進入與離開」不含「待 F02 `s-board-list` 定案後回填」；`s-board` 資料段不含「看板成員清單」；`s-swimlane-list`／`s-stage-list`「進入與離開」指向 `s-canvas` 而非 `s-board`；`s-board-list`「完成後去哪裡」與操作表指向 `s-canvas`；`ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0`、`ui-check(.dev/F02-user-membership/ui-user-membership.md)=0`（皆含 `--spec` 全部模組與全部既有 ui 檔一起檢查） | T7.02 |
| D-10 | doing | [F02] 人工決策解除 OQ-11、確認 OQ-12、確認 OQ-13：`s-login` 是未登入時的應用程式入口，`s-signup` 透過 `s-login` 上的一個連結（例如「還沒有帳號？前往註冊」）進入；`s-signup` 建立帳號成功後導向 `s-login`（OQ-12 原暫定結論，人工確認維持，不變更內容）；`s-login` 登入成功後導向 `s-board-list`（OQ-13 原暫定結論，人工確認維持，不變更內容）。修正 `ui-user-membership.md`：(1) `s-login`「從哪裡進來」新增一項「未登入時的應用程式入口」，移除 OQ-11 的 ⚠️。(2) `s-login` 操作表新增一列「前往建立帳號」（觸發 `—`，純前端導覽，成功後開啟 `s-signup`）。(3) `s-signup`「從哪裡進來」改為「`s-login` 的『前往建立帳號』連結」，移除 OQ-11 的 ⚠️。(4) `s-signup`「完成後去哪裡」與相關操作表「成功後」欄位移除 OQ-12 的 ⚠️（內容維持「導向 `s-login`」不變，只是從「暫定」轉為「已確認」）。(5) `s-login`「完成後去哪裡」與相關操作表「成功後」欄位移除 OQ-13 的 ⚠️（內容維持「導向 `s-board-list`」不變）。(6) 三畫面「待確認事項」移除指向 OQ-11、OQ-12、OQ-13 的條目。之後若要支援 Google 登入，屬於全新的 Use Case（不在現有 `uc-login`／`uc-create-user` 範圍內），不在本任務處理，待後續 CR 另外定義 | `s-login`「進入與離開」含「應用程式入口」；操作表含「前往建立帳號」列；`s-signup`「進入與離開」不含 OQ-11／OQ-12 的 ⚠️；`s-login`「進入與離開」不含 OQ-13 的 ⚠️；相關「待確認事項」不含 OQ-11／OQ-12／OQ-13；`ui-check(.dev/F02-user-membership/ui-user-membership.md)=0`（含 `--spec` 全部模組） | — |
