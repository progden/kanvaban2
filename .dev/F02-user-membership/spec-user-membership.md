# 使用者與看板成員管理使用案例（BDD）

本文件以 BDD（Behavior-Driven Development）的 Gherkin 語法，描述使用者身分與看板存取權限相關的核心使用案例，涵蓋：

- User（使用者帳號）建立與登入／登出
- Board 建立與成員邀請（Owner 可有多位，邀請即刻生效）
- Board 權限管理（Owner 與 Member 的權限差異）
- Board 存取權限（我能看到哪些 Board）
- Card 負責人指派（只有 Owner 角色，限定看板成員，可多選）

本功能的設計結論：新增 `User`、`BoardMembership` 兩個 Aggregate Root；`Card.assignee` 由自由文字改為參照看板成員，並改為可複選、只有負責人（Owner）角色，沒有另外的 Member 概念。

本次疊代範圍排除：帳號註冊流程的其他細節（例如信箱驗證）、拒絕邀請情境（邀請即生效，沒有「待接受」的中間狀態）。

## 名詞定義

| 名詞 | 說明 |
|------|------|
| User（使用者） | 可登入系統的帳號，具備 `username`（帳號 ID，登入用，全系統不可重複）、`displayName`（顯示名字，看板上顯示用，可以與其他帳號重複；建立時未指定則預設等於 `username`）、`password`；密碼可以留白，長度上限 40 字，字元不限制（可含英文大小寫、符號） |
| ActivityRecord（活動紀錄） | 記錄一筆操作事件的行為人、動作內容與發生時間，依附在某個 Board 之下；涵蓋這個 Board 及其下 Swimlane、Stage、Card、BoardMembership 的異動事件（見「Aggregate 事件盤點」） |
| Board（看板） | 既有 Aggregate，新增 `createdBy` 欄位參照建立者 |
| BoardMembership（看板成員關係） | 描述某個 User 對某個 Board 的存取角色，邀請即生效，沒有待接受狀態 |
| Board Owner（看板擁有者） | Board 的管理角色：可邀請／移除／升級成員、可新增／重新命名／刪除 Swimlane 與 Stage、可刪除 Board；同一個 Board 可以有多位 Owner，但至少要保留一位 |
| Board Member（看板成員） | 被加入 Board 的人，只能新增／編輯／移動／刪除卡片與留言，不能碰成員管理、看板結構（Swimlane/Stage）或刪除 Board |
| Card 負責人（Card Owner） | Card 上代表「由誰負責這項工作」的角色，只有這一種角色（沒有 Card 層級的 Member 概念），與 Board Owner 是不同概念；一張 Card 可以有多位負責人，一位成員也可以同時是多張 Card 的負責人 |
| 操作時間（occurredAt） | 本文件中 Board／Card 相關事件（例如卡片負責人指派）的「事件發生時間」、「活動紀錄時間」一律代表該 Board 的 Board Clock 當下時間（`BoardClock.now()`）；User 建立、BoardMembership（邀請／角色變更）維持系統時間，不受影響。詳見 `.dev/F04-board-clock/spec-board-clock.md` |

## Aggregate 標記說明

`User`、`Board`、`BoardMembership`、`Card` 是各自獨立的 Aggregate。每個 Scenario 前方以 Gherkin 註解標記該情境會存取哪個 Aggregate、以及是「讀取」還是「寫入」，格式如下：

```gherkin
# Related aggregate:
#   user: read
#   boardMembership: read, write
```

- 只列出實際會用到的 aggregate；沒用到的可以省略整行。
- `read` 代表需要先查詢既有資料才能決定如何寫入或驗證；`write` 代表會真正新增/修改/刪除該 aggregate 的資料。

## 變更紀錄（Change Log）

