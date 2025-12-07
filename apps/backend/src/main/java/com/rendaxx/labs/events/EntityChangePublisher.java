package com.rendaxx.labs.events;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityChangePublisher {

    private final ApplicationEventPublisher eventPublisher;

    public <T> void publish(String destination, Long entityId, @Nullable T payload, EntityChangeType changeType) {
        EntityChangedEvent<T> event = new EntityChangedEvent<>(destination, entityId, payload, changeType);
        eventPublisher.publishEvent(event);
    }
}
