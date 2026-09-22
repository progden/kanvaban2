package io.progden.kanban.query.workload;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.progden.kanban.core.domain.StageRole;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WorkloadCalculatorTest {

    private final UUID startStageId = UUID.randomUUID();
    private final UUID doneStageId = UUID.randomUUID();
    private final Map<UUID, StageRole> roleByStageId = Map.of(startStageId, StageRole.START, doneStageId, StageRole.DONE);
    private final UUID yating = UUID.randomUUID();
    private final UUID zhiming = UUID.randomUUID();

    @Test
    void countsActiveCardsPerAssignee() {
        List<CardWorkloadSource> cards = List.of(
                new CardWorkloadSource(UUID.randomUUID(), startStageId, List.of(yating)),
                new CardWorkloadSource(UUID.randomUUID(), startStageId, List.of(yating)),
                new CardWorkloadSource(UUID.randomUUID(), startStageId, List.of(yating)));

        WorkloadView result = WorkloadCalculator.calculate(cards, List.of(yating, zhiming), roleByStageId);

        assertEquals(3, workloadOf(result, yating));
        assertEquals(0, workloadOf(result, zhiming));
    }

    @Test
    void cardWithMultipleAssigneesCountsForEachOfThem() {
        List<CardWorkloadSource> cards = List.of(
                new CardWorkloadSource(UUID.randomUUID(), startStageId, List.of(yating, zhiming)));

        WorkloadView result = WorkloadCalculator.calculate(cards, List.of(yating, zhiming), roleByStageId);

        assertEquals(1, workloadOf(result, yating));
        assertEquals(1, workloadOf(result, zhiming));
    }

    @Test
    void cardsWithoutAssigneesAreCountedAsUnassigned() {
        List<CardWorkloadSource> cards = List.of(
                new CardWorkloadSource(UUID.randomUUID(), startStageId, List.of()),
                new CardWorkloadSource(UUID.randomUUID(), startStageId, List.of()));

        WorkloadView result = WorkloadCalculator.calculate(cards, List.of(yating), roleByStageId);

        assertEquals(2, result.unassignedCount());
    }

    @Test
    void cardsInDoneStageAreExcludedFromWorkload() {
        List<CardWorkloadSource> cards = List.of(
                new CardWorkloadSource(UUID.randomUUID(), doneStageId, List.of(yating)));

        WorkloadView result = WorkloadCalculator.calculate(cards, List.of(yating), roleByStageId);

        assertEquals(0, workloadOf(result, yating));
    }

    private int workloadOf(WorkloadView view, UUID userId) {
        return view.members().stream()
                .filter(m -> m.userId().equals(userId))
                .findFirst()
                .orElseThrow()
                .cardCount();
    }
}
