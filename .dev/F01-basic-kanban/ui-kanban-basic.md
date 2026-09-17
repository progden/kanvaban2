# 看板基本使用案例 UI 短規格

本檔案依 `.dev/conventions/ui-convention.md` 拆解 `.dev/F01-basic-kanban/spec-kanban-basic.md`，盤點對應畫面並逐一定案短規格。

## s-swimlane-list：Swimlane 列表
所屬 Feature：Swimlane 管理
類型：列表
狀態：已定案

### 目的
看板使用者在此檢視、新增、重新命名與拖曳排序看板的 Swimlane。

### 進入與離開
- 從哪裡進來：`s-board` 的「管理 Swimlane」操作（見 OQ-07；`s-board` 本身「從哪裡進來」仍為討論中，見 OQ-04）
- 完成後去哪裡：新增、重新命名、拖曳排序完成後停留本畫面，列表更新；刪除操作導向 `s-swimlane-delete-dialog`
- 中途放棄會怎樣：不適用（本畫面各操作皆為即時提交，無中途放棄流程）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user` | 全部 Swimlane 及其名稱、順序 | 新增 Swimlane、重新命名 Swimlane、拖曳調整順序、刪除 Swimlane |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 泳道名稱 | `swimlane.name` | 顯示、輸入（新增與重新命名時） | 非空 | — |
| 順序 | `board` 與 `swimlane` 的關係（依序） | 顯示（可拖曳調整） | — | 決定 Swimlane 在看板中的排列順序，依 `uc-reorder-swimlane` 調整 |

預設排序：依 `board` 與 `swimlane` 目前的關係順序。

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 新增 Swimlane | `uc-add-swimlane` | 新 Swimlane 顯示在列表最下方 | 依 `uc-add-swimlane` p1：輸入內容保留，顯示訊息 | 否 |
| 重新命名 Swimlane | `uc-rename-swimlane` | 該列名稱更新為新名稱 | 不適用（`uc-rename-swimlane` 無 fail 定義） | 否 |
| 拖曳調整順序 | `uc-reorder-swimlane` | 列表依拖曳結果重新排列 | 不適用（`uc-reorder-swimlane` 無 fail 定義） | 否 |
| 刪除 Swimlane | — | 開啟 `s-swimlane-delete-dialog` | 不適用 | 否 |

### 狀態
- 載入中：載入 Swimlane 清單時顯示
- 空資料：不適用（`board` 與 `swimlane` 的關係 min 為 1，看板至少保留一個 Swimlane，不會出現空列表）
- 錯誤：新增、重新命名或拖曳排序失敗時，依上方操作表顯示對應訊息
- 無權限：不適用（spec 僅定義 `r-user` 一種角色，無角色差異）
- 資料狀態差異：僅剩 1 個 Swimlane 時，刪除操作不可用（依 `uc-delete-swimlane` p1：「`board` 中的 `swimlane` 數量大於 1」）

### 驗收條件
- 既有 Swimlane 依序顯示於列表
- 新增成功後，新 Swimlane 顯示在列表最下方，且觸發 `uc-add-swimlane`
- 新增名稱為空時，輸入內容保留、顯示訊息，列表不變
- 重新命名送出後，該列名稱更新為新名稱，且觸發 `uc-rename-swimlane`
- 拖曳排序完成後，列表順序依拖曳結果更新，且觸發 `uc-reorder-swimlane`
- 僅剩 1 個 Swimlane 時，刪除操作無法使用
- 點擊刪除操作開啟 `s-swimlane-delete-dialog`

### 待確認事項
- （無）

## s-swimlane-delete-dialog：刪除 Swimlane 對話框
所屬 Feature：Swimlane 管理
類型：對話框
狀態：已定案

### 目的
看板使用者刪除 Swimlane 前確認，若該 Swimlane 內有卡片，一併告知將一同被刪除。

### 進入與離開
- 從哪裡進來：`s-swimlane-list` 的「刪除 Swimlane」操作
- 完成後去哪裡：回到 `s-swimlane-list`，列表重新載入
- 中途放棄會怎樣：關閉對話框，Swimlane 與卡片不變

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user` | 該 Swimlane 名稱、其內卡片數量 | 確認刪除、取消 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 泳道名稱 | `swimlane.name` | 顯示 | — | 要刪除的 Swimlane |
| 卡片數 | `swimlane`→`card` 關係計數 | 顯示 | — | 提示使用者有幾張卡片將一併被刪除 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 確認刪除 | `uc-delete-swimlane` | 關閉對話框，回列表 | 依 `uc-delete-swimlane` p1：保留對話框，顯示訊息 | 是（本畫面即確認） |
| 取消 | — | 關閉對話框 | — | 否 |