| 日期 | 票號 | 類型 | 摘要 |
|------|------|------|------|
| 2026-09-13 | CR-004 | 變更 | 名詞定義補上「操作時間（occurredAt）」，明訂 Board/Card 事件時間一律取自 Board Clock（見 F04），User／BoardMembership 事件維持系統時間；既有 Scenario 文字不需修改，故不掛 Scenario 層級 tag |
| 2026-09-13 | CR-004 | 開發完成 | `kanban-core` 的 `Board`／`Card` 事件時間已全面改用 Board Clock；User／BoardMembership 事件維持系統時間不受影響。CR-004 狀態改「處理完成」 |
| 2026-09-13 | F05 | 新增 | 「卡片負責人指派」Feature 補上「拖曳成員頭像到卡片上，追加該成員為負責人」與「拖曳已經是負責人的成員頭像到卡片上，不重複新增」兩條 Scenario，供 F05 人員 Workload 表的拖曳追加負責人操作使用；本檔尚未進入開發，可直接補上，不需開 CR。「拖曳已存在負責人不重複新增」採靜默忽略、不產生活動紀錄的假設，理由：與「指派多位負責人」情境的「負責人集合」語意一致（`assignTo` 追加時本來就是集合操作，重複元素不改變集合，不視為一次有效變更），對應 `spec-workload.md` Open Question 的定案 |
| 2026-09-13 | F05 | 開發完成 | `kanban-core` 的 `Card` 新增 `addAssignee`（追加單一負責人，重複則靜默忽略、不產生活動紀錄），對應上述兩條 Scenario 的實作 |

---

## Feature: 建立使用者帳號

```gherkin
Feature: 建立使用者帳號
  身為系統的使用者
  我想要建立一個帳號，密碼規則越單純越好
  以便快速取得系統存取權而不被過度嚴格的密碼規則卡住

  # Related aggregate:
  #   user: write
  Scenario: 建立帳號時密碼可以留白
    When 我建立一個帳號 "user2"，密碼留白
    Then 該帳號應該建立成功
    And 我應該能用空白密碼登入這個帳號

  # Related aggregate:
  #   user: write
  Scenario: 密碼長度剛好 40 字可以建立成功
    When 我建立一個帳號 "user3"，密碼長度為 40 個字，包含英文大小寫與符號
    Then 該帳號應該建立成功

  # Related aggregate:
  #   user: write
  Scenario: 密碼長度超過 40 字則建立失敗
    When 我建立一個帳號 "user4"，密碼長度為 41 個字
    Then 系統應該顯示錯誤訊息 "密碼長度不可超過 40 個字"
    And 該帳號不應該被建立

  # Related aggregate:
  #   user: write
  Scenario: 帳號 ID（username）不可重複
    Given 系統中已存在帳號 "user1"
    When 我嘗試建立另一個帳號 "user1"
    Then 系統應該顯示錯誤訊息 "此帳號已被使用"
    And 不應該建立新的帳號

  # Related aggregate:
  #   user: write
  Scenario: 建立帳號需要同時提供帳號 ID 與顯示名字
    When 我建立一個帳號，帳號 ID 為 "user5"，顯示名字為 "王小明"
    Then 該帳號應該建立成功
    And 該帳號的顯示名字應該是 "王小明"

  # Related aggregate:
  #   user: read, write
  Scenario: 顯示名字可以與其他帳號重複，帳號 ID 不可以
    Given 系統中已存在帳號 ID "user1"，顯示名字為 "王小明"
    When 我建立一個新帳號，帳號 ID 為 "user6"，顯示名字也為 "王小明"
    Then 該帳號應該建立成功
    And 系統中應該同時存在兩個顯示名字為 "王小明" 的帳號，帳號 ID 分別是 "user1" 與 "user6"
```

---

## Feature: 使用者登入與登出

