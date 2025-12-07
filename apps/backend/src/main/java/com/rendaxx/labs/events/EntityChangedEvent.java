package com.rendaxx.labs.events;

import org.jspecify.annotations.Nullable;

public record EntityChangedEvent<T>(String destination, Long entityId, @Nullable T payload, EntityChangeType changeType) {}