### 狀態
- 載入中：載入該 Swimlane 的卡片數量時顯示
- 空資料：不適用（進入此畫面代表指定的 `swimlane` 存在）
- 錯誤：確認刪除失敗時，依上方操作表顯示對應訊息
- 無權限：不適用（spec 僅定義 `r-user` 一種角色，無角色差異）
- 資料狀態差異：該 Swimlane 內有卡片時顯示卡片數量提示；無卡片時卡片數顯示為 0

### 驗收條件
- 開啟時顯示該 Swimlane 名稱
- 若該 Swimlane 內有卡片，顯示卡片數量
- 確認刪除後，回到 `s-swimlane-list` 且列表重新載入，且觸發 `uc-delete-swimlane`
- 取消後關閉對話框，Swimlane 與卡片不變

### 待確認事項
- （無）

## s-stage-list：Stage 列表
所屬 Feature：Stage（階段）管理
類型：列表
狀態：已定案

### 目的
看板使用者在此檢視、新增、重新命名、拖曳排序與設定角色（Start/Done）給看板的 Stage。

### 進入與離開
- 從哪裡進來：`s-board` 的「管理 Stage」操作（見 OQ-07；`s-board` 本身「從哪裡進來」仍為討論中，見 OQ-04）
- 完成後去哪裡：新增、重新命名、拖曳排序、設定角色完成後停留本畫面，列表更新；刪除操作導向 `s-stage-delete-dialog`
- 中途放棄會怎樣：不適用（本畫面各操作皆為即時提交，無中途放棄流程）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user` | 全部 Stage 及其名稱、順序、角色 | 新增 Stage、重新命名 Stage、拖曳調整順序、設定 Stage 角色、刪除 Stage |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 階段名稱 | `stage.name` | 顯示、輸入（新增與重新命名時） | — | — |
| 順序 | `board` 與 `stage` 的關係（依序） | 顯示（可拖曳調整） | — | 決定 Stage 在看板中的排列順序，依 `uc-reorder-stage` 調整 |
| 角色 | `stage.role` | 顯示、輸入（設定角色時） | enum(NONE, START, DONE)；同一 `board` 中 START、DONE 各至多一個 | 依 `uc-set-stage-role` 設定 |

預設排序：依 `board` 與 `stage` 目前的關係順序。

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 新增 Stage | `uc-add-stage` | 新 Stage 依指定位置插入列表，未指定位置時加到最後 | 不適用（`uc-add-stage` 無 fail 定義） | 否 |
| 重新命名 Stage | `uc-rename-stage` | 該列名稱更新為新名稱 | 不適用（`uc-rename-stage` 無 fail 定義） | 否 |
| 拖曳調整順序 | `uc-reorder-stage` | 列表依拖曳結果重新排列 | 不適用（`uc-reorder-stage` 無 fail 定義） | 否 |
| 設定 Stage 角色 | `uc-set-stage-role` | 該列角色更新為指定角色；若 `board` 中原本已有其他 Stage 持有該角色，該列角色顯示變回 NONE | 不適用（`uc-set-stage-role` 無 fail 定義） | 否 |
| 刪除 Stage | — | 開啟 `s-stage-delete-dialog` | 不適用 | 否 |

### 狀態
- 載入中：載入 Stage 清單時顯示
- 空資料：不適用（`board` 與 `stage` 的關係 min 為 1，看板至少保留一個 Stage，不會出現空列表）
- 錯誤：新增、重新命名、拖曳排序或設定角色失敗時，依上方操作表顯示對應訊息
- 無權限：不適用（spec 僅定義 `r-user` 一種角色，無角色差異）
- 資料狀態差異：僅剩 1 個 Stage 時，刪除操作不可用（依 `uc-delete-stage` p1：「`board` 中的 `stage` 數量大於 1」）；設定某 Stage 角色為 START 或 DONE 時，原持有該角色的 Stage 該列角色顯示同步變回 NONE

### 驗收條件
- 既有 Stage 依序顯示於列表，含各自角色
- 新增成功後，新 Stage 顯示在指定位置（未指定位置時在最後），且觸發 `uc-add-stage`
- 重新命名送出後，該列名稱更新為新名稱，且觸發 `uc-rename-stage`
- 拖曳排序完成後，列表順序依拖曳結果更新，且觸發 `uc-reorder-stage`
- 設定角色送出後，該列角色更新為指定角色，且觸發 `uc-set-stage-role`
- 將某 Stage 設為 START 或 DONE 後，原持有該角色的 Stage 該列角色顯示變回 NONE
- 僅剩 1 個 Stage 時，刪除操作無法使用
- 點擊刪除操作開啟 `s-stage-delete-dialog`

### 待確認事項
- （無）

## s-stage-delete-dialog：刪除 Stage 對話框
所屬 Feature：Stage（階段）管理
類型：對話框
狀態：已定案

### 目的
看板使用者刪除 Stage 前確認，若該 Stage 內有卡片，需選擇一個目的 Stage 來接收這些卡片。

### 進入與離開
- 從哪裡進來：`s-stage-list` 的「刪除 Stage」操作
- 完成後去哪裡：回到 `s-stage-list`，列表重新載入
- 中途放棄會怎樣：關閉對話框，Stage 與卡片不變

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user` | 該 Stage 名稱、其內卡片數量、可選擇的目的 Stage 清單 | 選擇目的 Stage、確認刪除、取消 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| Stage 名稱 | `stage.name` | 顯示 | — | 要刪除的 Stage |
| 卡片數 | `stage`→`card` 關係計數 | 顯示 | — | 提示使用者有幾張卡片需要轉移 |
| 目的 Stage | `board`→`stage` 關係（同一 `board` 中的其他 Stage） | 輸入（卡片數大於 0 時必選） | 排除欲刪除的 Stage 本身，清單只列其他 Stage | 依 `uc-delete-stage` post，接收該 Stage 內的卡片 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 選擇目的 Stage | — | 更新目的 Stage 選擇狀態 | 不適用 | 否 |
| 確認刪除 | `uc-delete-stage` | 關閉對話框，回列表 | 依 `uc-delete-stage` p1：保留對話框，顯示訊息 | 是（本畫面即確認） |
| 取消 | — | 關閉對話框 | — | 否 |

