# 本檔為 .dev/F03-kanban-widgets/spec-kanban-widgets.md「Feature: WIP 與 Aging WIP 監控」的
# 逐字複製（供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: WIP 與 Aging WIP 監控
  身為 看板使用者
  我想要檢視各 Stage 目前的卡片數量，以及進行中卡片已經停留多久
  以便及早發現流程卡住的地方

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And Stage "進行中" 已設定角色為 Start，Stage "完成" 已設定角色為 Done

  @uc-view-wip
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視各 Stage 目前的卡片數量
    Given Stage "待辦" 有 3 張卡片，Stage "進行中" 有 2 張卡片，Stage "完成" 有 5 張卡片
    When 我開啟 WIP 圖表
    Then 應該顯示 Stage "待辦" 卡片數 3、"進行中" 卡片數 2、"完成" 卡片數 5

  @uc-view-aging-wip
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視進行中卡片的年齡
    Given 卡片 "D" 於 2026-09-01 進入 Start 角色的 Stage，目前仍在該 Stage 未進入 Done
    And 看板時間目前為 2026-09-12
    When 我開啟 Aging WIP 圖表
    Then 卡片 "D" 的年齡應該顯示為 11 天
