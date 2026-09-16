# spec 遷移審查紀錄

[`spec-migration-review-prompt.md`](./spec-migration-review-prompt.md) 審查輪的產出，**只能在檔尾追加**。每則標題格式：`## Review — <YYYY-MM-DD HH:MM> — <HEAD 短 hash>`，下一次審查以最後一則的 hash 作為起點。

關卡（`G*`）前的審查會附「關卡摘要」，事後請先閱讀。

---

## Review — 2026-09-16 19:30 — f1ed1c7
### 範圍
`3038528`（baseline，首次審查）..`f1ed1c7`，任務 T0.01、T0.02、T1.01、T1.02、T1.03。`last-verify.md` 無失敗、無警告（沒有 F 編號修正，也沒有單輪改多份 spec）。OQ 檔期間內沒有新增列。下一個任務是 T1.04，不是關卡。

### 發現
1. **中**｜F01「Swimlane 管理」「Stage（階段）管理」的 9 個 uc｜每個 Scenario 都寫「該操作應該被記錄為一筆活動紀錄」，執行輪把它併進各 uc 的 `post`（「被記錄為 `board` 的一筆活動紀錄」），但 `crud` 沒有 `board` 的寫入（`uc-add-*` 只有 `board: R`，其餘沒有 `board`），成功 Scenario 的 Aggregate 註解也拿掉了原規格的 `board: read, write`。這樣 post 說有寫入 board，crud 卻沒有，CRUD 矩陣會少掉活動紀錄的寫入。另外，「活動紀錄不另立 uc、不 emits 事件」會影響 F02 的 ActivityRecord 實體和 T2.08 的 `requires` 接法，屬於高影響決定，但只記成低影響假設（PDCA／state），沒有記 OQ。採用的選項：活動紀錄屬於被操作的 Aggregate（F02 備份的實作備註寫明 `Board.activityLog`／`Card.activityLog`），crud 補 `board` U、成功 Scenario 註解補回 `board: read, write`，並補記 OQ → **D-01**。T1.04 的 Card uc 本來就有 `card` 寫入，照同一原則處理即可。
2. **低**｜「刪除空的 Swimlane」「刪除空的 Stage」｜`card` 註解由 `read` 改成 `read, write`。原因是 GH-06 要求同一 uc 的成功 Scenario 一律標 write，與刪除含卡片的情境共用 uc 是合理的（同一交易），可以接受，不開任務。
3. **低**｜`uc-add-stage.pre.p1`「`board` 存在」｜沒有對應的失敗 Scenario，只對應 Background「我已開啟…看板」，可以接受。`uc-delete-swimlane` 的 post 沒寫「確認訊息」這個 UI 步驟，post 只描述狀態，可以接受。
4. **低**｜名詞定義｜「Stage 角色（role）」同時出現在欄位表（`stage.role`）與其他名詞表，概念重複，但這是為了遵守「說明文字不刪」，保留，不開任務。原名詞表 6 列逐列對照都找得到（T1.01 驗收達成，PDCA 寫「5 列」是筆誤，不影響結果）。

其他檢查過、未發現偏差的面向：
- 鐵則 1：gherkin 步驟、tag 與變更紀錄都沒動。只有 H2 標題對齊 gherkin 的 Feature 名稱和「身為」行有改，這兩項都在允許範圍內。
- F 編號：沒有修正。
- 鐵則 2：其餘 pre／post／fail 都能對到 Scenario 步驟，`roles` 只有 `r-user`，依據是三個 Feature 相同的「身為」行。
- 拆分粒度：一個 When 動作對一個 uc，失敗情境併入同一個 uc，合理。
- 保留原文：沒有順手修正。
- CR.md：五列的內容與各 spec 變更紀錄相符。
- 任務完成度：PDCA 的 Check 與 `last-verify` 數字一致（302）。

### 待審任務處理
期間內沒有 `proposed` 的 D-xx。

### 關卡摘要
不適用（下一個任務是 D-01，之後是 T1.04）。
