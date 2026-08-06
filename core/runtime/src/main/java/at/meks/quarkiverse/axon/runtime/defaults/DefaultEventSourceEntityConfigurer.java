package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.Arrays;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;

import at.meks.quarkiverse.axon.annotations.IdType;
import at.meks.quarkiverse.axon.runtime.customizations.EventSourcedEntityConfigurer;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class DefaultEventSourceEntityConfigurer implements EventSourcedEntityConfigurer {

    @Override
    public <T> EventSourcedEntityModule<?, T> createConfigurer(Class<T> eventSourcedEntity) {
        var idTypeAnnotation = Arrays.stream(eventSourcedEntity.getAnnotationsByType(IdType.class)).findFirst();
        Class<?> idClass = String.class;
        if (idTypeAnnotation.isPresent()) {
            idClass = idTypeAnnotation.get().value();
        }
        return EventSourcedEntityModule.autodetected(idClass, eventSourcedEntity);
    }

}
