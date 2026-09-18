package io.progden.kanban.query.duedate;

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
 * {@link DueDateReminder} 純函式單元測試，對應 spec-kanban-widgets.md {@code uc-view-duedate-reminder}
 * post p1～p2、「檢視已逾期的卡片」「檢視即將到期的卡片」兩條 Scenario。
 */
class DueDateReminderTest {

    private static final LocalDate AS_OF_DATE = LocalDate.parse("2026-09-12");

    @Test
    void overdueCardBeforeAsOfDateIsListed() {
        CardTimeline cardE = notDoneWithDueDate("E", "2026-09-10");

        List<DueDateCardView> result = DueDateReminder.overdue(List.of(cardE), AS_OF_DATE);

        assertEquals(1, result.size());
        assertEquals("E", result.get(0).title());
    }

    @Test
    void upcomingCardWithinThresholdIsListed() {
        CardTimeline cardF = notDoneWithDueDate("F", "2026-09-14");

        List<DueDateCardView> result = DueDateReminder.upcoming(List.of(cardF), AS_OF_DATE, 3);

        assertEquals(1, result.size());
        assertEquals("F", result.get(0).title());
    }

    @Test
    void completedCardsExcludedFromBothLists() {
        CardTimeline done = new CardTimeline(UUID.randomUUID(), UUID.randomUUID(), "已完成",
                instantOf("2026-09-01"), instantOf("2026-09-02"), instantOf("2026-09-05"), UUID.randomUUID(),
                instantOf("2026-09-05"), List.of(), LocalDate.parse("2026-09-10"));

        assertTrue(DueDateReminder.overdue(List.of(done), AS_OF_DATE).isEmpty());
        assertTrue(DueDateReminder.upcoming(List.of(done), AS_OF_DATE, 30).isEmpty());
    }

    private static CardTimeline notDoneWithDueDate(String title, String dueDate) {
        Instant createdAt = instantOf("2026-09-01");
        return new CardTimeline(UUID.randomUUID(), UUID.randomUUID(), title, createdAt, null, null,
                UUID.randomUUID(), createdAt, List.of(), LocalDate.parse(dueDate));
    }

    private static Instant instantOf(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
