package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.Set;

import jakarta.inject.Inject;

import org.axonframework.config.Configurer;

import at.meks.quarkiverse.axon.runtime.customizations.QuarkusAggregateConfigurer;

public class AxonComponentenSetup {

    @Inject
    QuarkusAggregateConfigurer aggregateConfigurer;

    void configureAggregates(Configurer configurer, Set<Class<?>> aggregateClasses) {
        aggregateClasses.forEach(
                aggregate -> configurer.configureAggregate(aggregateConfigurer.createConfigurer(aggregate)));
    }

    void configureCommandHandlers(Configurer configurer, Set<Object> commandhandlers) {
        commandhandlers.forEach(handler -> configurer.registerCommandHandler(conf -> handler));
    }

    void configureQueryHandlers(Configurer configurer, Set<Object> queryhandlers) {
        queryhandlers.forEach(handler -> configurer.registerQueryHandler(conf -> handler));
    }

    void configureEventHandlers(Configurer configurer, Set<Object> eventhandlers) {
        if (!eventhandlers.isEmpty()) {
            eventhandlers.forEach(handler -> configurer.eventProcessing().registerEventHandler(conf -> handler));
        }
    }
}
