package io.progden.kanban.query.throughput;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/** {@code uc-view-cfd} 的一筆計算結果：某一天、每個 Stage 的累積卡片數量。 */
public record CfdDataPoint(LocalDate date, Map<UUID, Integer> countByStage) {
}
