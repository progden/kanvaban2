# 使用者與看板成員管理使用案例 UI 短規格

本檔案依 `.dev/conventions/ui-convention.md` 拆解 `.dev/F02-user-membership/spec-user-membership.md`，盤點對應畫面並逐一定案短規格。

## s-signup：註冊帳號
所屬 Feature：建立使用者帳號
類型：表單
狀態：已定案

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
狀態：已定案

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
系統使用者在此檢視自己有權限（Owner 或 Member）的 Board 清單，選擇其中一個 Board 進入，或建立新的 Board。

### 進入與離開
- 從哪裡進來：登入成功後（依 `s-login`「完成後去哪裡」）
- 完成後去哪裡：選擇列表中的 Board 進入該 Board（F01 `s-board`；未來依 OQ-18 改為導向 F07 s-canvas，見本畫面「待確認事項」）；前往建立 Board 操作導向 `s-board-create-dialog`
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
| 前往建立 Board | — | 開啟 `s-board-create-dialog` | 不適用（純前端導覽） | 否 |

### 狀態
- 載入中：載入 Board 列表時顯示
- 空資料：使用者尚未是任何 `board` 的 Owner 或 Member 時，顯示空清單
- 錯誤：不適用（`uc-view-board-list` 無 fail 定義）
- 無權限：嘗試直接開啟不屬於自己的 Board 時，依 `uc-reject-board-access-by-nonmember` post 顯示訊息，停留本畫面
- 資料狀態差異：不適用（spec 未定義 Owner／Member 列表呈現上的差異）

### 驗收條件
- 使用者是其 Owner 或 Member 的 Board 都顯示於列表，沒有權限的 Board 不顯示，且觸發 `uc-view-board-list`
- 尚未擁有任何 Board 時，列表顯示空清單
- 選擇列表中的 Board 後開啟該 Board（F01 `s-board`）
- 嘗試直接開啟不屬於自己的 Board 時，觸發 `uc-reject-board-access-by-nonmember`，顯示訊息，停留本畫面
- 觸發前往建立 Board 動作，開啟 `s-board-create-dialog`，不觸發任何 Use Case

### 待確認事項
- ⚠️ 依 OQ-17／OQ-18（人工決策，F07 是類 Miro 畫布），「完成後去哪裡」與操作表「選擇 Board 進入」引用的 F01 `s-board` 未來會改成導向 F07 s-canvas（該 Screen 尚未定案，T7.02 完成前不掛反引號引用），暫不修改本畫面內容，待 D-09 一併處理

## s-board-create-dialog：建立 Board 對話框
所屬 Feature：Board 建立與成員邀請
類型：表單
狀態：已定案

### 目的
系統使用者在此輸入 Board 名稱，建立一個新的 Board，並自動成為該 Board 的 Owner。

