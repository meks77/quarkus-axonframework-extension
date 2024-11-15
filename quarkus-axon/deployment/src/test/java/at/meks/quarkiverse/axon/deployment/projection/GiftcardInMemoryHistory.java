package at.meks.quarkiverse.axon.deployment.projection;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventhandling.EventHandler;

import at.meks.quarkiverse.axon.deployment.model.Api;
import io.quarkus.logging.Log;

@ApplicationScoped
public class GiftcardInMemoryHistory {

    private final List<Object> history = new ArrayList<>();

    @EventHandler
    void handle(Api.CardIssuedEvent event) {
        Log.debugf("handling event %s", event);
        history.add(event);
    }

    @EventHandler
    void handle(Api.CardRedeemedEvent event) {
        Log.debugf("handling event %s", event);
        history.add(event);
    }

    public boolean wasEventHandled(Object event) {
        return history.contains(event);
    }

}
