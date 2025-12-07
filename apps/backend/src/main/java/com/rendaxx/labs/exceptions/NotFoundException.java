package com.rendaxx.labs.exceptions;

import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
public class NotFoundException extends BusinessException {
    private final @Nullable Class<?> entityClass;
    private final @Nullable Object id;

    public NotFoundException(@Nullable Class<?> entityClass, @Nullable Object id) {
        super(BusinessErrorCode.NOT_FOUND, buildMessage(entityClass, id));
        this.entityClass = entityClass;
        this.id = id;
    }

    private static String buildMessage(@Nullable Class<?> entityClass, @Nullable Object id) {
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
