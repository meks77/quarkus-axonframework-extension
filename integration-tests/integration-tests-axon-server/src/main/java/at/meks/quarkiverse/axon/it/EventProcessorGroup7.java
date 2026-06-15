package at.meks.quarkiverse.axon.it;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.core.annotation.Namespace;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;

import at.meks.quarkiverse.axon.shared.model.Api;

@ApplicationScoped
@Namespace("EventProcessorGroup7")
public class EventProcessorGroup7 {

    @EventHandler
    public void on(Api.CardIssuedEvent event) {

    }

}
