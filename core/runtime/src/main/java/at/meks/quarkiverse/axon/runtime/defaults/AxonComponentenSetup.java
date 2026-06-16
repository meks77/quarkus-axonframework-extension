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
        CommandHandlingModule.CommandHandlerPhase chm = CommandHandlingModule
                .named("command-handler")
                .commandHandlers();
        commandhandlers.forEach(handler -> chm.autodetectedCommandHandlingComponent(config -> handler));
        configurer.registerCommandHandlingModule(chm.build());

        // TODO do we prefer one module with all handlers, or one module per handler?

//        configurer.messaging(mc -> commandhandlers.forEach(handler -> mc.registerCommandHandlingModule(
//                CommandHandlingModule
//                        .named(handler.getClass().getCanonicalName())
//                        .commandHandlers()
//                        .autodetectedCommandHandlingComponent(config -> handler)
//                        .build())));
    }

    void configureQueryHandlers(EventSourcingConfigurer configurer, Set<Object> queryhandlers) {
        var qhm = QueryHandlingModule.named(
                "query-handler").queryHandlers();
        queryhandlers.forEach(handler -> qhm.autodetectedQueryHandlingComponent(config -> handler));
        // TODO moduleName?
        configurer.messaging(mc -> mc.registerQueryHandlingModule(qhm.build()));

    }

    //    void configureEventHandlers(EventSourcingConfigurer configurer, Set<Object> eventhandlers) {
    //        if (!eventhandlers.isEmpty()) {
    //            eventhandlers.forEach(handler -> configurer.eventProcessing().registerEventHandler(conf -> handler));
    //        }
    //    }
}
