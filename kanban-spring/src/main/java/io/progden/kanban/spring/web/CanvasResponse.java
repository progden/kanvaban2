package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.Canvas;
import java.util.List;
import java.util.UUID;

record CanvasResponse(UUID id, UUID boardId, double zoomMin, double zoomMax, List<ItemResponse> items,
        ViewportResponse viewport) {

    static CanvasResponse of(Canvas canvas, List<ItemResponse> items, ViewportResponse viewport) {
        return new CanvasResponse(canvas.getId(), canvas.getBoardId(), canvas.getZoomMin(), canvas.getZoomMax(),
                items, viewport);
    }
}
