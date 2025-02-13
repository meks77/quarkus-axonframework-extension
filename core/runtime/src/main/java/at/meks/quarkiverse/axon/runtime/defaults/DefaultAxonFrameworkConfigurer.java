package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.commandhandling.gateway.RetryScheduler;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.serialization.upcasting.event.EventUpcasterChain;

import at.meks.quarkiverse.axon.runtime.customizations.*;
import io.quarkus.arc.DefaultBean;
import io.quarkus.logging.Log;

@Dependent
@DefaultBean
class DefaultAxonFrameworkConfigurer implements AxonFrameworkConfigurer {

    @Inject
    TransactionManager transactionManager;

    @Inject
    TokenStoreConfigurer tokenStoreConfigurer;

    @Inject
    AxonMetricsConfigurer metricsConfigurer;

    @Inject
    EventstoreConfigurer eventstoreConfigurer;

    @Inject
    QuarkusAggregateConfigurer aggregateConfigurer;

    @Inject
    Instance<AxonEventProcessingConfigurer> eventProcessingConfigurers;

    @Inject
    Instance<EventUpcasterChain> eventUpcasterChain;

    @Inject
    InterceptorConfigurer interceptorConfigurer;

    @Inject
    SagaStoreConfigurer sagaStoreConfigurer;

    @Inject
    AxonSerializerProducer axonSerializerProducer;

    @SuppressWarnings("unused")
    @Inject
    Instance<RetryScheduler> retrySchedulerProducer;

    @Inject
    RetrySchedulerConfigurer retrySchedulerConfigurer;

    private Set<Class<?>> aggregateClasses;
    private Set<Object> eventhandlers;
    private Set<Object> commandhandlers;
    private Set<Object> queryhandlers;
    private final Map<Class<?>, Object> injectableBeans = new HashMap<>();
    private final Set<Class<?>> sagaEventhandlerClasses = new HashSet<>();

    @Override
    public Configurer configure() {
        final Configurer configurer;
        Log.debug("creating the axon configuration");
        configurer = DefaultConfigurer.defaultConfiguration()
                .configureSerializer(conf -> axonSerializerProducer.createSerializer())
                .configureEventSerializer(config -> axonSerializerProducer.createEventSerializer())
                .configureMessageSerializer(conf -> axonSerializerProducer.createMessageSerializer());
        eventstoreConfigurer.configure(configurer);
        configureAggregates(configurer);
        configrueMessageHandler(configurer);
        configureTransactionManagement(configurer);
        metricsConfigurer.configure(configurer);
        interceptorConfigurer.registerInterceptors(configurer);
        registerInjectableBeans(configurer);
        registerEventUpcasters(configurer);
        configureSagas(configurer);
        configureCommandGateway(configurer);
        return configurer;
    }

    private void configureCommandGateway(Configurer configurer) {
        retrySchedulerConfigurer.retryScheduler()
                .ifPresent(retryScheduler -> configurer.registerComponent(CommandGateway.class,
                        conf -> createCommandGateway(conf, retryScheduler)));
    }

    private DefaultCommandGateway createCommandGateway(Configuration conf, RetryScheduler retryScheduler) {
        Log.infof("using CommandGateway with retryScheduler %s", retryScheduler.getClass().getName());
        return DefaultCommandGateway.builder()
                .commandBus(conf.commandBus())
                .retryScheduler(retryScheduler)
                .build();
    }

    private void registerEventUpcasters(Configurer configurer) {
        if (eventUpcasterChain.isResolvable()) {
            Log.info("registering eventUpcasterChain " + eventUpcasterChain.get().getClass().getName());
            configurer.registerEventUpcaster(conf -> eventUpcasterChain.get());
        } else if (eventUpcasterChain.isAmbiguous()) {
            throw new IllegalStateException(
                    "multiple eventUpcasterChain found: %s"
                            .formatted(eventUpcasterChain.stream().map(Object::getClass).map(Class::getName).toList()));
        } else {
            Log.info("no eventUpcasterChain found");
        }
    }

    private void configureAggregates(Configurer configurer) {
        aggregateClasses.forEach(
                aggregate -> configurer.configureAggregate(aggregateConfigurer.createConfigurer(aggregate)));
    }

    private void configrueMessageHandler(Configurer configurer) {
        configureEventHandling(configurer);

        commandhandlers.forEach(handler -> configurer.registerCommandHandler(conf -> handler));
        queryhandlers.forEach(handler -> configurer.registerQueryHandler(conf -> handler));
    }

    private void configureEventHandling(Configurer configurer) {
        if (!eventhandlers.isEmpty() || !sagaEventhandlerClasses.isEmpty()) {
            if (eventProcessingConfigurers.isUnsatisfied()) {
                throw new IllegalStateException(
                        "no eventProcessingConfigurer found. Either add a eventprocessing extension dependency or provide your own implementation of "
                                + AxonEventProcessingConfigurer.class.getName());
            } else if (eventProcessingConfigurers.isAmbiguous()) {
                throw new IllegalStateException(
                        "multiple eventProcessingConfigurers(" + AxonEventProcessingConfigurer.class.getName() + ") found.");
            }
            tokenStoreConfigurer.configureTokenStore(configurer);
            eventProcessingConfigurers.get().configure(configurer.eventProcessing(), eventhandlers);
            if (!eventhandlers.isEmpty()) {
                eventhandlers.forEach(handler -> registerEventHandler(handler, configurer));
            }
        }
    }

    private void registerEventHandler(Object handler, Configurer configurer) {
        configurer.eventProcessing().registerEventHandler(conf -> handler);
    }

    private void configureTransactionManagement(Configurer configurer) {
        configurer.configureTransactionManager(conf -> transactionManager);
    }

    private void registerInjectableBeans(Configurer configurer) {
        for (Map.Entry<Class<?>, Object> entry : injectableBeans.entrySet()) {
            //noinspection unchecked
            configurer.registerComponent((Class<Object>) entry.getKey(), configuration -> entry.getValue());
        }
    }

    private void configureSagas(Configurer configurer) {
        if (!sagaEventhandlerClasses.isEmpty()) {
            sagaStoreConfigurer.configureSagaStore(configurer);
            EventProcessingConfigurer eventProcessingConfigurer = configurer.eventProcessing();
            sagaEventhandlerClasses.forEach(eventProcessingConfigurer::registerSaga);
        }
    }

    @Override
    public void aggregateClasses(Set<Class<?>> aggregateClasses) {
        this.aggregateClasses = aggregateClasses;
    }

    @Override
    public void eventhandlers(Set<Object> eventhandlerInstances) {
        this.eventhandlers = eventhandlerInstances;
    }

    @Override
    public void commandhandlers(Set<Object> commandhandlerInstances) {
        this.commandhandlers = commandhandlerInstances;
    }

    @Override
    public void queryhandlers(Set<Object> queryhandlerInstances) {
        this.queryhandlers = queryhandlerInstances;
    }

    @Override
    public void injectableBeans(Map<Class<?>, Object> injectableBeans) {
        this.injectableBeans.putAll(injectableBeans);
    }

    @Override
    public void sagaClasses(Set<Class<?>> sagaEventhandlerClasses) {
        this.sagaEventhandlerClasses.addAll(sagaEventhandlerClasses);
    }

}