### 進入與離開
- 從哪裡進來：`s-board-list` 的「前往建立 Board」操作
- 完成後去哪裡：關閉對話框，回到 `s-board-list`，新的 Board 顯示於列表中
- 中途放棄會怎樣：取消，關閉對話框，不建立 Board

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-board-owner` | 本畫面表單欄位 | 輸入 Board 名稱、確認建立、取消 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| Board 名稱 | `board.name` | 輸入 | 必填、非空，依名詞定義欄位表 `board.name` 限制 | 建立後作為該 Board 的名稱 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 確認建立 | `uc-create-board` | 依「完成後去哪裡」導向下一畫面，`s-board-list` 顯示新建立的 Board | 不適用（`uc-create-board` 無 fail 定義） | 否（可透過 `uc-delete-board` 刪除復原，見該 uc post） |
| 取消 | — | 關閉對話框 | — | 否 |

### 狀態
- 載入中：不適用（本畫面無需載入既有資料）
- 空資料：不適用（本畫面僅為輸入表單，無列表資料）
- 錯誤：不適用（`uc-create-board` 無 fail 定義）
- 無權限：不適用（`uc-create-board` roles 僅 `r-board-owner`，spec 未定義本畫面內的角色差異）
- 資料狀態差異：不適用

### 驗收條件
- 開啟時顯示空白的 Board 名稱輸入欄
- 輸入 Board 名稱後確認建立，觸發 `uc-create-board`，成功後依「完成後去哪裡」導向下一畫面，且 `s-board-list` 顯示新建立的 Board
- 取消後關閉對話框，且不觸發 `uc-create-board`

### 待確認事項
- （無）

## s-member-management：Board 成員管理
所屬 Feature：Board 建立與成員邀請
類型：對話框
狀態：討論中

### 目的
Board 擁有者在此檢視成員清單、邀請新成員、變更成員角色或移除成員。

### 進入與離開
- 從哪裡進來：⚠️ 待確認——spec 未描述如何抵達本畫面，推論應由 F01 `s-board` 的 Owner 專屬操作進入，但該操作尚未列在 `s-board` 操作表中，見 OQ-22
- 完成後去哪裡：邀請、變更角色、移除成員完成後停留本畫面，成員清單更新
- 中途放棄會怎樣：關閉本畫面，回到「從哪裡進來」的畫面，成員清單不變（同上，目的地待 OQ-22 確認）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-board-owner` | 完整成員清單（帳號、顯示名稱、角色） | 邀請成員、變更成員角色、移除成員 |
| `r-board-member` | 完整成員清單（同上，⚠️ 待確認，見 OQ-23） | 嘗試邀請成員、嘗試變更成員角色 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 成員帳號 | `user.username` | 顯示 | — | 清單中每一列的成員帳號 |
| 成員顯示名稱 | `user.display-name` | 顯示 | — | 清單中每一列的成員顯示名稱 |
| 成員角色 | `board-membership.role` | 顯示 | enum(Owner, Member)，依欄位表限制 | — |
| 邀請對象帳號 | `user.username` | 輸入 | 不是該 `board` 現有成員，依 `uc-invite-member` pre p2；⚠️ 是否要求帳號已存在，spec 未定義，見 OQ-27 | 邀請成員時輸入 |
| 邀請角色 | `board-membership.role` | 輸入（單選） | enum(Owner, Member) | 邀請時指定的初始角色 |
| 變更後角色 | `board-membership.role` | 輸入（單選） | 依 `uc-change-member-role` pre p2：目標須為該 `board` 的 Member；post 僅定義變更為 Owner，未定義將 Owner 降級為 Member 的情境，⚠️ 待確認，見 OQ-24 | — |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 邀請成員 | `uc-invite-member` | 成員清單顯示新加入的成員 | 依 `uc-invite-member` fail p2：輸入內容保留，顯示訊息 | 否（可透過「移除成員」復原，見 `uc-remove-member`） |
| 嘗試邀請成員 | `uc-reject-invite-by-member` | 不適用（本操作恆不成功） | 顯示訊息，停留本畫面 | 否 |
| 變更成員角色 | `uc-change-member-role` | 清單中該成員的角色顯示為 Owner | 不適用（`uc-change-member-role` 無 fail 定義） | ⚠️ 待確認（可逆性判斷不出，見 OQ-24） |
| 嘗試變更成員角色 | `uc-reject-role-change-by-member` | 不適用（本操作恆不成功） | 顯示訊息，停留本畫面 | 否 |
| 移除成員 | `uc-remove-member` | 依 post：目標成員自清單移除 | 依 `uc-remove-member` fail p1：不移除，顯示訊息 | 是（附掛於清單列上；若該成員仍為 `card` 負責人，依 pre p2 需先顯示確認訊息並告知張數） |

### 狀態
- 載入中：載入成員清單時顯示
- 空資料：不適用（`board` 至少有一位 Owner，成員清單至少一筆）
- 錯誤：邀請、變更角色、移除操作失敗時，依上方操作表顯示對應訊息
- 無權限：`r-board-member` 嘗試邀請或變更角色時，依 `uc-reject-invite-by-member`／`uc-reject-role-change-by-member` post 顯示訊息，操作不生效
- 資料狀態差異：不適用

### 驗收條件
- Owner 邀請系統中存在且非現有成員的帳號後，觸發 `uc-invite-member`，成員清單新增一筆、成員數加 1
- 邀請已是成員的帳號時，觸發 `uc-invite-member`，輸入內容保留、顯示訊息，成員清單不變
- 非 Owner 嘗試邀請成員時，觸發 `uc-reject-invite-by-member`，顯示訊息，成員清單不變
- Owner 將 Member 變更為 Owner 後，觸發 `uc-change-member-role`，該成員角色顯示為 Owner
- 非 Owner 嘗試變更成員角色時，觸發 `uc-reject-role-change-by-member`，顯示訊息，清單中該成員的角色顯示不變
- 移除非唯一 Owner 或 Member 後，觸發 `uc-remove-member`，該成員自清單移除
- 移除看板唯一 Owner 時，觸發 `uc-remove-member`，顯示訊息，該成員仍留在清單中
- 移除仍是卡片負責人的成員時，先顯示確認訊息並告知卡片張數，確認後才觸發 `uc-remove-member`

