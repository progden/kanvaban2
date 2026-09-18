package io.progden.kanban.core.domain;

/**
 * 領域不變條件被違反時拋出的單一例外類別，呼叫端依 {@link #getCode()} 分支處理。
 */
public class DomainException extends RuntimeException {

    private final ErrorCode code;

    public DomainException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
