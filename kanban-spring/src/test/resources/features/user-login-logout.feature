# 本檔為 .dev/F02-user-membership/spec-user-membership.md「Feature: 使用者登入與登出」的逐字複製（供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 使用者登入與登出
  身為 系統使用者
  我想要用帳號密碼登入與登出
  以便安全地存取我有權限的 Board

  @uc-login
  # Related aggregate:
  #   user: read
  Scenario: 使用正確帳號密碼登入
    Given 系統中存在帳號 "user1"，密碼為 "correct-password"
    When 我輸入帳號 "user1" 與密碼 "correct-password"
    And 我送出登入表單
    Then 我應該登入成功
    And TopBar 應該顯示我的名稱 "user1"

  @added @CR-007 @uc-login
  # Related aggregate:
  #   user: read
  Scenario: 登入後 TopBar 顯示的是顯示名字而不是帳號 ID
    Given 系統中存在帳號 "user5"，顯示名字為 "王小明"，密碼為 "correct-password"
    When 我以帳號 "user5" 與密碼 "correct-password" 登入
    Then 我應該登入成功
    And TopBar 應該顯示我的名稱 "王小明"

  @uc-login @fail-p2
  # Related aggregate:
  #   user: read
  Scenario: 密碼錯誤時登入失敗
    Given 系統中存在帳號 "user1"，密碼為 "correct-password"
    When 我輸入帳號 "user1" 與密碼 "wrong-password"
    And 我送出登入表單
    Then 系統應該顯示錯誤訊息 "帳號或密碼錯誤"
    And 我應該仍停留在登入頁面

  @uc-login @fail-p1
  # Related aggregate:
  #   user: read
  Scenario: 帳號不存在時登入失敗
    Given 系統中不存在帳號 "ghost-user"
    When 我輸入帳號 "ghost-user" 與任意密碼
    And 我送出登入表單
    Then 系統應該顯示錯誤訊息 "帳號或密碼錯誤"

  @uc-logout
  # Related aggregate:
  #   user: read
  Scenario: 登出後回到登入頁面
    Given 我已登入系統，帳號為 "user1"
    When 我點擊「登出」
    Then 我應該回到登入頁面
    And 我應該無法在不重新登入的情況下存取 Board
