package io.progden.kanban.spring.application;

import io.progden.kanban.core.domain.BoardMembership;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardDetails;
import io.progden.kanban.core.domain.CardPlacement;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.Comment;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * spec-kanban-basic.md「Card（卡片）編輯」＋ spec-user-membership.md「卡片負責人指派」對應的
 * application 層。
 *
 * <p>{@code uc-add-card} 的 {@code board: R} 只用來確認 {@code board} 存在（新增時需要有效的
 * board 才能承接卡片）；spec 未要求驗證 {@code swimlaneId}／{@code stageId} 是否真的屬於該 board，
 * 本服務也不做這層檢查（低風險技術決定，見 decision-log.md）。
 *
 * <p>負責人相關方法（{@code setAssignees}／{@code dragAssign}／候選名單／依負責人查詢）都先查
 * {@link BoardMembershipApplicationService} 確認候選對象是該看板成員，再解析顯示名字組成活動紀錄
 * 文字後交給 {@link Card#assignTo}（{@code Card} 本身不知道 {@code User} 顯示名字，
 * design-user-membership.md 第 6 點）。
 */
@Service
public class CardApplicationService {

    private final CardRepository cardRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardMembershipApplicationService boardMembershipApplicationService;

    public CardApplicationService(CardRepository cardRepository, BoardRepository boardRepository,
            UserRepository userRepository, BoardMembershipApplicationService boardMembershipApplicationService) {
        this.cardRepository = cardRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.boardMembershipApplicationService = boardMembershipApplicationService;
    }

    public Card addCard(UUID boardId, UUID operatorId, String title, UUID swimlaneId, UUID stageId) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new DomainException(ErrorCode.BOARD_NOT_FOUND, "找不到指定的看板"));
        Card card = Card.create(operatorId, boardId, title, new CardPlacement(swimlaneId, stageId));
        cardRepository.save(card);
        return card;
    }

    public Card getCard(UUID cardId) {
        return loadCard(cardId);
    }

    public Card editCard(UUID cardId, UUID operatorId, String description, LocalDate dueDate, List<String> labels) {
        Card card = loadCard(cardId);
        card.edit(operatorId, new CardDetails(description, dueDate, labels));
        cardRepository.save(card);
        return card;
    }

    public Card moveCardSwimlane(UUID cardId, UUID operatorId, UUID swimlaneId) {
        Card card = loadCard(cardId);
        card.moveToSwimlane(operatorId, swimlaneId);
        cardRepository.save(card);
        return card;
    }

    public Card moveCardStage(UUID cardId, UUID operatorId, UUID stageId) {
        Card card = loadCard(cardId);
        card.moveToStage(operatorId, stageId);
        cardRepository.save(card);
        return card;
    }

    public Comment addComment(UUID cardId, UUID operatorId, String content) {
        Card card = loadCard(cardId);
        Comment comment = card.addComment(operatorId, content);
        cardRepository.save(card);
        return comment;
    }

    public void deleteCard(UUID cardId, UUID operatorId) {
        Card card = loadCard(cardId);
        card.delete(operatorId);
        cardRepository.save(card);
    }

    /**
     * {@code uc-set-card-assignees}：透過編輯畫面把 {@code card.assignees} 整批設定為指定的看板成員集合。
     */
    public Card setAssignees(UUID cardId, UUID operatorId, List<UUID> assigneeIds) {
        Card card = loadCard(cardId);
        ensureAllBoardMembers(card.getBoardId(), assigneeIds);
        applyAssignment(card, operatorId, assigneeIds);
        return card;
    }

    /**
     * {@code uc-assign-card-owner-by-drag}：拖曳單一成員頭像到卡片上，追加為負責人（已存在則不變）。
     */
    public Card dragAssign(UUID cardId, UUID operatorId, UUID memberUserId) {
        Card card = loadCard(cardId);
        ensureAllBoardMembers(card.getBoardId(), List.of(memberUserId));
        List<UUID> newAssigneeIds = new java.util.ArrayList<>(card.getAssigneeIds());
        if (!newAssigneeIds.contains(memberUserId)) {
            newAssigneeIds.add(memberUserId);
        }
        applyAssignment(card, operatorId, newAssigneeIds);
        return card;
    }

    /**
     * {@code uc-list-card-assignee-candidates}：負責人選單只顯示該看板的成員。
     */
    public List<User> listAssigneeCandidates(UUID boardId) {
        return boardMembershipApplicationService.listMembers(boardId).stream()
                .map(BoardMembership::getUserId)
                .map(this::loadUser)
                .toList();
    }

    /**
     * {@code uc-list-cards-by-assignee}：依負責人查詢卡片清單。
     */
    public List<Card> listCardsByAssignee(UUID boardId, UUID assigneeUserId) {
        return cardRepository.findActiveByBoardId(boardId).stream()
                .filter(card -> card.getAssigneeIds().contains(assigneeUserId))
                .toList();
    }

    private void applyAssignment(Card card, UUID operatorId, List<UUID> newAssigneeIds) {
        List<UUID> deduped = new java.util.ArrayList<>(new LinkedHashSet<>(newAssigneeIds));
        List<UUID> before = card.getAssigneeIds();
        if (deduped.equals(before)) {
            cardRepository.save(card);
            return;
        }
        Set<UUID> removedIds = before.stream().filter(id -> !deduped.contains(id)).collect(Collectors.toSet());
        boolean pureRemoval = !removedIds.isEmpty() && deduped.stream().allMatch(before::contains);
        String actionText;
        if (pureRemoval) {
            String removedNames = removedIds.stream().map(this::displayNameOf).collect(Collectors.joining("、"));
            actionText = "將 " + removedNames + " 從卡片負責人中移除";
        } else {
            String allNames = deduped.stream().map(this::displayNameOf).collect(Collectors.joining("、"));
            actionText = "將卡片負責人設定為 " + allNames;
        }
        card.assignTo(operatorId, deduped, actionText);
        cardRepository.save(card);
    }

    private void ensureAllBoardMembers(UUID boardId, List<UUID> userIds) {
        Set<UUID> memberIds = boardMembershipApplicationService.listMembers(boardId).stream()
                .map(BoardMembership::getUserId)
                .collect(Collectors.toSet());
        for (UUID userId : userIds) {
            if (!memberIds.contains(userId)) {
                throw new DomainException(ErrorCode.ASSIGNEE_NOT_BOARD_MEMBER, "負責人必須是該看板的成員");
            }
        }
    }

    private String displayNameOf(UUID userId) {
        return loadUser(userId).getDisplayName();
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ErrorCode.CARD_NOT_FOUND, "找不到指定的使用者"));
    }

    private Card loadCard(UUID cardId) {
        return cardRepository.findById(cardId)
                .filter(card -> !card.isDeleted())
                .orElseThrow(() -> new DomainException(ErrorCode.CARD_NOT_FOUND, "找不到指定的卡片"));
    }
}
