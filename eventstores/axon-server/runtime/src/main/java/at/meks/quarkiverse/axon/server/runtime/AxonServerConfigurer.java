package at.meks.quarkiverse.axon.server.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.eventhandling.conversion.EventConverter;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.EventstoreConfigurer;
import io.axoniq.framework.axonserver.connector.api.AxonServerConfiguration;
import io.axoniq.framework.axonserver.connector.api.AxonServerConnectionManager;
import io.axoniq.framework.axonserver.connector.event.AggregateBasedAxonServerEventStorageEngine;

@ApplicationScoped
public class AxonServerConfigurer implements EventstoreConfigurer {

    @Inject
    QuarkusAxonServerConfiguration serverConfiguration;

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    AxonServers axonSevers;

    public void configure(EventSourcingConfigurer configurer) {
        AxonServerConfiguration axonServerConfiguration = axonServerConfiguration();
        configurer.componentRegistry(
                cr -> cr.registerComponent(AxonServerConfiguration.class, cfg -> axonServerConfiguration)
                        .registerComponent(AxonServerConnectionManager.class,
                                cfg -> axonServerConnectionManager(axonServerConfiguration)))

                .registerEventStorageEngine(config -> {
                    var connectionManager = config.getComponent(AxonServerConnectionManager.class);
                    return new AggregateBasedAxonServerEventStorageEngine(connectionManager.getConnection(),
                            config.getComponent(
                                    EventConverter.class));
                });
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
