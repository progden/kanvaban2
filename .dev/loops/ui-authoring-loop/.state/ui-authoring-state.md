# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中
- 上一輪任務：T2.06（[F02] 定案 `s-member-management`，對應 `uc-invite-member`／`uc-change-member-role`／`uc-remove-member`）——已完成，八段內容補齊；三個拒絕 uc 中兩個（`uc-reject-invite-by-member`／`uc-reject-role-change-by-member`）放進對應操作列，第三個 `uc-reject-structure-change-by-member` 判定不屬本畫面範圍（對象是 Swimlane，非成員），未列入操作表，記入 OQ-25
- 下一個任務：T2.07（[F02] 定案 `s-board-delete-dialog`，對應 `uc-delete-board`）
- D-10 仍列 `doing`：其整檔 `ui-check=0` 驗收條件依賴 T2.05～T2.10 全部完成，內容本身已完成，待 T2.11 收尾任務整份歸零後可直接改 `done`，不需再動內容
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`／`s-board-list`／`s-board-create-dialog`；`s-member-management` 內容完整但因入口未定（OQ-22）維持「討論中」；其餘 4 個畫面骨架未討論
- 已定義的共用 ID：無新增
- 最近 4 條 OQ：OQ-22（`s-member-management` 進入路徑推論待 F01 `s-board` 補操作）／OQ-23（`r-board-member` 是否看得到本畫面完整清單）／OQ-24（`uc-change-member-role` 無降級情境，可逆性未定）／OQ-25（`uc-reject-structure-change-by-member` 不屬本畫面，待確認是否該回填 F01 兩個列表畫面）
- 待注意：跑 `./scripts/ui-check <單一檔案>` 務必加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；單檔案模式下 F02 對 F01 `s-board`／`s-swimlane-list`／`s-stage-list` 的跨模組引用仍會誤判 `REF-07`（OQ-16 已知限制，需與 F01 檔一起跑才會消失，本輪已驗證兩檔一起跑後這些 REF-07 全消失）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用；驗收條件斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」；本輪整份檔案 error 數 65→52（單檔跑法，`--spec` 皆已帶入）。
