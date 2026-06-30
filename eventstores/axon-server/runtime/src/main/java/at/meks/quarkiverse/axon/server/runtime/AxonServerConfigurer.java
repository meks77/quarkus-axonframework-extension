package at.meks.quarkiverse.axon.server.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.conversion.GeneralConverter;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.SnapshotCapableEventStorageEngine;
import org.axonframework.messaging.eventhandling.conversion.EventConverter;
import org.jspecify.annotations.NonNull;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.EventstoreConfigurer;
import at.meks.quarkiverse.axon.runtime.customizations.SnapshotStoreConfigurer;
import io.axoniq.framework.axonserver.connector.api.AxonServerConfiguration;
import io.axoniq.framework.axonserver.connector.api.AxonServerConnectionManager;
import io.axoniq.framework.axonserver.connector.event.AggregateBasedAxonServerEventStorageEngine;
import io.axoniq.framework.axonserver.connector.event.AxonServerEventStorageEngine;
import io.axoniq.framework.axonserver.connector.snapshot.AxonServerSnapshotStore;

@ApplicationScoped
public class AxonServerConfigurer implements EventstoreConfigurer {

    @Inject
    QuarkusAxonServerConfiguration serverConfiguration;

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    Instance<SnapshotStoreConfigurer> snapshotStoreConfigurer;

    @Inject
    AxonServers axonSevers;

    public void configure(EventSourcingConfigurer configurer) {
        AxonServerConfiguration axonServerConfiguration = axonServerConfiguration();
        AxonServerConnectionManager connectionManager = axonServerConnectionManager(axonServerConfiguration);
        configurer.componentRegistry(
                cr -> cr.registerComponent(AxonServerConfiguration.class, cfg -> axonServerConfiguration)
                        .registerComponent(AxonServerConnectionManager.class, cfg -> connectionManager));

        configurer.registerEventStorageEngine(
                config -> createStorageEngine(config, connectionManager, axonServerConfiguration));
    }

    /*
     * TODO snapshot store config
     * some way to configure snapshot capabilities:
     * add some config to enable it or
     * activate snapshot capabilities by analyzing if entities are annotated with @Snapshotting
     * how to deal with stores, that do not (or not by itself alone) snapshotting
     * entity annotation without snapshot store currently throws an error on start (more of a test problem)
     * can we somehow make this a warning and proceed without the desired snapshotting?
     * how to make this approach (wrapping in snapshotcapable...) work with SnapshotStoreConfigurer Interface
     * -> maybe property would be better? what are the potential use cases / permutations?
     *
     * further: snapshot trigger definition, declarative way for global config? looks to me as this is only per entity as well
     */
    private @NonNull EventStorageEngine createStorageEngine(Configuration config, AxonServerConnectionManager connectionManager,
                                                            AxonServerConfiguration axonServerConfiguration) {
        EventStorageEngine axonStorageEngine = configureStorageEngine(config, connectionManager);
        if (serverConfiguration.storageEngine() == QuarkusAxonServerConfiguration.StorageEngineType.AGGREGATE_BASED) {
            // the aggregate based store does not support snapshotting by itself and might require an additional DCB context store
            // details are unclear, therefor we disabled it for now
            return axonStorageEngine;
        }
        // we configure snapshot capable storage engine, if we're on DCB regardless of actual usage
        return new SnapshotCapableEventStorageEngine(axonStorageEngine,
                new AxonServerSnapshotStore(connectionManager.getConnection(axonServerConfiguration.getContext()),
                        config.getComponent(
                                GeneralConverter.class)));
    }

    private @NonNull EventStorageEngine configureStorageEngine(Configuration config,
            AxonServerConnectionManager connectionManager) {
        if (serverConfiguration.storageEngine() == QuarkusAxonServerConfiguration.StorageEngineType.DCB) {
            return new AxonServerEventStorageEngine(connectionManager.getConnection(), config.getComponent(
                    EventConverter.class));
        }
        return new AggregateBasedAxonServerEventStorageEngine(connectionManager.getConnection(),
                config.getComponent(
                        EventConverter.class));
    }

    private AxonServerConfiguration axonServerConfiguration() {
        AxonServerConfiguration.Builder builder = AxonServerConfiguration.builder()
                .servers(axonSevers.axonServersAsConnectionString())
                .componentName(axonConfiguration.axonApplicationName());
        if (serverConfiguration.sslTrustStore().isPresent()) {
            Path sslTrustStorePath = serverConfiguration.sslTrustStore().get();
            if (!Files.exists(sslTrustStorePath.toAbsolutePath())) {
                throw new IllegalStateException("Cannot find ssl trust store at " + sslTrustStorePath);
            }
            builder.ssl(sslTrustStorePath.toString());
        }
        if (serverConfiguration.tokenRequired() && serverConfiguration.token().isEmpty()) {
            throw new IllegalStateException("Axon server token is required but not configured");
        }
        maxGrpcMessageSize().ifPresent(builder::maxMessageSize);
        serverConfiguration.token().ifPresent(builder::token);

        AxonServerConfiguration configuration = builder.build();
        serverConfiguration.commandThreads().ifPresent(configuration::setCommandThreads);
        return configuration;
    }

    Optional<Integer> maxGrpcMessageSize() {
        var grpcMessageSize = serverConfiguration.maxMessageSize();
        return grpcMessageSize.value().map(size -> size * grpcMessageSize.unit().factor());
    }

    private static AxonServerConnectionManager axonServerConnectionManager(AxonServerConfiguration axonServerConfiguration) {
        return AxonServerConnectionManager.builder()
                .axonServerConfiguration(axonServerConfiguration)
                .build();
    }

}
