package io.progden.kanban.query.timeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.progden.kanban.core.domain.StageRole;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * {@link CardTimelineProjector} 純函式單元測試，對應 spec-kanban-widgets.md
 * 「Cycle Time 與 Lead Time 分析」Feature 的三條 Scenario。
 */
class CardTimelineProjectorTest {

    private static final UUID TODO_STAGE = UUID.randomUUID();
    private static final UUID START_STAGE = UUID.randomUUID();
    private static final UUID DONE_STAGE = UUID.randomUUID();

    private static final Map<UUID, StageRole> ROLES =
            Map.of(TODO_STAGE, StageRole.NONE, START_STAGE, StageRole.START, DONE_STAGE, StageRole.DONE);

    @Test
    void completedCardHasLeadAndCycleTime() {
        Instant createdAt = instantOf("2026-09-01");
        Instant startedAt = instantOf("2026-09-02");
        Instant doneAt = instantOf("2026-09-05");
        CardTimelineSource source = new CardTimelineSource(UUID.randomUUID(), UUID.randomUUID(), "A", createdAt,
                null, DONE_STAGE, List.of(
                        new StageTransitionRecord(TODO_STAGE, START_STAGE, startedAt),
                        new StageTransitionRecord(START_STAGE, DONE_STAGE, doneAt)));

        CardTimeline timeline = CardTimelineProjector.project(source, ROLES);

        assertEquals(createdAt, timeline.createdAt());
        assertEquals(startedAt, timeline.startedAt());
        assertEquals(doneAt, timeline.doneAt());
        assertEquals(DONE_STAGE, timeline.currentStageId());
    }

    @Test
    void cardCompletedWithoutEnteringStartHasNoCycleTimeStart() {
        Instant createdAt = instantOf("2026-09-01");
        Instant doneAt = instantOf("2026-09-03");
        CardTimelineSource source = new CardTimelineSource(UUID.randomUUID(), UUID.randomUUID(), "B", createdAt,
                null, DONE_STAGE, List.of(new StageTransitionRecord(TODO_STAGE, DONE_STAGE, doneAt)));

        CardTimeline timeline = CardTimelineProjector.project(source, ROLES);

        assertNull(timeline.startedAt());
        assertEquals(doneAt, timeline.doneAt());
    }

    @Test
    void doneAtOnlyReflectsLastEntryAfterLeavingAndReenteringDone() {
        Instant createdAt = instantOf("2026-09-01");
        Instant firstDoneAt = instantOf("2026-09-05");
        Instant leftDoneAt = instantOf("2026-09-06");
        Instant secondDoneAt = instantOf("2026-09-08");
        CardTimelineSource source = new CardTimelineSource(UUID.randomUUID(), UUID.randomUUID(), "C", createdAt,
                null, DONE_STAGE, List.of(
                        new StageTransitionRecord(TODO_STAGE, DONE_STAGE, firstDoneAt),
                        new StageTransitionRecord(DONE_STAGE, TODO_STAGE, leftDoneAt),
                        new StageTransitionRecord(TODO_STAGE, DONE_STAGE, secondDoneAt)));

        CardTimeline timeline = CardTimelineProjector.project(source, ROLES);

        assertEquals(secondDoneAt, timeline.doneAt());
    }

    private static Instant instantOf(String date) {
        return LocalDate.parse(date).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
    }
}
