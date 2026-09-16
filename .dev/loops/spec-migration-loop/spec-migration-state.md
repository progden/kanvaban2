# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 41）
- 目前階段：階段 3（跨模組收尾）進行中
- 上一輪任務：T3.01（全部 spec 一起跑 spec-check 到 0 error）｜結果：done（開工前已是 0 error，本輪為確認性驗收，未改任何檔案）
- 下一個任務：T3.02（`.dev/CR.md` 回填 CR-001～CR-005 影響 ID，跑 cr-check 到通過）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01～F06 全部完成，全檔 0 error；`./scripts/spec-check` 總計 0 error(s)、0 warning(s)
- 已定義的共用 ID：無新增
- 最近 3 條假設：本輪無新增假設、無新增 OQ（T3.01 驗收條件開工前即已成立）
- 待注意：`.dev/CR.md` 目前仍不存在，T3.02 要建立並回填 CR-001～CR-004（依各 spec `@CR-` Scenario）與 CR-005（本次遷移改動的全部實體／uc）影響 ID，再跑 `cr-check`
