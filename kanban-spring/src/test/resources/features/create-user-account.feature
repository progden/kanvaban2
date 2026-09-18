# 本檔為 .dev/F02-user-membership/spec-user-membership.md「Feature: 建立使用者帳號」的逐字複製（供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 建立使用者帳號
  身為 系統使用者
  我想要建立一個帳號，密碼規則越單純越好
  以便快速取得系統存取權而不被過度嚴格的密碼規則卡住

  @uc-create-user
  # Related aggregate:
  #   user: write
  Scenario: 建立帳號時密碼可以留白
    When 我建立一個帳號 "user2"，密碼留白
    Then 該帳號應該建立成功
    And 我應該能用空白密碼登入這個帳號

  @uc-create-user
  # Related aggregate:
  #   user: write
  Scenario: 密碼長度剛好 40 字可以建立成功
    When 我建立一個帳號 "user3"，密碼長度為 40 個字，包含英文大小寫與符號
    Then 該帳號應該建立成功

  @uc-create-user @fail-p1
  # Related aggregate:
  #   user: read
  Scenario: 密碼長度超過 40 字則建立失敗
    When 我建立一個帳號 "user4"，密碼長度為 41 個字
    Then 系統應該顯示錯誤訊息 "密碼長度不可超過 40 個字"
    And 該帳號不應該被建立

  @uc-create-user @fail-p2
  # Related aggregate:
  #   user: read
  Scenario: 帳號 ID（username）不可重複
    Given 系統中已存在帳號 "user1"
    When 我嘗試建立另一個帳號 "user1"
    Then 系統應該顯示錯誤訊息 "此帳號已被使用"
    And 不應該建立新的帳號

  @CR-006 @uc-create-user @fail-p3
  # Related aggregate:
  #   user: read
  Scenario: 帳號 ID（username）不可留空
    When 我建立一個帳號，帳號 ID 留空
    Then 系統應該顯示錯誤訊息 "使用者名稱不能為空"
    And 不應該建立新的帳號

  @uc-create-user
  # Related aggregate:
  #   user: write
  Scenario: 建立帳號需要同時提供帳號 ID 與顯示名字
    When 我建立一個帳號，帳號 ID 為 "user5"，顯示名字為 "王小明"
    Then 該帳號應該建立成功
    And 該帳號的顯示名字應該是 "王小明"

  @uc-create-user
  # Related aggregate:
  #   user: read, write
  Scenario: 顯示名字可以與其他帳號重複，帳號 ID 不可以
    Given 系統中已存在帳號 ID "user1"，顯示名字為 "王小明"
    When 我建立一個新帳號，帳號 ID 為 "user6"，顯示名字也為 "王小明"
    Then 該帳號應該建立成功
    And 系統中應該同時存在兩個顯示名字為 "王小明" 的帳號，帳號 ID 分別是 "user1" 與 "user6"
