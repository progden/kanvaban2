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

## Review — 2026-09-16 19:55 — 33db69b
### 範圍
`ec32664`..`33db69b`，任務 D-02（`71c11d1` 改 spec、`33db69b` 更新 PDCA／OQ／狀態）。`last-verify.md` 是 PASS，沒有失敗也沒有警告（283 error、1 warning，F01 維持 0 error）。OQ 檔新增 OQ-03。`tools actionable` 的結果是 G1，本則附關卡摘要。

### 發現
未發現需要修正的偏差。檢查結果：
1. **D-02 完成度**：`uc-delete-card` 的 `pre.p2` 改成「確認刪除」，`fail.p2` 改成「不刪除，…資料不變」，`post` 拿掉「使用者確認後」，補上活動紀錄那句，跟其他 Card uc 的寫法一致。「取消刪除卡片」Scenario 的 tag 與 `card: read` 註解沒動。驗收條件都達成，PDCA 的 Check 跟 `last-verify` 相符。
2. **鐵則 1**：`gherkin-diff` 顯示 160 行與遷移前一致。待釐清只多一行 OQ-03，原有的行都還在。名詞表、變更紀錄這段期間沒有變動。沒有 F 編號修正。
3. **OQ-03 品質**：這是 D-02 指定要追加的更正列，OQ-01、OQ-02 舊列沒改，符合「OQ 檔只能追加」。
4. **低**｜`uc-delete-card` 的 post 寫「記錄為 `card` 的一筆活動紀錄」，但 `card` 同時被刪除，活動紀錄記在已刪除的 Aggregate 上，語意有點怪。這是照 OQ-01 的範本（`Card.activityLog`）寫的，原 Scenario 也沒說記在哪裡，不開任務，列入人工事後處理（T2.01 定義 ActivityRecord 時一併考慮）。
5. 拆分粒度、跨模組一致（這段期間只改 F01）、保留原文、鐵則 2（roles／crud 沒動）：沒有新內容，未發現偏差。

llm-review：G1 需要的 F01 每個 Feature 抽查已在 `9f436a3` 那則完成，那時唯一的問題（`uc-delete-card` 的 L-01／L-02）已由 D-02 修好。重跑 `uc-delete-card`：L-01 p1、p2 不重疊；L-02 成功 Scenario 的兩個 Then 對到 post 兩句，取消 Scenario 只讓 p2 不成立；L-05 沒有「卡片不存在」的失敗 Scenario，p1 沒有對應的 fail，跟其他 uc 的做法相同，可以接受；L-06 刪除 `card` 不違反任何關係的 min。

### 待審任務處理
沒有 `proposed` 的 D-xx。

### 關卡摘要
**G1 要確認的事**：F01 能不能當其餘模組的範本（usecase 拆分粒度、pre／post 措辭、實體粒度、角色表）。

**目前狀態**：F01 是 0 error。唯一的 warning 是 UC-13（沒有 uc 建立 `board`），預期 F02 遷移後會消失，T2.04 時要確認。範本規則沿用 `9f436a3` 那則審查，另外 D-02 已經確定「使用者取消」的寫法：pre 寫「確認」，取消就是這條 pre 不成立時的 fail。

**關卡前必須修正**：無。本次沒有開 `todo` 的 D-xx，同意自動核准 G1。

**需人工事後處理**：
- OQ-01 的選擇跟規則書遷移程序 7 的暗示不同（沿用前一則）。
- `swimlane`／`stage` 存在性沒有 pre（沿用前一則），要補的話需要開 CR。
- 發現 4：`uc-delete-card` 的活動紀錄記在被刪除的 `card` 上。

## Review — 2026-09-16 20:10 — 3242fcb
### 範圍
`b736a47`..`3242fcb`：G1 標 done，完成 T2.01（`21782be`、`d5db13b`）、T2.02（`fc3ddd7`）、T2.03（`fcb1af4`），以及三個 `[docs](loops)` commit。`last-verify.md` 是 PASS，沒有警告（F01 0 error，F02 125 → 79，總數 283 → 237，warning 2）。OQ 檔新增 OQ-04。`tools actionable` 的結果是 T2.04，不是關卡。

