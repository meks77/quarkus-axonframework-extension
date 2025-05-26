package at.meks.quarkiverse.axon.shared.projection;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventhandling.*;

import at.meks.quarkiverse.axon.shared.model.Api;
import io.quarkus.logging.Log;

@ApplicationScoped
public class GiftcardInMemoryHistory {

    private final List<Object> history = new ArrayList<>();
    private boolean cardIssuedEventWasHandled = false;

    @EventHandler
    void handle(Api.CardIssuedEvent event, @SequenceNumber long sequenceNumber) {
        Log.debugf("handling event %s", event);
        cardIssuedEventWasHandled = true;
        history.add(event);
    }

    @EventHandler
    void handle(Api.CardIssuedEvent event, TrackingToken trackingToken, @SequenceNumber Long sequenceNumber,
            ReplayStatus replayStatus) {
        Log.debugf("handling event %s", event);
        cardIssuedEventWasHandled = true;
        history.add(event);
    }

    @EventHandler
    void handle(Api.CardRedeemedEvent event) {
        Log.debugf("handling event %s", event);
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