### 狀態
- 載入中：載入該 Stage 的卡片數量與可選目的 Stage 清單時顯示
- 空資料：不適用（進入此畫面代表指定的 `stage` 存在）
- 錯誤：確認刪除失敗時，依上方操作表顯示對應訊息
- 無權限：不適用（spec 僅定義 `r-user` 一種角色，無角色差異）
- 資料狀態差異：該 Stage 內有卡片時，需先選擇目的 Stage 才能確認刪除；無卡片時可直接確認刪除，不需選擇目的 Stage

### 驗收條件
- 開啟時顯示該 Stage 名稱
- 若該 Stage 內有卡片，顯示卡片數量，且需選擇目的 Stage 才能確認刪除
- 若該 Stage 內無卡片，可直接確認刪除，不需選擇目的 Stage
- 確認刪除後，該 Stage 不再顯示於 `s-stage-list`，回到 `s-stage-list` 且列表重新載入，且觸發 `uc-delete-stage`
- 取消後關閉對話框，Stage 與卡片不變

### 待確認事項
- （無）

## s-board：看板
所屬 Feature：Card（卡片）編輯
類型：儀表板
狀態：討論中

### 目的
看板使用者在此檢視看板所有 Swimlane 與 Stage 交會格內的卡片，可拖曳卡片跨 Swimlane 或跨 Stage 移動，也可拖曳看板成員頭像到卡片上追加負責人。

