# T-09-be-canvas-layout 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | Cucumber 的「拒絕」步驟沒有驗證 spec Then 子句的後半段「且資料不變」。 `CanvasSteps.java:365` 的 `@Then("^拒絕，訊息為 \"([^\"]+)\"，且資料不變$")` 只做兩件事： 斷言 HTTP status >= 400、斷言回應 body 的 `message` 相符，完全沒有檢查任何 `item`／`viewport` 的資料是否真的沒變。canvas-layout 五個 feature 檔共 12 個 `@fail-pN` Scenario 全部共用這一個步驟， 因此 spec 每一條 fail 情境的「且資料不變」目前都沒有被測到——例如 `canvas-item-arrangement.feature`「不可移動設為不可移動的元素」，即使實作在拒絕前先把 `背景框` 的座標寫進資料庫、只是回應 409，這個測試仍然會通過。 （本輪人工檢視 `Item.move`／`Item.resize`／`CanvasApplicationService.moveItems`／`removeItems`／ `setViewport` 的程式碼，確認驗證都發生在寫入前，行為目前應該是對的；問題在於「測試沒有把 spec 寫出來的斷言測出來」，之後任何人改動都不會被擋下。） 怎樣才算修好：`拒絕，訊息為 "..."，且資料不變` 這個 step 要真的斷言資料不變。例如在每個 When 發出請求前，對該 canvas 的全部 `canvas_items`（含 id／x／y／width／height／z／anchor／movable／ resizable／removable）與全部 `viewports` 取快照，這個 Then 再重新查一次並逐欄比對完全相同 （含筆數）。修好後 12 個 `@fail-pN` Scenario 仍須全數通過。 |
| D-02 | 1 | todo | — | `item.z` 的唯一鍵範圍偏離欄位表原文，卻沒有走 OQ 流程。 `spec-canvas-layout.md` 欄位表對 `item.z` 的限制欄逐字為：『必填、同一 `canvas` 內唯一』； 實作（`ItemJpaEntity.java:19`）下的唯一鍵是 `{"canvas_id", "anchor", "z"}`，也就是「同一 canvas 的同一錨定方式內唯一」，比原文寬。Dev 只在該類別的 Javadoc 裡自行寫下理由， `decision-log.md` 與 `open-questions.md` 都沒有這一條。 這不是單純的技術細節：spec 自己有兩段互相拉扯的文字（uc-place-item post 逐字為 『`item.z` 為同一錨定方式現有元素最大 z 值加一；尚無元素時為 1』、「其他名詞」的「層序」逐字為 『畫面固定元素永遠繪於畫布元素之上，兩者各自比較 z 值。因各元素為獨立 aggregate，z 值的唯一性 由儲存層唯一鍵保證』），照原文的 canvas 內唯一實作，會讓 `canvas-item-placement.feature`「放置固定在畫面上的元件」這種情境（畫布元素與畫面固定元素各自 從 1 起算）直接撞唯一鍵。依規則書第 5 節，spec 兩處文字不一致時要開 OQ 記錄，不可以只在程式碼 註解裡自己決定。 怎樣才算修好：用 `loopctl oq add --level 高 --blocking no --reason-code spec-conflict --scope canvas-layout/item.z --owner 人工` 開一則 OQ，情況欄用【兩處矛盾並列】，把上面三段原文 逐字並列（欄位表限制欄、uc-place-item post、「層序」段），推論另起一行說明為何取 `(canvas_id, anchor, z)`，選項列出維持現況與改成 canvas 內唯一兩種。實作可以維持現況，不必改。 |
