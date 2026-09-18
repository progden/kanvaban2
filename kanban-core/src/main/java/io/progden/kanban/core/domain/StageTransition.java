package io.progden.kanban.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * StageTransition（狀態異動紀錄）：{@code Card} 的內部實體，記錄一次跨 Stage 移動的操作人、
 * 異動時間與異動前後的 Stage（{@code uc-move-card-stage} post），只能透過 {@link Card#moveToStage}
 * 新增（design-kanban-basic.md 第 5 節）。
 */
public final class StageTransition {

    private final UUID id;
    private final UUID operatorId;
    private final UUID fromStageId;
    private final UUID toStageId;
    private final Instant occurredAt;

    StageTransition(UUID operatorId, UUID fromStageId, UUID toStageId, Instant occurredAt) {
        this(UUID.randomUUID(), operatorId, fromStageId, toStageId, occurredAt);
    }

    private StageTransition(UUID id, UUID operatorId, UUID fromStageId, UUID toStageId, Instant occurredAt) {
        this.id = id;
        this.operatorId = operatorId;
        this.fromStageId = fromStageId;
        this.toStageId = toStageId;
        this.occurredAt = occurredAt;
    }

    static StageTransition reconstruct(
            UUID id, UUID operatorId, UUID fromStageId, UUID toStageId, Instant occurredAt) {
        return new StageTransition(id, operatorId, fromStageId, toStageId, occurredAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public UUID getFromStageId() {
        return fromStageId;
    }

    public UUID getToStageId() {
        return toStageId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
