package at.meks.quarkiverse.axon.eventprocessor.pooled.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class PooledProcessorTest extends JavaArchiveTest {

    @Override
    protected void assertConfiguration(Map<String, EventProcessor> eventProcessors) {
        assertThat(eventProcessors)
                .containsKeys(expectedEventProcessorNames());

        Map<String, PooledStreamingEventProcessor> pooledStreamingEventProcessors = eventProcessors.entrySet().stream()
                .filter(e -> e.getValue() instanceof PooledStreamingEventProcessor)
                .filter(e -> !e.getKey().equals("CardReturnSagaProcessor"))
                .map(entry -> Map.entry(entry.getKey(), (PooledStreamingEventProcessor) entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(pooledStreamingEventProcessors)
                .containsOnlyKeys(expectedEventProcessorNames());

        assertPooledConfigurations(pooledStreamingEventProcessors);
    }

    protected void assertPooledConfigurations(Map<String, PooledStreamingEventProcessor> list) {
    }

    protected String[] expectedEventProcessorNames() {
        return new String[] { "GiftCardInMemory", "at.meks.quarkiverse.axon.shared.projection",
                "at.meks.quarkiverse.axon.shared.projection2" };
    }

}
