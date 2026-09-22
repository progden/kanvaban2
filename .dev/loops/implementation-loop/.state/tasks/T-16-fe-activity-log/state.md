# T-16-fe-activity-log state

> 2026-09-22 Review 第 1 輪 收尾，status＝`doing`。每輪由 `loopctl finish` 覆寫。

Review 第 1 輪判定：退回（`doing`），留一條 D-01 待下一輪 Dev 處理。

自己實跑的驗證：`pnpm exec vitest run` 47/47 全過、`pnpm run build` 成功、`pnpm run lint` 只有 T-13 既有的 fast-refresh 警告；`git diff loop/implementation...HEAD --stat` 10 檔全在 T-16 範圍內，`.state/` 只動自己的任務目錄，邊界乾淨。spec 面核對 `uc-view-board-activity-log`（`fail` 為空，無 `@fail-pN` 可抽）：排序（後端 `occurredAt` 反序、前端原樣顯示）、操作人與動作呈現、null 備援、API 欄位契約、權限（`ensureMember`）皆與 post 相符，4 個前端測試涵蓋排序／空清單／備援／載入失敗。未觸及 Java 檔，`kanban-core` 純度不受影響。

退回原因（D-01）：`ActivityLogItem` 載入失敗的 `.activity-log__error` 未沿用專案唯一的錯誤樣式 `.form-error`（`src/index.css:148`），只設了 padding／font-size、無配色，偏離 `.dev/ui-prototype/README.md` 第 36 行對無設計稿畫面「沿用既有視覺語彙、不另外發明風格」的要求；`s-activity-log` 正是該句點名的畫面。

OQ：核對 Dev 的 OQ-T-16-fe-activity-log-01，引文逐字無誤，等級「高」／阻塞「否」／接手「無」判定正確，維持有效；其「情況」欄一句推論把 OQ-29 的兩個前提說成都已成立（實際上 spec 原文仍寫『本情境目前尚未實作』），另開 OQ-T-16-fe-activity-log-02（高、不阻塞、接手：人工）補正，不取代原則。兩則皆不阻塞。

下一輪 Dev 要做：修 D-01（錯誤段落改用 `form-error`，`.activity-log__error` 收斂成只管版面），重跑 vitest／build／lint 確認全綠，不需動其他檔案。
