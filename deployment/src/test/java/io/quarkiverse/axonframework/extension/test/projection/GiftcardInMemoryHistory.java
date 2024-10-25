package io.quarkiverse.axonframework.extension.test.projection;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventhandling.EventHandler;

import io.quarkiverse.axonframework.extension.test.model.Api;
import io.quarkus.logging.Log;

@ApplicationScoped
public class GiftcardInMemoryHistory {

    private final List<Object> history = new ArrayList<>();

    @EventHandler
    void handle(Api.CardIssuedEvent event) {
        Log.infof("handling event %s", event);
        history.add(event);
    }

    @EventHandler
    void handle(Api.CardRedeemedEvent event) {
        Log.infof("handling event %s", event);
        history.add(event);
    }

    public boolean wasEventHandled(Object event) {
        return history.contains(event);
    }

}
