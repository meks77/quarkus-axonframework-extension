package at.meks.quarkiverse.axon.it;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;

import at.meks.quarkiverse.axon.shared.model.Api;

@ApplicationScoped
@ProcessingGroup("EventProcessorGroup6")
public class EventProcessorGroup6 {

    @EventHandler
    public void on(Api.CardIssuedEvent event) {

    }

}
