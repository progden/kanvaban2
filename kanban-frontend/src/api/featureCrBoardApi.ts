// 對應 kanban-spring FeatureCrBoardController 的 GET /api/boards/{boardId}/feature-cr-board（T-08 新增，
// spec-feature-cr-board.md「Feature: Feature／CR 追蹤表」uc-view-feature-cr-board），
// 欄位名稱與後端 FeatureCrBoardResponse 保持一致；status 為後端已組好的中文字串
// （「已完成」／「開發中」／「未開發」，見 FeatureCrBoardCalculator.statusOf）。
import { apiClient } from './http';

export interface FeatureCrView {
  crId: string;
  status: string;
}

export interface FeatureView {
  featureId: string;
  status: string;
  crs: FeatureCrView[];
}

export interface FeatureCrBoardView {
  features: FeatureView[];
  orphanCrIds: string[];
  warnings: string[];
}

export function viewFeatureCrBoard(boardId: string): Promise<FeatureCrBoardView> {
  return apiClient.get<FeatureCrBoardView>(`/api/boards/${boardId}/feature-cr-board`);
}
