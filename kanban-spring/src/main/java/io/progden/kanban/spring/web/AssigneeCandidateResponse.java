package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.User;
import java.util.UUID;

record AssigneeCandidateResponse(UUID id, String username, String displayName) {

    static AssigneeCandidateResponse from(User user) {
        return new AssigneeCandidateResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
