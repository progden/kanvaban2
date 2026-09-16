# spec 遷移任務清單（Task Ledger）

本檔是 spec 遷移 loop **唯一的任務來源**。規則（由 `verify-spec-migration.sh` 以 `spec-migration-tools.py check-ledger` 自動檢查，違反即判定本輪失敗）：

- **T／G 任務只有人工可以新增、刪除或改寫**。執行輪與審查輪只能修改「狀態」欄；任務描述、驗收條件、依賴一律不可改。
- 執行輪發現計畫外的必要工作：在「發現的任務」表追加 `D-xx`，狀態固定 `proposed`，**不可自行開工**；由審查輪或人工改為 `todo`（或 `rejected`）。審查輪可以直接追加 `todo` 的 D-xx。
- 狀態：`todo` 待做／`doing` 進行中（上一輪未完成）／`done` 完成／`blocked` 只用於環境限制（見規則書「假設分級」，只有人工可以解除）／`proposed` 待審／`rejected` 不做。
- 挑選順序（`spec-migration-tools.py actionable`）：`doing` → `D-xx` 的 `todo` → 依表格順序第一個 `todo`；依賴必須全部 `done`。
- `G*` 是關卡：loop 會先跑審查輪；審查沒有留下待修 D-xx 就由腳本建立 `runtime/gates/<G編號>.approved` 自動核准（`AUTO_APPROVE_GATES=0` 時改為人工建立），下一個執行輪才可將它標為 `done`。
- 「任務」欄開頭的 `[F0x]` 標示任務對應的模組 spec；驗證腳本用它找「`doing` 任務的檔案」（檢查 [9]）。
- 「驗收條件」中的**機械條件**由 `tools accept-check` 判定，驗證腳本每輪對所有 `done` 任務重驗：
  - `errors(F01)=0`：全部 spec 一起解析時，F01 spec 的 error 數為 0。
  - `errors(F01#<Feature 名>)=0`：同上，只計該 Feature 所在 `## Feature:` 段落內的 error。`<Feature 名>` 是 gherkin 區塊裡 `Feature:` 後的名稱（遷移不會改它）。
  - `errors(F01;GH-08,REF-03)=0`：同上，只計列出的檢查 ID。
  - `errors(all)=0`：全部 spec 的 error 數為 0。
  - `crcheck(CR-005)=0`：`./scripts/cr-check --base <runtime/baseline> --cr CR-005` 通過。
- 所有任務都隱含「`gherkin-diff`、`tag-diff`、`changelog-check` 通過」（驗證腳本每輪對全部 spec 檢查），驗收條件不再重複寫。

---

## 階段 0：準備

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T0.01 | done | 確認 baseline 與起點：`runtime/baseline` 由 loop 腳本寫入，確認它存在且是某個 commit；確認六份 `legacy-spec-*.md` 備份存在；把 `tools errors-json` 的結果（每個檔的 error 數）貼進 PDCA 作為起點 | PDCA 本則 Check 含 baseline hash、六份備份檔名、各檔 error 數與 `spec-check` 結果行 | — |
| T0.02 | done | 建立 `.dev/CR.md`：表頭依 `cr-convention.md` §6；登記 CR-001～CR-004（依各 spec 變更紀錄填標題、類型、日期、影響模組，狀態「處理完成」，影響 ID 先空）與 CR-005「規格格式遷移至 usecase 區塊」（類型「變更」、狀態「修改規格」、影響模組六份 spec，影響 ID 先空）；每筆下方寫背景／變更內容／驗收標準 | `.dev/CR.md` 存在且主表有 CR-001～CR-005 五列；`errors(F01;REF-06)=0` | T0.01 |

