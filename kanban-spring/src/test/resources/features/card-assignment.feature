# 本檔為 .dev/F02-user-membership/spec-user-membership.md「Feature: 卡片負責人指派」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 卡片負責人指派
  身為 看板使用者
  我想要從看板成員中選擇一位或多位卡片負責人
  以便清楚追蹤每張卡片由誰負責，而不是靠自由輸入文字

  Background:
    Given 我已登入系統，帳號為 "user1"
    And 我已開啟一個名為 "產品開發看板" 的看板
    And "雅婷" 與 "建宏" 都是這個看板的 Member
    And 看板中存在一張卡片 "設計登入頁面"

  @uc-set-card-assignees
  # Related aggregate:
  #   board-membership: read
  #   card: write
  Scenario: 指派多位負責人給卡片
    When 我開啟卡片 "設計登入頁面" 的詳細編輯畫面
    And 我在「負責人」欄位選擇 "雅婷" 與 "建宏"
    And 我儲存變更
    Then 卡片的負責人應該是 "雅婷" 與 "建宏"
    And 卡片縮圖應該同時顯示 "雅婷" 與 "建宏"
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「將卡片負責人設定為 雅婷、建宏」

  @uc-list-card-assignee-candidates
  # Related aggregate:
  #   board-membership: read
  Scenario: 負責人選單只列出這個看板的成員
    Given 存在另一個使用者 "志明"，並非這個看板的成員
    When 我開啟卡片 "設計登入頁面" 的負責人選單
    Then 選單應該顯示 "雅婷" 與 "建宏"
    And 選單不應該顯示 "志明"

  @uc-view-card-assignees
  # Related aggregate:
  #   card: read
  Scenario: 卡片可以沒有負責人
    Given 卡片 "設計登入頁面" 目前沒有指派負責人
    When 我開啟卡片的詳細編輯畫面
    Then 「負責人」欄位應該顯示為未指派
    And 卡片縮圖不應該顯示負責人資訊

  @uc-set-card-assignees
  # Related aggregate:
  #   card: write
  Scenario: 從卡片移除其中一位負責人
    Given 卡片 "設計登入頁面" 的負責人是 "雅婷" 與 "建宏"
    When 我在負責人欄位中移除 "建宏"
    And 我儲存變更
    Then 卡片的負責人應該只剩下 "雅婷"
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「將 建宏 從卡片負責人中移除」

  @uc-list-cards-by-assignee
  # Related aggregate:
  #   card: read
  Scenario: 一位成員同時是多張卡片的負責人
    Given "雅婷" 是卡片 "設計登入頁面" 的負責人
    And 看板中還有另一張卡片 "撰寫 API 文件"，負責人也是 "雅婷"
    When 我查看 "雅婷" 負責的卡片清單
    Then 清單應該同時包含 "設計登入頁面" 與 "撰寫 API 文件"

  @uc-assign-card-owner-by-drag
  # Related aggregate:
  #   board-membership: read
  #   card: write
  Scenario: 拖曳成員頭像到卡片上，追加該成員為負責人
    Given 卡片 "設計登入頁面" 目前的負責人只有 "建宏"
    When 我將 "雅婷" 的頭像拖曳到卡片 "設計登入頁面" 上
    Then 卡片 "設計登入頁面" 的負責人應該包含 "建宏" 與 "雅婷"
    And 應該產生一筆活動紀錄：操作人 "user1"、動作為「將卡片負責人設定為 建宏、雅婷」

  @uc-assign-card-owner-by-drag
  # Related aggregate:
  #   board-membership: read
  #   card: write
  Scenario: 拖曳已經是負責人的成員頭像到卡片上，不重複新增
    Given 卡片 "設計登入頁面" 目前的負責人是 "雅婷" 與 "建宏"
    When 我將 "雅婷" 的頭像拖曳到卡片 "設計登入頁面" 上
    Then 卡片 "設計登入頁面" 的負責人應該仍然只有 "雅婷" 與 "建宏"
    And 不應該產生新的活動紀錄
