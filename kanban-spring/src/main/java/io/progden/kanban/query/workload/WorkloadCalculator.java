package io.progden.kanban.query.workload;

import io.progden.kanban.core.domain.StageRole;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code uc-view-workload} 的計算邏輯：純函式，依 Active Card（未刪除、所在 Stage 角色不是 Done）的
 * {@code assigneeIds} 分組統計工作量（spec-workload.md「其他名詞」Active Card／Workload／未指派）。
 *
 * <p>post p2「同時有多位負責人時，每位負責人的 Workload 各自包含該 card」：一張卡片對它的每位負責人
 * 都各記一次，不是均分。
 */
public final class WorkloadCalculator {

    private WorkloadCalculator() {
    }

    public static WorkloadView calculate(
            List<CardWorkloadSource> cards, List<UUID> memberUserIds, Map<UUID, StageRole> roleByStageId) {
        Map<UUID, Integer> countByMember = new LinkedHashMap<>();
        memberUserIds.forEach(id -> countByMember.put(id, 0));
        int unassignedCount = 0;

        for (CardWorkloadSource card : cards) {
            if (roleByStageId.get(card.stageId()) == StageRole.DONE) {
                continue;
            }
            if (card.assigneeIds().isEmpty()) {
                unassignedCount++;
                continue;
            }
            for (UUID assigneeId : card.assigneeIds()) {
                countByMember.merge(assigneeId, 1, Integer::sum);
            }
        }

        List<MemberWorkloadView> members = countByMember.entrySet().stream()
                .map(e -> new MemberWorkloadView(e.getKey(), e.getValue()))
                .toList();
        return new WorkloadView(members, unassignedCount);
    }
}
