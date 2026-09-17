# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中
- 上一輪任務：T2.03（[F02] 定案 `s-login`，類型：表單，對應 `uc-login`；`uc-logout` 併入操作表）——`ui-check` 對 `s-login`／`s-signup` 0 error，八段齊全；「未登入進入點」「完成後去哪裡」標 ⚠️ 待確認（OQ-11／OQ-13），畫面狀態「討論中」
- 下一個任務：T2.04（[F02] 定案 `s-board-list`，類型：列表，對應 `uc-view-board-list`；無權限狀態對應 `uc-reject-board-access-by-nonmember`）
- 進行中任務的剩餘工作：無（T2.03 已 done，T2.04 尚未開工）
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`／`s-login`，其餘 7 個畫面骨架（皆「未討論」），F03～F07 尚未開始
- 已定義的共用 ID：無新增（本輪只引用既有 ID，未新增畫面外的 ID）
- 最近 3 條 OQ：OQ-11（`s-signup`／`s-login` 未登入進入點，暫定模組入口為 `s-signup`）；OQ-12（`s-signup` 完成後去哪裡，暫定導向 `s-login`）；OQ-13（`s-login` 完成後去哪裡，暫定導向 `s-board-list`）
- 待注意：跑 `./scripts/ui-check <單一檔案>` 務必加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；DS-05 要求「角色與權限」表「做得到」欄需用頓號分隔且逐字等於「操作」表的操作名稱字串（不可用分號或括號附註混在同一欄）；`uc-logout` fail 為空，操作表「失敗時」欄填「不適用」。
