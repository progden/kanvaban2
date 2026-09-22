package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import io.progden.kanban.query.workload.MemberWorkloadView;
import io.progden.kanban.query.workload.WorkloadView;
import io.progden.kanban.query.workload.WorkloadQueryService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * spec-workload.md {@code uc-view-workload} 對應的 web 層（角色 {@code r-user}）。
 * {@code uc-drag-assign-card-owner} 與 spec-user-membership.md {@code uc-assign-card-owner-by-drag}
 * 是同一個操作，沿用 {@code CardController} 既有的 {@code POST /api/cards/{cardId}/assignees/drag}
 * 端點（見 spec-workload.md「Use Case 定義」下方說明），這裡不重複實作寫入端點。操作人身分驗證沿用
 * {@link BoardController} 的登入慣例。
 */
@RestController
@RequestMapping("/api/boards/{boardId}/workload")
public class WorkloadController {

    private final WorkloadQueryService workloadQueryService;
    private final UserRepository userRepository;

    public WorkloadController(WorkloadQueryService workloadQueryService, UserRepository userRepository) {
        this.workloadQueryService = workloadQueryService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> viewWorkload(@PathVariable UUID boardId, HttpSession session) {
        return withOperator(session, operatorId ->
                ResponseEntity.ok(toResponse(workloadQueryService.viewWorkload(boardId))));
    }

    private WorkloadResponse toResponse(WorkloadView view) {
        List<MemberWorkloadEntry> members = view.members().stream()
                .map(this::toEntry)
                .toList();
        return new WorkloadResponse(members, view.unassignedCount());
    }

    private MemberWorkloadEntry toEntry(MemberWorkloadView view) {
        User user = userRepository.findById(view.userId())
                .orElseThrow(() -> new IllegalStateException("找不到使用者：" + view.userId()));
        return new MemberWorkloadEntry(view.userId(), user.getUsername(), user.getDisplayName(), view.cardCount());
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
            case BOARD_NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
