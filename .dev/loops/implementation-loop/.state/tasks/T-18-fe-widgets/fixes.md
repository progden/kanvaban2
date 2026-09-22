# T-18-fe-widgets 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | done | — | OQ-T-18-fe-widgets-01 的 spec 引文不是逐字引用，且被刪掉的正是與本決策直接相牴觸的一句；請訂正 OQ 內文（不是改程式碼）。 問題所在：OQ 把 `.dev/F07-canvas-layout/spec-canvas-layout.md` 第 31 行 `item.component` 欄位說明引成—— 『元件本體的識別碼，由元件所屬模組定義；本模組不解讀其內容。看板本體固定為實體ID `board`；其餘元件的值待各自所屬模組實作對應Item時決定』 源頭第 31 行的原文是—— 『元件本體的識別碼，由元件所屬模組定義；本模組不解讀其內容。看板本體固定為實體 ID `board`（見「看板畫布初始化」Feature；不用 UI 層的 Screen ID，spec 不引用 ui，見 `docs-convention.md` 第 3 節）；其餘元件的值待各自所屬模組實作對應 Item 時決定。元件模組定案後改為 ref，元件與元素的生命週期連動（誰刪誰）屆時一併補』 被刪掉的括號裡寫著『不用 UI 層的 Screen ID，spec 不引用 ui』，而本任務把 `item.component` 定成四個 ui 層 Screen ID（`s-cycle-lead-time-dashboard` 等，見 `kanban-frontend/src/widgets/registerKanbanWidgets.ts`）。也就是說：OQ 刪掉的那一句，正是「唯一已定案的 component 值（看板本體）當初為什麼不用 Screen ID」的理由。人工讀這則 OQ 時會以為 spec 對命名毫無指引，實際上有一個明確的反向先例。全域 CLAUDE.md「修復 OQ／open question 時」明訂『一定要去源頭文件把具體文字拿出來，逐字引用用『』包住；不要自己縮寫、改寫、摘要成一句話』。 怎樣才算修好（三件事都要做）： 1. 用 `loopctl oq add` 另開一則 OQ 取代 OQ-T-18-fe-widgets-01，內文第一行註明「本則取代 OQ-T-18-fe-widgets-01（原則引文刪節）」，spec 第 31 行改為完整逐字引用（含括號與最後一句），ui-canvas-layout.md 那段引文也補回原文的空白（原文是『待整合 CR 定案』不是『待整合CR定案』）。等級「高」／阻塞「否」／接手「無」維持不變（做完本任務不需要違反任何定稿原文，spec 本來就把這個值交給所屬模組決定，這兩欄本身判定正確）。 2. 選項至少補上第三個：沿用 spec 層識別碼而非 ui 層 Screen ID 的命名（例如以 `uc-view-*` 或模組代號為基礎），理由是看板本體已有『不用 UI 層的 Screen ID』的先例；並把「日後 OQ-49 整合 CR 定案後需要遷移既有 item 資料」的代價寫在各選項底下。 3. 在 `decision-log.md` 記一句：讀完完整原文（含那段括號）之後，本任務仍維持 Screen ID 命名，還是改用 spec 層識別碼；維持原決定也可以，但理由要正面回應『不用 UI 層的 Screen ID』這句先例，不能再當它不存在。若決定改名，`registerKanbanWidgets.ts` 與四個 widget 測試的 `component` 值要一起改，測試需維持全綠。 |
