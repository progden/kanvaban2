# 本檔為 .dev/F01-basic-kanban/spec-kanban-basic.md「Feature: Stage（階段）管理」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: Stage（階段）管理
  身為 看板使用者
  我想要新增、命名、排序與刪除 Stage
  以便定義工作項目在流程中會經過的各個狀態

  Background:
    Given 我已登入系統
    And 我已開啟一個名為 "產品開發看板" 的看板
    And 看板目前的 Stage 依序為 "待辦"、"進行中"、"完成"

  @CR-001 @uc-add-stage
  # Related aggregate:
  #   board: read, write
  #   stage: write
  Scenario: 新增一個 Stage
    When 我點擊「新增 Stage」按鈕
    And 我輸入名稱 "驗收中"
    And 我選擇插入在 "進行中" 與 "完成" 之間
    Then Stage 順序應該變為 "待辦"、"進行中"、"驗收中"、"完成"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-add-stage
  # Related aggregate:
  #   board: read, write
  #   stage: write
  Scenario: 新增 Stage 時未指定插入位置，預設加到最後面
    When 我點擊「新增 Stage」按鈕
    And 我輸入名稱 "驗收中"
    And 我沒有指定插入位置就確認新增
    Then Stage 順序應該變為 "待辦"、"進行中"、"完成"、"驗收中"
    And 新的 Stage "驗收中" 應該出現在最後一個欄位
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-rename-stage
  # Related aggregate:
  #   board: read, write
  #   stage: read, write
  Scenario: 重新命名 Stage
    When 我將 Stage "待辦" 重新命名為 "規劃中"
    Then Stage 名稱應該更新為 "規劃中"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-reorder-stage
  # Related aggregate:
  #   board: read, write
  #   stage: read, write
  Scenario: 拖曳調整 Stage 順序
    When 我將 Stage "完成" 拖曳到 "待辦" 的左側
    Then Stage 順序應該變為 "完成"、"待辦"、"進行中"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-delete-stage
  # Related aggregate:
  #   board: read, write
  #   stage: read, write
  #   card: read, write
  Scenario: 刪除空的 Stage
    Given Stage "驗收中" 目前沒有任何卡片
    When 我刪除 Stage "驗收中"
    Then 看板不應該再顯示 "驗收中"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-delete-stage
  # Related aggregate:
  #   board: read, write
  #   stage: read, write
  #   card: read, write
  Scenario: 刪除包含卡片的 Stage 需要先轉移卡片
    Given Stage "進行中" 中包含 2 張卡片
    When 我嘗試刪除 Stage "進行中"
    Then 系統應該提示我選擇一個目的 Stage 來接收這 2 張卡片
    When 我選擇目的 Stage 為 "待辦"
    Then 這 2 張卡片應該被移動到 "待辦"
    And Stage "進行中" 應該被刪除
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @uc-delete-stage @fail-p1
  # Related aggregate:
  #   stage: read
  Scenario: 看板至少保留一個 Stage
    Given 看板中只剩下 1 個 Stage "待辦"
    When 我嘗試刪除該 Stage
    Then 系統應該顯示錯誤訊息 "看板至少需要保留一個 Stage"
    And 該 Stage 不應該被刪除

  @added @CR-003 @uc-set-stage-role
  # Related aggregate:
  #   board: read, write
  #   stage: read, write
  Scenario: 設定 Stage 角色
    Given 看板目前所有 Stage 的角色皆為 NONE
    When 我將 Stage "待辦" 的角色設定為 START
    Then Stage "待辦" 的角色應該變為 START
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間
    When 我將 Stage "進行中" 的角色設定為 START
    Then Stage "進行中" 的角色應該變為 START
    And Stage "待辦" 的角色應該自動變回 NONE
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間
