package io.progden.kanban.spring.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * {@code StageTransition} 的 JPA 對應表格，透過 {@code card_id} 歸屬到 {@link CardJpaEntity}。
 * 異動紀錄只會新增、不會更新，因此 persistence 層只提供建構子，沒有 update 方法。
 */
@Entity
@Table(name = "card_stage_transitions")
public class StageTransitionJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private CardJpaEntity card;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(name = "from_stage_id")
    private UUID fromStageId;

    @Column(name = "to_stage_id", nullable = false)
    private UUID toStageId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected StageTransitionJpaEntity() {
    }

    public StageTransitionJpaEntity(UUID id, CardJpaEntity card, UUID operatorId, UUID fromStageId,
            UUID toStageId, Instant occurredAt) {
        this.id = id;
        this.card = card;
        this.operatorId = operatorId;
        this.fromStageId = fromStageId;
        this.toStageId = toStageId;
        this.occurredAt = occurredAt;
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