### 進入與離開
- 從哪裡進來：⚠️ 待確認（見 OQ-04）：跨模組，需先選擇／開啟一個看板（見 F02 spec-user-membership.md），對應畫面尚未定案
- 完成後去哪裡：拖曳移動卡片、拖曳頭像追加負責人完成後停留本畫面，交會格內容更新；新增卡片操作導向 `s-card-add-dialog`；點擊卡片導向 `s-card-detail`；刪除卡片操作導向 `s-card-delete-dialog`；管理 Swimlane 操作導向 `s-swimlane-list`；管理 Stage 操作導向 `s-stage-list`
- 中途放棄會怎樣：不適用（拖曳操作皆為即時提交，無中途放棄流程；新增卡片的中途放棄行為由 `s-card-add-dialog` 定義）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user` | 全部 Swimlane × Stage 交會格與其中卡片 | 拖曳卡片跨 Swimlane、拖曳卡片跨 Stage、新增卡片、開啟卡片詳情、刪除卡片、前往管理 Swimlane、前往管理 Stage |
| `r-board-member`（F02，跨模組） | 同上 | 拖曳成員頭像到卡片追加負責人 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| Swimlane 清單 | `board` 與 `swimlane` 的關係（依序） | 顯示 | — | 決定橫向分組與排列順序 |
| Stage 清單 | `board` 與 `stage` 的關係（依序） | 顯示 | — | 決定縱向欄位與排列順序 |
| 卡片所在位置 | `card.swimlane`、`card.stage` | 顯示 | — | 決定卡片顯示於哪個交會格 |
| 卡片標題 | `card.title` | 顯示 | — | 顯示於交會格中的卡片縮圖 |
| 卡片截止日期 | `card.due-date` | 顯示 | — | 依 `uc-edit-card` post：卡片縮圖顯示 `card.due-date` |
| 卡片負責人 | `card.assignees`（見 F02 spec-user-membership.md） | 顯示 | — | 依 F02 `uc-set-card-assignees` post：卡片縮圖同步顯示 `card.assignees` 的所有成員 |
| 看板成員清單 | `board-membership`（見 F02 spec-user-membership.md） | 顯示 | — | 供拖曳頭像指派負責人使用；⚠️ 已改為 F07 s-canvas 上另一個獨立 item，非本畫面資料，見 OQ-19，待 D-09 依 T7.02 完成後移除本列 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 拖曳卡片跨 Swimlane | `uc-move-card-swimlane` | 卡片顯示於目的 Swimlane 與原 Stage 的交會格 | 不適用（`uc-move-card-swimlane` 無 fail 定義） | 否 |
| 拖曳卡片跨 Stage | `uc-move-card-stage` | 卡片顯示於目的 Stage | 不適用（`uc-move-card-stage` 無 fail 定義） | 否 |
| 拖曳成員頭像到卡片追加負責人 | `uc-assign-card-owner-by-drag`（F02） | 卡片縮圖的負責人顯示更新 | 不適用（`uc-assign-card-owner-by-drag` 無 fail 定義） | 否 |
| 新增卡片 | — | 開啟 `s-card-add-dialog` | 不適用 | 否 |
| 開啟卡片詳情 | — | 開啟 `s-card-detail` | 不適用 | 否 |
| 刪除卡片 | — | 開啟 `s-card-delete-dialog` | 不適用 | 否 |
| 管理 Swimlane | — | 開啟 `s-swimlane-list` | 不適用 | 否 |
| 管理 Stage | — | 開啟 `s-stage-list` | 不適用 | 否 |

### 狀態
- 載入中：載入 Swimlane、Stage 與卡片資料時顯示
- 空資料：不適用（`board` 與 `swimlane`、`stage` 的關係 min 皆為 1，交會格結構至少存在；單一交會格內無卡片時顯示為空格，不視為錯誤）
- 錯誤：不適用（`uc-move-card-swimlane`／`uc-move-card-stage`／`uc-assign-card-owner-by-drag` 目前均無 fail 定義）
- 無權限：不適用（F01 spec 僅定義 `r-user` 一種角色，無角色差異）
- 資料狀態差異：不適用

### 驗收條件
- 每個 Swimlane × Stage 交會格依序顯示，格內列出屬於該交會格的卡片
- 卡片縮圖顯示標題，並在有截止日期、負責人時一併顯示
- 拖曳卡片到另一個 Swimlane 後，卡片顯示於目的 Swimlane 與原 Stage 的交會格，且觸發 `uc-move-card-swimlane`
- 拖曳卡片到另一個 Stage 後，卡片顯示於目的 Stage，且觸發 `uc-move-card-stage`
- 拖曳看板成員頭像到卡片後，卡片縮圖的負責人顯示更新，且觸發 `uc-assign-card-owner-by-drag`
- 點擊新增卡片開啟 `s-card-add-dialog`
- 點擊卡片開啟 `s-card-detail`
- 點擊刪除卡片開啟 `s-card-delete-dialog`
- 點擊管理 Swimlane 開啟 `s-swimlane-list`
- 點擊管理 Stage 開啟 `s-stage-list`

### 待確認事項
- ⚠️ 進入路徑已依 OQ-18 改為由 F07 s-canvas 承接（非「導向進入」），機制細節仍待 F07「待釐清」定案，見 OQ-18（`ui-authoring-open-questions.md`），待 D-09 依 T7.02 完成後改寫「從哪裡進來」
- ⚠️ 看板成員清單已改為 F07 s-canvas 上另一個獨立 item，非本畫面資料：見 OQ-19（`ui-authoring-open-questions.md`），待 D-09 依 T7.02 完成後移除相關內容

## s-card-add-dialog：新增卡片對話框
所屬 Feature：Card（卡片）編輯
類型：表單
狀態：已定案

### 目的
看板使用者在指定的 Swimlane 與 Stage 交會格中，輸入標題以建立一張新卡片。

### 進入與離開
- 從哪裡進來：`s-board` 的「新增卡片」操作
- 完成後去哪裡：確認新增成功後關閉對話框，回到 `s-board`，該交會格內容更新
- 中途放棄會怎樣：取消，關閉對話框，不建立卡片

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user` | 目的 Swimlane、目的 Stage 名稱 | 輸入標題、確認新增、取消 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 卡片標題 | `card.title` | 輸入 | 非空 | 依 `uc-add-card` pre p1 |
| 目的 Swimlane | `swimlane.name` | 顯示 | — | 依進入情境帶入：`s-board` 觸發「新增卡片」時所在交會格的 Swimlane，本畫面不可變更 |
| 目的 Stage | `stage.name` | 顯示 | — | 依進入情境帶入：`s-board` 觸發「新增卡片」時所在交會格的 Stage，本畫面不可變更 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 確認新增 | `uc-add-card` | 關閉對話框，回到 `s-board`，該卡片顯示於指定 Swimlane 與 Stage 的交會格 | 依 `uc-add-card` p1：輸入內容保留，顯示訊息 | 否 |
| 取消 | — | 關閉對話框 | — | 否 |