```gherkin
Feature: 使用者登入與登出
  身為系統的使用者
  我想要用帳號密碼登入與登出
  以便安全地存取我有權限的 Board

  # Related aggregate:
  #   user: read
  Scenario: 使用正確帳號密碼登入
    Given 系統中存在帳號 "user1"，密碼為 "correct-password"
    When 我輸入帳號 "user1" 與密碼 "correct-password"
    And 我送出登入表單
    Then 我應該登入成功
    And TopBar 應該顯示我的名稱 "user1"

  # Related aggregate:
  #   user: read
  Scenario: 密碼錯誤時登入失敗
    Given 系統中存在帳號 "user1"，密碼為 "correct-password"
    When 我輸入帳號 "user1" 與密碼 "wrong-password"
    And 我送出登入表單
    Then 系統應該顯示錯誤訊息 "帳號或密碼錯誤"
    And 我應該仍停留在登入頁面

  # Related aggregate:
  #   user: read
  Scenario: 帳號不存在時登入失敗
    Given 系統中不存在帳號 "ghost-user"
    When 我輸入帳號 "ghost-user" 與任意密碼
    And 我送出登入表單
    Then 系統應該顯示錯誤訊息 "帳號或密碼錯誤"

  Scenario: 登出後回到登入頁面
    Given 我已登入系統，帳號為 "user1"
    When 我點擊「登出」
    Then 我應該回到登入頁面
    And 我應該無法在不重新登入的情況下存取 Board
```

---

## Feature: Board 建立與成員邀請

```gherkin
Feature: Board 建立與成員邀請
  身為 Board 的擁有者
  我想要建立 Board、邀請其他使用者加入，並可以指定多位共同擁有者
  以便與團隊成員共同協作與管理

  Background:
    Given 我已登入系統，帳號為 "user1"

  # Related aggregate:
  #   board: write
  #   boardMembership: write
  Scenario: 建立 Board 的人自動成為 Owner
    When 我建立一個名為 "產品開發看板" 的 Board
    Then 該 Board 的建立者應該顯示為 "user1"
    And 我對該 Board 的角色應該是 "Owner"
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「建立看板」

  # Related aggregate:
  #   boardMembership: read, write
  Scenario: Owner 邀請其他使用者，邀請即刻生效
    Given 我是 Board "產品開發看板" 的 Owner
    And 系統中存在帳號 "雅婷"
    When 我邀請 "雅婷" 加入這個 Board，角色為 "Member"
    Then "雅婷" 應該立即成為這個 Board 的 Member，不需要對方確認
    And "雅婷" 應該能在自己的 Board 列表中看到 "產品開發看板"
    And Board 的成員數徽章應該增加 1
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「邀請 雅婷 加入看板，角色為 Member」

  # Related aggregate:
  #   boardMembership: read
  Scenario: 邀請已經是成員的使用者
    Given "雅婷" 已經是 Board "產品開發看板" 的 Member
    When 我再次邀請 "雅婷" 加入這個 Board
    Then 系統應該顯示錯誤訊息 "此使用者已經是看板成員"

  # Related aggregate:
  #   boardMembership: read, write
  Scenario: Owner 將 Member 升級為共同 Owner
    Given "雅婷" 是 Board "產品開發看板" 的 Member
    When 我將 "雅婷" 的角色變更為 "Owner"
    Then "雅婷" 對這個 Board 的角色應該是 "Owner"
    And 這個 Board 現在應該有 2 位 Owner："user1" 與 "雅婷"
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「將 雅婷 的角色變更為 Owner」

  # Related aggregate:
  #   boardMembership: read, write
  Scenario: 多位 Owner 都擁有相同的管理權限
    Given "雅婷" 與 "user1" 都是 Board "產品開發看板" 的 Owner
    And 系統中存在帳號 "建宏"
    When "雅婷" 邀請 "建宏" 加入這個 Board
    Then "建宏" 應該成為這個 Board 的 Member
    When "雅婷" 將 "建宏" 從這個 Board 移除
    Then "建宏" 應該無法再存取這個 Board

  # Related aggregate:
  #   boardMembership: read, write
  Scenario: 還有其他 Owner 時，可以移除其中一位 Owner
    Given Board "產品開發看板" 有 2 位 Owner："user1" 與 "雅婷"
    When 我將 "雅婷" 從這個 Board 移除
    Then "雅婷" 應該不再是這個 Board 的成員
    And Board "產品開發看板" 應該仍保留 "user1" 這位 Owner
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「將 雅婷 移出看板」

  # Related aggregate:
  #   boardMembership: read, write
  Scenario: Board 至少保留一個 Owner
    Given 我是 Board "產品開發看板" 唯一的 Owner
    When 我嘗試將自己從這個 Board 移除
    Then 系統應該顯示錯誤訊息 "看板至少需要保留一位 Owner"
    And 我對該 Board 的成員關係不應該被移除

  # Related aggregate:
  #   boardMembership: read, write
  #   card: read, write
  Scenario: 移除仍是卡片負責人的成員時需要確認
    Given "雅婷" 是 Board "產品開發看板" 的 Member
    And "雅婷" 是這個 Board 中 2 張卡片的負責人
    When 我嘗試將 "雅婷" 從這個 Board 移除
    Then 系統應該顯示確認訊息，告知 "雅婷" 仍是 2 張卡片的負責人，移除後這些卡片會變成未指派
    When 我確認移除
    Then "雅婷" 應該不再是這個 Board 的成員
    And 這 2 張卡片的負責人欄位都應該變成未指派

  # Related aggregate:
  #   boardMembership: read, write
  #   card: read, write
  Scenario: 卡片有多位負責人時，移除其中一位成員只清空該成員
    Given "雅婷" 與 "建宏" 都是卡片 "設計登入頁面" 的負責人
    And "雅婷" 與 "建宏" 都是 Board "產品開發看板" 的 Member
    When 我確認將 "雅婷" 從這個 Board 移除
    Then 卡片 "設計登入頁面" 的負責人應該只剩下 "建宏"
```

