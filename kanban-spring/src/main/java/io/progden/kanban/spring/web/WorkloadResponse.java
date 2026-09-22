package io.progden.kanban.spring.web;

import java.util.List;
import java.util.UUID;

record WorkloadResponse(List<MemberWorkloadEntry> members, int unassignedCount) {
}

record MemberWorkloadEntry(UUID userId, String username, int cardCount) {
}
