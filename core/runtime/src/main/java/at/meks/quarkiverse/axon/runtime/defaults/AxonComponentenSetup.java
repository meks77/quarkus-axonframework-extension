package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.Set;

import jakarta.inject.Inject;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.commandhandling.configuration.CommandHandlingModule;
import org.axonframework.messaging.queryhandling.configuration.QueryHandlingModule;

import at.meks.quarkiverse.axon.runtime.customizations.EventAggregateConfigurer;

public class AxonComponentenSetup {

    @Inject
    EventAggregateConfigurer aggregateConfigurer;

    void configureAggregates(EventSourcingConfigurer configurer, Set<Class<?>> aggregateClasses) {
        aggregateClasses.forEach(
                aggregate -> configurer.modelling(
                        mc -> mc.registerEntity(aggregateConfigurer.createConfigurer(aggregate))));
    }

    void configureCommandHandlers(EventSourcingConfigurer configurer, Set<Object> commandhandlers) {
        CommandHandlingModule.CommandHandlerPhase chm = CommandHandlingModule
                .named("command-handler")
                .commandHandlers();
        commandhandlers.forEach(handler -> chm.autodetectedCommandHandlingComponent(config -> handler));
        configurer.registerCommandHandlingModule(chm.build());
    }

    void configureQueryHandlers(EventSourcingConfigurer configurer, Set<Object> queryhandlers) {
        var qhm = QueryHandlingModule.named(
                "query-handler").queryHandlers();
        queryhandlers.forEach(handler -> qhm.autodetectedQueryHandlingComponent(config -> handler));
        configurer.messaging(mc -> mc.registerQueryHandlingModule(qhm.build()));
    }

}