---

## Feature: Board 權限管理（Owner 與 Member 的權限差異）

```gherkin
Feature: Board 權限管理
  身為 Board 的 Owner
  我想要保有管理成員與看板結構的專屬權限
  以便避免一般成員誤動看板設定或成員名單

  Background:
    Given 我已登入系統，帳號為 "user1"
    And 我是 Board "產品開發看板" 的 Owner
    And "雅婷" 是這個 Board 的 Member

  # Related aggregate:
  #   boardMembership: read
  Scenario: Member 無法邀請其他成員
    Given 系統中存在帳號 "建宏"
    When "雅婷" 嘗試邀請 "建宏" 加入這個 Board
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以邀請成員"
    And "建宏" 不應該成為這個 Board 的成員

  # Related aggregate:
  #   boardMembership: read
  Scenario: Member 無法移除或升級成員
    When "雅婷" 嘗試將自己升級為 "Owner"
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以變更成員角色"

  # Related aggregate:
  #   board: read
  #   boardMembership: read
  Scenario: Member 無法新增、重新命名或刪除 Swimlane 與 Stage
    Given 看板目前有 1 個 Swimlane "預設泳道"
    When "雅婷" 嘗試新增一個 Swimlane
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以調整看板結構"
    And 看板的 Swimlane 數量不應該改變

  # Related aggregate:
  #   board: read, write
  #   boardMembership: read
  #   card: read, write
  Scenario: 只有 Owner 可以刪除 Board，刪除後底下的資料一併刪除
    Given 這個 Board 有 2 個 Swimlane、3 個 Stage，以及數張卡片
    When "雅婷" 嘗試刪除這個 Board
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以刪除看板"
    When 我以 Owner 身分刪除這個 Board
    Then 這個 Board 應該不再存在
    And 這個 Board 底下的所有 Swimlane、Stage 與卡片都應該一併被刪除

  # Related aggregate:
  #   boardMembership: read
  #   card: write
  Scenario: Member 可以正常新增與編輯卡片
    When "雅婷" 在這個 Board 新增一張卡片 "撰寫測試案例"
    Then 該卡片應該被成功建立
```

---

## Feature: Board 存取權限（我的 Board 列表）

