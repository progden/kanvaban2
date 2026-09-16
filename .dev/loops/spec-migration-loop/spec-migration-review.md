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

## Review — 2026-09-16 19:47 — 25c2bb7
### 範圍
`f1ed1c7`..`25c2bb7`，任務 D-01、T1.04、T1.05、T1.06。`last-verify.md` 無失敗、無警告（沒有 F 編號修正，也沒有單輪改多份 spec）。OQ 檔期間內新增 OQ-01、OQ-02。下一個任務是 G1，本則附關卡摘要。

### 發現
1. **高**｜F01 `uc-delete-card`（Card（卡片）編輯）｜`pre.p2` 寫成「使用者於確認訊息中選擇取消」，方向反了。pre 是成功要滿足的條件，照這樣寫，成功 Scenario「刪除卡片需要確認」（使用者按確認）反而不滿足 pre，p2 不成立時的 fail 也變成「沒按取消就拒絕」，意思顛倒。執行輪為了遷就，把 post 寫成「使用者確認後，…移除」，把條件塞進 post。另外，這個 Scenario 最後一步「該操作應該被記錄為一筆活動紀錄」在 post 裡漏掉了，其他 5 個 Card uc 都有寫。OQ-02 的歸類決定（取消當成 fail-p2，註解維持 read）本身合理，要修的只是措辭；OQ 檔只能追加，所以另外追加一列更正 → **D-02**。
2. **低**｜「卡片標題不可為空」的 Aggregate 註解｜`card: write` 改成 `card: read`。遷移程序 9 規定失敗 Scenario 只標 read，屬於允許的對齊，不算順手修正，接受。
3. **低**｜變更紀錄 CR-004「開發完成」那列｜摘要裡的反引號改成「」。這是遷移程序 10 要求的清理，`changelog-check` 也通過，文字意思沒變，接受。
4. **低**｜OQ-01 與規則書遷移程序 7 的關係｜程序 7 寫「emits／requires 只在 Scenario 明說『該操作應該被記錄為活動紀錄』這類跨 uc 效果時才填」，暗示活動紀錄可以用 emits 表達，OQ-01 採用了另一個選項 (b)。這是上次審查 D-01 指定的方向，依據是 F02 備份實作備註裡的 `Board.activityLog`／`Card.activityLog`，維持不變，列入「需人工事後處理」。
5. **低**｜llm-review L-05｜`uc-add-card` 沒有「指定的 `swimlane`／`stage` 存在」這條 pre（欄位表寫「建立時必填」），`uc-move-card-*` 也沒寫「目的 `swimlane`／`stage` 存在」。原規格沒有對應的失敗 Scenario，依鐵則 2 不能補，不開任務，列入人工事後處理。
6. **低**｜`uc-delete-swimlane` 的「刪除包含卡片的 Swimlane 需要確認」也有確認步驟，但沒有對應的 pre，跟 D-02 修正後的 `uc-delete-card` 寫法不同。差別在於 Swimlane 沒有「取消」的 Scenario，所以沒有 fail 需要對應，可以接受。範本規則：只有存在取消 Scenario 時，才把「確認」寫成 pre。

llm-review 抽查（F01 每個 Feature 至少一個 uc）：
- `uc-delete-swimlane`：L-01 只有一句 pre，無重疊；L-02 兩個成功 Scenario 的 Then 各自對到 post 三句，fail Scenario 只讓 p1 不成立；L-05 目前的失敗 Scenario 只有一個，沒有遺漏；L-06 p1 保住 `board`→`swimlane` min=1，刪卡片符合 `card.swimlane` 恰好 1 的語意。
- `uc-set-stage-role`：L-01、L-05 無問題（沒有 fail）；L-02 兩段 When/Then 對到 post 三句；L-06 post 第二句保住「START、DONE 各至多一個」。
- `uc-delete-card`：L-01 修正前 p2 與成功 Scenario 矛盾（發現 1）；L-02 post 少了活動紀錄（發現 1）；L-05、L-06 無問題。
- `uc-add-card`：L-05 見發現 5，其餘無問題。

