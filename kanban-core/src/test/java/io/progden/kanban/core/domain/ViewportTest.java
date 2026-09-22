package io.progden.kanban.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * {@code Viewport} 不變條件單元測試，對應 spec-canvas-layout.md「檢視區」Feature。
 */
class ViewportTest {

    private final UUID canvasId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void should_createFor_when_firstTime() {
        Viewport viewport = Viewport.createFor(canvasId, userId, 0, 0, 1);

        assertEquals(canvasId, viewport.getCanvasId());
        assertEquals(userId, viewport.getUserId());
        assertEquals(0, viewport.getX());
        assertEquals(0, viewport.getY());
        assertEquals(1, viewport.getZoom());
    }

    @Test
    void should_update_when_panOrZoom() {
        Viewport viewport = Viewport.createFor(canvasId, userId, 0, 0, 1);

        viewport.update(1200, -300, 2);

        assertEquals(1200, viewport.getX());
        assertEquals(-300, viewport.getY());
        assertEquals(2, viewport.getZoom());
    }
}
