package io.progden.kanban.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * {@link Comment} 的唯讀快照，供 persistence 層在 {@link Card#reconstruct} 時傳入既有資料，
 * 不暴露 {@code Comment} 本身的 package-private 建構子。
 */
public record CommentSnapshot(UUID id, UUID authorId, String content, Instant createdAt) {
}
