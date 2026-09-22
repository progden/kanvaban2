# T-13-fe-canvas-shell open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-13-fe-canvas-shell-01

[Level: F07/s-canvas]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-conflict
- 開立：Dev 第 2 輪（2026-09-22）
- 狀態：**已解除（2026-09-22，人工採選項 A）**

解除說明：依 spec 定案內容修訂 `ui-canvas-layout.md`：「角色與權限」表三列補上與 F02 `board-membership.role`（Owner／Member → `r-canvas-editor`；Viewer → `r-canvas-viewer`）的對應說明、移除「⚠️ 見 OQ-44」字樣；「狀態」段「無權限」列、「待確認事項」段同樣移除過時的「對應關係未定義，見 OQ-44」字樣。`./scripts/ui-check` 執行後 0 error（既有 10 則 warning 皆與本次修訂無關）。

情況：【兩處矛盾並列】

`.dev/F07-canvas-layout/spec-canvas-layout.md`「角色定義」逐字：
『｜ r-canvas-editor ｜ 畫布編輯者 ｜ 可放置與排列畫布元素，並平移與縮放自己的檢視區；對應該 Board 的 `board-membership.role` 為 Owner 或 Member 的使用者（即 `.dev/F02-user-membership/spec-user-membership.md` 的 `r-board-owner`／`r-board-member`） ｜』

`.dev/F07-canvas-layout/ui-canvas-layout.md`「待確認事項」逐字：
『- ⚠️ `r-canvas-editor`／`r-canvas-viewer`／`r-board-owner`（F02）三者的對應關係 spec 未定義，見 OQ-44』

推論：依 `docs-convention.md` 引用方向 spec 優先於 ui，spec 本體「角色定義」已把 `r-canvas-editor` 定義為對應 `board-membership.role` 為 Owner 或 Member 者，屬已定案內容；`ui-canvas-layout.md` 仍逐字保留「spec 未定義，見 OQ-44」字樣，是舊文字未隨 spec 更新。本任務（T-13）依 spec 定案內容實作 `BoardCanvasPage` 的 `canEdit` 判斷（`role !== 'VIEWER'`），未違反任何定稿文字，故不阻塞；但 `ui-canvas-layout.md`「角色與權限」表與「待確認事項」尚未同步移除過時的「⚠️」與 OQ-44 引用，人工需要知道並修訂。

問題：`ui-canvas-layout.md` 的「待確認事項」與「角色與權限」表（含 `r-board-owner` 那一列）是否應依 spec 定案內容修訂，移除過時的「三者對應關係未定義」字樣？

選項：A. 依 spec 定案內容修訂 `ui-canvas-layout.md`，移除過時的待確認事項與 OQ-44 引用；B. 保留現狀，待後續 CR 一併處理。
