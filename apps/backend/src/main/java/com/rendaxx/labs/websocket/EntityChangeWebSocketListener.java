package com.rendaxx.labs.websocket;

import com.rendaxx.labs.events.EntityChangePayload;
import com.rendaxx.labs.events.EntityChangedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EntityChangeWebSocketListener {

    private final SimpMessagingTemplate messagingTemplate;

    public EntityChangeWebSocketListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityChanged(EntityChangedEvent<?> event) {
        EntityChangePayload<?> payload =
                new EntityChangePayload<>(event.entityId(), event.payload(), event.changeType());
        messagingTemplate.convertAndSend(event.destination(), payload);
    }
}