```gherkin
Feature: Board 存取權限
  身為系統的使用者
  我想要只看到我有權限的 Board
  以便不被無關的 Board 干擾

  Background:
    Given 我已登入系統，帳號為 "user1"

  # Related aggregate:
  #   boardMembership: read
  Scenario: Board 列表只顯示我有權限的 Board
    Given 我是 Board "產品開發看板" 的 Owner
    And 我是 Board "行銷活動排程" 的 Member
    And 存在另一個我沒有權限的 Board "客服問題追蹤"
    When 我開啟「我的 Board」列表
    Then 列表應該顯示 "產品開發看板" 與 "行銷活動排程"
    And 列表不應該顯示 "客服問題追蹤"

  # Related aggregate:
  #   board: read
  #   boardMembership: read
  Scenario: 非成員嘗試直接開啟 Board 應該被拒絕
    Given 存在一個我沒有權限的 Board "客服問題追蹤"
    When 我嘗試直接開啟 Board "客服問題追蹤"
    Then 系統應該顯示錯誤訊息 "你沒有權限存取這個看板"
```

---

## Feature: 卡片負責人指派

```gherkin
Feature: 卡片負責人指派
  身為看板的成員
  我想要從看板成員中選擇一位或多位卡片負責人
  以便清楚追蹤每張卡片由誰負責，而不是靠自由輸入文字

  Background:
    Given 我已登入系統，帳號為 "user1"
    And 我已開啟一個名為 "產品開發看板" 的看板
    And "雅婷" 與 "建宏" 都是這個看板的 Member
    And 看板中存在一張卡片 "設計登入頁面"

  # Related aggregate:
  #   boardMembership: read
  #   card: write
  Scenario: 指派多位負責人給卡片
    When 我開啟卡片 "設計登入頁面" 的詳細編輯畫面
    And 我在「負責人」欄位選擇 "雅婷" 與 "建宏"
    And 我儲存變更
    Then 卡片的負責人應該是 "雅婷" 與 "建宏"
    And 卡片縮圖應該同時顯示 "雅婷" 與 "建宏"
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「將卡片負責人設定為 雅婷、建宏」

  # Related aggregate:
  #   boardMembership: read
  Scenario: 負責人選單只列出這個看板的成員
    Given 存在另一個使用者 "志明"，並非這個看板的成員
    When 我開啟卡片 "設計登入頁面" 的負責人選單
    Then 選單應該顯示 "雅婷" 與 "建宏"
    And 選單不應該顯示 "志明"

  # Related aggregate:
  #   card: write
  Scenario: 卡片可以沒有負責人
    Given 卡片 "設計登入頁面" 目前沒有指派負責人
    When 我開啟卡片的詳細編輯畫面
    Then 「負責人」欄位應該顯示為未指派
    And 卡片縮圖不應該顯示負責人資訊

  # Related aggregate:
  #   card: write
  Scenario: 從卡片移除其中一位負責人
    Given 卡片 "設計登入頁面" 的負責人是 "雅婷" 與 "建宏"
    When 我在負責人欄位中移除 "建宏"
    And 我儲存變更
    Then 卡片的負責人應該只剩下 "雅婷"
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「將 建宏 從卡片負責人中移除」

  # Related aggregate:
  #   card: read
  Scenario: 一位成員同時是多張卡片的負責人
    Given "雅婷" 是卡片 "設計登入頁面" 的負責人
    And 看板中還有另一張卡片 "撰寫 API 文件"，負責人也是 "雅婷"
    When 我查看 "雅婷" 負責的卡片清單
    Then 清單應該同時包含 "設計登入頁面" 與 "撰寫 API 文件"

  # Related aggregate:
  #   boardMembership: read
  #   card: write
  Scenario: 拖曳成員頭像到卡片上，追加該成員為負責人
    Given 卡片 "設計登入頁面" 目前的負責人只有 "建宏"
    When 我將 "雅婷" 的頭像拖曳到卡片 "設計登入頁面" 上
    Then 卡片 "設計登入頁面" 的負責人應該包含 "建宏" 與 "雅婷"
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「將卡片負責人設定為 建宏、雅婷」

  # Related aggregate:
  #   boardMembership: read
  #   card: write
  Scenario: 拖曳已經是負責人的成員頭像到卡片上，不重複新增
    Given 卡片 "設計登入頁面" 目前的負責人是 "雅婷" 與 "建宏"
    When 我將 "雅婷" 的頭像拖曳到卡片 "設計登入頁面" 上
    Then 卡片 "設計登入頁面" 的負責人應該仍然只有 "雅婷" 與 "建宏"
    And 不應該產生新的活動紀錄
```

