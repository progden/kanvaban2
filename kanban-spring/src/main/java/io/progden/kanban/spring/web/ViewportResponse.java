package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.Viewport;

record ViewportResponse(double x, double y, double zoom) {

    static ViewportResponse from(Viewport viewport) {
        return new ViewportResponse(viewport.getX(), viewport.getY(), viewport.getZoom());
    }
}
