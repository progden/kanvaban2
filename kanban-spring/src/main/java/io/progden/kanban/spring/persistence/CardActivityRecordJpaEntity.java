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
 * {@code Card} 自己的 {@code ActivityRecord}（CR-001），透過 {@code card_id} 歸屬到
 * {@link CardJpaEntity}；與 {@link ActivityRecordJpaEntity}（Board 的活動紀錄）分開儲存，
 * 兩個 Aggregate 各自記錄自己身上發生的事（design-kanban-basic.md 5a 節）。
 */
@Entity
@Table(name = "card_activity_records")
public class CardActivityRecordJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private CardJpaEntity card;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(nullable = false)
    private String action;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected CardActivityRecordJpaEntity() {
    }

    public CardActivityRecordJpaEntity(
            UUID id, CardJpaEntity card, UUID operatorId, String action, Instant occurredAt) {
        this.id = id;
        this.card = card;
        this.operatorId = operatorId;
        this.action = action;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public String getAction() {
        return action;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
