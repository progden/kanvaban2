# 看板標準圖表使用案例 UI 短規格

本檔案依 `.dev/conventions/ui-convention.md` 拆解 `.dev/F03-kanban-widgets/spec-kanban-widgets.md`，盤點對應畫面並逐一定案短規格。

## s-cycle-lead-time-dashboard：Cycle Time 與 Lead Time 儀表板
所屬 Feature：Cycle Time 與 Lead Time 分析
類型：儀表板
狀態：討論中

### 目的
看板使用者檢視卡片從開始到完成花費的時間，以便評估團隊的交付速度與承諾交期。

### 進入與離開
- 從哪裡進來：不適用——內容以 F07 item 形式顯示於 s-canvas，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）
- 完成後去哪裡：不適用（純檢視畫面，無完成後導向其他畫面的動作）
- 中途放棄會怎樣：不適用（無多步驟流程）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user`（F01，跨模組） | 全部已完成卡片的 Lead Time／Cycle Time 與統計摘要 | 檢視圖表 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 卡片標題 | `card.title` | 顯示 | — | 識別卡片清單中每一列 |
| Lead Time | 衍生：`card` 建立時間到進入 Done 角色 Stage 的時間差 | 顯示 | — | 依 `uc-view-cycle-lead-time` post p1 |
| Cycle Time | 衍生：`card` 第一次進入 Start 角色 Stage 到完成的時間差；未曾進入 Start 就完成時顯示「無」 | 顯示 | — | 依 `uc-view-cycle-lead-time` post p1／p2 |
| 完成時間 | 衍生：`card` 最後一次進入 Done 角色 Stage 的時間 | 顯示 | — | 依 `uc-view-cycle-lead-time` post p3，離開 Done 後再進入以最後一次為準 |
| 排除計算的卡片數 | 衍生：Cycle Time 顯示為「無」的卡片數量加總 | 顯示 | — | 依 `uc-view-cycle-lead-time` post p2 |
| 統計摘要（平均值、百分位） | 衍生：卡片清單 Cycle Time 統計排除 Cycle Time 為「無」的卡片；Lead Time 統計是否比照排除，spec 未定義，見 OQ-33 | 顯示 | ⚠️ 待確認：具體百分位數（例如 P50／P85）spec 未定義，見 OQ-33 | 依 `uc-view-cycle-lead-time` post p2 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 檢視圖表 | `uc-view-cycle-lead-time` | 顯示卡片清單、Lead Time／Cycle Time 與統計摘要 | 不適用（`uc-view-cycle-lead-time` 無 fail 定義） | 否 |

### 狀態
- 載入中：載入卡片與 Stage 歷史資料以計算 Lead Time／Cycle Time 時顯示
- 空資料：尚無已完成卡片時，卡片清單與統計摘要顯示為空
- 錯誤：不適用（`uc-view-cycle-lead-time` 無 fail 定義）
- 無權限：不適用（F03 spec 僅使用跨模組 `r-user` 一種角色，無角色差異）
- 資料狀態差異：不適用（`uc-view-cycle-lead-time` post 未定義卡片清單以外的呈現差異；Cycle Time「無」與排除計算卡片數已列於「資料」段）

### 驗收條件
- 已完成卡片清單顯示每張卡片的 Lead Time 與 Cycle Time
- 未曾進入 Start 角色 Stage 就完成的卡片，Cycle Time 顯示為「無」
- 統計摘要的 Cycle Time 平均值與百分位計算排除 Cycle Time 顯示為「無」的卡片
- 統計摘要顯示「排除計算的卡片數」
- 卡片離開 Done 後再次完成時，完成時間顯示為最後一次進入 Done 的時間
- 開啟畫面時觸發 `uc-view-cycle-lead-time`

### 待確認事項
- ⚠️ 統計摘要的百分位計算所指定的具體百分位數（例如 P50／P85），以及 Lead Time 統計是否比照 Cycle Time 排除「無」的卡片，spec 未定義，見 OQ-33

## s-wip-dashboard：WIP 與 Aging WIP 儀表板
所屬 Feature：WIP 與 Aging WIP 監控
類型：儀表板
狀態：已定案

### 目的
看板使用者檢視各 Stage 目前的卡片數量，以及進行中卡片已經停留多久，以便及早發現流程卡住的地方。

### 進入與離開
- 從哪裡進來：不適用——內容以 F07 item 形式顯示於 s-canvas，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）
- 完成後去哪裡：不適用（純檢視畫面，無完成後導向其他畫面的動作）
- 中途放棄會怎樣：不適用（無多步驟流程）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user`（F01，跨模組） | 各 Stage 目前卡片數量、進行中卡片的年齡 | 檢視 WIP 圖表、檢視 Aging WIP 圖表 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| Stage 名稱 | `stage`（F01，跨模組） | 顯示 | — | WIP 圖表依 Stage 分組 |
| Stage 卡片數 | 衍生：該 Stage 目前的 `card` 數量 | 顯示 | — | 依 `uc-view-wip` post p1 |
| 卡片標題 | `card.title` | 顯示 | — | 識別 Aging WIP 清單中每一列 |
| 卡片年齡 | 衍生：`card` 進入 Start 角色 Stage 到 asOf（看板時間目前）所經過的時間 | 顯示 | — | 依 `uc-view-aging-wip` post p1，僅列已進入 Start、尚未進入 Done 的卡片 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 檢視 WIP 圖表 | `uc-view-wip` | 顯示各 Stage 目前卡片數量 | 不適用（`uc-view-wip` 無 fail 定義） | 否 |
| 檢視 Aging WIP 圖表 | `uc-view-aging-wip` | 顯示進行中卡片清單與年齡 | 不適用（`uc-view-aging-wip` 無 fail 定義） | 否 |

