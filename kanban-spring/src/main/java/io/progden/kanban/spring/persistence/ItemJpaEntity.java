package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.ItemAnchor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

/**
 * {@code Item} 的 JPA 對應表格，只在 persistence 層使用。{@code canvas_id}／{@code anchor}／
 * {@code z} 加唯一鍵對應「{@code item.z} 必填、同一 canvas 內唯一」（spec-canvas-layout.md
 * 「層序」：畫面固定元素與畫布元素各自比較 z 值，因此唯一鍵含 anchor）。
 */
@Entity
@Table(name = "canvas_items", uniqueConstraints = @UniqueConstraint(columnNames = {"canvas_id", "anchor", "z"}))
public class ItemJpaEntity {

    @Id
    private UUID id;

    @Column(name = "canvas_id", nullable = false)
    private UUID canvasId;

    @Column(nullable = false, length = 100)
    private String component;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemAnchor anchor;

    @Column(nullable = false)
    private double x;

    @Column(nullable = false)
    private double y;

    @Column(nullable = false)
    private double width;

    @Column(nullable = false)
    private double height;

    @Column(nullable = false)
    private long z;

    @Column(nullable = false)
    private boolean movable;

    @Column(nullable = false)
    private boolean resizable;

    @Column(nullable = false)
    private boolean removable;

    protected ItemJpaEntity() {
    }

    public ItemJpaEntity(UUID id, UUID canvasId) {
        this.id = id;
        this.canvasId = canvasId;
    }

    public void update(String component, ItemAnchor anchor, double x, double y, double width, double height, long z,
            boolean movable, boolean resizable, boolean removable) {
        this.component = component;
        this.anchor = anchor;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.z = z;
        this.movable = movable;
        this.resizable = resizable;
        this.removable = removable;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCanvasId() {
        return canvasId;
    }

    public String getComponent() {
        return component;
    }

    public ItemAnchor getAnchor() {
        return anchor;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public long getZ() {
        return z;
    }

    public boolean isMovable() {
        return movable;
    }

    public boolean isResizable() {
        return resizable;
    }

    public boolean isRemovable() {
        return removable;
    }
}
