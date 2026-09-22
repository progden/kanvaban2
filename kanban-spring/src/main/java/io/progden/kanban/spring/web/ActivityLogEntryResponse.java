package io.progden.kanban.spring.web;

import io.progden.kanban.query.BoardActivityLogEntry;
import java.time.Instant;

record ActivityLogEntryResponse(String operatorUsername, String operatorDisplayName, String action, Instant occurredAt) {

    static ActivityLogEntryResponse of(BoardActivityLogEntry entry, String operatorUsername, String operatorDisplayName) {
        return new ActivityLogEntryResponse(operatorUsername, operatorDisplayName, entry.action(), entry.occurredAt());
    }
}
