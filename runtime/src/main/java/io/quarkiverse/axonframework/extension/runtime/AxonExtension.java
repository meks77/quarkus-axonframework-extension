package io.quarkiverse.axonframework.extension.runtime;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.event.axon.AxonServerEventStore;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventBusSpanFactory;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.modelling.command.Repository;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.serialization.json.JacksonSerializer;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;

@Singleton
public class AxonExtension {

    @Inject
    AxonConfiguration axonConfiguration;
    private Configuration configuration;
    private final Set<Class<?>> aggregateClasses = new HashSet<>();
    private final Set<Object> evenhandlers = new HashSet<>();
    private final Set<Object> commandhandlers = new HashSet<>();
    private final Set<Object> queryHandlers = new HashSet<>();

    public AxonExtension() {
    }

    @Startup
    void init() {
        if (configuration == null) {
            final Configurer configurer;
            Log.debug("creating the axon configuration");
            configurer = DefaultConfigurer.defaultConfiguration()
                    .registerComponent(AxonServerConfiguration.class,
                            cfg -> axonServerConfiguration())
                    .configureEventStore(this::axonserverEventStore)
                    .configureSerializer(conf -> JacksonSerializer.defaultSerializer())
                    .configureEventSerializer(confg -> JacksonSerializer.defaultSerializer());
            aggregateClasses.forEach(configurer::configureAggregate);
            evenhandlers.forEach(handler -> registerEventHandler(handler, configurer));
            commandhandlers.forEach(handler -> configurer.registerCommandHandler(conf -> handler));
            queryHandlers.forEach(handler -> configurer.registerQueryHandler(conf -> handler));
            Log.info("starting axon");
            configuration = configurer.start();
        }
    }

    private AxonServerConfiguration axonServerConfiguration() {
        return AxonServerConfiguration.builder()
                .servers(axonConfiguration.server().hostname() + ":" + axonConfiguration.server().grpcPort())
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
        Log.infof("registering event handler %s", handler.getClass().getName());
        configurer.registerEventHandler(conf -> handler);
    }

    public void addAggregateForRegistration(Class<?> aggregateClass) {
        aggregateClasses.add(aggregateClass);
    }

    public void addEventhandlerForRegistration(Object eventhandler) {
        evenhandlers.add(eventhandler);
    }

    public void addQueryHandlerForRegistration(Object queryHandler) {
        queryHandlers.add(queryHandler);
    }

    @Shutdown
    void onShutdown() {
        Log.info("shutdown axon");
        if (configuration != null) {
            configuration.shutdown();
            configuration = null;
        }
    }

    @Produces
    @ApplicationScoped
    public EventGateway eventGateway() {
        return configuration.eventGateway();
    }

    @Produces
    @ApplicationScoped
    public EventBus eventBus() {
        return configuration.eventBus();
    }

    @Produces
    @ApplicationScoped
    public CommandBus commandBus() {
        return configuration.commandBus();
    }

    @Produces
    @ApplicationScoped
    public CommandGateway commandGateway() {
        return configuration.commandGateway();
    }

    @Produces
    @ApplicationScoped
    public QueryGateway queryGateway() {
        return configuration.queryGateway();
    }

    @Produces
    @ApplicationScoped
    public QueryBus queryBus() {
        return configuration.queryBus();
    }

    @Produces
    @ApplicationScoped
    public Configuration configuration() {
        return configuration;
    }

    public void addCommandhandlerForRegistration(Object commandhandler) {
        commandhandlers.add(commandhandler);
    }

    @Produces
    @Dependent
    public <T> Repository<T> repository(InjectionPoint injectionPoint) {
        Class<T> aggregateClass = aggregateClass(
                ((ParameterizedType) injectionPoint.getType()).getActualTypeArguments()[0].getTypeName());
        return configuration.repository(aggregateClass);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> aggregateClass(String typeName) {
        return (Class<T>) aggregateClasses.stream()
                .filter(clazz -> clazz.getTypeName().equals(typeName))
                .findFirst()
                .orElse(null);
    }

}
