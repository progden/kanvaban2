package io.progden.kanban.query.wip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.progden.kanban.query.timeline.CardTimeline;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * {@link AgingCalculator} 純函式單元測試，對應 spec-kanban-widgets.md {@code uc-view-aging-wip}
 * post p1、Scenario「檢視進行中卡片的年齡」。
 */
class AgingCalculatorTest {

    @Test
    void computesAgeFromStartedAtToAsOf() {
        UUID stageId = UUID.randomUUID();
        Instant startedAt = instantOf("2026-09-01");
        Instant asOf = instantOf("2026-09-12");
        CardTimeline timeline = new CardTimeline(UUID.randomUUID(), UUID.randomUUID(), "D", startedAt, startedAt,
                null, stageId, startedAt, List.of(), null);

        List<AgingCardView> result = AgingCalculator.age(List.of(timeline), asOf);

        assertEquals(1, result.size());
        assertEquals(11L, result.get(0).ageDays());
    }

    @Test
    void excludesCardsNotYetStartedOrAlreadyDone() {
        Instant asOf = instantOf("2026-09-12");
        CardTimeline notStarted = new CardTimeline(UUID.randomUUID(), UUID.randomUUID(), "未開始",
                instantOf("2026-09-01"), null, null, UUID.randomUUID(), instantOf("2026-09-01"), List.of(), null);
        CardTimeline done = new CardTimeline(UUID.randomUUID(), UUID.randomUUID(), "已完成",
                instantOf("2026-09-01"), instantOf("2026-09-02"), instantOf("2026-09-05"), UUID.randomUUID(),
                instantOf("2026-09-05"), List.of(), null);

        List<AgingCardView> result = AgingCalculator.age(List.of(notStarted, done), asOf);

        assertTrue(result.isEmpty());
    }

    private static Instant instantOf(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
