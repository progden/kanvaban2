package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.Comment;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import io.progden.kanban.spring.application.CardApplicationService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * spec-kanban-basic.md「Card（卡片）編輯」＋ spec-user-membership.md「卡片負責人指派」對應的 web 層。
 *
 * <p>建立卡片掛在 {@code /api/boards/{boardId}/cards}（{@code uc-add-card} 的 {@code board: R}
 * 需要先確認 board 存在）；其餘動作 Card 是獨立 Aggregate，直接掛在 {@code /api/cards/{cardId}}
 * （低風險技術決定，見 decision-log.md）。負責人候選名單／依負責人查詢掛在
 * {@code /api/boards/{boardId}/...}（需要 boardId 才能查看板成員／卡片）。
 *
 * <p>操作人身分沿用 {@link UserController} 的登入慣例；新增卡片與設定／拖曳負責人這些寫入動作
 * 會檢查操作者是否為該看板的成員、且不是唯讀的 Viewer（{@link CardApplicationService} 呼叫
 * {@code BoardMembershipApplicationService.ensureCanEdit}），{@code uc-member-add-card} 等 uc
 * 的 {@code fail} 未定義訊息與狀態碼，沿用該方法統一產生的文字（D-01 交接事項，見 OQ）。
 */
@RestController
public class CardController {

    private final CardApplicationService cardApplicationService;
    private final UserRepository userRepository;

    public CardController(CardApplicationService cardApplicationService, UserRepository userRepository) {
        this.cardApplicationService = cardApplicationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/boards/{boardId}/cards")
    public ResponseEntity<?> addCard(
            @PathVariable UUID boardId, @RequestBody CreateCardRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Card card = cardApplicationService.addCard(
                    boardId, operatorId, request.title(), request.swimlaneId(), request.stageId());
            return ResponseEntity.status(HttpStatus.CREATED).body(CardResponse.from(card));
        });
    }

    @GetMapping("/api/cards/{cardId}")
    public ResponseEntity<?> getCard(@PathVariable UUID cardId, HttpSession session) {
        return withOperator(session, operatorId -> {
            Card card = cardApplicationService.getCard(cardId);
            return ResponseEntity.ok(CardResponse.from(card));
        });
    }

    @PatchMapping("/api/cards/{cardId}")
    public ResponseEntity<?> editCard(
            @PathVariable UUID cardId, @RequestBody EditCardRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Card card = cardApplicationService.editCard(
                    cardId, operatorId, request.description(), request.dueDate(), request.labels());
            return ResponseEntity.ok(CardResponse.from(card));
        });
    }

    @PostMapping("/api/cards/{cardId}/move-swimlane")
    public ResponseEntity<?> moveCardSwimlane(
            @PathVariable UUID cardId, @RequestBody MoveCardSwimlaneRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Card card = cardApplicationService.moveCardSwimlane(cardId, operatorId, request.swimlaneId());
            return ResponseEntity.ok(CardResponse.from(card));
        });
    }

    @PostMapping("/api/cards/{cardId}/move-stage")
    public ResponseEntity<?> moveCardStage(
            @PathVariable UUID cardId, @RequestBody MoveCardStageRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Card card = cardApplicationService.moveCardStage(cardId, operatorId, request.stageId());
            return ResponseEntity.ok(CardResponse.from(card));
        });
    }

    @PostMapping("/api/cards/{cardId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable UUID cardId, @RequestBody AddCommentRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Comment comment = cardApplicationService.addComment(cardId, operatorId, request.content());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CardResponse.CommentView(
                            comment.getId(), comment.getAuthorId(), comment.getContent(), comment.getCreatedAt()));
        });
    }

    @DeleteMapping("/api/cards/{cardId}")
    public ResponseEntity<?> deleteCard(@PathVariable UUID cardId, HttpSession session) {
        return withOperator(session, operatorId -> {
            cardApplicationService.deleteCard(cardId, operatorId);
            return ResponseEntity.noContent().build();
        });
    }

    @PatchMapping("/api/cards/{cardId}/assignees")
    public ResponseEntity<?> setAssignees(
            @PathVariable UUID cardId, @RequestBody SetAssigneesRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Card card = cardApplicationService.setAssignees(cardId, operatorId, request.assigneeIds());
            return ResponseEntity.ok(CardResponse.from(card));
        });
    }

    @PostMapping("/api/cards/{cardId}/assignees/drag")
    public ResponseEntity<?> dragAssign(
            @PathVariable UUID cardId, @RequestBody DragAssignRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Card card = cardApplicationService.dragAssign(cardId, operatorId, request.userId());
            return ResponseEntity.ok(CardResponse.from(card));
        });
    }

    @GetMapping("/api/boards/{boardId}/assignee-candidates")
    public ResponseEntity<?> listAssigneeCandidates(@PathVariable UUID boardId, HttpSession session) {
        return withOperator(session, operatorId -> {
            List<AssigneeCandidateResponse> candidates = cardApplicationService
                    .listAssigneeCandidates(boardId, operatorId)
                    .stream()
                    .map(AssigneeCandidateResponse::from)
                    .toList();
            return ResponseEntity.ok(candidates);
        });
    }

    @GetMapping("/api/boards/{boardId}/cards/by-assignee/{assigneeUserId}")
    public ResponseEntity<?> listCardsByAssignee(
            @PathVariable UUID boardId, @PathVariable UUID assigneeUserId, HttpSession session) {
        return withOperator(session, operatorId -> {
            List<CardResponse> cards = cardApplicationService
                    .listCardsByAssignee(boardId, assigneeUserId, operatorId)
                    .stream()
                    .map(CardResponse::from)
                    .toList();
            return ResponseEntity.ok(cards);
        });
    }

    private ResponseEntity<?> withOperator(HttpSession session, Function<UUID, ResponseEntity<?>> action) {
        Object username = session.getAttribute(UserController.SESSION_USERNAME_ATTRIBUTE);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("尚未登入"));
        }
        User operator = userRepository.findByUsername((String) username).orElse(null);
        if (operator == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("尚未登入"));
        }
        try {
            return action.apply(operator.getId());
        } catch (DomainException e) {
            return ResponseEntity.status(statusFor(e.getCode())).body(new ErrorResponse(e.getMessage()));
        }
    }

    private HttpStatus statusFor(ErrorCode code) {
        return switch (code) {
            case EMPTY_CARD_TITLE, EMPTY_COMMENT_CONTENT, ASSIGNEE_NOT_BOARD_MEMBER -> HttpStatus.BAD_REQUEST;
            case CARD_NOT_FOUND, BOARD_NOT_FOUND, SWIMLANE_NOT_FOUND, STAGE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case BOARD_CLOCK_BEHIND_LAST_EVENT -> HttpStatus.CONFLICT;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
