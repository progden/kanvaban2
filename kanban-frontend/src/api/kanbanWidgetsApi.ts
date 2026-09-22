// 對應 kanban-spring KanbanWidgetsController（spec-kanban-widgets.md 六個 uc-view-* 的 web 層），
// 欄位名稱與後端 record 保持一致。
import { apiClient } from './http';

export interface CardTimingView {
  cardId: string;
  title: string;
  leadTimeDays: number;
  cycleTimeDays: number | null;
  doneAt: string;
}

export interface PercentileView {
  p50: number;
  p85: number;
  p95: number;
}

export interface CycleLeadTimeResponse {
  cards: CardTimingView[];
  leadTime: PercentileView;
  cycleTime: PercentileView;
  excludedCycleTimeCount: number;
}

export interface StageWipEntry {
  stageId: string;
  stageName: string;
  count: number;
}

export interface WipResponse {
  stages: StageWipEntry[];
}

export interface AgingCardEntry {
  cardId: string;
  title: string;
  stageId: string;
  ageDays: number;
}

export interface AgingWipResponse {
  cards: AgingCardEntry[];
}

export type ThroughputUnit = 'day' | 'week';

export interface ThroughputPeriodEntry {
  periodStart: string;
  count: number;
}

export interface ThroughputResponse {
  periods: ThroughputPeriodEntry[];
}

export interface CfdEntry {
  date: string;
  countByStage: Record<string, number>;
}

export interface CfdResponse {
  dataPoints: CfdEntry[];
}

export interface DueDateCardEntry {
  cardId: string;
  title: string;
  dueDate: string;
}

export interface DueDateReminderResponse {
  overdue: DueDateCardEntry[];
  upcoming: DueDateCardEntry[];
}

export function getCycleLeadTime(boardId: string): Promise<CycleLeadTimeResponse> {
  return apiClient.get<CycleLeadTimeResponse>(`/api/boards/${boardId}/widgets/cycle-lead-time`);
}

export function getWip(boardId: string): Promise<WipResponse> {
  return apiClient.get<WipResponse>(`/api/boards/${boardId}/widgets/wip`);
}

export function getAgingWip(boardId: string): Promise<AgingWipResponse> {
  return apiClient.get<AgingWipResponse>(`/api/boards/${boardId}/widgets/aging-wip`);
}

export function getThroughput(boardId: string, unit: ThroughputUnit): Promise<ThroughputResponse> {
  return apiClient.get<ThroughputResponse>(`/api/boards/${boardId}/widgets/throughput?unit=${unit}`);
}

export function getCfd(boardId: string): Promise<CfdResponse> {
  return apiClient.get<CfdResponse>(`/api/boards/${boardId}/widgets/cfd`);
}

export function getDueDateReminder(boardId: string, thresholdDays?: number): Promise<DueDateReminderResponse> {
  const query = thresholdDays === undefined ? '' : `?thresholdDays=${thresholdDays}`;
  return apiClient.get<DueDateReminderResponse>(`/api/boards/${boardId}/widgets/duedate-reminder${query}`);
}
