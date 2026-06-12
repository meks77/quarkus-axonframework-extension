package at.meks.quarkiverse.axon.it;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.common.configuration.ProcessingGroup;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;

import at.meks.quarkiverse.axon.shared.model.Api;

@ApplicationScoped
@ProcessingGroup("EventProcessorGroup5")
public class EventProcessorGroup5 {

    @EventHandler
    public void on(Api.CardIssuedEvent event) {

    }

}
