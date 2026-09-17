# 人員工作量 UI 短規格

本檔案依 `.dev/conventions/ui-convention.md` 拆解 `.dev/F05-workload/spec-workload.md`，盤點對應畫面並逐一定案短規格。

## s-workload-dashboard：人員工作量儀表板
所屬 Feature：人員工作量檢視
類型：儀表板
狀態：討論中

### 目的
看板使用者檢視每位成員目前手上有幾張進行中卡片與未指派卡片數量，以便平衡團隊的工作分配。

### 進入與離開
- 從哪裡進來：⚠️ 待確認（見 OQ-41）：spec Background 只寫已開啟 Board，未描述如何前往本畫面；進入路徑不預設
- 完成後去哪裡：不適用（純檢視搭配拖曳指派，無完成後導向其他畫面的動作）
- 中途放棄會怎樣：不適用（無多步驟流程）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user`（F01，跨模組） | 全部成員的工作量與未指派卡片數量 | 檢視工作量表、拖曳成員頭像到卡片追加負責人 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 成員名稱 | `user.display-name`（F02，跨模組） | 顯示 | — | 依 `uc-view-workload` post p1，依 `board-membership` 成員分組 |
| 成員工作量 | 衍生：該成員擔任負責人的 Active Card 數量 | 顯示 | — | 依 `uc-view-workload` post p1／p2；`card` 同時有多位負責人時，每位負責人的工作量各自包含該 `card` |
| 未指派卡片數 | 衍生：`card.assignees` 為空的 Active Card 數量 | 顯示 | — | 依 `uc-view-workload` post p3 |
| 卡片標題 | `card.title`（F01，跨模組） | ⚠️ 待確認 | — | ⚠️ 待確認：本畫面是否列出個別卡片供拖曳頭像指派負責人，spec 未寫明呈現方式，見 OQ-42 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 檢視工作量表 | `uc-view-workload` | 顯示各成員工作量與未指派卡片數量 | 不適用（`uc-view-workload` 無 fail 定義） | 否 |
| 拖曳成員頭像到卡片追加負責人 | `uc-drag-assign-card-owner` | 觸發 `uc-drag-assign-card-owner`（⚠️ 見 OQ-42） | 不適用（`uc-drag-assign-card-owner` 無 fail 定義） | 否（可透過 F02 `uc-set-card-assignees` 調整負責人清單，非不可逆操作） |

### 狀態
- 載入中：載入 `board-membership` 成員清單與 Active Card 分佈以計算工作量時顯示
- 空資料：看板沒有任何成員或 Active Card 時，工作量清單顯示為空
- 錯誤：不適用（`uc-view-workload`／`uc-drag-assign-card-owner` 皆無 fail 定義）
- 無權限：不適用（F05 spec 僅使用跨模組 `r-user` 一種角色，無角色差異）
- 資料狀態差異：不適用（兩個 uc 的 post 未定義清單以外的呈現差異）

### 驗收條件
- 成員工作量數字顯示 `uc-view-workload` 回傳的值
- 未指派任何負責人的 Active Card 數量顯示為「未指派」
- 開啟畫面時觸發 `uc-view-workload`
- 拖曳成員頭像到卡片後，觸發 `uc-drag-assign-card-owner`（⚠️ 見 OQ-42）

### 待確認事項
- ⚠️ 進入路徑未定義，見 OQ-41：進入路徑不預設，待後續決定
- ⚠️ 本畫面是否列出個別卡片供拖曳頭像指派負責人，spec 未寫明呈現方式，見 OQ-42
