# 本檔為 .dev/F01-basic-kanban/spec-kanban-basic.md「Feature: Card（卡片）編輯」的逐字複製
# （供 Cucumber 執行，spec 本身是行為唯一依據）。
Feature: Card（卡片）編輯
  身為 看板使用者
  我想要建立、編輯、移動與刪除卡片
  以便追蹤每一項工作的詳細內容與進度

  Background:
    Given 我已登入系統
    And 我已開啟一個名為 "產品開發看板" 的看板
    And 看板中存在 Swimlane "本週優先" 與 Stage "待辦"、"進行中"、"完成"

  @CR-001 @uc-add-card
  # Related aggregate:
  #   board: read
  #   card: write
  Scenario: 在指定 Swimlane 與 Stage 建立新卡片
    When 我在 Swimlane "本週優先" 的 Stage "待辦" 欄位點擊「新增卡片」
    And 我輸入標題 "設計登入頁面"
    And 我確認新增
    Then 該卡片應該出現在 Swimlane "本週優先" 與 Stage "待辦" 的交會格中
    And 卡片標題應該顯示為 "設計登入頁面"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @uc-add-card @fail-p1
  # Related aggregate:
  #   card: read
  Scenario: 卡片標題不可為空
    When 我嘗試新增一張標題為空的卡片
    Then 系統應該顯示錯誤訊息 "卡片標題不可為空"
    And 不應該建立新的卡片

  @CR-001 @CR-002 @uc-edit-card
  # Related aggregate:
  #   card: read, write
  Scenario: 編輯卡片詳細內容（不含負責人）
    Given 存在一張卡片 "設計登入頁面"
    When 我開啟該卡片的詳細編輯畫面
    And 我填寫以下欄位：
      | 欄位     | 內容                     |
      | 描述     | 設計符合品牌風格的登入頁面 |
      | 截止日期 | 2026-09-20               |
      | 標籤     | UI, 前端                 |
    And 我儲存變更
    Then 卡片應該保存上述所有欄位的內容
    And 卡片縮圖應該顯示截止日期 "2026-09-20"
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-move-card-swimlane
  # Related aggregate:
  #   board: read
  #   card: read, write
  Scenario: 在同一 Stage 內，卡片跨 Swimlane 移動
    Given 存在一張卡片 "設計登入頁面"，位於 Swimlane "本週優先" 與 Stage "待辦"
    And 看板中還有另一個 Swimlane "下週規劃"
    When 我將該卡片拖曳到 Swimlane "下週規劃" 的 Stage "待辦"
    Then 該卡片應該顯示於 Swimlane "下週規劃" 與 Stage "待辦" 的交會格中
    And 該卡片不應該再出現在 Swimlane "本週優先" 中
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @CR-001 @uc-move-card-stage
  # Related aggregate:
  #   board: read
  #   card: read, write
  Scenario: 卡片跨 Stage 移動（更新工作狀態）
    Given 存在一張卡片 "設計登入頁面"，位於 Stage "待辦"
    When 我將該卡片拖曳到 Stage "進行中"
    Then 該卡片應該顯示於 Stage "進行中"
    And 卡片的狀態異動應該被記錄，包含操作人、異動時間與異動前後的 Stage

  @uc-add-comment
  # Related aggregate:
  #   card: read
  #   comment: write
  Scenario: 為卡片新增留言
    Given 存在一張卡片 "設計登入頁面"
    When 我在卡片中新增留言 "已完成初稿，請協助審閱"
    Then 該留言應該顯示在卡片的留言列表中
    And 留言應該記錄留言者與留言時間

  @CR-001 @uc-delete-card
  # Related aggregate:
  #   card: read, write
  Scenario: 刪除卡片需要確認
    Given 存在一張卡片 "設計登入頁面"
    When 我點擊刪除該卡片
    Then 系統應該顯示確認訊息 "確定要刪除這張卡片嗎？此動作無法復原"
    When 我確認刪除
    Then 該卡片應該從看板中移除
    And 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間

  @uc-delete-card @fail-p2
  # Related aggregate:
  #   card: read
  Scenario: 取消刪除卡片
    Given 存在一張卡片 "設計登入頁面"
    When 我點擊刪除該卡片
    And 我在確認訊息中選擇取消
    Then 該卡片應該仍然存在於看板中
