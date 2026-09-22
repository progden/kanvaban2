// 對應 kanban-spring CardController 的依負責人查詢卡片（spec-user-membership.md
// uc-list-cards-by-assignee，s-cards-by-assignee 使用）與拖曳頭像追加負責人
// （uc-assign-card-owner-by-drag／spec-workload.md uc-drag-assign-card-owner 共用同一端點，
// 見 WorkloadController 檔頭註解）。這裡只取這兩個任務目前需要的欄位，CardResponse 完整形狀
// 留給之後的卡片任務（T-14）自行擴充。
import { apiClient } from './http';

export interface CardSummary {
  id: string;
  title: string;
}

export function listCardsByAssignee(boardId: string, userId: string): Promise<CardSummary[]> {
  return apiClient.get<CardSummary[]>(`/api/boards/${boardId}/cards/by-assignee/${userId}`);
}

export function dragAssignCardOwner(cardId: string, userId: string): Promise<CardSummary> {
  return apiClient.post<CardSummary>(`/api/cards/${cardId}/assignees/drag`, { userId });
}
