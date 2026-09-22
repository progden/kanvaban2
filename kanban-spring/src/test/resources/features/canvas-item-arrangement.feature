# 本檔為 .dev/F07-canvas-layout/spec-canvas-layout.md「Feature: 畫布元素排列」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 畫布元素排列
  身為 畫布編輯者
  我想要 移動、縮放畫布上的元素、調整前後順序，並決定元素是否固定在畫面上
  以便 安排畫布的版面

  Background:
    Given 畫布已由系統建立
    And 畫布中存在元素 "銷售圖表"，錨定於畫布，左上角 (100, 200)，大小 300 × 200，可移動且可調整大小

  @uc-move-item
  # Related aggregate:
  #   item: read, write
  Scenario: 移動元素
    When 我將元素 "銷售圖表" 移動到左上角 (350, 40)
    Then 元素 "銷售圖表" 的左上角位於 (350, 40)
    And 元素 "銷售圖表" 的大小仍為 300 × 200

  @uc-move-item
  # Related aggregate:
  #   item: read, write
  Scenario: 元素可移動到負座標
    When 我將元素 "銷售圖表" 移動到左上角 (-500, -120)
    Then 元素 "銷售圖表" 的左上角位於 (-500, -120)

  @uc-move-item @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可移動不存在的元素
    Given 畫布中不存在元素 "備註"
    When 我將元素 "備註" 移動到左上角 (0, 0)
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-move-item @fail-p2
  # Related aggregate:
  #   item: read
  Scenario: 不可移動設為不可移動的元素
    Given 畫布中存在元素 "背景框"，左上角 (0, 0)，不可移動
    When 我將元素 "背景框" 移動到左上角 (50, 50)
    Then 拒絕，訊息為 "此元素不可移動"，且資料不變

  @uc-resize-item
  # Related aggregate:
  #   item: read, write
  Scenario: 以左上角為錨點調整元素大小
    When 我將元素 "銷售圖表" 調整為左上角 (100, 200)，大小 500 × 320
    Then 元素 "銷售圖表" 的大小為 500 × 320
    And 元素 "銷售圖表" 的左上角仍位於 (100, 200)

  @uc-resize-item
  # Related aggregate:
  #   item: read, write
  Scenario: 以右下角為錨點調整元素大小
    When 我將元素 "銷售圖表" 調整為左上角 (50, 150)，大小 350 × 250
    Then 元素 "銷售圖表" 的左上角位於 (50, 150)
    And 元素 "銷售圖表" 的大小為 350 × 250

  @uc-resize-item
  # Related aggregate:
  #   item: read, write
  Scenario: 不可移動但可調整大小的元素可以在原位置調整大小
    Given 畫布中存在元素 "固定圖表"，左上角 (0, 0)，大小 300 × 200，不可移動、可調整大小
    When 我將元素 "固定圖表" 調整為左上角 (0, 0)，大小 400 × 300
    Then 元素 "固定圖表" 的大小為 400 × 300
    And 元素 "固定圖表" 的左上角仍位於 (0, 0)

  @uc-resize-item @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可調整不存在元素的大小
    Given 畫布中不存在元素 "備註"
    When 我將元素 "備註" 調整為左上角 (0, 0)，大小 100 × 100
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-resize-item @fail-p2
  # Related aggregate:
  #   item: read
  Scenario: 不可調整設為不可調整大小的元素
    Given 畫布中存在元素 "背景框"，大小 800 × 600，不可調整大小
    When 我將元素 "背景框" 調整為左上角 (0, 0)，大小 400 × 300
    Then 拒絕，訊息為 "此元素不可調整大小"，且資料不變

  @uc-resize-item @fail-p3
  # Related aggregate:
  #   item: read
  Scenario: 調整後的寬與高必須大於 0
    When 我將元素 "銷售圖表" 調整為左上角 (100, 200)，大小 300 × 0
    Then 拒絕，訊息為 "寬與高必須大於 0"，且資料不變

  @uc-resize-item @fail-p4
  # Related aggregate:
  #   item: read
  Scenario: 不可移動的元素不可藉由調整大小改變位置
    Given 畫布中存在元素 "固定圖表"，左上角 (0, 0)，大小 300 × 200，不可移動、可調整大小
    When 我將元素 "固定圖表" 調整為左上角 (-50, -50)，大小 350 × 250
    Then 拒絕，訊息為 "此元素不可移動"，且資料不變

  @uc-set-item-capabilities
  # Related aggregate:
  #   item: write
  Scenario: 將元素設為不可移動、不可調整大小、不可移除
    When 我將元素 "銷售圖表" 設為不可移動、不可調整大小、不可移除
    Then 元素 "銷售圖表" 不可移動、不可調整大小且不可移除

  @uc-set-item-capabilities @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可設定不存在元素的能力
    Given 畫布中不存在元素 "備註"
    When 我將元素 "備註" 設為不可移動、不可調整大小
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-set-item-anchor
  # Related aggregate:
  #   item: read, write
  Scenario: 將畫布元素改為固定在畫面上
    Given 畫布中存在元素 "總覽儀表"，固定於畫面，層序為 1
    When 我將元素 "銷售圖表" 固定於畫面，左上角為畫面座標 (16, 160)，大小 240 × 120
    Then 元素 "銷售圖表" 固定於畫面
    And 元素 "銷售圖表" 的左上角位於畫面座標 (16, 160)
    And 元素 "銷售圖表" 的大小為畫面單位 240 × 120
    And 元素 "銷售圖表" 的層序高於元素 "總覽儀表"

  @uc-set-item-anchor
  # Related aggregate:
  #   item: read, write
  Scenario: 將畫面固定元素改回錨定於畫布
    Given 畫布中存在元素 "總覽儀表"，固定於畫面
    When 我將元素 "總覽儀表" 錨定於畫布，左上角為畫布座標 (400, 400)，大小 300 × 150
    Then 元素 "總覽儀表" 錨定於畫布
    And 元素 "總覽儀表" 的左上角位於 (400, 400)
    And 元素 "總覽儀表" 的大小為 300 × 150

  @uc-set-item-anchor @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可設定不存在元素的錨定方式
    Given 畫布中不存在元素 "備註"
    When 我將元素 "備註" 固定於畫面，左上角為畫面座標 (0, 0)，大小 100 × 100
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-reorder-item
  # Related aggregate:
  #   item: read, write
  Scenario: 將元素置頂
    Given 畫布中存在元素 "備註"，錨定於畫布，層序高於元素 "銷售圖表"
    When 我將元素 "銷售圖表" 置頂
    Then 元素 "銷售圖表" 的層序高於所有錨定於畫布的其他元素
    And 元素 "備註" 的層序不變

  @uc-reorder-item
  # Related aggregate:
  #   item: read, write
  Scenario: 將元素置底
    Given 畫布中存在元素 "備註"，錨定於畫布，層序低於元素 "銷售圖表"
    When 我將元素 "銷售圖表" 置底
    Then 元素 "銷售圖表" 的層序低於所有錨定於畫布的其他元素
    And 元素 "備註" 的層序不變

  @uc-reorder-item @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可調整不存在元素的層序
    Given 畫布中不存在元素 "備註"
    When 我將元素 "備註" 置頂
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變
