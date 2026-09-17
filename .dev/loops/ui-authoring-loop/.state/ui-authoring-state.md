# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中
- 上一輪任務：T2.07（[F02] 定案 `s-board-delete-dialog`，對應 `uc-delete-board`）——已完成，八段內容補齊，比照 F01 `s-swimlane-delete-dialog` 前例（本畫面即確認）
- 下一個任務：T2.08（[F02] 定案 `s-card-assignee-picker`，對應 `uc-set-card-assignees`／`uc-list-card-assignee-candidates`／`uc-view-card-assignees`）
- D-10 仍列 `doing`：其整檔 `ui-check=0` 驗收條件依賴 T2.05～T2.10 全部完成，內容本身已完成，待 T2.11 收尾任務整份歸零後可直接改 `done`，不需再動內容
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`／`s-board-list`／`s-board-create-dialog`／`s-board-delete-dialog`；`s-member-management` 內容完整但因入口未定（OQ-22）維持「討論中」；`s-board-delete-dialog` 同樣因入口未定（OQ-26）維持「討論中」；其餘 3 個畫面骨架未討論
- 已定義的共用 ID：無新增
- 最近 4 條 OQ：OQ-23（`r-board-member` 是否看得到 `s-member-management` 完整清單）／OQ-24（`uc-change-member-role` 無降級情境，可逆性未定）／OQ-25（`uc-reject-structure-change-by-member` 不屬 `s-member-management`，待確認是否該回填 F01 兩個列表畫面）／OQ-26（`s-board-delete-dialog` 進入路徑推論待 F01 `s-board` 補操作，與 OQ-22 同類缺口但對象不同 Screen）
- 待注意：跑 `./scripts/ui-check <單一檔案>` 務必加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；單檔案模式下 F02 對 F01 `s-board`／`s-swimlane-list`／`s-stage-list` 的跨模組引用仍會誤判 `REF-07`（OQ-16 已知限制，需與 F01 檔一起跑才會消失）；「進入與離開」段落內的反引號會被 DS-03 解析為 Screen 引用，entity（如 `board`）在該段落不可加反引號，否則會誤判為「種類錯誤」（本輪修正過一次）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用；驗收條件斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」；本輪整份檔案 error 數 52→39（單檔跑法，`--spec` 皆已帶入）。
