# T-01-be-user 修正任務（D-xx）

> Review 退回時追加；編號在本任務內遞增。遷移前的舊編號（全域流水號）原樣保留。

## 2026-09-18 Review 第 1 輪退回

| ID | 母任務 | 狀態 | 描述 |
|---|---|---|---|
| D-01 | T-01-be-user | done | 已在 `open-questions.md` 新開 OQ-IMPL-09，逐字引用 `uc-create-user`／`uc-login`／`uc-logout` 的 `fail`／`post` 原文，列出目前程式碼採用的 HTTP 狀態碼對應（400／409／401／204／401）與其他選項；`state.md` 已補「待確認事項」。程式碼未變動。 |
| D-02 | T-01-be-user | done | 已在 `open-questions.md` 新開 OQ-IMPL-10（情況：兩處矛盾並列），逐字並列欄位表『非空、全系統不可重複』與 `uc-create-user` pre p2『`user.username` 在系統中不可重複』（沒有非空）、以及 `ui-user-membership.md` 資料表的對應標註，問「空 username 要怎麼拒絕、訊息是什麼」。未自行編訊息、未加防護，OQ 有結論前程式碼維持原狀，`state.md` 已補「待確認事項」。 |
