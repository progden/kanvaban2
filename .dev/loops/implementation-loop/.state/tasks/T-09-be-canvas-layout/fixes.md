# T-09-be-canvas-layout 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | Cucumber 的「拒絕」步驟沒有驗證 spec Then 子句的後半段「且資料不變」。 `CanvasSteps.java:365` 的 `@Then("^拒絕，訊息為 \"([^\"]+)\"，且資料不變$")` 只做兩件事： 斷言 HTTP status >= 400、斷言回應 body 的 `message` 相符，完全沒有檢查任何 `item`／`viewport` 的資料是否真的沒變。canvas-layout 五個 feature 檔共 12 個 `@fail-pN` Scenario 全部共用這一個步驟， 因此 spec 每一條 fail 情境的「且資料不變」目前都沒有被測到——例如 `canvas-item-arrangement.feature`「不可移動設為不可移動的元素」，即使實作在拒絕前先把 `背景框` 的座標寫進資料庫、只是回應 409，這個測試仍然會通過。 （本輪人工檢視 `Item.move`／`Item.resize`／`CanvasApplicationService.moveItems`／`removeItems`／ `setViewport` 的程式碼，確認驗證都發生在寫入前，行為目前應該是對的；問題在於「測試沒有把 spec 寫出來的斷言測出來」，之後任何人改動都不會被擋下。） 怎樣才算修好：`拒絕，訊息為 "..."，且資料不變` 這個 step 要真的斷言資料不變。例如在每個 When 發出請求前，對該 canvas 的全部 `canvas_items`（含 id／x／y／width／height／z／anchor／movable／ resizable／removable）與全部 `viewports` 取快照，這個 Then 再重新查一次並逐欄比對完全相同 （含筆數）。修好後 12 個 `@fail-pN` Scenario 仍須全數通過。 |
