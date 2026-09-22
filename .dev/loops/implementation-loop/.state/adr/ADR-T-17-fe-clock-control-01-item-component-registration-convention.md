## ADR-T-17-fe-clock-control-01：Canvas item 元件識別碼與註冊位置慣例

- 狀態：Accepted（2026-09-22，人工修正決策 1，見下方「修正」段；決策 2／3 維持原案）
- 日期：2026-09-22
- 提出者：Dev（T-17-fe-clock-control）

### 背景（Context）

`spec-canvas-layout.md` 只定義 `item.component` 的值 `"board"`（由 `uc-init-canvas` 自動建立），其餘元件的識別碼與「如何把一個模組的畫面內容變成一個 canvas item」的機制尚未定案（`ui-board-clock.md`「進入與離開」：『如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）』）。`itemComponentRegistry.tsx`（T-13 提供）只給了 `registerItemComponent(component, renderer)` 這個掛載點，沒有規定：(1) `component` 字串怎麼命名，(2) 在哪個檔案呼叫 `registerItemComponent`，(3) item 內容需要 `boardId` 才能打 API，但 `ItemContentProps` 原本沒有這個欄位。T-17 是第一個在正式程式碼（非測試）裡實際呼叫 `registerItemComponent` 的任務，T-14／T-15／T-16／T-18／T-19／T-20 都在平行分支各自面對同樣的問題。

### 決策（Decision）

1. ~~`item.component` 命名：對應畫面的 Screen ID 去掉 `s-` 前綴，直接用 kebab-case（例：`s-board-clock-control` → `board-clock-control`）。~~ **修正（2026-09-22，人工決策，見 OQ-T-18-fe-widgets-02／03 解除說明）**：改用完整 Screen ID（含 `s-` 前綴），例：`s-board-clock-control`。理由：T-18 四個儀表板已經用完整 Screen ID 實作且已合併，若採 kebab-case 去前綴，等於要回頭改 T-18 而非其餘五個，代價更高；完整 Screen ID 也更貼近 `docs-convention.md` 一路可追溯的識別碼原則。原本已用 kebab-case 去前綴的 `activity-log`／`board-clock-control`／`workload-dashboard`／`feature-cr-board` 四個已改回加上 `s-` 前綴（`board-members`，T-15，例外：它不是獨立 Screen，沒有對應的 Screen ID，不適用本決策，維持原樣）。
2. 註冊位置：在掛載該 item 內容的畫面檔（目前只有 `BoardCanvasPage.tsx`／`main.tsx`）頂層以模組層級呼叫 `registerItemComponent(...)`，import 該畫面時即完成註冊，不另外建集中式註冊表。
3. `ItemContentProps`（`itemComponentRegistry.tsx`）新增 `boardId: string`，由 `CanvasStage.tsx` 傳入；任何需要呼叫看板相關 API 的 item 內容都可以直接拿到，不用另外從 URL 參數重新解析。

### 考慮過的替代方案（Alternatives）

- 集中式註冊表（例如 `registerAllItemComponents.ts` 一次 import 全部模組並註冊）：優點是註冊位置單一、不會有平行任務同時改 `BoardCanvasPage.tsx` 造成合併衝突；缺點是這個檔案會變成所有前端 item 任務的共同修改點，一樣有合併衝突風險，只是移到另一個檔案，且目前只有一個畫面掛載 CanvasStage，集中表暫無額外好處，故未採用（YAGNI）。

### 後果（Consequences）

- T-14／T-15／T-16／T-18／T-19／T-20 若沿用本慣例，会各自在 `BoardCanvasPage.tsx` 加一行 `registerItemComponent(...)` 呼叫與一行 import，多個平行分支同時改同一個檔案的相鄰位置，合併時大機率需要人工／驅動腳本解衝突（非邏輯衝突，只是同檔案多處新增）。
- 若之後人工定案 OQ-49 的整合機制與本慣例不同（例如改成後端驅動的元件登錄表、或者集中式註冊表），所有已用本慣例的任務（含本任務）都要回頭改註冊呼叫的位置，`item.component` 字串值本身不受影響。
- `ItemContentProps` 新增 `boardId` 是不可逆的介面擴充（現有呼叫端 `CanvasStage.tsx` 已同步更新），後續任務的 item 內容元件可以直接假設這個欄位存在。

對應 OQ：`OQ-T-17-fe-clock-control-01`。
