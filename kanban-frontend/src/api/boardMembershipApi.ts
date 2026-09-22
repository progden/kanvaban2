// 對應 kanban-spring BoardMembershipController（T-04）的 /api/boards/{boardId}/members 系列端點；
// s-canvas 用 listMembers 判斷操作者對應 r-canvas-editor／r-canvas-viewer（見 spec-canvas-layout.md
// 角色定義：board-membership.role 為 Owner／Member 對應 r-canvas-editor，Viewer 對應 r-canvas-viewer）。
// inviteMember／changeMemberRole／removeMember 對應 s-member-management（T-15）的三個操作。
import { apiClient } from './http';

export type BoardRole = 'OWNER' | 'MEMBER' | 'VIEWER';

export interface MemberView {
  userId: string;
  username: string;
  displayName: string;
  role: BoardRole;
}

export function listMembers(boardId: string): Promise<MemberView[]> {
  return apiClient.get<MemberView[]>(`/api/boards/${boardId}/members`);
}

export function inviteMember(boardId: string, username: string, role: BoardRole): Promise<void> {
  return apiClient.post<void>(`/api/boards/${boardId}/members`, { username, role });
}

export function changeMemberRole(boardId: string, username: string, role: BoardRole): Promise<void> {
  return apiClient.patch<void>(`/api/boards/${boardId}/members/${encodeURIComponent(username)}/role`, { role });
}

// confirmed＝false（預設）：若目標成員仍是卡片負責人，後端回 409，訊息含卡片張數
// （uc-remove-member pre p2）；呼叫端依訊息內容顯示確認、使用者確認後再以 confirmed＝true 重打。
export function removeMember(boardId: string, username: string, confirmed: boolean): Promise<void> {
  const query = confirmed ? '?confirmed=true' : '';
  return apiClient.del<void>(`/api/boards/${boardId}/members/${encodeURIComponent(username)}${query}`);
}
