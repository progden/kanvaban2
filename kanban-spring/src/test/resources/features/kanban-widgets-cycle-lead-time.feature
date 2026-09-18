# 本檔為 .dev/F03-kanban-widgets/spec-kanban-widgets.md「Feature: Cycle Time 與 Lead Time 分析」的
# 逐字複製（供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: Cycle Time 與 Lead Time 分析
  身為 看板使用者
  我想要檢視卡片從開始到完成花費的時間
  以便評估團隊的交付速度與承諾交期

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And Stage "進行中" 已設定角色為 Start，Stage "完成" 已設定角色為 Done

  @uc-view-cycle-lead-time
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視已完成卡片的 Cycle Time 與 Lead Time
    Given 卡片 "A" 於 2026-09-01 建立、2026-09-02 進入 Start、2026-09-05 進入 Done
    When 我開啟 Cycle Time / Lead Time 圖表
    Then 卡片 "A" 的 Lead Time 應該顯示為 4 天
    And 卡片 "A" 的 Cycle Time 應該顯示為 3 天
    And 統計摘要應該顯示 Lead Time 與 Cycle Time 的 P50、P85、P95 三個百分位數

  @uc-view-cycle-lead-time
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 沒有經過 Start 就完成的卡片，Cycle Time 不計入統計
    Given 卡片 "B" 於 2026-09-01 建立、未曾進入 Start 角色的 Stage、2026-09-03 進入 Done
    When 我開啟 Cycle Time / Lead Time 圖表
    Then 卡片清單中應該包含卡片 "B"，其 Cycle Time 顯示為「無」
    And 統計摘要的 Cycle Time 平均值與百分位計算應該排除卡片 "B"
    And 統計摘要應該顯示「排除計算的卡片數」為 1

  @uc-view-cycle-lead-time
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 卡片離開 Done 後再次完成，只計最後一次完成時間
    Given 卡片 "C" 於 2026-09-05 首次進入 Done，之後於 2026-09-06 被移出 Done，並於 2026-09-08 再次進入 Done
    When 我開啟 Cycle Time / Lead Time 圖表
    Then 卡片 "C" 的完成時間應該顯示為 2026-09-08
