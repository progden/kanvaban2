package io.progden.kanban.spring.application;

import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 「建立使用者帳號」「使用者登入與登出」對應的 application 層，
 * 負責查詢 {@link UserRepository} 取得 {@code User} 無法自行得知的資料，再呼叫 domain 層方法。
 */
@Service
public class UserApplicationService {

    private final UserRepository userRepository;

    public UserApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String displayName, String password) {
        boolean usernameTaken = userRepository.existsByUsername(username);
        User user = User.create(username, displayName, password, usernameTaken);
        userRepository.save(user);
        return user;
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException(ErrorCode.INVALID_CREDENTIALS, "帳號或密碼錯誤"));
        if (!user.authenticate(password)) {
            throw new DomainException(ErrorCode.INVALID_CREDENTIALS, "帳號或密碼錯誤");
        }
        return user;
    }
}
