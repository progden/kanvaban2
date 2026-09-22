# T-17-fe-clock-control open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-17-fe-clock-control-01

[Level: s-board-clock-control]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：canvas-item-integration-undefined
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】
`ui-board-clock.md`「進入與離開」原文：『從哪裡進來：不適用——內容以 F07 item 形式顯示於 s-canvas，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）』。
`itemComponentRegistry.tsx` 原有註解：『這裡提供一個最小掛載點，讓之後的前端任務（T-14 起）用 registerItemComponent 掛上自己的內容』，`PlaceItemDialog.tsx` 原有註解：『spec-canvas-layout.md 只定義「board」這一個 item.component 值（由 uc-init-canvas 自動建立），其餘元件的值待各自模組定案』。
推論：由於整合機制未定案，本任務逕自決定 `item.component` 的識別碼為字串 `"board-clock-control"`，並在 `BoardCanvasPage.tsx`（掛載 CanvasStage 的畫面）以模組頂層呼叫 `registerItemComponent('board-clock-control', BoardClockControl)` 完成註冊；同時把 `ItemContentProps`（`itemComponentRegistry.tsx`）補上 `boardId` 欄位，供 item 內容呼叫看板 API（原本只有 itemId/component/width/height，任何需要打 API 的 item 內容都會遇到同樣缺口）。這兩個決定都是本任務為了讓 uc-adjust-board-clock／uc-pause-resume-board-clock 可被觸發而做的推論，不是規格定案內容；並列的畫布 item 命名慣例（T-14/T-15/T-16/T-18/T-19/T-20，各自平行分支中）尚未有共同依據，可能各自命名不一致，也可能各自在同一個檔案（如 App.tsx／BoardCanvasPage.tsx）加註冊呼叫造成合併衝突。
問題：後續任務（尤其 T-14 board 本體、T-15/T-16/T-18/T-19/T-20）的 item.component 命名慣例與註冊位置，是否要依本任務先例統一，或另訂規則？
選項：A. 採用本任務先例（component 值用 kebab-case 對應 screen id 去掉 s- 前綴；註冊呼叫放在掛載該 item 的畫面檔頂層），後續任務比照辦理，合併衝突由驅動腳本／人工個別排解；B. 由人工另訂集中式註冊表（例如統一在 main.tsx 或新建一個 registerAllItemComponents.ts 一次註冊全部元件），本任務與後續任務都改用該表，需要一次性重構。
