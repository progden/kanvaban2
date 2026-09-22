// 對應 kanban-spring BoardController（/api/boards 系列）的請求／回應形狀，
// 欄位名稱與後端 record 保持一致；swimlanes／stages 只取 s-board-delete-dialog 需要的計數用途。
// cardCount 對應 T-12 新增的 GET /api/boards/{boardId}/card-count（見 decision-log：
// 既有 /api/boards、/api/boards/{boardId} 都不包含卡片數，重用 deleteBoard 已在用的
// CardRepository.findActiveByBoardId 新增一個最小、獨立的端點，不更動既有 BoardResponse 形狀）。

import { apiClient } from './http';

export interface SwimlaneView {
  id: string;
  name: string;
  order: number;
}

export interface StageView {
  id: string;
  name: string;
  order: number;
  role: string;
}

export interface BoardResponse {
  id: string;
  name: string;
  createdBy: string;
  swimlanes: SwimlaneView[];
  stages: StageView[];
  clockTime: string;
  clockStatus: string;
}

export function listBoards(): Promise<BoardResponse[]> {
  return apiClient.get<BoardResponse[]>('/api/boards');
}

export function getBoard(boardId: string): Promise<BoardResponse> {
  return apiClient.get<BoardResponse>(`/api/boards/${boardId}`);
}

export function createBoard(name: string): Promise<BoardResponse> {
  return apiClient.post<BoardResponse>('/api/boards', { name });
}

export function deleteBoard(boardId: string): Promise<void> {
  return apiClient.del<void>(`/api/boards/${boardId}`);
}

export function countActiveCards(boardId: string): Promise<number> {
  return apiClient
    .get<{ cardCount: number }>(`/api/boards/${boardId}/card-count`)
    .then((response) => response.cardCount);
}