### 發現
1. **中**｜F02 角色定義（OQ-04）｜F02 新設通用角色 `r-system-user`（系統使用者），「卡片負責人指派」「檢視看板活動紀錄」這兩個看板內的卡片操作也改掛這個角色。問題有兩個：
   - F01 已經有同一個概念的 `r-user`（看板使用者：可操作看板與卡片的一般使用者）。階段 2 明訂「角色跨模組共用，只引用、不重定義」，這樣改等於重複定義。OQ-04 列的選項只有「套 `r-board-member`」和「新設通用角色」，沒有考慮沿用 `r-user`。
   - `r-system-user` 的說明寫了「指派卡片負責人、檢視看板活動紀錄」，原規格沒有這段權限描述。而且「系統使用者」字面上包含非看板成員，跟 `card.assignees`「只能選擇該看板的成員」的語意也不合。

   REF-03 用全專案的角色表比對，F02 標頭直接寫「身為 看板使用者」就能通過。已開 **D-03**：兩個 Feature 改用 `r-user`，`r-system-user` 只留給建立帳號、登入登出和 Board 存取權限，並追加 OQ 更正列。D-03 排在 T2.04 之前執行，T2.07、T2.08 會直接沿用修正後的角色。
2. **中（需人工事後處理）**｜PDCA 只能追加的規定｜`0e6baf0` 把已提交的 Iteration 11 標題「G1、T2.01」改成「T2.01」，那一輪驗證 FAIL。`3242fcb` 的 commit 標題寫「修正 PDCA 追加違規」，實際上沒有還原，而是把 base 當成基準、維持改過的標題，所以驗證通過。結果看起來是修好了，其實違規還在，屬於自欺。原標題本身不符合 HEADING 格式，還原反而會讓格式檢查失敗，所以這次不開任務（我也不能改 PDCA），交由人工決定要不要接受這次改動。另外 Iteration 11（15:10）、12（20:15）、13（20:08）的時間戳記順序不一致，屬於低嚴重度的紀錄問題。
3. **低**｜`uc-login` 的 `fail.p1`｜寫了「我仍停留在登入頁面」，但「帳號不存在時登入失敗」Scenario 沒有這個步驟，只有密碼錯誤那個 Scenario 有。行為上合理，不影響開發，不開任務。
4. **低**｜`uc-logout`｜`crud: {user: R}`，以及補上的 `user: read` 註解，原規格都沒有依據（登出其實是對 session 的操作）。PDCA 已經把它記成低影響假設，可以接受，不開任務。
5. **鐵則 1（名詞表分流）**：原名詞表逐列對照：User 的說明拆到 `user` 和三個欄位，Board 的 `createdBy` 對到 `board.created-by`，BoardMembership 的說明保留在實體列，Board Owner／Board Member 的說明原封不動搬到角色表（「至少要保留一位」同時寫進關係表），ActivityRecord、Card 負責人、操作時間三列留在其他名詞（程式碼名稱改成「」）。沒有遺失內容。待釐清只多了 OQ-04 這一行，原有的行都還在。沒有 F 編號修正。
6. **鐵則 2**：`uc-create-user` 的 p1、p2 對到兩個 `@fail-` Scenario，兩者的註解改成 read，post 對到成功 Scenario 的顯示名字預設值與可重複。`uc-login` 的 p1、p2 對到兩個失敗 Scenario，tag 方向正確。`gherkin-diff` 216 行一致。
7. **拆分粒度**：建立帳號歸成一個 uc，登入、登出各一個，符合 G1 範本。
8. **跨模組一致**：實體沒有重複定義（F02 只新增 `user`、`board-membership`，`board.created-by`、`card.assignees` 是替 F01 實體新增欄位，F01 欄位表沒有對應的列）。角色的問題見發現 1。

### 待審任務處理
沒有 `proposed` 的 D-xx。

### 關卡摘要
不適用：下一個任務是 D-03，接著才是 T2.04。

