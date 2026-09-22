# 本檔為 .dev/F07-canvas-layout/spec-canvas-layout.md「Feature: 畫布元素批次操作」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 畫布元素批次操作
  身為 畫布編輯者
  我想要 一次移動或移除多個元素
  以便 整組調整版面時不必逐一操作

  Background:
    Given 畫布已由系統建立
    And 畫布中存在元素 "銷售圖表"，錨定於畫布，左上角 (100, 200)，可移動
    And 畫布中存在元素 "備註"，錨定於畫布，左上角 (500, 200)，可移動

  @uc-move-items
  # Related aggregate:
  #   item: read, write
  Scenario: 批次移動多個元素
    When 我將元素 "銷售圖表"、"備註" 一起移動，位移量為 (+50, -30)
    Then 元素 "銷售圖表" 的左上角位於 (150, 170)
    And 元素 "備註" 的左上角位於 (550, 170)

  @uc-move-items @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 批次移動包含不存在的元素時全部不動
    Given 畫布中不存在元素 "舊圖表"
    When 我將元素 "銷售圖表"、"舊圖表" 一起移動，位移量為 (+50, 0)
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-move-items @fail-p2
  # Related aggregate:
  #   item: read
  Scenario: 不可同時批次移動畫布元素與畫面固定元素
    Given 畫布中存在元素 "總覽儀表"，固定於畫面，可移動
    When 我將元素 "銷售圖表"、"總覽儀表" 一起移動，位移量為 (+50, 0)
    Then 拒絕，訊息為 "不可同時移動畫布元素與畫面固定元素"，且資料不變

  @uc-move-items @fail-p3
  # Related aggregate:
  #   item: read
  Scenario: 批次移動包含不可移動的元素時全部不動
    Given 畫布中存在元素 "背景框"，錨定於畫布，左上角 (0, 0)，不可移動
    When 我將元素 "銷售圖表"、"背景框" 一起移動，位移量為 (+50, 0)
    Then 拒絕，訊息為 "所選元素中有不可移動的元素"，且資料不變

  @uc-remove-items
  # Related aggregate:
  #   item: read, write
  Scenario: 批次移除多個元素
    Given 畫布中存在元素 "背景框"，層序為 3
    When 我一起移除元素 "銷售圖表"、"備註"
    Then 畫布中不存在元素 "銷售圖表"
    And 畫布中不存在元素 "備註"
    And 元素 "背景框" 的層序仍為 3

  @uc-remove-items @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 批次移除包含不存在的元素時全部保留
    Given 畫布中不存在元素 "舊圖表"
    When 我一起移除元素 "銷售圖表"、"舊圖表"
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-remove-items @fail-p2
  # Related aggregate:
  #   item: read
  Scenario: 批次移除包含不可移除的元素時全部保留
    Given 畫布中存在元素 "背景框"，不可移除
    When 我一起移除元素 "銷售圖表"、"背景框"
    Then 拒絕，訊息為 "所選元素中有不可移除的元素"，且資料不變
