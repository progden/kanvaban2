package io.progden.kanban.query.workload;

import java.util.List;

public record WorkloadView(List<MemberWorkloadView> members, int unassignedCount) {
}