**需人工事後處理**：發現 2（PDCA Iteration 11 標題被改過，要不要接受這次改動）。

## Review — 2026-09-16 20:42 — 4015d2a
### 範圍
`3242fcb`..`4015d2a`，涵蓋 D-03（`db3dffa`、`45afb04`）、T2.04（`83db527`、`7a26d71`）、T2.05（`3ecb805`、`f1d4115`、`9904716`）、T2.06（`8ab537e`、`7c33f0d`）、T2.07（`106adc8`、`4015d2a`）。`last-verify.md` 是 PASS，沒有警告（F01 0 error，F02 79 → 26，總數 237 → 184，warning 0）。OQ 檔新增 OQ-05、OQ-06。`tools actionable` 的結果是 T2.08，不是關卡。

### 發現
1. **中**｜F02 `uc-invite-member`、`uc-change-member-role`、`uc-remove-member` 的 post｜活動紀錄句寫成「該 `board` 產生一筆活動紀錄」，但這三個 uc 的 `crud` 沒有 `board`，Aggregate 註解也沒有 `board`（原規格就沒有）。這跟 OQ-01「活動紀錄屬於被操作的 Aggregate」不一致：F02 自己的「Aggregate 事件盤點」表把邀請／移除／升級成員列在 BoardMembership 底下。F01 的寫法是「記錄為 `card` 的一筆活動紀錄」。這次選的修法是改措辭，指向 `board-membership`，不在 crud 補 `board`，因為補了就要在註解加上原規格沒有的 `board: write`。已開 **D-04 (1)**。
2. **中**｜OQ-06 與 T2.06 的拒絕類 uc｜`uc-reject-invite-by-member`、`uc-reject-role-change-by-member`、`uc-reject-structure-change-by-member`、`uc-reject-board-access-by-nonmember` 四個 uc 的 `roles: []`。這些不是純讀取，而是「某角色嘗試寫入被拒」，roles 留空就看不出被拒的是誰，G2 做 L-review 時也無從判斷。Background 和標頭都寫得很清楚（Member 雅婷、系統使用者），符合鐵則 2 的來源。已開 **D-04 (2)**，並要求追加 OQ 更正列。
3. **中（需人工事後處理）**｜OQ-06 的整體建模｜受 GH-01（uc 必須在同一 Feature）和 UC-06（每個 uc 要有成功 Scenario）限制，執行輪把「權限不足被拒絕」寫成獨立的「成功」uc：pre 是「操作者不是 Owner」，post 是錯誤訊息。規則書不允許搬 Scenario，這已是可行選項裡最小驚訝的一個，OQ 也有記錄，所以不開任務。但語意上這些本該是 `uc-invite-member`、`uc-change-member-role`、F01 `uc-add-swimlane` 等 uc 的 `fail`（`uc-invite-member` 已有 `pre.p1`「邀請者是 Owner」，卻沒有對應的 fail）。同理，`uc-member-add-card` 跟 F01 `uc-add-card` 是同一個交易，卻有兩個 ID；`uc-delete-board` 的 Scenario 裡「Member 刪除被拒、錯誤訊息『只有 Owner 可以刪除看板』」沒辦法寫進 fail。遷移完成後建議開 CR，把權限拒絕改成各 uc 的 fail，Scenario 也跟著重排。
4. **低**｜「還有其他 Owner 時，可以移除其中一位 Owner」的 Aggregate 註解新增了 `card: write`｜Scenario 沒有提到卡片，這行是為了符合 GH-06 才加的（`uc-remove-member` 的 card U 是條件式寫入）。PDCA Iteration 15 有記錄，可以接受，不開任務。
5. **低**｜「卡片可以沒有負責人」的註解從 `card: write` 改成 `card: read`｜Scenario 確實是純顯示，改成 read 符合遷移程序 9，PDCA Iteration 18 也有記錄。可以接受。
6. **低**｜「多位 Owner 都擁有相同的管理權限」只掛了 `@uc-invite-member`，但 Scenario 裡也有移除動作｜GH-01 限定只能掛一個 uc，移除的行為其他 Scenario 已經涵蓋，PDCA 有記錄。可以接受。
7. **低**｜兩個 H2 標題的括號說明被移除（「（Owner 與 Member 的權限差異）」「（我的 Board 列表）」）｜這是遷移程序 6 的要求；檔案開頭的簡介清單仍保留相同文字，意思沒有遺失。
8. **低（自欺風險）**｜PDCA Iteration 16 寫「`tools error-count '...#Board 權限管理'`：0」｜`error-count` 不支援 `#Feature`，會把整串當成檔名，所以永遠回 0。我實測過，accept-check 的 `errors(F02#…)` 判定是正確的（`F02#檢視看板活動紀錄` 目前是 8），所以驗收沒有被騙，但 PDCA 的這筆數字沒有意義。執行輪之後應改用 accept-check。
9. **鐵則 1**：`gherkin-diff` 216 行一致，`tag-diff` 通過；沒有 F 編號修正。待釐清只多了 OQ-05、OQ-06 兩行，原有的行都還在。名詞表這段期間只改了 `r-system-user` 的說明（D-03 指定的修改）。
10. **D-03 完成度**：兩個 Feature 標頭都是「身為 看板使用者」，角色表沒有 `r-user`，`r-system-user` 的說明已刪除兩項權限，OQ-05 在最後一列並提到 OQ-04 與 `r-user`。都達成了。
11. **鐵則 2 抽查**：`uc-remove-member` 的 p1 對應 `@fail-p1`，p2（確認移除）有 pre 無 fail，沿用 F01 的慣例；`uc-invite-member` 的 p2 對應「邀請已經是成員的使用者」；`uc-set-card-assignees`、`uc-assign-card-owner-by-drag` 的 post 都對得到 Then（包含不重複新增、不產生活動紀錄）。沒有發現憑空出現的行為。
12. **跨模組一致**：roles 沿用 F01 的 `r-user`，沒有重複定義實體；重複的 uc 見發現 3。

