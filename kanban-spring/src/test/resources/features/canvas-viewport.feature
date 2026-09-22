# 本檔為 .dev/F07-canvas-layout/spec-canvas-layout.md「Feature: 檢視區」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 檢視區
  身為 畫布編輯者
  我想要 平移與縮放自己的檢視區，並在下次開啟時回到同一位置
  以便 在畫布的任何範圍上工作，且不影響其他人看到的範圍

  Background:
    Given 畫布已由系統建立
    And 縮放範圍為 0.1 ～ 4
    And 我是使用者 "user1"，我的檢視區左上角位於畫布座標 (0, 0)，縮放比例為 1

  @uc-set-viewport
  # Related aggregate:
  #   canvas: read
  #   viewport: read, write
  Scenario: 平移檢視區
    When 我將檢視區設定為左上角 (1200, -300)，縮放比例 1
    Then 我的檢視區左上角位於畫布座標 (1200, -300)
    And 我的檢視區的縮放比例為 1

  @uc-set-viewport
  # Related aggregate:
  #   canvas: read
  #   viewport: read, write
  Scenario: 縮放檢視區
    When 我將檢視區設定為左上角 (150, 100)，縮放比例 2
    Then 我的檢視區的縮放比例為 2
    And 我的檢視區左上角位於畫布座標 (150, 100)

  @uc-set-viewport
  # Related aggregate:
  #   canvas: read
  #   viewport: read, write
  Scenario: 設定自己的檢視區不影響其他使用者
    Given 使用者 "user2" 的檢視區左上角位於畫布座標 (0, 0)，縮放比例為 1
    When 我將檢視區設定為左上角 (1200, -300)，縮放比例 1
    Then 使用者 "user2" 的檢視區左上角仍位於畫布座標 (0, 0)
    And 使用者 "user2" 的檢視區的縮放比例仍為 1

  @uc-set-viewport
  # Related aggregate:
  #   canvas: read
  #   viewport: read, write
  Scenario: 第一次設定檢視區時建立
    Given 使用者 "user3" 尚無檢視區
    And 我是使用者 "user3"
    When 我將檢視區設定為左上角 (300, 300)，縮放比例 1
    Then 我的檢視區左上角位於畫布座標 (300, 300)

  @uc-set-viewport
  # Related aggregate:
  #   canvas: read
  #   viewport: read, write
  Scenario: 平移檢視區不影響畫面固定元素
    Given 畫布中存在元素 "總覽儀表"，固定於畫面，左上角為畫面座標 (16, 16)
    When 我將檢視區設定為左上角 (1200, -300)，縮放比例 1
    Then 元素 "總覽儀表" 的左上角仍位於畫面座標 (16, 16)

  @uc-set-viewport @fail-p1
  # Related aggregate:
  #   canvas: read
  #   viewport: read
  Scenario: 縮放比例不可超出縮放範圍
    When 我將檢視區設定為左上角 (0, 0)，縮放比例 0
    Then 拒絕，訊息為 "縮放比例超出範圍"，且資料不變
