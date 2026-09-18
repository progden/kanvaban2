# 本檔為 .dev/F03-kanban-widgets/spec-kanban-widgets.md「Feature: Throughput 與累積流量圖」的
# 逐字複製（供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: Throughput 與累積流量圖
  身為 看板使用者
  我想要檢視單位時間完成的卡片數量與各 Stage 卡片數量隨時間的變化
  以便掌握團隊的產出趨勢與流程瓶頸

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And Stage "進行中" 已設定角色為 Start，Stage "完成" 已設定角色為 Done

  @uc-view-throughput
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視每日完成卡片數量
    Given 2026-09-10 有 2 張卡片進入 Done，2026-09-11 有 1 張卡片進入 Done
    When 我開啟 Throughput 圖表，並選擇以「日」為單位
    Then 2026-09-10 的完成數應該顯示為 2
    And 2026-09-11 的完成數應該顯示為 1

  @uc-view-cfd
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視累積流量圖
    When 我開啟 CFD 圖表
    Then 應該顯示每一天、每個 Stage 的累積卡片數量
