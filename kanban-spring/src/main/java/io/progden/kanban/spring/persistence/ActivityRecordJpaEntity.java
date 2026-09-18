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
 * {@code ActivityRecord}（CR-001）的 JPA 對應表格，透過 {@code board_id} 歸屬到 {@link BoardJpaEntity}。
 * 活動紀錄只會新增、不會更新或刪除，因此 persistence 層只提供建構子，沒有 update 方法。
 */
@Entity
@Table(name = "board_activity_records")
public class ActivityRecordJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardJpaEntity board;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(nullable = false)
    private String action;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected ActivityRecordJpaEntity() {
    }

    public ActivityRecordJpaEntity(UUID id, BoardJpaEntity board, UUID operatorId, String action, Instant occurredAt) {
        this.id = id;
        this.board = board;
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
