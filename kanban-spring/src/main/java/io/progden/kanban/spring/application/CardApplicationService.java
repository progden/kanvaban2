package io.progden.kanban.spring.application;

import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardDetails;
import io.progden.kanban.core.domain.CardPlacement;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.Comment;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * spec-kanban-basic.md「Card（卡片）編輯」對應的 application 層。
 *
 * <p>{@code uc-add-card} 的 {@code board: R} 只用來確認 {@code board} 存在（新增時需要有效的
 * board 才能承接卡片）；spec 未要求驗證 {@code swimlaneId}／{@code stageId} 是否真的屬於該 board，
 * 本服務也不做這層檢查（低風險技術決定，見 decision-log.md）。
 */
@Service
public class CardApplicationService {

    private final CardRepository cardRepository;
    private final BoardRepository boardRepository;

    public CardApplicationService(CardRepository cardRepository, BoardRepository boardRepository) {
        this.cardRepository = cardRepository;
        this.boardRepository = boardRepository;
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

    private Card loadCard(UUID cardId) {
        return cardRepository.findById(cardId)
                .filter(card -> !card.isDeleted())
                .orElseThrow(() -> new DomainException(ErrorCode.CARD_NOT_FOUND, "找不到指定的卡片"));
    }
}
