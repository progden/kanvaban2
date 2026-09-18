package io.progden.kanban.query.timeline;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code uc-view-cycle-lead-time} 的計算邏輯：純函式，輸入為 {@link CardTimeline} 清單
 * （同 {@code FeatureCrBoardCalculator} 的原則）。
 *
 * <p>只列出「已完成」（{@code doneAt != null}）的卡片（post p1）；Lead Time 一律可計算、不排除，
 * Cycle Time 在卡片未曾進入 Start 角色 Stage 時顯示為「無」（{@code cycleTimeDays == null}），
 * 不計入 Cycle Time 的百分位計算（post p2）。百分位數採「nearest-rank」法
 * （{@code rank = ceil(p / 100 * n)}），spec 未指定內插方式，屬低風險技術決定（見 decision-log.md）。
 *
 * <p>天數採「日曆日差」（UTC {@code LocalDate} 相減，與 design-kanban-widgets.md 第 7 節的 UTC
 * 分組假設一致），不是精確 24 小時的倍數；Scenario 只給日期（無時分秒），這樣才能穩定得到
 * Scenario 期待的整數天，不受事件實際發生的秒／毫秒細節影響（低風險技術決定，見 decision-log.md）。
 */
public final class CycleLeadTimeCalculator {

    private CycleLeadTimeCalculator() {
    }

    public static CycleLeadTimeView calculate(List<CardTimeline> timelines) {
        List<CardTiming> cards = new ArrayList<>();
        List<Long> leadDays = new ArrayList<>();
        List<Long> cycleDays = new ArrayList<>();
        int excludedCycleTimeCount = 0;

        for (CardTimeline timeline : timelines) {
            if (timeline.doneAt() == null) {
                continue;
            }
            long lead = daysBetween(timeline.createdAt(), timeline.doneAt());
            Long cycle = timeline.startedAt() == null
                    ? null
                    : daysBetween(timeline.startedAt(), timeline.doneAt());
            cards.add(new CardTiming(timeline.cardId(), timeline.title(), lead, cycle, timeline.doneAt()));
            leadDays.add(lead);
            if (cycle == null) {
                excludedCycleTimeCount++;
            } else {
                cycleDays.add(cycle);
            }
        }

        return new CycleLeadTimeView(cards, percentileSummary(leadDays), percentileSummary(cycleDays),
                excludedCycleTimeCount);
    }

    private static PercentileSummary percentileSummary(List<Long> values) {
        if (values.isEmpty()) {
            return new PercentileSummary(0, 0, 0);
        }
        List<Long> sorted = values.stream().sorted().toList();
        return new PercentileSummary(percentile(sorted, 50), percentile(sorted, 85), percentile(sorted, 95));
    }

    private static long percentile(List<Long> sorted, int p) {
        int n = sorted.size();
        int rank = (int) Math.ceil(p / 100.0 * n);
        int index = Math.min(Math.max(rank - 1, 0), n - 1);
        return sorted.get(index);
    }

    private static long daysBetween(Instant from, Instant to) {
        return ChronoUnit.DAYS.between(from.atZone(ZoneOffset.UTC).toLocalDate(), to.atZone(ZoneOffset.UTC).toLocalDate());
    }
}
