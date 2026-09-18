# T-03-be-card open questions

> 由 `loopctl oq add` 產生，不要直接編輯；解除說明由人工補在各則底下。

## OQ-T-03-be-card-01

[Level: uc-add-comment]
- 等級：高
- 阻塞：否
- 接手：人工
- 原因代碼：spec-ambiguous
- 開立：Dev 第 2 輪（2026-09-19）
- 狀態：待處理

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
