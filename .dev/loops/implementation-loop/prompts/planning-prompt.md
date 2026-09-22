# planning-prompt（安排階段）

> 由驅動腳本在「無可執行任務」時自動重跑（見 `iteration-prompt.md` 第 0、8 節），也可在規格遷移進度更新後人工重跑校正；每次執行都是依現況校對、產出／校正 `.state/tasks.md`。單一 agent、讀規則書 `iteration-prompt.md` 第 3 節（任務排序原則）後執行本提示詞。
> 這一輪**不寫程式碼**，只讀規格與設計稿、寫任務清單。

## 你的任務

1. 讀 `.dev/F0x-*/spec-*.md` 的「名詞定義」三張表（實體／欄位／關係）＋「其他名詞」，列出每個模組目前實際定義了哪些 Aggregate Root（有表格內容才算，空表視為未遷移）。
2. 讀 `.dev/F01-basic-kanban/ui-basic-kanban.md`（目前唯一存在的 `ui-*.md`）的 Screen 清單與各畫面操作表引用的 `uc-xxx`。
3. 讀下方「前端設計稿畫面清單」（已固定列出，來自 claude.ai Design 類型 Artifact，你不需要也不應該重新去解析該 Artifact 的內部機制，只需要把清單當作既有畫面依據）。
4. 依 `iteration-prompt.md` 第 3 節五條規則，把每個 Aggregate Root／畫面群組整理成一列任務，`T-xx` 由你排定依賴後的建議執行序（依賴要先於被依賴者，但驅動腳本實際會依「依賴是否 done」平行選取，排列順序只是給人看的參考序，不是唯一合法序）。
5. 校對／更新 `.state/tasks.md`：若既有任務列的描述、依賴、範圍已經涵蓋規格內容，不要無理由重寫；若規格自上次安排後有變（例如某模組剛完成遷移），更新該列，並在 `.state/tasks/_planning/decision-log.md` 記一則決策紀錄說明改了什麼、依據哪裡。

5-1. 對每個已有 `done` 實例的主體：取 spec 目前該主體的 `@CR-xxx` 集合 A，取該主體所有 `done` 列備註「涵蓋 CR」的聯集 B。A − B 非空 → 追加修訂實例列 `<原ID>-rN`；為空 → 不動。主體最新實例狀態為 `doing`／`review-pending` 時本輪不追加。`todo` 且未開工的列若 spec 有變，直接更新該列的產出範圍與備註，不開修訂實例。
5-2. 一筆 CR 波及多個主體時，依 `iteration-prompt.md` 第 3 節第 2 條的主體依賴方向排各修訂實例的依賴。
5-3. CR 在 `.dev/CR.md` 有列但 spec 沒打 `@CR-xxx` tag、或 tag 指向的實體找不到 → 該修訂實例標 `blocked`、開 `OQ-PLAN-xx`，不可自行推斷範圍。
5-4. 前端畫面群組以 `ui-*.md` 的 CR tag 做同樣比對。

6. 任何找不到依據的地方（規格未遷移、ui 檔不存在、設計稿沒有對應畫面）：任務標 `blocked`（寫 `.state/tasks/<task-id>/status`），在 `.state/tasks/_planning/open-questions.md` 用 lesson-learned 的 OQ 格式追加一則（ID 格式 `OQ-PLAN-<兩位數>`），**不可以自己編一個「合理的」範圍去填補**。

## 前端設計稿畫面清單（檔案已匯出到 `.dev/ui-prototype/`，檔案與 Screen ID 的正式對照見該目錄 `README.md`；下表是當初排任務用的推測）

