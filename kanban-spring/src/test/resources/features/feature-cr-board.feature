# 本檔為 .dev/F06-feature-cr-board/spec-feature-cr-board.md「Feature: Feature／CR 追蹤表」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: Feature／CR 追蹤表
  身為 看板使用者
  我想要用看板卡片檢視每個 Feature 與 CR 目前的開發狀態
  以便掌握整個專案的進度，不需要另外查閱文件

  Background:
    Given 我已登入系統，並開啟 Board "產品開發看板"
    And Stage "進行中" 已設定角色為 Start，Stage "完成" 已設定角色為 Done

  @uc-view-feature-cr-board
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視 Feature 的開發狀態
    Given 卡片 "basic-kanban" 標籤為 "F01"，目前在角色為 Done 的 Stage
    When 我開啟 Feature／CR 追蹤表
    Then Feature "F01" 的狀態應該顯示為「已完成」

  @CR-013 @uc-view-feature-cr-board
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 檢視 CR 影響哪個 Feature 以及其狀態
    Given 卡片 "basic-kanban" 標籤為 "F01"
    And 卡片 "看板時間" 標籤為 "CR-004"，並帶有 "affects:F01" 標籤，目前在角色為 Start 的 Stage
    When 我開啟 Feature／CR 追蹤表
    Then Feature "F01" 底下應該顯示一筆狀態為「開發中」的 CR "CR-004"

  @uc-view-feature-cr-board
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: CR 指到不存在的 Feature 時列為 orphan
    Given 卡片 "某 CR" 標籤為 "CR-099"，並帶有 "affects:F99" 標籤
    When 我開啟 Feature／CR 追蹤表
    Then "CR-099" 應該出現在 orphan CR 清單中

  @uc-view-feature-cr-board
  # Related aggregate:
  #   board: read
  #   card: read
  Scenario: 標籤格式不合時列為警告，不影響其他卡片顯示
    Given 卡片 "格式錯誤的卡" 同時帶有 "F01" 與 "F02" 兩個 Feature 標籤
    When 我開啟 Feature／CR 追蹤表
    Then 應該顯示一筆警告訊息，說明該卡片有兩個 Feature 標籤
    And 其他卡片的 Feature／CR 統計不應該受影響

  @uc-view-feature-cr-board
  # Related aggregate:
  #   card: read
  Scenario: Feature／CR 標籤不分大小寫
    Given 卡片 "basic-kanban" 標籤為 "f01"
    When 我開啟 Feature／CR 追蹤表
    Then 應該視同標籤為 "F01" 顯示