### 待確認事項
- ⚠️ 本畫面「從哪裡進來」與「完成後去哪裡（放棄時）」待確認，見 OQ-22
- ⚠️ `r-board-member` 是否真的能看到完整成員清單（僅操作被拒絕，畫面本身可見），或本來就看不到本畫面，見 OQ-23
- ⚠️ 「變更成員角色」操作是否可逆（spec 未定義 Owner 降級為 Member 的情境）待確認，見 OQ-24
- ⚠️ `uc-reject-structure-change-by-member`（非 Owner 嘗試調整看板結構）不屬於本畫面操作範圍（該操作對象是 Swimlane／Stage，非成員），本畫面不列入操作表；是否應回頭在 F01 `s-swimlane-list`／`s-stage-list` 的操作失敗欄跨模組引用，待確認，見 OQ-25
- ⚠️ 邀請對象帳號是否要求帳號已存在，`uc-invite-member` pre 未定義，待確認，見 OQ-27

## s-board-delete-dialog：刪除 Board 對話框
所屬 Feature：Board 權限管理
類型：對話框
狀態：討論中

### 目的
Board 擁有者刪除 Board 前確認，一併告知底下的 Swimlane、Stage 與卡片都將一同被刪除。

### 進入與離開
- 從哪裡進來：⚠️ 待確認——spec 未描述如何抵達本畫面，推論應由 F01 `s-board` 的 Owner 專屬操作進入，但該操作尚未列在 `s-board` 操作表中，見 OQ-26
- 完成後去哪裡：導向 `s-board-list`
- 中途放棄會怎樣：關閉對話框，回到進入前的畫面

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-board-owner` | 該 Board 名稱、其底下的 Swimlane 數、Stage 數與卡片數 | 確認刪除、取消 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| Board 名稱 | `board.name` | 顯示 | — | 要刪除的 Board |
| Swimlane 數 | `board`→`swimlane` 關係計數 | 顯示 | — | 提示使用者有幾個 Swimlane 將一併被刪除 |
| Stage 數 | `board`→`stage` 關係計數 | 顯示 | — | 提示使用者有幾個 Stage 將一併被刪除 |
| 卡片數 | `swimlane`→`card`、`stage`→`card` 關係計數（彙總屬於該 `board` 的所有 `card`） | 顯示 | — | 提示使用者有幾張卡片將一併被刪除 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 確認刪除 | `uc-delete-board` | 導向 `s-board-list` | 不適用（`uc-delete-board` 無 fail 定義） | 是（本畫面即確認） |
| 取消 | — | 關閉對話框 | — | 否 |

### 狀態
- 載入中：載入該 Board 的 Swimlane、Stage 與卡片數量時顯示
- 空資料：不適用（進入本畫面代表指定的 `board` 存在，其 `board`→`swimlane`、`board`→`stage` 關係 min 皆為 1）
- 錯誤：不適用（`uc-delete-board` 無 fail 定義）
- 無權限：不適用（`uc-delete-board` roles 僅 `r-board-owner`，spec 未定義本畫面內的角色差異）
- 資料狀態差異：不適用

### 驗收條件
- 開啟時顯示該 Board 名稱、Swimlane 數、Stage 數與卡片數
- 確認刪除後，觸發 `uc-delete-board`，導向 `s-board-list`
- 取消後關閉對話框，且不觸發 `uc-delete-board`

### 待確認事項
- ⚠️ 本畫面「從哪裡進來」spec 未定義，推論應由 F01 `s-board` 的 Owner 專屬操作進入，但該操作尚未列在 `s-board` 操作表中，見 OQ-26

## s-card-assignee-picker：卡片負責人選取
所屬 Feature：卡片負責人指派
類型：對話框
狀態：已定案

### 目的
看板成員在此從看板成員中選擇一位或多位卡片負責人。

### 進入與離開
- 從哪裡進來：F01 `s-card-detail` 的負責人指派入口
- 完成後去哪裡：回到 `s-card-detail`，負責人欄位顯示更新後的負責人名單
- 中途放棄會怎樣：關閉本畫面，不套用本次未儲存的勾選變更，卡片負責人維持進入前的樣子

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-board-member` | 負責人候選清單、卡片目前的負責人 | 勾選／取消勾選負責人、儲存變更、取消 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 負責人候選清單 | `board-membership` | 顯示 | 不顯示非該 `board` 成員的 `user`，依 `uc-list-card-assignee-candidates` post | 選單來源，僅列出該 `card` 所屬 `board` 的成員 |
| 已選負責人 | `card.assignees` | 顯示 / 輸入（多選） | 只能選擇該看板的成員，依欄位表 `card.assignees` 限制；依 `uc-view-card-assignees` post，沒有指派負責人時顯示為未指派 | 使用者從候選清單勾選的結果 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 儲存變更 | `uc-set-card-assignees` | 關閉本畫面，回 `s-card-detail`，負責人欄位顯示儲存後的名單，卡片縮圖同步顯示 | 不適用（`uc-set-card-assignees` 無 fail 定義） | 否（可重新開啟本畫面調整負責人，非不可逆操作） |
| 取消 | — | 關閉本畫面，不套用變更 | — | 否 |