| 檔名 | 推測對應模組／用途 |
|---|---|
| `Login.dc.html` | F02：登入 |
| `Signup.dc.html` | F02：註冊 |
| `BoardList.dc.html` | F01/F02：看板清單 |
| `BoardCreateDialog.dc.html` | F01：建立看板 |
| `BoardDeleteDialog.dc.html` | F01：刪除看板 |
| `StateBoardListLoading.dc.html`／`StateBoardListEmpty.dc.html`／`StateBoardListDenied.dc.html` | F01/F02：看板清單的載入中／空清單／無權限狀態 |
| `Main.dc.html` | F01：看板主畫面（Swimlane × Stage 版面） |
| `PanelStage.dc.html` | F01：Stage 欄位面板 |
| `StageDeleteDialog.dc.html`／`SwimlaneDeleteDialog.dc.html` | F01：刪除 Stage／Swimlane |
| `CardAddDialog.dc.html`／`CardDeleteDialog.dc.html`／`CardDetail.dc.html` | F01：卡片新增／刪除／詳情 |
| `StateBoardLoading.dc.html`／`StateCardDetailLoading.dc.html` | F01：看板／卡片詳情載入中狀態 |
| `AssigneePicker.dc.html` | F02：負責人多選 |
| `MemberManagement.dc.html` | F02：看板成員管理 |
| `CanvasPanel.dc.html` | F07：Canvas 版面（Item 放置／移動／縮放／層序） |

**沒有對應畫面**：F03（kanban-widgets）、F05（workload）、F06（feature-cr-board）目前設計稿裡沒有畫面，且這三個模組也沒有 `ui-*.md`——這三塊前端任務必須標 `blocked`，不可以憑 F01 既有畫面風格臆測版面。

## 任務清單格式（`.state/tasks.md`）

沿用既有兩個 loop 的任務清單慣例（狀態值：`todo`／`doing`／`review-pending`／`blocked`／`done`；`G*` 關卡、`D-xx` 修正任務），每列另外要有「產出範圍」「依賴（已合併才算滿足）」兩欄。

**`.state/tasks.md` 沒有狀態欄**：表格欄位固定為 `| ID | 產出範圍 | 依賴（需已合併） | 備註 |`，任務狀態寫在 `.state/tasks/<task-id>/status`（單行檔，不存在＝`todo`；只有要標 `blocked` 時才需要你建立）；`D-xx` 在各任務的 `fixes.md`；備註欄固定記「涵蓋 CR：CR-xxx, CR-yyy」（見 `iteration-prompt.md` 第 2 節）。原因見 `iteration-prompt.md` 第 2.1 節（並行 worktree 合併衝突）。**不可以改已經存在的 `status` 檔**（那是驅動腳本／Dev／Review 的執行進度），除非是把已補齊依據的 `blocked` 改回 `todo`。

格式範例（僅示範格式，實際內容以 `.state/tasks.md` 現況與規格校對結果為準）：

```
| ID | 產出範圍 | 依賴（需已合併） | 備註 |
|---|---|---|---|
| T-03-be-card | F01 card Aggregate（card + comment） | T-02-be-board、T-01-be-user（assignees/author） | 涵蓋 CR：CR-001, CR-002 |
| T-06-be-kanban-widgets | F03 唯讀 projection（Lead/Cycle Time、WIP、Throughput/CFD、到期提醒） | T-02-be-board、T-03-be-card、T-05-be-board-clock | blocked（spec 名詞定義尚未遷移，見 OQ-PLAN-xx）；涵蓋 CR：無 |
| T-03-be-card-r2 | CR-011：卡片目的 Swimlane/Stage 不存在時補上 pre/fail，只改此差異 | T-03-be-card（需已合併）、T-05-be-board-clock-r1 | 涵蓋 CR：CR-001, CR-002, CR-011 |
```

校對既有 `.state/tasks.md` 時，確認以下規則段仍在表格前，且與 `iteration-prompt.md` 一致；缺漏或不一致時補上／修正即可：狀態值定義、挑選順序（依賴全 `done`（已合併）且 `doing` 中任務數 < 5 才能選）、`G*` 關卡怎麼核准、`blocked` 要開 OQ。新增或校正 `blocked` 列時，各自在 `.state/tasks/_planning/open-questions.md` 開一則 OQ，指向對應模組的 spec-migration 進度／缺失的 ui 檔／CR 依據缺口（見步驟 5-3）。

## 收尾

完成後在 `.state/tasks/_planning/decision-log.md` 記一則決策紀錄，`.state/tasks/_planning/state.md` 覆寫 20 行內摘要（目前有幾個 `todo`／`blocked`、下一輪驅動腳本可以平行跑哪幾個任務）。commit 訊息 `[docs](loops) <摘要>`。所有輸出（含最後回覆）一律繁體中文 zh-TW。
