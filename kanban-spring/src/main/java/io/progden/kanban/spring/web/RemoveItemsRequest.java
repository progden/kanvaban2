package io.progden.kanban.spring.web;

import java.util.List;
import java.util.UUID;

record RemoveItemsRequest(List<UUID> itemIds) {
}
