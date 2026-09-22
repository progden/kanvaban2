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
    /** 看板時間早於該 board 最後一筆事件的發生時間，不可建立新事件（uc-guard-clock-monotonicity）。 */
    BOARD_CLOCK_BEHIND_LAST_EVENT,
    /** 只有 Owner 可以調整或暫停／恢復看板時間（uc-adjust-board-clock／uc-pause-resume-board-clock）。 */
    NOT_BOARD_OWNER,
    /** 截止日期提醒的門檻天數不是 1 到 365 之間的正整數（uc-view-duedate-reminder fail p1）。 */
    INVALID_DUEDATE_THRESHOLD_DAYS,
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
    ASSIGNEE_NOT_BOARD_MEMBER,
    /** 找不到指定的畫布（canvas）；正常流程下應已由 uc-init-canvas 建立，屬防呆檢查。 */
    CANVAS_NOT_FOUND,
    /** 找不到指定的畫布元素（item）。 */
    CANVAS_ITEM_NOT_FOUND,
    /** 指定的元素設為不可移動，不可移動或藉調整大小改變位置（uc-move-item／uc-resize-item p4／uc-move-items）。 */
    CANVAS_ITEM_NOT_MOVABLE,
    /** 指定的元素設為不可調整大小（uc-resize-item）。 */
    CANVAS_ITEM_NOT_RESIZABLE,
    /** 指定的元素設為不可移除（uc-remove-item／uc-remove-items）。 */
    CANVAS_ITEM_NOT_REMOVABLE,
    /** 指定的寬或高不大於 0（uc-place-item／uc-resize-item）。 */
    INVALID_ITEM_SIZE,
    /** 指定的錨定方式不是 canvas 或 screen（web 層輸入驗證，非 spec 定義的失敗情境）。 */
    INVALID_ITEM_ANCHOR,
    /** 批次移動指定的元素錨定方式不一致（uc-move-items p2）。 */
    MIXED_ITEM_ANCHOR_IN_BATCH,
    /** 指定的縮放比例超出該 canvas 的縮放範圍（uc-set-viewport）。 */
    VIEWPORT_ZOOM_OUT_OF_RANGE
}
