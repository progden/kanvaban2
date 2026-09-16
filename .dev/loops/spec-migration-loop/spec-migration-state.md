# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 34）
- 目前階段：階段 2（F05 遷移進行中）
- 上一輪任務：完成 T2.18（[F05]「人員工作量檢視」usecase 區塊＋tag＋Aggregate 註解；4 個查詢 Scenario 歸入新立的 `uc-view-workload`；2 個拖曳追加負責人 Scenario 因 GH-01 無法跨檔引用 F02 的 `uc-assign-card-owner-by-drag`，另立本地 `uc-drag-assign-card-owner`，記 OQ-09）｜結果：done
- 下一個任務：T2.19（[F05] 遷移程序 10～11 與收尾，F05 全檔 0 error）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01（全檔 0 error）；F02（全檔 0 error）；F03（全檔 0 error）；F04（全檔 0 error）；F05 名詞／角色表與 usecase 區塊已遷移，正文清理與變更紀錄收尾留待 T2.19
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user）、F02（r-board-owner、board-membership、uc-assign-card-owner-by-drag）；F05 新增 uc-view-workload、uc-drag-assign-card-owner（本模組本地）
- 最近 3 條假設：F05 拖曳追加負責人兩個 Scenario 另立本地 uc-drag-assign-card-owner，未沿用 F02 同義 uc（GH-01 禁止跨 Feature 引用，REF-08 禁止重複定義 ID），記 OQ-09（高影響）；4 個查詢 Scenario 歸入單一 uc-view-workload（純讀取、同一個「開啟 Workload 表」動作，低影響）；post 句補上反引號 ID 以符合 UC-03（低影響格式修正）
- 待注意：全部 spec error 總數 43（56 → 43）；F01=0、F02=0、F03=0、F04=0、F05=13（剩餘皆在「人員工作量檢視」Feature 之外，正文反引號與變更紀錄留給 T2.19 遷移程序 10～11）、F06=30
