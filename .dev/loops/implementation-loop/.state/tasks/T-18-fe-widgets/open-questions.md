# T-18-fe-widgets open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-18-fe-widgets-01

[Level: F03 四個儀表板 item.component 值]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】

`.dev/F07-canvas-layout/ui-canvas-layout.md` 第 80 行逐字：『⚠️ 依 OQ-49，F03 四個圖表畫面、F04 時鐘控制畫面、F05 工作量儀表板、F06 追蹤表畫面，人工已確認也是 `s-canvas` 上可放置的獨立 `item`（跟看板本體一樣，不透過其他畫面的操作列進入）；但這些各自的 `item.component` 對應值、如何從既有畫布新增這類元件（使用者操作流程）spec「待釐清」尚未定義，待整合CR定案』。

`.dev/F07-canvas-layout/spec-canvas-layout.md` 欄位表 `item.component` 定義逐字：『元件本體的識別碼，由元件所屬模組定義；本模組不解讀其內容。看板本體固定為實體ID `board`；其餘元件的值待各自所屬模組實作對應Item時決定』。

推論：本任務（T-18-fe-widgets）就是「各自所屬模組實作對應 Item」的時機，依上述欄位定義，`item.component` 的值本來就該由本任務決定；但這個值一旦定案，日後 OQ-49 對應的整合 CR 若採用不同命名，需要一次遷移既有畫布上已放置的這幾種 item（改 `item.component` 值），屬於覆蓋既有資料的風險，而非違反已定稿條文本身。

問題：F03 四個儀表板 item 的 `item.component` 字串值該取什麼，才不會與 OQ-49 之後定案的整合 CR 命名衝突？

選項：A. 採用本任務已實作的做法：直接用 Screen ID 字串（`s-cycle-lead-time-dashboard`、`s-wip-dashboard`、`s-throughput-cfd-dashboard`、`s-duedate-reminder`）作為 component 值，理由是這是規格裡唯一已存在、跨模組不會撞號的識別碼，之後 OQ-49 整合 CR 若要改名，只需一次資料遷移；B. 先不定案，等 OQ-49 整合 CR 出來後才實作這四個 item 的內容元件（會讓 T-18 整個 blocked，不符合任務排程，且會拖到 T-19／T-20 依賴的 canvas item 掛載慣例）。
