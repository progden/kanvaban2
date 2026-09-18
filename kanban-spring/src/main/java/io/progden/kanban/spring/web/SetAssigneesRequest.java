package io.progden.kanban.spring.web;

import java.util.List;
import java.util.UUID;

record SetAssigneesRequest(List<UUID> assigneeIds) {
}
