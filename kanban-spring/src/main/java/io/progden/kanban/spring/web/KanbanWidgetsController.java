package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import io.progden.kanban.query.throughput.ThroughputGranularity;
import io.progden.kanban.query.widgets.KanbanWidgetsQueryService;
import io.progden.kanban.spring.web.KanbanWidgetsResponses.AgingWipResponse;
import io.progden.kanban.spring.web.KanbanWidgetsResponses.CfdResponse;
import io.progden.kanban.spring.web.KanbanWidgetsResponses.CycleLeadTimeResponse;
import io.progden.kanban.spring.web.KanbanWidgetsResponses.DueDateReminderResponse;
import io.progden.kanban.spring.web.KanbanWidgetsResponses.ThroughputResponse;
import io.progden.kanban.spring.web.KanbanWidgetsResponses.WipResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * spec-kanban-widgets.md 六個 {@code uc-view-*}（角色皆 {@code r-board-member}）對應的 web 層。
 * 操作人身分驗證沿用 {@link BoardController} 的登入慣例；{@code r-board-member} 是否為看板成員
 * 尚未檢查，F02 {@code BoardMembership} 對讀取類 API 的檢查機制目前整個專案皆未實作
 * （同 {@link FeatureCrBoardController} 的既有落差，非本任務新增缺口）。
 */
@RestController
@RequestMapping("/api/boards/{boardId}/widgets")
public class KanbanWidgetsController {

    private final KanbanWidgetsQueryService kanbanWidgetsQueryService;
    private final UserRepository userRepository;

    public KanbanWidgetsController(KanbanWidgetsQueryService kanbanWidgetsQueryService, UserRepository userRepository) {
        this.kanbanWidgetsQueryService = kanbanWidgetsQueryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/cycle-lead-time")
    public ResponseEntity<?> viewCycleLeadTime(@PathVariable UUID boardId, HttpSession session) {
        return withOperator(session, operatorId -> ResponseEntity.ok(
                CycleLeadTimeResponse.from(kanbanWidgetsQueryService.viewCycleLeadTime(boardId))));
    }

    @GetMapping("/wip")
    public ResponseEntity<?> viewWip(@PathVariable UUID boardId, HttpSession session) {
        return withOperator(session, operatorId -> ResponseEntity.ok(
                WipResponse.from(kanbanWidgetsQueryService.viewWip(boardId))));
    }

    @GetMapping("/aging-wip")
    public ResponseEntity<?> viewAgingWip(@PathVariable UUID boardId, HttpSession session) {
        return withOperator(session, operatorId -> ResponseEntity.ok(
                AgingWipResponse.from(kanbanWidgetsQueryService.viewAgingWip(boardId))));
    }

    @GetMapping("/throughput")
    public ResponseEntity<?> viewThroughput(@PathVariable UUID boardId,
            @RequestParam(name = "unit", defaultValue = "day") String unit, HttpSession session) {
        return withOperator(session, operatorId -> {
            ThroughputGranularity granularity;
            try {
                granularity = ThroughputGranularity.valueOf(unit.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("unit 必須是 day 或 week"));
            }
            return ResponseEntity.ok(
                    ThroughputResponse.from(kanbanWidgetsQueryService.viewThroughput(boardId, granularity)));
        });
    }

    @GetMapping("/cfd")
    public ResponseEntity<?> viewCfd(@PathVariable UUID boardId, HttpSession session) {
        return withOperator(session, operatorId -> ResponseEntity.ok(
                CfdResponse.from(kanbanWidgetsQueryService.viewCfd(boardId))));
    }

    @GetMapping("/duedate-reminder")
    public ResponseEntity<?> viewDueDateReminder(@PathVariable UUID boardId,
            @RequestParam(name = "thresholdDays", required = false) Integer thresholdDays, HttpSession session) {
        return withOperator(session, operatorId -> ResponseEntity.ok(DueDateReminderResponse.from(
                kanbanWidgetsQueryService.viewDueDateReminder(boardId, thresholdDays))));
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
            case INVALID_DUEDATE_THRESHOLD_DAYS -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
