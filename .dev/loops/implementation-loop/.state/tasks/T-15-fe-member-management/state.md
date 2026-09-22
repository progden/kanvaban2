# T-15-fe-member-management state

> 2026-09-22 Dev 第 1 輪 收尾，status＝`review-pending`。每輪由 `loopctl finish` 覆寫。

現況：`s-member-management`（邀請／變更角色／移除成員）與 F07 s-canvas 上的「看板成員」item（`item.component='board-members'`）已實作完成，`pnpm test`／`lint`／`build` 全綠。

這輪做了什麼：新增 `kanban-frontend/src/canvas/members/`（`MemberManagementDialog.tsx`＋test＋css、`BoardMembersItem.tsx`＋test＋css），擴充 `boardMembershipApi.ts`（inviteMember／changeMemberRole／removeMember），`BoardCanvasPage.tsx` 加一行 side-effect import 註冊畫布元件。

Review 要先看什麼：
1. OQ-T-15-fe-member-management-01（`item.component` 值 `"board-members"` 是否合理）、OQ-02（「移除成員」確認時機與 `r-board-member` 隱藏移除按鈕的推論）兩則 OQ 的推論是否站得住腳。
2. `MemberManagementDialog.tsx` 的 confirmed=false→409→確認→confirmed=true 兩段式移除流程是否正確對應後端 `BoardMembershipApplicationService.removeMember`。
3. `ItemContentProps` 沒有帶 `boardId`，改用 `useParams` 取得的做法是否可接受（T-14 可能也需要同樣處理）。
