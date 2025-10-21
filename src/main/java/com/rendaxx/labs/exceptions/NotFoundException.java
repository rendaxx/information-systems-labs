package com.rendaxx.labs.exceptions;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private Class<?> entityClass;
    private Object id;

    public NotFoundException(Class<?> entityClass, Object id) {
        this.entityClass = entityClass;
        this.id = id;
    }
}
