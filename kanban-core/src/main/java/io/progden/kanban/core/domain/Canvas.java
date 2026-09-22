package io.progden.kanban.core.domain;

import java.util.UUID;

/**
 * Canvas Aggregate Root：對應 spec-canvas-layout.md 的 {@code canvas}，每個 {@code board} 至多一個，
 * 使用者第一次開啟該 Board 時由系統自動建立（{@code uc-init-canvas}），不由使用者建立或刪除。
 *
 * <p>{@code canvas.zoom-min}／{@code canvas.zoom-max} 由系統設定，本模組固定用預設值
 * （0.1／4，spec 欄位定義），設定方式不在本模組範圍內，故不提供異動這兩個欄位的方法。
 */
public final class Canvas {

    private static final double DEFAULT_ZOOM_MIN = 0.1;
    private static final double DEFAULT_ZOOM_MAX = 4;

    private final UUID id;
    private final UUID boardId;
    private final double zoomMin;
    private final double zoomMax;

    private Canvas(UUID id, UUID boardId, double zoomMin, double zoomMax) {
        this.id = id;
        this.boardId = boardId;
        this.zoomMin = zoomMin;
        this.zoomMax = zoomMax;
    }

    public static Canvas createFor(UUID boardId) {
        return new Canvas(UUID.randomUUID(), boardId, DEFAULT_ZOOM_MIN, DEFAULT_ZOOM_MAX);
    }

    public static Canvas reconstruct(UUID id, UUID boardId, double zoomMin, double zoomMax) {
        return new Canvas(id, boardId, zoomMin, zoomMax);
    }

    public void ensureZoomInRange(double zoom) {
        if (zoom < zoomMin || zoom > zoomMax) {
            throw new DomainException(ErrorCode.VIEWPORT_ZOOM_OUT_OF_RANGE, "縮放比例超出範圍");
        }
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
