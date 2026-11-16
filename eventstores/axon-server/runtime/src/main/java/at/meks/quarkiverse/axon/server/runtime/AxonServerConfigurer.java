package at.meks.quarkiverse.axon.server.runtime;

import static io.quarkus.arc.ComponentsProvider.LOG;

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
import org.axonframework.eventsourcing.snapshot.store.SnapshotStore;
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
    AxonServers axonServers;

    public void configure(EventSourcingConfigurer configurer) {
        AxonServerConfiguration axonServerConfiguration = axonServerConfiguration();
        configurer.componentRegistry(
                cr -> cr.registerComponent(AxonServerConfiguration.class, cfg -> axonServerConfiguration));

        configurer.registerEventStorageEngine(config -> configureStorageEngine(config, config.getComponent(
                AxonServerConnectionManager.class)));

        configureSnapshotStore(configurer, axonServerConfiguration);
    }

    private void configureSnapshotStore(EventSourcingConfigurer configurer, AxonServerConfiguration axonServerConfiguration) {
        if (snapshotStoreConfigurer.isAmbiguous()) {
            throw new IllegalStateException("More than one snapshot store configured");
        }

        if (snapshotStoreConfigurer.isResolvable()) {
            snapshotStoreConfigurer.get().configure(configurer);
        } else {
            LOG.infof("Snapshot Store not configured, using default Axon Snapshot store");
            configurer.componentRegistry(
                    registry -> registry.registerComponent(SnapshotStore.class, axonServerConfiguration.getContext(),
                            c -> new AxonServerSnapshotStore(
                                    c.getComponent(AxonServerConnectionManager.class)
                                            .getConnection(axonServerConfiguration.getContext()),
                                    c.getComponent(GeneralConverter.class))));
        }
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
                .servers(axonServers.axonServersAsConnectionString())
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

}