### 狀態
- 載入中：載入 Stage 卡片數量與卡片時間軸以計算年齡時顯示
- 空資料：看板沒有任何卡片時，WIP 圖表顯示為空；沒有已進入 Start、尚未進入 Done 的卡片時，Aging WIP 清單顯示為空
- 錯誤：不適用（`uc-view-wip`／`uc-view-aging-wip` 皆無 fail 定義）
- 無權限：不適用（F03 spec 僅使用跨模組 `r-user` 一種角色，無角色差異）
- 資料狀態差異：不適用（兩個 uc 的 post 未定義清單以外的呈現差異）

### 驗收條件
- 各 Stage 顯示目前的卡片數量
- 已進入 Start 角色 Stage、尚未進入 Done 的卡片，顯示從進入 Start 到看板時間目前所經過的年齡
- 開啟畫面時觸發 `uc-view-wip`
- 開啟畫面時觸發 `uc-view-aging-wip`

### 待確認事項
- （無）

## s-throughput-cfd-dashboard：Throughput 與累積流量圖儀表板
所屬 Feature：Throughput 與累積流量圖
類型：儀表板
狀態：已定案

### 目的
看板使用者檢視單位時間完成的卡片數量與各 Stage 卡片數量隨時間的變化，以便掌握團隊的產出趨勢與流程瓶頸。

