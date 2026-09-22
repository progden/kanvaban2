# T-18-fe-widgets open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-18-fe-widgets-01

[Level: F03 四個儀表板 item.component 值]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：**已由 OQ-T-18-fe-widgets-02 取代並解除（2026-09-22）**，見該則解除說明

情況：【推論＋所本原文】

`.dev/F07-canvas-layout/ui-canvas-layout.md` 第 80 行逐字：『⚠️ 依 OQ-49，F03 四個圖表畫面、F04 時鐘控制畫面、F05 工作量儀表板、F06 追蹤表畫面，人工已確認也是 `s-canvas` 上可放置的獨立 `item`（跟看板本體一樣，不透過其他畫面的操作列進入）；但這些各自的 `item.component` 對應值、如何從既有畫布新增這類元件（使用者操作流程）spec「待釐清」尚未定義，待整合CR定案』。

`.dev/F07-canvas-layout/spec-canvas-layout.md` 欄位表 `item.component` 定義逐字：『元件本體的識別碼，由元件所屬模組定義；本模組不解讀其內容。看板本體固定為實體ID `board`；其餘元件的值待各自所屬模組實作對應Item時決定』。

推論：本任務（T-18-fe-widgets）就是「各自所屬模組實作對應 Item」的時機，依上述欄位定義，`item.component` 的值本來就該由本任務決定；但這個值一旦定案，日後 OQ-49 對應的整合 CR 若採用不同命名，需要一次遷移既有畫布上已放置的這幾種 item（改 `item.component` 值），屬於覆蓋既有資料的風險，而非違反已定稿條文本身。

問題：F03 四個儀表板 item 的 `item.component` 字串值該取什麼，才不會與 OQ-49 之後定案的整合 CR 命名衝突？

選項：A. 採用本任務已實作的做法：直接用 Screen ID 字串（`s-cycle-lead-time-dashboard`、`s-wip-dashboard`、`s-throughput-cfd-dashboard`、`s-duedate-reminder`）作為 component 值，理由是這是規格裡唯一已存在、跨模組不會撞號的識別碼，之後 OQ-49 整合 CR 若要改名，只需一次資料遷移；B. 先不定案，等 OQ-49 整合 CR 出來後才實作這四個 item 的內容元件（會讓 T-18 整個 blocked，不符合任務排程，且會拖到 T-19／T-20 依賴的 canvas item 掛載慣例）。

## OQ-T-18-fe-widgets-02

[Level: F03 四個儀表板 item.component 值]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 2 輪（2026-09-22）
- 狀態：待處理

本則取代 OQ-T-18-fe-widgets-01（原則引文刪節）。

情況：【推論＋所本原文】

`.dev/F07-canvas-layout/ui-canvas-layout.md` 第 80 行逐字：『依 OQ-49，F03 四個圖表畫面、F04 時鐘控制畫面、F05 工作量儀表板、F06 追蹤表畫面，人工已確認也是 `s-canvas` 上可放置的獨立 `item`（跟看板本體一樣，不透過其他畫面的操作列進入）；但這些各自的 `item.component` 對應值、如何從既有畫布新增這類元件（使用者操作流程）spec「待釐清」尚未定義，待整合 CR 定案』。

`.dev/F07-canvas-layout/spec-canvas-layout.md` 欄位表 `item.component` 定義逐字：『元件本體的識別碼，由元件所屬模組定義；本模組不解讀其內容。看板本體固定為實體 ID `board`（見「看板畫布初始化」Feature；不用 UI 層的 Screen ID，spec 不引用 ui，見 `docs-convention.md` 第 3 節）；其餘元件的值待各自所屬模組實作對應 Item 時決定。元件模組定案後改為 ref，元件與元素的生命週期連動（誰刪誰）屆時一併補』。

推論：欄位定義把「看板本體」的值固定為實體 ID `board`，並在括號裡明講理由是『不用 UI 層的 Screen ID，spec 不引用 ui』；這是唯一已定案的先例，方向是優先採用 spec 層識別碼而非 ui 層 Screen ID。但 F03 四個儀表板本身在 spec-kanban-widgets.md 沒有 entity／aggregate，只有六個 `uc-view-*`，且與四個 Screen 不是一對一（`s-wip-dashboard` 同時對應 `uc-view-wip`／`uc-view-aging-wip`，`s-throughput-cfd-dashboard` 同時對應 `uc-view-throughput`／`uc-view-cfd`），spec 沒有定義「一組 usecase 合成一個可放置元件」這個分組概念的識別碼；這個分組本身是 ui 層（ui-kanban-widgets.md 的 Screen）才有的概念。因此「不用 Screen ID」這個先例的適用前提（spec 本身已有對應識別碼可用）在 F03 這四個 item 上不成立。

問題：F03 四個儀表板 item 的 `item.component` 字串值該取 Screen ID，還是改用 spec 層的 `uc-` 識別碼（需另外解決分組問題）？

