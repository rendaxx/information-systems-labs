package com.rendaxx.labs.exceptions;

import lombok.Getter;

@Getter
public class NotFoundException extends BusinessException {
    private final Class<?> entityClass;
    private final Object id;

    public NotFoundException(Class<?> entityClass, Object id) {
        super(BusinessErrorCode.NOT_FOUND, buildMessage(entityClass, id));
        this.entityClass = entityClass;
        this.id = id;
    }

    private static String buildMessage(Class<?> entityClass, Object id) {
        if (entityClass == null) {
            return "Resource not found";
        }
        String entityName = entityClass.getSimpleName();
        if (id == null) {
            return String.format("%s was not found", entityName);
        }
        return String.format("%s with id '%s' was not found", entityName, id);
    }
}