### 狀態
- 載入中：載入負責人候選清單與卡片目前負責人時顯示
- 空資料：卡片目前沒有指派負責人時，依 `uc-view-card-assignees` post，候選清單全部維持未勾選
- 錯誤：不適用（`uc-set-card-assignees`、`uc-list-card-assignee-candidates`、`uc-view-card-assignees` 均無 fail 定義）
- 無權限：不適用（三個對應 Use Case 的 `roles` 皆僅 `r-board-member`，spec 未定義本畫面內的角色差異）
- 資料狀態差異：不適用

### 驗收條件
- 開啟時，候選清單只列出該 `card` 所屬 `board` 的成員，不含非成員
- 開啟時，卡片目前的負責人於候選清單中顯示為已勾選；卡片目前沒有負責人時，候選清單全部未勾選
- 勾選一位或多位候選人後儲存，觸發 `uc-set-card-assignees`，回到 `s-card-detail` 且負責人欄位顯示儲存後的名單
- 取消後關閉本畫面，且不觸發 `uc-set-card-assignees`，`s-card-detail` 的負責人欄位維持進入前的樣子

### 待確認事項
- （無）

## s-cards-by-assignee：依負責人查詢卡片
所屬 Feature：卡片負責人指派
類型：列表
狀態：討論中

### 目的
看板成員在此查看指定成員目前負責的卡片清單。

### 進入與離開
- 從哪裡進來：⚠️ 待確認——spec 未描述使用者從哪個既有畫面前往查看指定成員的負責卡片清單，見 OQ-28
- 完成後去哪裡：不適用（本畫面為唯讀清單，沒有提交或完成流程）
- 中途放棄會怎樣：關閉本畫面，回到來源畫面（來源待確認，同上）

### 角色與權限
| 角色 | 看得到 | 做得到 |
|---|---|---|
| `r-board-member` | 指定成員目前負責的卡片清單 | 查看清單 |

### 資料
| 欄位 | 來源 | 顯示 / 輸入 | 驗證 / 格式 | 說明 |
|---|---|---|---|---|
| 查詢對象 | `user.display-name` | 顯示 | — | 本清單查詢的目標成員 |
| 卡片清單 | `card`（透過 `card.assignees`） | 顯示 | — | 列出所有以查詢對象為負責人的 `card`，依 `uc-list-cards-by-assignee` post |
| 卡片標題 | `card.title` | 顯示 | 非空，依 F01 `spec-kanban-basic.md` 欄位表限制（跨模組引用） | 清單中每張卡片顯示的標題 |

### 操作
| 操作 | 觸發 | 成功後 | 失敗時 | 需確認？ |
|---|---|---|---|---|
| 開啟卡片詳情 | — | 開啟 F01 `s-card-detail`（跨模組） | 不適用 | 否 |

### 狀態
- 載入中：載入查詢對象的卡片清單時顯示
- 空資料：查詢對象目前沒有負責任何卡片時，清單顯示為空（`uc-list-cards-by-assignee` post 未排除此情況）
- 錯誤：不適用（`uc-list-cards-by-assignee` 無 fail 定義）
- 無權限：不適用（`uc-list-cards-by-assignee` 的角色僅 `r-board-member`，spec 未定義本畫面內的角色差異）
- 資料狀態差異：不適用

### 驗收條件
- 開啟時，清單列出所有以查詢對象為負責人的卡片，觸發 `uc-list-cards-by-assignee`
- 查詢對象同時是多張卡片的負責人時，清單同時包含這些卡片
- 點擊清單中的卡片開啟 F01 `s-card-detail`

### 待確認事項
- ⚠️ 本畫面「從哪裡進來」待確認，spec 未描述使用者如何抵達本畫面，見 OQ-28

## s-activity-log：看板活動紀錄
所屬 Feature：檢視看板活動紀錄
類型：列表
狀態：未討論
