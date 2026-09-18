# T-10-fe-shell 修正任務（D-xx）

> Review 退回時追加；編號在本任務內遞增。遷移前的舊編號（全域流水號）原樣保留。

## 2026-09-18 Review 第 1 輪退回

| ID | 母任務 | 狀態 | 描述 |
|---|---|---|---|
| D-03 | T-10-fe-shell | done | 已在 `open-questions.md` 新開 OQ-IMPL-11（情況：推論＋所本原文），逐字引用 `ui-user-membership.md` 第 78、90 行操作表／驗收條件、第 26～27 行資料表，以及 `spec-user-membership.md` 第 28～29 行欄位表，列出「TopBar 目前顯示 `user.username`」這個推論與 A／B／C 三個選項。OQ 定案前 TopBar 維持顯示 `user.username`，未自行改成 `display-name`，`state.md` 已補「待確認事項」。程式碼未變動。 |
| D-04 | T-10-fe-shell | done | 已在 `decision-log.md` 追加一則更正條目（既有條目未改），指出 OQ-IMPL-09 已於 `06c0a14`（早於本任務 Dev commit `4614e17`）解除並升級為 `adr.md` ADR-001；並說明 `ApiError.status` 的設計跟 ADR-001 分類表一致（例如未登入查 `GET /api/session` 回 401），後續任務依 ADR-001 狀態碼分類做基本分流，精確文案仍依回應內容的錯誤代碼／訊息決定。`tasks.md` T-10 備註、`state.md` 的過期字樣已同步更正。程式碼未變動。 |
