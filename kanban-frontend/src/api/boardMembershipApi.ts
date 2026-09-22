// 對應 kanban-spring BoardMembershipController（T-04）的 GET /api/boards/{boardId}/members；
// s-canvas 用它判斷操作者對應 r-canvas-editor／r-canvas-viewer（見 spec-canvas-layout.md 角色定義：
// board-membership.role 為 Owner／Member 對應 r-canvas-editor，Viewer 對應 r-canvas-viewer）。
import { apiClient } from './http';

export type BoardRole = 'OWNER' | 'MEMBER' | 'VIEWER';

export interface MemberView {
  username: string;
  displayName: string;
  role: BoardRole;
}

export function listMembers(boardId: string): Promise<MemberView[]> {
  return apiClient.get<MemberView[]>(`/api/boards/${boardId}/members`);
}
