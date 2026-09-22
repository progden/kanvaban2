package io.progden.kanban.spring.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * {@code board-membership} 事件（邀請成員／變更角色／移除成員）的活動紀錄，供
 * {@code uc-view-board-activity-log} 與 {@code Board} 自己的活動紀錄合併顯示。
 *
 * <p>只以 {@code boardId}（UUID 欄位）參照，不對 {@code board_memberships} 建立 JPA 關聯／
 * cascade：{@code uc-remove-member} 會刪掉那一筆 {@code BoardMembership}，但「將 X 移出看板」這筆
 * 活動紀錄仍要留著（design-user-membership.md 第 8 節：這是獨立於 Aggregate 之外的查詢投影，
 * 見 kanban-spring 的 {@code io.progden.kanban.query} 查詢服務）。
 */
@Entity
@Table(name = "board_membership_activity_records")
public class BoardMembershipActivityJpaEntity {

    @Id
    private UUID id;

    @Column(name = "board_id", nullable = false)
    private UUID boardId;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(nullable = false)
    private String action;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected BoardMembershipActivityJpaEntity() {
    }

    public BoardMembershipActivityJpaEntity(UUID boardId, UUID operatorId, String action, Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.boardId = boardId;
        this.operatorId = operatorId;
        this.action = action;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBoardId() {
        return boardId;
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
