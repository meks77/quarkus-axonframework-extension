package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;

import at.meks.quarkiverse.axon.runtime.customizations.QuarkusAggregateConfigurer;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class DefaultAggregateConfigurer implements QuarkusAggregateConfigurer {

    @Override
    public <ID, T> EventSourcedEntityModule<ID, T> createConfigurer(Class<T> eventSourcedEntity) {
        // TODO: Fix this by supporting entities with other id types than string
        return EventSourcedEntityModule.autodetected((Class<ID>) String.class, eventSourcedEntity);
    }

}
