# 本檔為 .dev/F02-user-membership/spec-user-membership.md「Feature: Board 存取權限」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: Board 存取權限
  身為 系統使用者
  我想要只看到我有權限的 Board
  以便不被無關的 Board 干擾

  Background:
    Given 我已登入系統，帳號為 "user1"

  @uc-view-board-list
  # Related aggregate:
  #   board-membership: read
  Scenario: Board 列表只顯示我有權限的 Board
    Given 我是 Board "產品開發看板" 的 Owner
    And 我是 Board "行銷活動排程" 的 Member
    And 存在另一個我沒有權限的 Board "客服問題追蹤"
    When 我開啟「我的 Board」列表
    Then 列表應該顯示 "產品開發看板" 與 "行銷活動排程"
    And 列表不應該顯示 "客服問題追蹤"

  @uc-reject-board-access-by-nonmember
  # Related aggregate:
  #   board: read
  #   board-membership: read
  Scenario: 非成員嘗試直接開啟 Board 應該被拒絕
    Given 存在一個我沒有權限的 Board "客服問題追蹤"
    When 我嘗試直接開啟 Board "客服問題追蹤"
    Then 系統應該顯示錯誤訊息 "你沒有權限存取這個看板"
