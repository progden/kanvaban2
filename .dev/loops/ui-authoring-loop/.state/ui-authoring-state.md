# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中
- 上一輪任務：D-11（[F02] 修正 `s-member-management` 把業務結果寫進 UI 段落）——已完成，操作表五處成功後／失敗時改成呈現方式、刪掉重述 post 的文字，驗收條件與資料表同步修正，邀請對象帳號是否須已存在改標 ⚠️ 並記入 OQ-27
- 下一個任務：D-12（[F02] 修正 `s-board-delete-dialog`／`s-board-create-dialog`／`s-board-list` 把領域狀態寫進 UI 段落）
- D-10 仍列 `doing`：其整檔 `ui-check=0` 驗收條件依賴 T2.05～T2.10 全部完成，內容本身已完成，待 T2.11 收尾任務整份歸零後可直接改 `done`，不需再動內容
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`／`s-board-list`／`s-board-create-dialog`／`s-board-delete-dialog`；`s-member-management` 內容完整（D-11 已修正業務結果問題）但因入口未定（OQ-22）維持「討論中」；`s-board-delete-dialog` 因入口未定（OQ-26）維持「討論中」；其餘 3 個畫面骨架未討論
- 已定義的共用 ID：無新增
- 最近 4 條 OQ：OQ-24（`uc-change-member-role` 無降級情境，可逆性未定）／OQ-25（`uc-reject-structure-change-by-member` 不屬 `s-member-management`，待確認是否該回填 F01 兩個列表畫面）／OQ-26（`s-board-delete-dialog` 進入路徑推論待 F01 `s-board` 補操作）／OQ-27（`uc-invite-member` pre 未定義邀請對象帳號是否須已存在）
- 待注意：跑 `./scripts/ui-check <單一檔案>` 務必加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；驗收條件寫「不含 XX」這種排除詞時，改寫替代文字要避開驗收條件本身列出的禁止詞面（本輪曾誤把「須為系統中已存在帳號」寫進 ⚠️ 提示裡，撞到驗收條件自身的排除詞，改成「是否要求帳號已存在」才過）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用；本輪整份檔案 error 數維持 39（純文字改寫，不影響 DS/REF 檢查）。
