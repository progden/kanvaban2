# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 22）
- 目前階段：階段 3（F03 遷移）進行中
- 上一輪任務：上一輪驗證 PASS；本輪任務：T2.10（[F03] 遷移程序 1～6：狀態行、名詞／角色表、四個 Feature 的「身為」行）｜結果：done
- 下一個任務：T2.11（[F03] 「Cycle Time 與 Lead Time 分析」與「WIP 與 Aging WIP 監控」兩個 Feature 的 usecase 區塊＋tag＋Aggregate 註解）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03 已完成遷移程序 1～6（狀態行、名詞／角色表拆分、四個 Feature 的「身為」行），尚待 usecase 區塊與正文清理
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user；uc-add-swimlane 等 15 個 uc）與 F02（user、board-membership；r-system-user、r-board-owner、r-board-member）；F03 本輪未新增任何實體／角色，四個 Feature 的「身為」行改引用 F01 的 r-user（名稱「看板使用者」）
- 最近 3 條假設：低影響格式選擇：F03 的實體／欄位／關係表本輪留空（因 board、card 已在 F01 定義，未新增欄位或關係，避免 REF-08 重複定義）；角色表本輪留空（未新增角色，四個 Feature 的「身為」直接引用 F01 的 r-user 名稱）；原「## 名詞定義」單一表格內容整批搬到新「### 其他名詞」子表，文字未改動；未新增 OQ
- 待注意：全部 spec error 總數 149（F01=0、F02=0、F03=33、F04=54、F05=32、F06=30）
