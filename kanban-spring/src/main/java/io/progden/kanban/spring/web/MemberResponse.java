package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.BoardRole;
import io.progden.kanban.core.domain.User;

record MemberResponse(String username, String displayName, BoardRole role) {

    static MemberResponse of(User user, BoardRole role) {
        return new MemberResponse(user.getUsername(), user.getDisplayName(), role);
    }
}
