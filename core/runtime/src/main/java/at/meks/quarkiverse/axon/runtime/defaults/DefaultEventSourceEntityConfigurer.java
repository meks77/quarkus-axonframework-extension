package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Reserve;

import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;

import at.meks.quarkiverse.axon.runtime.customizations.EventSourcedEntityConfigurer;

@ApplicationScoped
@Reserve
@Priority(0)
public class DefaultEventSourceEntityConfigurer implements EventSourcedEntityConfigurer {

    @Override
    public <T> EventSourcedEntityModule<?, T> createConfigurer(Class<T> eventSourcedEntity, Class<?> idClass) {
        return EventSourcedEntityModule.autodetected(idClass, eventSourcedEntity);
    }

}
