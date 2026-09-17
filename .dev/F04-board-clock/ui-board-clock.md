# 看板時鐘 UI 短規格

本檔案依 `.dev/conventions/ui-convention.md` 拆解 `.dev/F04-board-clock/spec-board-clock.md`，盤點對應畫面並逐一定案短規格。

## s-board-clock-control：看板時鐘控制
所屬 Feature：看板時間管理
類型：對話框
狀態：討論中

### 目的
看板 Owner 調整、暫停或恢復該看板的時鐘，以模擬不同時間點的操作，觀察圖表與統計如何變化。

### 進入與離開
- 從哪裡進來：不適用——內容以 F07 item 形式顯示於 s-canvas，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）
- 完成後去哪裡：調整或暫停／恢復後停留本畫面，看板時間顯示更新為最新狀態
- 中途放棄會怎樣：關閉對話框，看板時間維持關閉前的狀態（不套用未送出的調整輸入）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-board-owner`（F02，跨模組） | 看板時間目前值與狀態 | 調整看板時間、暫停看板時間、恢復看板時間 |
| `r-user`（F01，跨模組） | ⚠️ 待確認，見 OQ-37 | 不做得到（依 `uc-adjust-board-clock`／`uc-pause-resume-board-clock` 的角色定義僅 `r-board-owner`） |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 看板時間目前值 | ⚠️ 待確認，見 OQ-36（spec 名詞定義三張表無對應 Attribute ID） | 顯示 | — | 依 Feature Background，看板時間有一個目前值 |
| 看板時間狀態 | ⚠️ 待確認，見 OQ-36（同上，僅「其他名詞」描述 REALTIME／PAUSED 兩種模式，無 Attribute ID） | 顯示 | — | 決定顯示「暫停」或「恢復」操作 |
| 調整目標時間 | ⚠️ 待確認，見 OQ-36（同上） | 輸入 | ⚠️ 待確認，見 OQ-38（型別／範圍 spec 未定義） | 供「調整看板時間」操作使用 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 調整看板時間 | `uc-adjust-board-clock` | 依 `uc-adjust-board-clock` post：看板時間顯示更新為調整後的時間 | 依 `uc-adjust-board-clock` p1：保留對話框與已輸入的目標時間，顯示訊息；`uc-guard-clock-monotonicity` 屬於建立新事件時的限制，不是本操作的失敗，不獨立成一列 | 否 |
| 暫停看板時間 | `uc-pause-resume-board-clock` | 依 `uc-pause-resume-board-clock` post：看板時間狀態顯示為 PAUSED | 不適用（`uc-pause-resume-board-clock` fail 為空） | 否 |
| 恢復看板時間 | `uc-pause-resume-board-clock` | 依 `uc-pause-resume-board-clock` post：看板時間狀態顯示為 REALTIME | 不適用（`uc-pause-resume-board-clock` fail 為空） | 否 |
| 關閉 | — | 關閉對話框 | — | 否 |

### 狀態
- 載入中：載入看板時間目前值與狀態時顯示
- 空資料：不適用（每個 Board 都有自己的時鐘，一定有值）
- 錯誤：`uc-adjust-board-clock` 失敗時保留對話框與已輸入值，顯示訊息；`uc-pause-resume-board-clock` 目前無 fail 定義
- 無權限：⚠️ 待確認，見 OQ-37（非 Owner 是否仍看得到本畫面）
- 資料狀態差異：PAUSED 狀態下顯示「恢復」操作，REALTIME 狀態下顯示「暫停」操作

### 驗收條件
- 開啟時顯示看板時間目前值與狀態（REALTIME／PAUSED）
- 調整看板時間後，畫面顯示更新為調整後的時間，且觸發 `uc-adjust-board-clock`
- `uc-adjust-board-clock` 回傳 p1 時，畫面顯示訊息，且看板時間顯示不變（⚠️ 見 OQ-37）
- 暫停看板時間後，畫面顯示狀態為 PAUSED，且觸發 `uc-pause-resume-board-clock`
- 恢復看板時間後，畫面顯示狀態為 REALTIME，且觸發 `uc-pause-resume-board-clock`

### 待確認事項
- ⚠️ 看板時間目前值／狀態／調整目標時間欄位在 spec 名詞定義三張表無對應 Attribute ID，見 OQ-36
- ⚠️ 非 Owner（`r-user`）是否看得到本畫面 spec 未定義，見 OQ-37
- ⚠️ 調整目標時間輸入的型別／範圍限制 spec 未定義，見 OQ-38
