package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.Set;

import jakarta.inject.Inject;

import org.axonframework.config.Configurer;
import org.axonframework.config.EventProcessingConfigurer;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.QuarkusAggregateConfigurer;
import at.meks.quarkiverse.axon.runtime.customizations.SagaStoreConfigurer;

public class AxonComponentenSetup {

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    SagaStoreConfigurer sagaStoreConfigurer;

    @Inject
    QuarkusAggregateConfigurer aggregateConfigurer;

    void configureAggregates(Configurer configurer, Set<Class<?>> aggregateClasses) {
        if (axonConfiguration.discovery().aggregates().enabled()) {
            aggregateClasses.forEach(
                    aggregate -> configurer.configureAggregate(aggregateConfigurer.createConfigurer(aggregate)));
        }
    }

    void configureSagas(Configurer configurer, Set<Class<?>> sagaEventhandlerClasses) {
        if (axonConfiguration.discovery().sagaHandlers().enabled()) {
            if (!sagaEventhandlerClasses.isEmpty()) {
                sagaStoreConfigurer.configureSagaStore(configurer);
                EventProcessingConfigurer eventProcessingConfigurer = configurer.eventProcessing();
                sagaEventhandlerClasses.forEach(eventProcessingConfigurer::registerSaga);
            }
        }
    }

    public void configureCommandHandlers(Configurer configurer, Set<Object> commandhandlers) {
        if (axonConfiguration.discovery().commandHandlers().enabled()) {
            commandhandlers.forEach(handler -> configurer.registerCommandHandler(conf -> handler));
        }
    }

    public void configureQueryHandlers(Configurer configurer, Set<Object> queryhandlers) {
        if (axonConfiguration.discovery().queryHandlers().enabled()) {
            queryhandlers.forEach(handler -> configurer.registerQueryHandler(conf -> handler));
        }
    }

    void configureEventHandlers(Configurer configurer, Set<Object> eventhandlers) {
        if (axonConfiguration.discovery().eventHandlers().enabled() && !eventhandlers.isEmpty()) {
            eventhandlers.forEach(handler -> configurer.eventProcessing().registerEventHandler(conf -> handler));
        }
    }
}
