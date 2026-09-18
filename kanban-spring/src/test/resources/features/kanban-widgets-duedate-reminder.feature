# 本檔為 .dev/F03-kanban-widgets/spec-kanban-widgets.md「Feature: 截止日期提醒」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 截止日期提醒
  身為 看板使用者
  我想要檢視已逾期或即將到期的卡片
  以便優先處理有時間壓力的工作項目

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And 看板時間目前為 2026-09-12

  @uc-view-duedate-reminder
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視已逾期的卡片
    Given 卡片 "E" 截止日期為 2026-09-10，尚未完成
    When 我開啟逾期提醒圖表
    Then 卡片 "E" 應該出現在「已逾期」清單中

  @uc-view-duedate-reminder
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視即將到期的卡片
    Given 卡片 "F" 截止日期為 2026-09-14，尚未完成
    And 即將到期的門檻設定為 3 天
    When 我開啟逾期提醒圖表
    Then 卡片 "F" 應該出現在「即將到期」清單中

  @uc-view-duedate-reminder @fail-p1
  # Related aggregate:
  #   board: read
  Scenario: 門檻天數必須是 1 到 365 之間的正整數
    When 我將即將到期的門檻設定為 0 天
    Then 系統應該顯示錯誤訊息 "門檻天數必須是 1 到 365 之間的正整數"
