# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中
- 上一輪任務：T2.08——定案 `s-card-assignee-picker`（對話框），對應 `uc-set-card-assignees`／`uc-list-card-assignee-candidates`／`uc-view-card-assignees`；「進入與離開」寫明從 F01 `s-card-detail` 進入、完成回到該畫面；`ui-check` 本檔 error 數 39 → 27（原骨架的 DS-02／DS-04 缺段落錯誤消除）
- 下一個任務：T2.09（[F02] 定案 `s-cards-by-assignee`：對應 `uc-list-cards-by-assignee`，類型：列表）
- D-10 仍列 `doing`：其整檔 `ui-check=0` 驗收條件依賴 T2.05～T2.10 全部完成，內容本身已完成，待 T2.11 收尾任務整份歸零後可直接改 `done`，不需再動內容
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`／`s-board-list`／`s-board-create-dialog`／`s-board-delete-dialog`／`s-card-assignee-picker`；`s-member-management` 內容完整但因入口未定（OQ-22）維持「討論中」；`s-board-delete-dialog` 因入口未定（OQ-26）維持「討論中」；`s-cards-by-assignee`／`s-activity-log` 骨架未討論
- 已定義的共用 ID：無新增
- 最近 4 條 OQ：OQ-24（`uc-change-member-role` 無降級情境，可逆性未定）／OQ-25（`uc-reject-structure-change-by-member` 不屬 `s-member-management`，待確認是否該回填 F01 兩個列表畫面）／OQ-26（`s-board-delete-dialog` 進入路徑推論待 F01 `s-board` 補操作）／OQ-27（`uc-invite-member` pre 未定義邀請對象帳號是否須已存在）
- 待注意：`s-card-assignee-picker` 目前有 DS-07 warn（沒有畫面導向它），因 F01 `s-card-detail` 的指派入口留待 T2.11 回填，屬預期；PDCA 每則 Check 段落務必貼 `./scripts/ui-check <本輪檔案> --spec ".dev/F[0-9][0-9]-*/spec-*.md"` 的最後一行；跑該指令務必加 `--spec`；單檔測試若跨模組引用（例如 `s-card-detail`）務必連同其他已存在的 ui 檔一起餵給 `ui-check`，否則會誤判 REF-07（`tools error-count` 已內建）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用；資料表「來源」欄只能填 Attribute 或 Entity ID，不可放 UseCase 反引號（DS-03），uc 相關說明改寫進「驗證/格式」或「說明」欄。
