package at.meks.quarkiverse.axon.deployment.live.reloading;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.eventhandling.annotation.EventHandler;

import at.meks.quarkiverse.axon.shared.model.Api;

@ApplicationScoped
public class EventHandlerForLiveReloading {

    @EventHandler
    void handle(Api.CardIssuedEvent cardIssuedEvent) {
        //simply do nothing
    }

}
