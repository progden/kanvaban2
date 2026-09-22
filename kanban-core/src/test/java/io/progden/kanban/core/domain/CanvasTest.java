package io.progden.kanban.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * {@code Canvas} 不變條件單元測試，對應 spec-canvas-layout.md「看板畫布初始化」「檢視區」Feature。
 */
class CanvasTest {

    private final UUID boardId = UUID.randomUUID();

    @Test
    void should_createWithDefaultZoomRange_when_createFor() {
        Canvas canvas = Canvas.createFor(boardId);

        assertEquals(boardId, canvas.getBoardId());
        assertEquals(0.1, canvas.getZoomMin());
        assertEquals(4, canvas.getZoomMax());
    }

    @Test
    void should_passEnsureZoomInRange_when_withinRange() {
        Canvas canvas = Canvas.createFor(boardId);

        canvas.ensureZoomInRange(0.1);
        canvas.ensureZoomInRange(4);
        canvas.ensureZoomInRange(1);
    }

    @Test
    void should_rejectEnsureZoomInRange_when_belowMin() {
        Canvas canvas = Canvas.createFor(boardId);

        DomainException exception = assertThrows(DomainException.class, () -> canvas.ensureZoomInRange(0));

        assertEquals(ErrorCode.VIEWPORT_ZOOM_OUT_OF_RANGE, exception.getCode());
    }

    @Test
    void should_rejectEnsureZoomInRange_when_aboveMax() {
        Canvas canvas = Canvas.createFor(boardId);

        DomainException exception = assertThrows(DomainException.class, () -> canvas.ensureZoomInRange(4.1));

        assertEquals(ErrorCode.VIEWPORT_ZOOM_OUT_OF_RANGE, exception.getCode());
    }
}
