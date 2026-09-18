package io.progden.kanban.core.domain;

import java.util.List;
import java.util.UUID;

/**
 * BoardMembership Aggregate Root：對應 spec-user-membership.md 的 {@code board-membership}，
 * 描述某個 {@code userId} 對某個 {@code boardId} 的角色。獨立於 {@code Board}／{@code User} 之外
 * （design-user-membership.md 第 2 節），只透過 id 參照，不持有物件參照。
 *
 * <p>邀請即生效，沒有「待接受」的中間狀態；「只有 Owner 能邀請／變更角色／調整結構／刪除看板」的權限
 * 檢查不在這裡，由呼叫端（application 層）先查詢角色後才呼叫（design-user-membership.md 第 4 節）。
 */
public final class BoardMembership {

    private final UUID id;
    private final UUID boardId;
    private final UUID userId;
    private BoardRole role;

    private BoardMembership(UUID id, UUID boardId, UUID userId, BoardRole role) {
        this.id = id;
        this.boardId = boardId;
        this.userId = userId;
        this.role = role;
    }

    /**
     * 建立新的成員關係；{@code alreadyMember} 由呼叫端查詢後傳入（同 {@link User#create} 的慣例）。
     */
    public static BoardMembership invite(UUID boardId, UUID userId, BoardRole role, boolean alreadyMember) {
        if (alreadyMember) {
            throw new DomainException(ErrorCode.ALREADY_BOARD_MEMBER, "此使用者已經是看板成員");
        }
        return new BoardMembership(UUID.randomUUID(), boardId, userId, role);
    }

    public static BoardMembership reconstruct(UUID id, UUID boardId, UUID userId, BoardRole role) {
        return new BoardMembership(id, boardId, userId, role);
    }

    public void changeRole(BoardRole newRole) {
        this.role = newRole;
    }

    /**
     * 「至少保留一位 Owner」不變條件（design-user-membership.md 第 5 節）：
     * 呼叫端把同一個 Board 目前所有的 membership 傳進來，排除掉 {@code excludingMembershipId}
     * 之後若沒有其他 Owner，就拒絕。
     */
    public static void ensureAnotherOwnerRemains(List<BoardMembership> boardMemberships, UUID excludingMembershipId) {
        boolean anotherOwnerExists = boardMemberships.stream()
                .anyMatch(m -> !m.getId().equals(excludingMembershipId) && m.getRole() == BoardRole.OWNER);
        if (!anotherOwnerExists) {
            throw new DomainException(ErrorCode.MINIMUM_BOARD_OWNER, "看板至少需要保留一位 Owner");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public UUID getUserId() {
        return userId;
    }

    public BoardRole getRole() {
        return role;
    }
}
