package io.progden.kanban.query.timeline;

import java.time.Instant;
import java.util.UUID;

/**
 * {@link CardTimelineProjector} 的輸入之一：一筆 Stage 異動（對應 {@code card.stageTransitions}），
 * 依 {@code occurredAt} 由舊到新排序後交給投影使用。
 */
public record StageTransitionRecord(UUID fromStageId, UUID toStageId, Instant occurredAt) {
}
