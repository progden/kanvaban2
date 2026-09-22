// 對應 kanban-spring WorkloadController（spec-workload.md uc-view-workload）的
// GET /api/boards/{boardId}/workload，欄位名稱與後端 WorkloadResponse／MemberWorkloadEntry record 保持一致。
import { apiClient } from './http';

export interface MemberWorkloadEntry {
  userId: string;
  username: string;
  displayName: string;
  cardCount: number;
}

export interface WorkloadView {
  members: MemberWorkloadEntry[];
  unassignedCount: number;
}

export function getWorkload(boardId: string): Promise<WorkloadView> {
  return apiClient.get<WorkloadView>(`/api/boards/${boardId}/workload`);
}
