# T-03-be-card open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-03-be-card-01

[Level: uc-add-comment]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-ambiguous
- 開立：Dev 第 2 輪（2026-09-19）
- 狀態：**已解除（2026-09-19，人工決策，採選項 A）**

解除說明：人工開 CR-010，`uc-add-comment` 新增 `pre.p2`（`comment.content` 非空）與對應 `fail.p2`，新增 Scenario「留言內容不可為空」（`@CR-010 @uc-add-comment @fail-p2`），`ui-kanban-basic.md` `s-card-detail` 操作表「新增留言」的「失敗時」欄同步更新。`kanban-spring` 的 `CardSteps.java` 補上對應 step definition（`我嘗試在卡片中新增一則空白留言`／`不應該新增留言`），`./gradlew clean build` 確認 133 個測試全綠、新 Scenario 有跑到；`./scripts/spec-check`／`./scripts/ui-check` 0 error。既有的 `Comment.java` 拒絕邏輯（`EMPTY_COMMENT_CONTENT`）不用改，現況已符合新補的 fail 定義。詳見 `.dev/CR.md` CR-010。

情況：【推論＋所本原文】

spec-kanban-basic.md 名詞定義／欄位表對 `comment.content` 的限制欄逐字寫：

『comment.content | string | 非空 | 留言內容』

但 `uc-add-comment` 的 usecase 區塊逐字寫：

```
- id: uc-add-comment
  name: 為卡片新增留言
  roles: [r-user]
  crud: {card: R, comment: C}
  pre:
    p1: "`card` 存在"
  post:
    - "新的 `comment` 建立成功，`comment.content` 為輸入內容，`comment.author` 為留言者，`comment.created-at` 為留言的操作時間"
    - "新的 `comment` 加入 `card` 的留言列表"
  fail: {}
  emits: []
  requires: []
  calls-sync: []
```

`fail: {}` 沒有任何拒絕分支。ui-kanban-basic.md 操作表對「新增留言」的「失敗時」欄逐字寫：

『新增留言 | `uc-add-comment` | 依 `uc-add-comment` post：該留言顯示於留言列表 | 不適用（`uc-add-comment` 無 fail 定義） | 否』

且驗收條件段落逐字寫：

『錯誤：不適用（`uc-edit-card`、`uc-add-comment` 均無 fail 定義）』

推論：欄位表的「非空」限制與 usecase／ui 都沒有對應的拒絕分支，是規格本身的缺口（不是兩處矛盾，是同一份文件內欄位限制與 usecase fail 定義沒有互相對齊）。本任務目前的實作是依欄位表的「非空」限制，在 `Comment.create`／`Card.addComment` 拒絕空白留言並回應 400 `EMPTY_COMMENT_CONTENT`，這個拒絕分支是 Dev 推論出來的，spec／ui 都沒有逐字定義它的訊息或呈現方式。

問題：`uc-add-comment` 要不要正式補上 fail 分支（例如 `fail-p1: "留言內容不可為空"`），並讓 ui 操作表「失敗時」欄跟著補上呈現方式？若不補，目前程式碼擋掉空白留言的行為算不算超出規格範圍？

選項：A. 補 `uc-add-comment` 的 `fail` 分支與對應 Scenario、ui 操作表「失敗時」欄，讓「非空」限制有正式的拒絕定義，走 CR；B. 維持 spec 現狀（`fail: {}`），把「拒絕空白留言」視為 Dev 對欄位限制的合理延伸，不需要 CR，只需在 decision-log／ADR 記錄這個對應關係。

## OQ-T-03-be-card-02

[Level: uc-add-card/uc-move-card-swimlane/uc-move-card-stage]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-ambiguous
- 開立：Dev 第 2 輪（2026-09-19）
- 狀態：待處理

情況：【推論＋所本原文】

spec-kanban-basic.md 名詞定義／欄位表逐字寫：

『card.swimlane | ref swimlane | 建立時必填 | 卡片所屬泳道』
『card.stage | ref stage | 建立時必填 | 卡片所屬階段』

但 `uc-add-card` 的 usecase 區塊逐字寫：

```
- id: uc-add-card
  name: 建立新卡片
  roles: [r-user]
  crud: {board: R, card: C}
  pre:
    p1: "`card.title` 非空"
  post:
    - "新的 `card` 出現在指定的 `swimlane` 與 `stage` 交會格中"
    - "`card.title` 顯示為輸入的標題"
    - "該操作被記錄為 `card` 的一筆活動紀錄，包含操作人與操作時間"
  fail:
    p1: "拒絕，不建立新的 `card`"
  emits: []
  requires: []
  calls-sync: []
```

`pre` 只有 `card.title` 非空一條，`fail` 只有 p1（對應標題留空），沒有任何一條要求驗證 `swimlaneId`／`stageId` 是否存在、是否屬於指定的 `board`。`uc-move-card-swimlane`／`uc-move-card-stage` 的 `fail` 也都是 `{}`：

```
- id: uc-move-card-swimlane
  ...
  fail: {}
- id: uc-move-card-stage
  ...
  fail: {}
```

推論：欄位表把 `card.swimlane`／`card.stage` 標成「ref swimlane」「ref stage」「建立時必填」，隱含這兩個欄位理論上應該參照真實存在、且屬於同一 `board` 的 `swimlane`／`stage`，但 `uc-add-card`／`uc-move-card-swimlane`／`uc-move-card-stage` 都沒有把「目的 swimlane/stage 不存在或不屬於該 board」定義成 fail 情境。本任務目前的實作（`BoardApplicationService`／`CardApplicationService`）沒有加這層驗證，可以用不存在或屬於別的 board 的 UUID 建立／移動卡片，形成孤兒卡片。

問題：`uc-add-card`／`uc-move-card-swimlane`／`uc-move-card-stage` 要不要正式補上「目的 swimlane/stage 不存在或不屬於該 board」的 fail 分支？若要補，錯誤訊息與對應 Scenario 怎麼寫；若不補，是否代表現階段刻意不驗證交會格合法性（例如前端保證一定傳合法的 ID，後端不用重複檢查）。

選項：A. 補三個 usecase 的 fail 分支（新增一個 `fail-p2` 或共用一條，例如「拒絕，指定的 swimlane/stage 不存在或不屬於該看板」），連同 ui 的「失敗時」欄與 Scenario，走 CR；B. 維持 spec 現狀，後端不加這層驗證，只在 decision-log 記錄「目前信任呼叫端傳入合法的交會格 ID」這個假設，等真的出問題（例如 T-07 canvas 或前端串接後）再回頭補。