其他檢查過、未發現偏差的面向：
- 鐵則 1：gherkin 步驟、狀態 tag、`@CR-` 都沒動，待釐清原有的 5 行保留，名詞表在這段期間沒有變動。
- F 編號：沒有修正。
- 鐵則 2：Card 的 6 個 uc，roles 都是 `r-user`（依據「身為」行）；`crud` 都能對到 Aggregate 註解。`uc-move-card-stage` 的 post 保留原文「狀態異動」的措辭，沒有改寫。
- 拆分粒度：一個 When 動作對一個 uc，跨 Swimlane 與跨 Stage 移動分成兩個 uc，合理（Then 的紀錄內容不同）。
- 保留原文：留言沒有另立實體，記成低影響假設，合理（原名詞表沒有這個詞）。
- 任務完成度：D-01 的 9 個 crud 都含 `board: U`，9 個成功註解都補回 `board: read, write`，OQ-01 已登記。T1.05 全文確實沒有 `design.md`。PDCA 裡的數字與 `last-verify`（283）一致。

### 待審任務處理
期間內沒有 `proposed` 的 D-xx。

### 關卡摘要
**G1 要確認的事**：F01 能不能當其餘模組的範本，看四點：usecase 拆分粒度、pre／post 措辭、實體粒度、角色表。

**目前狀態**：F01 是 0 error、1 warning（UC-13：沒有 uc 建立 `board`，F02「Board 建立與成員邀請」遷移後應該會消失，T2.04 時要確認）。範本規則：
- 拆分粒度：一個 When 動作一個 uc，成功與失敗 Scenario 放同一個 uc。
- 活動紀錄：寫成 post 一句，並對所屬 Aggregate（`board` 或 `card`）標 U／write，不用 emits（OQ-01）。
- 使用者取消：當成「確認」這條 pre 不成立的 fail（OQ-02＋D-02）。
- 失敗 Scenario 只標 read。
- 實體：4 個，`swimlane`／`stage` 屬於 `board`，留言不另立實體。
- 角色：只有 `r-user` 一個。F02 要把 Owner／Member 權限定成角色時，要決定 F01 的 uc 是否一併改 roles。依規則書，roles 只能來自「身為」行或 F02 已寫明的權限；若要改 F01，必須開 D-xx。

**關卡前必須修正**：**D-02**（`uc-delete-card` 的 pre 方向與 post 缺漏）。這一項是範本的一部分，不修的話 F02～F06 會照抄錯誤寫法，所以不同意自動核准，D-02 完成後才放行。

**需人工事後處理**：
- OQ-01 的選擇與規則書遷移程序 7 的暗示不同（發現 4），T2.01／T2.08 要沿用 OQ-01。若人工改採 emits，F01 的 9＋5 個 uc 都要回頭改。
- 發現 5 提到 `swimlane`／`stage` 存在性沒有 pre，若要補，需開 CR 並新增 Scenario。

## Review — 2026-09-16 19:50 — 9f436a3
### 範圍
`9f436a3`..`9f436a3`：上次審查之後沒有新 commit，沒有任務完成。`last-verify.md` 是 PASS，沒有失敗也沒有警告（283 error、1 warning，跟上次相同）。OQ 檔在這段期間沒有新增列（最後一列仍是 OQ-02）。`tools actionable` 的結果是 D-02、G1，所以下一個任務是 D-02，不是關卡。

### 發現
未發現新偏差。重新確認上次開的 D-02 仍然需要做：F01 `uc-delete-card` 的 `pre.p2` 還是「選擇取消」，`post` 也還沒補活動紀錄，D-02 的內容與驗收條件不用改。

沒有新改動，所以以下面向都沒有新的內容可查：鐵則 1、F 編號修正、鐵則 2、拆分粒度、跨模組一致、保留原文、OQ 品質、任務完成度。

### 待審任務處理
沒有 `proposed` 的 D-xx。

### 關卡摘要
不適用：下一個任務是 D-02，G1 排在它後面。G1 的關卡摘要沿用上一則審查。D-02 完成後，下一次審查要確認修正結果，才能放行 G1。
