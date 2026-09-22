package io.progden.kanban.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * {@code Item} 不變條件單元測試，對應 spec-canvas-layout.md「元件放置」「畫布元素排列」
 * 「畫布元素批次操作」Feature。
 */
class ItemTest {

    private final UUID canvasId = UUID.randomUUID();

    @Test
    void should_place_when_validSize() {
        Item item = Item.place(canvasId, "銷售圖表", ItemAnchor.CANVAS, 100, 200, 300, 200, 1, true, true, true);

        assertEquals(canvasId, item.getCanvasId());
        assertEquals("銷售圖表", item.getComponent());
        assertEquals(ItemAnchor.CANVAS, item.getAnchor());
        assertEquals(100, item.getX());
        assertEquals(200, item.getY());
        assertEquals(300, item.getWidth());
        assertEquals(200, item.getHeight());
        assertEquals(1, item.getZ());
    }

    @Test
    void should_rejectPlace_when_widthNotPositive() {
        DomainException exception = assertThrows(DomainException.class,
                () -> Item.place(canvasId, "銷售圖表", ItemAnchor.CANVAS, 0, 0, 0, 100, 1, true, true, true));

        assertEquals(ErrorCode.INVALID_ITEM_SIZE, exception.getCode());
    }

    @Test
    void should_move_when_movable() {
        Item item = place(100, 200, 300, 200, true, true, true);

        item.move(350, 40);

        assertEquals(350, item.getX());
        assertEquals(40, item.getY());
        assertEquals(300, item.getWidth());
        assertEquals(200, item.getHeight());
    }

    @Test
    void should_rejectMove_when_notMovable() {
        Item item = place(0, 0, 800, 600, false, true, true);

        DomainException exception = assertThrows(DomainException.class, () -> item.move(50, 50));

        assertEquals(ErrorCode.CANVAS_ITEM_NOT_MOVABLE, exception.getCode());
        assertEquals("此元素不可移動", exception.getMessage());
    }

    @Test
    void should_resizeFromTopLeft_when_resizable() {
        Item item = place(100, 200, 300, 200, true, true, true);

        item.resize(100, 200, 500, 320);

        assertEquals(100, item.getX());
        assertEquals(200, item.getY());
        assertEquals(500, item.getWidth());
        assertEquals(320, item.getHeight());
    }

    @Test
    void should_rejectResize_when_notResizable() {
        Item item = place(0, 0, 800, 600, true, false, true);

        DomainException exception = assertThrows(DomainException.class, () -> item.resize(0, 0, 400, 300));

        assertEquals(ErrorCode.CANVAS_ITEM_NOT_RESIZABLE, exception.getCode());
    }

    @Test
    void should_rejectResize_when_sizeNotPositive() {
        Item item = place(100, 200, 300, 200, true, true, true);

        DomainException exception = assertThrows(DomainException.class, () -> item.resize(100, 200, 300, 0));

        assertEquals(ErrorCode.INVALID_ITEM_SIZE, exception.getCode());
    }

    @Test
    void should_allowResizeInPlace_when_notMovableButResizable() {
        Item item = place(0, 0, 300, 200, false, true, true);

        item.resize(0, 0, 400, 300);

        assertEquals(400, item.getWidth());
        assertEquals(300, item.getHeight());
    }

    @Test
    void should_rejectResize_when_notMovableAndPositionChanged() {
        Item item = place(0, 0, 300, 200, false, true, true);

        DomainException exception = assertThrows(DomainException.class, () -> item.resize(-50, -50, 350, 250));

        assertEquals(ErrorCode.CANVAS_ITEM_NOT_MOVABLE, exception.getCode());
        assertEquals("此元素不可移動", exception.getMessage());
    }

    @Test
    void should_setCapabilities_when_itemExists() {
        Item item = place(0, 0, 300, 200, true, true, true);

        item.setCapabilities(false, false, false);

        assertEquals(false, item.isMovable());
        assertEquals(false, item.isResizable());
        assertEquals(false, item.isRemovable());
    }

    @Test
    void should_changeAnchor_when_toScreen() {
        Item item = place(0, 0, 300, 200, true, true, true);

        item.changeAnchor(ItemAnchor.SCREEN, 16, 160, 240, 120, 2);

        assertEquals(ItemAnchor.SCREEN, item.getAnchor());
        assertEquals(16, item.getX());
        assertEquals(160, item.getY());
        assertEquals(240, item.getWidth());
        assertEquals(120, item.getHeight());
        assertEquals(2, item.getZ());
    }

    @Test
    void should_reorderTo_when_toFrontOrBack() {
        Item item = place(0, 0, 100, 100, true, true, true);

        item.reorderTo(99);

        assertEquals(99, item.getZ());
    }

    @Test
    void should_translate_when_calledDirectly() {
        Item item = place(100, 200, 300, 200, true, true, true);

        item.translate(50, -30);

        assertEquals(150, item.getX());
        assertEquals(170, item.getY());
    }

    @Test
    void should_removeCheckPass_when_removable() {
        Item item = place(0, 0, 100, 100, true, true, true);

        item.ensureRemovable();
    }

    @Test
    void should_rejectRemove_when_notRemovable() {
        Item item = place(0, 0, 100, 100, true, true, false);

        DomainException exception = assertThrows(DomainException.class, item::ensureRemovable);

        assertEquals(ErrorCode.CANVAS_ITEM_NOT_REMOVABLE, exception.getCode());
        assertEquals("此元素不可移除", exception.getMessage());
    }

    @Test
    void should_useBatchMessage_when_ensureMovableWithMessage() {
        Item item = place(0, 0, 100, 100, false, true, true);

        DomainException exception = assertThrows(DomainException.class,
                () -> item.ensureMovable("所選元素中有不可移動的元素"));

        assertEquals("所選元素中有不可移動的元素", exception.getMessage());
    }

    @Test
    void should_useBatchMessage_when_ensureRemovableWithMessage() {
        Item item = place(0, 0, 100, 100, true, true, false);

        DomainException exception = assertThrows(DomainException.class,
                () -> item.ensureRemovable("所選元素中有不可移除的元素"));

        assertEquals("所選元素中有不可移除的元素", exception.getMessage());
    }

    private Item place(double x, double y, double width, double height, boolean movable, boolean resizable,
            boolean removable) {
        return Item.place(canvasId, "測試元件", ItemAnchor.CANVAS, x, y, width, height, 1, movable, resizable,
                removable);
    }
}
