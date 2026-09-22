# 本檔為 .dev/F07-canvas-layout/spec-canvas-layout.md「Feature: 看板畫布初始化」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 看板畫布初始化
  身為 畫布編輯者
  我想要 開啟看板時自動看到畫布與看板本體
  以便 不用自己手動建立畫布或放置看板本體

  @uc-init-canvas
  # Related aggregate:
  #   canvas: read, write
  #   item: read, write
  Scenario: 開啟尚未建立畫布的看板時自動初始化
    Given 看板 "產品開發看板" 尚無 canvas
    When 我開啟看板 "產品開發看板"
    Then 該看板存在一個 canvas，縮放範圍為 0.1 ～ 4
    And 該 canvas 存在一個元素，元件識別碼為 "board"
    And 該元素的左上角位於 (0, 0)，大小為 900 × 600
    And 該元素錨定於畫布，層序為 1
    And 該元素可移動、可調整大小且可移除

  @uc-init-canvas
  # Related aggregate:
  #   canvas: read, write
  #   item: read, write
  Scenario: 開啟已有畫布的看板不重複初始化
    Given 看板 "產品開發看板" 已有 canvas
    And 該 canvas 存在元素 "看板本體"，元件識別碼為 "board"，層序為 1
    And 該 canvas 另存在元素 "銷售圖表"，層序為 2
    When 我再次開啟看板 "產品開發看板"
    Then 該看板仍只有一個 canvas
    And 元素 "看板本體" 的層序仍為 1
    And 元素 "銷售圖表" 的層序仍為 2

  @uc-init-canvas @fail-p1
  # Related aggregate:
  #   canvas: read
  Scenario: 不可為不存在的看板初始化畫布
    Given 看板 "已刪除的看板" 不存在
    When 我開啟看板 "已刪除的看板"
    Then 拒絕，訊息為 "看板不存在"，且資料不變
