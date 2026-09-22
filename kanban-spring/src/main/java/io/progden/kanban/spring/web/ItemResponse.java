package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.Item;
import java.util.UUID;

record ItemResponse(UUID id, String component, String anchor, double x, double y, double width, double height,
        long z, boolean movable, boolean resizable, boolean removable) {

    static ItemResponse from(Item item) {
        return new ItemResponse(item.getId(), item.getComponent(), item.getAnchor().name().toLowerCase(),
                item.getX(), item.getY(), item.getWidth(), item.getHeight(), item.getZ(), item.isMovable(),
                item.isResizable(), item.isRemovable());
    }
}
