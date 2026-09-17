# 看板基本使用案例 UI 短規格

本檔案依 `.dev/conventions/ui-convention.md` 拆解 `.dev/F01-basic-kanban/spec-kanban-basic.md`，盤點對應畫面並逐一定案短規格。

## s-swimlane-list：Swimlane 列表
所屬 Feature：Swimlane 管理
類型：列表
狀態：討論中

### 目的
看板使用者在此檢視、新增、重新命名與拖曳排序看板的 Swimlane。

### 進入與離開
- 從哪裡進來：⚠️ 待確認（見 OQ-01），暫定為模組入口
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
| 重新命名 Swimlane | `uc-rename-swimlane` | 該列名稱更新為新名稱 | 不適用（`uc-rename-swimlane` 無 `fail` 定義） | 否 |
| 拖曳調整順序 | `uc-reorder-swimlane` | 列表依拖曳結果重新排列 | 不適用（`uc-reorder-swimlane` 無 `fail` 定義） | 否 |
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
- ⚠️ 進入路徑未定：見 OQ-01（`ui-authoring-open-questions.md`）

## s-swimlane-delete-dialog：刪除 Swimlane 對話框
所屬 Feature：Swimlane 管理
類型：對話框
狀態：未討論

## s-stage-list：Stage 列表
所屬 Feature：Stage（階段）管理
類型：列表
狀態：未討論

## s-stage-delete-dialog：刪除 Stage 對話框
所屬 Feature：Stage（階段）管理
類型：對話框
狀態：未討論

## s-board：看板
所屬 Feature：Card（卡片）編輯
類型：儀表板
狀態：未討論

## s-card-add-dialog：新增卡片對話框
所屬 Feature：Card（卡片）編輯
類型：對話框
狀態：未討論

## s-card-detail：卡片詳情
所屬 Feature：Card（卡片）編輯
類型：詳情
狀態：未討論

## s-card-delete-dialog：刪除卡片對話框
所屬 Feature：Card（卡片）編輯
類型：對話框
狀態：未討論
