package io.progden.kanban.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * {@link StageTransition} 的唯讀快照，供 persistence 層在 {@link Card#reconstruct} 時傳入既有資料。
 */
public record StageTransitionSnapshot(
        UUID id, UUID operatorId, UUID fromStageId, UUID toStageId, Instant occurredAt) {
}
