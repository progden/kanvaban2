package io.progden.kanban.core.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link BoardClock} 不變條件單元測試，對應 spec-board-clock.md 的
 * {@code uc-adjust-board-clock}／{@code uc-guard-clock-monotonicity}／{@code uc-pause-resume-board-clock}。
 */
class BoardClockTest {

    private static final Instant T0 = Instant.parse("2026-09-12T11:00:00Z");

    @Test
    void should_returnAdjustedTime_when_realtimeAndNotAdvanced() {
        BoardClock clock = BoardClock.initial();

        clock.adjustTo(T0, T0);

        assertEquals(T0, clock.now(T0));
    }

    @Test
    void should_advanceWithSystemTime_when_realtime() {
        BoardClock clock = BoardClock.initial();
        clock.adjustTo(T0, T0);

        Instant later = T0.plusSeconds(5);

        assertEquals(T0.plusSeconds(5), clock.now(later));
    }

    @Test
    void should_freezeTime_when_paused() {
        BoardClock clock = BoardClock.initial();
        clock.adjustTo(T0, T0);

        clock.pause(T0);

        assertEquals(T0, clock.now(T0.plusSeconds(10)));
    }

    @Test
    void should_continueFromPausedValue_when_resumed() {
        BoardClock clock = BoardClock.initial();
        clock.adjustTo(T0, T0);
        clock.pause(T0);

        clock.resume(T0.plusSeconds(100));

        assertEquals(T0.plusSeconds(1), clock.now(T0.plusSeconds(101)));
    }

    @Test
    void should_doNothing_when_resumeCalledWhileNotPaused() {
        BoardClock clock = BoardClock.initial();
        clock.adjustTo(T0, T0);

        clock.resume(T0.plusSeconds(50));

        assertEquals(T0.plusSeconds(50), clock.now(T0.plusSeconds(50)));
    }

    @Test
    void should_allowAdjustingBackward_when_adjustClock() {
        BoardClock clock = BoardClock.initial();
        clock.adjustTo(T0.plusSeconds(7200), T0);
        clock.recordEventTime(T0);

        clock.adjustTo(T0.plusSeconds(3600), T0);

        assertEquals(T0.plusSeconds(3600), clock.now(T0));
    }

    @Test
    void should_recordAndAdvanceLastEventAt_when_recordingNewEvent() {
        BoardClock clock = BoardClock.initial();
        clock.adjustTo(T0, T0);

        Instant recorded = clock.recordEventTime(T0);

        assertEquals(T0, recorded);
    }

    @Test
    void should_rejectNewEvent_when_currentTimeBeforeLastEventAt() {
        BoardClock clock = BoardClock.initial();
        clock.adjustTo(T0.plusSeconds(7200), T0);
        clock.recordEventTime(T0);
        clock.adjustTo(T0.plusSeconds(3600), T0);

        DomainException exception = assertThrows(DomainException.class, () -> clock.recordEventTime(T0));

        assertEquals(ErrorCode.BOARD_CLOCK_BEHIND_LAST_EVENT, exception.getCode());
        String expectedTime = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                .format(T0.plusSeconds(7200));
        assertTrue(exception.getMessage().contains(expectedTime));
    }

    @Test
    void should_notMoveLastEventAtBackward_when_adjustingClockBackward() {
        BoardClock clock = BoardClock.initial();
        clock.adjustTo(T0.plusSeconds(7200), T0);
        clock.recordEventTime(T0);
        clock.adjustTo(T0.plusSeconds(3600), T0);

        assertThrows(DomainException.class, () -> clock.recordEventTime(T0));
        BoardClockSnapshot snapshot = clock.snapshot();
        assertEquals(T0.plusSeconds(7200), snapshot.lastEventAt());
    }

    @Test
    void should_clampSystemNow_when_systemClockJitterGoesBackward() {
        BoardClock clock = BoardClock.initial();
        clock.adjustTo(T0, T0);
        Instant sampledAhead = T0.plusSeconds(10);
        clock.now(sampledAhead);

        Instant jitteredBack = T0.plusSeconds(9);

        assertEquals(T0.plusSeconds(10), clock.now(jitteredBack));
    }

    @Test
    void should_roundTripThroughSnapshot_when_reconstructing() {
        BoardClock clock = BoardClock.initial();
        clock.adjustTo(T0, T0);
        clock.pause(T0);

        BoardClock reconstructed = BoardClock.reconstruct(clock.snapshot());

        assertEquals(ClockStatus.PAUSED, reconstructed.getStatus());
        assertEquals(T0, reconstructed.now(T0.plusSeconds(999)));
    }
}
