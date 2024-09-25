package io.quarkiverse.axonframework.extension.runtime;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
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
            Log.info("starting axon");
            configuration = configurer.start();
        }
    }

    private AxonServerConfiguration axonServerConfiguration() {
        return AxonServerConfiguration.builder()
                .servers("localhost:" + axonConfiguration.server().grpcPort())
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

    public void registerAggregate(Class<?> aggregateClass) {
        aggregateClasses.add(aggregateClass);
    }
}