## 階段 1：F01 作為範本

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T1.01 | done | [F01] 遷移程序 1～6：狀態行、名詞定義三張表＋其他名詞、角色定義、三個 Feature 的「身為」行與 `## Feature:` 標題（實體 ID 含所屬 Aggregate；原名詞表的說明文字分流到四張表，不刪） | `errors(F01;GH-08,REF-03,REF-04,REF-05,REF-09)=0`；原名詞表每一列說明都能在新表找到（PDCA Check 逐列對照） | T0.02 |
| T1.02 | done | [F01] 「Swimlane 管理」Feature 的 usecase 區塊＋該 Feature 全部 Scenario 的 `@uc-`／`@fail-` tag 與 Aggregate 註解對齊（遷移程序 7～9） | `errors(F01#Swimlane 管理)=0` | T1.01 |
| T1.03 | done | [F01] 同上，「Stage（階段）管理」 | `errors(F01#Stage（階段）管理)=0` | T1.02 |
| T1.04 | done | [F01] 同上，「Card（卡片）編輯」 | `errors(F01#Card（卡片）編輯)=0` | T1.03 |
| T1.05 | done | [F01] 遷移程序 10～11：正文反引號清理（程式碼名稱改「」）、`design.md` 稱呼改為 `design-kanban-basic.md`、變更紀錄改四欄格式並追加 CR-005 一列 | `errors(F01;REF-01,REF-06)=0`；變更紀錄最後一列票號為 CR-005 | T1.04 |
| T1.06 | done | [F01] 收尾：F01 全檔 0 error，修掉剩餘項目 | `errors(F01)=0` | T1.05 |
| G1 | done | 關卡：審查輪檢視 F01 是否可作為其餘模組的範本（usecase 拆分粒度、pre／post 措辭、實體粒度、角色表） | 腳本建立 `runtime/gates/G1.approved` | T1.06 |

## 階段 2：F02～F06（實體與角色跨模組共用，後面的模組只引用、不重定義）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T2.01 | done | [F02] 遷移程序 1～6：狀態行、名詞／角色表（F01 已定義的實體不重列；`boardMembership` 這類 camelCase 改成 kebab 實體 ID；Owner／Member 權限有寫明才分角色）、七個 Feature 的「身為」行與 `## Feature:` 標題 | `errors(F02;GH-08,REF-03,REF-04,REF-05,REF-08,REF-09)=0` | G1 |
| T2.02 | done | [F02] 「建立使用者帳號」usecase 區塊＋tag＋Aggregate 註解 | `errors(F02#建立使用者帳號)=0` | T2.01 |
| T2.03 | done | [F02] 「使用者登入與登出」usecase 區塊＋tag＋Aggregate 註解 | `errors(F02#使用者登入與登出)=0` | T2.02 |
| T2.04 | done | [F02] 「Board 建立與成員邀請」usecase 區塊＋tag＋Aggregate 註解 | `errors(F02#Board 建立與成員邀請)=0` | T2.03 |
| T2.05 | done | [F02] 「Board 權限管理」usecase 區塊＋tag＋Aggregate 註解 | `errors(F02#Board 權限管理)=0` | T2.04 |
| T2.06 | done | [F02] 「Board 存取權限」usecase 區塊＋tag＋Aggregate 註解 | `errors(F02#Board 存取權限)=0` | T2.05 |
| T2.07 | done | [F02] 「卡片負責人指派」usecase 區塊＋tag＋Aggregate 註解 | `errors(F02#卡片負責人指派)=0` | T2.06 |
| T2.08 | done | [F02] 「檢視看板活動紀錄」usecase 區塊＋tag＋Aggregate 註解；若 F01／F02 各 uc 有 `emits` 活動紀錄事件，這裡的 uc 以 `requires` 接上 | `errors(F02#檢視看板活動紀錄)=0` | T2.07 |
| T2.09 | done | [F02] 遷移程序 10～11 與收尾：正文反引號清理（含「Aggregate 事件盤點」「實作備註」段落）、`design.md` 稱呼改實際檔名、變更紀錄改格式並追加 CR-005；F02 全檔 0 error | `errors(F02)=0`；`errors(F01)=0` | T2.08 |
| T2.10 | done | [F03] 遷移程序 1～6：狀態行、名詞／角色表（只引用 F01／F02 已定義的實體與角色）、四個 Feature 的「身為」行 | `errors(F03;GH-08,REF-03,REF-04,REF-05,REF-08,REF-09)=0` | T2.09 |
| T2.11 | done | [F03] 「Cycle Time 與 Lead Time 分析」與「WIP 與 Aging WIP 監控」兩個 Feature 的 usecase 區塊＋tag＋Aggregate 註解（讀取類 uc） | `errors(F03#Cycle Time 與 Lead Time 分析)=0`；`errors(F03#WIP 與 Aging WIP 監控)=0` | T2.10 |
| T2.12 | done | [F03] 「Throughput 與累積流量圖」與「截止日期提醒」兩個 Feature 的 usecase 區塊＋tag＋Aggregate 註解 | `errors(F03#Throughput 與累積流量圖)=0`；`errors(F03#截止日期提醒)=0` | T2.11 |
| T2.13 | todo | [F03] 遷移程序 10～11 與收尾，F03 全檔 0 error | `errors(F03)=0` | T2.12 |
| T2.14 | todo | [F04] 遷移程序 1～6：狀態行、名詞／角色表、「身為」行；「決議紀錄」段落保留 | `errors(F04;GH-08,REF-03,REF-04,REF-05,REF-08,REF-09)=0` | T2.13 |
| T2.15 | todo | [F04] 「看板時間管理」usecase 區塊＋tag＋Aggregate 註解 | `errors(F04#看板時間管理)=0` | T2.14 |
| T2.16 | todo | [F04] 遷移程序 10～11 與收尾，F04 全檔 0 error | `errors(F04)=0` | T2.15 |
| T2.17 | todo | [F05] 遷移程序 1～6：狀態行、名詞／角色表、「身為」行 | `errors(F05;GH-08,REF-03,REF-04,REF-05,REF-08,REF-09)=0` | T2.16 |
| T2.18 | todo | [F05] 「人員工作量檢視」usecase 區塊＋tag＋Aggregate 註解（拖曳追加負責人的 Scenario 屬於哪個 uc，依 F02「卡片負責人指派」已定義的 uc 判斷，不重複定義） | `errors(F05#人員工作量檢視)=0` | T2.17 |
| T2.19 | todo | [F05] 遷移程序 10～11 與收尾，F05 全檔 0 error | `errors(F05)=0` | T2.18 |
| T2.20 | todo | [F06] 遷移程序 1～6：狀態行、名詞／角色表、「身為」行 | `errors(F06;GH-08,REF-03,REF-04,REF-05,REF-08,REF-09)=0` | T2.19 |
| T2.21 | todo | [F06] 「Feature／CR 追蹤表」usecase 區塊＋tag＋Aggregate 註解（Scenario 裡的卡片標籤 `"F01"` 等是範例資料，不是功能引用） | `errors(F06#Feature／CR 追蹤表)=0` | T2.20 |
| T2.22 | todo | [F06] 遷移程序 10～11 與收尾（原變更紀錄是兩欄表格，改四欄），F06 全檔 0 error | `errors(F06)=0` | T2.21 |

