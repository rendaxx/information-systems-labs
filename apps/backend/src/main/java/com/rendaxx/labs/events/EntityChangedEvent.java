package com.rendaxx.labs.events;

public record EntityChangedEvent<T>(String destination, Long entityId, T payload, EntityChangeType changeType) {}
