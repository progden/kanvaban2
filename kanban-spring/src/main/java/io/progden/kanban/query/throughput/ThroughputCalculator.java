package io.progden.kanban.query.throughput;

import io.progden.kanban.query.timeline.CardTimeline;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@code uc-view-throughput} 的計算邏輯：純函式，只對 {@code doneAt != null} 的卡片，依
 * {@code doneAt} 對應的日期分組計數（design-kanban-widgets.md 第 6～7 節）。
 *
 * <p>日期分組一律採 {@link ZoneOffset#UTC} 轉換（design-kanban-widgets.md 第 7 節的既有假設，
 * 與 asOf 無關的分組時區，跨環境可重現）；「週」以 ISO 週一為起始日。
 */
public final class ThroughputCalculator {

    private ThroughputCalculator() {
    }

    public static List<ThroughputEntry> countByPeriod(List<CardTimeline> timelines, ThroughputGranularity granularity) {
        Map<LocalDate, Integer> counts = new TreeMap<>();
        for (CardTimeline timeline : timelines) {
            if (timeline.doneAt() == null) {
                continue;
            }
            LocalDate date = timeline.doneAt().atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate period = granularity == ThroughputGranularity.WEEK ? date.with(DayOfWeek.MONDAY) : date;
            counts.merge(period, 1, Integer::sum);
        }
        return counts.entrySet().stream().map(e -> new ThroughputEntry(e.getKey(), e.getValue())).toList();
    }
}
