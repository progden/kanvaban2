package io.progden.kanban.spring.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

import io.progden.kanban.core.domain.StageRole;

/**
 * {@code Stage} 的 JPA 對應表格，透過 {@code board_id} 歸屬到 {@link BoardJpaEntity}。
 */
@Entity
@Table(name = "stages")
public class StageJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardJpaEntity board;

    @Column(nullable = false)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StageRole role;

    protected StageJpaEntity() {
    }

    public StageJpaEntity(UUID id, BoardJpaEntity board, String name, int sortOrder, StageRole role) {
        this.id = id;
        this.board = board;
        this.name = name;
        this.sortOrder = sortOrder;
        this.role = role;
    }

    public void update(String name, int sortOrder, StageRole role) {
        this.name = name;
        this.sortOrder = sortOrder;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public StageRole getRole() {
        return role;
    }
}
