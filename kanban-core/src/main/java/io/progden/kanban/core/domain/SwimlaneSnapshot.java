package io.progden.kanban.core.domain;

import java.util.UUID;

/**
 * {@link Swimlane} 的唯讀快照，供 persistence 層在 {@link Board#reconstruct} 時傳入既有資料。
 */
public record SwimlaneSnapshot(UUID id, String name, int order) {
}
