package io.progden.kanban.core.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * BoardClock（看板時鐘）：{@link Board} 的內部狀態（design-board-clock.md 第 1 節），不是獨立
 * Aggregate。取代系統時間（wall clock）作為 Board／Card 事件的「occurredAt」來源
 * （spec-board-clock.md「其他名詞」）。
 *
 * <p>REALTIME 模式下的「現在」＝呼叫端傳入的系統時間（{@code systemNow}）加上一個偏移量
 * （{@code offsetMillis}），PAUSED 模式下固定停在 {@code pausedAt}。
 *
 * <p>{@code adjustTo}／{@code pause}／{@code resume} 本身（調整時鐘）不受單調性限制，可以調回過去
 * （spec-board-clock.md「其他名詞」：單調性只限制「新事件」）；只有 {@link #recordEventTime} 這條
 * 「寫入新事件」路徑會檢查候選時間是否早於 {@code lastEventAt}（design-board-clock.md 第 3 節）。
 *
 * <p>{@code lastSystemNow} 用來夾住系統時鐘取樣的抖動（design-board-clock.md 第 5 節）：虛擬機／
 * NTP 校時環境下 {@code Instant.now()} 兩次取樣間可能回退幾百毫秒，若不夾住會誤觸單調性檢查。
 */
final class BoardClock {

    private static final DateTimeFormatter LAST_EVENT_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private ClockStatus status;
    private long offsetMillis;
    private Instant pausedAt;
    private Instant lastEventAt;
    private Instant lastSystemNow;

    private BoardClock(ClockStatus status, long offsetMillis, Instant pausedAt, Instant lastEventAt,
            Instant lastSystemNow) {
        this.status = status;
        this.offsetMillis = offsetMillis;
        this.pausedAt = pausedAt;
        this.lastEventAt = lastEventAt;
        this.lastSystemNow = lastSystemNow;
    }

    static BoardClock initial() {
        return new BoardClock(ClockStatus.REALTIME, 0L, null, Instant.MIN, null);
    }

    static BoardClock reconstruct(BoardClockSnapshot snapshot) {
        return new BoardClock(snapshot.status(), snapshot.offsetMillis(), snapshot.pausedAt(),
                snapshot.lastEventAt(), snapshot.lastSystemNow());
    }

    BoardClockSnapshot snapshot() {
        return new BoardClockSnapshot(status, offsetMillis, pausedAt, lastEventAt, lastSystemNow);
    }

    ClockStatus getStatus() {
        return status;
    }

    boolean isPaused() {
        return status == ClockStatus.PAUSED;
    }

    /** 唯讀：目前的看板時間，不檢查、不更新單調性基準。 */
    Instant now(Instant systemNow) {
        if (status == ClockStatus.PAUSED) {
            return pausedAt;
        }
        return clampSystemNow(systemNow).plusMillis(offsetMillis);
    }

    /**
     * 寫入新事件用：算出目前看板時間，若早於 {@code lastEventAt} 就拒絕（
     * {@code uc-guard-clock-monotonicity} fail p1），否則把它記為新的 {@code lastEventAt} 並回傳。
     */
    Instant recordEventTime(Instant systemNow) {
        Instant candidate = now(systemNow);
        if (candidate.isBefore(lastEventAt)) {
            throw new DomainException(ErrorCode.BOARD_CLOCK_BEHIND_LAST_EVENT,
                    "看板時間早於最後一筆事件（" + LAST_EVENT_TIME_FORMAT.format(lastEventAt) + "），無法建立新事件");
        }
        lastEventAt = candidate;
        return candidate;
    }

    /** 調整看板時間到指定值；不受單調性限制，PAUSED 時直接改 {@code pausedAt}。 */
    void adjustTo(Instant newTime, Instant systemNow) {
        if (status == ClockStatus.PAUSED) {
            pausedAt = newTime;
            return;
        }
        offsetMillis = Duration.between(clampSystemNow(systemNow), newTime).toMillis();
    }

    /** 暫停：停在目前的看板時間；已是 PAUSED 則不動作。 */
    void pause(Instant systemNow) {
        if (status == ClockStatus.PAUSED) {
            return;
        }
        pausedAt = now(systemNow);
        status = ClockStatus.PAUSED;
    }

    /** 恢復：從 {@code pausedAt} 繼續隨系統時間前進；不是 PAUSED 則不動作（design-board-clock.md 第 6 節）。 */
    void resume(Instant systemNow) {
        if (status != ClockStatus.PAUSED) {
            return;
        }
        offsetMillis = Duration.between(clampSystemNow(systemNow), pausedAt).toMillis();
        status = ClockStatus.REALTIME;
        pausedAt = null;
    }

    private Instant clampSystemNow(Instant systemNow) {
        if (lastSystemNow != null && systemNow.isBefore(lastSystemNow)) {
            return lastSystemNow;
        }
        lastSystemNow = systemNow;
        return systemNow;
    }
}
