// 對應 kanban-spring BoardController 的 GET /api/boards/{boardId}/activity-log（T-04 新增，
// spec-user-membership.md「Feature: 檢視看板活動紀錄」uc-view-board-activity-log），
// 欄位名稱與後端 ActivityLogEntryResponse 保持一致；operatorUsername／operatorDisplayName
// 允許為 null（後端找不到操作人時回傳 null，見 BoardController.toActivityLogEntryResponse）。
import { apiClient } from './http';

export interface ActivityLogEntryView {
  operatorUsername: string | null;
  operatorDisplayName: string | null;
  action: string;
  occurredAt: string;
}

export function viewActivityLog(boardId: string): Promise<ActivityLogEntryView[]> {
  return apiClient.get<ActivityLogEntryView[]>(`/api/boards/${boardId}/activity-log`);
}
