package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;

import at.meks.quarkiverse.axon.runtime.customizations.EventSourcedEntityConfigurer;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class DefaultEventSourceEntityConfigurer implements EventSourcedEntityConfigurer {

    @Override
    public <T> EventSourcedEntityModule<?, T> createConfigurer(Class<T> eventSourcedEntity, Class<?> idClass) {
        return EventSourcedEntityModule.autodetected(idClass, eventSourcedEntity);
    }

}
