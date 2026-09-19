# adr（Architecture Decision Record）

> 只能追加，不可修改或刪除既有條目；決策要推翻時新開一筆 ADR，狀態設 `Superseded by ADR-xxx`，並讓被取代的舊條目狀態改成 `Superseded by ADR-<新編號>`（唯一允許回頭改動既有條目的情況，只能改狀態欄）。
> 收錄**跨任務、影響後續實作方式**的結構性決策（例如 port 介面怎麼切、`kanban-core`／`kanban-spring` 的邊界怎麼畫、跨模組共用的技術選型）；只影響單一任務內部的技術細節寫在 [`decision-log.md`](decision-log.md) 就好，不必升成 ADR。

## 格式

```
## ADR-<三位數>：<標題>

- 狀態：Proposed｜Accepted｜Superseded by ADR-xxx
- 日期：<date>
- 提出者：<Dev｜Review｜Planning，任務 ID>

### 背景（Context）
<為什麼需要做這個決定；有哪些限制或既有事實>

### 決策（Decision）
<選了什麼做法>

### 考慮過的替代方案（Alternatives）
<至少列出一個沒選的做法，說明為什麼不選>

### 後果（Consequences）
<這個決定往後會讓哪些任務／哪些程式碼必須遵守；有什麼代價或風險>
```

## ADR-001：usecase fail 對應 HTTP 狀態碼的慣例

- 狀態：Accepted
- 日期：2026-09-18
- 提出者：Planning（人工決策，源自 T-01-be-user 的 OQ-IMPL-09）

### 背景（Context）
所有 `spec-*.md` 的 `usecase` 區塊只定義 `fail` 的文字訊息與資料不變條件（例如「拒絕，不建立新的 `user`」），完全沒有定義對應的 HTTP 狀態碼；`ui-*.md` 操作表的「失敗時」欄也只寫呈現方式，一樣沒有狀態碼。T-01-be-user 的 Dev 輪已經依 REST 慣例暫定了一套對應（見下方決策），並開了 OQ-IMPL-09。這個問題不是 T-01 獨有——T-02～T-09 的 web 層之後都要對各自的 `fail` key 做一樣的映射，如果不統一，同一種語意的失敗（例如「資源已存在」）會在不同模組回不同狀態碼，前端要各自處理例外。

### 決策（Decision）
全專案 `kanban-spring` 的 web 層，usecase 的 `fail` key 對應 HTTP 狀態碼統一依下列慣例（依 fail 情境的語意分類，不是逐一枚舉）：

| fail 情境語意 | HTTP 狀態碼 | 範例（T-01 已用） |
|---|---|---|
| 輸入格式／內容不合法（長度、格式、必填等） | 400 Bad Request | `PASSWORD_TOO_LONG` |
| 與既有資料衝突（唯一性、狀態衝突） | 409 Conflict | `USERNAME_ALREADY_EXISTS` |
| 身分驗證失敗或未登入 | 401 Unauthorized | `INVALID_CREDENTIALS`、未登入查 session |
| 已登入但無權限操作 | 403 Forbidden | （T-01 未用到，留給後續有權限檢查的任務） |
| 指定的資源不存在 | 404 Not Found | （T-01 未用到，留給後續任務） |
| 寫入類操作成功且不需回傳內容 | 204 No Content | 登出成功 |

回應內容一律帶錯誤代碼（例如 `UserController` 目前用的 `ErrorCode` enum 風格），不是只靠狀態碼區分細節；狀態碼判斷語意分類，錯誤代碼判斷精確情境。

### 考慮過的替代方案（Alternatives）
全部失敗情境統一回 400，由回應內容的錯誤代碼欄位區分細節——被否決，因為狀態碼本身失去語意（401/403/404/409 是 HTTP 語意的一部分，前端／API 使用者可以只看狀態碼就做基本分流，不用每次都解析回應內容）。

### 後果（Consequences）
T-02～T-09（以及任何之後新增的 web 端點）的 Dev 輪，實作 `fail` 對應的 HTTP 狀態碼時要依上表分類，不必再各自開 OQ 討論；遇到表格涵蓋不到的新語意分類（例如批次操作部分失敗），才需要新開 OQ 或回頭修這則 ADR（推翻時開新 ADR，狀態改 `Superseded by ADR-xxx`）。Review 審查時可以直接依這張表核對，不用逐案判斷合不合理。

## ADR-002：BoardMembership 上線前的 Owner 權限代理，必須在合併後由下一個動到它的任務換成正式查詢

