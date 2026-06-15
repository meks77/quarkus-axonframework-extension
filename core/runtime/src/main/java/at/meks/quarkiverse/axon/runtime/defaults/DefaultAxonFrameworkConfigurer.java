package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.conversion.DelegatingGeneralConverter;
import org.axonframework.conversion.GeneralConverter;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.core.conversion.DelegatingMessageConverter;
import org.axonframework.messaging.core.conversion.MessageConverter;
import org.axonframework.messaging.core.retry.RetryScheduler;
import org.axonframework.messaging.core.unitofwork.transaction.TransactionManager;
import org.axonframework.messaging.eventhandling.configuration.EventProcessingConfigurer;
import org.axonframework.messaging.eventhandling.conversion.DelegatingEventConverter;
import org.axonframework.messaging.eventhandling.conversion.EventConverter;
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
    AxonConverterProducer axonConverterProducer;

    @SuppressWarnings("unused")
    @Inject
    Instance<RetryScheduler> retrySchedulerProducer;

    // TODO: Migrate RetrySchedulerConfigurer or remove it, if it is not necessary anymore
    //    @Inject
    //    RetrySchedulerConfigurer retrySchedulerConfigurer;

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

    @Override
    public EventSourcingConfigurer configure() {
        LOG.debug("creating the axon configuration");
        final EventSourcingConfigurer configurer = EventSourcingConfigurer.create()
                .componentRegistry(registry -> {
                    registry.registerComponent(GeneralConverter.class, c -> new DelegatingGeneralConverter(
                            axonConverterProducer.createGeneralConverter()));
                    registry.registerComponent(MessageConverter.class, c -> new DelegatingMessageConverter(
                            axonConverterProducer.createMessageConverter()));
                    registry.registerComponent(EventConverter.class, c -> new DelegatingEventConverter(
                            axonConverterProducer.createEventConverter()));
                });
        configureTracing(configurer);
        eventstoreConfigurer.configure(configurer);
        axonComponentSetup.configureAggregates(configurer, aggregateClasses);
        configureMessageHandler(configurer);
        configureTransactionManagement(configurer);
        metricsConfigurer.configure(configurer);
        configurer.messaging(messagingConfigurer -> interceptorConfigurer.registerInterceptors(messagingConfigurer));
        registerInjectableBeans(configurer);
        registerEventUpcasters(configurer);
        commandBusConfigurer.configureCommandBus(configurer);
        //        configureCommandGateway(configurer);
        return configurer;
    }

    private void configureMessageHandler(EventSourcingConfigurer configurer) {
        configureEventHandling(configurer);

        axonComponentSetup.configureCommandHandlers(configurer, commandhandlers);
        axonComponentSetup.configureQueryHandlers(configurer, queryhandlers);
    }

    private void configureEventHandling(EventSourcingConfigurer configurer) {
        setDefaultEventProcessorType(configurer);
        EventProcessingConfigurer processingConfigurer = configurer.eventProcessing();
        assignProcessingGroupsToSubscribingEventProcessor(processingConfigurer);
        if (!eventhandlers.isEmpty()) {
            tokenStoreConfigurer.configureTokenStore(configurer);
            eventProcessingConfigurers.handles().forEach(
                    handle -> handle.get().configure(processingConfigurer));
            axonComponentSetup.configureEventHandlers(configurer, eventhandlers);
        }
    }

    private void setDefaultEventProcessorType(EventSourcingConfigurer configurer) {
        axonConfiguration.eventProcessing().defaultEventProcessingType().ifPresent(type -> {
            var eventProcessingConfigurer = configurer.eventProcessing();
            switch (type) {
                case SUBSCRIBING -> eventProcessingConfigurer.usingSubscribingEventProcessors();
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

    private void configureTransactionManagement(EventSourcingConfigurer configurer) {
        configurer.componentRegistry(
                reg -> reg.registerComponent(TransactionManager.class, config -> transactionManager));
    }

    private void registerInjectableBeans(EventSourcingConfigurer configurer) {
        for (Map.Entry<Class<?>, Object> entry : injectableBeans.entrySet()) {
            //noinspection unchecked
            configurer.componentRegistry(
                    reg -> reg.registerComponent((Class<Object>) entry.getKey(), config -> entry.getValue()));
        }
    }

    private void registerEventUpcasters(EventSourcingConfigurer configurer) {
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

    //    TODO where to put the retry strategy?
    //    private void configureCommandGateway(EventSourcingConfigurer configurer) {
    //        retrySchedulerConfigurer.retryScheduler()
    //                .ifPresent(retryScheduler -> configurer.messaging( c -> c.componentRegistry(
    //                        reg -> reg.registerComponent(RetryScheduler.class, config -> retryScheduler))));
    //    }

    private void configureTracing(EventSourcingConfigurer configurer) {
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

}
