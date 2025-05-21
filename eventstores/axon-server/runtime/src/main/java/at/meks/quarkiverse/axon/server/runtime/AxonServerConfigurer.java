package at.meks.quarkiverse.axon.server.runtime;

import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.event.axon.AxonServerEventStore;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.EventBusSpanFactory;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.EventstoreConfigurer;

@ApplicationScoped
public class AxonServerConfigurer implements EventstoreConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(AxonServerConfigurer.class);

    @Inject
    QuarkusAxonServerConfiguration serverConfiguration;

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    AxonServers axonSevers;

    public void configure(Configurer configurer) {
        configurer.registerComponent(AxonServerConfiguration.class,
                cfg -> axonServerConfiguration())
                .configureEventStore(this::axonserverEventStore);
    }

    private org.axonframework.axonserver.connector.AxonServerConfiguration axonServerConfiguration() {
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
        serverConfiguration.token().ifPresent(builder::token);
        return builder.build();
    }

    private EventStore axonserverEventStore(Configuration conf) {
        LOG.info("configure connection to axon server");
        return AxonServerEventStore.builder()
                .configuration(axonServerConfiguration())
                .platformConnectionManager(conf.getComponent(AxonServerConnectionManager.class))
                .defaultContext(serverConfiguration.context())
                .messageMonitor(conf.messageMonitor(AxonServerEventStore.class, "eventStore"))
                .snapshotSerializer(conf.serializer())
                .eventSerializer(conf.eventSerializer())
                .snapshotFilter(conf.snapshotFilter())
                .upcasterChain(conf.upcasterChain())
                .spanFactory(conf.getComponent(EventBusSpanFactory.class))
                .build();
    }
}
