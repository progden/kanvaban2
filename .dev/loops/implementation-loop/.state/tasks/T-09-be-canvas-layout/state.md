# T-09-be-canvas-layout state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

第 1 輪已完成 canvas-layout（F07）全部 11 個 use case 的實作與測試，兩個 commit（dev／test 分開）。

Review 應先看：
- `kanban-core` 三個新 Aggregate（`Canvas`／`Item`／`Viewport`）與其單元測試。
- `CanvasApplicationService` 的角色檢查（editor→`ensureCanEdit`／viewer 也可→`ensureMember`）與
  `item.z` 計算邏輯。
- `CanvasSteps.java` 的正規表示式 step definition 是否忠實對應 spec 逐字語意（非窮舉排列組合寫法，
  用子句解析器 `applyDescriptorClause`）。
- OQ-T-09-be-canvas-layout-01（不阻塞）：HTTP 狀態碼分配未在 spec 定義，已依既有慣例實作。

全部 130 個 Cucumber scenario 與既有單元測試皆綠燈（`./gradlew build --no-daemon` 通過）。
