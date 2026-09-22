# T-09-be-canvas-layout open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-09-be-canvas-layout-01

[Level: canvas-layout/uc-move-item,uc-resize-item,uc-remove-item]
- 等級：高
- 阻塞：否
- 接手：無
- 原因代碼：spec-ambiguous
- 開立：Dev 第 1 輪（2026-09-22）
- 狀態：待處理

情況：【推論＋所本原文】

`spec-canvas-layout.md`「畫布元素排列」Feature 只定義失敗情境的訊息文字與資料不變，沒有定義對應的 HTTP 狀態碼，例如 uc-move-item fail p2 逐字為：

『Then 拒絕，訊息為 "此元素不可移動"，且資料不變』

uc-resize-item fail p2 逐字為：

『Then 拒絕，訊息為 "此元素不可調整大小"，且資料不變』

uc-remove-item fail p2 逐字為：

『Then 拒絕，訊息為 "此元素不可移除"，且資料不變』

推論：這三種「item 存在，但因為能力設定（movable/resizable/removable=false）被拒絕」的情境，語意上比較接近「資源狀態衝突」而非「請求格式錯誤」，所以我對應到 HTTP 409 Conflict（CanvasController.statusFor），與現有 BoardController 對 SWIMLANE_HAS_CARDS／STAGE_HAS_CARDS（同樣是「資源存在但目前狀態不允許」）採用 409 的既有慣例一致。其餘 CANVAS_ITEM_NOT_FOUND／CANVAS_NOT_FOUND／BOARD_NOT_FOUND 對到 404，INVALID_ITEM_SIZE／INVALID_ITEM_ANCHOR／MIXED_ITEM_ANCHOR_IN_BATCH／VIEWPORT_ZOOM_OUT_OF_RANGE 對到 400。

問題：這個狀態碼分配（能力限制類→409、找不到→404、輸入驗證→400）是否符合預期？若前端／驗收測試已經對特定狀態碼有依賴，需要調整。

選項：A. 維持現況（409/404/400 分配）；B. 全部改成單一慣例（例如一律 400，訊息文字才是唯一依據，狀態碼不具語意）。
