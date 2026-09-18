package io.progden.kanban.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * 一筆活動紀錄（CR-001）：記錄 {@code Board} 的異動事件，含操作人與操作時間。
 * 只能透過 {@link Board} 的方法新增，不單獨存在。
 */
public final class ActivityRecord {

    private final UUID id;
    private final UUID operatorId;
    private final String action;
    private final Instant occurredAt;

    ActivityRecord(UUID operatorId, String action, Instant occurredAt) {
        this(UUID.randomUUID(), operatorId, action, occurredAt);
    }

    private ActivityRecord(UUID id, UUID operatorId, String action, Instant occurredAt) {
        this.id = id;
        this.operatorId = operatorId;
        this.action = action;
        this.occurredAt = occurredAt;
    }

    static ActivityRecord reconstruct(UUID id, UUID operatorId, String action, Instant occurredAt) {
        return new ActivityRecord(id, operatorId, action, occurredAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public String getAction() {
        return action;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
