package io.quarkiverse.axonframework.extension.runtime;

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
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.snapshotting.SnapshotFilter;
import org.axonframework.serialization.json.JacksonSerializer;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;

@Singleton
public class AxonExtension {

    private Configuration configuration;
    @Inject
    AxonConfiguration axonConfiguration;

    public AxonExtension() {
    }

    @Startup
    void init() {
        if (configuration == null) {
            final Configurer configurer;
            Log.debug("creating the axon configuration");
            AxonServerConfiguration axonServerConfiguration = AxonServerConfiguration.builder()
                    .servers(
                            "localhost:"
                                    + axonConfiguration.server()
                                            .grpcPort())
                    .build();
            // TODO: #3 How to configure the AxonServerEventStore? What are the correct properties?
            configurer = DefaultConfigurer.defaultConfiguration()
                    .configureEventStore(conf -> AxonServerEventStore.builder()
                            .eventSerializer(conf.eventSerializer())
                            .snapshotSerializer(conf.serializer())
                            .platformConnectionManager(
                                    AxonServerConnectionManager.builder()
                                            .axonServerConfiguration(
                                                    axonServerConfiguration)
                                            .build())
                            .snapshotFilter(
                                    SnapshotFilter.allowAll())
                            .configuration(
                                    axonServerConfiguration)
                            .build())
                    .configureSerializer(conf -> JacksonSerializer.defaultSerializer())
                    .configureEventSerializer(confg -> JacksonSerializer.defaultSerializer());
            Log.info("starting axon");
            configuration = configurer.start();
        }
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
}
