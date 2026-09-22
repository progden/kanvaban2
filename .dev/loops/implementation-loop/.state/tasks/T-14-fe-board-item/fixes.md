# T-14-fe-board-item 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | s-card-detail 的留言列表沒有顯示 `comment.created-at`。 `ui-kanban-basic.md` s-card-detail 資料表：『｜ 留言列表 ｜ `comment.content`／`comment.author`／`comment.created-at` ｜ 顯示 / 輸入（新增留言內容） ｜ `comment.content` 非空 ｜ 依 `uc-add-comment` post，`card` 底下的留言依 `comment.created-at` 排序顯示 ｜』，三個來源欄位都標「顯示」；設計稿 `CardDetail.dc.html` 的留言區塊也同時畫出作者「蔡依帆」與時間「09/15 14:02」，並標註「依 comment.created-at 排序」。 實際實作（`kanban-frontend/src/board/CardDetailDialog.tsx` 的 `card.comments.map`）只渲染 `displayNameOf(comment.authorId)` 與 `comment.content`，沒有用到 `CommentView.createdAt`（該欄位後端 `CardResponse.CommentView` 已回傳、前端 `cardApi.ts` 型別也已宣告）。 怎樣才算修好：s-card-detail 的每一則留言同時顯示作者與 `comment.created-at`（時間格式屬技術決定，沿用設計稿的呈現即可），並補一個測試斷言留言的時間有顯示，以及留言依 `comment.created-at` 由舊到新排序顯示。 |
