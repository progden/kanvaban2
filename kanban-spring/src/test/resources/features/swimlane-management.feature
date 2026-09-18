# 本檔為 .dev/F01-basic-kanban/spec-kanban-basic.md「Feature: Swimlane 管理」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: Swimlane 管理
  身為 看板使用者
  我想要新增、命名、排序與刪除 Swimlane
  以便依照類別（例如團隊、優先度）將工作項目分組呈現

  Background:
    Given 我已登入系統
    And 我已開啟一個名為 "產品開發看板" 的看板

  @CR-001 @uc-add-swimlane
  # Related aggregate:
  #   board: read, write
  #   swimlane: write
  Scenario: 新增一個 Swimlane
    Given 看板目前有 1 個 Swimlane "預設泳道"
    When 我點擊「新增 Swimlane」按鈕
    And 我輸入名稱 "緊急項目"
    And 我確認新增
    Then 看板應該顯示 2 個 Swimlane
    And 新的 Swimlane "緊急項目" 應該出現在看板最下方
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @uc-add-swimlane @fail-p1
  # Related aggregate:
  #   board: read
  Scenario: Swimlane 名稱不可為空
    Given 我正在新增一個 Swimlane
    When 我沒有輸入任何名稱就確認新增
    Then 系統應該顯示錯誤訊息 "Swimlane 名稱不可為空"
    And 不應該建立新的 Swimlane

  @CR-001 @uc-rename-swimlane
  # Related aggregate:
  #   board: read, write
  #   swimlane: read, write
  Scenario: 重新命名 Swimlane
    Given 看板中存在一個 Swimlane "緊急項目"
    When 我將該 Swimlane 重新命名為 "本週優先"
    Then 該 Swimlane 的名稱應該更新為 "本週優先"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-reorder-swimlane
  # Related aggregate:
  #   board: read, write
  #   swimlane: read, write
  Scenario: 拖曳調整 Swimlane 順序
    Given 看板中依序存在 Swimlane "A"、"B"、"C"
    When 我將 Swimlane "C" 拖曳到 "A" 的上方
    Then Swimlane 的順序應該變為 "C"、"A"、"B"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-delete-swimlane
  # Related aggregate:
  #   board: read, write
  #   swimlane: read, write
  #   card: read, write
  Scenario: 刪除空的 Swimlane
    Given 看板中存在一個沒有任何卡片的 Swimlane "測試泳道"
    When 我刪除該 Swimlane
    Then 看板不應該再顯示 "測試泳道"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-delete-swimlane
  # Related aggregate:
  #   board: read, write
  #   swimlane: read, write
  #   card: read, write
  Scenario: 刪除包含卡片的 Swimlane 需要確認
    Given 看板中存在一個 Swimlane "本週優先"，其中包含 3 張卡片
    When 我嘗試刪除該 Swimlane
    Then 系統應該顯示確認訊息，告知該 Swimlane 內有 3 張卡片將一併被刪除
    When 我確認刪除
    Then 該 Swimlane 與其所有卡片都應該被移除
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @uc-delete-swimlane @fail-p1
  # Related aggregate:
  #   swimlane: read
  Scenario: 看板至少保留一個 Swimlane
    Given 看板中只剩下 1 個 Swimlane "預設泳道"
    When 我嘗試刪除該 Swimlane
    Then 系統應該顯示錯誤訊息 "看板至少需要保留一個 Swimlane"
    And 該 Swimlane 不應該被刪除
