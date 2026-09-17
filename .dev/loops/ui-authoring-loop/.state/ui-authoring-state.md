# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中，T2.04 卡在環境限制
- 上一輪任務：T2.04（[F02] 定案 `s-board-list`）——內容已完成八段，但因跨模組引用 F01 `s-board` 撞到 `ui-authoring-tools.py` 單檔案 design 載入限制（同 OQ-10 類型，見 OQ-16），標 `blocked`
- 下一個任務：T2.04 需人工解除 `blocked`（調整 `findings()` 的 design 路徑邏輯或驗收條件）後改回 `todo`／`done`，才能繼續 T2.05（`s-board-create-dialog`）
- 進行中任務的剩餘工作：無（`s-board-list` 內容完整，兩份 ui 檔一起檢查時該畫面 0 error/0 warn）
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`／`s-board-list`（`s-board-list` 因 OQ-16 標「討論中」），其餘 6 個畫面骨架未討論，F03～F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 3 條 OQ：OQ-14（`s-board-list` 的「Board 名稱」欄位缺 Attribute ID，同 OQ-09 模式）；OQ-15（`uc-reject-board-access-by-nonmember` 顯示訊息後停留畫面未定，暫定停留 `s-board-list`）；OQ-16（環境限制，`s-board-list` 對 F01 `s-board` 的跨模組 Screen 引用單檔案檢查會被誤判 REF-07）
- 待注意：跑 `./scripts/ui-check <單一檔案>` 務必加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；但跨 ui 檔的 Screen ID 引用（如 F02 引用 F01 `s-board`）單檔案檢查仍會被誤判 REF-07，需兩份 ui 檔一起帶入 `ui-check` 才會消失，這是 `ui-authoring-tools.py` `findings()` 的 `paths = [target_path]` 尚未修的限制（OQ-16）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用；驗收條件斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」。
