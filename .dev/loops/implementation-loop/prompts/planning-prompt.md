# planning-prompt（安排階段）

> 執行一次（或在規格遷移進度更新後重跑校正），產出／校正 `.state/tasks.md`。單一 agent、讀規則書 `iteration-prompt.md` 第 3 節（任務排序原則）後執行本提示詞。
> 這一輪**不寫程式碼**，只讀規格與設計稿、寫任務清單。

## 你的任務

1. 讀 `.dev/F0x-*/spec-*.md` 的「名詞定義」三張表（實體／欄位／關係）＋「其他名詞」，列出每個模組目前實際定義了哪些 Aggregate Root（有表格內容才算，空表視為未遷移）。
2. 讀 `.dev/F01-basic-kanban/ui-basic-kanban.md`（目前唯一存在的 `ui-*.md`）的 Screen 清單與各畫面操作表引用的 `uc-xxx`。
3. 讀下方「前端設計稿畫面清單」（已固定列出，來自 claude.ai Design 類型 Artifact，你不需要也不應該重新去解析該 Artifact 的內部機制，只需要把清單當作既有畫面依據）。
4. 依 `iteration-prompt.md` 第 3 節五條規則，把每個 Aggregate Root／畫面群組整理成一列任務，`T-xx` 由你排定依賴後的建議執行序（依賴要先於被依賴者，但驅動腳本實際會依「依賴是否 done」平行選取，排列順序只是給人看的參考序，不是唯一合法序）。
5. 校對／更新 `.state/tasks.md`：若既有任務列的描述、依賴、範圍已經涵蓋規格內容，不要無理由重寫；若規格自上次安排後有變（例如某模組剛完成遷移），更新該列，並在 `.state/decision-log.md` 記一則決策紀錄說明改了什麼、依據哪裡。
6. 任何找不到依據的地方（規格未遷移、ui 檔不存在、設計稿沒有對應畫面）：任務標 `blocked`，在 `.state/open-questions.md` 用 lesson-learned 的 OQ 格式追加一列，**不可以自己編一個「合理的」範圍去填補**。

## 前端設計稿畫面清單（固定資料，來源：claude.ai Design Artifact，35 個檔案，擷取自 `project/*.dc.html`）

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

沿用既有兩個 loop 的任務清單慣例（狀態值：`todo`／`doing`／`review-pending`／`blocked`／`done`；`G*` 關卡、`D-xx` 修正任務），每列另外要有「產出範圍」「依賴（已合併才算滿足）」兩欄。下方是目前依規格導出的種子資料，供你校對，不是要你從零重排：

```
T-00-scaffold   | 建立 kanban-core / kanban-spring / kanban-frontend 專案骨架（Gradle 多模組、pnpm 前端專案、CI 可跑但無業務邏輯） | 依賴：無 | 狀態：todo
T-01-be-user    | F02 User Aggregate Root（domain + port + application + web + persistence，不含 BoardMembership） | 依賴：T-00-scaffold | 狀態：todo
T-02-be-board   | F01 board Aggregate（board + swimlane + stage） | 依賴：T-01-be-user（board.created-by） | 狀態：todo
T-03-be-card    | F01 card Aggregate（card + comment） | 依賴：T-02-be-board、T-01-be-user（assignees/author） | 狀態：todo
T-04-be-board-membership | F02 BoardMembership + ActivityRecord | 依賴：T-02-be-board、T-01-be-user | 狀態：todo
T-05-be-board-clock | F04 Board Clock（board.clock-time/clock-status，改寫 board/card 事件的 occurredAt 來源） | 依賴：T-02-be-board、T-03-be-card | 狀態：todo
T-06-be-kanban-widgets | F03 唯讀 projection（Lead/Cycle Time、WIP、Throughput/CFD、到期提醒） | 依賴：T-02-be-board、T-03-be-card、T-05-be-board-clock | 狀態：blocked（spec 名詞定義尚未遷移，見 OQ）
T-07-be-workload | F05 唯讀 projection（Active Card 統計） | 依賴：T-03-be-card、T-04-be-board-membership | 狀態：blocked（spec 名詞定義尚未遷移，見 OQ）
T-08-be-feature-cr-board | F06 唯讀 projection（Feature/CR 卡標籤解讀） | 依賴：T-03-be-card | 狀態：blocked（spec 名詞定義尚未遷移，見 OQ）
T-09-be-canvas-layout | F07 canvas + item + viewport | 依賴：T-02-be-board、T-04-be-board-membership（viewport.user） | 狀態：todo
T-10-fe-shell   | 前端 app shell（路由、API client、登入態管理） | 依賴：T-00-scaffold、T-01-be-user | 狀態：todo
T-11-fe-auth    | Login、Signup | 依賴：T-10-fe-shell | 狀態：todo
T-12-fe-board-list | BoardList、BoardCreateDialog、BoardDeleteDialog、State*（BoardList 系列） | 依賴：T-10-fe-shell、T-02-be-board、T-04-be-board-membership | 狀態：todo
T-13-fe-board-detail | Main、PanelStage、CardAddDialog、CardDeleteDialog、CardDetail、StageDeleteDialog、SwimlaneDeleteDialog、AssigneePicker、State*（Board/CardDetail 系列） | 依賴：T-12-fe-board-list、T-03-be-card | 狀態：todo
T-14-fe-member-management | MemberManagement | 依賴：T-13-fe-board-detail、T-04-be-board-membership | 狀態：todo
T-15-fe-widgets | F03 對應畫面 | 依賴：T-13-fe-board-detail、T-06-be-kanban-widgets | 狀態：blocked（無 ui-kanban-widgets.md、無設計稿）
T-16-fe-workload | F05 對應畫面 | 依賴：T-12-fe-board-list、T-07-be-workload | 狀態：blocked（無 ui-workload.md、無設計稿）
T-17-fe-feature-cr-board | F06 對應畫面 | 依賴：T-13-fe-board-detail、T-08-be-feature-cr-board | 狀態：blocked（無 ui-feature-cr-board.md、無設計稿）
T-18-fe-canvas  | CanvasPanel | 依賴：T-13-fe-board-detail、T-09-be-canvas-layout | 狀態：todo（範圍限 spec「待釐清」以外的 Item/Viewport CRUD；與 F03 圖表元件整合部分不在範圍內，spec 本身尚未定義）
```

把以上種子資料寫入 `.state/tasks.md` 時，補上任務清單慣例要求的規則段（狀態值定義、挑選順序：依賴全 `done`（已合併）且 `doing` 中任務數 < 5 才能選、`G*` 關卡怎麼核准）；`blocked` 的四列要各自在 `.state/open-questions.md` 開一則 OQ，指向對應模組的 spec-migration 進度／缺失的 ui 檔。

## 收尾

完成後在 `.state/decision-log.md` 記一則決策紀錄，`.state/state.md` 覆寫 20 行內摘要（目前有幾個 `todo`／`blocked`、下一輪驅動腳本可以平行跑哪幾個任務）。commit 訊息 `[docs](loops) <摘要>`。
