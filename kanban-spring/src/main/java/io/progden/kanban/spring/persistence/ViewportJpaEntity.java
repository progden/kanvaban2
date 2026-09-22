package io.progden.kanban.spring.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

/**
 * {@code Viewport} 的 JPA 對應表格，只在 persistence 層使用。{@code canvas_id}／{@code user_id}
 * 加唯一鍵對應「同一 canvas 內以 viewport.user 唯一」（spec-canvas-layout.md 欄位定義）。
 */
@Entity
@Table(name = "viewports", uniqueConstraints = @UniqueConstraint(columnNames = {"canvas_id", "user_id"}))
public class ViewportJpaEntity {

    @Id
    private UUID id;

    @Column(name = "canvas_id", nullable = false)
    private UUID canvasId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private double x;

    @Column(nullable = false)
    private double y;

    @Column(nullable = false)
    private double zoom;

    protected ViewportJpaEntity() {
    }

    public ViewportJpaEntity(UUID id, UUID canvasId, UUID userId) {
        this.id = id;
        this.canvasId = canvasId;
        this.userId = userId;
    }

    public void update(double x, double y, double zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCanvasId() {
        return canvasId;
    }

    public UUID getUserId() {
        return userId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZoom() {
        return zoom;
    }
}