### 狀態
- 載入中：不適用（目的 Swimlane、目的 Stage 由進入情境提供，本畫面無需額外載入資料）
- 空資料：不適用（進入此畫面代表指定的 `swimlane` 與 `stage` 已存在）
- 錯誤：確認新增失敗時，依上方操作表顯示對應訊息
- 無權限：不適用（spec 僅定義 `r-user` 一種角色，無角色差異）
- 資料狀態差異：不適用

### 驗收條件
- 開啟時顯示目的 Swimlane、目的 Stage 名稱
- 標題輸入為空時確認新增，對話框維持開啟、輸入內容保留、顯示訊息
- 確認新增成功後，關閉對話框，回到 `s-board`，該卡片顯示於指定 Swimlane 與 Stage 的交會格，且觸發 `uc-add-card`
- 取消後關閉對話框，且不觸發 `uc-add-card`

### 待確認事項
- （無）

## s-card-detail：卡片詳情
所屬 Feature：Card（卡片）編輯
類型：表單
狀態：已定案

### 目的
看板使用者在此檢視並編輯卡片詳細內容，並新增留言追蹤討論。

### 進入與離開
- 從哪裡進來：`s-board` 的「開啟卡片詳情」操作
- 完成後去哪裡：儲存變更或新增留言後停留本畫面，對應欄位更新為最新內容；開啟負責人選取入口後導向 `s-card-assignee-picker`（F02），選取完成後回到本畫面，負責人欄位顯示更新後的名單；關閉回到 `s-board`，看板交會格內容更新
- 中途放棄會怎樣：關閉未儲存的欄位變更，卡片內容維持關閉前的樣子（不套用本次未儲存的編輯）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user` | 卡片完整內容、留言列表 | 儲存變更、新增留言、關閉 |
| `r-board-member`（F02，跨模組） | 同上 | 開啟負責人選取入口 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 卡片標題 | `card.title` | 顯示 | — | 依 `uc-edit-card` post，本畫面僅更新 `card.description`、`card.due-date`、`card.labels`，標題不可於此編輯 |
| 卡片描述 | `card.description` | 顯示 / 輸入 | — | 依欄位表 `card.description` 限制欄無資料 |
| 截止日期 | `card.due-date` | 顯示 / 輸入 | — | 依欄位表 `card.due-date` 限制欄無資料 |
| 標籤 | `card.labels` | 顯示 / 輸入（多值） | — | 依欄位表 `card.labels` 限制欄無資料 |
| 負責人 | `card.assignees`（F02，跨模組） | 顯示 | — | 依 `uc-view-card-assignees` post；沒有指派負責人時顯示為未指派；透過操作表「開啟負責人選取」進入 `s-card-assignee-picker`（F02）調整 |
| 留言列表 | `comment.content`／`comment.author`／`comment.created-at` | 顯示 / 輸入（新增留言內容） | `comment.content` 非空 | 依 `uc-add-comment` post，`card` 底下的留言依 `comment.created-at` 排序顯示 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 儲存變更 | `uc-edit-card` | 停留本畫面，描述、截止日期、標籤欄位顯示儲存後的內容 | 不適用（`uc-edit-card` 無 fail 定義） | 否 |
| 新增留言 | `uc-add-comment` | 依 `uc-add-comment` post：該留言顯示於留言列表 | 不適用（`uc-add-comment` 無 fail 定義） | 否 |
| 開啟負責人選取 | — | 開啟 `s-card-assignee-picker`（F02），回來後負責人欄位顯示更新後的名單 | 不適用（純前端導覽） | 否（可重新開啟本入口調整負責人，非不可逆操作） |
| 關閉 | — | 回到 `s-board`，看板交會格內容更新 | — | 否 |

### 狀態
- 載入中：載入卡片內容與留言列表時顯示
- 空資料：不適用（進入本畫面代表指定的 `card` 已存在，依 `uc-edit-card` pre p1）
- 錯誤：不適用（`uc-edit-card`、`uc-add-comment` 均無 fail 定義）
- 無權限：不適用（F01 spec 僅定義 `r-user` 一種角色，無角色差異）
- 資料狀態差異：不適用

### 驗收條件
- 開啟時顯示卡片標題、描述、截止日期、標籤、負責人與既有留言列表；沒有指派負責人時，負責人欄位顯示為未指派
- 儲存變更後，本畫面的描述、截止日期、標籤欄位顯示儲存的內容，且觸發 `uc-edit-card`
- 新增留言後，該留言顯示於留言列表，且觸發 `uc-add-comment`
- 開啟負責人選取入口後開啟 `s-card-assignee-picker`，不觸發任何 Use Case；選取完成回到本畫面後，負責人欄位顯示更新後的名單
- 關閉後回到 `s-board`，看板交會格內容更新

### 待確認事項
- （無）

## s-card-delete-dialog：刪除卡片對話框
所屬 Feature：Card（卡片）編輯
類型：對話框
狀態：已定案

### 目的
看板使用者刪除卡片前確認，此動作無法復原。

### 進入與離開
- 從哪裡進來：`s-board` 的「刪除卡片」操作
- 完成後去哪裡：回到 `s-board`，該卡片自看板移除
- 中途放棄會怎樣：關閉對話框，回到 `s-board`

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user` | 該卡片標題 | 確認刪除、取消 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 卡片標題 | `card.title` | 顯示 | — | 要刪除的卡片 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 確認刪除 | `uc-delete-card` | 關閉對話框，回到 `s-board`，該卡片自看板移除 | 不適用（`uc-delete-card` 除 p2 取消分支外無其他失敗定義） | 是（本畫面即確認） |
| 取消 | — | 依 `uc-delete-card` p2：關閉對話框 | — | 否 |

### 狀態
- 載入中：載入該卡片標題時顯示
- 空資料：不適用（進入此畫面代表指定的 `card` 存在，依 `uc-delete-card` pre p1）
- 錯誤：不適用（`uc-delete-card` 除 p2 取消分支外無其他失敗定義）
- 無權限：不適用（spec 僅定義 `r-user` 一種角色，無角色差異）
- 資料狀態差異：不適用

### 驗收條件
- 開啟時顯示該卡片標題
- 確認刪除後，該卡片不再顯示於 `s-board`，回到 `s-board`，且觸發 `uc-delete-card`
- 取消後關閉對話框，且不觸發 `uc-delete-card`

### 待確認事項
- （無）
