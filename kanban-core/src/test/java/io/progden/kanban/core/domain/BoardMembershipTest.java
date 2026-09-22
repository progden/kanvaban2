package io.progden.kanban.core.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@code BoardMembership} 不變條件單元測試，對應 spec-user-membership.md
 * 「Board 建立與成員邀請」Feature（design-user-membership.md 第 5 節）。
 */
class BoardMembershipTest {

    private final UUID boardId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void should_createMembership_when_notAlreadyMember() {
        BoardMembership membership = BoardMembership.invite(boardId, userId, BoardRole.MEMBER, false);

        assertEquals(boardId, membership.getBoardId());
        assertEquals(userId, membership.getUserId());
        assertEquals(BoardRole.MEMBER, membership.getRole());
    }

    @Test
    void should_rejectInvite_when_alreadyMember() {
        DomainException exception = assertThrows(DomainException.class,
                () -> BoardMembership.invite(boardId, userId, BoardRole.MEMBER, true));

        assertEquals(ErrorCode.ALREADY_BOARD_MEMBER, exception.getCode());
    }

    @Test
    void should_changeRole_when_changeRoleCalled() {
        BoardMembership membership = BoardMembership.invite(boardId, userId, BoardRole.MEMBER, false);

        membership.changeRole(BoardRole.OWNER);

        assertEquals(BoardRole.OWNER, membership.getRole());
    }

    @Test
    void should_passEnsureAnotherOwnerRemains_when_anotherOwnerExists() {
        BoardMembership owner1 = BoardMembership.invite(boardId, userId, BoardRole.OWNER, false);
        BoardMembership owner2 = BoardMembership.invite(boardId, UUID.randomUUID(), BoardRole.OWNER, false);

        BoardMembership.ensureAnotherOwnerRemains(List.of(owner1, owner2), owner1.getId());
    }

    @Test
    void should_rejectEnsureAnotherOwnerRemains_when_lastOwner() {
        BoardMembership owner = BoardMembership.invite(boardId, userId, BoardRole.OWNER, false);
        BoardMembership member = BoardMembership.invite(boardId, UUID.randomUUID(), BoardRole.MEMBER, false);

        DomainException exception = assertThrows(DomainException.class,
                () -> BoardMembership.ensureAnotherOwnerRemains(List.of(owner, member), owner.getId()));

        assertEquals(ErrorCode.MINIMUM_BOARD_OWNER, exception.getCode());
    }
}
