package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.Card;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

record CardResponse(UUID id, UUID boardId, String title, String description, LocalDate dueDate,
        List<String> labels, UUID swimlaneId, UUID stageId, List<UUID> assigneeIds, List<CommentView> comments) {

    static CardResponse from(Card card) {
        List<CommentView> commentViews = card.getComments().stream()
                .map(c -> new CommentView(c.getId(), c.getAuthorId(), c.getContent(), c.getCreatedAt()))
                .toList();
        return new CardResponse(card.getId(), card.getBoardId(), card.getTitle(), card.getDescription(),
                card.getDueDate(), card.getLabels(), card.getSwimlaneId(), card.getStageId(),
                card.getAssigneeIds(), commentViews);
    }

    record CommentView(UUID id, UUID authorId, String content, Instant createdAt) {
    }
}
