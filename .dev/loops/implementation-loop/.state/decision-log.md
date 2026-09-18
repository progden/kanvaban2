# decision-log

> 只能追加。每輪（Dev／Review／安排階段）結束前至少追加一則，記錄「這輪做了什麼判斷、為什麼」，取代文件 loop 慣用的 PDCA 格式——實作階段要留的是**決策與理由**，不是計畫執行的四階段。
> 影響範圍跨任務、或往後的任務都要遵守的結構性選擇，額外在 [`adr.md`](adr.md) 開一筆 ADR，這裡的條目用 `ADR: ADR-xxx` 欄位引用它；只影響單一任務內部的技術選擇，寫在這裡就好，不必開 ADR。

## 格式

```
### <date> T-xx（Dev｜Review｜Planning）
- 決策：<這輪做了什麼判斷／選擇>
- 理由：<為什麼這樣選，若有考慮過其他做法一併寫>
- 影響：<影響到哪些檔案／任務／之後的實作>
- ADR：<ADR-xxx，若有開 ADR；沒有則省略此行>
```

低風險等級的決策（見 `iteration-prompt.md` 第 5 節）也要記，但可以只寫「決策」「理由」兩行，不必展開「影響」。

### 2026-09-18 T-06/T-07/T-08/T-15～T-20/T-18（校正，Planning）
- 決策：把 T-06、T-07、T-08 從 `blocked` 改回 `todo`；原前端 T-15／T-16／T-17（現改編號 T-18／T-19／T-20）從 `blocked` 改為 `todo` 並改依賴（從誤植的 `T-13-fe-board-detail` 改為新設的 `T-13-fe-canvas-shell`）；新增 `T-13-fe-canvas-shell`、`T-16-fe-activity-log`、`T-17-fe-clock-control` 三個原本遺漏的任務；解除 OQ-IMPL-01～06，新增 OQ-IMPL-07。
- 理由：安排階段最初讀到 F03／F05／F06 的「名詞定義」表是空的，誤判成「spec 尚未遷移」（依據錯誤的 CLAUDE.md 舊版敘述）；實際上 `spec-migration-loop` 已完成（`spec-migration-state.md`：『F01～F06 全部完成，全檔 0 error』），三個模組本來就「只做讀取投影，不新增 aggregate」（`CLAUDE.md` 逐字），表格空白是正確定案狀態。同理，`ui-*.md` 也已由 `ui-authoring-loop` 補齊全部七個模組，不再只有 F01。此外 `ui-authoring-loop` 的 OQ-49（人工已確認）把 F01 `s-board`、F03 四個儀表板、F04 時鐘控制、F05 工作量儀表板、F06 追蹤表全部定為 F07 `s-canvas` 上的獨立 item，原本「Canvas 依賴其他畫面」的方向是反的。
- 影響：`tasks.md` 全面重排（見該檔 2026-09-18 校正說明）；`open-questions.md` OQ-IMPL-01～06 標記已解除並附解除說明；新開 OQ-IMPL-07 追蹤 `item.component` 對應值與 `canvas` 建立時機這兩個 spec 本身仍待整合 CR 的缺口，不影響 T-09／T-13 已定義範圍的動工。
- ADR：無（這是任務清單內容的校正，不是跨任務的架構決策；Canvas-centric 架構本身已由 ui-authoring-loop 的 OQ-49 定案，不需要本 loop 重新開 ADR）。
