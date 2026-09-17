# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中，穿插修正上一輪審查發現的 D-06～D-08
- 上一輪任務：D-07（[F02] 修正 `s-signup`／`s-login` 驗收條件中以領域狀態為主詞的斷言）——已修正，四處禁止詞彙已移除，`ui-check` error 數維持 91（未增加）
- 下一個任務：D-08（[F01] 修正 `s-card-delete-dialog` 業務結果寫進 UI 段落）
- 進行中任務的剩餘工作：無（D-07 已 done）；D-08 完成後接 T2.04（[F02] 定案 `s-board-list`）
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error，但 D-08 待修正 `s-card-delete-dialog` 寫法問題）；F02 已定案 `s-signup`／`s-login`（各自畫面內 0 error，驗收條件寫法問題已修），其餘 7 個畫面骨架未討論，F03～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 3 條 OQ：OQ-11（未登入進入點，暫定 `s-signup` 為模組入口）；OQ-12（`s-signup` 完成後導向 `s-login`）；OQ-13（`s-login` 完成後導向 `s-board-list`）
- 待注意：跑 `./scripts/ui-check <單一檔案>` 務必加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；核對「本畫面無 error」一律用該畫面的行號範圍比對，不可用名稱字串篩選（`ui-check` 輸出只有行號無 Screen ID，見 D-01／D-06 陷阱）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用，其他詞（如 `fail`）不可包反引號，否則觸發 REF-07；驗收條件斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」，不寫領域狀態（D-02／D-04／D-05／D-07 同一類問題，新畫面撰寫時要留意）。
