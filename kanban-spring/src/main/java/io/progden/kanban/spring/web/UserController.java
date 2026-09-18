package io.progden.kanban.spring.web;

import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.spring.application.UserApplicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 「建立使用者帳號」「使用者登入與登出」對應的 web 層；登入狀態以 {@link HttpSession} 保存 username。
 */
@RestController
@RequestMapping("/api")
public class UserController {

    static final String SESSION_USERNAME_ATTRIBUTE = "username";

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = userApplicationService.createUser(
                    request.username(), request.displayName(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
        } catch (DomainException e) {
            return ResponseEntity.status(statusFor(e.getCode())).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            User user = userApplicationService.login(request.username(), request.password());
            session.setAttribute(SESSION_USERNAME_ATTRIBUTE, user.getUsername());
            return ResponseEntity.ok(UserResponse.from(user));
        } catch (DomainException e) {
            return ResponseEntity.status(statusFor(e.getCode())).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session")
    public ResponseEntity<?> currentSession(HttpSession session) {
        Object username = session.getAttribute(SESSION_USERNAME_ATTRIBUTE);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("尚未登入"));
        }
        return ResponseEntity.ok(new SessionResponse((String) username));
    }

    private HttpStatus statusFor(ErrorCode code) {
        return switch (code) {
            case USERNAME_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case PASSWORD_TOO_LONG -> HttpStatus.BAD_REQUEST;
            case INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
        };
    }
}
