# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-11-fe-auth：Review 第 2 輪**附保留核准**，狀態改成 `done`，可以合併回 `loop/implementation`（見 `review.md`、`decision-log.md`）。
- Review 自己重跑：`pnpm test`（4 個檔案、16 個測試全過）、`pnpm build`、`pnpm lint`、`./gradlew build -q --no-daemon`，全部 exit 0。
- D-05～D-07 都已處理；沒有新增 D-xx。
- 保留事項：OQ-IMPL-13（帳號重複時是否「不觸發 `uc-create-user`」，定案為 A 時要走 CR，並改 `SignupPage.tsx`／測試）、OQ-IMPL-12（版面待對照設計稿），兩則都待人工處理。
- 下一步：驅動腳本合併 `impl/T-11-fe-auth`。

T-02-be-board：Review 第 2 輪**附保留核准**，狀態改為 `done`，可合併回 `loop/implementation`（見 `review.md`、`decision-log.md` 同日「Review，第 2 輪」條目）。
- 建置：Review 自己重跑 `./gradlew clean build --no-daemon`，exit 0；core 28、spring 27 個測試全過。
- D-05～D-08 都已核對完成；OQ-IMPL-17 由 Review 補列四個替身 step definition。
- 保留事項：R1 OQ-IMPL-17（刪除 Swimlane／Stage 時卡片的連帶刪除／轉移，沒有任務接手，**需要 Planning 或人工在 T-03 開工前決定**）；R2 OQ-IMPL-15（Owner 權限，等 T-04）；R3 OQ-IMPL-16（建立看板時一併建立 Owner membership，等 T-04）；R4 OQ-IMPL-14（預設 Swimlane／Stage 名稱）。
- 下一步：驅動腳本合併 `impl/T-02-be-board`；T-03-be-card、T-04-be-board-membership 的依賴滿足。