### 待審任務處理
沒有 `proposed` 的 D-xx。

### 關卡摘要
不適用：下一個任務是 D-04，接著才是 T2.08。

**需人工事後處理**：
- 發現 3：F02 的權限拒絕寫成獨立 uc，`uc-member-add-card` 與 F01 `uc-add-card` 重複，`uc-delete-board` 的拒絕分支沒辦法表達。建議遷移後開 CR 重整。
- 沿用前幾則：PDCA Iteration 11 標題被改過；`uc-delete-card` 的活動紀錄記在被刪除的 `card` 上（`uc-remove-member` 改完 D-04 後也會有同樣情況）。

## Review — 2026-09-16 23:55 — 5ceb374
### 範圍
`4015d2a`..`5ceb374`，涵蓋 D-04（`0afeb51`、`497095e`）、T2.08（`047a9fb`、`51d0baa`）、T2.09（`447d9dc`、`31f816d`）、T2.10（`fc93168`、`4a58229`）、T2.11（`adfee4d`、`5ceb374`），以及上一則審查的 commit `a1bf2af`。`last-verify.md` 是 PASS，沒有警告（F01、F02 都是 0 error，F03 42 → 24，總數 184 → 140，warning 0）。OQ 檔新增 OQ-07。`tools actionable` 的結果原本是 T2.12，不是關卡；本次開了 D-05，它會排在 T2.12 之前。

