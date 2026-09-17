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
| T1.02 | todo | [F01] 定案 `s-swimlane-list`（類型：列表）：對應 `uc-add-swimlane`／`uc-rename-swimlane`／`uc-reorder-swimlane`（新增、重新命名、拖曳排序都在同一張列表上操作） | `ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)` 對本畫面無 error；八段齊全 | T1.01 |
| T1.03 | todo | [F01] 定案 `s-swimlane-delete-dialog`（類型：對話框）：對應 `uc-delete-swimlane`（含目的泳道選擇的成功／失敗情境） | 同上，本畫面無 error | T1.02 |
| T1.04 | todo | [F01] 定案 `s-stage-list`（類型：列表）：對應 `uc-add-stage`／`uc-rename-stage`／`uc-reorder-stage`／`uc-set-stage-role` | 同上 | T1.03 |
| T1.05 | todo | [F01] 定案 `s-stage-delete-dialog`（類型：對話框）：對應 `uc-delete-stage`（含卡片轉移情境） | 同上 | T1.04 |
| T1.06 | todo | [F01] 定案 `s-board`（類型：流程／看板主畫面）：對應 `uc-move-card-swimlane`／`uc-move-card-stage`（拖曳卡片跨泳道／跨階段）；同時在「操作」表列出 F02 的 `uc-assign-card-owner-by-drag`（拖曳成員頭像到卡片指派負責人），跨模組引用、不重新定義 | 同上，且操作表含 `uc-assign-card-owner-by-drag` 一列 | T1.05 |
| T1.07 | todo | [F01] 定案 `s-card-add-dialog`（類型：表單）：對應 `uc-add-card` | 同上 | T1.06 |
| T1.08 | todo | [F01] 定案 `s-card-detail`（類型：表單）：對應 `uc-edit-card`／`uc-add-comment`；負責人相關欄位（顯示目前負責人、開啟指派）先標 `⚠️` 待確認、待確認事項寫「負責人欄位與指派入口待 F02 `s-card-assignee-picker` 定案後回填」，記入 OQ | 同上；待確認事項含上述 `⚠️` 條目；OQ 檔追加對應列 | T1.07 |
| T1.09 | todo | [F01] 定案 `s-card-delete-dialog`（類型：對話框）：對應 `uc-delete-card` | 同上 | T1.08 |
| T1.10 | todo | [F01] 收尾：`ui-check .dev/F01-basic-kanban/ui-kanban-basic.md` 0 error；過一遍 `DS-06`（寫入 uc 有沒有被引用）／`DS-07`（畫面有沒有被導向）warn，沒理由的回頭補 | `ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)=0`；PDCA 本則列出 `DS-06`／`DS-07` warn 清單與處理結果 | T1.09 |

## 階段 2：F02 user-membership（`.dev/F02-user-membership/spec-user-membership.md` → `ui-user-membership.md`）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T2.01 | todo | [F02] 建立 `ui-user-membership.md`：檔頭 ＋ 9 個畫面標題骨架（`s-signup`、`s-login`、`s-board-list`、`s-board-create-dialog`、`s-member-management`、`s-board-delete-dialog`、`s-card-assignee-picker`、`s-cards-by-assignee`、`s-activity-log`） | 同 T1.01 模式 | T1.10 |
| T2.02 | todo | [F02] 定案 `s-signup`（類型：表單）：對應 `uc-create-user` | 同 T1.02 模式 | T2.01 |
| T2.03 | todo | [F02] 定案 `s-login`（類型：表單）：對應 `uc-login`；`uc-logout` 併入本畫面的操作表（例如全域導覽列的登出動作，「觸發」欄仍填 `uc-logout`），不獨立開一個畫面 | 同上，操作表含 `uc-login` 與 `uc-logout` 兩列 | T2.02 |
| T2.04 | todo | [F02] 定案 `s-board-list`（類型：列表）：對應 `uc-view-board-list`；無權限狀態對應 `uc-reject-board-access-by-nonmember` | 同上 | T2.03 |
| T2.05 | todo | [F02] 定案 `s-board-create-dialog`（類型：表單）：對應 `uc-create-board` | 同上 | T2.04 |
| T2.06 | todo | [F02] 定案 `s-member-management`（類型：對話框／側欄）：對應 `uc-invite-member`／`uc-change-member-role`／`uc-remove-member`；三個 `uc-reject-invite-by-member`／`uc-reject-role-change-by-member`／`uc-reject-structure-change-by-member` 的拒絕情境放對應操作的「失敗時」欄 | 同上 | T2.05 |
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

## 階段 7：跨模組收尾

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T7.01 | todo | 六份 `ui-*.md` 一起跑 `./scripts/ui-check` 到 0 error；`./scripts/ui-check --report` 看畫面總表與追溯矩陣，`DS-06`（寫入 uc 沒被任何畫面引用）／`DS-07`（畫面沒被任何畫面導向，也不是模組入口）warn 逐項確認有理由（例如背景作業、確實是模組入口）或回頭補 | `ui-check(all)=0`；PDCA 本則列出 `DS-06`／`DS-07` 完整 warn 清單與逐項處理結果 | T6.03 |
| G1 | todo | 關卡：自我審查——依 `.dev/conventions/llm-review.md` L-09（「失敗時」是否對應 `fail`、「成功後」是否對應 `post`）與 `checks.md` DS-05（操作可用角色與 uc `roles` 是否一致）各抽查六個模組至少一個畫面；有偏差在對應 ui 檔直接修正並記錄。**手動模式**（`/loop`）：同一輪內完成，不開 D-xx 拖到下一輪，直接把本關卡標 `done`。**自動模式**（`run-ui-authoring-loop.sh`）：由獨立審查輪（`ui-authoring-review-prompt.md`）執行，偏差開成 `D-xx`（`todo`），審查後無待修項目才由驅動腳本建立 `runtime/gates/G1.approved`、下一輪才能把本關卡標 `done` | PDCA 或 `ui-authoring-review.md` 本則列出抽查的 6 個畫面與檢查結果；若有修正，修正後 `ui-check(all)=0` | T7.01 |
| T7.02 | todo | 最終確認並建立 `runtime/DONE`：`ui-check(all)=0`；六份 `ui-*.md` 都存在；任務清單除本任務外無 `todo`／`doing`／`blocked` | `runtime/DONE` 存在（不進版控）；PDCA 本則貼 `ui-check` 最終結果行 | G1 |

## 發現的任務（D-xx）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
