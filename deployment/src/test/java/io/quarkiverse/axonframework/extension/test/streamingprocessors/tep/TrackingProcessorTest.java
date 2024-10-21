package io.quarkiverse.axonframework.extension.test.streamingprocessors.tep;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.Test;

import io.quarkiverse.axonframework.extension.test.AbstractConfigurationTest;

public abstract class TrackingProcessorTest extends AbstractConfigurationTest {
    @Inject
    Configuration configuration;

    @Test
    final void eventprocessorIsTracking() {
        Optional<EventProcessor> eventProcessorOptional = configuration.eventProcessingConfiguration().eventProcessor(
                "io.quarkiverse.axonframework.extension.test.projection");

        // I don't like this kind of assertion, but I found no better way, how to validate that it is a tracking processor
        assertThat(eventProcessorOptional).isPresent().get()
                .isInstanceOf(TrackingEventProcessor.class);
        assertConfiguration((TrackingEventProcessor) eventProcessorOptional.get());
    }

    protected abstract void assertConfiguration(TrackingEventProcessor trackingEventProcessor);
}
