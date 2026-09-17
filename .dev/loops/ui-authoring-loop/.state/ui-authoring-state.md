# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中
- 上一輪任務：D-10（依人工決策解除 OQ-11／OQ-12／OQ-13）——內容全部完成（`s-signup`／`s-login`「待確認事項」清空為「（無）」，`s-login` 補上「前往建立帳號」操作），但任務描述裡的機械驗收條件 `ui-check(ui-user-membership.md)=0` 是整份檔案的 error 數，目前仍卡在 78（全部來自 T2.05～T2.10 尚未定案的骨架畫面，與本輪改動無關），無法在依賴任務完成前單獨歸零，故狀態標 `doing` 而非 `done`
- 下一個任務：T2.05（[F02] 定案 `s-board-create-dialog`，對應 `uc-create-board`）
- 進行中任務的剩餘工作：D-10 的實質內容已完成，等到 T2.11 收尾任務把 T2.05～T2.10 都定案、整份檔案歸零 error 後，可把 D-10 直接改 `done`（不需再動內容）
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`（皆已確認，無 ⚠️）／`s-board-list`（因 OQ-16 標「討論中」），其餘 6 個畫面骨架未討論
- 已定義的共用 ID：無新增
- 最近 3 條 OQ：OQ-14（`s-board-list`「Board 名稱」缺 Attribute ID）；OQ-15（`uc-reject-board-access-by-nonmember` 顯示訊息後停留畫面未定，暫定停留 `s-board-list`）；OQ-16（跨模組 Screen 引用單檔案檢查誤判 REF-07，已由人工修 `ui-authoring-tools.py` 解除）
- 待注意：跑 `./scripts/ui-check <單一檔案>` 務必加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；`s-signup` 因不再是模組入口、也未被「進入與離開」段導向（新導覽放在 `s-login` 操作表），新增一筆 `DS-07` warn，屬預期、不擋 `done`；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用；驗收條件斷言主詞只能是畫面元素或「是否觸發 `uc-xxx`」。