- 狀態：Accepted
- 日期：2026-09-19
- 提出者：人工決策（源自 T-05-be-board-clock 的 OQ-T-05-be-board-clock-02）

### 背景（Context）
`spec-board-clock.md`「決議紀錄」逐字：『權限檢查由呼叫端查「BoardMembership」後決定是否呼叫
「Board」的方法，「kanban-core」本身不做權限判斷』——正式的 Owner 判斷依據是 F02 的
`BoardMembership`（`board-membership.role` 為 Owner），但 `BoardMembership` 由 T-04-be-board-membership
實作，跟需要「僅 Owner 可操作」的其他任務（T-02 的 9 個結構調整端點、T-05 的
`uc-adjust-board-clock`／`uc-pause-resume-board-clock`）平行開發，互相看不到對方，且合併順序不固定。

目前已出現兩種不同的暫時處理方式：
1. T-02（`BoardApplicationService` 的 9 個結構調整方法）：完全不做 Owner 檢查，只要求已登入
   （`implementation-loop OQ-IMPL-15`，明確待 T-04 補上）。
2. T-05（`BoardApplicationService.requireOwner`，`adjustClock`／`pauseClock`／`resumeClock` 共用）：
   用 `board.getCreatedBy().equals(operatorId)` 代理 Owner 判斷（`OQ-T-05-be-board-clock-02`），
   因為 `uc-adjust-board-clock` 的 `fail.p1` 是這個 usecase 自己明確定義、要測的情境，不能比照 T-02
   完全不測。

T-04 的任務範圍（`.state/tasks.md`）明確限定只能改「T-02 的九個結構調整端點」，不包含 T-05 的看板時鐘
三個端點；也就是說 T-04 完成後，第 1 種暫時做法會被 T-04 自然接手換掉，但第 2 種做法（以及未來任何
task 可能各自發明的第三種、第四種代理方式）不會有任何任務主動去改，會變成永遠留在程式碼裡的技術債，
且 Review 沒有一份清單可以核對「這裡是不是還在用代理判斷」。

### 決策（Decision）
訂立通則：**在 `BoardMembership`（T-04）合併之前，任何需要「僅 Owner 可操作」的程式碼，可以用
`board.createdBy` 代理判斷或完全不檢查（兩種暫時做法都可接受，依各任務當下的 spec fail 定義決定）；
但 T-04 合併之後，下一個實際去動到那段程式碼（`requireOwner` 或任何新的權限判斷方法）的任務，
有義務先把它換成查詢 `BoardMembership`，才能繼續做該任務原本要做的修改**——不必另外開一個專門
「補權限檢查」的追加任務，也不必每個代理判斷各自開一則 OQ 討論「由誰來改」。Review 審查任何
任務時，只要 diff 動到含有 Owner／權限判斷邏輯的檔案，且當下 `BoardMembership` 已存在，就要檢查
是否仍在用 `createdBy` 代理或完全略過檢查，是的話退回，要求該任務一併換成正式查詢。

### 考慮過的替代方案（Alternatives）
- 人工在 `.state/tasks.md` 補一個專門的追加任務（依賴 T-04、T-05）——被否決：每多一種代理權限判斷
  的寫法，就要再多開一個追加任務，任務清單會一直長；而且在那個追加任務排進去、開跑之前，程式碼
  一直帶著已知的技術債，沒有人在 Review 時會去攔。
- 現在就把 `board.createdBy` 代理正式寫進 spec 決議紀錄（走 CR）——被否決：這會跟同一份 spec 已經
  寫的『kanban-core 本身不做權限判斷、由呼叫端查 BoardMembership』互相矛盾，等於用 CR 覆蓋掉決議
  紀錄裡明確的長期設計方向，只是為了讓一個過渡期的權宜之計看起來像是定案。

### 後果（Consequences）
T-04 合併之後，任何任務（不限定是哪一個）只要要去修改 `BoardApplicationService` 裡帶有 Owner／權限
判斷邏輯的方法（目前已知的是 `requireOwner` 供 `adjustClock`／`pauseClock`／`resumeClock` 共用，以及
T-02 九個結構調整方法本來就沒做的檢查），都要在那次改動裡把判斷依據換成查詢 `BoardMembership`
（`board-membership.role` 為 Owner），不能維持代理判斷或略過不驗證。Review 依此條目直接核對，不用
另開 OQ；真的要推翻這個處理方式（例如決定要幫這件事另開專門任務），開新 ADR 取代本則。
