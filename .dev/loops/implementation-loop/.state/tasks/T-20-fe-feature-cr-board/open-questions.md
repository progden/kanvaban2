# T-20-fe-feature-cr-board open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-20-fe-feature-cr-board-01

[Level: feature-cr-board/s-feature-cr-board]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：**已解除（2026-09-22，人工混合選項 A／B）**

解除說明：入口機制（維持一般化的「＋ 加入元件」入口，不新增專屬入口）與版面沿用選項 A，視為先行版本；但 `item.component` 命名規則不等 OQ-49，現在就統一定案為完整 Screen ID，見 `OQ-T-18-fe-widgets-02`／`03` 解除說明。本任務的值已從 `"feature-cr-board"` 改為 `"s-feature-cr-board"`（`pnpm build`／`pnpm test` 全綠）。

情況：【推論＋所本原文】
`ui-feature-cr-board.md` `s-feature-cr-board`「進入與離開」段落逐字：『從哪裡進來：不適用——內容以 F07 item 形式顯示於 s-canvas，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）』。
`.dev/loops/implementation-loop/.state/tasks.md` T-20 該列備註逐字：『原標 blocked 已解除，見 OQ-IMPL-06「解除說明」；依賴改為 T-13；涵蓋 CR：無』；`OQ-IMPL-06` 解除說明逐字：『`.dev/F06-feature-cr-board/ui-feature-cr-board.md` 已有 `s-feature-cr-board`。同 OQ-IMPL-04，改依賴 T-13-fe-canvas-shell，任務編號改為 T-20-fe-feature-cr-board。』——只解除依賴／編號問題，未處理「具體機制待整合 CR 定案」這句本身。
推論：同一批前端任務中，T-16（`s-activity-log`）遇到相同性質的「無專屬入口機制」情境，已依 T-13 建立的一般化掛載機制（畫布「＋ 加入元件」對話框輸入 `item.component` 識別碼）先行實作，開立不阻塞 OQ 說明（`OQ-T-16-fe-activity-log-01`）。本任務比照同一結論：不新增專屬入口按鈕，`item.component` 字串值固定為 "feature-cr-board"（本任務推論命名，spec／ui 未定案任何識別碼字串）；版面（Feature 清單、CR 巢狀分組、orphan CR 區塊、警告區塊的呈現方式）沿用既有 Canvas item（`ActivityLogItem`）樣式語彙推論決定，`ui-feature-cr-board.md`「資料」「狀態」欄只定義要顯示哪些欄位／狀態，未定義版面結構。
問題：`s-feature-cr-board` 目前的實作（`item.component` 固定為 "feature-cr-board"；Feature／CR／orphan／警告四個區塊的簡單清單版面；沿用畫布既有的「＋ 加入元件」一般化入口，無專屬入口）是否符合預期？是否需要等 `spec-canvas-layout.md`「待釐清」與 OQ-49 的整合 CR 正式定案後，回頭修正 `item.component` 命名或改走專屬入口機制？
選項：A. 維持現狀，視為先行版本，待 OQ-49 對應的整合 CR 定案後另開修訂任務實例核對／調整；B. 立即由人工／CR 先定案 `item.component` 命名慣例與各元件入口機制（不只本任務，含 T-16、未來 F07 圖表元件皆受影響），本任務的實作依定案結果修正。