選項：A. 沿用本任務已實作的做法：用 Screen ID 字串（`s-cycle-lead-time-dashboard`／`s-wip-dashboard`／`s-throughput-cfd-dashboard`／`s-duedate-reminder`）；理由：spec 沒有對應「一組 uc 合成一個 item」的識別碼，四個 Screen 分組本身是 ui-kanban-widgets.md 定義的概念，Screen ID 是目前唯一能精準對應這四個畫面單位、且跨模組不會撞號的識別碼；代價：OQ-49 整合 CR 若改用別的命名，需一次遷移既有畫布上已放置的 item。B. 改用 spec 層的 `uc-` 識別碼，例如取每組第一個 uc（`uc-view-cycle-lead-time`／`uc-view-wip`／`uc-view-throughput`／`uc-view-duedate-reminder`）當代表值；理由：正面貫徹『不用 UI 層的 Screen ID』的先例方向；代價：uc 與 item 不是一對一（`uc-view-wip` 與 `uc-view-aging-wip` 同屬一個 item、`uc-view-throughput` 與 `uc-view-cfd` 同屬一個 item），用其中一個 uc 代表整組會讓值本身看不出這個 item 其實承載兩個 uc，容易誤導；且仍要另外決定這個「代表值」的取捨規則，spec 沒有依據可循，屬於本任務自創規則，跟直接用 Screen ID 相比並沒有更貼近 spec。C. 等 OQ-49 整合 CR 定案後才實作這四個 item 的內容元件（與 OQ-T-18-fe-widgets-01 選項 B 相同）；理由：徹底避免任何命名日後需要遷移；代價：T-18 整個 blocked，不符合任務排程，也會拖到依賴 canvas item 掛載慣例的下游任務。

## OQ-T-18-fe-widgets-03

[Level: item.component 命名規則（跨 T-17／T-19／T-20）]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-ambiguous
- 開立：Review 第 2 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】

`.dev/F07-canvas-layout/spec-canvas-layout.md` 第 31 行 `item.component` 欄位說明逐字：『元件本體的識別碼，由元件所屬模組定義；本模組不解讀其內容。看板本體固定為實體 ID `board`（見「看板畫布初始化」Feature；不用 UI 層的 Screen ID，spec 不引用 ui，見 `docs-convention.md` 第 3 節）；其餘元件的值待各自所屬模組實作對應 Item 時決定。元件模組定案後改為 ref，元件與元素的生命週期連動（誰刪誰）屆時一併補』。

`.dev/loops/implementation-loop/.state/tasks.md` 第 45 行逐字：『| T-17-fe-clock-control | `s-board-clock-control`（F04，Canvas item） | T-13-fe-canvas-shell、T-05-be-board-clock | 原任務清單遺漏，2026-09-18 校正時補上；涵蓋 CR：無 |』。

`.dev/loops/implementation-loop/.state/tasks.md` 第 47 行逐字：『| T-19-fe-workload | `s-workload-dashboard`（Canvas item）＋ `s-cards-by-assignee`（從前者點擊進入） | T-13-fe-canvas-shell、T-07-be-workload | 原標 blocked 已解除，見 OQ-IMPL-05「解除說明」；依賴改為 T-13；涵蓋 CR：無 |』。

`.dev/loops/implementation-loop/.state/tasks.md` 第 48 行逐字：『| T-20-fe-feature-cr-board | `s-feature-cr-board`（Canvas item） | T-13-fe-canvas-shell、T-08-be-feature-cr-board | 原標 blocked 已解除，見 OQ-IMPL-06「解除說明」；依賴改為 T-13；涵蓋 CR：無 |』。

推論：本則補充 OQ-T-18-fe-widgets-02（該則只討論 F03 這四個值本身），不取代它。T-18 是第一個實際決定 `item.component` 值的前端任務（看板本體的 `board` 是 spec 直接定案、非任務自行決定），因此它選的命名方式會成為 T-17、T-19、T-20 三個尚未開工的 Canvas item 任務的既成慣例——這三列的產出範圍都是 Canvas item，都會面對同一個「component 值取什麼」的問題，而 `.state/tasks.md` 裡沒有任何一列的產出範圍涵蓋 OQ-49 的整合 CR。若人工日後在整合 CR 採用與 Screen ID 不同的命名，遷移代價不只是 OQ-T-18-fe-widgets-02 選項 A 底下寫的「本任務四個 item」，而是四個任務、至少七個 Canvas item 的註冊值與既有畫布資料。

問題：`item.component` 的命名規則要現在就由人工統一定案（讓 T-17／T-19／T-20 照同一條規則實作），還是讓各任務各自沿用 T-18 的 Screen ID 慣例、等 OQ-49 整合 CR 時一次遷移？

選項：A. 現在由人工定一條跨模組的 `item.component` 命名規則（例如一律用 Screen ID，或一律用 spec 層識別碼），寫進 `spec-canvas-layout.md`「待釐清」或整合 CR；代價：需要人工在 T-17／T-19／T-20 開工前介入，會擋住排程；B. 不定案，T-17／T-19／T-20 沿用 T-18 的 Screen ID 慣例，等 OQ-49 整合 CR 一次定案並遷移；代價：遷移範圍擴大到四個任務的註冊值與既有畫布上所有這幾類 item 資料，但不擋住目前排程。