## 階段 3：跨模組收尾

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| T3.01 | todo | 全部 spec 一起跑 `spec-check` 到 0 error（REF-08 重複定義、跨模組引用、事件配對） | `errors(all)=0` | T2.22 |
| T3.02 | todo | `.dev/CR.md` 回填 CR-001～CR-004 的影響 ID（依各 spec 掛該 `@CR-` 的 Scenario 所屬 uc 與相關實體）；CR-005 影響 ID 列出本次遷移改動到的全部實體與 uc；跑 `cr-check` 到通過 | `crcheck(CR-005)=0`；`errors(all)=0` | T3.01 |
| T3.03 | todo | 把 `./scripts/spec-check --report` 的輸出貼進 PDCA 供人工檢視；PDCA 同一則依模組整理 OQ 檔的全部 OQ-xx（編號、一句話、採用的選項） | PDCA 本則含 CRUD 矩陣、角色 × UseCase 矩陣、事件表與 OQ 摘要 | T3.02 |
| G2 | todo | 關卡：審查輪做 `llm-review.md` L-01、L-02、L-05、L-06 抽查（每模組至少一個 uc），偏差開 D-xx 或記入審查紀錄 | 腳本建立 `runtime/gates/G2.approved` | T3.03 |
| T3.04 | todo | 最終確認並建立 `runtime/DONE` | `errors(all)=0`；`crcheck(CR-005)=0`；任務清單除本任務外無 todo／doing／blocked／proposed | G2 |

