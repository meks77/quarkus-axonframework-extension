package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.Set;

import jakarta.inject.Inject;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.commandhandling.configuration.CommandHandlingModule;
import org.axonframework.messaging.queryhandling.configuration.QueryHandlingModule;

import at.meks.quarkiverse.axon.runtime.customizations.QuarkusAggregateConfigurer;

public class AxonComponentenSetup {

    @Inject
    QuarkusAggregateConfigurer aggregateConfigurer;

    void configureAggregates(EventSourcingConfigurer configurer, Set<Class<?>> aggregateClasses) {
        aggregateClasses.forEach(
                aggregate -> configurer.modelling(
                        mc -> mc.registerEntity(aggregateConfigurer.createConfigurer(aggregate))));
    }

    void configureCommandHandlers(EventSourcingConfigurer configurer, Set<Object> commandhandlers) {
        configurer.messaging(mc -> commandhandlers.forEach(handler -> mc.registerCommandHandlingModule(
                CommandHandlingModule
                        .named("command-handler")
                        .commandHandlers()
                        .autodetectedCommandHandlingComponent(config -> handler)
                        .build())));
    }

    void configureQueryHandlers(EventSourcingConfigurer configurer, Set<Object> queryhandlers) {
        // TODO moduleName?
        configurer.messaging(mc -> queryhandlers.forEach(handler -> mc.registerQueryHandlingModule(
                QueryHandlingModule.named("query-handler").queryHandlers().autodetectedQueryHandlingComponent(
                        config -> handler).build()
        )));

    }

    //    void configureEventHandlers(EventSourcingConfigurer configurer, Set<Object> eventhandlers) {
    //        if (!eventhandlers.isEmpty()) {
    //            eventhandlers.forEach(handler -> configurer.eventProcessing().registerEventHandler(conf -> handler));
    //        }
    //    }
}
