package io.progden.kanban.spring.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

/**
 * {@code Canvas} 的 JPA 對應表格，只在 persistence 層使用。{@code board_id} 加唯一鍵對應
 * 「每個 board 至多一個 canvas」（spec-canvas-layout.md 關係表 board→canvas max 1）。
 */
@Entity
@Table(name = "canvases", uniqueConstraints = @UniqueConstraint(columnNames = "board_id"))
public class CanvasJpaEntity {

    @Id
    private UUID id;

    @Column(name = "board_id", nullable = false)
    private UUID boardId;

    @Column(name = "zoom_min", nullable = false)
    private double zoomMin;

    @Column(name = "zoom_max", nullable = false)
    private double zoomMax;

    protected CanvasJpaEntity() {
    }

    public CanvasJpaEntity(UUID id, UUID boardId, double zoomMin, double zoomMax) {
        this.id = id;
        this.boardId = boardId;
        this.zoomMin = zoomMin;
        this.zoomMax = zoomMax;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public double getZoomMin() {
        return zoomMin;
    }

    public double getZoomMax() {
        return zoomMax;
    }
}
