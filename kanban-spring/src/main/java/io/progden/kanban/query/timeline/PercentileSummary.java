package io.progden.kanban.query.timeline;

/** 固定顯示 P50、P85、P95 三個百分位數（post p3，2026-09-18 補上）。空清單時三者皆為 0。 */
public record PercentileSummary(long p50, long p85, long p95) {
}
