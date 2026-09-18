package io.progden.kanban.core.domain;

import java.time.Instant;

/**
 * {@link BoardClock} 的唯讀快照，供 persistence 層在 {@link Board#reconstruct} 時傳入既有資料，
 * 對稱於 {@link SwimlaneSnapshot}／{@link StageSnapshot} 的模式。
 */
public record BoardClockSnapshot(
        ClockStatus status, long offsetMillis, Instant pausedAt, Instant lastEventAt, Instant lastSystemNow) {
}
