package io.quarkiverse.axonframework.extension.test;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventhandling.EventHandler;

@ApplicationScoped
public class GiftcardInMemoryHistory {

    private final List<Object> history = new ArrayList<>();

    @EventHandler
    void handle(Api.CardIssuedEvent event) {
        history.add(event);
    }

    @EventHandler
    void handle(Api.CardRedeemedEvent event) {
        history.add(event);
    }

    boolean wasEventHandled(Object event) {
        return history.contains(event);
    }

}
