package com.rendaxx.labs.events;

public record EntityChangePayload<T>(
    Long id,
    T dto,
    EntityChangeType changeType
) {
}
