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
    INVALID_CREDENTIALS,
    /** 看板名稱留空。 */
    BOARD_NAME_BLANK,
    /** 找不到指定的看板。 */
    BOARD_NOT_FOUND,
    /** Swimlane 名稱留空。 */
    EMPTY_SWIMLANE_NAME,
    /** 找不到指定的 Swimlane。 */
    SWIMLANE_NOT_FOUND,
    /** 看板已經只剩最後一個 Swimlane，不可再刪除。 */
    MINIMUM_SWIMLANE,
    /** 該 Swimlane 內仍有卡片，不可直接刪除。 */
    SWIMLANE_HAS_CARDS,
    /** 找不到指定的 Stage。 */
    STAGE_NOT_FOUND,
    /** 看板已經只剩最後一個 Stage，不可再刪除。 */
    MINIMUM_STAGE,
    /** 該 Stage 內仍有卡片，不可直接刪除。 */
    STAGE_HAS_CARDS,
    /** Card 標題留空。 */
    EMPTY_CARD_TITLE,
    /** 找不到指定的 Card。 */
    CARD_NOT_FOUND,
    /** Comment 內容留空。 */
    EMPTY_COMMENT_CONTENT,
    /** 刪除 Stage 時指定的目的 Stage 與來源相同，或不屬於同一看板。 */
    INVALID_DESTINATION_STAGE,
    /** 該使用者已經是這個看板的成員（uc-invite-member）。 */
    ALREADY_BOARD_MEMBER,
    /** 看板已經只剩最後一位 Owner，不可再移除或降級（uc-remove-member）。 */
    MINIMUM_BOARD_OWNER,
    /** 找不到指定的 BoardMembership。 */
    MEMBERSHIP_NOT_FOUND,
    /** 操作者不是該看板的成員，或角色不足以執行此操作（權限管理／存取權限相關 uc）。 */
    FORBIDDEN,
    /** 移除的成員仍是部分卡片的負責人，需要使用者先確認（uc-remove-member）。 */
    CARD_ASSIGNEE_CONFIRMATION_NEEDED,
    /** 指定的負責人不是該看板的成員（uc-set-card-assignees）。 */
    ASSIGNEE_NOT_BOARD_MEMBER
}
