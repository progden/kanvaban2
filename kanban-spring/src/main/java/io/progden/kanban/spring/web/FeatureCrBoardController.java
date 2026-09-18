package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import io.progden.kanban.query.featurecrboard.FeatureCrBoardQueryService;
import io.progden.kanban.query.featurecrboard.FeatureCrBoardView;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * spec-feature-cr-board.md {@code uc-view-feature-cr-board} 對應的 web 層，操作人身分驗證沿用
 * {@link BoardController} 的登入慣例（見該類別註解）；{@code uc-view-feature-cr-board} 無 fail 定義，
 * 只需處理「看板不存在」與「未登入」兩種前置條件。
 */
@RestController
@RequestMapping("/api/boards")
public class FeatureCrBoardController {

    private final FeatureCrBoardQueryService featureCrBoardQueryService;
    private final UserRepository userRepository;

    public FeatureCrBoardController(
            FeatureCrBoardQueryService featureCrBoardQueryService, UserRepository userRepository) {
        this.featureCrBoardQueryService = featureCrBoardQueryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{boardId}/feature-cr-board")
    public ResponseEntity<?> viewFeatureCrBoard(@PathVariable UUID boardId, HttpSession session) {
        Object username = session.getAttribute(UserController.SESSION_USERNAME_ATTRIBUTE);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("尚未登入"));
        }
        User operator = userRepository.findByUsername((String) username).orElse(null);
        if (operator == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("尚未登入"));
        }
        try {
            FeatureCrBoardView view = featureCrBoardQueryService.view(boardId);
            return ResponseEntity.ok(FeatureCrBoardResponse.from(view));
        } catch (DomainException e) {
            HttpStatus status = e.getCode() == ErrorCode.BOARD_NOT_FOUND ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(new ErrorResponse(e.getMessage()));
        }
    }
}
