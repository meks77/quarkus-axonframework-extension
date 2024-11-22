package at.meks.quarkiverse.axon.runtime;

import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.event.axon.AxonServerEventStore;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBusSpanFactory;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.serialization.json.JacksonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.DefaultBean;
import io.quarkus.logging.Log;

@Dependent
@DefaultBean
class DefaultAxonFrameworkConfigurer implements AxonFrameworkConfigurer {

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    TransactionManager transactionManager;

    @Inject
    TokenStoreConfigurer tokenStoreConfigurer;

    @Inject
    AxonMetricsConfigurer metricsConfigurer;

    @Inject
    Instance<AxonEventProcessingConfigurer> eventProcessingConfigurers;

    private Set<Class<?>> aggregateClasses;
    private Set<Object> eventhandlers;
    private Set<Object> commandhandlers;
    private Set<Object> queryhandlers;

    @Override
    public Configurer configure() {
        final Configurer configurer;
        Log.debug("creating the axon configuration");
        JacksonSerializer jacksonSerializer = JacksonSerializer.builder().objectMapper(objectMapper).build();
        configurer = DefaultConfigurer.defaultConfiguration()
                .registerComponent(AxonServerConfiguration.class,
                        cfg -> axonServerConfiguration())
                .configureEventStore(this::axonserverEventStore)
                .configureSerializer(conf -> jacksonSerializer)
                .configureEventSerializer(confg -> jacksonSerializer);

        aggregateClasses.forEach(configurer::configureAggregate);

        configureEventHandling(configurer);

        commandhandlers.forEach(handler -> configurer.registerCommandHandler(conf -> handler));
        queryhandlers.forEach(handler -> configurer.registerQueryHandler(conf -> handler));

        configureTransactionManagement(configurer);
        metricsConfigurer.configure(configurer);

        return configurer;
    }

    private void configureEventHandling(Configurer configurer) {
        if (!eventhandlers.isEmpty()) {
            if (eventProcessingConfigurers.isUnsatisfied()) {
                throw new IllegalStateException(
                        "no eventProcessingConfigurer found. Either add a eventprocessing extension dependency or provide your own implementation of "
                                + AxonEventProcessingConfigurer.class.getName());
            } else if (eventProcessingConfigurers.isAmbiguous()) {
                throw new IllegalStateException(
                        "multiple eventProcessingConfigurers(" + AxonEventProcessingConfigurer.class.getName() + ") found.");
            }
            tokenStoreConfigurer.configureTokenStore(configurer);
            eventProcessingConfigurers.get().configure(configurer.eventProcessing());

            eventhandlers.forEach(handler -> registerEventHandler(handler, configurer));
        }
    }

    private void configureTransactionManagement(Configurer configurer) {
        configurer.configureTransactionManager(conf -> transactionManager);
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

    private AxonServerConfiguration axonServerConfiguration() {
        return AxonServerConfiguration.builder()
                .servers(axonConfiguration.server().hostname() + ":" + axonConfiguration.server().grpcPort())
                .componentName(axonConfiguration.axonApplicationName())
                .build();
    }

    private EventStore axonserverEventStore(Configuration conf) {
        Log.info("configure connection to axon server");
        return AxonServerEventStore.builder()
                .configuration(axonServerConfiguration())
                .platformConnectionManager(conf.getComponent(AxonServerConnectionManager.class))
                .defaultContext(axonConfiguration.server().context())
                .messageMonitor(conf.messageMonitor(AxonServerEventStore.class, "eventStore"))
                .snapshotSerializer(conf.serializer())
                .eventSerializer(conf.eventSerializer())
                .snapshotFilter(conf.snapshotFilter())
                .upcasterChain(conf.upcasterChain())
                .spanFactory(conf.getComponent(EventBusSpanFactory.class))
                .build();
    }

    private void registerEventHandler(Object handler, Configurer configurer) {
        configurer.eventProcessing().registerEventHandler(conf -> handler);
    }

}
