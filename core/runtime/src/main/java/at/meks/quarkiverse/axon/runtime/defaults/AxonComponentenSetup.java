package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.Set;

import jakarta.inject.Inject;

import org.axonframework.config.Configurer;

import at.meks.quarkiverse.axon.runtime.customizations.QuarkusAggregateConfigurer;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

public class AxonComponentenSetup {

    @Inject
    QuarkusAggregateConfigurer aggregateConfigurer;

    void configureAggregates(EventSourcingConfigurer configurer, Set<Class<?>> aggregateClasses) {
        aggregateClasses.forEach(
                aggregate -> configurer.configureAggregate(aggregateConfigurer.createConfigurer(aggregate)));
    }

    void configureCommandHandlers(EventSourcingConfigurer configurer, Set<Object> commandhandlers) {
        commandhandlers.forEach(handler -> configurer.registerCommandHandler(conf -> handler));
    }

    void configureQueryHandlers(EventSourcingConfigurer configurer, Set<Object> queryhandlers) {
        queryhandlers.forEach(handler -> configurer.registerQueryHandler(conf -> handler));
    }

    void configureEventHandlers(EventSourcingConfigurer configurer, Set<Object> eventhandlers) {
        if (!eventhandlers.isEmpty()) {
            eventhandlers.forEach(handler -> configurer.eventProcessing().registerEventHandler(conf -> handler));
        }
    }
}
