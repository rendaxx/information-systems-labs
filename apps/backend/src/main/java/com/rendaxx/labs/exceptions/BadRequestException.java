package com.rendaxx.labs.exceptions;

public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(BusinessErrorCode.BAD_REQUEST, message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(BusinessErrorCode.BAD_REQUEST, message, cause);
    }
}
