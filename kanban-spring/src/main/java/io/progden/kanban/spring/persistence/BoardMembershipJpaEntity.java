package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.BoardRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * {@code BoardMembership} 的 JPA 對應表格，只在 persistence 層使用，不外流到 domain／application 層。
 * 只以 {@code boardId}／{@code userId}（UUID 欄位）參照，不建立 JPA 關聯——{@code BoardMembership}
 * 是獨立 Aggregate（design-user-membership.md 第 2 節）。
 */
@Entity
@Table(name = "board_memberships")
public class BoardMembershipJpaEntity {

    @Id
    private UUID id;

    @Column(name = "board_id", nullable = false)
    private UUID boardId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardRole role;

    protected BoardMembershipJpaEntity() {
    }

    public BoardMembershipJpaEntity(UUID id, UUID boardId, UUID userId, BoardRole role) {
        this.id = id;
        this.boardId = boardId;
        this.userId = userId;
        this.role = role;
    }

    public void updateRole(BoardRole role) {
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public UUID getUserId() {
        return userId;
    }

    public BoardRole getRole() {
        return role;
    }
}
