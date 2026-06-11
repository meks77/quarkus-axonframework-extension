package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;

import at.meks.quarkiverse.axon.runtime.customizations.EventstoreConfigurer;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class InMemoryEventStoreConfigurer implements EventstoreConfigurer {

    @Override
    public void configure(EventSourcingConfigurer configurer) {
        configurer.registerEventStorageEngine(configuration -> new InMemoryEventStorageEngine());
    }

}