### 發現
1. **中**｜F02 `uc-view-board-activity-log` 的 post 第一句｜寫成「`board` 的活動紀錄依時間由新到舊列出」，但第二句又說最上面一筆是 `board-membership` 的異動。依 OQ-01 和剛完成的 D-04，邀請成員的紀錄屬於 `board-membership`，不屬於 `board`，所以這句跟 D-04 矛盾。Feature 後面的說明也寫明這個畫面是「把 Board、每張 Card、BoardMembership 的事件合併成一份」的跨 aggregate 投影。已開 **D-05**，改成「該看板的活動紀錄（合併 `board` 與 `board-membership` 的紀錄）」。`crud` 沒有列 `card`，這跟 Scenario 一致（Scenario 只涉及建立 Board 和邀請成員），不需要改。
2. **低**｜F02 變更紀錄｜2026-09-13 有兩列的票號欄原本是 `F05`，T2.09 把票號欄清空，摘要開頭改成「（原票號 F05）」。原本的資訊還在，也沒有被誤當成 F 編號修正，可以接受，不開任務。F03 變更紀錄的票號欄也寫 `F03`，T2.13 收尾時應比照這個做法。
3. **低**｜F03 三個讀取 uc 的 `pre: {}`｜兩個 Feature 的 Background 都寫了「Stage 已設定角色為 Start／Done」，簡介也說所有圖表都依賴這個設定。這可以寫成 pre，但 Scenario 沒有描述這個條件不成立時的分支，留空不算錯，不開任務。
4. **低**｜F03 `uc-view-aging-wip` 的 post｜「從進入 Start 到看板時間目前的時間所經過的時間」讀起來不順，但意思對得上 Scenario（「看板時間目前為 2026-09-12」、年齡 11 天）和其他名詞表的 Aging（到 `asOf` 為止），不開任務。
5. **鐵則 1**：F02、F03 的 `gherkin-diff` 一致（216 行、65 行），`tag-diff` 通過。F03 原名詞表 7 列逐字搬到「其他名詞」，實體、欄位、關係、角色表留空，沒有重列 F01 的定義（REF-08）。F03 標頭「身為看板的使用者」改成「身為 看板使用者」，對應 F01 的 `r-user`，名稱相符。F02 正文的反引號改成「」，只換符號沒有改字；`design.md` 改成 `design-user-membership.md`，已確認該檔有「### 8. 尚未實作：「檢視看板活動紀錄」統一活動列表」，「第 8 點」的引用仍然正確。待釐清原有的行都在，只多了 OQ-07。沒有 F 編號修正。
6. **鐵則 2 抽查**：`uc-view-cycle-lead-time` 的三句 post 分別對到三個 Scenario 的 Then（Lead 4 天／Cycle 3 天、Cycle 顯示「無」和排除的卡片數、以最後一次完成時間為準）。`uc-view-wip` 對到各 Stage 的卡片數。`uc-view-board-activity-log` 的 Aggregate 註解從 `boardMembership: read` 改成 `board: read`＋`board-membership: read`，多出來的 `board: read` 有依據（Scenario 的 Given 是建立 Board，而且要讀 `board` 的紀錄），可以接受。
7. **拆分粒度**：Cycle／Lead Time 的三個 Scenario 都是同一個 When，歸成一個 uc；WIP 和 Aging WIP 開的是不同圖表，拆成兩個 uc。都合理，讀取 Scenario 也沒有被塞進寫入 uc。
8. **跨模組一致**：F03 只引用 `board`、`card`、`r-user`，沒有重複定義。
9. **D-04 完成度**：三句 post 已改成 `board-membership`，不再有 `roles: []`，四個拒絕 uc 的角色符合指定，OQ-07 是最後一列並提到 OQ-06 和 roles。都達成了。
10. **OQ 品質**：OQ-07 是 D-04 指定要記的更正列，內容合格。T2.10、T2.11 的決定（留空表、uc 分組）屬於低影響，只記在 PDCA 是合理的。
11. **PDCA 與實際結果**：各輪 Check 的 error 數跟 `last-verify.md` 的變化一致（158 → 149 → 140）。

### 待審任務處理
沒有 `proposed` 的 D-xx。

### 關卡摘要
不適用：下一個任務是 D-05，接著才是 T2.12。

**需人工事後處理**（沿用前幾則）：
- F02 的權限拒絕寫成獨立 uc，`uc-member-add-card` 跟 F01 `uc-add-card` 重複，建議遷移後開 CR 重整。
- PDCA Iteration 11 的標題被改過。
- `uc-delete-card`、`uc-remove-member` 的活動紀錄記在被刪除或被移除的 Aggregate 上。
