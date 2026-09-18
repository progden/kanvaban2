package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.ClockStatus;
import io.progden.kanban.core.domain.StageRole;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record BoardResponse(UUID id, String name, UUID createdBy, List<SwimlaneView> swimlanes, List<StageView> stages,
        Instant clockTime, ClockStatus clockStatus) {

    static BoardResponse from(Board board) {
        List<SwimlaneView> swimlaneViews = board.getSwimlanes().stream()
                .map(s -> new SwimlaneView(s.getId(), s.getName(), s.getOrder()))
                .toList();
        List<StageView> stageViews = board.getStages().stream()
                .map(s -> new StageView(s.getId(), s.getName(), s.getOrder(), s.getRole()))
                .toList();
        Instant systemNow = Instant.now();
        return new BoardResponse(board.getId(), board.getName(), board.getCreatedBy(), swimlaneViews, stageViews,
                board.getClockTime(systemNow), board.getClockStatus());
    }

    record SwimlaneView(UUID id, String name, int order) {
    }

    record StageView(UUID id, String name, int order, StageRole role) {
    }
}
