package com.rendaxx.labs.mappers.api.support;

import org.jspecify.annotations.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;

public interface JsonNullableMapper {

    default <T> JsonNullable<T> toNullable(@Nullable T value) {
        if (value == null) {
            return JsonNullable.undefined();
        }
        return JsonNullable.of(value);
    }

    default <T> @Nullable T fromNullable(@Nullable JsonNullable<T> value) {
        if (value == null || !value.isPresent()) {
            return null;
        }
        return value.get();
    }
}
