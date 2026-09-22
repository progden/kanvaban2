package io.progden.kanban.query.workload;

import java.util.List;
import java.util.UUID;

/**
 * {@link WorkloadCalculator} 的輸入：單張卡片與 Workload 計算相關的欄位（去除 persistence 細節）。
 */
public record CardWorkloadSource(UUID cardId, UUID stageId, List<UUID> assigneeIds) {
}
