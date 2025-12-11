package com.rendaxx.labs.events;

import org.jspecify.annotations.Nullable;

public record EntityChangePayload<T>(Long id, @Nullable T dto, EntityChangeType changeType) {}
