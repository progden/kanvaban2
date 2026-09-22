package io.progden.kanban.spring.web;

import java.util.List;
import java.util.UUID;

record MoveItemsRequest(List<UUID> itemIds, double dx, double dy) {
}
