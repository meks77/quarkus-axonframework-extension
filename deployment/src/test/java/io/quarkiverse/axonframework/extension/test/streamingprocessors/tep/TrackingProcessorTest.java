package io.quarkiverse.axonframework.extension.test.streamingprocessors.tep;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;

import io.quarkiverse.axonframework.extension.test.AbstractConfigurationTest;

public abstract class TrackingProcessorTest extends AbstractConfigurationTest {

    @Override
    protected void assertConfiguration(EventProcessor eventProcessor) {
        // I don't like this kind of assertion, but I found no better way, how to validate that it is a tracking processor
        assertThat(eventProcessor).isInstanceOf(TrackingEventProcessor.class);
        assertConfiguration((TrackingEventProcessor) eventProcessor);
    }

    protected abstract void assertConfiguration(TrackingEventProcessor trackingEventProcessor);

}
