# T-08-be-feature-cr-board 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | orphan CR 清單會重複列出同一張 CR：`FeatureCrBoardCalculator.calculate` 的 affects 迴圈裡，每一個找不到 Feature 卡的 affects 目標都會執行一次 `orphanCrIds.add(cr.crId())`。一張 CR 卡帶「CR-010」「affects:F98」「affects:F99」時，`orphanCrIds` 會是 ["CR-010","CR-010"]。spec post p3 寫的是『該 `card` 列入 orphan CR 清單』，主詞是卡片，一張卡只該列一次。 怎樣才算修好：同一張 CR 卡在 orphan 清單最多出現一次（例如改用 LinkedHashSet，或每張 CR 只判斷一次），並在 `FeatureCrBoardCalculatorTest` 補一個「一張 CR 帶兩個不存在的 affects 目標」的測試，斷言 `orphanCrIds` 只有一筆。 |
