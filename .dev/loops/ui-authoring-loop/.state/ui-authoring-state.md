# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）進行中
- 上一輪任務：T2.02（[F02] 定案 `s-signup`，類型：表單，對應 `uc-create-user`）——`ui-check` 對 `s-signup` 0 error，八段齊全；「從哪裡進來」「完成後去哪裡」標 ⚠️ 待確認（OQ-11／OQ-12），畫面狀態「討論中」
- 下一個任務：T2.03（[F02] 定案 `s-login`，類型：表單，對應 `uc-login`；`uc-logout` 併入本畫面操作表）
- 進行中任務的剩餘工作：無（T2.02 已 done，T2.03 尚未開工）
- 已完成的模組：F01（8 個畫面全部定案，`ui-check` 0 error）；F02 已定案 `s-signup`，其餘 8 個畫面骨架（皆「未討論」），F03～F07 尚未開始
- 已定義的共用 ID：無新增（本輪只引用既有 ID，未新增畫面外的 ID）
- 最近 3 條 OQ：OQ-10（blocked，環境限制，`ui-check` 單檔跨模組引用需帶 `--spec`）；OQ-11（`s-signup` 從哪裡進來，暫定模組入口）；OQ-12（`s-signup` 完成後去哪裡，暫定導向 `s-login`）
- 待注意：跑 `./scripts/ui-check <單一檔案>` 務必加 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"`；`user.password` 可留白，`user.display-name` 未填時預設等於 `user.username`（依 `uc-create-user` post）；`s-signup` 操作表「確認建立帳號」需確認標「是（本畫面即確認）」——因 spec 無刪除 `user` 的 Use Case，建立後無法復原。
