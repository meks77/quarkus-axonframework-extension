package at.meks.quarkiverse.axon.it;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;

import at.meks.quarkiverse.axon.shared.model.Api;

@ApplicationScoped
@ProcessingGroup("EventProcessorGroup8")
public class EventProcessorGroup8 {

    @EventHandler
    public void on(Api.CardIssuedEvent event) {

    }

}
