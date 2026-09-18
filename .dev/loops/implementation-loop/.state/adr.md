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
