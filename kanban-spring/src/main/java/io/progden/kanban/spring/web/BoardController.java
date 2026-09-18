package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import io.progden.kanban.spring.application.BoardApplicationService;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * spec-kanban-basic.md「Swimlane 管理」「Stage（階段）管理」對應的 web 層。
 *
 * <p>操作人身分沿用 {@link UserController} 的登入慣例，從 {@link HttpSession} 取出 username 後
 * 查詢 {@link UserRepository} 換得 {@code User} 的 UUID 作為 operatorId。
 *
 * <p>{@code r-board-owner} 權限檢查（僅 Owner 可調整看板結構）尚未實作，見
 * {@link BoardApplicationService} 類別註解與 implementation-loop OQ-IMPL-15。
 */
@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardApplicationService boardApplicationService;
    private final UserRepository userRepository;

    public BoardController(BoardApplicationService boardApplicationService, UserRepository userRepository) {
        this.boardApplicationService = boardApplicationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createBoard(@RequestBody CreateBoardRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Board board = boardApplicationService.createBoard(operatorId, request.name());
            return ResponseEntity.status(HttpStatus.CREATED).body(BoardResponse.from(board));
        });
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<?> getBoard(@PathVariable UUID boardId, HttpSession session) {
        return withOperator(session, operatorId -> {
            Board board = boardApplicationService.getBoard(boardId);
            return ResponseEntity.ok(BoardResponse.from(board));
        });
    }

    @PostMapping("/{boardId}/swimlanes")
    public ResponseEntity<?> addSwimlane(
            @PathVariable UUID boardId, @RequestBody NameRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Board board = boardApplicationService.addSwimlane(boardId, operatorId, request.name());
            return ResponseEntity.status(HttpStatus.CREATED).body(BoardResponse.from(board));
        });
    }

    @PatchMapping("/{boardId}/swimlanes/{swimlaneId}")
    public ResponseEntity<?> renameSwimlane(@PathVariable UUID boardId, @PathVariable UUID swimlaneId,
            @RequestBody NameRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Board board = boardApplicationService.renameSwimlane(boardId, operatorId, swimlaneId, request.name());
            return ResponseEntity.ok(BoardResponse.from(board));
        });
    }

    @PostMapping("/{boardId}/swimlanes/{swimlaneId}/move")
    public ResponseEntity<?> moveSwimlane(@PathVariable UUID boardId, @PathVariable UUID swimlaneId,
            @RequestBody MoveRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Board board = boardApplicationService.moveSwimlane(boardId, operatorId, swimlaneId, request.beforeId());
            return ResponseEntity.ok(BoardResponse.from(board));
        });
    }

    @DeleteMapping("/{boardId}/swimlanes/{swimlaneId}")
    public ResponseEntity<?> removeSwimlane(@PathVariable UUID boardId, @PathVariable UUID swimlaneId,
            @RequestParam(name = "confirmed", defaultValue = "false") boolean confirmed, HttpSession session) {
        return withOperator(session, operatorId -> {
            boardApplicationService.removeSwimlane(boardId, operatorId, swimlaneId, confirmed);
            return ResponseEntity.noContent().build();
        });
    }

    @PostMapping("/{boardId}/stages")
    public ResponseEntity<?> addStage(
            @PathVariable UUID boardId, @RequestBody AddStageRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Board board = boardApplicationService.addStage(
                    boardId, operatorId, request.name(), request.beforeStageId());
            return ResponseEntity.status(HttpStatus.CREATED).body(BoardResponse.from(board));
        });
    }

    @PatchMapping("/{boardId}/stages/{stageId}")
    public ResponseEntity<?> renameStage(@PathVariable UUID boardId, @PathVariable UUID stageId,
            @RequestBody NameRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Board board = boardApplicationService.renameStage(boardId, operatorId, stageId, request.name());
            return ResponseEntity.ok(BoardResponse.from(board));
        });
    }

    @PostMapping("/{boardId}/stages/{stageId}/move")
    public ResponseEntity<?> moveStage(@PathVariable UUID boardId, @PathVariable UUID stageId,
            @RequestBody MoveRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Board board = boardApplicationService.moveStage(boardId, operatorId, stageId, request.beforeId());
            return ResponseEntity.ok(BoardResponse.from(board));
        });
    }

    @DeleteMapping("/{boardId}/stages/{stageId}")
    public ResponseEntity<?> removeStage(@PathVariable UUID boardId, @PathVariable UUID stageId,
            @RequestParam(name = "destinationStageId", required = false) UUID destinationStageId,
            HttpSession session) {
        return withOperator(session, operatorId -> {
            boardApplicationService.removeStage(boardId, operatorId, stageId, destinationStageId);
            return ResponseEntity.noContent().build();
        });
    }

    @PatchMapping("/{boardId}/stages/{stageId}/role")
    public ResponseEntity<?> setStageRole(@PathVariable UUID boardId, @PathVariable UUID stageId,
            @RequestBody SetStageRoleRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            Board board = boardApplicationService.setStageRole(boardId, operatorId, stageId, request.role());
            return ResponseEntity.ok(BoardResponse.from(board));
        });
    }

    private ResponseEntity<?> withOperator(HttpSession session, java.util.function.Function<UUID, ResponseEntity<?>> action) {
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
            case BOARD_NAME_BLANK, EMPTY_SWIMLANE_NAME, INVALID_DESTINATION_STAGE -> HttpStatus.BAD_REQUEST;
            case BOARD_NOT_FOUND, SWIMLANE_NOT_FOUND, STAGE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case MINIMUM_SWIMLANE, MINIMUM_STAGE, SWIMLANE_HAS_CARDS, STAGE_HAS_CARDS -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
