package io.progden.kanban.query.timeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * {@link CycleLeadTimeCalculator} 純函式單元測試，對應 spec-kanban-widgets.md
 * {@code uc-view-cycle-lead-time} post p1～p3。
 */
class CycleLeadTimeCalculatorTest {

    @Test
    void completedCardShowsLeadAndCycleTimeAndPercentiles() {
        CardTimeline timelineA = timeline("A", "2026-09-01", "2026-09-02", "2026-09-05");

        CycleLeadTimeView view = CycleLeadTimeCalculator.calculate(List.of(timelineA));

        assertEquals(1, view.cards().size());
        assertEquals(4L, view.cards().get(0).leadTimeDays());
        assertEquals(3L, view.cards().get(0).cycleTimeDays());
        assertEquals(4L, view.leadTime().p50());
        assertEquals(3L, view.cycleTime().p50());
    }

    @Test
    void cardWithoutStartExcludedFromCycleTimeStats() {
        CardTimeline timelineB = timeline("B", "2026-09-01", null, "2026-09-03");

        CycleLeadTimeView view = CycleLeadTimeCalculator.calculate(List.of(timelineB));

        assertEquals(1, view.cards().size());
        assertNull(view.cards().get(0).cycleTimeDays());
        assertEquals(1, view.excludedCycleTimeCount());
        assertEquals(2L, view.leadTime().p50());
    }

    @Test
    void incompleteCardExcludedFromList() {
        CardTimeline notDone = timeline("Z", "2026-09-01", "2026-09-02", null);

        CycleLeadTimeView view = CycleLeadTimeCalculator.calculate(List.of(notDone));

        assertEquals(0, view.cards().size());
    }

    private static CardTimeline timeline(String title, String createdAt, String startedAt, String doneAt) {
        return new CardTimeline(UUID.randomUUID(), UUID.randomUUID(), title, instantOf(createdAt),
                startedAt == null ? null : instantOf(startedAt), doneAt == null ? null : instantOf(doneAt),
                UUID.randomUUID(), instantOf(createdAt), List.of(), null);
    }

    private static Instant instantOf(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
