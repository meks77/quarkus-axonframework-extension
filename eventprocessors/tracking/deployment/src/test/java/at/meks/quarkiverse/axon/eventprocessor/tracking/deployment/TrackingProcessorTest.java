package at.meks.quarkiverse.axon.eventprocessor.tracking.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;

import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class TrackingProcessorTest extends JavaArchiveTest {

    @Override
    protected void assertConfiguration(Map<String, EventProcessor> eventProcessors) {
        Optional<EventProcessor> eventProcessor = Optional.ofNullable(eventProcessors.get(
                "at.meks.quarkiverse.axon.shared.projection"));
        // I don't like this kind of assertion, but I found no better way, how to validate that it is a tracking processor
        assertThat(eventProcessor).get().isInstanceOf(TrackingEventProcessor.class);
        assertConfiguration((TrackingEventProcessor) eventProcessor.orElseThrow());
    }

    protected abstract void assertConfiguration(TrackingEventProcessor trackingEventProcessor);

}
