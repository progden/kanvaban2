package io.progden.kanban.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code User} 不變條件單元測試，對應 spec-user-membership.md 「建立使用者帳號」「使用者登入與登出」。
 */
class UserTest {

    @Test
    void should_createUser_when_passwordIsBlank() {
        User user = User.create("user2", null, "", false);

        assertNotNull(user.getId());
        assertTrue(user.authenticate(""));
    }

    @Test
    void should_createUser_when_passwordLengthIsExactly40() {
        String password = "a".repeat(40);

        User user = User.create("user3", null, password, false);

        assertTrue(user.authenticate(password));
    }

    @Test
    void should_rejectCreate_when_passwordLengthExceeds40() {
        String password = "a".repeat(41);

        DomainException exception = assertThrows(DomainException.class,
                () -> User.create("user4", null, password, false));

        assertEquals(ErrorCode.PASSWORD_TOO_LONG, exception.getCode());
        assertEquals("密碼長度不可超過 40 個字", exception.getMessage());
    }

    @Test
    void should_rejectCreate_when_usernameAlreadyTaken() {
        DomainException exception = assertThrows(DomainException.class,
                () -> User.create("user1", null, "password", true));

        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS, exception.getCode());
        assertEquals("此帳號已被使用", exception.getMessage());
    }

    @Test
    void should_defaultDisplayNameToUsername_when_displayNameNotSpecified() {
        User user = User.create("user1", null, "password", false);

        assertEquals("user1", user.getDisplayName());
    }

    @Test
    void should_useGivenDisplayName_when_displayNameSpecified() {
        User user = User.create("user5", "王小明", "password", false);

        assertEquals("王小明", user.getDisplayName());
    }

    @Test
    void should_authenticateSucceed_when_passwordMatches() {
        User user = User.create("user1", null, "correct-password", false);

        assertTrue(user.authenticate("correct-password"));
    }

    @Test
    void should_authenticateFail_when_passwordDoesNotMatch() {
        User user = User.create("user1", null, "correct-password", false);

        assertFalse(user.authenticate("wrong-password"));
    }
}
