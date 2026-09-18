package io.progden.kanban.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * {@link ActivityRecord} 的唯讀快照，供 persistence 層在 {@link Board#reconstruct} 時傳入既有資料，
 * 不暴露 {@code ActivityRecord} 本身的 package-private 建構子。
 */
public record ActivityRecordSnapshot(UUID id, UUID operatorId, String action, Instant occurredAt) {
}
