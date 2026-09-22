package io.progden.kanban.query.workload;

import java.util.UUID;

public record MemberWorkloadView(UUID userId, int cardCount) {
}