### 進入與離開
- 從哪裡進來：不適用——內容以 F07 item 形式顯示於 s-canvas，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）
- 完成後去哪裡：不適用（純檢視畫面，無完成後導向其他畫面的動作）
- 中途放棄會怎樣：不適用（無多步驟流程）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user`（F01，跨模組） | 各期間完成卡片數量趨勢、每一天每個 Stage 的累積卡片數量 | 檢視 Throughput 圖表、檢視 CFD 圖表 |

### 資料
Throughput 圖與 CFD 圖兩個資料區塊以「圖表」欄區分，同一個 Screen ID 內不拆兩個畫面。

| 圖表 | 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|---|
| Throughput | 單位時間 | 使用者選擇（列舉：日、週） | 輸入 | 僅限「日」或「週」 | 依 `uc-view-throughput` post p1 |
| Throughput | 期間完成數 | 衍生：依選定單位時間分組，該期間內進入 Done 的 `card` 數量 | 顯示 | — | 依 `uc-view-throughput` post p1 |
| CFD | Stage 名稱 | `stage`（F01，跨模組） | 顯示 | — | CFD 依 Stage 分層堆疊 |
| CFD | 日期 | 衍生：資料範圍內的每一天 | 顯示 | — | 依 `uc-view-cfd` post p1 |
| CFD | 累積卡片數 | 衍生：該日期、該 Stage 的累積 `card` 數量 | 顯示 | — | 依 `uc-view-cfd` post p1 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 檢視 Throughput 圖表 | `uc-view-throughput` | 依選定單位時間顯示各期間完成卡片數量 | 不適用（`uc-view-throughput` 無 fail 定義） | 否 |
| 檢視 CFD 圖表 | `uc-view-cfd` | 顯示每一天、每個 Stage 的累積卡片數量 | 不適用（`uc-view-cfd` 無 fail 定義） | 否 |

### 狀態
- 載入中：載入卡片時間軸以計算各期間完成數量／各 Stage 每日累積數時顯示
- 空資料：選定範圍內無完成卡片，或尚無卡片資料時，對應圖表顯示為空
- 錯誤：不適用（`uc-view-throughput`／`uc-view-cfd` 皆無 fail 定義）
- 無權限：不適用（F03 spec 僅使用跨模組 `r-user` 一種角色，無角色差異）
- 資料狀態差異：不適用（兩個 uc 的 post 未定義清單以外的呈現差異）

### 驗收條件
- 依選定單位時間（日／週）分組，顯示各期間完成卡片數量
- CFD 圖顯示每一天、每個 Stage 的累積卡片數量
- 開啟畫面時觸發 `uc-view-throughput`
- 開啟畫面時觸發 `uc-view-cfd`

### 待確認事項
- （無）

## s-duedate-reminder：截止日期提醒列表
所屬 Feature：截止日期提醒
類型：列表
狀態：討論中

### 目的
看板使用者檢視已逾期或即將到期的卡片，以便優先處理有時間壓力的工作項目。

### 進入與離開
- 從哪裡進來：不適用——內容以 F07 item 形式顯示於 s-canvas，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）
- 完成後去哪裡：不適用（純檢視畫面，無完成後導向其他畫面的動作）
- 中途放棄會怎樣：不適用（無多步驟流程）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user`（F01，跨模組） | 已逾期與即將到期的卡片清單 | 檢視逾期提醒圖表 |

### 資料
| 清單 | 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|---|
| 共用 | 門檻天數 | 使用者輸入（查詢時設定） | 輸入 | ⚠️ 待確認：型別／範圍 spec 未定義，見 OQ-32 | 依 `uc-view-duedate-reminder` post p2，決定「即將到期」清單的判斷門檻 |
| 已逾期 | 卡片標題 | `card.title` | 顯示 | — | 依 `uc-view-duedate-reminder` post p1 |
| 已逾期 | 截止日期 | `card.due-date` | 顯示 | — | 依 `uc-view-duedate-reminder` post p1 |
| 即將到期 | 卡片標題 | `card.title` | 顯示 | — | 依 `uc-view-duedate-reminder` post p2 |
| 即將到期 | 截止日期 | `card.due-date` | 顯示 | — | 依 `uc-view-duedate-reminder` post p2 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 檢視逾期提醒圖表 | `uc-view-duedate-reminder` | 顯示已逾期與即將到期的卡片清單 | 不適用（`uc-view-duedate-reminder` 無 fail 定義） | 否 |

### 狀態
- 載入中：載入卡片截止日期與完成狀態以計算已逾期／即將到期清單時顯示
- 空資料：沒有已逾期或即將到期的卡片時，對應清單顯示為空
- 錯誤：不適用（`uc-view-duedate-reminder` 無 fail 定義）
- 無權限：不適用（F03 spec 僅使用跨模組 `r-user` 一種角色，無角色差異）
- 資料狀態差異：不適用（`uc-view-duedate-reminder` post 未定義清單以外的呈現差異）

### 驗收條件
- 截止日期早於看板目前時間、尚未完成的卡片顯示於「已逾期」清單
- 截止日期與看板目前時間相差在門檻天數內、尚未完成的卡片顯示於「即將到期」清單
- 門檻天數由使用者於查詢時設定
- 開啟畫面時觸發 `uc-view-duedate-reminder`

### 待確認事項
- ⚠️ 門檻天數的型別／範圍 spec 未定義，僅有範例值 3 天，見 OQ-32
