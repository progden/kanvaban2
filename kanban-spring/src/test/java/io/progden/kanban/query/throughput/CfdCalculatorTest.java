package io.progden.kanban.query.throughput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.progden.kanban.query.timeline.CardTimeline;
import io.progden.kanban.query.timeline.StageVisit;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * {@link CfdCalculator} 純函式單元測試，對應 spec-kanban-widgets.md {@code uc-view-cfd}
 * Scenario「檢視累積流量圖」：顯示每一天、每個 Stage 的累積卡片數量，卡片離開某 Stage 後
 * 該 Stage 的累積計數仍然包含它（標準 CFD 語意）。
 */
class CfdCalculatorTest {

    @Test
    void cumulativeCountIncludesCardsThatHaveLeftTheStage() {
        UUID todo = UUID.randomUUID();
        UUID inProgress = UUID.randomUUID();
        Instant enteredTodo = instantOf("2026-09-01");
        Instant enteredInProgress = instantOf("2026-09-03");
        CardTimeline timeline = new CardTimeline(UUID.randomUUID(), UUID.randomUUID(), "A", enteredTodo, null, null,
                inProgress, enteredInProgress,
                List.of(new StageVisit(todo, enteredTodo, enteredInProgress),
                        new StageVisit(inProgress, enteredInProgress, null)),
                null);
        Instant asOf = instantOf("2026-09-05");

        List<CfdDataPoint> points = CfdCalculator.cumulativeByStageAndDate(
                List.of(timeline), List.of(todo, inProgress), asOf);

        assertEquals(LocalDate.parse("2026-09-01"), points.get(0).date());
        assertEquals(LocalDate.parse("2026-09-05"), points.get(points.size() - 1).date());
        // 09-01 起，todo 的累積數就包含這張卡片，即使它已經在 09-03 離開 todo。
        assertEquals(1, points.get(0).countByStage().get(todo));
        assertEquals(1, points.get(points.size() - 1).countByStage().get(todo));
        assertEquals(1, points.get(points.size() - 1).countByStage().get(inProgress));
    }

    @Test
    void emptyTimelinesProduceNoDataPoints() {
        Instant asOf = instantOf("2026-09-05");

        List<CfdDataPoint> points = CfdCalculator.cumulativeByStageAndDate(List.of(), List.of(), asOf);

        assertTrue(points.size() >= 1);
    }

    private static Instant instantOf(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
