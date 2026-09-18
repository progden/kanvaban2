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

目前尚無條目。
