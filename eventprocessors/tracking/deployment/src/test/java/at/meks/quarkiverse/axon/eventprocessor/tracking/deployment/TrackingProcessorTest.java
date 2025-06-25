package at.meks.quarkiverse.axon.eventprocessor.tracking.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class TrackingProcessorTest extends JavaArchiveTest {

    @Override
    protected void assertConfiguration(Map<String, EventProcessor> eventProcessors) {
        assertThat(eventProcessors)
                .containsKeys(
                        "GiftCardInMemory", "at.meks.quarkiverse.axon.shared.projection",
                        "at.meks.quarkiverse.axon.shared.projection2");

        Map<String, TrackingEventProcessor> trackingProcessors = eventProcessors.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof TrackingEventProcessor)
                .filter(e -> !e.getKey().equals("CardReturnSagaProcessor"))
                .map(entry -> Map.entry(entry.getKey(), (TrackingEventProcessor) entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(trackingProcessors)
                .containsOnlyKeys(
                        "GiftCardInMemory", "at.meks.quarkiverse.axon.shared.projection",
                        "at.meks.quarkiverse.axon.shared.projection2");

        assertTrackingConfiguration(trackingProcessors);
    }

    protected abstract void assertTrackingConfiguration(Map<String, TrackingEventProcessor> trackingEventProcessors);

}
