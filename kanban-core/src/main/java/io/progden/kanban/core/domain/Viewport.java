package io.progden.kanban.core.domain;

import java.util.UUID;

/**
 * Viewport Aggregate Root：對應 spec-canvas-layout.md 的 {@code viewport}，某位使用者在某個
 * {@code canvas} 目前的平移偏移與縮放比例，每位使用者各一個，第一次設定時建立。
 *
 * <p>{@code viewport.user} 欄位表型別寫 string(100)（帳號模組定案後改為 ref），本模組沿用既有
 * {@code User} 的 {@code UUID} id（低風險技術決定，與 {@code BoardMembership.userId} 等既有欄位一致，
 * 見 decision-log.md）。
 *
 * <p>縮放範圍檢查（{@code canvas.zoom-min}／{@code canvas.zoom-max}）由呼叫端先呼叫
 * {@link Canvas#ensureZoomInRange(double)} 完成，本類別不持有 {@code Canvas} 參照。
 */
public final class Viewport {

    private final UUID id;
    private final UUID canvasId;
    private final UUID userId;
    private double x;
    private double y;
    private double zoom;

    private Viewport(UUID id, UUID canvasId, UUID userId, double x, double y, double zoom) {
        this.id = id;
        this.canvasId = canvasId;
        this.userId = userId;
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public static Viewport createFor(UUID canvasId, UUID userId, double x, double y, double zoom) {
        return new Viewport(UUID.randomUUID(), canvasId, userId, x, y, zoom);
    }

    public static Viewport reconstruct(UUID id, UUID canvasId, UUID userId, double x, double y, double zoom) {
        return new Viewport(id, canvasId, userId, x, y, zoom);
    }

    /** {@code uc-set-viewport}：更新自己的平移偏移與縮放比例。 */
    public void update(double newX, double newY, double newZoom) {
        this.x = newX;
        this.y = newY;
        this.zoom = newZoom;
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
