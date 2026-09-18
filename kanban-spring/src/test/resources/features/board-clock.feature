# 本檔為 .dev/F04-board-clock/spec-board-clock.md「Feature: 看板時間管理」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: 看板時間管理
  身為 看板使用者
  我想要調整、暫停或恢復看板的時鐘
  以便模擬不同時間點的操作，觀察圖表與統計如何變化

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And 看板時間目前為 2026-09-12 11:00:00

  @uc-guard-clock-monotonicity
  # Related aggregate:
  #   board: read, write
  Scenario: 把看板時間調整到未來後建立卡片，事件時間應為調整後的時間
    When 我將看板時間調整為 2026-09-12 13:00:00
    And 我建立一張卡片 "A"
    Then 卡片 "A" 的建立時間應該是 2026-09-12 13:00:00

  @uc-adjust-board-clock
  # Related aggregate:
  #   board: read, write
  Scenario: 看板時間可以往回調整
    Given 看板中最後一筆事件發生於 2026-09-12 13:00:00
    When 我將看板時間調整為 2026-09-12 12:00:00
    Then 看板時間應該顯示 2026-09-12 12:00:00

  @uc-guard-clock-monotonicity @fail-p1
  # Related aggregate:
  #   board: read
  Scenario: 看板時間早於最後一筆事件時，不可建立新事件
    Given 看板中最後一筆事件發生於 2026-09-12 13:00:00
    And 看板時間目前為 2026-09-12 12:00:00
    When 我嘗試建立一張卡片 "B"
    Then 系統應該顯示錯誤訊息 "看板時間早於最後一筆事件（13:00），無法建立新事件"
    And 不應該建立新的卡片

  @uc-pause-resume-board-clock
  # Related aggregate:
  #   board: write
  Scenario: 暫停看板時間
    When 我暫停看板時間
    And 我等待 10 秒
    Then 看板時間應該仍顯示 2026-09-12 11:00:00

  @uc-pause-resume-board-clock
  # Related aggregate:
  #   board: write
  Scenario: 恢復看板時間
    Given 看板時間目前為暫停狀態，暫停時的時間為 2026-09-12 11:00:00
    When 我恢復看板時間
    Then 看板時間應該從 2026-09-12 11:00:00 繼續隨系統時間前進

  @uc-adjust-board-clock @fail-p1
  # Related aggregate:
  #   board: read
  Scenario: 非 Owner 嘗試調整看板時間
    Given 我不是該 Board 的 Owner
    When 我嘗試將看板時間調整為 2026-09-12 13:00:00
    Then 系統應該顯示錯誤訊息 "只有 Owner 可以調整看板時間"
    And 看板時間應該維持不變

  @uc-adjust-board-clock
  # Related aggregate:
  #   board: write
  Scenario: 調整看板時間應記錄一筆活動紀錄
    Given 我是該 Board 的 Owner
    When 我將看板時間調整為 2026-09-12 13:00:00
    Then 應該新增一筆活動紀錄，說明看板時間被調整為 2026-09-12 13:00:00

  @uc-pause-resume-board-clock
  # Related aggregate:
  #   board: write
  Scenario: 暫停或恢復看板時間應記錄一筆活動紀錄
    Given 我是該 Board 的 Owner
    When 我暫停看板時間
    Then 應該新增一筆活動紀錄，說明看板時間已暫停
    When 我恢復看板時間
    Then 應該新增一筆活動紀錄，說明看板時間已恢復
