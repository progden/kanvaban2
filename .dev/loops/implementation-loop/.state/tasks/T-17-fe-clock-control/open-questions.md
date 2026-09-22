# T-17-fe-clock-control open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-17-fe-clock-control-01

[Level: s-board-clock-control]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：canvas-item-integration-undefined
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：**已解除（2026-09-22，人工決策）**

解除說明：命名慣例定案為完整 Screen ID（含 `s-` 前綴），見 `OQ-T-18-fe-widgets-02`／`OQ-T-18-fe-widgets-03` 解除說明與 `ADR-T-17-fe-clock-control-01`「修正」段（原提案的 kebab-case 去前綴被覆蓋）。另外 `s-board-clock-control` 本身依 `OQ-T-17-fe-clock-control-02` 改為對話框，不再是 canvas item，`item.component` 命名慣例對它已不適用；`activity-log`／`workload-dashboard`／`feature-cr-board` 三個仍是 item 的都已改為 `s-activity-log`／`s-workload-dashboard`／`s-feature-cr-board`。註冊位置維持原案（畫面檔頂層）。

情況：【推論＋所本原文】
`ui-board-clock.md`「進入與離開」原文：『從哪裡進來：不適用——內容以 F07 item 形式顯示於 s-canvas，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）』。
`itemComponentRegistry.tsx` 原有註解：『這裡提供一個最小掛載點，讓之後的前端任務（T-14 起）用 registerItemComponent 掛上自己的內容』，`PlaceItemDialog.tsx` 原有註解：『spec-canvas-layout.md 只定義「board」這一個 item.component 值（由 uc-init-canvas 自動建立），其餘元件的值待各自模組定案』。
推論：由於整合機制未定案，本任務逕自決定 `item.component` 的識別碼為字串 `"board-clock-control"`，並在 `BoardCanvasPage.tsx`（掛載 CanvasStage 的畫面）以模組頂層呼叫 `registerItemComponent('board-clock-control', BoardClockControl)` 完成註冊；同時把 `ItemContentProps`（`itemComponentRegistry.tsx`）補上 `boardId` 欄位，供 item 內容呼叫看板 API（原本只有 itemId/component/width/height，任何需要打 API 的 item 內容都會遇到同樣缺口）。這兩個決定都是本任務為了讓 uc-adjust-board-clock／uc-pause-resume-board-clock 可被觸發而做的推論，不是規格定案內容；並列的畫布 item 命名慣例（T-14/T-15/T-16/T-18/T-19/T-20，各自平行分支中）尚未有共同依據，可能各自命名不一致，也可能各自在同一個檔案（如 App.tsx／BoardCanvasPage.tsx）加註冊呼叫造成合併衝突。
問題：後續任務（尤其 T-14 board 本體、T-15/T-16/T-18/T-19/T-20）的 item.component 命名慣例與註冊位置，是否要依本任務先例統一，或另訂規則？
選項：A. 採用本任務先例（component 值用 kebab-case 對應 screen id 去掉 s- 前綴；註冊呼叫放在掛載該 item 的畫面檔頂層），後續任務比照辦理，合併衝突由驅動腳本／人工個別排解；B. 由人工另訂集中式註冊表（例如統一在 main.tsx 或新建一個 registerAllItemComponents.ts 一次註冊全部元件），本任務與後續任務都改用該表，需要一次性重構。

## OQ-T-17-fe-clock-control-02

[Level: s-board-clock-control]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-conflict
- 開立：Review 第 1 輪（2026-09-22）
- 狀態：**已解除（2026-09-22，人工採選項 A，開 CR-014）**

解除說明：以操作表／「類型」欄為準，`ui-board-clock.md`「進入與離開」改為「F07 s-canvas 畫面上方列的『看板時間』按鈕」，「角色與權限」表移除「比照畫布 item 一律可見」的過時附註，詳見 `.dev/CR.md` CR-014。程式碼同步：`BoardClockControl.tsx` 改為 `dialog-backdrop`／`dialog-panel` 對話框（含「關閉」按鈕，觸發 `onClose`），從 `BoardCanvasPage.tsx` 上方列新增的「看板時間」按鈕開啟，不再是 canvas item（移除 `registerItemComponent('s-board-clock-control', ...)`）。`./scripts/ui-check` 0 error；`pnpm build`／`pnpm test` 全綠（19 測試檔、129 測試，含新增「點擊關閉按鈕觸發 onClose」測試）。

情況：【兩處矛盾並列】
`.dev/F04-board-clock/ui-board-clock.md`「s-board-clock-control」標頭與操作表，逐字引用：『類型：對話框』、『- 中途放棄會怎樣：關閉對話框，看板時間維持關閉前的狀態（不套用未送出的調整輸入）』、操作表最後一列『| 關閉 | — | 關閉對話框 | — | 否 |』、「狀態」段『錯誤：`uc-adjust-board-clock` 失敗時保留對話框與已輸入值，顯示訊息』。
同一份 ui 檔同一個畫面的「進入與離開」段，逐字引用：『- 從哪裡進來：不適用——內容以 F07 item 形式顯示於 s-canvas，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）』。
推論：同一個已定案畫面同時被描述為「對話框」（有「關閉」操作、有「關閉前的狀態」「保留對話框」語彙）與「以 F07 item 形式常駐顯示於 s-canvas」，兩者不可能同時成立——canvas item 沒有「關閉」這個動作，它的出現／消失是 `uc-place-item`／`uc-remove-item`（`spec-canvas-layout.md`）的事。T-17 第 1 輪依「進入與離開」那段實作為常駐 canvas item，因此操作表的「關閉」那一列沒有對應實作（決策紀錄有說明，但 Dev 未就此開 OQ）。本則補開，取代 Dev 未開立的部分；`OQ-T-17-fe-clock-control-01`（元件識別碼與註冊位置慣例）仍然有效，兩則不重疊。
問題：`s-board-clock-control` 的「類型」與操作表「關閉」那一列是否應隨「內容以 F07 item 形式顯示」一併修正（走 CR 把類型改為 item、移除「關閉」列與「關閉對話框」語彙），或者這個畫面真的要同時存在一個可關閉的對話框外觀？
選項：A. 認定「進入與離開」那段為準，開 CR 把 `s-board-clock-control` 的「類型」「中途放棄」「操作表關閉列」「狀態段的對話框語彙」改為 F07 item 的說法，T-17 現有實作不需改動；B. 認定操作表為準，這個畫面要實作成可關閉的對話框（含關閉按鈕與關閉後不套用未送出輸入的行為），需要一個後續修正任務改寫 T-17 的實作。
