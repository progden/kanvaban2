# 本檔為 .dev/F02-user-membership/spec-user-membership.md「Feature: Board 權限管理」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: Board 權限管理
  身為 Board 擁有者
  我想要保有管理成員與看板結構的專屬權限
  以便避免一般成員誤動看板設定或成員名單

  Background:
    Given 我已登入系統，帳號為 "user1"
    And 我是 Board "產品開發看板" 的 Owner
    And "雅婷" 是這個 Board 的 Member

  @uc-reject-invite-by-member
  # Related aggregate:
  #   board-membership: read
  Scenario: Member 無法邀請其他成員
    Given 系統中存在帳號 "建宏"
    When "雅婷" 嘗試邀請 "建宏" 加入這個 Board
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以邀請成員"
    And "建宏" 不應該成為這個 Board 的成員

  @uc-reject-role-change-by-member
  # Related aggregate:
  #   board-membership: read
  Scenario: Member 無法移除或升級成員
    When "雅婷" 嘗試將自己升級為 "Owner"
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以變更成員角色"

  @uc-reject-structure-change-by-member
  # Related aggregate:
  #   board: read
  #   board-membership: read
  Scenario: Member 無法新增、重新命名或刪除 Swimlane 與 Stage
    Given 看板目前有 1 個 Swimlane "預設泳道"
    When "雅婷" 嘗試新增一個 Swimlane
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以調整看板結構"
    And 看板的 Swimlane 數量不應該改變

  @uc-delete-board
  # Related aggregate:
  #   board: read, write
  #   board-membership: read
  #   card: read, write
  Scenario: 只有 Owner 可以刪除 Board，刪除後底下的資料一併刪除
    Given 這個 Board 有 2 個 Swimlane、3 個 Stage，以及數張卡片
    When "雅婷" 嘗試刪除這個 Board
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以刪除看板"
    When 我以 Owner 身分刪除這個 Board
    Then 這個 Board 應該不再存在
    And 這個 Board 底下的所有 Swimlane、Stage 與卡片都應該一併被刪除

  @uc-member-add-card
  # Related aggregate:
  #   board-membership: read
  #   card: write
  Scenario: Member 可以正常新增與編輯卡片
    When "雅婷" 在這個 Board 新增一張卡片 "撰寫測試案例"
    Then 該卡片應該被成功建立
