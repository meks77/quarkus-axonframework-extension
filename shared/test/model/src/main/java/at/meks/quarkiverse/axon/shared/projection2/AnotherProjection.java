package at.meks.quarkiverse.axon.shared.projection2;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.eventhandling.replay.annotation.ResetHandler;

import at.meks.quarkiverse.axon.shared.model.Api;

@ApplicationScoped
public class AnotherProjection {

    private boolean cardIssuedEventWasHandled = false;

    @EventHandler
    void on(Api.CardIssuedEvent event) {
        cardIssuedEventWasHandled = true;
    }

    @ResetHandler
    void reset() {
        cardIssuedEventWasHandled = false;
    }

    public boolean cardIssuedEventWasHandled() {
        return cardIssuedEventWasHandled;
    }

}
