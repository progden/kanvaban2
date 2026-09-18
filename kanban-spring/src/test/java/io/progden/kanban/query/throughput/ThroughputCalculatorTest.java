package io.progden.kanban.query.throughput;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.progden.kanban.query.timeline.CardTimeline;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * {@link ThroughputCalculator} 純函式單元測試，對應 spec-kanban-widgets.md {@code uc-view-throughput}
 * Scenario「檢視每日完成卡片數量」。
 */
class ThroughputCalculatorTest {

    @Test
    void countsCompletedCardsByDay() {
        List<CardTimeline> timelines = List.of(
                doneAt("2026-09-10"), doneAt("2026-09-10"), doneAt("2026-09-11"));

        List<ThroughputEntry> result = ThroughputCalculator.countByPeriod(timelines, ThroughputGranularity.DAY);

        assertEquals(2, result.size());
        assertEquals(LocalDate.parse("2026-09-10"), result.get(0).periodStart());
        assertEquals(2, result.get(0).count());
        assertEquals(LocalDate.parse("2026-09-11"), result.get(1).periodStart());
        assertEquals(1, result.get(1).count());
    }

    @Test
    void incompleteCardsExcluded() {
        CardTimeline notDone = new CardTimeline(UUID.randomUUID(), UUID.randomUUID(), "未完成",
                instantOf("2026-09-01"), null, null, UUID.randomUUID(), instantOf("2026-09-01"), List.of(), null);

        List<ThroughputEntry> result = ThroughputCalculator.countByPeriod(List.of(notDone), ThroughputGranularity.DAY);

        assertEquals(0, result.size());
    }

    private static CardTimeline doneAt(String date) {
        Instant doneAt = instantOf(date);
        return new CardTimeline(UUID.randomUUID(), UUID.randomUUID(), "卡片", doneAt, doneAt, doneAt,
                UUID.randomUUID(), doneAt, List.of(), null);
    }

    private static Instant instantOf(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
