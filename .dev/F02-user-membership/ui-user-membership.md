# 使用者與看板成員管理使用案例 UI 短規格

本檔案依 `.dev/conventions/ui-convention.md` 拆解 `.dev/F02-user-membership/spec-user-membership.md`，盤點對應畫面並逐一定案短規格。

## s-signup：註冊帳號
所屬 Feature：建立使用者帳號
類型：表單
狀態：討論中

### 目的
尚未擁有帳號的使用者在此輸入帳號 ID、顯示名字與密碼，建立一個新的系統帳號。

### 進入與離開
- 從哪裡進來：`s-login` 的『前往建立帳號』連結
- 完成後去哪裡：導向 `s-login`
- 中途放棄會怎樣：離開畫面，不建立帳號

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-system-user` | 本畫面表單欄位 | 輸入帳號 ID、顯示名字、密碼、確認建立帳號 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 帳號 ID | `user.username` | 輸入 | 非空、全系統不可重複，依 `uc-create-user` pre p2 | 登入用 |
| 顯示名字 | `user.display-name` | 輸入（選填） | 未指定時預設等於 `user.username` | — |
| 密碼 | `user.password` | 輸入（選填，可留白） | 長度上限 40 字，字元不限制，依 `uc-create-user` pre p1 | — |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 確認建立帳號 | `uc-create-user` | 依「完成後去哪裡」導向下一畫面 | 依 `uc-create-user` p1／p2：輸入內容保留，顯示訊息 | 是（本畫面即確認，`user` 目前無刪除 Use Case，建立後無法復原） |

### 狀態
- 載入中：不適用（本畫面無需載入既有資料）
- 空資料：不適用（本畫面僅為輸入表單，無列表資料）
- 錯誤：確認建立帳號失敗時，依上方操作表顯示對應訊息
- 無權限：不適用（spec 未定義本畫面的角色差異，`r-system-user` 皆可使用）
- 資料狀態差異：不適用

### 驗收條件
- 帳號 ID 為空時確認建立帳號，畫面維持顯示、輸入內容保留、顯示訊息，且不觸發 `uc-create-user`
- 密碼長度超過 40 字時確認建立帳號，輸入內容保留、顯示訊息，且不觸發 `uc-create-user`
- 帳號 ID 與系統中既有帳號重複時確認建立帳號，輸入內容保留、顯示訊息，且不觸發 `uc-create-user`
- 密碼留白時確認建立帳號成功，且觸發 `uc-create-user`

### 待確認事項
（無）

## s-login：登入
所屬 Feature：使用者登入與登出
類型：表單
狀態：討論中

### 目的
已擁有帳號的使用者在此輸入帳號 ID 與密碼登入系統；`uc-logout` 併入本畫面操作表（例如全域導覽列的登出動作），不獨立開一個畫面。

### 進入與離開
- 從哪裡進來：
  - 建立帳號成功後（依 `s-signup`「完成後去哪裡」，導向本畫面）
  - 登出後（`uc-logout` post：「登出成功，回到登入頁面」）
  - 未登入時的應用程式入口
- 完成後去哪裡：導向 `s-board-list`
- 中途放棄會怎樣：離開畫面，不登入

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-system-user` | 本畫面表單欄位；已登入狀態下的全域導覽列「登出」動作（附掛於其他已登入畫面，非本畫面顯示，見操作表） | 輸入帳號 ID、密碼、送出登入表單、登出 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 帳號 ID | `user.username` | 輸入 | 須為系統中已存在的 `user.username`，依 `uc-login` pre p1 | 登入用 |
| 密碼 | `user.password` | 輸入 | 須與該帳號對應的 `user.password` 相符，依 `uc-login` pre p2 | — |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 送出登入表單 | `uc-login` | 依「完成後去哪裡」導向下一畫面，TopBar 顯示帳號名稱 | 依 `uc-login` p1／p2：帳號 ID 與密碼欄位不變，顯示訊息，停留本畫面 | 否（帳號密碼錯誤可重新輸入再次嘗試，非不可逆操作） |
| 登出 | `uc-logout` | 回到本畫面（`s-login`） | 不適用（`uc-logout` 無 fail 定義） | 否（登出後可重新登入，非不可逆操作） |
| 前往建立帳號 | — | 開啟 `s-signup` | 不適用（純前端導覽） | 否（純畫面導覽，非不可逆操作） |

### 狀態
- 載入中：不適用（本畫面無需載入既有資料）
- 空資料：不適用（本畫面僅為輸入表單，無列表資料）
- 錯誤：送出登入表單失敗時，依上方操作表顯示對應訊息
- 無權限：不適用（spec 未定義本畫面的角色差異，`r-system-user` 皆可使用）
- 資料狀態差異：不適用

