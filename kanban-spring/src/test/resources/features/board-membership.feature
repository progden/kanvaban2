# 本檔為 .dev/F02-user-membership/spec-user-membership.md「Feature: Board 建立與成員邀請」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: Board 建立與成員邀請
  身為 Board 擁有者
  我想要建立 Board、邀請其他使用者加入，並可以指定多位共同擁有者
  以便與團隊成員共同協作與管理

  Background:
    Given 我已登入系統，帳號為 "user1"

  @CR-009 @uc-create-board
  # Related aggregate:
  #   board: write
  #   swimlane: write
  #   stage: write
  #   board-membership: write
  Scenario: 建立 Board 的人自動成為 Owner
    When 我建立一個名為 "產品開發看板" 的 Board
    Then 該 Board 的建立者應該顯示為 "user1"
    And 我對該 Board 的角色應該是 "Owner"
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「建立看板」

  @added @wip @CR-009 @uc-create-board
  # Related aggregate:
  #   board: write
  #   swimlane: write
  #   stage: write
  #   board-membership: write
  Scenario: 建立 Board 後帶有預設的 Swimlane 與 Stage
    When 我建立一個名為 "產品開發看板" 的 Board
    Then 該 Board 應該有 1 個 Swimlane "預設泳道"
    And 該 Board 的 Stage 應該依序為 "待辦"、"進行中"、"完成"
    And 該 Board 所有 Stage 的角色應該皆為 NONE

  @uc-invite-member
  # Related aggregate:
  #   board-membership: read, write
  Scenario: Owner 邀請其他使用者，邀請即刻生效
    Given 我是 Board "產品開發看板" 的 Owner
    And 系統中存在帳號 "雅婷"
    When 我邀請 "雅婷" 加入這個 Board，角色為 "Member"
    Then "雅婷" 應該立即成為這個 Board 的 Member，不需要對方確認
    And "雅婷" 應該能在自己的 Board 列表中看到 "產品開發看板"
    And Board 的成員數徽章應該增加 1
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「邀請 雅婷 加入看板，角色為 Member」

  @uc-invite-member @fail-p2
  # Related aggregate:
  #   board-membership: read
  Scenario: 邀請已經是成員的使用者
    Given "雅婷" 已經是 Board "產品開發看板" 的 Member
    When 我再次邀請 "雅婷" 加入這個 Board
    Then 系統應該顯示錯誤訊息 "此使用者已經是看板成員"

  @uc-change-member-role
  # Related aggregate:
  #   board-membership: read, write
  Scenario: Owner 將 Member 升級為共同 Owner
    Given "雅婷" 是 Board "產品開發看板" 的 Member
    When 我將 "雅婷" 的角色變更為 "Owner"
    Then "雅婷" 對這個 Board 的角色應該是 "Owner"
    And 這個 Board 現在應該有 2 位 Owner："user1" 與 "雅婷"
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「將 雅婷 的角色變更為 Owner」

  @uc-invite-member
  # Related aggregate:
  #   board-membership: read, write
  Scenario: 多位 Owner 都擁有相同的管理權限
    Given "雅婷" 與 "user1" 都是 Board "產品開發看板" 的 Owner
    And 系統中存在帳號 "建宏"
    When "雅婷" 邀請 "建宏" 加入這個 Board
    Then "建宏" 應該成為這個 Board 的 Member
    When "雅婷" 將 "建宏" 從這個 Board 移除
    Then "建宏" 應該無法再存取這個 Board

  @uc-remove-member
  # Related aggregate:
  #   board-membership: read, write
  #   card: write
  Scenario: 還有其他 Owner 時，可以移除其中一位 Owner
    Given Board "產品開發看板" 有 2 位 Owner："user1" 與 "雅婷"
    When 我將 "雅婷" 從這個 Board 移除
    Then "雅婷" 應該不再是這個 Board 的成員
    And Board "產品開發看板" 應該仍保留 "user1" 這位 Owner
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「將 雅婷 移出看板」

  @uc-remove-member @fail-p1
  # Related aggregate:
  #   board-membership: read
  Scenario: Board 至少保留一個 Owner
    Given 我是 Board "產品開發看板" 唯一的 Owner
    When 我嘗試將自己從這個 Board 移除
    Then 系統應該顯示錯誤訊息 "看板至少需要保留一位 Owner"
    And 我對該 Board 的成員關係不應該被移除

  @uc-remove-member
  # Related aggregate:
  #   board-membership: read, write
  #   card: read, write
  Scenario: 移除仍是卡片負責人的成員時需要確認
    Given "雅婷" 是 Board "產品開發看板" 的 Member
    And "雅婷" 是這個 Board 中 2 張卡片的負責人
    When 我嘗試將 "雅婷" 從這個 Board 移除
    Then 系統應該顯示確認訊息，告知 "雅婷" 仍是 2 張卡片的負責人，移除後這些卡片會變成未指派
    When 我確認移除
    Then "雅婷" 應該不再是這個 Board 的成員
    And 這 2 張卡片的負責人欄位都應該變成未指派

  @uc-remove-member
  # Related aggregate:
  #   board-membership: read, write
  #   card: read, write
  Scenario: 卡片有多位負責人時，移除其中一位成員只清空該成員
    Given "雅婷" 與 "建宏" 都是卡片 "設計登入頁面" 的負責人
    And "雅婷" 與 "建宏" 都是 Board "產品開發看板" 的 Member
    When 我確認將 "雅婷" 從這個 Board 移除
    Then 卡片 "設計登入頁面" 的負責人應該只剩下 "建宏"
