package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.BoardMembership;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import io.progden.kanban.spring.application.BoardMembershipApplicationService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * spec-user-membership.md「Board 建立與成員邀請」「Board 權限管理」「檢視看板活動紀錄」對應的 web 層
 * （implementation-loop T-04）。操作人身分沿用 {@link UserController} 的登入慣例。
 */
@RestController
@RequestMapping("/api/boards/{boardId}/members")
public class BoardMembershipController {

    private final BoardMembershipApplicationService boardMembershipApplicationService;
    private final UserRepository userRepository;

    public BoardMembershipController(
            BoardMembershipApplicationService boardMembershipApplicationService, UserRepository userRepository) {
        this.boardMembershipApplicationService = boardMembershipApplicationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> listMembers(@PathVariable UUID boardId, HttpSession session) {
        return withOperator(session, operatorId -> {
            List<MemberResponse> members = boardMembershipApplicationService.listMembers(boardId).stream()
                    .map(this::toMemberResponse)
                    .toList();
            return ResponseEntity.ok(members);
        });
    }

    @PostMapping
    public ResponseEntity<?> inviteMember(
            @PathVariable UUID boardId, @RequestBody InviteMemberRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            UUID targetUserId = requireUserId(request.username());
            boardMembershipApplicationService.inviteMember(boardId, operatorId, targetUserId, request.role());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        });
    }

    @PatchMapping("/{username}/role")
    public ResponseEntity<?> changeMemberRole(@PathVariable UUID boardId, @PathVariable String username,
            @RequestBody ChangeMemberRoleRequest request, HttpSession session) {
        return withOperator(session, operatorId -> {
            UUID targetUserId = requireUserId(username);
            boardMembershipApplicationService.changeMemberRole(boardId, operatorId, targetUserId, request.role());
            return ResponseEntity.ok().build();
        });
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> removeMember(@PathVariable UUID boardId, @PathVariable String username,
            @RequestParam(name = "confirmed", defaultValue = "false") boolean confirmed, HttpSession session) {
        return withOperator(session, operatorId -> {
            UUID targetUserId = requireUserId(username);
            boardMembershipApplicationService.removeMember(boardId, operatorId, targetUserId, confirmed);
            return ResponseEntity.noContent().build();
        });
    }

    private MemberResponse toMemberResponse(BoardMembership membership) {
        User user = userRepository.findById(membership.getUserId())
                .orElseThrow(() -> new DomainException(ErrorCode.MEMBERSHIP_NOT_FOUND, "找不到指定的使用者"));
        return MemberResponse.of(user, membership.getRole());
    }

    private UUID requireUserId(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new DomainException(ErrorCode.MEMBERSHIP_NOT_FOUND, "找不到指定的使用者"));
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
            case ALREADY_BOARD_MEMBER, MINIMUM_BOARD_OWNER, CARD_ASSIGNEE_CONFIRMATION_NEEDED -> HttpStatus.CONFLICT;
            case MEMBERSHIP_NOT_FOUND, BOARD_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
