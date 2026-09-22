// 對應 kanban-spring CardController（spec-kanban-basic.md「Card（卡片）編輯」＋
// spec-user-membership.md「卡片負責人指派」）的請求／回應形狀，欄位名稱與後端 record 保持一致。
// listCardsForBoard 對應 T-14 新增的 GET /api/boards/{boardId}/cards（見 decision-log：
// s-board 需要看板全部卡片才能畫出 Swimlane × Stage 交會格，spec 未定義獨立 uc，
// 比照 T-12 card-count 端點的先例，重用既有的 CardRepository.findActiveByBoardId）。
import { apiClient } from './http';

export interface CommentView {
  id: string;
  authorId: string;
  content: string;
  createdAt: string;
}

export interface CardResponse {
  id: string;
  boardId: string;
  title: string;
  description: string | null;
  dueDate: string | null;
  labels: string[];
  swimlaneId: string;
  stageId: string;
  assigneeIds: string[];
  comments: CommentView[];
}

export interface AssigneeCandidate {
  id: string;
  username: string;
  displayName: string;
}

export function listCardsForBoard(boardId: string): Promise<CardResponse[]> {
  return apiClient.get<CardResponse[]>(`/api/boards/${boardId}/cards`);
}

export function addCard(
  boardId: string,
  title: string,
  swimlaneId: string,
  stageId: string,
): Promise<CardResponse> {
  return apiClient.post<CardResponse>(`/api/boards/${boardId}/cards`, { title, swimlaneId, stageId });
}

export function getCard(cardId: string): Promise<CardResponse> {
  return apiClient.get<CardResponse>(`/api/cards/${cardId}`);
}

export function editCard(
  cardId: string,
  description: string | null,
  dueDate: string | null,
  labels: string[],
): Promise<CardResponse> {
  return apiClient.patch<CardResponse>(`/api/cards/${cardId}`, { description, dueDate, labels });
}

export function moveCardSwimlane(cardId: string, swimlaneId: string): Promise<CardResponse> {
  return apiClient.post<CardResponse>(`/api/cards/${cardId}/move-swimlane`, { swimlaneId });
}

export function moveCardStage(cardId: string, stageId: string): Promise<CardResponse> {
  return apiClient.post<CardResponse>(`/api/cards/${cardId}/move-stage`, { stageId });
}

export function addComment(cardId: string, content: string): Promise<CommentView> {
  return apiClient.post<CommentView>(`/api/cards/${cardId}/comments`, { content });
}

export function deleteCard(cardId: string): Promise<void> {
  return apiClient.del<void>(`/api/cards/${cardId}`);
}

export function setAssignees(cardId: string, assigneeIds: string[]): Promise<CardResponse> {
  return apiClient.patch<CardResponse>(`/api/cards/${cardId}/assignees`, { assigneeIds });
}

export function listAssigneeCandidates(boardId: string): Promise<AssigneeCandidate[]> {
  return apiClient.get<AssigneeCandidate[]>(`/api/boards/${boardId}/assignee-candidates`);
}
