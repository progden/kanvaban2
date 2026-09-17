# Feature／CR 追蹤表 UI 短規格

本檔案依 `.dev/conventions/ui-convention.md` 拆解 `.dev/F06-feature-cr-board/spec-feature-cr-board.md`，盤點對應畫面並逐一定案短規格。

## s-feature-cr-board：Feature／CR 追蹤表
所屬 Feature：Feature／CR 追蹤表
類型：儀表板
狀態：已定案

### 目的
看板使用者透過卡片標籤檢視每個 Feature 與 CR 目前的開發狀態，以便掌握專案進度，不需要另外查閱文件。

### 進入與離開
- 從哪裡進來：不適用——內容以 F07 item 形式顯示於 s-canvas，見 spec-canvas-layout.md；如何新增此類元件的具體機制仍待該 spec「待釐清」與整合 CR 定案（依 OQ-49）
- 完成後去哪裡：不適用（純檢視，無完成後導向其他畫面的動作）
- 中途放棄會怎樣：不適用（無多步驟流程）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-user`（F01，跨模組） | 全部 Feature／CR 狀態、orphan CR 清單與格式錯誤警告 | 檢視 Feature／CR 追蹤表 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| Feature 編號 | 衍生：`card.labels` 中符合「其他名詞」表「Feature 卡」格式的標籤 | 顯示 | — | 依 `uc-view-feature-cr-board` post p1／p5（比對不分大小寫） |
| Feature 狀態 | 衍生：該 Feature 卡所在 Stage 的 `stage.role`，對應「其他名詞」表「狀態」定義（NONE＝未開發、START＝開發中、DONE＝已完成） | 顯示 | — | 依 post p1 |
| CR 編號 | 衍生：`card.labels` 中符合「其他名詞」表「CR 卡」格式的標籤 | 顯示 | — | 依 post p2／p5（比對不分大小寫） |
| CR 所屬 Feature | 衍生：`card.labels` 中符合「其他名詞」表「affects 標籤」格式的標籤 | 顯示 | — | 依 post p2；指到不存在的 Feature 編號時改列入 orphan CR 清單（post p3） |
| CR 狀態 | 衍生：該 CR 卡所在 Stage 的 `stage.role`，對應「其他名詞」表「狀態」定義 | 顯示 | — | 依 post p2 |
| orphan CR 清單 | 衍生：「其他名詞」表「orphan CR」定義（affects 指到不存在 Feature 編號的 CR 卡） | 顯示 | — | 依 post p3 |
| 標籤格式錯誤警告 | 衍生：`card` 同時帶有兩個 Feature 標籤時產生的警告訊息 | 顯示 | — | 依 post p4，僅影響該卡片本身，不影響其他卡片的 Feature／CR 統計 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 檢視 Feature／CR 追蹤表 | `uc-view-feature-cr-board` | 顯示各 Feature 狀態、各 CR 狀態、orphan CR 清單與格式錯誤警告 | 不適用（`uc-view-feature-cr-board` 無 fail 定義） | 否 |

### 狀態
- 載入中：讀取 `board` 全部 `card` 標籤以計算 Feature／CR 狀態時顯示
- 空資料：`board` 沒有任何符合 Feature 卡或 CR 卡標籤格式的 `card` 時，清單顯示為空
- 錯誤：不適用（`uc-view-feature-cr-board` 無 fail 定義）
- 無權限：不適用（F06 spec 僅使用跨模組 `r-user` 一種角色，無角色差異）
- 資料狀態差異：`card` 同時帶有兩個 Feature 標籤時，顯示一筆警告訊息，但不影響其他卡片的 Feature／CR 統計（依 post p4）

### 驗收條件
- Feature 列表、CR 狀態、orphan CR 清單、警告訊息顯示 `uc-view-feature-cr-board` 回傳的內容
- 開啟畫面時觸發 `uc-view-feature-cr-board`

### 待確認事項
- （無）
