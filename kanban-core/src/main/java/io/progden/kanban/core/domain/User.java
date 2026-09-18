package io.progden.kanban.core.domain;

import java.util.UUID;

/**
 * User Aggregate Root：可登入系統的帳號（對應 spec-user-membership.md `user`）。
 *
 * <p>唯一性檢查（{@code usernameTaken}）由呼叫端（application 層）先查詢 {@link UserRepository}
 * 後傳入，{@code User} 本身不持有其他 {@code User} 的資料。
 */
public final class User {

    private static final int MAX_PASSWORD_LENGTH = 40;

    private final UUID id;
    private final String username;
    private final String displayName;
    private final String password;

    private User(UUID id, String username, String displayName, String password) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.password = password;
    }

    /**
     * 建立新帳號；{@code usernameTaken} 由呼叫端查詢後傳入。
     */
    public static User create(String username, String displayName, String password, boolean usernameTaken) {
        if (username == null || username.isBlank()) {
            throw new DomainException(ErrorCode.USERNAME_BLANK, "使用者名稱不能為空");
        }
        String resolvedPassword = password == null ? "" : password;
        if (resolvedPassword.length() > MAX_PASSWORD_LENGTH) {
            throw new DomainException(ErrorCode.PASSWORD_TOO_LONG, "密碼長度不可超過 40 個字");
        }
        if (usernameTaken) {
            throw new DomainException(ErrorCode.USERNAME_ALREADY_EXISTS, "此帳號已被使用");
        }
        String resolvedDisplayName = (displayName == null || displayName.isBlank()) ? username : displayName;
        return new User(UUID.randomUUID(), username, resolvedDisplayName, resolvedPassword);
    }

    /**
     * 從既有持久化資料重建 {@code User}，不重跑建立時的不變條件檢查（資料已經是合法狀態）。
     */
    public static User reconstruct(UUID id, String username, String displayName, String password) {
        return new User(id, username, displayName, password);
    }

    /**
     * 密碼是否與這個帳號相符；空密碼帳號需以空字串比對才會相符。
     */
    public boolean authenticate(String candidatePassword) {
        String resolvedCandidate = candidatePassword == null ? "" : candidatePassword;
        return password.equals(resolvedCandidate);
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPassword() {
        return password;
    }
}
