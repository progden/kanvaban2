# archive（遷移前的共用紀錄，唯讀）

2026-09-18 之前，所有任務共用 `decision-log.md`／`review.md`／`open-questions.md`／`state.md`／`adr.md`，並行 worktree 各自往檔尾追加，合併回整合分支時必衝突、流水號也撞號（T-11 與 T-02 各自開了 `OQ-IMPL-12`／`13`，合併時人工把 T-02 那邊重編為 `OQ-IMPL-14～17`）。

之後改成每個任務寫自己的 `../tasks/<task-id>/`，這裡的檔案原封保留：

- **不可再追加或修改**（唯一例外：人工在既有 OQ 底下補「解除說明」）。
- 裡面的 ID（`OQ-IMPL-01～17`、`ADR-001`、舊 `D-xx`）照舊有效，程式碼註解、`.dev/CR.md` 的引用不用改。
- 要看全部 OQ（新＋舊）用 `../../scripts/collect.sh oq`。
