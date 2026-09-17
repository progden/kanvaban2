# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中，D-06～D-08 修正輪已全數完成
- 上一輪任務：D-08（[F01] 修正 `s-card-delete-dialog` 3 處把業務結果「卡片不變」寫進 UI 段落）——已修正，`ui-check(.dev/F01-basic-kanban/ui-kanban-basic.md)` 0 error
- 下一個任務：T2.04（[F02] 定案 `s-board-list`）
- 進行中任務的剩餘工作：無
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`（各自畫面內 0 error），其餘 7 個畫面骨架未討論，F03～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 3 條 OQ：OQ-11（未登入進入點，暫定 `s-signup` 為模組入口）；OQ-12（`s-signup` 完成後導向 `s-login`）；OQ-13（`s-login` 完成後導向 `s-board-list`）
- 待注意：`進入與離開` 段落所有反引號 ID 一律被 DS-03 驗證為 Screen 種類（含「中途放棄會怎樣」列），該段落內不可放 `uc-` 等其他種類 ID，即使是「不觸發 uc-xxx」這種敘述也要挪到操作表／驗收條件表達（D-08 實際修正時的偏差記錄）；跑 `./scripts/ui-check <單一檔案>` 務必加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用；驗收條件斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」，不寫領域狀態。
