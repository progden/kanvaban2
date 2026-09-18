package io.progden.kanban.query.throughput;

import java.time.LocalDate;

/** {@code uc-view-throughput} 的一筆計算結果：某個期間（日或週的起始日）完成的卡片數。 */
public record ThroughputEntry(LocalDate periodStart, int count) {
}
