package io.progden.kanban.spring.application;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardMembership;
import io.progden.kanban.core.domain.BoardMembershipRepository;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.BoardRole;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import io.progden.kanban.spring.persistence.BoardMembershipActivityJpaEntity;
import io.progden.kanban.spring.persistence.BoardMembershipActivityJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * spec-user-membership.md「Board 建立與成員邀請」「Board 權限管理」「Board 存取權限」對應的
 * application 層。
 *
 * <p>「只有 Owner 能邀請／變更角色／調整看板結構／刪除看板」的權限檢查在這裡（呼叫端）做，
 * {@code Board}／{@code BoardMembership} 本身不驗證（design-user-membership.md 第 4 節）。
 *
 * <p>{@code board-membership} 的活動紀錄（邀請／變更角色／移除成員）不掛在 {@code BoardMembership}
 * 或 {@code Board} 的 {@code activityLog} 上——前者會隨成員被移除而消失，後者會讓
 * {@code uc-invite-member} 等 usecase 的 {@code crud} 多出未宣告的 {@code board: U}；改記在獨立的
 * {@link BoardMembershipActivityJpaEntity} 投影表，由 {@code io.progden.kanban.query} 的
 * {@code BoardActivityLogQueryService} 與 {@code Board} 自己的活動紀錄合併顯示
 * （design-user-membership.md 第 7～8 節）。
 */
@Service
public class BoardMembershipApplicationService {

    private final BoardMembershipRepository boardMembershipRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final BoardMembershipActivityJpaRepository membershipActivityJpaRepository;

    public BoardMembershipApplicationService(BoardMembershipRepository boardMembershipRepository,
            BoardRepository boardRepository, UserRepository userRepository, CardRepository cardRepository,
            BoardMembershipActivityJpaRepository membershipActivityJpaRepository) {
        this.boardMembershipRepository = boardMembershipRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.membershipActivityJpaRepository = membershipActivityJpaRepository;
    }

    /**
     * {@code uc-create-board} post 第 2 條協調用：建立者自動成為 Owner，
     * 呼叫端（{@link BoardApplicationService#createBoard}）要在同一次交易內呼叫（CR-009 交接事項 2）。
     */
    public void createOwnerMembership(UUID boardId, UUID ownerUserId) {
        BoardMembership membership = BoardMembership.invite(boardId, ownerUserId, BoardRole.OWNER, false);
        boardMembershipRepository.save(membership);
    }

    @Transactional
    public void inviteMember(UUID boardId, UUID operatorId, UUID targetUserId, BoardRole role) {
        ensureOwner(boardId, operatorId, "只有 Owner 可以邀請成員");
        boolean alreadyMember = boardMembershipRepository.findByBoardIdAndUserId(boardId, targetUserId).isPresent();
        BoardMembership membership = BoardMembership.invite(boardId, targetUserId, role, alreadyMember);
        boardMembershipRepository.save(membership);
        String targetName = displayNameOf(targetUserId);
        recordMembershipActivity(boardId, operatorId, "邀請 " + targetName + " 加入看板，角色為 " + displayNameOf(role));
    }

    @Transactional
    public void changeMemberRole(UUID boardId, UUID operatorId, UUID targetUserId, BoardRole newRole) {
        ensureOwner(boardId, operatorId, "只有 Owner 可以變更成員角色");
        BoardMembership membership = boardMembershipRepository.findByBoardIdAndUserId(boardId, targetUserId)
                .orElseThrow(() -> new DomainException(ErrorCode.MEMBERSHIP_NOT_FOUND, "找不到指定的看板成員"));
        membership.changeRole(newRole);
        boardMembershipRepository.save(membership);
        String targetName = displayNameOf(targetUserId);
        recordMembershipActivity(boardId, operatorId, "將 " + targetName + " 的角色變更為 " + displayNameOf(newRole));
    }

    @Transactional
    public void removeMember(UUID boardId, UUID operatorId, UUID targetUserId, boolean confirmed) {
        ensureOwner(boardId, operatorId, "只有 Owner 可以變更成員角色");
        BoardMembership membership = boardMembershipRepository.findByBoardIdAndUserId(boardId, targetUserId)
                .orElseThrow(() -> new DomainException(ErrorCode.MEMBERSHIP_NOT_FOUND, "找不到指定的看板成員"));
        List<BoardMembership> allMemberships = boardMembershipRepository.findByBoardId(boardId);
        BoardMembership.ensureAnotherOwnerRemains(allMemberships, membership.getId());

        List<Card> assignedCards = cardRepository.findActiveByBoardId(boardId).stream()
                .filter(card -> card.getAssigneeIds().contains(targetUserId))
                .toList();
        if (!assignedCards.isEmpty() && !confirmed) {
            String targetName = displayNameOf(targetUserId);
            throw new DomainException(ErrorCode.CARD_ASSIGNEE_CONFIRMATION_NEEDED,
                    targetName + " 仍是 " + assignedCards.size() + " 張卡片的負責人，移除後這些卡片會變成未指派");
        }
        for (Card card : assignedCards) {
            card.unassignMember(targetUserId);
            cardRepository.save(card);
        }

        boardMembershipRepository.delete(membership);
        String targetName = displayNameOf(targetUserId);
        recordMembershipActivity(boardId, operatorId, "將 " + targetName + " 移出看板");
    }

    public List<BoardMembership> listMembers(UUID boardId) {
        return boardMembershipRepository.findByBoardId(boardId);
    }

    public List<Board> listBoardsForUser(UUID userId) {
        return boardMembershipRepository.findByUserId(userId).stream()
                .map(membership -> boardRepository.findById(membership.getBoardId()))
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    /**
     * {@code uc-reject-board-access-by-nonmember}／各結構調整 uc 共用的成員資格檢查。
     */
    public void ensureMember(UUID boardId, UUID userId) {
        boardMembershipRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new DomainException(ErrorCode.FORBIDDEN, "你沒有權限存取這個看板"));
    }

    /**
     * {@code uc-add-swimlane}…{@code uc-set-stage-role} 九個結構調整端點、{@code uc-delete-board}
     * 共用的 Owner 檢查（implementation-loop T-02 交接事項 1）。
     */
    public void ensureOwner(UUID boardId, UUID userId, String forbiddenMessage) {
        BoardMembership membership = boardMembershipRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new DomainException(ErrorCode.FORBIDDEN, forbiddenMessage));
        if (membership.getRole() != BoardRole.OWNER) {
            throw new DomainException(ErrorCode.FORBIDDEN, forbiddenMessage);
        }
    }

    private void recordMembershipActivity(UUID boardId, UUID operatorId, String action) {
        membershipActivityJpaRepository.save(
                new BoardMembershipActivityJpaEntity(boardId, operatorId, action, Instant.now()));
    }

    /**
     * 活動紀錄文字要求角色顯示為 spec-user-membership.md 欄位表寫的樣子（Owner／Member／Viewer，
     * 字首大寫），不是 {@link BoardRole} enum 的全大寫名稱（見「邀請 雅婷 加入看板，角色為 Member」
     * 等 Scenario 逐字比對）。
     */
    private String displayNameOf(BoardRole role) {
        String name = role.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private String displayNameOf(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorCode.MEMBERSHIP_NOT_FOUND, "找不到指定的使用者"));
        return user.getDisplayName();
    }
}
