package com.rendaxx.labs.exceptions;

public class InternalServerException extends BusinessException {

    public InternalServerException(String message, Throwable cause) {
        super(BusinessErrorCode.INTERNAL_ERROR, message, cause);
    }

    public InternalServerException(Throwable cause) {
        this(null, cause);
    }
}
