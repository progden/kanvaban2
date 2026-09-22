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

## OQ-T-09-be-canvas-layout-02

[Level: canvas-layout/item.z]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-conflict
- 開立：Dev 第 2 輪（2026-09-22）
- 狀態：待處理

情況：【兩處矛盾並列】

`spec-canvas-layout.md` 名詞定義欄位表對 `item.z` 的限制欄逐字為：

『必填、同一 `canvas` 內唯一』

`uc-place-item` 的 `post` 逐字為：

『`item.z` 為同一錨定方式現有元素最大 z 值加一；尚無元素時為 1』

「其他名詞」的「層序」逐字為：

『畫面固定元素永遠繪於畫布元素之上，兩者各自比較 z 值。因各元素為獨立 aggregate，z 值的唯一性由儲存層唯一鍵保證』

推論：欄位表限制欄字面是「同一 canvas 內唯一」，但 `uc-place-item` post 與「層序」段落都指出 z 值是「同一錨定方式」（畫布／畫面）各自比較、各自從 1 起算，若照欄位表字面做成 canvas 內唯一，會讓畫布元素與畫面固定元素的 z 值互相搶號，與 post／層序兩段矛盾；因此實作採用 `(canvas_id, anchor, z)` 唯一鍵，只在 Java 註解（`ItemJpaEntity.java`）記錄理由，未依規則開 OQ。

問題：`item.z` 的唯一鍵範圍應以欄位表限制欄字面（同一 canvas 內唯一）為準，還是以 `uc-place-item` post／「層序」段落（同一錨定方式內唯一）為準？

選項：A. 維持現況，`(canvas_id, anchor, z)` 唯一鍵（同一錨定方式內唯一），並回頭修正欄位表限制欄文字使其與 post／層序一致；B. 改為 `(canvas_id, z)` 唯一鍵（canvas 內唯一），並修正 `uc-place-item` post 與「層序」段落文字使其與欄位表一致。
