package io.progden.kanban.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Comment（留言，CR-05 補上）：{@code Card} 的內部實體，依附於 {@code Card}，不單獨存在，
 * 只能透過 {@link Card#addComment} 新增（design-kanban-basic.md 第 5 節）。
 */
public final class Comment {

    private final UUID id;
    private final UUID authorId;
    private final String content;
    private final Instant createdAt;

    private Comment(UUID id, UUID authorId, String content, Instant createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
    }

    static Comment create(UUID authorId, String content, Instant now) {
        if (content == null || content.isBlank()) {
            throw new DomainException(ErrorCode.EMPTY_COMMENT_CONTENT, "留言內容不可為空");
        }
        return new Comment(UUID.randomUUID(), authorId, content, now);
    }

    static Comment reconstruct(UUID id, UUID authorId, String content, Instant createdAt) {
        return new Comment(id, authorId, content, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
