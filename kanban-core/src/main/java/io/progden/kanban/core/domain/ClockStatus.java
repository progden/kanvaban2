package io.progden.kanban.core.domain;

/**
 * {@code board.clock-status}（spec-board-clock.md 名詞定義）：看板時鐘目前是否隨系統時間前進。
 */
public enum ClockStatus {
    /** 隨系統時間正常前進，可能帶有一個時間偏移量。 */
    REALTIME,
    /** 停在某個固定時間點，不隨系統時間前進。 */
    PAUSED
}
