# 本檔為 .dev/F02-user-membership/spec-user-membership.md「Feature: 檢視看板活動紀錄」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 檢視看板活動紀錄
  身為 看板使用者
  我想要看到這個看板最近發生了哪些操作、由誰執行
  以便掌握團隊協作的異動歷程

  Background:
    Given 我已登入系統，帳號為 "user1"
    And 我是 Board "產品開發看板" 的 Owner

  @uc-view-board-activity-log
  # Related aggregate:
  #   board: read
  #   board-membership: read
  Scenario: 活動紀錄依時間新到舊排序，並顯示操作人與動作
    Given "user1" 建立了這個 Board
    And "user1" 邀請 "雅婷" 加入這個 Board
    When 我開啟這個 Board 的活動紀錄
    Then 活動紀錄應該依時間由新到舊列出，且每一筆都顯示操作人與動作內容
    And 最上面一筆應該是 "user1" 邀請 "雅婷" 加入看板
