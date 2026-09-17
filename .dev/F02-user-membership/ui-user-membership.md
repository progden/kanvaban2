# 使用者與看板成員管理使用案例 UI 短規格

本檔案依 `.dev/conventions/ui-convention.md` 拆解 `.dev/F02-user-membership/spec-user-membership.md`，盤點對應畫面並逐一定案短規格。

## s-signup：註冊帳號
所屬 Feature：建立使用者帳號
類型：表單
狀態：討論中

### 目的
尚未擁有帳號的使用者在此輸入帳號 ID、顯示名字與密碼，建立一個新的系統帳號。

### 進入與離開
- 從哪裡進來：⚠️ 待確認（見 OQ-11），暫定為模組入口
- 完成後去哪裡：⚠️ 待確認（見 OQ-12），暫定導向 `s-login`
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
- 帳號 ID 為空時確認建立帳號，畫面維持顯示、輸入內容保留、顯示訊息，且不觸發 `uc-create-user` 建立新帳號
- 密碼長度超過 40 字時確認建立帳號，輸入內容保留、顯示訊息，該帳號不應該被建立
- 帳號 ID 與系統中既有帳號重複時確認建立帳號，輸入內容保留、顯示訊息，不應該建立新的帳號
- 密碼留白時確認建立帳號成功，且觸發 `uc-create-user`
- 未輸入顯示名字時確認建立帳號成功後，該帳號的顯示名字等於帳號 ID

### 待確認事項
- ⚠️ 「從哪裡進來」spec 未描述使用者如何抵達本畫面，見 OQ-11
- ⚠️ 「完成後去哪裡」`uc-create-user` post 未描述導向哪個畫面，見 OQ-12

## s-login：登入
所屬 Feature：使用者登入與登出
類型：表單
狀態：未討論

## s-board-list：我的 Board 列表
所屬 Feature：Board 存取權限
類型：列表
狀態：未討論

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
