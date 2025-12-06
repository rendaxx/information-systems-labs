package com.rendaxx.labs.exceptions;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {
    private final BusinessErrorCode errorCode;

    protected BusinessException(BusinessErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BusinessException(BusinessErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
