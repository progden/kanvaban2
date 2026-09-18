# state

> 每輪覆寫，20 行內；給下一輪／驅動腳本快速回復現況（zero context）。

T-00-scaffold：Review 已核准，狀態 `done`，等驅動腳本合併 `impl/T-00-scaffold` 回 `loop/implementation`。
- 第三次 Review（HEAD `b9c40a4`）重新驗證：`./gradlew clean build`（core／spring 各 1 個測試）和 `kanban-frontend` 的 `pnpm install --frozen-lockfile && pnpm run build && pnpm run test`（1 passed）都通過，程式碼跟第一次核准時一樣。
- 沒有 D-xx；三則不擋核准的觀察見 `review.md`。
- ⚠️ `done` 任務已經被重複觸發 Review 兩次，請人工確認 `run-loop.sh` 有沒有真的執行合併。
- 下一個可執行任務：T-01-be-user（合併後依賴解除）。
