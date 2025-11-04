package com.rendaxx.labs.mappers.api.support;

import org.openapitools.jackson.nullable.JsonNullable;

public interface JsonNullableMapper {

    default <T> JsonNullable<T> toNullable(T value) {
        if (value == null) {
            return JsonNullable.undefined();
        }
        return JsonNullable.of(value);
    }

    default <T> T fromNullable(JsonNullable<T> value) {
        if (value == null || !value.isPresent()) {
            return null;
        }
        return value.orElse(null);
    }
}
