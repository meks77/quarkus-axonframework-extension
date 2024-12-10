package at.meks.quarkiverse.axon.server.runtime;

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

import at.meks.quarkiverse.axon.runtime.api.EventstoreConfigurer;
import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;

@ApplicationScoped
public class AxonServerConfigurer implements EventstoreConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(AxonServerConfigurer.class);

    @Inject
    QuarkusAxonServerConfiguration serverConfiguration;

    @Inject
    AxonConfiguration axonConfiguration;

    public void configure(Configurer configurer) {
        configurer.registerComponent(AxonServerConfiguration.class,
                cfg -> axonServerConfiguration())
                .configureEventStore(this::axonserverEventStore);
    }

    private org.axonframework.axonserver.connector.AxonServerConfiguration axonServerConfiguration() {
        return AxonServerConfiguration.builder()
                .servers(serverConfiguration.hostname() + ":" + serverConfiguration.grpcPort())
                .componentName(axonConfiguration.axonApplicationName())
                .build();
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