## 發現的任務（D-xx）

| ID | 狀態 | 任務 | 驗收條件 | 依賴 |
|----|------|------|----------|------|
| D-01 | done | [F01] 活動紀錄的寫入要反映在 crud 與 Aggregate 註解，並補記 OQ：採用「活動紀錄屬於被操作的 Aggregate」（依 F02 備份「實作備註」：`Board.activityLog`／`Card.activityLog`）。Swimlane 管理與 Stage（階段）管理的 9 個 uc，`crud` 加上 `board` 的 U（`uc-add-swimlane`、`uc-add-stage` 由 `board: R` 改 `board: RU`），這 9 個 uc 的成功 Scenario 在 Aggregate 註解補回 `board: read, write`（還原原規格的 board 寫入）；失敗 Scenario 註解不變。在 OQ 檔追加一列（模組 F01，情況：活動紀錄不另立 uc、不 emits 事件，改由各 uc 的 post 與 `board` 寫入表達；採用上述選項；依據：F01「重新命名 Swimlane」等 Scenario 與 F02「檢視看板活動紀錄」的實作備註），F01 `## 待釐清` 加一行指向該 OQ；T2.01 定義 ActivityRecord 實體時要依這個 OQ 決定是否回頭改 F01 的 crud | `errors(F01#Swimlane 管理)=0`；`errors(F01#Stage（階段）管理)=0`；OQ 檔有一列模組為 F01、內容提到活動紀錄；9 個 uc 的 `crud` 都含 `board` 的 U | — |
| D-02 | done | [F01] 修正 `uc-delete-card` 的 pre／post（採用「pre 寫成功所需條件：使用者確認刪除；取消屬於 p2 不成立」）：`pre.p2` 由「使用者於刪除 `card` 的確認訊息中選擇取消」改為「使用者於刪除 `card` 的確認訊息中確認刪除」；`fail.p2` 改為「不刪除，該 `card` 仍存在於看板中，資料不變」；`post` 第一句改為「該 `card` 從看板中移除」，並補上「該操作被記錄為 `card` 的一筆活動紀錄，包含操作人與操作時間」（對應「刪除卡片需要確認」Scenario 最後一步）。「取消刪除卡片」Scenario 的 tag（`@uc-delete-card @fail-p2`）與註解（`card: read`）不變。OQ-01、OQ-02 不可改，改在 OQ 檔末尾追加一列（模組 F01，情況：OQ-02 採用內容中 `pre.p2` 措辭方向寫反，成功 Scenario 反而不滿足 pre；採用：p2 改為確認刪除、取消為 p2 不成立；依據：F01「刪除卡片需要確認」「取消刪除卡片」Scenario），F01 `## 待釐清` 追加一行指向該 OQ | `errors(F01)=0`；`errors(F01#Card（卡片）編輯)=0`；F01 spec 內 `uc-delete-card` 區塊不再含「選擇取消」字樣、含「確認刪除」與「活動紀錄」；OQ 檔最後一列模組為 F01、內容提到 `uc-delete-card` | — |
| D-03 | done | [F02] 修正 OQ-04 的角色分組，改成跨模組共用 F01 已定義的角色（採用「看板上的卡片操作沿用 F01 的 `r-user`（看板使用者），`r-system-user` 只給不涉及特定看板的帳號與看板清單操作」）：「卡片負責人指派」「檢視看板活動紀錄」兩個 Feature 的 gherkin 標頭改為「身為 看板使用者」（F02 角色表不重列 `r-user`，直接引用 F01 的定義）；`r-system-user` 的說明改為只涵蓋建立帳號、登入登出、檢視自己有權限的 Board，刪除「指派卡片負責人、檢視看板活動紀錄」；「Board 存取權限」維持「身為 系統使用者」；T2.07、T2.08 的 uc `roles` 依此填 `[r-user]`。OQ-04 那一列不可改，改在 OQ 檔末尾追加一列（模組 F02，情況：OQ-04 沒有考慮沿用 F01 的 `r-user`，另外新設通用角色，違反階段 2「角色跨模組共用」，而且 `r-system-user` 的說明寫進原文沒有的權限；採用：上述選項；依據：F01 `r-user`「可操作看板（Swimlane、Stage）與卡片的一般使用者」、F02 備份「卡片負責人指派」「檢視看板活動紀錄」的原標頭「身為看板的成員」「身為 Board 的成員」，以及後者 Background 由 Owner 示範），F02 `## 待釐清` 追加一行指向該 OQ（OQ-04 那行保留） | `errors(F02;GH-08,REF-03,REF-04,REF-05,REF-08,REF-09)=0`；`errors(F01)=0`；`errors(F02#建立使用者帳號)=0`；`errors(F02#使用者登入與登出)=0`；F02 spec 中「Feature: 卡片負責人指派」「Feature: 檢視看板活動紀錄」下一行都是「  身為 看板使用者」；F02 角色表沒有 `r-user` 列，`r-system-user` 那列不含「指派卡片負責人」「活動紀錄」；OQ 檔最後一列模組為 F02、內容提到 OQ-04 與 `r-user` | — |
| D-04 | done | [F02] 修正 T2.04～T2.06 的兩處 usecase 內容（只改 usecase 區塊、待釐清與 OQ 檔，gherkin 不動）。(1) 活動紀錄措辭依 OQ-01「活動紀錄屬於被操作的 Aggregate」與本檔「Aggregate 事件盤點」表（邀請／移除／升級成員的 Aggregate 是 BoardMembership）：`uc-invite-member`、`uc-change-member-role`、`uc-remove-member` 的 post 活動紀錄句，把開頭「該 `board` 產生一筆活動紀錄」改為「該操作被記錄為 `board-membership` 的一筆活動紀錄」，後面「記錄操作人與…（例如「…」）」原文保留；三個 uc 的 `crud` 不加 `board`（`uc-create-board` 不改）。(2) 拒絕類 uc 的 `roles` 不留空（採用「依 Background／Feature 標頭填上被拒絕的操作者角色」）：`uc-reject-invite-by-member`、`uc-reject-role-change-by-member`、`uc-reject-structure-change-by-member` 改為 `roles: [r-board-member]`（依據：「Board 權限管理」Background「"雅婷" 是這個 Board 的 Member」，以及三個 Scenario 都由 "雅婷" 操作）；`uc-reject-board-access-by-nonmember` 改為 `roles: [r-system-user]`（依據：「Board 存取權限」標頭「身為 系統使用者」）。OQ-06 那一列不可改，改在 OQ 檔末尾追加一列（模組 F02，情況：OQ-06 與 T2.06 讓拒絕類 uc 的 roles 留空，看不出被拒絕的是誰；採用上述 (2)；依據同上），F02 `## 待釐清` 追加一行指向該 OQ | `errors(F02#Board 建立與成員邀請)=0`；`errors(F02#Board 權限管理)=0`；`errors(F02#Board 存取權限)=0`；`errors(F01)=0`；F02 spec 中「該 `board` 產生一筆活動紀錄」只剩 1 句（`uc-create-board` 的），且有 3 句含「`board-membership` 的一筆活動紀錄」；F02 spec 不再有 `roles: []`；OQ 檔最後一列模組為 F02、內容提到 OQ-06 與 roles | — |
| D-05 | done | [F02] 修正 `uc-view-board-activity-log` 的 post 第一句，讓它跟 OQ-01、D-04「活動紀錄屬於被操作的 Aggregate」一致（只改 usecase 區塊，gherkin 與 Aggregate 註解不動）。採用「看板活動紀錄是合併檢視，不是 `board` 自己的紀錄」：把「`board` 的活動紀錄依時間由新到舊列出，每一筆都顯示操作人與動作內容」改為「該看板的活動紀錄（合併 `board` 與 `board-membership` 的紀錄）依時間由新到舊列出，每一筆都顯示操作人與動作內容」；第二句不變；`crud` 維持 `{board: R, board-membership: R}`。不需新增 OQ | `errors(F02)=0`；`errors(F01)=0`；F02 spec 不再含「`board` 的活動紀錄依時間」字樣，且含「合併 `board` 與 `board-membership` 的紀錄」 | — |
