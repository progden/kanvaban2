package io.progden.kanban.core.domain;

import java.util.UUID;

/**
 * Item Aggregate Root：對應 spec-canvas-layout.md 的 {@code item}，某個元件在畫布上的一次放置。
 *
 * <p>{@code item.z} 的計算（同一錨定方式現有元素最大 z 值加一、置頂／置底）需要跨 {@code item} 查詢，
 * 由呼叫端（application 層）透過 {@code ItemRepository} 查出同一 {@code canvasId}／{@code anchor}
 * 現有元素後計算好新的 z 值再傳進來；本類別本身不查詢其他 {@code item}。
 */
public final class Item {

    private final UUID id;
    private final UUID canvasId;
    private String component;
    private ItemAnchor anchor;
    private double x;
    private double y;
    private double width;
    private double height;
    private long z;
    private boolean movable;
    private boolean resizable;
    private boolean removable;

    private Item(UUID id, UUID canvasId, String component, ItemAnchor anchor, double x, double y, double width,
            double height, long z, boolean movable, boolean resizable, boolean removable) {
        this.id = id;
        this.canvasId = canvasId;
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

    /**
     * {@code uc-place-item}／{@code uc-init-canvas} 共用：{@code z} 由呼叫端算好傳入
     * （同一錨定方式現有元素最大 z 值加一，尚無元素時為 1）。
     */
    public static Item place(UUID canvasId, String component, ItemAnchor anchor, double x, double y, double width,
            double height, long z, boolean movable, boolean resizable, boolean removable) {
        ensureValidSize(width, height);
        return new Item(UUID.randomUUID(), canvasId, component, anchor, x, y, width, height, z, movable, resizable,
                removable);
    }

    public static Item reconstruct(UUID id, UUID canvasId, String component, ItemAnchor anchor, double x, double y,
            double width, double height, long z, boolean movable, boolean resizable, boolean removable) {
        return new Item(id, canvasId, component, anchor, x, y, width, height, z, movable, resizable, removable);
    }

    /** {@code uc-move-item}：移動元素，{@code item.width}／{@code item.height}／{@code item.z} 不變。 */
    public void move(double newX, double newY) {
        ensureMovable();
        this.x = newX;
        this.y = newY;
    }

    /**
     * {@code uc-resize-item}：{@code item.movable} 為 false 時，指定的 {@code item.x}、{@code item.y}
     * 須與現值相同（p4），否則視為藉調整大小改變位置，拒絕。
     */
    public void resize(double newX, double newY, double newWidth, double newHeight) {
        ensureResizable();
        ensureValidSize(newWidth, newHeight);
        if (!movable && (newX != this.x || newY != this.y)) {
            throw new DomainException(ErrorCode.CANVAS_ITEM_NOT_MOVABLE, "此元素不可移動");
        }
        this.x = newX;
        this.y = newY;
        this.width = newWidth;
        this.height = newHeight;
    }

    /** {@code uc-set-item-capabilities}：更新可否移動／調整大小／移除，{@code pre} 只要求元素存在。 */
    public void setCapabilities(boolean movable, boolean resizable, boolean removable) {
        this.movable = movable;
        this.resizable = resizable;
        this.removable = removable;
    }

    /**
     * {@code uc-set-item-anchor}：新錨定座標系與單位由呼叫端解讀後傳入指定值，不由系統換算；
     * 新的 {@code item.z} 由呼叫端算好傳入（新錨定方式現有元素最大 z 值加一）。
     */
    public void changeAnchor(ItemAnchor newAnchor, double newX, double newY, double newWidth, double newHeight,
            long newZ) {
        this.anchor = newAnchor;
        this.x = newX;
        this.y = newY;
        this.width = newWidth;
        this.height = newHeight;
        this.z = newZ;
    }

    /** {@code uc-reorder-item}：置頂或置底，新的 {@code item.z} 由呼叫端算好傳入。 */
    public void reorderTo(long newZ) {
        this.z = newZ;
    }

    /**
     * {@code uc-move-items} 批次移動用：位移量套用到目前位置，不做 {@code movable} 檢查——批次操作
     * 的「所選元件皆可移動」由呼叫端在套用前先對全部指定元素驗證過（{@link #ensureMovable()}），
     * 全部通過才逐一呼叫本方法，確保「全成功或全失敗」。
     */
    public void translate(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    /** {@code uc-move-item}：單一移動的可移動檢查，訊息固定為「此元素不可移動」。 */
    public void ensureMovable() {
        ensureMovable("此元素不可移動");
    }

    /**
     * {@code uc-move-items} 批次逐一驗證用：訊息由呼叫端指定（批次情境訊息為
     * 「所選元素中有不可移動的元素」，與單一操作的訊息不同）。
     */
    public void ensureMovable(String message) {
        if (!movable) {
            throw new DomainException(ErrorCode.CANVAS_ITEM_NOT_MOVABLE, message);
        }
    }

    private void ensureResizable() {
        if (!resizable) {
            throw new DomainException(ErrorCode.CANVAS_ITEM_NOT_RESIZABLE, "此元素不可調整大小");
        }
    }

    /** {@code uc-remove-item}：單一移除的可移除檢查，訊息固定為「此元素不可移除」。 */
    public void ensureRemovable() {
        ensureRemovable("此元素不可移除");
    }

    /**
     * {@code uc-remove-items} 批次逐一驗證用：訊息由呼叫端指定（批次情境訊息為
     * 「所選元素中有不可移除的元素」，與單一操作的訊息不同）。
     */
    public void ensureRemovable(String message) {
        if (!removable) {
            throw new DomainException(ErrorCode.CANVAS_ITEM_NOT_REMOVABLE, message);
        }
    }

    private static void ensureValidSize(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new DomainException(ErrorCode.INVALID_ITEM_SIZE, "寬與高必須大於 0");
        }
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
