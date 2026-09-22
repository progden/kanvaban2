package io.progden.kanban.core.domain;

/**
 * {@code board-membership.role}：某個 {@code user} 對某個 {@code board} 的角色
 * （spec-user-membership.md 欄位定義：Owner、Member、Viewer）。
 */
public enum BoardRole {
    OWNER,
    MEMBER,
    VIEWER
}
