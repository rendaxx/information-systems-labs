package com.rendaxx.labs.exceptions;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final Class<?> entityClass;
    private final Object id;

    public NotFoundException(Class<?> entityClass, Object id) {
        this.entityClass = entityClass;
        this.id = id;
    }
}
