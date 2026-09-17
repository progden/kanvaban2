# 畫布 UI 短規格

本檔案依 `.dev/conventions/ui-convention.md` 拆解 `.dev/F07-canvas-layout/spec-canvas-layout.md`，盤點對應畫面並逐一定案短規格。

## s-canvas：畫布
所屬 Feature：元件放置
類型：儀表板
狀態：討論中

### 目的
畫布編輯者在此檢視、放置、排列 Canvas 上的元件；畫布檢視者在此檢視並平移、縮放自己的檢視範圍。

### 進入與離開
- 從哪裡進來：F02 `s-board-list`（跨模組）選擇某個 Board 進入；依 spec 簡介「使用者開啟某個 Board 時，看到的就是該 Board 的 Canvas」
- 完成後去哪裡：各操作皆為即時提交，完成後停留本畫面，對應元素或檢視區即時更新；離開操作回到 F02 `s-board-list`（跨模組）
- 中途放棄會怎樣：不適用（各操作皆為即時提交，無中途放棄流程）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-canvas-editor` | 全部畫布元素與自己的檢視區 | 放置元件、移除元件、移動元件、調整元件大小、設定元件能力、設定元件錨定方式、調整元件層序（置頂／置底）、批次移動元件、批次移除元件、平移／縮放檢視區、離開、開啟管理 Swimlane、開啟管理 Stage |
| `r-canvas-viewer` | 全部畫布元素與自己的檢視區 | 平移／縮放檢視區、離開 |
| `r-user`（F01，跨模組） | 同 `r-canvas-editor` | 新增 Swimlane、新增 Stage |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 畫布元素清單 | `canvas` 與 `item` 的關係 | 顯示 | — | 依全部 `item` 的位置、大小、層序、錨定方式顯示於畫布或畫面固定位置 |
| 元件識別碼 | `item.component` | 顯示 | 必填 | 元件本體內容由所屬模組定義，本畫面僅依此值渲染對應元件；⚠️ 「看板本體」對應的值 spec「待釐清」尚未定義，見 OQ-17／OQ-18 |
| 元素位置 | `item.x`／`item.y` | 顯示 / 輸入（放置、移動、批次移動、調整大小、設定錨定方式時） | 必填 | 依 `item.anchor` 以畫布座標系或畫面座標系解讀 |
| 元素大小 | `item.width`／`item.height` | 顯示 / 輸入（放置、調整大小、設定錨定方式時） | 必填、皆須大於 0 | 錨定於畫布時為畫布單位，固定於畫面時為畫面單位 |
| 元素層序 | `item.z` | 顯示 | 必填、同一 `canvas` 內唯一 | 決定元素重疊時的前後順序；畫面固定元素永遠繪於畫布元素之上 |
| 元素錨定方式 | `item.anchor` | 顯示 / 輸入（放置、設定錨定方式時） | enum(canvas, screen)，預設 canvas | canvas 表示隨畫布平移縮放，screen 表示固定於畫面上 |
| 元素能力 | `item.movable`／`item.resizable`／`item.removable` | 顯示 / 輸入（放置、設定能力時） | 布林，預設皆為 true | 決定該元素可否被移動、調整大小、移除 |
| 我的檢視區 | `viewport`（`viewport.user` 為操作者本人） | 顯示 / 輸入（平移縮放時） | `viewport.zoom` 必填，須在縮放範圍內，預設 1 | 每位使用者各自一份，互不影響；第一次設定時建立 |
| 縮放範圍 | `canvas.zoom-min`／`canvas.zoom-max` | 顯示 | `canvas.zoom-min` > 0，`canvas.zoom-max` > `canvas.zoom-min`，預設 0.1～4 | 用於限制檢視區縮放輸入範圍 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 放置元件 | `uc-place-item` | 該元件顯示於指定位置、大小、層序、能力設定 | 依 `uc-place-item` p1：不建立元素，顯示訊息 | 否 |
| 移除元件 | `uc-remove-item` | 該元件自畫布移除 | 依 `uc-remove-item` p1／p2：元素不變，顯示訊息 | 是 |
| 移動元件 | `uc-move-item` | 該元件顯示於新位置 | 依 `uc-move-item` p1／p2：位置不變，顯示訊息 | 否 |
| 調整元件大小 | `uc-resize-item` | 該元件顯示新的位置與大小 | 依 `uc-resize-item` p1～p4：位置與大小不變，顯示訊息 | 否 |
| 設定元件能力 | `uc-set-item-capabilities` | 該元件的能力設定更新 | 依 `uc-set-item-capabilities` p1：能力設定不變，顯示訊息 | 否 |
| 設定元件錨定方式 | `uc-set-item-anchor` | 該元件的錨定方式、位置、大小、層序更新 | 依 `uc-set-item-anchor` p1：錨定方式與位置不變，顯示訊息 | 否 |
| 調整元件層序（置頂／置底） | `uc-reorder-item` | 該元件層序更新為指定方向的最上層或最下層 | 依 `uc-reorder-item` p1：層序不變，顯示訊息 | 否 |
| 批次移動元件 | `uc-move-items` | 所選元件皆依同一位移量更新位置 | 依 `uc-move-items` p1～p3：所選元件位置皆不變，顯示訊息 | 否 |
| 批次移除元件 | `uc-remove-items` | 所選元件皆自畫布移除 | 依 `uc-remove-items` p1／p2：所選元件皆不變，顯示訊息 | 是 |
| 平移／縮放檢視區 | `uc-set-viewport` | 我的檢視區顯示新的平移位置與縮放比例 | 依 `uc-set-viewport` p1：檢視區不變，顯示訊息 | 否 |
| 新增 Swimlane | `uc-add-swimlane`（F01，跨模組） | 新 Swimlane 顯示於 F01 `s-swimlane-list`（F01，跨模組）最下方 | 依 `uc-add-swimlane` p1：輸入內容保留，顯示訊息 | 否 |
| 新增 Stage | `uc-add-stage`（F01，跨模組） | 新 Stage 依指定位置插入 F01 `s-stage-list`（F01，跨模組），未指定位置時加到最後 | 不適用（`uc-add-stage` 無 fail 定義） | 否 |
| 開啟管理 Swimlane | — | 開啟 F01 `s-swimlane-list`（F01，跨模組） | 不適用 | 否 |
| 開啟管理 Stage | — | 開啟 F01 `s-stage-list`（F01，跨模組） | 不適用 | 否 |
| 離開 | — | 回到 F02 `s-board-list`（跨模組） | — | 否 |

### 狀態
- 載入中：載入畫布全部元件與自己的檢視區時顯示
- 空資料：畫布上沒有任何元件時，顯示空白畫布（`canvas` 與 `item` 的關係 min 為 0，允許空畫布）
- 錯誤：任一操作失敗時，依上方操作表顯示對應訊息
- 無權限：不適用（spec 僅定義 `r-canvas-editor`／`r-canvas-viewer` 兩種角色，兩者可見範圍相同、僅可執行的操作不同，未定義完全無法存取本畫面的角色；⚠️ 這兩個角色與 F02 `board-membership` 角色的對應關係未定義，見 OQ-44）
- 資料狀態差異：操作者角色為 `r-canvas-viewer` 時，僅能平移／縮放自己的檢視區與離開，其餘操作皆不可用

### 驗收條件
- 開啟時顯示畫布上全部元件，依各自的位置、大小、層序、錨定方式呈現
- 放置元件成功後，該元件顯示於指定位置、大小，且觸發 `uc-place-item`
- 觸發移除元件前顯示確認；確認後該元件自畫布移除，且觸發 `uc-remove-item`
- 移動、調整大小、設定能力、設定錨定方式、調整層序後，對應元件立即顯示更新後的內容，並分別觸發 `uc-move-item`／`uc-resize-item`／`uc-set-item-capabilities`／`uc-set-item-anchor`／`uc-reorder-item`
- 觸發批次移除前顯示確認；批次移動、批次移除操作後，所選元件皆依結果更新或移除，並分別觸發 `uc-move-items`／`uc-remove-items`
- 平移或縮放後，我的檢視區顯示新的位置與縮放比例，且觸發 `uc-set-viewport`；縮放比例超出範圍時，檢視區不變、顯示訊息
- 操作者角色為 `r-canvas-viewer` 時，畫面上僅平移縮放與離開可用，其餘操作皆無法使用
- 新增 Swimlane、新增 Stage 成功後，分別觸發 `uc-add-swimlane`、`uc-add-stage`
- 開啟管理 Swimlane、開啟管理 Stage 分別開啟 F01 `s-swimlane-list`、`s-stage-list`
- 離開後回到 F02 `s-board-list`

### 待確認事項
- ⚠️ 看板本體如何成為 `s-canvas` 上的一個 `item`（`item.component` 對應值、預設位置與大小、建立時機）spec「待釐清」尚未定義；人工已確認固定顯示於畫布中間的方向，確切機制待整合 CR 定案，見 OQ-17／OQ-18
- ⚠️ `r-canvas-editor`／`r-canvas-viewer`／`r-user`（F01）三者的對應關係 spec 未定義，見 OQ-44
