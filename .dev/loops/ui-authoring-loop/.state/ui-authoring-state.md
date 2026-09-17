# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中
- 上一輪任務：T2.05（[F02] 定案 `s-board-create-dialog`，對應 `uc-create-board`）——已完成，`s-board-create-dialog` 八段內容補齊並標「已定案」；同步在已定案的 `s-board-list` 補上「前往建立 Board」導覽與操作列，讓兩畫面互相對得上（`DS-07` 只認「進入與離開」段的反引號）
- 下一個任務：T2.06（[F02] 定案 `s-member-management`，對應 `uc-invite-member`／`uc-change-member-role`／`uc-remove-member`，三個拒絕情境放對應操作「失敗時」欄）
- D-10 仍列 `doing`：其整檔 `ui-check=0` 驗收條件依賴 T2.05～T2.10 全部完成，內容本身已完成，待 T2.11 收尾任務整份歸零後可直接改 `done`，不需再動內容
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`／`s-board-list`／`s-board-create-dialog`；其餘 5 個畫面骨架未討論
- 已定義的共用 ID：無新增
- 最近 3 條 OQ：OQ-14／OQ-15／OQ-16（同前，本輪未新增 OQ）
- 待注意：跑 `./scripts/ui-check <單一檔案>` 務必加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；單檔案模式下 F02 對 F01 `s-board` 的跨模組引用仍會誤判 `REF-07`（OQ-16 已知限制，需與 F01 檔一起跑才會消失）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用；驗收條件斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」；本輪整份檔案 error 數 78→70。