---

## Aggregate 事件盤點（活動紀錄用）

活動紀錄要「把操作的人記清楚，包括做了什麼事情」，所以先盤點各 Aggregate 會發生哪些事件、目前是否已記錄操作人：

| Aggregate | 事件 | 現況 | 操作人記錄 |
|-----------|------|------|-----------|
| Board | 建立 Board | 本文件新增 | 已補上（見上方「建立 Board 的人自動成為 Owner」） |
| Board | 刪除 Board | F01 已上線（`spec-kanban-basic.md`「只有 Owner 可以刪除看板」） | 尚未記錄；目前刪除仍等同封存，不在 F01 規格範圍內，暫緩（CR-001 決議） |
| BoardMembership | 邀請成員 / 移除成員 / 升級為 Owner | 本文件新增 | 已補上（見上方各情境） |
| Swimlane | 新增 / 命名 / 排序 / 刪除 | F01 已上線 | 已補上（CR-001，已處理完成） |
| Stage | 新增 / 命名 / 排序 / 刪除 | F01 已上線 | 已補上（CR-001，已處理完成） |
| Card | 建立 / 編輯 / 刪除 / 跨 Swimlane 或 Stage 移動 | F01 已上線 | 已補上（CR-001，已處理完成） |
| Card | 新增留言 | F01 已上線 | 已記錄留言者，不需要 CR |
| Card | 指派 / 移除負責人 | 本文件新增（CR-002：負責人改為多選、參照看板成員） | 已補上（見上方各情境） |

F02 是尚未進入開發的規格，可以直接補上操作人記錄；F01 的事件已經上線，依 `cr-convention.md` 第 1 節，任何行為變更（包含補記錄操作人）都要先開 CR，不能直接改 `spec-kanban-basic.md`。CR-001（Swimlane/Stage/Card 補操作人）與 CR-002（負責人改為多選）都已登記並處理完成。

---

## Feature: 檢視看板活動紀錄

```gherkin
Feature: 檢視看板活動紀錄
  身為 Board 的成員
  我想要看到這個看板最近發生了哪些操作、由誰執行
  以便掌握團隊協作的異動歷程

  Background:
    Given 我已登入系統，帳號為 "user1"
    And 我是 Board "產品開發看板" 的 Owner

  # Related aggregate:
  #   boardMembership: read
  Scenario: 活動紀錄依時間新到舊排序，並顯示操作人與動作
    Given "user1" 建立了這個 Board
    And "user1" 邀請 "雅婷" 加入這個 Board
    When 我開啟這個 Board 的活動紀錄
    Then 活動紀錄應該依時間由新到舊列出，且每一筆都顯示操作人與動作內容
    And 最上面一筆應該是 "user1" 邀請 "雅婷" 加入看板
```

Board／Swimlane／Stage／Card 既有事件的操作人記錄已隨 CR-001／CR-002 補上（各自記在 `Board.activityLog`／`Card.activityLog`）；但目前 `kanban-core` 每個 aggregate 只管自己的活動紀錄，還沒有一個地方把 Board、每張 Card、BoardMembership 的事件合併成一份、依時間排序的統一列表——這件事本質上是跨 aggregate 的查詢投影，留給 `kanban-spring` 有 application／persistence 層時再做（見 `design.md` 第 8 點）。本情境目前尚未實作。

---

## 待釐清 / 未來擴充（Open Questions）

- `Label`（標籤）Aggregate 設計不在本文件範圍內，將於獨立的 Feature 文件中處理。

## 實作備註（留給 `design.md`）

- 刪除 Board 對使用者而言是「底下的 Swimlane、Stage、Card 全部一併刪除」（如上述 Scenario），但實作上第一版可以用**封存（archive）**取代真正的實體刪除（例如加一個 `archivedAt` 欄位），行為上仍表現為使用者看不到、找不到這些資料即可，不必真的刪除資料列。
- 角色只有 `Owner`／`Member` 兩種，不需要唯讀 Viewer 或其他角色。
