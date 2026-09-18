package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.User;

record UserResponse(String username, String displayName) {

    static UserResponse from(User user) {
        return new UserResponse(user.getUsername(), user.getDisplayName());
    }
}
