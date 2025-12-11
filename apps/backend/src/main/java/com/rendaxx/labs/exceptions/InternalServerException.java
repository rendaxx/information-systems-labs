package com.rendaxx.labs.exceptions;

import org.jspecify.annotations.Nullable;

public class InternalServerException extends BusinessException {

    public InternalServerException(@Nullable String message, @Nullable Throwable cause) {
        super(BusinessErrorCode.INTERNAL_ERROR, resolveMessage(message), cause);
    }

    public InternalServerException(@Nullable String message) {
        this(resolveMessage(message), null);
    }

    public InternalServerException(@Nullable Throwable cause) {
        this(null, cause);
    }

    private static String resolveMessage(@Nullable String message) {
        if (message == null || message.isBlank()) {
            return "Internal server error";
        }
        return message;
    }
}
