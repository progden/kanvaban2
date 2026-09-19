package io.progden.kanban.spring.application;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardDetails;
import io.progden.kanban.core.domain.CardPlacement;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.Comment;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * spec-kanban-basic.md「Card（卡片）編輯」對應的 application 層。
 *
 * <p>{@code uc-add-card} 的 {@code board: R} 用來確認 {@code board} 存在（新增時需要有效的
 * board 才能承接卡片），並確認指定的 {@code swimlaneId}／{@code stageId} 真的存在、屬於該 board
 * （CR-011，透過 {@link Board#ensureSwimlaneExists}／{@link Board#ensureStageExists}）；
 * {@code uc-move-card-swimlane}／{@code uc-move-card-stage} 的目的地同樣要通過這層檢查。
 *
 * <p>CR-004（Board Clock）：{@code Card} 沒有自己的時鐘，每個會寫入事件的方法都先載入所屬
 * {@code Board}，透過 {@link Board#newEventTime} 取得看板時間並套用 {@code uc-guard-clock-monotonicity}
 * 的單調性檢查，通過才把這個時間戳記傳進 {@code Card} 的方法。{@code boardRepository.save} 一律排在
 * {@code Card} 的 domain 方法成功之後才呼叫（並整個方法標 {@code @Transactional}），避免 card 因驗證
 * 失敗被拒絕時，board 的 {@code lastEventAt} 基準卻已經前進並存檔，造成後續單調性檢查引用一個不存在的
 * 「最後一筆事件」（D-01）。
 */
@Service
public class CardApplicationService {

    private final CardRepository cardRepository;
    private final BoardRepository boardRepository;

    public CardApplicationService(CardRepository cardRepository, BoardRepository boardRepository) {
        this.cardRepository = cardRepository;
        this.boardRepository = boardRepository;
    }

    @Transactional
    public Card addCard(UUID boardId, UUID operatorId, String title, UUID swimlaneId, UUID stageId) {
        Board board = loadBoard(boardId);
        board.ensureSwimlaneExists(swimlaneId);
        board.ensureStageExists(stageId);
        Instant now = board.newEventTime(Instant.now());
        Card card = Card.create(operatorId, boardId, title, new CardPlacement(swimlaneId, stageId), now);
        boardRepository.save(board);
        cardRepository.save(card);
        return card;
    }

    public Card getCard(UUID cardId) {
        return loadCard(cardId);
    }

    @Transactional
    public Card editCard(UUID cardId, UUID operatorId, String description, LocalDate dueDate, List<String> labels) {
        Card card = loadCard(cardId);
        Board board = loadBoard(card.getBoardId());
        Instant now = board.newEventTime(Instant.now());
        card.edit(operatorId, new CardDetails(description, dueDate, labels), now);
        boardRepository.save(board);
        cardRepository.save(card);
        return card;
    }

    @Transactional
    public Card moveCardSwimlane(UUID cardId, UUID operatorId, UUID swimlaneId) {
        Card card = loadCard(cardId);
        Board board = loadBoard(card.getBoardId());
        board.ensureSwimlaneExists(swimlaneId);
        Instant now = board.newEventTime(Instant.now());
        card.moveToSwimlane(operatorId, swimlaneId, now);
        boardRepository.save(board);
        cardRepository.save(card);
        return card;
    }

    @Transactional
    public Card moveCardStage(UUID cardId, UUID operatorId, UUID stageId) {
        Card card = loadCard(cardId);
        Board board = loadBoard(card.getBoardId());
        board.ensureStageExists(stageId);
        Instant now = board.newEventTime(Instant.now());
        card.moveToStage(operatorId, stageId, now);
        boardRepository.save(board);
        cardRepository.save(card);
        return card;
    }

    @Transactional
    public Comment addComment(UUID cardId, UUID operatorId, String content) {
        Card card = loadCard(cardId);
        Board board = loadBoard(card.getBoardId());
        Instant now = board.newEventTime(Instant.now());
        Comment comment = card.addComment(operatorId, content, now);
        boardRepository.save(board);
        cardRepository.save(card);
        return comment;
    }

    @Transactional
    public void deleteCard(UUID cardId, UUID operatorId) {
        Card card = loadCard(cardId);
        Board board = loadBoard(card.getBoardId());
        Instant now = board.newEventTime(Instant.now());
        card.delete(operatorId, now);
        boardRepository.save(board);
        cardRepository.save(card);
    }

    private Board loadBoard(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new DomainException(ErrorCode.BOARD_NOT_FOUND, "找不到指定的看板"));
    }

    private Card loadCard(UUID cardId) {
        return cardRepository.findById(cardId)
                .filter(card -> !card.isDeleted())
                .orElseThrow(() -> new DomainException(ErrorCode.CARD_NOT_FOUND, "找不到指定的卡片"));
    }
}
