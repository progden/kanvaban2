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

## OQ-T-09-be-canvas-layout-03

[Level: canvas-layout/uc-init-canvas]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-conflict
- 開立：Dev 第 2 輪（2026-09-22）
- 狀態：**已由 OQ-T-09-be-canvas-layout-04 取代並解除（2026-09-22）**，見該則解除說明

情況：【兩處矛盾並列】

`spec-canvas-layout.md` `uc-init-canvas` 的 `post` 第 2 條逐字為：

『若該 `canvas` 原本沒有任何 `item`，建立一個 `item`：`item.component` 為 `board`、`item.x` 為 0、`item.y` 為 0、`item.width` 為 900、`item.height` 為 600、`item.anchor` 為 canvas、`item.z` 為 1、`item.movable`／`item.resizable`／`item.removable` 皆為 true』

「元件放置」Feature 的 Background 與 Scenario 逐字為：

『Background:
    Given 畫布已由系統建立』

『Scenario: 放置畫布元素
    Given 畫布中沒有任何元素
    ...』

推論：Background「畫布已由系統建立」若真的走 `uc-init-canvas`，canvas 建立當下一定會連帶建立一個 z=1 的看板本體 `item`（post 第 2 條），與後面「畫布中沒有任何元素」「畫布中存在 2 個錨定於畫布的元素，層序分別為 1、2」等 Given 假設的初始狀態（z 從 1 起算、或完全無元素）矛盾。測試前置（`CanvasSteps.givenCanvasEstablished`）因此直接以 `canvasJpaRepository.save(...)` 建立空白 canvas，繞過 `uc-init-canvas`，這個偏離沒有依規則開 OQ。

問題：「元件放置」「畫布元素排列」「畫布元素批次操作」三個 Feature 的 Background「畫布已由系統建立」，其前置狀態應理解為「已走過 `uc-init-canvas`（因此已有 z=1 看板本體元素，後續 Scenario 的 z 期望值需相應偏移）」，還是「單純建立一個空白 canvas（不含任何元素，維持現有測試前置與 Scenario 字面 z 值）」？

選項：A. 維持現況，Background 前置用空白 canvas（不呼叫 `uc-init-canvas`），並修正 spec 讓「元件放置」等 Feature 的 Background 文字明確排除看板本體元素；B. Background 改走 `GET /api/boards/{boardId}/canvas`（真正觸發 `uc-init-canvas`），並回頭修正「元件放置」等 Feature 內所有依賴「畫布中沒有任何元素」「層序從 1 起算」的 Scenario 與其 z 值期望。

## OQ-T-09-be-canvas-layout-04

[Level: canvas-layout/uc-init-canvas]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-conflict
- 開立：Dev 第 3 輪（2026-09-22）
- 狀態：待處理

本則取代 OQ-T-09-be-canvas-layout-03（該則 Scenario 標題引文非原文）。

情況：【兩處矛盾並列】

`spec-canvas-layout.md` `uc-init-canvas` 的 `post` 第 2 條逐字為：

『若該 `canvas` 原本沒有任何 `item`，建立一個 `item`：`item.component` 為 `board`、`item.x` 為 0、`item.y` 為 0、`item.width` 為 900、`item.height` 為 600、`item.anchor` 為 canvas、`item.z` 為 1、`item.movable`／`item.resizable`／`item.removable` 皆為 true』

「元件放置」Feature 的 Background 與 Scenario 逐字為：

『Background:
    Given 畫布已由系統建立』

『Scenario: 放置元件到空畫布
    Given 畫布中沒有任何元素』

推論：Background「畫布已由系統建立」若真的走 `uc-init-canvas`，canvas 建立當下一定會連帶建立一個 z=1 的看板本體 `item`（post 第 2 條），與後面「畫布中沒有任何元素」「畫布中存在 2 個錨定於畫布的元素，層序分別為 1、2」等 Given 假設的初始狀態（z 從 1 起算、或完全無元素）矛盾。測試前置（`CanvasSteps.givenCanvasEstablished`）因此直接以 `canvasJpaRepository.save(...)` 建立空白 canvas，繞過 `uc-init-canvas`，這個偏離未依規則開 OQ。

問題：「元件放置」「畫布元素排列」「畫布元素批次操作」三個 Feature 的 Background「畫布已由系統建立」，其前置狀態應理解為「已走過 `uc-init-canvas`（因此已有 z=1 看板本體元素，後續 Scenario 的 z 期望值需相應偏移）」，還是「單純建立一個空白 canvas（不含任何元素，維持現有測試前置與 Scenario 字面 z 值）」？

選項：A. 維持現況，Background 前置用空白 canvas（不呼叫 `uc-init-canvas`），並修正 spec 讓「元件放置」等 Feature 的 Background 文字明確排除看板本體元素；B. Background 改走 `GET /api/boards/{boardId}/canvas`（真正觸發 `uc-init-canvas`），並回頭修正「元件放置」等 Feature 內所有依賴「畫布中沒有任何元素」「層序從 1 起算」的 Scenario 與其 z 值期望。
