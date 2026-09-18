package io.progden.kanban.spring.web;

import java.util.UUID;

record CreateCardRequest(String title, UUID swimlaneId, UUID stageId) {
}
