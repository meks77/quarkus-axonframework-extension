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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.conf.SubscribingProcessorConf;
import at.meks.quarkiverse.axon.runtime.customizations.*;
import io.quarkus.arc.DefaultBean;

@Dependent
@DefaultBean
public class DefaultAxonFrameworkConfigurer implements AxonFrameworkConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAxonFrameworkConfigurer.class);

    @Inject
    TransactionManager transactionManager;

    @Inject
    TokenStoreConfigurer tokenStoreConfigurer;

    @Inject
    AxonMetricsConfigurer metricsConfigurer;

    @Inject
    EventstoreConfigurer eventstoreConfigurer;

    @Inject
    Instance<AxonEventProcessingConfigurer> eventProcessingConfigurers;

    @Inject
    Instance<EventUpcasterChain> eventUpcasterChain;

    @Inject
    InterceptorConfigurer interceptorConfigurer;

    @Inject
    AxonSerializerProducer axonSerializerProducer;

    @SuppressWarnings("unused")
    @Inject
    Instance<RetryScheduler> retrySchedulerProducer;

    @Inject
    RetrySchedulerConfigurer retrySchedulerConfigurer;

    @Inject
    CommandBusConfigurer commandBusConfigurer;

    @Inject
    Instance<AxonTracingConfigurer> axonTracingConfigurer;

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    AxonComponentenSetup axonComponentSetup;

    private Set<Class<?>> aggregateClasses;
    private Set<Object> eventhandlers;
    private Set<Object> commandhandlers;
    private Set<Object> queryhandlers;
    private final Map<Class<?>, Object> injectableBeans = new HashMap<>();
    private final Set<Class<?>> sagaEventhandlerClasses = new HashSet<>();

    @Override
    public Configurer configure() {
        final Configurer configurer;
        LOG.debug("creating the axon configuration");
        configurer = DefaultConfigurer.defaultConfiguration()
                .configureSerializer(conf -> axonSerializerProducer.createSerializer())
                .configureEventSerializer(config -> axonSerializerProducer.createEventSerializer())
                .configureMessageSerializer(conf -> axonSerializerProducer.createMessageSerializer());
        configureTracing(configurer);
        eventstoreConfigurer.configure(configurer);
        axonComponentSetup.configureAggregates(configurer, aggregateClasses);
        configureMessageHandler(configurer);
        configureTransactionManagement(configurer);
        metricsConfigurer.configure(configurer);
        interceptorConfigurer.registerInterceptors(configurer);
        registerInjectableBeans(configurer);
        registerEventUpcasters(configurer);
        axonComponentSetup.configureSagas(configurer, sagaEventhandlerClasses);
        commandBusConfigurer.configureCommandBus(configurer);
        configureCommandGateway(configurer);
        return configurer;
    }

    private void configureMessageHandler(Configurer configurer) {
        configureEventHandling(configurer);

        axonComponentSetup.configureCommandHandlers(configurer, commandhandlers);
        axonComponentSetup.configureQueryHandlers(configurer, queryhandlers);
    }

    private void configureEventHandling(Configurer configurer) {
        setDefaultEventProcessorType(configurer);
        EventProcessingConfigurer processingConfigurer = configurer.eventProcessing();
        assignProcessingGroupsToSubscribingEventProcessor(processingConfigurer);
        if (!eventhandlers.isEmpty() || !sagaEventhandlerClasses.isEmpty()) {
            tokenStoreConfigurer.configureTokenStore(configurer);
            eventProcessingConfigurers.handles().forEach(
                    handle -> handle.get().configure(processingConfigurer));
            axonComponentSetup.configureEventHandlers(configurer, eventhandlers);
        }
    }

    private void setDefaultEventProcessorType(Configurer configurer) {
        axonConfiguration.eventProcessing().defaultEventProcessingType().ifPresent(type -> {
            var eventProcessingConfigurer = configurer.eventProcessing();
            switch (type) {
                case SUBSCRIBING -> eventProcessingConfigurer.usingSubscribingEventProcessors();
                case TRACKING -> eventProcessingConfigurer.usingTrackingEventProcessors();
                case POOLED -> eventProcessingConfigurer.usingPooledStreamingEventProcessors();
            }
        });
    }

    private void assignProcessingGroupsToSubscribingEventProcessor(EventProcessingConfigurer configurer) {
        SubscribingProcessorConf conf = axonConfiguration.subscribingProcessorConf();
        conf.processingGroupNames().ifPresent(groupNames -> {
            String processorName = conf.name().orElse("Subscribing");
            configurer.registerSubscribingEventProcessor(processorName);
            groupNames.stream()
                    .map(String::trim)
                    .forEach(groupName -> configurer.assignProcessingGroup(groupName,
                            processorName));
        });
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

    private void registerEventUpcasters(Configurer configurer) {
        if (eventUpcasterChain.isResolvable()) {
            LOG.info("registering eventUpcasterChain {}", eventUpcasterChain.get().getClass().getName());
            configurer.registerEventUpcaster(conf -> eventUpcasterChain.get());
        } else if (eventUpcasterChain.isAmbiguous()) {
            throw new IllegalStateException(
                    "multiple eventUpcasterChain found: %s"
                            .formatted(eventUpcasterChain.stream().map(Object::getClass).map(Class::getName).toList()));
        } else {
            LOG.info("no eventUpcasterChain found");
        }
    }

    private void configureCommandGateway(Configurer configurer) {
        retrySchedulerConfigurer.retryScheduler()
                .ifPresent(retryScheduler -> configurer.registerComponent(CommandGateway.class,
                        conf -> createCommandGateway(conf, retryScheduler)));
    }

    private DefaultCommandGateway createCommandGateway(Configuration conf, RetryScheduler retryScheduler) {
        LOG.info("using CommandGateway with retryScheduler {}", retryScheduler.getClass().getName());
        return DefaultCommandGateway.builder()
                .commandBus(conf.commandBus())
                .retryScheduler(retryScheduler)
                .build();
    }

    private void configureTracing(Configurer configurer) {
        if (axonTracingConfigurer.isResolvable()) {
            axonTracingConfigurer.get().configureTracing(configurer);
        } else {
            LOG.info("Tracing configuration is not available");
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