### 驗收條件
- 帳號密碼正確時送出登入表單，觸發 `uc-login`，成功後 TopBar 顯示該使用者名稱
- 帳號不存在時送出登入表單，帳號 ID 與密碼欄位保留、顯示訊息、停留本畫面，且 TopBar 不顯示帳號名稱
- 密碼與帳號不相符時送出登入表單，帳號 ID 與密碼欄位保留、顯示訊息、停留本畫面，且 TopBar 不顯示帳號名稱
- 已登入狀態下觸發登出動作，觸發 `uc-logout`，並回到本畫面（`s-login`）
- 觸發前往建立帳號動作，開啟 `s-signup`，不觸發任何 Use Case

### 待確認事項
（無）

## s-board-list：我的 Board 列表
所屬 Feature：Board 存取權限
類型：列表
狀態：討論中

### 目的
系統使用者在此檢視自己有權限（Owner 或 Member）的 Board 清單，並選擇其中一個 Board 進入。

### 進入與離開
- 從哪裡進來：登入成功後（依 `s-login`「完成後去哪裡」）
- 完成後去哪裡：選擇列表中的 Board 進入該 Board（F01 `s-board`；未來依 OQ-18 改為導向 F07 s-canvas，見本畫面「待確認事項」）
- 中途放棄會怎樣：不適用（本畫面僅為列表檢視與導覽，無中途放棄流程）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-system-user` | 自己是 Owner 或 Member 的 `board` 清單 | 選擇列表中的 Board 進入該 Board、嘗試直接開啟不屬於自己的 Board |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| Board 名稱 | `board.name` | 顯示 | 非空 | 依 `uc-view-board-list` Scenario，用於識別列表中每個 Board |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 選擇 Board 進入 | — | 開啟該 Board（F01 `s-board`） | 不適用 | 否 |
| 嘗試直接開啟不屬於自己的 Board | `uc-reject-board-access-by-nonmember` | 依 `uc-reject-board-access-by-nonmember` post：顯示訊息，停留本畫面 | 不適用（`uc-reject-board-access-by-nonmember` 無 fail 定義） | 否 |

### 狀態
- 載入中：載入 Board 列表時顯示
- 空資料：使用者尚未是任何 `board` 的 Owner 或 Member 時，顯示空清單
- 錯誤：不適用（`uc-view-board-list` 無 fail 定義）
- 無權限：嘗試直接開啟不屬於自己的 Board 時，依 `uc-reject-board-access-by-nonmember` post 顯示訊息，操作者仍無法存取該 `board`
- 資料狀態差異：不適用（spec 未定義 Owner／Member 列表呈現上的差異）

### 驗收條件
- 使用者是其 Owner 或 Member 的 Board 都顯示於列表，沒有權限的 Board 不顯示，且觸發 `uc-view-board-list`
- 尚未擁有任何 Board 時，列表顯示空清單
- 選擇列表中的 Board 後開啟該 Board（F01 `s-board`）
- 嘗試直接開啟不屬於自己的 Board 時，觸發 `uc-reject-board-access-by-nonmember`，且操作者仍無法存取該 Board

### 待確認事項
- ⚠️ 本畫面對 F01 `s-board` 的跨模組引用單獨檢查本檔時會被 ui-check 誤判為未定義（工具限制，見 OQ-16），需與 `.dev/F01-basic-kanban/ui-kanban-basic.md` 一起檢查才會消失；OQ-16 已由人工修正 ui-authoring-tools.py 解除
- ⚠️ 依 OQ-17／OQ-18（人工決策，F07 是類 Miro 畫布），「完成後去哪裡」與操作表「選擇 Board 進入」引用的 F01 `s-board` 未來會改成導向 F07 s-canvas（該 Screen 尚未定案，T7.02 完成前不掛反引號引用），暫不修改本畫面內容，待 D-09 一併處理

## s-board-create-dialog：建立 Board 對話框
所屬 Feature：Board 建立與成員邀請
類型：對話框
狀態：未討論

## s-member-management：Board 成員管理
所屬 Feature：Board 建立與成員邀請
類型：列表
狀態：未討論

## s-board-delete-dialog：刪除 Board 對話框
所屬 Feature：Board 權限管理
類型：對話框
狀態：未討論

## s-card-assignee-picker：卡片負責人選取
所屬 Feature：卡片負責人指派
類型：對話框
狀態：未討論

## s-cards-by-assignee：依負責人查詢卡片
所屬 Feature：卡片負責人指派
類型：列表
狀態：未討論

## s-activity-log：看板活動紀錄
所屬 Feature：檢視看板活動紀錄
類型：列表
狀態：未討論
