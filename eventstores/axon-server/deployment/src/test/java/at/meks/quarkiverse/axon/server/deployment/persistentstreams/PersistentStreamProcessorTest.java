package at.meks.quarkiverse.axon.server.deployment.persistentstreams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.messaging.eventhandling.processing.EventProcessor;
import org.axonframework.messaging.eventhandling.processing.subscribing.SubscribingEventProcessor;
import org.junit.jupiter.api.Disabled;

import at.meks.quarkiverse.axon.server.runtime.QuarkusAxonServerConfiguration;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.axoniq.axonserver.connector.event.EventChannel;
import io.axoniq.axonserver.grpc.streams.StreamStatus;
import io.axoniq.framework.axonserver.connector.api.AxonServerConnectionManager;

@Disabled("TODO: Activate as soon as AxonFramework supports Persistent Streams again")
public abstract class PersistentStreamProcessorTest extends JavaArchiveTest {

    @Inject
    Configuration configuration;

    @Inject
    QuarkusAxonServerConfiguration axonServerConfiguration;

    @Override
    protected final void assertConfiguration(Map<String, EventProcessor> eventProcessors) {
        assertThat(eventProcessors)
                .containsKeys(
                        "GiftCardInMemory", "at.meks.quarkiverse.axon.shared.projection",
                        "at.meks.quarkiverse.axon.shared.projection2");

        Map<String, SubscribingEventProcessor> pooledStreamingEventProcessors = eventProcessors.entrySet().stream()
                .filter(e -> e.getValue() instanceof SubscribingEventProcessor)
                .filter(e -> !e.getKey().equals("CardReturnSagaProcessor"))
                .map(entry -> Map.entry(entry.getKey(), (SubscribingEventProcessor) entry.getValue()))
                // TODO: uncomment as soon as AxonFramework supports PersistentStreams again
                //                .filter(entry -> entry.getValue().getMessageSource() instanceof PersistentStreamMessageSource)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(pooledStreamingEventProcessors)
                .containsOnlyKeys(
                        "GiftCardInMemory", "at.meks.quarkiverse.axon.shared.projection",
                        "at.meks.quarkiverse.axon.shared.projection2");

        assertPersistedStreamConfigurations(pooledStreamingEventProcessors);
    }

    protected void assertPersistedStreamConfigurations(Map<String, SubscribingEventProcessor> list) {

    }

    @Override
    protected void teardown() {
        EventChannel eventChannel = configuration.getComponent(AxonServerConnectionManager.class)
                .getConnection(axonServerConfiguration.context()).eventChannel();
        List<String> streamIds = eventChannel.persistentStreams().join()
                .stream().map(StreamStatus::getStreamId)
                .toList();
        streamIds.forEach(eventChannel::deletePersistentStream);
    }
}
