package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.*;
import java.util.stream.Stream;

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
import org.axonframework.messaging.eventhandling.configuration.EventHandlingComponentsConfigurer;
import org.axonframework.messaging.eventhandling.conversion.DelegatingEventConverter;
import org.axonframework.messaging.eventhandling.conversion.EventConverter;
//import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.conf.SubscribingProcessorConf;
import at.meks.quarkiverse.axon.runtime.customizations.*;
import at.meks.quarkiverse.axon.runtime.defaults.eventprocessors.EventhandlersPerNamespace;
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

    // TODO: Migrate Eventupcasting as soon as it is supported by Axoniq
    //    @Inject
    //    Instance<EventUpcasterChain> eventUpcasterChain;

    @Inject
    InterceptorConfigurer interceptorConfigurer;

    @Inject
    AxonConverterProducer axonConverterProducer;

    @SuppressWarnings("unused")
    @Inject
    Instance<RetryScheduler> retrySchedulerProducer;

    @Inject
    CommandBusConfigurer commandBusConfigurer;

    @Inject
    Instance<AxonTracingConfigurer> axonTracingConfigurer;

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    AxonComponentenSetup axonComponentSetup;

    private Set<Class<?>> eventSourcedEntityClasses;
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
        axonComponentSetup.configureEventSourcedEntities(configurer, eventSourcedEntityClasses);
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
        if (!eventhandlers.isEmpty()) {
            EventhandlersPerNamespace eventhandlersPerNamespace = new EventhandlersPerNamespace(this.eventhandlers);
            List<EventhandlersPerNamespace.NamespaceName> subscribingProcessorNamespaceNames = axonConfiguration
                    .subscribingProcessorConf().namespaces().stream()
                    .flatMap(List::stream)
                    .map(EventhandlersPerNamespace.NamespaceName::new)
                    .toList();

            assignNamespacesToSubscribingEventProcessor(configurer,
                    eventhandlersPerNamespace.getEventhandlers(subscribingProcessorNamespaceNames));

            tokenStoreConfigurer.configureTokenStore(configurer);
            eventProcessingConfigurers.handles().forEach(
                    handle -> handle.get().configure(configurer, eventhandlersForPoolProcessors(
                            eventhandlersPerNamespace, subscribingProcessorNamespaceNames)));
        }
    }

    private static @NonNull Stream<EventhandlersPerNamespace.EventhandlersOfANamespace> eventhandlersForPoolProcessors(
            EventhandlersPerNamespace eventhandlersPerNamespace,
            List<EventhandlersPerNamespace.NamespaceName> subscribingProcessorNamespaceNames) {
        return eventhandlersPerNamespace.stream()
                .filter(namespace -> !subscribingProcessorNamespaceNames.contains(namespace.namespaceName()));
    }

    // TODO: Refactor code into new class in evenprocessor package and optimize code(eg. reduce duplicates, remove visibility of records)
    private void assignNamespacesToSubscribingEventProcessor(EventSourcingConfigurer configurer,
            Stream<EventhandlersPerNamespace.Eventhandler> eventhandlers) {
        SubscribingProcessorConf conf = axonConfiguration.subscribingProcessorConf();
        List<EventhandlersPerNamespace.NamespaceName> namespacenames = conf.namespaces().stream()
                .flatMap(List::stream).map(EventhandlersPerNamespace.NamespaceName::new).toList();
        if (!namespacenames.isEmpty()) {

            configurer.messaging(messagingConfigurer -> messagingConfigurer.eventProcessing(
                    eventProcessingConfigurer -> eventProcessingConfigurer.subscribing(
                            subscribingEventProcessorsConfigurer -> subscribingEventProcessorsConfigurer
                                    .processor(conf.name().orElse("Subscribing"),
                                            config -> config
                                                    .eventHandlingComponents(
                                                            requiredComponentPhase -> configureHandlingComponents(
                                                                    requiredComponentPhase, eventhandlers.toList()))
                                                    .notCustomized()))));
        }
    }

    private EventHandlingComponentsConfigurer.CompletePhase configureHandlingComponents(
            EventHandlingComponentsConfigurer.RequiredComponentPhase requiredComponentPhase,
            Collection<EventhandlersPerNamespace.Eventhandler> eventhandlers) {
        EventHandlingComponentsConfigurer.AdditionalComponentPhase componentPhase = null;
        for (var eventhandler : eventhandlers) {
            componentPhase = requiredComponentPhase.autodetected(eventhandler.name(),
                    config -> eventhandler.instance());
        }

        return componentPhase;
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
        // TODO: Migrate Eventupcasting as soon as it is supported by Axoniq
        //        if (eventUpcasterChain.isResolvable()) {
        //            LOG.info("registering eventUpcasterChain {}", eventUpcasterChain.get().getClass().getName());
        //            configurer.registerEventUpcaster(conf -> eventUpcasterChain.get());
        //        } else if (eventUpcasterChain.isAmbiguous()) {
        //            throw new IllegalStateException(
        //                    "multiple eventUpcasterChain found: %s"
        //                            .formatted(eventUpcasterChain.stream().map(Object::getClass).map(Class::getName).toList()));
        //        } else {
        //            LOG.info("no eventUpcasterChain found");
        //        }
    }

    private void configureTracing(EventSourcingConfigurer configurer) {
        if (axonTracingConfigurer.isResolvable()) {
            axonTracingConfigurer.get().configureTracing(configurer);
        } else {
            LOG.info("Tracing configuration is not available");
        }
    }

    @Override
    public void eventSourcedEntityClasses(Set<Class<?>> eventSourcedEntityClasses) {
        this.eventSourcedEntityClasses = eventSourcedEntityClasses;
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
