package at.meks.quarkiverse.axon.shared.projection;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.core.annotation.Namespace;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.eventhandling.processing.streaming.token.TrackingToken;
import org.axonframework.messaging.eventhandling.replay.ReplayStatus;
import org.axonframework.messaging.eventhandling.replay.annotation.ResetHandler;

import at.meks.quarkiverse.axon.shared.model.Api;
import io.quarkus.logging.Log;

@ApplicationScoped
@Namespace("GiftCardInMemory")
public class GiftcardInMemoryHistory {

    private final List<Object> history = new ArrayList<>();
    private boolean cardIssuedEventWasHandled = false;

    @EventHandler
    void handle(Api.CardIssuedEvent event) {
        Log.infof("handling event %s", event);
        cardIssuedEventWasHandled = true;
        history.add(event);
    }

    @EventHandler
    void handle(Api.CardIssuedEvent event, TrackingToken trackingToken,
            ReplayStatus replayStatus) {
        Log.infof("handling event %s", event);
        cardIssuedEventWasHandled = true;
        history.add(event);
    }

    @EventHandler
    void handle(Api.CardRedeemedEvent event) {
        Log.infof("handling event %s", event);
        history.add(event);
    }

    @ResetHandler
    void reset() {
        history.clear();
        cardIssuedEventWasHandled = false;
    }

    public boolean wasEventHandled(Object event) {
        return history.contains(event);
    }

    public boolean cardIssuedEventWasHandled() {
        return cardIssuedEventWasHandled;
    }
}
