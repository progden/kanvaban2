package io.progden.kanban.spring.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * {@code Swimlane} 的 JPA 對應表格，透過 {@code board_id} 歸屬到 {@link BoardJpaEntity}。
 */
@Entity
@Table(name = "swimlanes")
public class SwimlaneJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardJpaEntity board;

    @Column(nullable = false)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected SwimlaneJpaEntity() {
    }

    public SwimlaneJpaEntity(UUID id, BoardJpaEntity board, String name, int sortOrder) {
        this.id = id;
        this.board = board;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public void update(String name, int sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
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
}
