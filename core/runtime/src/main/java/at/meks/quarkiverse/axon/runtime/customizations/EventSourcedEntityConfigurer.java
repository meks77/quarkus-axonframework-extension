package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;

/**
 * Interface for configuring event sourced entities in a Quarkus-based Axon Framework application.
 * Provides methods to create event sourced entity configurations.
 */
public interface EventSourcedEntityConfigurer {

    /**
     * Creates an {@link EventSourcedEntityModule} for a specific eventSourcedEntity type.
     *
     * @param <T> the type of the eventSourcedEntity
     * @param eventSourcedEntity the class of the eventSourcedEntity type
     * @return an {@link EventSourcedEntityModule} instance for the given eventSourcedEntity type
     */
    <ID, T> EventSourcedEntityModule<ID, T> createConfigurer(Class<T> eventSourcedEntity);
}
