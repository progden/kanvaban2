# 本檔為 .dev/F05-workload/spec-workload.md「Feature: 人員工作量檢視」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 人員工作量檢視
  身為 看板使用者
  我想要檢視每位成員目前手上有幾張進行中的卡片
  以便平衡團隊的工作分配

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And 看板成員包含 "雅婷" 與 "志明"
    And Stage "完成" 已設定角色為 Done

  @uc-view-workload
  # Related aggregate:
  #   board: read
  #   board-membership: read
  #   card: read
  Scenario: 檢視單一負責人的工作量
    Given "雅婷" 是 3 張進行中卡片的負責人
    When 我開啟 Workload 表
    Then "雅婷" 的工作量應該顯示為 3

  @uc-view-workload
  # Related aggregate:
  #   board: read
  #   board-membership: read
  #   card: read
  Scenario: 多位負責人的卡片，每人各算一張
    Given 卡片 "A" 的負責人同時是 "雅婷" 與 "志明"，且尚未完成
    When 我開啟 Workload 表
    Then "雅婷" 的工作量應該包含卡片 "A"
    And "志明" 的工作量應該包含卡片 "A"

  @uc-view-workload
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視未指派負責人的卡片數量
    Given 有 2 張進行中卡片沒有指派任何負責人
    When 我開啟 Workload 表
    Then "未指派" 的卡片數量應該顯示為 2

  @uc-view-workload
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 已完成的卡片不計入工作量
    Given "雅婷" 是 1 張已進入 Done 角色 Stage 的卡片的負責人
    When 我開啟 Workload 表
    Then "雅婷" 的工作量不應該包含該卡片

  @uc-drag-assign-card-owner
  # Related aggregate:
  #   board-membership: read
  #   card: write
  Scenario: 拖曳成員頭像到卡片上，追加該成員為負責人
    Given 卡片 "B" 目前的負責人只有 "志明"
    When 我將 "雅婷" 的頭像拖曳到卡片 "B" 上
    Then 卡片 "B" 的負責人應該包含 "志明" 與 "雅婷"

  @uc-drag-assign-card-owner
  # Related aggregate:
  #   board-membership: read
  #   card: write
  Scenario: 拖曳已經是負責人的成員頭像到卡片上，不重複新增
    Given 卡片 "B" 目前的負責人是 "志明" 與 "雅婷"
    When 我將 "雅婷" 的頭像拖曳到卡片 "B" 上
    Then 卡片 "B" 的負責人應該仍然只有 "志明" 與 "雅婷"
