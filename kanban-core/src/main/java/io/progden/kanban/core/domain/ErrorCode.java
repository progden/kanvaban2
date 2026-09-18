package io.progden.kanban.core.domain;

/**
 * {@link DomainException} 攜帶的錯誤情境代碼。
 */
public enum ErrorCode {
    /** 帳號 ID（username）已被使用。 */
    USERNAME_ALREADY_EXISTS,
    /** 帳號 ID（username）留空（CR-006）。 */
    USERNAME_BLANK,
    /** 密碼長度超過上限。 */
    PASSWORD_TOO_LONG,
    /** 登入帳號或密碼不正確。 */
    INVALID_CREDENTIALS
}
