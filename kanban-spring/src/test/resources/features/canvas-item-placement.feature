# 本檔為 .dev/F07-canvas-layout/spec-canvas-layout.md「Feature: 元件放置」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 元件放置
  身為 畫布編輯者
  我想要 把元件放到畫布上，或從畫布上移除
  以便 決定畫布上有哪些內容

  Background:
    Given 畫布已由系統建立

  @uc-place-item
  # Related aggregate:
  #   item: read, write
  Scenario: 放置元件到空畫布
    Given 畫布中沒有任何元素
    When 我將元件 "銷售圖表" 放置到畫布，欄位如下：
      | 欄位 | 內容 |
      | X    | 100  |
      | Y    | 200  |
      | 寬   | 300  |
      | 高   | 200  |
    Then 畫布中存在元素 "銷售圖表"
    And 元素 "銷售圖表" 的左上角位於 (100, 200)
    And 元素 "銷售圖表" 的大小為 300 × 200
    And 元素 "銷售圖表" 錨定於畫布
    And 元素 "銷售圖表" 的層序為 1
    And 元素 "銷售圖表" 可移動、可調整大小且可移除

  @uc-place-item
  # Related aggregate:
  #   item: read, write
  Scenario: 新放置的元素位於同一錨定方式的最上層
    Given 畫布中存在 2 個錨定於畫布的元素，層序分別為 1、2
    When 我將元件 "備註" 放置到畫布，左上角 (0, 0)，大小 100 × 100
    Then 元素 "備註" 的層序為 3

  @uc-place-item
  # Related aggregate:
  #   item: read, write
  Scenario: 放置固定在畫面上的元件
    When 我將元件 "總覽儀表" 放置到畫布，欄位如下：
      | 欄位   | 內容   |
      | X      | 16     |
      | Y      | 16     |
      | 寬     | 240    |
      | 高     | 120    |
      | 錨定   | 畫面   |
    Then 元素 "總覽儀表" 固定於畫面
    And 元素 "總覽儀表" 的左上角位於畫面座標 (16, 16)

  @uc-place-item
  # Related aggregate:
  #   item: read, write
  Scenario: 放置時指定元素不可移動且不可調整大小
    When 我將元件 "背景框" 放置到畫布，欄位如下：
      | 欄位       | 內容 |
      | X          | 0    |
      | Y          | 0    |
      | 寬         | 800  |
      | 高         | 600  |
      | 可移動     | 否   |
      | 可調整大小 | 否   |
    Then 元素 "背景框" 不可移動且不可調整大小

  @uc-place-item @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 元素寬與高必須大於 0
    When 我將元件 "銷售圖表" 放置到畫布，左上角 (0, 0)，大小 0 × 100
    Then 拒絕，訊息為 "寬與高必須大於 0"，且資料不變

  @uc-remove-item
  # Related aggregate:
  #   item: read, write
  Scenario: 移除畫布元素
    Given 畫布中存在元素 "銷售圖表"，層序為 1
    And 畫布中存在元素 "備註"，層序為 2
    When 我移除元素 "銷售圖表"
    Then 畫布中不存在元素 "銷售圖表"
    And 元素 "備註" 的層序仍為 2

  @uc-remove-item @fail-p1
  # Related aggregate:
  #   item: read
  Scenario: 不可移除不存在的元素
    Given 畫布中不存在元素 "銷售圖表"
    When 我移除元素 "銷售圖表"
    Then 拒絕，訊息為 "畫布元素不存在"，且資料不變

  @uc-remove-item @fail-p2
  # Related aggregate:
  #   item: read
  Scenario: 不可移除設為不可移除的元素
    Given 畫布中存在元素 "背景框"，不可移除
    When 我移除元素 "背景框"
    Then 拒絕，訊息為 "此元素不可移除"，且資料不變
