# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（人工介入，loop 之外）
- 目前階段：階段 3（跨模組收尾），T3.02 已解除 blocked
- 上一輪任務：T3.02（`.dev/CR.md` 回填 CR-001～CR-005 影響 ID，跑 `cr-check` 到通過）｜結果：done（見 OQ-11：人工把 spec 狀態欄拆成「定稿」（檔頭）／「開發中」（衍生，`.dev/CR.md` 有 CR 狀態「待處理」才算），`gh_05_diff` 改用衍生狀態判定；六個模組目前都沒有「待處理」CR，`cr-check`（含 `--cr CR-005`）0 error(s)、0 warning(s)，未補掛任何 `@CR-005`）
- 下一個任務：T3.03（把 `./scripts/spec-check --report` 輸出貼進 PDCA；PDCA 整理 OQ 檔全部 OQ-xx 摘要，含新增的 OQ-11）
- 進行中任務的剩餘工作：無，T3.02 已 done
- 已遷移完成的模組：F01～F06 全部完成，全檔 0 error；`./scripts/spec-check` 總計 0 error(s)、0 warning(s)；六份 spec 檔頭已改「狀態：定稿」
- 已定義的共用 ID：無新增
- 最近 3 條假設：OQ-11（人工決議，見 `spec-migration-open-questions.md`：spec 狀態拆兩層解決 OQ-10 矛盾，改動 `spec-convention.md`／`cr-convention.md`／`scripts/speccheck/model.py`／`checks/gh.py`／`checks/cr.py`，屬遷移完成後的規範修訂，非遷移輪次本身）
- 待注意：恢復 loop 前先確認 `runtime/baseline`、`runtime/gates/G1.approved` 等執行期檔案仍有效；下一輪從 T3.03 開始（`actionable` 應回報 T3.03）
