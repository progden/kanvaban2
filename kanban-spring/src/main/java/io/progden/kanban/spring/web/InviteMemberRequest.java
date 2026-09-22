package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.BoardRole;

record InviteMemberRequest(String username, BoardRole role) {
}
