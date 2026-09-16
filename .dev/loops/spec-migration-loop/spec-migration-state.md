# spec 遷移狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-16（Iteration 38）
- 目前階段：階段 2（F06 usecase 區塊完成，準備進入正文清理與變更紀錄收尾）
- 上一輪任務：完成 T2.21（[F06]「Feature／CR 追蹤表」usecase 區塊＋tag＋Aggregate 註解）｜結果：done
- 下一個任務：T2.22（[F06] 遷移程序 10～11 與收尾：正文反引號清理、變更紀錄改四欄，F06 全檔 0 error）
- 進行中任務的剩餘工作：無
- 已遷移完成的模組：F01～F05（全檔 0 error）；F06 進行中（剩「Aggregate 標記說明」段落與變更紀錄兩處的反引號／CR 登記待 T2.22 處理，`errors(F06#Feature／CR 追蹤表)=0`）
- 已定義的共用 ID：沿用 F01（board、swimlane、stage、card；r-user）、F02（r-board-owner、board-membership、uc-assign-card-owner-by-drag）；F06 本輪未新增實體／角色，沿用 F01 的 r-user；新增 uc-view-feature-cr-board（本 Feature 唯一 usecase，五個 Scenario 皆屬同一讀取操作）
- 最近 3 條假設：F06「Feature／CR 追蹤表」usecase 的 post 只列 Scenario 實際展示的行為（Done／Start 兩種角色狀態），未列「其他名詞」表中 Scenario 未展示的 NONE／未開發對應，避免無 Scenario 依據的推導（低影響，比照鐵則 2）；fail 留空，因五個 Scenario 都是同一讀取操作的不同輸出（警告、orphan、大小寫），不是驗證失敗情境，不掛 @fail-（低影響，比照 F03／F05 純讀取 usecase 前例）；post 每句都補上反引號 ID（`card`）以符合 UC-03（低影響，純格式要求）
- 待注意：全部 spec error 總數 15→8（F06 由 15 降到 8，剩「Aggregate 標記說明」段落 `read`／`write` 誤判為 Entity 反引號、變更紀錄段落反引號與 REF-06 CR 未登記，待 T2.22 一併清理）；F01=0、F02=0、F03=0、F04=0、F05=0、F06=8；接下來做 T2.22
