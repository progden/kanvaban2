# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-10-fe-shell：D-03、D-04 已處理，狀態改為 `review-pending`，等 Review 第 2 輪。
- D-03：已在 `open-questions.md` 新開 OQ-IMPL-11（TopBar 顯示 `user.username` 還是 `user.display-name`，情況：推論＋所本原文），逐字引用 `ui-user-membership.md` 第 78、90、26～27 行與 `spec-user-membership.md` 第 28～29 行。**待確認事項**：OQ-IMPL-11 未定案前 TopBar 維持顯示 `user.username`，未自行改成 `display-name`；若定案為 `display-name`，需回頭修改 T-01 的 `SessionResponse`／`UserResponse`（屬於 T-01-be-user 範圍，已完成合併，需另開修正）。
- D-04：已在 `decision-log.md` 追加更正條目（既有條目未改）：OQ-IMPL-09 已於 `06c0a14` 解除、升級為 ADR-001，不是「仍待處理」；`tasks.md` T-10 備註已同步更正。
- 程式碼未變動；`pnpm run test`／`build`／`lint`、`./gradlew clean build` 上一輪已綠，本輪未再改動程式碼故未重跑。
- 下一步：Review 第 2 輪；OQ-IMPL-11 如果還沒定案，只能附保留核准。
