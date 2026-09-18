package io.progden.kanban.core.domain;

import java.util.UUID;

/**
 * {@link Stage} 的唯讀快照，供 persistence 層在 {@link Board#reconstruct} 時傳入既有資料。
 */
public record StageSnapshot(UUID id, String name, int order, StageRole role) {
}
