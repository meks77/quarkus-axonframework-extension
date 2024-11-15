package at.meks.quarkiverse.axon.deployment.live.reloading;

import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.event.axon.AxonServerEventStore;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventBusSpanFactory;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.serialization.json.JacksonSerializer;

import io.quarkus.logging.Log;

class TestAxonFrameworkConfigurer {

    public Configurer configure(String grpcPort) {
        final Configurer configurer;
        Log.debug("creating the axon configuration");
        JacksonSerializer jacksonSerializer = JacksonSerializer.builder().build();
        configurer = DefaultConfigurer.defaultConfiguration()
                .registerComponent(AxonServerConfiguration.class,
                        cfg -> axonServerConfiguration(grpcPort))
                .configureEventStore(conf1 -> axonserverEventStore(conf1, grpcPort))
                .configureSerializer(conf -> jacksonSerializer);
        return configurer;
    }

    private AxonServerConfiguration axonServerConfiguration(String grpcPort) {
        return AxonServerConfiguration.builder()
                .servers("localhost:" + grpcPort)
                .componentName("Live-Reload-Test")
                .componentName("Unittest client")
                .build();
    }

    private EventStore axonserverEventStore(Configuration conf, String grpcPort) {
        Log.info("configure connection to axon server");
        return AxonServerEventStore.builder()
                .configuration(axonServerConfiguration(grpcPort))
                .platformConnectionManager(conf.getComponent(AxonServerConnectionManager.class))
                .defaultContext("default")
                .messageMonitor(conf.messageMonitor(AxonServerEventStore.class, "eventStore"))
                .snapshotSerializer(conf.serializer())
                .eventSerializer(conf.eventSerializer())
                .snapshotFilter(conf.snapshotFilter())
                .upcasterChain(conf.upcasterChain())
                .spanFactory(conf.getComponent(EventBusSpanFactory.class))
                .build();
    }

}
